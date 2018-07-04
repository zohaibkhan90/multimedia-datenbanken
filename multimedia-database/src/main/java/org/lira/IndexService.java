package org.lira;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.LuceneUtils.AnalyzerType;

import org.apache.lucene.store.FSDirectory;

/**
 * Simple class showing the process of indexing
 * @author zohaib
 */
/**
 * @author zohaib
 *
 */
public class IndexService {
	private static IndexWriter iw = null;
	private static String BASE_PATH;
	private static Boolean reindex = false;
	private static Properties prop = new Properties();
	private static InputStream input = null;
	private static String indexName = null;
	private static String indexDir = null;
	private static Integer NumberOfResults = 0;
	private static final String DOCUMENT_ID = "document_id";
	private static String testImage = null;
	private static AnalyzerType analyzerType = LuceneUtils.AnalyzerType.StandardAnalyzer; 
	static {
		try {
			String properties_path = System.getenv("lire.project.properties");
			if(null == properties_path)
				properties_path = "/home/oracle/Desktop/config.properties";
			System.out.println("properties_path = "+properties_path);
			input = new FileInputStream(properties_path);
			prop.load(input);
			BASE_PATH = prop.getProperty("base_path", "image.vary.jpg/");
			reindex = prop.getProperty("reindex", "false").equals("true");
			indexDir = prop.getProperty("index_dir", "indexes/");
			indexName = indexDir+prop.getProperty("index_name", "false");
			NumberOfResults = Integer.valueOf(prop.getProperty("num_of_results", "12"));
			testImage = prop.getProperty("test_image", "resources/imageA.jpg");
			System.out.println("base_path: "+BASE_PATH);
			System.out.println("reindex: "+reindex);
			System.out.println("index_name: "+indexName);
			System.out.println("num_of_results: "+NumberOfResults);
			System.out.println("index_dir: "+indexDir);
			System.out.println("test_image: "+testImage);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Properties getProperties(){
		return prop;
	}
	
	private static void setLoggerSource() throws Exception {
		System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(BASE_PATH + "logs/wrapper_stdout", true)), true));
		System.setErr(new PrintStream(new BufferedOutputStream(new FileOutputStream(BASE_PATH + "logs/wrapper_stderr", true)), true));
	}

//	public static void main(String[] args) throws Exception {
//		String searchImageName = "bird.jpg";
//		String deleteImageName = "bird6.jpeg";
//		Boolean indexExists = exists(indexName);
//		System.out.println("index "+indexName+" exists: "+indexExists);
//		listIndexes(indexDir);
//		if(reindex){
//			if(indexExists)
//				truncateIndex(indexName);
//			indexImages(BASE_PATH, indexName);
//		}
//
//		InputStream imageToSearch = new FileInputStream(BASE_PATH+searchImageName);
//		listSimilarEntries(imageToSearch, indexName);
//		System.out.println("Before adding: Max documents in index: "+indexName+" = "+getNumberOfDocuments(indexName));
//		System.out.println("Adding new Image:");
//		insert(testImage, indexName);
//		System.out.println("After adding: Max documents in index: "+indexName+" = "+getNumberOfDocuments(indexName));
//		InputStream imageToDelete = new FileInputStream(BASE_PATH+deleteImageName);
//		remove(imageToDelete, indexName);
//		Thread.sleep(10000);
//		System.out.println("After deleting: Max documents in index: "+indexName+" = "+getNumberOfDocuments(indexName));
//		truncateIndex(indexName);
//		System.out.println("Deleting Index");
//		deleteIndex(indexName);
//		System.out.println("Index Deleted");
//	}

	/**
	 * takes indexName of an index
	 * creates the index with the provided name 
	 */
	public static void createIndex(String indexName) throws IOException {
		IndexWriter iw = LuceneUtils.createIndexWriter(indexName, true, analyzerType);
		iw.close();
	}
	
	public static boolean deleteIndex(String indexName) {
		File index = new File(indexName);
		return deleteDirectory(index);
	}
	
	private static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	/**
	 * takes indexName as name of an index
	 * Deletes all entries in the provided index
	 */
	public static boolean truncateIndex(String indexName) throws IOException {
		if(null == iw || !iw.isOpen()){
			iw = LuceneUtils.createIndexWriter(indexName, false, analyzerType);			
		}
		iw.deleteAll();
		iw.close();
		System.out.println("Deleted");
		return true;
	}
	
	/**
	 * takes indexName as name of an index
	 * returns true if indexName index exists, false otherwise
	 */
	public static boolean exists(String index) throws IOException {
		return DirectoryReader.indexExists(FSDirectory.open(Paths.get(index)));
	}
	
	/**
	 * takes dirName and indexName as String
	 * adds all images from the provided directory into the provided index
	 */

	public static boolean indexImages(String dirPath, String indexName) throws IOException {
		if(null == iw || !iw.isOpen()){
			iw = LuceneUtils.createIndexWriter(indexName, false, analyzerType);	
		}
		File f = new File(dirPath);
		System.out.println("Indexing images in " + dirPath);
		if (f.exists() && f.isDirectory()) {
			// Getting all images from a directory and its sub directories.
			ArrayList<String> images = FileUtils.readFileLines(new File(dirPath), true);

			// Creating a CEDD document builder and indexing all files.
			GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);
			// Creating an Lucene IndexWriter

			// Iterating through images building the low level features
			for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
				String imageFilePath = it.next();
				System.out.println("Indexing " + imageFilePath);
				try {
					BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
					Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
					UUID uuid = UUID.randomUUID();
					document.add(new StringField(DOCUMENT_ID, uuid.toString().replace("-", ""), Field.Store.YES));
					iw.addDocument(document);
				} catch (Exception e) {
					System.err.println("Error reading image or indexing it.");
					e.printStackTrace();
				}
			}
			// closing the IndexWriter
			LuceneUtils.closeWriter(iw);
			System.out.println("Finished indexing.");
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * takes image as InputStream and indexName as String
	 * returns 12 images that has best match with the provided image in the provided index 
	 */
	public static String[] listSimilarEntries(InputStream compareImageStream, String indexName) {
		try {
			if(null == iw || !iw.isOpen()){
				iw = LuceneUtils.createIndexWriter(indexName, false, analyzerType);	
			}
			System.out.println("search");

			BufferedImage img = ImageIO.read(compareImageStream);

			IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
			ImageSearcher searcher = new GenericFastImageSearcher(NumberOfResults, CEDD.class);

			ImageSearchHits hits = searcher.search(img, ir);

			String[] results = new String[hits.length()];
			for (int i = 0; i < hits.length(); i++) {
				
				String path = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
				String fileName = path;
				System.out.println(hits.score(i) + ": \t" + fileName);
				results[i] = fileName;
			}
			return results;
		}catch(Exception ex){
			System.out.println("message: "+ex.getMessage());
			System.out.println(ex.getCause());
			ex.printStackTrace();
			return null;
		}
	}
	
	
	public static String[] listIndexes(String index) throws IOException{
		System.out.println("Printing list of indexes - start: "+index);
		String [] list = FSDirectory.listAll(Paths.get(indexDir+index));
		for(String l:list){
			System.out.println(l);
		}
		System.out.println("Printing list of indexes - end");
		return list;
	}
	
	/**
	 * takes indexName as String
	 * returns number of documents found in the provided index
	 * @throws IOException 
	 */
	private static int getNumberOfDocuments(String indexName) throws IOException{
		IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
		return ir.maxDoc() - ir.numDeletedDocs();
	}

	/**
	 * takes imagePath and indexName
	 * inserts the provided image in the provided index
	 * @throws IOException 
	 */
	public static void insert(String imagePath, String indexName, String rid) throws IOException{
		if(null == iw || !iw.isOpen()){
			iw = LuceneUtils.createIndexWriter(indexName, false, analyzerType);			
		}
		BufferedImage img = ImageIO.read(new FileInputStream(imagePath));
		GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);
		rid = rid.replace(" ", "+"); //
		Document document = globalDocumentBuilder.createDocument(img, rid);
		UUID uuid = UUID.randomUUID();
		document.add(new StringField(DOCUMENT_ID, uuid.toString().replace("-", ""), Field.Store.YES));
		iw.addDocument(document);
		iw.close();
	}
	
	/**
	 * takes image as InputStream
	 * removes the provided image from the provided index
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static boolean remove(InputStream image, String indexName) throws IOException, ParseException {
		String value = getIndexIdentifier(image, indexName);
	    String field = DOCUMENT_ID;
	    if(null == value){
	    	System.out.println("Image not found in indexes");
	    	return false;
	    }
		if(null == iw || !iw.isOpen()){
			iw = LuceneUtils.createIndexWriter(indexName, false, analyzerType);			
		}
		QueryParser parser = new QueryParser(field, new StandardAnalyzer());
	    Query query = parser.parse(value);
		iw.deleteDocuments(query);
		iw.commit();
		iw.close();
		return true;
	}
	
	/**
	 * takes image as InputStream and indexName as String
	 * searches image from indexes and returns it's identifier, returns null of image does not exist in index
	 */
	private static String getIndexIdentifier(InputStream is, String indexName){
		try {
			if(null == iw || !iw.isOpen()){
				iw = LuceneUtils.createIndexWriter(indexName, false, analyzerType);
			}
			BufferedImage img = ImageIO.read(is);
			IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
			ImageSearcher searcher = new GenericFastImageSearcher(1, CEDD.class);

			ImageSearchHits hits = searcher.search(img, ir);
			if(hits.length()>0){
				if(hits.score(0)==0){
					String identifier = ir.document(hits.documentID(0)).getValues(DOCUMENT_ID)[0];
					return identifier;					
				}
			}
			return null;
		}catch(Exception ex){
			System.out.println("message: "+ex.getMessage());
			System.out.println(ex.getCause());
			ex.printStackTrace();
			return null;
		}
	}
	
	private static Document searchByIdentifier(String indexName, String id) {
		try{
			IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
			QueryParser parser = new QueryParser(DOCUMENT_ID, new StandardAnalyzer());
			Query query = parser.parse(id);
			IndexSearcher isearcher = new IndexSearcher(ir);
			ScoreDoc[] hits = isearcher.search(query, 10).scoreDocs;

			if(hits.length>0){
				Document hitDoc = isearcher.doc(hits[0].doc);
				return hitDoc;
			} else {
				return null;
			}	
		} catch(IOException ioe){
			return null;
		} catch(ParseException pe) {
			return null;
		}
	}

}