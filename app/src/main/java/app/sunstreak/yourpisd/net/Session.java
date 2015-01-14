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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;

import app.sunstreak.yourpisd.util.HTTPResponse;
import app.sunstreak.yourpisd.util.Request;

public abstract class Session {

	Domain domain;
	String username;
	String password;
	String[] passthroughCredentials;
	String[] gradebookCredentials;

	String pageUniqueId;
	String viewState;
	String eventValidation;

	// 1= success. 0 = not tried; <0 = failure.
	int editureLogin = 0;
	int gradebookLogin = 0;

	Set<String> cookies = new HashSet<String>();

	String studentName = "";
	Bitmap studentPictureBitmap;

	List<Student> students = new ArrayList<Student>();
	public int studentIndex = 0;
	public boolean MULTIPLE_STUDENTS;

	public static Session createSession(String username, String password) {
		if (username.equals("test"))
			return new TestSession();
		else if (username.contains("@mypisd.net") || !username.contains("@"))
			return new StudentSession(username, password);
		else
			return new ParentSession(username, password);
	}

	/*
	 * public Session (Domain domain, String username, String password) {
	 * this.domain = domain; this.username = username; this.password = password;
	 * }
	 * 
	 * public Session (String username, String password) { this.username =
	 * username; this.password = password;
	 * 
	 * // Find out whether student or parent using the username. if
	 * (username.equals("test")) { this.domain = Domain.TEST; students = new
	 * ArrayList<Student>(); //students = getTestStudents();
	 * passthroughCredentials = new String[] {"", ""}; gradebookCredentials =
	 * new String[] {"", ""}; MULTIPLE_STUDENTS = true; } else if
	 * (username.contains("@mypisd.net") || !username.contains("@")) this.domain
	 * = Domain.STUDENT; else this.domain = Domain.PARENT; }
	 */

	public Domain getDomain() {
		return domain;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/*
	 * public void clearData () { domain = null; username = null; password =
	 * null; passthroughCredentials = null; gradebookCredentials = null;
	 * pageUniqueId = null; editureLogin = 0; gradebookLogin = 0; cookies = new
	 * ArrayList<String>();
	 * 
	 * studentName = ""; studentPictureBitmap = null;
	 * 
	 * students = new ArrayList<Student>(); studentIndex = 0;
	 * 
	 * }
	 */

	/**
	 * Logs in to Editure/New Age portal. Retrieves passthroughCredentials.
	 * Precondition: username and password are defined.
	 * 
	 * @throws java.net.MalformedURLException
	 * @throws IllegalUrlException
	 * @throws java.io.IOException
	 * @throws PISDException
	 * @throws InterruptedException
	 * @throws java.util.concurrent.ExecutionException
	 * @return 1 if success, -1 if parent failure, -2 if student failure, 0 if
	 *         domain value is not 0 or 1
	 */
	public abstract int login() throws MalformedURLException, IOException,
			InterruptedException, ExecutionException;

	public int tryLoginGradebook() throws IOException {
		int loginAttempt = 0;

		for (int counter = 0; counter < 7 && loginAttempt != 1; counter++) {

			try {
				// Only sleep extra if student account.
				if (domain == Domain.STUDENT) {
					System.out.println("sleeping 3.5s");
					Thread.sleep(3500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			loginAttempt = loginGradebook(passthroughCredentials[0],
					passthroughCredentials[1], username, password);

			// Internet connection lost
			if (loginAttempt == -10)
				return -3;
		}

		// If even 7 tries was not enough and still getting NotSet.
		if (loginAttempt == -1) {
			if (domain == Domain.STUDENT)
				logout();
			return -2;
		}
		return 1;
	}

	/**
	 * Logs into Gradebook using PIV_Passthrough.aspx and receives classList
	 * (from InternetViewerService.ashx/Init).
	 * 
	 * @param userType
	 *            P (parent) or S (student)
	 * @param uID
	 * @param email
	 * @param password
	 * @return -10 for internet failure, -1 for other failure, 1 for success
	 * @throws java.net.MalformedURLException
	 * @throws IllegalUrlException
	 * @throws java.io.IOException
	 * @throws PISDException
	 */
	public int loginGradebook(String userType, String uID, String email,
			String password) throws MalformedURLException, IOException {

		/*
		 * ConnectivityManager connMgr = (ConnectivityManager)
		 * application.getSystemService(Context.CONNECTIVITY_SERVICE);
		 * NetworkInfo networkInfo = connMgr.getActiveNetworkInfo(); if
		 * (networkInfo == null || !networkInfo.isConnected()) return -10;
		 */

		// commented request paramater is allowed for parent account, not
		// allowed for student account. never required.
		String url = "https://parentviewer.pisd.edu/EP/PIV_Passthrough.aspx?action=trans&uT="
				+ userType + "&uID=" + uID /*
											 * +
											 * "&submit=Login+to+Parent+Viewer"
											 */;
		String postParams = "password=" + password + "&username=" + email;

		HTTPResponse passthrough = Request.sendPost(url, postParams, cookies);

		String response = passthrough.getData();
		int responseCode = passthrough.getResponseCode();

		String[] gradebookCredentials = Parser
				.getGradebookCredentials(response);

		/*
		 * escapes if access not granted.
		 */
		if (Parser.accessGrantedGradebook(gradebookCredentials)) {
			System.out.println("Gradebook access granted!");
			gradebookLogin = 1;
		} else {
			System.out.println("Bad username/password 2!");
			gradebookLogin--;
			return -1;
		}

		postParams = "userId=" + gradebookCredentials[0] + "&password="
				+ gradebookCredentials[1];

		HTTPResponse link = Request
				.sendPost(
						"https://gradebook.pisd.edu/Pinnacle/Gradebook/link.aspx?target=InternetViewer",
						postParams, cookies);

		response = link.getData();
		responseCode = link.getResponseCode();

		// for teh cookiez
		HTTPResponse defaultAspx = Request.sendPost(
				"https://gradebook.pisd.edu/Pinnacle/Gradebook/Default.aspx",
				postParams, cookies);

		response = defaultAspx.getData();
		responseCode = defaultAspx.getResponseCode();

		for (String[] args : Parser.parseStudents(response)) {
			students.add(new Student(Integer.parseInt(args[0]), args[1], this));
		}

		MULTIPLE_STUDENTS = students.size() > 1;

		for (Student st : students) {
			cookies.add("PinnacleWeb.StudentId=" + st.studentId);
		}

		/*
		 * retrieves the pageUniqueID from html of link.aspx. retrieves the
		 * student id from a cookie, PinnacleWeb.StudentId=###
		 */
		pageUniqueId = Parser.pageUniqueId(response);
		// throws PISDException

		if (pageUniqueId == null) {
			System.out.println("Some error. pageUniqueId is null");
			return -1;
		}

		for (Student st : students) {
			st.loadClassList();
		}

		return 1;
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

	public int getEditureLogin() {
		return editureLogin;
	}

	public int getGradebookLogin() {
		return gradebookLogin;
	}

	public String[] getGradebookCredentials() {
		return gradebookCredentials;
	}

	public String[] getPassthroughCredentials() {
		return passthroughCredentials;
	}

	public List<Student> getStudents() {
		return students;
	}

	public Student getCurrentStudent() {
		return students.get(studentIndex);
	}

	public boolean logout() {
		return false;
	}

}
