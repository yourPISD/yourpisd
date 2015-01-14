/**
 * This file is part of yourPISD.
 *
 *  yourPISD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  yourPISD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with yourPISD.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.sunstreak.yourpisd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Request {

	static String redirectLocation = "";

	public static String getRedirectLocation() {
		return redirectLocation;
	}

	public static HTTPResponse sendGet(String url) throws MalformedURLException, IOException {
		return sendGet(url, new HashSet<String>());
	}

	public static HTTPResponse sendGet(String url, boolean isSecure) throws MalformedURLException,
			IOException {
		return sendGet(url, new HashSet<String>(), null, isSecure);
	}

	public static HTTPResponse sendGet(String url, Set<String> cookies)
			throws MalformedURLException, IOException {
		return sendGet(url, cookies, null, isSecure(url));
	}

	/**
	 * returns Object[] {String response, int responseCode, Set<String> cookies}
	 */
	public static HTTPResponse sendGet(String url, Set<String> cookies,
			ArrayList<String[]> requestProperties, boolean isSecure) throws MalformedURLException,
			IOException {
		return sendRequest(url, cookies, requestProperties, isSecure, "GET", null);
	}

	public static HTTPResponse sendPost(String url, String postParams, Set<String> cookies)
			throws MalformedURLException, IOException {

		return sendPost(url, cookies, null, isSecure(url), postParams);

	}

	public static HTTPResponse sendPost(String url, String postParams, Set<String> cookies,
			ArrayList<String[]> requestProperties) throws MalformedURLException, IOException {
		return sendPost(url, cookies, requestProperties, isSecure(url), postParams);
	}

	public static HTTPResponse sendPost(String url, Set<String> cookies,
			ArrayList<String[]> requestProperties, boolean isSecure, String postParams)
			throws MalformedURLException, IOException {
		return sendRequest(url, cookies, requestProperties, isSecure, "POST", postParams);
	}

	/**
	 * returns true if begins with https, false if begins with http. Otherwise,
	 * throws IllegalUrlException.
	 */
	public static boolean isSecure(String url) throws MalformedURLException {
		if (url.substring(0, 5).equals("https"))
			return true;
		else if (url.substring(0, 4).equals("http"))
			return false;
		else
			throw new MalformedURLException("Not a valid url: " + url);
	}

	/**
	 * 
	 * @param url
	 * @param cookies
	 * @param requestProperties
	 * @param isSecure
	 * @return
	 * @throws java.io.IOException
	 * @throws Exception
	 */
	public static Object[] getBitmap(String url, Set<String> cookies,
			ArrayList<String[]> requestProperties, boolean isSecure) {
		try {
			CookieManager cm = new CookieManager();
			CookieHandler.setDefault(cm);
			CookieStore cs = cm.getCookieStore();

			URL obj = new URL(url);

			URLConnection conn = obj.openConnection();

			if (isSecure) {
				((HttpsURLConnection) conn).setRequestMethod("GET");
			} else {
				((HttpURLConnection) conn).setRequestMethod("GET");
			}

			conn.setUseCaches(false); // what does this do?

			if (requestProperties != null && requestProperties.size() > 0)
				for (String[] property : requestProperties)
					conn.addRequestProperty(property[0], property[1]);

			// Concatenates the cookies into one cookie string, seperated by
			// semicolons.
			if (cookies != null && cookies.size() > 0) {
				String cookieRequest = concatCookies(cookies);
				conn.setRequestProperty("Cookie", cookieRequest);
			}

			int responseCode;
			if (isSecure) {
				responseCode = ((HttpsURLConnection) conn).getResponseCode();
			} else {
				responseCode = ((HttpURLConnection) conn).getResponseCode();
			}

			/*
			 * BufferedReader in = new BufferedReader(new
			 * InputStreamReader(conn.getInputStream())); String inputLine;
			 * StringBuffer response = new StringBuffer();
			 * 
			 * while ((inputLine = in.readLine()) != null) {
			 * response.append(inputLine); } in.close();
			 * 
			 * // Get the response cookies
			 * //setCookies(conn.getHeaderFields().get("Set-Cookie"));
			 * setCookies();
			 */
			for (HttpCookie c : cs.getCookies()) {
				cookies.add(c.toString());
			}

			InputStream in = conn.getInputStream();
			Bitmap bmp = BitmapFactory.decodeStream(in);

			return new Object[] { bmp, responseCode, cookies };
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String concatCookies(Set<String> cookies) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> it = cookies.iterator(); it.hasNext();) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append("; ");
		}
		return sb.toString();
	}

	public static HTTPResponse sendRequest(String url, Set<String> cookies,
			ArrayList<String[]> requestProperties, boolean isSecure, String requestMethod,
			String postParams) throws MalformedURLException, IOException {
		// System.out.println(url);
		// System.out.println(postParams);

		CookieManager cm = new CookieManager();
		CookieHandler.setDefault(cm);
		CookieStore cs = cm.getCookieStore();

		final int MAX_RETRIES = 3;
		int numTries = 0;
		boolean success = false;

		URLConnection conn = null;
		int responseCode = 0;

		long startTime = System.currentTimeMillis();
		// 3 tries in order to evade EOFException. EOFException implements
		// IOException.
		// copied from
		// http://stackoverflow.com/questions/17208336/getting-java-io-eofexception-using-httpurlconnection
		while (!success && numTries < MAX_RETRIES) {

			if (numTries != 0) {
				System.out.println(numTries + " tries; this attempt took "
						+ (System.currentTimeMillis() - startTime) + "ms");
				startTime = System.currentTimeMillis();
			}

			try {

				conn = (new URL(url)).openConnection();
				conn.setReadTimeout(4 * 10000 /* milliseconds */);
				conn.setConnectTimeout(4 * 15000 /* milliseconds */);
				if (isSecure) {
					((HttpsURLConnection) conn).setRequestMethod(requestMethod);
				} else {
					((HttpURLConnection) conn).setRequestMethod(requestMethod);
				}

				conn.setUseCaches(false);

				if (requestProperties != null && requestProperties.size() > 0)
					for (String[] property : requestProperties)
						conn.addRequestProperty(property[0], property[1]);

				// conn.addRequestProperty("User-Agent", USER_AGENT);
				// conn.addRequestProperty("Content-Length", "" +
				// postParams.getBytes().length);

				// Concatenates the cookies into one cookie string, seperated by
				// semicolons.
				if (cookies != null && cookies.size() > 0) {
					String cookieRequest = concatCookies(cookies);
					conn.setRequestProperty("Cookie", cookieRequest);
				}

				if (requestMethod.equals("POST")) {
					conn.setDoOutput(true);
					conn.setDoInput(true);

					OutputStream wr = conn.getOutputStream();
					wr.write(postParams.getBytes());
					wr.flush();
				}

				if (isSecure) {
					responseCode = ((HttpsURLConnection) conn).getResponseCode();
				} else {
					responseCode = ((HttpURLConnection) conn).getResponseCode();
				}

				success = true;

				redirectLocation = conn.getHeaderField("Location");

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {

				// if (conn != null)
				// if (isSecure)
				// ((HttpsURLConnection) conn).disconnect();
				// else
				// ((HttpURLConnection)conn).disconnect();
			}
			numTries++;

			if (numTries == MAX_RETRIES) {
				System.out.println("Max retries reached. Giving up...");
				return null;
			}
		}

		System.out.println("Success! " + (System.currentTimeMillis() - startTime) + "ms "
				+ responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		for (HttpCookie c : cs.getCookies()) {
			cookies.add(c.toString());
		}

		return new HTTPResponse(response.toString(), responseCode);
	}

}
