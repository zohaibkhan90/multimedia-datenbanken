package org.restful.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.data.Message;
import org.lira.IndexService;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableAutoConfiguration
public class MultimediaServices {

//	private static final String IMAGES_DIR = IndexService.getProperties().getProperty("images_source", "/home/oracle/Desktop/image_files");
	@RequestMapping(value = "/home", method = RequestMethod.GET)
	String home() {
		return "Hello World!";
	}
	
	@RequestMapping(value = "index/listIndexes", method = RequestMethod.GET)
	String[] listIndexes(String indexName){
		try {
			return IndexService.listIndexes(indexName);
		} catch (IOException e) {
			e.printStackTrace();
			return new String[0];
		}
	}
	
	@RequestMapping(value = "index/exists", method = RequestMethod.GET)
	Boolean exists(String indexName){
		try {
			System.out.println("Checking if index "+(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName)+" exists");
			return IndexService.exists(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName);
		} catch (IOException e) {
			e.printStackTrace();
			return Boolean.FALSE;
		}
	}
	
	@RequestMapping(value = "index/createIndex", method = RequestMethod.POST)
	Message createIndex(String indexName){
		Message message = new Message();
		try {
			System.out.println("Creating index "+(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName));
			IndexService.createIndex(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName);
			message.setSuccess(true);
			message.setDescription("Index \""+indexName+"\" created successfully");
		} catch (IOException e) {
			e.printStackTrace();
			message.setSuccess(false);
			message.setDescription("Unable to create index \""+indexName+"\"");
		}
		return message;
	}
	
	@RequestMapping(value = "index/deleteIndex", method = RequestMethod.DELETE)
	Message deleteIndex(String indexName){
		Message message = new Message();
		try {
			System.out.println("Deleting index "+(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName));
			IndexService.deleteIndex(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName);
			message.setSuccess(true);
			message.setDescription("Index \""+indexName+"\" deleted successfully");
		} catch (Exception e) {
			e.printStackTrace();
			message.setSuccess(false);
			message.setDescription("Unable to delete index \""+indexName+"\"");
		}
		return message;
	}
	
	@RequestMapping(value = "index/truncateIndex", method = RequestMethod.GET)
	Message truncateIndex(String indexName){
		Message message = new Message();
		try {
			System.out.println("Truncating index "+(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName));
			IndexService.truncateIndex(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName);
			message.setSuccess(true);
			message.setDescription("Index \""+indexName+"\" truncated successfully");
		} catch (Exception e) {
			e.printStackTrace();
			message.setSuccess(false);
			message.setDescription("Unable to truncate index \""+indexName+"\"");
		}
		return message;
	}
	
	@RequestMapping(value = "index/insert", method = RequestMethod.POST)
	Message insert(String indexName, String entryName, String rid){
		Message message = new Message();
		try { 	//indexName=images_index&entryName=resources/imageA.jpg&
			System.out.println("Inserting Image " + entryName + " in index "+(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName)+" with rid = "+rid);
			IndexService.insert(entryName, IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName, rid);
			message.setSuccess(true);
			message.setDescription("Image inserted successfully in index \""+indexName+"\"");
		} catch (Exception e) {
			e.printStackTrace();
			message.setSuccess(false);
			message.setDescription("Unable to insert image in index \""+indexName+"\"");
		}
		return message;
	}
	
	@RequestMapping(value = "index/remove", method = RequestMethod.DELETE)
	Message remove(String indexName, String entryName){
		Message message = new Message();
		try { 	//indexName=images_index&entryName=resources/imageA.jpg&
			System.out.println("Removing Image " + entryName + " in index "+(IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName));
			InputStream imageToDelete = new FileInputStream(entryName);
			IndexService.remove(imageToDelete, IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName);
			message.setSuccess(true);
			message.setDescription("Image removed successfully in index \""+indexName+"\"");
		} catch (Exception e) {
			e.printStackTrace();
			message.setSuccess(false);
			message.setDescription("Unable to remove image from index \""+indexName+"\"");
		}
		return message;
	}
	
	@RequestMapping(value = "index/listSimilarEntries", method = RequestMethod.GET)
	String[] listSimilarEntries(String indexName, String entryName){
		try { 	//listSimilarEntries?indexName=images_index&entryName=resources/bird.jpg
			InputStream imageToDelete = new FileInputStream(entryName);
			return IndexService.listSimilarEntries(imageToDelete, IndexService.getProperties().getProperty("index_dir", "indexes/")+indexName);
		} catch (Exception e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(MultimediaServices.class, args);
	}

}