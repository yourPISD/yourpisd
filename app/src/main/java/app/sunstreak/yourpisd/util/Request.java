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

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Request {

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

}
