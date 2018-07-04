package org.odci.oracle.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.utils.HttpUtil;
import oracle.CartridgeServices.ContextManager;
import oracle.CartridgeServices.InvalidKeyException;
import oracle.ODCI.ODCIRidList;
import oracle.jdbc.OracleBfile;
import oracle.jdbc.OracleDriver;
import oracle.sql.CustomDatum; 
import oracle.sql.CustomDatumFactory;
import oracle.sql.Datum; 
import oracle.sql.STRUCT; 
import oracle.jdbc.driver.OracleConnection; 
import oracle.sql.StructDescriptor; 

public class ODCIIndex implements CustomDatum, CustomDatumFactory {

	
	/**
	 * BigDecimal constant for SUCCESS
	 */
	final static BigDecimal SUCCESS = new BigDecimal("0");
	
	/**
	 * BigDecimal constant for ERROR
	 */
	final static BigDecimal ERROR = new BigDecimal("1");

	/**
	 * BASE_URL where restful services are available to interact with LIRE
	 */
	private static final String BASE_URL = "http://localhost:8090/index";
	
	/**
	 * BASE_PATH where we can create log folder to save application logs
	 */
	private static String BASE_PATH = "/home/oracle/Desktop/ODCI/";
	
	/**
	 * Map to put dba_directories, so that we don't have to query dba_directories each time we need a value of directory Alias
	 */
	private static Map<String, String> directories = new HashMap<String, String>();

	
	/**
	 * Key to be used by context manager to store and retrieve similarity results 
	 */
	private BigDecimal key;
	
	public BigDecimal getKey() {
		return key;
	}

	public void setKey(BigDecimal key) {
		this.key = key;
	}

	/**
	 * instance of ODCIIndex class to be returned by the getFactory() method and used to create new ODCIIndex objects 
	 */
	static final ODCIIndex _ODCIIndexFactory = new ODCIIndex(null);

	/**
	 * @return an empty ODCIIndex object that you can use to create new ODCIIndex objects. 
	 */
	public static CustomDatumFactory getFactory() 
	{ 
		return _ODCIIndexFactory; 
	}

	/**
	 * default constructor
	 */
	public ODCIIndex() {}

	
	/**
	 * @param key
	 */
	public ODCIIndex(BigDecimal key) {
		this.key = key;
	}

	/**
	 * @param basePath
	 * sets System's Out stream to the provided directory for standard and errors outputs
	 */
	public static void init(String basePath) {
		try {
			new File(basePath+"/logs").mkdirs();
			System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(basePath + "logs/wrapper_stdout", true)), true));
			System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(basePath + "logs/wrapper_stderr", true)), true));			
		} catch(Exception ex){
			ex.printStackTrace();
		}
	}

	/**
	 * @param dirAlias
	 * @return path for the dirAlias provided, queried from dba_directories table of oracle.
	 */
	private static String getDirectoryPath(String dirAlias){
		String path = "";
		try{
			if(directories.containsKey(dirAlias)){
				path = directories.get(dirAlias);
			} else {
				OracleDriver ora = new OracleDriver();
				Connection conn = ora.defaultConnection();
				PreparedStatement ps = conn.prepareStatement("select directory_path from dba_directories where directory_name = '"+dirAlias+"'");
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

	/**
	 * @param indexInfo
	 * @param params
	 * @param env
	 * @return BigDecimal with value 0 for Success and 1 for Failure
	 * Creates an index in LIRE
	 */
	public static BigDecimal ODCIIndexCreate(oracle.ODCI.ODCIIndexInfo indexInfo, String params, oracle.ODCI.ODCIEnv env) {
		init(BASE_PATH);
		String url = BASE_URL+"/createIndex?";
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("indexName", indexInfo.getIndexName());
			System.out.println(HttpUtil.executeMethod("Post", url, parameters));
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * @param indexInfo
	 * @param env
	 * @return BigDecimal with value 0 for Success and 1 for Failure
	 * Drops and index from LIRE
	 */
	public static BigDecimal ODCIIndexDrop(oracle.ODCI.ODCIIndexInfo indexInfo, oracle.ODCI.ODCIEnv env) {
		init(BASE_PATH);
		String url = BASE_URL+"/deleteIndex?";		
		try {
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("indexName", indexInfo.getIndexName());
			System.out.println(HttpUtil.executeMethod("Delete", url, parameters));
		} catch (Exception e) {
			e.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * @param indexInfo
	 * @param rid
	 * @param image
	 * @param evn
	 * @return BigDecimal with value 0 for Success and 1 for Failure
	 * Inserts a new image in LIRE
	 */
	public static BigDecimal ODCIIndexInsert(oracle.ODCI.ODCIIndexInfo indexInfo, String rid, OracleBfile image, oracle.ODCI.ODCIEnv evn) {
		init(BASE_PATH);
		String url = BASE_URL+"/insert?";;
		try {
			String path = getDirectoryPath(image.getDirAlias());
			System.out.println("rid from oracle: "+rid);
			System.out.print("File Name: "+image.getName());
			Map<String, String> parameters = new HashMap<String, String>();
			String imagePath = path + "/" + image.getName();
			parameters.put("indexName", indexInfo.getIndexName());
			parameters.put("entryName", imagePath);
			parameters.put("rid", rid);
			System.out.println(HttpUtil.executeMethod("Post", url, parameters));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	/**
	 * @param indexInfo
	 * @param params
	 * @param image
	 * @param evn
	 * @return BigDecimal with value 0 for Success and 1 for Failure
	 * Deletes an Image from LIRE
	 */
	public static BigDecimal ODCIIndexDelete(oracle.ODCI.ODCIIndexInfo indexInfo, String params, OracleBfile image, oracle.ODCI.ODCIEnv evn) {
		init(BASE_PATH);
		String url = BASE_URL+"/remove?";
		try {
			String path = getDirectoryPath(image.getDirAlias());
			Map<String, String> parameters = new HashMap<String, String>();
			String imagePath = path + "/" + image.getName();
			parameters.put("indexName", indexInfo.getIndexName());
			parameters.put("entryName", imagePath);
			System.out.println(HttpUtil.executeMethod("Delete", url, parameters));
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}

	
	/**
	 * @param indexInfo
	 * @param params
	 * @param imageOld
	 * @param imageNew
	 * @param evn
	 * @return BigDecimal with value 0 for Success and 1 for Failure
	 * Updates and image from LIRE
	 */
	public static BigDecimal ODCIIndexUpdate(oracle.ODCI.ODCIIndexInfo indexInfo, String params, OracleBfile imageOld, OracleBfile imageNew, oracle.ODCI.ODCIEnv evn) {
		ODCIIndexDelete(indexInfo, null, imageOld, null);
		ODCIIndexInsert(indexInfo, null, imageNew, null);
		return SUCCESS;
	}

	/**
	 * @param sctx
	 * @param ia
	 * @param op
	 * @param qi
	 * @param strt
	 * @param stop
	 * @param cmpval
	 * @param env
	 * @return BigDecimal with value 0 for Success and 1 for Failure, returns an instance of ODCIIndex on 0th index of sctx by reference
	 * Initialized search and saves results in context with a new key, to be used later by ODCIIndexFetch method
	 */
	public static BigDecimal ODCIIndexStart(ODCIIndex[] sctx, oracle.ODCI.ODCIIndexInfo ia, oracle.ODCI.ODCIPredInfo op, 
			oracle.ODCI.ODCIQueryInfo qi, java.math.BigDecimal strt, java.math.BigDecimal stop,
			java.lang.String cmpval, oracle.ODCI.ODCIEnv env) {
		init(BASE_PATH);
		String url = BASE_URL+"/listSimilarEntries?";
		try{
			String result[] = null;
			Map<String, String> parameters = new HashMap<String, String>();
			parameters.put("indexName", ia.getIndexName());
			parameters.put("entryName", cmpval);
			String response = HttpUtil.executeMethod("Get", url, parameters);
			System.out.println("response = "+response);
			result = response.split("\",\"");
			if(null != result && result.length>0){
				result[0] = result[0].substring(2);
				result[result.length-1] = result[result.length-1].substring(0, result[result.length-1].length()-2);
			}
			LinkedList<String> resultsList = new LinkedList<String>();
			System.out.println("complete result start: \n");
			for(String rt:result){
				System.out.println(rt+"\n");
				resultsList.add(rt);
			}
			System.out.println("complete result end: \n");
			int key = ContextManager.setContext(resultsList);
			sctx[0] = new ODCIIndex(new BigDecimal(key));
		} catch (Exception ex) {
			ex.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * @param nrows
	 * @param rids
	 * @param env
	 * @return BigDecimal with value 0 for Success and 1 for Failure, returns nrows number of results in rids list by reference.  
	 * Selects nrows number of results to be returned and removes them from context, repeatedly called until all the results are fetched from context
	 */
	public BigDecimal ODCIIndexFetch(java.math.BigDecimal nrows, 
			oracle.ODCI.ODCIRidList[] rids, oracle.ODCI.ODCIEnv env) {
		init(BASE_PATH);
		System.out.println("ODCIIndexFetch "+ nrows);
		try {
			LinkedList<String> result;
			result = (LinkedList<String>) ContextManager.getContext((key.intValue()));
			LinkedList<String> response = new LinkedList<String>();
			if(nrows.intValue() > result.size()){ //if number of results are less than nrows requested, then use the results size at the size of response array of results in this fetch
				response.addAll(result);
				response.add(null); //end of results
			} else{
				for(int i =0; i< nrows.intValue(); i++){
					response.add(result.get(i));
				}
				result.removeAll(response);
			}
			System.out.print("fetch response start: \n");
			for(String resp:response){
				System.out.print(resp+"\n");
			}
			System.out.print("fetch response end: \n");
			rids[0] = new ODCIRidList(response.toArray(new String[response.size()]));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * @param env
	 * @return BigDecimal with value 0 for Success and 1 for Failure
	 * clears results from context by key
	 */
	public BigDecimal ODCIIndexClose(oracle.ODCI.ODCIEnv env) {
		init(BASE_PATH);
		try {
			ContextManager.clearContext(key.intValue());
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return ERROR;
		}
		return SUCCESS;
	}

	/**
	 * implementation of method create(Datum, int) from CustomDatumFactory interface
	 */
	public CustomDatum create(Datum d, int sqlType) throws SQLException {

		if (d == null) return null; 
		System.out.println(d); 
		Object [] attributes = ((STRUCT) d).getAttributes(); 
		System.out.println("attributes: "+attributes);
		System.out.println("attributes length: "+attributes.length);
		System.out.println("attributes length-1 element: "+attributes[attributes.length-1]);
		return new ODCIIndex((BigDecimal) attributes[0]); 
	}

	/**
	 * implementation of method toDatum(OracleConnection) from CustomDatum interface
	 */
	public Datum toDatum(OracleConnection c) throws SQLException {

		StructDescriptor sd = StructDescriptor.createDescriptor("SYS.ODCIINDEX", c); 
		System.out.println("toDatum");
		Object [] attributes = { key }; 
		return new STRUCT(sd, c, attributes); 
	}

}
