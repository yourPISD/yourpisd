package app.sunstreak.yourpisd.net;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class Request {
	
	public static final String USER_AGENT = "yourPISD/1.0 (Android " + android.os.Build.VERSION.RELEASE + ")";
	
	static URLConnection conn;
	
	public static String getRedirectLocation() {
		return conn.getHeaderField("Location");
	}

	public static Object[] sendGet(String url) throws IllegalUrlException, MalformedURLException, IOException {
		return sendGet (url, new ArrayList<String>());
	}
	

	public static Object[] sendGet(String url, boolean isSecure) throws IllegalUrlException, MalformedURLException, IOException {
		return sendGet (url, new ArrayList<String>(), null, isSecure);
	}
	

	public static Object[] sendGet(String url, ArrayList<String> cookies) throws IllegalUrlException, MalformedURLException, IOException {
			return sendGet (url, cookies, null, isSecure(url));
	}
	
	/**
	 * returns Object[] {String response, int responseCode, ArrayList<String> cookies}
	 */
	public static Object[] sendGet(String url, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure) throws MalformedURLException, IOException {
		
		CookieManager cm = new CookieManager();
		CookieHandler.setDefault(cm);
		CookieStore cs = cm.getCookieStore();
		
		final int MAX_RETRIES = 3;
		int numTries = 0;
		boolean success = false;
		
		conn = null;
		int responseCode = 0;
		
		long startTime = System.currentTimeMillis();
		// 3 tries in order to evade EOFException. EOFException implements IOException.
		// copied from http://stackoverflow.com/questions/17208336/getting-java-io-eofexception-using-httpurlconnection
		while (!success && numTries < MAX_RETRIES) {
			
			if (numTries != 0) {
           		System.out.println(numTries + " tries; this attempt took " + (System.currentTimeMillis() - startTime) + "ms");
           		startTime = System.currentTimeMillis();
       		} 
			
			try {
		
				conn = (new URL(url)).openConnection();
				// Timeout extended because of really slow Portal.
				conn.setReadTimeout(4*10000 /* milliseconds */);
				conn.setConnectTimeout(4*15000 /* milliseconds */);
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
					
				conn.addRequestProperty("User-Agent",USER_AGENT);
				
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
				
		
			
				if (isSecure) {
					responseCode = ((HttpsURLConnection) conn).getResponseCode();
				}
				else {
					responseCode = ((HttpURLConnection) conn).getResponseCode();
				}
		
				success = true;
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally { 
		 
//				if (conn != null)
//					if (isSecure)
//						((HttpsURLConnection) conn).disconnect();
//					else
//						((HttpURLConnection)conn).disconnect();
			} 
				numTries++;
				
				if (numTries == MAX_RETRIES) {
					System.out.println("Max retries reached. Giving up..."); 
					return null;
				}
		}
		
		System.out.println("Success! " + (System.currentTimeMillis() - startTime) + "ms");
		
	 
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
//		System.out.println("Cookie-size: " + cs.getCookies().size());
		for (HttpCookie c : cs.getCookies()) {
			cookies.add(c.toString());
		}
		
		return new Object[] {response.toString(), responseCode, cookies};
	}
	

	
	public static Object[] sendPost(String url, String postParams, ArrayList<String> cookies) throws IllegalUrlException, MalformedURLException, IOException {
		
		return sendPost (url, cookies, null, isSecure(url), postParams);
		
	}
	
	public static Object[] sendPost(String url, String postParams, ArrayList<String> cookies, ArrayList<String[]> requestProperties) throws IllegalUrlException, MalformedURLException, IOException {
		if (isSecure(url))
			return sendPost (url, cookies, requestProperties, true, postParams);
		else
			return sendPost (url, cookies, requestProperties, false, postParams);
	}
	
	public static Object[] sendPost(String url, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure, String postParams) throws MalformedURLException, IOException {
//		System.out.println(url);
//		System.out.println(postParams);
		
		CookieManager cm = new CookieManager();
		CookieHandler.setDefault(cm);
		CookieStore cs = cm.getCookieStore();
		
		final int MAX_RETRIES = 3;
		int numTries = 0;
		boolean success = false;
		
		conn = null;
		int responseCode = 0;
		
		long startTime = System.currentTimeMillis();
		// 3 tries in order to evade EOFException. EOFException implements IOException.
		// copied from http://stackoverflow.com/questions/17208336/getting-java-io-eofexception-using-httpurlconnection
		while (!success && numTries < MAX_RETRIES) {
			
			if (numTries != 0) {
				System.out.println(numTries + " tries; this attempt took " + (System.currentTimeMillis() - startTime) + "ms");
           		startTime = System.currentTimeMillis();
       		} 
			
			try {

				conn = (new URL(url)).openConnection();
				conn.setReadTimeout(4*10000 /* milliseconds */);
				conn.setConnectTimeout(4*15000 /* milliseconds */);
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
				
				conn.addRequestProperty("User-Agent", USER_AGENT);
				
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
				
				
				if (isSecure) {
					responseCode = ((HttpsURLConnection) conn).getResponseCode();
				}
				else {
					responseCode = ((HttpURLConnection) conn).getResponseCode();
				}
				
				
				success = true;
				
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally { 
	 
//				if (conn != null)
//					if (isSecure)
//						((HttpsURLConnection) conn).disconnect();
//					else
//						((HttpURLConnection)conn).disconnect();
			} 
			numTries++;
			
			if (numTries == MAX_RETRIES) {
				System.out.println("Max retries reached. Giving up..."); 
				return null;
			}
		}

		System.out.println("Success! " + (System.currentTimeMillis() - startTime) + "ms");


	 
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
//		System.out.println("Cookie-size: " + cs.getCookies().size());
		for (HttpCookie c : cs.getCookies()) {
//			System.out.println(c.toString());
			cookies.add(c.toString());
		}
		
		return new Object[] {response.toString(), responseCode, cookies};
	}
	
	
	/**
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
	
	
	/**
	 * 
	 * @param url
	 * @param cookies
	 * @param requestProperties
	 * @param isSecure
	 * @return
	 * @throws IOException 
	 * @throws Exception
	 */
	public static Object[] getBitmap (String url, ArrayList<String> cookies, ArrayList<String[]> requestProperties, boolean isSecure) {
		try {
			CookieManager cm = new CookieManager();
			CookieHandler.setDefault(cm);
			CookieStore cs = cm.getCookieStore();
			
			URL obj = new URL(url);
			
			URLConnection conn = obj.openConnection();
			
			if (isSecure) {
				((HttpsURLConnection) conn).setRequestMethod("GET");
			}
			else {
				((HttpURLConnection) conn).setRequestMethod("GET");
			}
			
			conn.setUseCaches(false);		//what does this do?
			
			
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
	
			
			/*
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
		 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
		 
			// Get the response cookies
			//setCookies(conn.getHeaderFields().get("Set-Cookie"));
			setCookies();
			*/
			for (HttpCookie c : cs.getCookies()) {
				cookies.add(c.toString());
			}
			
			
			InputStream in = conn.getInputStream();
			Bitmap bmp = BitmapFactory.decodeStream(in);

			
			return new Object[] {bmp, responseCode, cookies};
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
}
