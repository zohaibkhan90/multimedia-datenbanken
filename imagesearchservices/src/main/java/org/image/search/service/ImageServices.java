package org.image.search.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

import oracle.jdbc.OracleBfile;
import oracle.jdbc.OracleResultSet;

@RestController
@EnableAutoConfiguration
public class ImageServices {

	
	private static Connection connection = null;
	private static Map<String, String> directories = new HashMap<String, String>();
	private static String temporary_dir = "/home/oracle/Desktop/temp";
	private static String temporary_image = "temp_image.jpg";
	static {
		try{
			new File(temporary_dir).mkdirs();
			Class.forName("oracle.jdbc.driver.OracleDriver");
	        connection = DriverManager.getConnection("jdbc:oracle:thin:@//localhost:1521/orcl", "sys as sysdba", "oracle");
			System.out.println("Connected to oracle!!!");
			System.out.println(connection);	
		} catch(Exception ex) {
			System.out.println("failed to connect to oracle database!");
			ex.printStackTrace();
		}
	}
	private static String getDirectoryPath(String dirAlias){
		String path = "";
		try{
			if(directories.containsKey(dirAlias)){
				path = directories.get(dirAlias);
			} else {
				PreparedStatement ps = connection.prepareStatement("select directory_path from dba_directories where directory_name = '"+dirAlias+"'");
				ResultSet rs = ps.executeQuery();
				while(rs.next()){
					path = rs.getString(1);
					System.out.println("Directory Path = "+path);
				}
				directories.put(dirAlias, path);
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return path;
	}
	
	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}
	
	@RequestMapping(value = "getSimilarityResults", method = RequestMethod.POST)
	String[] getSimilarityResults(String encodedImage) throws Exception {
		if(null == encodedImage)
			throw new Exception("Please provide encoded image file in parameter \"encodedImage\"");
		ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(encodedImage));
		BufferedImage imageToSearch = ImageIO.read(bis);
		System.out.println(imageToSearch);
		
		File outputfile = new File(temporary_dir+"/"+temporary_image);
		ImageIO.write(imageToSearch, "jpg", outputfile);
		String[] results = new String[12];
		try{
			String selectSQL = "SELECT similarity(COLUMN1, '"+temporary_dir+"/"+temporary_image+"') as sim, COLUMN1 FROM IMG_TABLE WHERE similarity(COLUMN1, '"+temporary_dir+"/"+temporary_image+"') > 0";
			PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
			OracleResultSet rs = (OracleResultSet)preparedStatement.executeQuery(selectSQL );
			int i = 0;
			while (rs.next()) {
				OracleBfile bfile =  rs.getBfile("COLUMN1");
				String basePath = getDirectoryPath(bfile.getDirAlias());
				System.out.println("basePath = "+basePath + ", File name = "+bfile.getName());

				BufferedImage img = ImageIO.read(Files.newInputStream(Paths.get(basePath +"/"+ bfile.getName())));
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				String result = Base64.getEncoder().encodeToString(baos.toByteArray());
				results[i] = result;
				i++;
			}
			System.out.println("Completed Reading");
		} catch(Exception ex){
			System.out.println("Failure!");
			ex.printStackTrace();
		}
		return results;
	}

	@RequestMapping(value = "shutdown", method = RequestMethod.GET)
	String shutdown(String encodedImage) throws Exception {
		connection.close();
		return "Connection closed";
	}
	public static void main(String[] args) throws Exception {
		SpringApplication.run(ImageServices.class, args);
	}

}