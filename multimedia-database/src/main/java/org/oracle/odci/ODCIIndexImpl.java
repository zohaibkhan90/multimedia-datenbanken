package org.oracle.odci;

import java.io.IOException;

import org.apache.commons.httpclient.NameValuePair;
import org.lira.IndexService;
import org.utils.HttpUtil;


public class ODCIIndexImpl implements ODCIIndex {
	private static ScanContext scanContext = new ScanContext();
	private static final String BASE_URL = IndexService.getProperties().getProperty("base_url", "http://localhost:8080/index");
//	public static void main(String[] args) {
//		ODCIIndexImpl impl = new ODCIIndexImpl();
////		impl.ODCIIndexCreate("test_index");
////		impl.ODCIIndexInsert("test_index", "resources/imageA.jpg");
//		impl.ODCIIndexStart("abc", "images_index", "resources/imageA.jpg");
//		impl.ODCIIndexFetch("abc", 3);
//		impl.ODCIIndexFetch("abc", 3);
//		impl.ODCIIndexClose("abc");
//		impl.ODCIIndexFetch("abc", 3);
////		impl.ODCIIndexDrop("test_index");
////		impl.ODCIIndexUpdate("test_index", "resources/imageA.jpg", "resources/imageA.jpg");
//	}

	@Override
	public void ODCIIndexCreate(String indexName) {
		String url = BASE_URL+"/createIndex?";
		NameValuePair nameValuePairs[]=new NameValuePair[1];
		nameValuePairs[0]=new NameValuePair("indexName", indexName);
		try {
			System.out.println(HttpUtil.executePost(url, nameValuePairs));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void ODCIIndexDrop(String indexName) {
		String url = BASE_URL+"/deleteIndex?";
		NameValuePair nameValuePairs[]=new NameValuePair[1];
		nameValuePairs[0]=new NameValuePair("indexName", indexName);
		try {
			System.out.println(HttpUtil.executeDelete(url, nameValuePairs));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void ODCIIndexInsert(String indexName, String imagePath) {
		String url = BASE_URL+"/insert?";
		NameValuePair nameValuePairs[]=new NameValuePair[2];
		nameValuePairs[0]=new NameValuePair("indexName", indexName);
		nameValuePairs[1]=new NameValuePair("entryName", imagePath);
		try {
			System.out.println(HttpUtil.executePost(url, nameValuePairs));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void ODCIIndexDelete(String indexName, String imagePath) {
		String url = BASE_URL+"/remove?";
		NameValuePair nameValuePairs[]=new NameValuePair[2];
		nameValuePairs[0]=new NameValuePair("indexName", indexName);
		nameValuePairs[1]=new NameValuePair("entryName", imagePath);
		try {
			System.out.println(HttpUtil.executeDelete(url, nameValuePairs));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void ODCIIndexUpdate(String indexName, String oldImagePath, String newImagePath) {
		// TODO Auto-generated method stub
		this.ODCIIndexDelete(indexName, oldImagePath);
		this.ODCIIndexInsert(indexName, newImagePath);
	}

	@Override
	public void ODCIIndexStart(String uuid, String indexName, String inputImage) {
		// TODO Auto-generated method stub
		ContextData contextData = new ContextData();
		contextData.setInputImage(inputImage);
		contextData.setSkip(0);
		String url = BASE_URL+"/listSimilarEntries?";
		NameValuePair nameValuePairs[]=new NameValuePair[2];
		nameValuePairs[0]=new NameValuePair("indexName", indexName);
		nameValuePairs[1]=new NameValuePair("entryName", inputImage);
		try {
			String results = HttpUtil.executeGet(url, nameValuePairs);
			System.out.println(results);
			results = results.replace("[\"", "");
			results = results.replace("\"]", "");
			int i = 0;
			contextData.setSimilarityResults(new String[results.split("\",\"").length]);
			for(String result:results.split("\",\"")){
				contextData.getSimilarityResults()[i] = result;
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		scanContext.save(uuid, contextData);
		System.out.println("Session has been started");
		
	}

	@Override
	public void ODCIIndexFetch(String uuid, int n) {
		// TODO Auto-generated method stub
		ContextData contextData = scanContext.get(uuid);
		if(null != contextData) {
			for (int i = contextData.getSkip() ; i<contextData.getSkip()+n; i++ ){ //skip the previous results and return the n number of new results
				System.out.println(contextData.getSimilarityResults()[i]);
			}
			contextData.setSkip(contextData.getSkip()+n); //skip the current results to show next ones for the next time
		} else {
			System.out.println("This context has expired");
		}
	}

	@Override
	public void ODCIIndexClose(String uuid) {
		scanContext.remove(uuid);
		System.out.println("Session has been ended");
		// TODO Auto-generated method stub
		
	}

}
