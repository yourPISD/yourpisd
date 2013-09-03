package com.sunstreaks.mypisd.net;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class Request {
	
	

	public static Object[] sendGet(String url) throws IllegalUrlException, MalformedURLException, IOException {
		return sendGet (url, new ArrayList<String>());
	}
	

	public static Object[] sendGet(String url, boolean isSecure) throws IllegalUrlException, MalformedURLException, IOException {
		return sendGet (url, new ArrayList<String>(), null, isSecure);
	}
	

	public static Object[] sendGet(String url, ArrayList<String> cookies) throws IllegalUrlException, MalformedURLException, IOException {
			return sendGet (url, cookies, null, isSecure(url));
	}
	
	/*
	 * returns Object[] {String response, int responseCode, ArrayList<String> cookies}
	 */
	public static Object[] sendGet(String url, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure) throws MalformedURLException, IOException {
		
		CookieManager cm = new CookieManager();
		CookieHandler.setDefault(cm);
		CookieStore cs = cm.getCookieStore();
		
		final static int MAX_RETRIES = 3;
		int numTries = 0;
		URLConnection conn = null;
		
		// 3 tries in order to evade EOFException. EOFException implements IOException.
		// copied from http://stackoverflow.com/questions/17208336/getting-java-io-eofexception-using-httpurlconnection
		while (numTries < MAX_RETRIES) {
			
			if (numTries != 0) {
           		LOGV(TAG, "Retry n°" + numTries);
       		} 
			
			try {
		
				conn = (new URL(url)).openConnection();
				
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				if (isSecure) {
					((HttpsURLConnection) conn).setRequestMethod("GET");
				}
				else {
					((HttpURLConnection) conn).setRequestMethod("GET");
				}
				
				
				conn.setUseCaches(false);
				
				
				if (requestProperties != null && requestProperties.size()>0)
					for (String[] property : requestProperties)
						conn.addRequestProperty(property[0], property[1]);
					
				
				// Concatenates the cookies into one cookie string, seperated by semicolons.
				if (cookies != null && cookies.size()>0) {
					String cookieRequest = "";
					for (int i = 0; i < cookies.size(); i++) {
						cookieRequest += cookies.get(i);
						if (i < cookies.size() - 1)
							cookieRequest += "; ";
					}
					conn.setRequestProperty("Cookie", cookieRequest);
				}
				
		
				
				int responseCode;
				if (isSecure) {
					responseCode = ((HttpsURLConnection) conn).getResponseCode();
				}
				else {
					responseCode = ((HttpURLConnection) conn).getResponseCode();
				}
		
			} catch (UnsupportedEncodingException e) {
				LOGV(TAG, "Unsupported encoding exception"); 
				} catch (MalformedURLException e) {
					LOGV(TAG, "Malformed URL exception"); 
				} catch (IOException e) {
					LOGV(TAG, "IO exception: " + e.toString());
					// e.printStackTrace(); 
				} finally { 
		 
					if (conn != null)
						conn.disconnect();
				} 
				numTries++;
				
				if (numTries == MAX_RETRIES)
					LOGV(TAG, "Max retries reached. Giving up..."); 
		}
		
//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);
	 
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
	 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	 
		// Get the response cookies
		//setCookies(conn.getHeaderFields().get("Set-Cookie"));
		
		// Do not clear cookies each time!
		//cookies = new ArrayList<String>();
		System.out.println("Cookie-size: " + cs.getCookies().size());
		for (HttpCookie c : cs.getCookies()) {
			cookies.add(c.toString());
		}
		
		return new Object[] {response.toString(), responseCode, cookies};
	}
	

	
	public static Object[] sendPost(String url, String postParams, ArrayList<String> cookies) throws IllegalUrlException, MalformedURLException, IOException {
		
		return sendPost (url, postParams, cookies, null, isSecure(url));
		
	}
	
	public static Object[] sendPost(String url, String postParams, ArrayList<String> cookies, ArrayList<String[]> requestProperties) throws IllegalUrlException, MalformedURLException, IOException {
		if (isSecure(url))
			return sendPost (url, postParams, cookies, requestProperties, true);
		else
			return sendPost (url, postParams, cookies, requestProperties, false);
	}
	
	public static Object[] sendPost(String url, String postParams, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure) throws MalformedURLException, IOException {
//		System.out.println(url);
//		System.out.println(postParams);
		
		CookieManager cm = new CookieManager();
		CookieHandler.setDefault(cm);
		CookieStore cs = cm.getCookieStore();
		
		final static int MAX_RETRIES = 3;
		int numTries = 0;
		URLConnection conn = null;
		
		// 3 tries in order to evade EOFException. EOFException implements IOException.
		// copied from http://stackoverflow.com/questions/17208336/getting-java-io-eofexception-using-httpurlconnection
		while (numTries < MAX_RETRIES) {
			
			if (numTries != 0) {
           		LOGV(TAG, "Retry n°" + numTries);
       		} 
			
			try {

				conn = (new URL(url)).openConnection();
				conn.setReadTimeout(10000 /* milliseconds */);
				conn.setConnectTimeout(15000 /* milliseconds */);
				if (isSecure) {
					((HttpsURLConnection) conn).setRequestMethod("POST");
				}
				else {
					((HttpURLConnection) conn).setRequestMethod("POST");
				}
				
				
				conn.setUseCaches(false);
				
				if (requestProperties != null && requestProperties.size()>0)
					for (String[] property : requestProperties)
						conn.addRequestProperty(property[0], property[1]);
				
				// Concatenates the cookies into one cookie string, seperated by semicolons.
				if (cookies != null && cookies.size()>0) {
					String cookieRequest = "";
					for (int i = 0; i < cookies.size(); i++) {
						cookieRequest += cookies.get(i);
						if (i < cookies.size() - 1)
							cookieRequest += "; ";
					}
					conn.setRequestProperty("Cookie", cookieRequest);
				}
				
				conn.setDoOutput(true);
				conn.setDoInput(true);
				
		
				
				DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
				wr.writeBytes(postParams);
				wr.flush();
				wr.close();
				
				int responseCode;
				if (isSecure) {
					responseCode = ((HttpsURLConnection) conn).getResponseCode();
				}
				else {
					responseCode = ((HttpURLConnection) conn).getResponseCode();
				}
			} catch (UnsupportedEncodingException e) {
            LOGV(TAG, "Unsupported encoding exception"); 
			} catch (MalformedURLException e) {
				LOGV(TAG, "Malformed URL exception"); 
			} catch (IOException e) {
				LOGV(TAG, "IO exception: " + e.toString());
				// e.printStackTrace(); 
			} finally { 
	 
				if (conn != null)
					conn.disconnect();
			} 
			numTries++;
			
			if (numTries == MAX_RETRIES)
				LOGV(TAG, "Max retries reached. Giving up..."); 
		}
		

//		System.out.println("\nSending 'GET' request to URL : " + url);
//		System.out.println("Response Code : " + responseCode);
	 
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();
	 
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
	 
		// Get the response cookies
		//setCookies(conn.getHeaderFields().get("Set-Cookie"));
		
		// Do not clear cookies each time!
		//cookies = new ArrayList<String>();
		System.out.println("Cookie-size: " + cs.getCookies().size());
		for (HttpCookie c : cs.getCookies()) {
			System.out.println(c.toString());
			cookies.add(c.toString());
		}
		
		return new Object[] {response.toString(), responseCode, cookies};
	}
	
	
	/*
	 * returns true if begins with https, false if begins with http. Otherwise, throws IllegalUrlException.
	 */
	public static boolean isSecure (String url) throws IllegalUrlException {
		if (url.substring(0,5).equals("https"))
			return true;
		else if (url.substring(0,4).equals("http"))
			return false;
		else
			throw new IllegalUrlException("Not a valid url: " + url);
	}
}
