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

package app.sunstreak.yourpisd.net;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;

import app.sunstreak.yourpisd.net.data.Student;


public class Session {
	public static final String GRADEBOOK_ROOT = "https://gradebook.pisd.edu/Pinnacle/Gradebook";
	public static final String LOGOFF = GRADEBOOK_ROOT + "/Logon.aspx?Action=Logout";
	public static final String LOGON = GRADEBOOK_ROOT + "/logon.aspx";
	public static final SimpleDateFormat FULL_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd yyyy H:mm:ss 'GMT'z", Locale.ENGLISH);
				//Example: Sat Sep 03 2016 20:31:32 GMT-0500 (Central Daylight Time)

	private final String username;
	private final String password;
	private boolean loggedIn = false;

	private final Map<String, String> cookies = new HashMap<>();
	private Date expiration;

	List<Student> students = new ArrayList<>();
	public int studentIndex = 0;
	public boolean MULTIPLE_STUDENTS;

	public static Session createSession(String username, String password) {
		return new Session(username, password);
	}

	private Session(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Loads a GET request from a specified path and query info
	 * @param path the path (beginning with a forward slash).
	 * @param params parameter information to pass as query.
	 * @return the body string, or null if error occurs.
	 * @throws IOException
	 */
	public String request(String path, Map<String, String> params) throws IOException {
		if (checkExpiration() != 1)
			return null;

		Connection conn = Jsoup.connect(Session.GRADEBOOK_ROOT + path).cookies(getCookies());
		if (params != null)
			conn.data(params);

		Connection.Response resp = conn.execute();

		if (resp.statusCode() == 200)
			return resp.body();
		else
			return null;
	}

	/**
	 * Temporary code. In use because login1.mypisd.net has an expired
	 * certificate. with new portal website, should not be necessary.
	 */
	/*
	 * public static void acceptAllCertificates() { TrustManager[] trustAllCerts
	 * = new TrustManager[]{ new X509TrustManager() { public
	 * java.security.cert.X509Certificate[] getAcceptedIssuers() { return null;
	 * } public void checkClientTrusted( java.security.cert.X509Certificate[]
	 * certs, String authType) { } public void checkServerTrusted(
	 * java.security.cert.X509Certificate[] certs, String authType) { } } };
	 * 
	 * // Install the all-trusting trust manager try { SSLContext sc =
	 * SSLContext.getInstance("SSL"); sc.init(null, trustAllCerts, new
	 * java.security.SecureRandom());
	 * HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); }
	 * catch (Exception e) { } }
	 */

	public List<Student> getStudents() {
		return students;
	}

	public Map<String, String> getCookies()
	{
		return cookies;
	}

	public Student getCurrentStudent() {
		return students.get(studentIndex);
	}

	/**
	 * Checks the expiration of the current session
	 * @return the status code (1 for good, -1 for bad password, -2 for server error).
	 * @throws IOException
     */
	public int checkExpiration() throws IOException
	{
		if (!loggedIn || new Date().after(expiration))
		{
			int status = login();
			if (status <= 0)
				return status;
		}

		Document doc = Jsoup.connect(GRADEBOOK_ROOT + "/InternetViewer/GradeSummary.aspx").cookies(cookies).get();
		//TODO: parse to see whether if we experience an error msg with connection.

		return 1;
	}


	/**
     * This function logs in the student/ parent with their current credentials to Gradebook Pinnacle.
     * It then records session IDs and expiration time via cookies.
     * Precondition: username and password are both defined.
     *
     * @return a status code (1 for no error, -1 for bad password, and -2 for server error.)
     * @throws IOException if an I/O error occurs while connecting to server.
     */
    public int login() throws IOException
    {
        final URL LOAD_URL = new URL(LOGON);
        final String USERNAME_FIELD = "ctl00$ContentPlaceHolder$Username";
        final String PASSWORD_FIELD = "ctl00$ContentPlaceHolder$Password";

        try
        {
            // Submitting form data.
            Document html = Jsoup.parse(LOAD_URL, 60000);
            FormElement form = (FormElement) html.getElementsByTag("form").get(0);
            Connection conn = form.submit();

            // Find username and password field.
            Connection.KeyVal usernameField = null;
            Connection.KeyVal passwordField = null;
            for (Connection.KeyVal field : conn.request().data())
                if (field.key().equals(USERNAME_FIELD))
                    usernameField = field;
                else if (field.key().equals(PASSWORD_FIELD))
                    passwordField = field;

            if (usernameField == null)
            {
                System.err.println("EXPECTED: Form field for username.");
                return -2;
            }

            if (passwordField == null)
            {
                System.err.println("EXPECTED: Form field for password.");
                return -2;
            }

            // Submit username and password
            usernameField.value(username);
            passwordField.value(password);
            Connection.Response resp = conn.execute();

            if (resp.url().equals(LOAD_URL))
                return -1;
            else
            {
                cookies.putAll(resp.cookies());
				updateExpirationDate();

				students.addAll(Parser.parseStudents(this, resp.body()));
				MULTIPLE_STUDENTS = students.size() > 1;

				loggedIn = true;
                return 1;
            }
        }
        catch (SocketTimeoutException e)
        {
            e.printStackTrace();
            return -2;
        }
    }

	/**
	 * Updates expiration dates for the current session.
	 */
	public void updateExpirationDate()
	{
		try {
			expiration = FULL_DATE_FORMAT.parse(cookies.get("SessionReminder"));
		} catch (ParseException e) {
			System.err.println("Unable to parse expiration time-stamp");
			e.printStackTrace();
		}
	}

	/**
	 * Logs out the user from their account, and resets cookies.
     * @return true if we successfully logged out, false for an error.
     */
    public boolean logout() {
        try {
            boolean success = Jsoup.connect(LOGOFF).timeout(60000).cookies(cookies).execute()
                    .statusCode() == 200;
            cookies.clear();
            return success;
        } catch (IOException e) {
            return false;
        }
    }
}
