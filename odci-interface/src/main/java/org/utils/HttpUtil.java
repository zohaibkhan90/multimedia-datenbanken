package org.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
//import java.util.HashMap;
import java.util.Map;

//import org.apache.commons.httpclient.NameValuePair;


public class HttpUtil {


	public static String executeMethod(String type, String uri, Map<String, String> parameters){

		String output = null;
		try {

			for(String key: parameters.keySet()){
				uri = uri + key+"="+parameters.get(key)+"&";
			}
			
			URL url = new URL(uri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(type.toUpperCase());
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

			System.out.println("Output from Server .... \n");
			String response = "";
			while ((output = br.readLine()) != null) {
				System.out.println(output);
				response = output;
			}

			conn.disconnect();
			return response;
		  } catch (MalformedURLException e) {

			e.printStackTrace();

		  } catch (IOException e) {

			e.printStackTrace();

		  }
		return output;
	}
	
	/*
	public static String executeGet(String url, NameValuePair[] nameValuePairs) throws IOException {
		
		for(NameValuePair nameValuePair : nameValuePairs){
			url = url+nameValuePair.getName()+"="+nameValuePair.getValue()+"&";
		}
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		} else {
			System.out.println("GET request not worked");
			return null;
		}

	}

	public static String executePost(String url, NameValuePair[] nameValuePairs) throws IOException {
		URL obj = new URL(url);
		System.out.println("Hitting URL: "+url);
		String postParams = "";
		for(NameValuePair nameValuePair : nameValuePairs){
			postParams = postParams+nameValuePair.getName()+"="+nameValuePair.getValue()+"&";
		}
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("POST");

		// For POST only - START
		con.setDoOutput(true);
		OutputStream os = con.getOutputStream();
		os.write(postParams.getBytes());
		os.flush();
		os.close();
		// For POST only - END

		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);

		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		} else {
			System.out.println("POST request not worked");
			return null;
		}
	}

public static String executeDelete(String url, NameValuePair[] nameValuePairs) throws IOException {
		System.out.println(url);
		for(NameValuePair nameValuePair : nameValuePairs){
			url = url+nameValuePair.getName()+"="+nameValuePair.getValue()+"&";
		}
		URL obj = new URL(url);
		System.out.println(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("DELETE");
		int responseCode = con.getResponseCode();
		System.out.println("Delete Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		} else {
			System.out.println("Delete request not worked");
			return null;
		}

	}
	*/
}