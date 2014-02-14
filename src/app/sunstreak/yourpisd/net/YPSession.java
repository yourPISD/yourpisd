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
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.graphics.Bitmap;

public class YPSession {

	Domain domain;
	String username;
	String password;
	String[] passthroughCredentials;
	String[] gradebookCredentials;
	String pageUniqueId;
	// 1= success. 0 = not tried; <0 = failure.
	int editureLogin = 0;
	int gradebookLogin = 0;

	ArrayList<String> cookies = new ArrayList<String>();

	String studentName = "";
	Bitmap studentPictureBitmap;

	List<Student> students = new ArrayList<Student>();
	public int studentIndex = 0;
	public boolean MULTIPLE_STUDENTS;

	public YPSession (Domain domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
	}

	public YPSession (String username, String password) {
		this.username = username;
		this.password = password;

		// Find out whether student or parent using the username.
		if (username.equals("test")) {
			this.domain = Domain.TEST;
			students = new ArrayList<Student>();
			//students = getTestStudents();
			passthroughCredentials = new String[] {"", ""};
			gradebookCredentials = new String[] {"", ""};
			MULTIPLE_STUDENTS = true;
		}
		else if (username.contains("@mypisd.net") || !username.contains("@"))
			this.domain = Domain.STUDENT;
		else
			this.domain = Domain.PARENT;
	}

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
	public void clearData () {
		domain = null;
		username = null;
		password = null;
		passthroughCredentials = null;
		gradebookCredentials = null;
		pageUniqueId = null;
		editureLogin = 0;
		gradebookLogin = 0;
		cookies = new ArrayList<String>();

		studentName = "";
		studentPictureBitmap = null;

		students = new ArrayList<Student>();
		studentIndex = 0;

	}
	 */

	/**
	 * Logs in to Editure/New Age portal. Retrieves passthroughCredentials.
	 * Precondition: username and password are defined.
	 * @throws MalformedURLException
	 * @throws IllegalUrlException
	 * @throws IOException
	 * @throws PISDException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @return 1 if success, -1 if parent failure, -2 if student failure, 0 if domain value is not 0 or 1
	 */
	public int login(/*Domain dom, String username, String password*/)
			throws MalformedURLException, IOException, InterruptedException, ExecutionException {

		String response;
		int responseCode;
		String postParams;

		switch (domain) {
		case PARENT:

			String[][] requestProperties1 = new String[][] {
					{"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"},
					{"Accept-Encoding","gzip,deflate,sdch"},
					{"Accept-Language","en-US,en;q=0.8,es;q=0.6"},
					{"Connection","keep-alive"},
					{"Content-Type","application/x-www-form-urlencoded"},
					{"Host", "parentviewer.pisd.edu"},
					{"Origin", "https://parentviewer.pisd.edu"},
					{"Referer", "https://parentviewer.pisd.edu/"},
					{"User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/30.0.1599.114 Chrome/30.0.1599.114 Safari/537.36"}
			};

			ArrayList<String[]> rp1 = new ArrayList<String[]>(java.util.Arrays.asList(requestProperties1));

			postParams = 
					"__LASTFOCUS=" +
							"&__EVENTTARGET=" +
							"&__EVENTARGUMENT=" +
							"&__VIEWSTATE=%2FwEPDwULLTEwNjY5NzA4NTBkZMM%2FuYdqyffE27bFnREF10B%2FRqD4" +
							"&__SCROLLPOSITIONX=0" +
							"&__SCROLLPOSITIONY=0" +
							"&__EVENTVALIDATION=%2FwEdAASCW34hepkNwIXSnvGxEUTlqcZt0XO7QUOibAd3ocrpayqHxD2e5zCnWBj9%2Bm7TCi0S%2BC76MEjhL0ie%2FPsBbOp%2BShjkt2W533uAqvBQcWZNXoh672M%3D" +
							"&ctl00%24ContentPlaceHolder1%24portalLogin%24UserName=" + URLEncoder.encode(username, "UTF-8") +
							"&ctl00%24ContentPlaceHolder1%24portalLogin%24Password=" + URLEncoder.encode(password, "UTF-8") +
							"&ctl00%24ContentPlaceHolder1%24portalLogin%24LoginButton=Login";
			Object[] login = Request.sendPost(
					"https://parentviewer.pisd.edu/", 
					postParams, 
					cookies,
					rp1);
			response = (String) login[0];
			responseCode = (Integer) login[1];
			cookies = (ArrayList<String>) login[2];

			postParams = "username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8");
			Object[] cookieAuth = Request.sendPost(
					"http://parent.mypisd.net/CookieAuth?domain=www.parent.mypisd.net", 
					postParams, 
					cookies);
			response = (String) cookieAuth[0];
			responseCode = (Integer) cookieAuth[1];
			cookies = (ArrayList<String>) cookieAuth[2];

			if (Parser.accessGrantedEditure(response)) {
				System.out.println("Editure access granted!");
				editureLogin = 1;


				passthroughCredentials = Parser.passthroughCredentials(response);

				return 1;
			}
			else {
				System.out.println("Bad username/password 1!");
				System.out.println(response);
				editureLogin = -1;
				return -1;
			}
		case STUDENT: 
			Object[] portalDefaultPage = Request.sendGet(
					domain.loginAddress,
					cookies);

			response = (String) portalDefaultPage[0];
			responseCode = (Integer) portalDefaultPage[1];
			cookies = (ArrayList<String>) portalDefaultPage[2];

			String[][] requestProperties = new String[][] {
					{"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"},
					{"Accept-Encoding","gzip,deflate,sdch"},
					{"Accept-Language","en-US,en;q=0.8,es;q=0.6"},
					{"Cache-Control","max-age=0"},
					{"Connection","keep-alive"},
					{"Content-Type","application/x-www-form-urlencoded"},
					{"Host","sso.portal.mypisd.net"},
					{"Origin","https://sso.portal.mypisd.net"},
					{"Referer","https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin"}
			};

			ArrayList<String[]> rp = new ArrayList<String[]>(java.util.Arrays.asList(requestProperties));

			String lt = Parser.portalLt(response);

			postParams = "username=" + URLEncoder.encode(username, "UTF-8") + 
					"&password=" + URLEncoder.encode(password, "UTF-8") + 
					"&lt=" + lt +
					"&_eventId=submit";
			try {
				Object[] portalLogin = Request.sendPost(
						domain.loginAddress,
						cookies,
						rp,
						true,
						postParams);

				response = (String) portalLogin[0];
				responseCode = (Integer) portalLogin[1];
				cookies = (ArrayList<String>) portalLogin[2];


				// weird AF way of checking for bad password.
				if (Request.getRedirectLocation() == null)
					return -1;

				Object[] ticket = Request.sendGet(
						Request.getRedirectLocation(),
						cookies);

				if (ticket == null)
					return -2;

				response = (String) ticket[0];
				responseCode = (Integer) ticket[1];
				cookies = (ArrayList<String>) ticket[2];

				passthroughCredentials = Parser.passthroughCredentials(response);
				return 1;
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
				return -2;
			}
		case TEST:
			Thread.sleep(500);
			System.out.println("Test login");
			editureLogin = 1;
			return 1;
		default:
			return 0;
		}

	}

	public int tryLoginGradebook () throws IOException {
		int loginAttempt = 0;
		int counter = 0;
		for (;counter < 7 && loginAttempt != 1; counter++) {

			try {
				// Only sleep extra if student account.
				if (domain == Domain.STUDENT) {
					System.out.println("sleeping 3.5s");
					Thread.sleep(3500);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			loginAttempt = loginGradebook(
					passthroughCredentials[0], passthroughCredentials[1], username, password);

			// Internet connection lost
			if (loginAttempt == -10)
				return -3;
		}

		// If even 7 tries was not enough and still getting NotSet.
		if (loginAttempt == -1) {
			if (domain==Domain.STUDENT)
				logout();
			return -2;
		}
		return 1;
	}

	/**
	 * Logs into Gradebook using PIV_Passthrough.aspx and receives classList (from InternetViewerService.ashx/Init).
	 * @param userType P (parent) or S (student)
	 * @param uID
	 * @param email
	 * @param password
	 * @return -10 for internet failure, -1 for other failure, 1 for success
	 * @throws MalformedURLException
	 * @throws IllegalUrlException
	 * @throws IOException
	 * @throws PISDException
	 */
	public int loginGradebook(String userType, String uID, String email, String password) throws MalformedURLException, IOException {

		if (domain == Domain.TEST) {
			try {
				Thread.sleep(500);
				System.out.println("Test login gradebook");
			} catch (InterruptedException e) {
			}
			gradebookLogin = 1;
			return 1;
		}

		/*
		ConnectivityManager connMgr = (ConnectivityManager) 
				application.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected())
			return -10;
		 */

		// commented request paramater is allowed for parent account, not allowed for student account. never required.
		String url = "https://parentviewer.pisd.edu/EP/PIV_Passthrough.aspx?action=trans&uT=" + userType + "&uID=" + uID /*+ "&submit=Login+to+Parent+Viewer"*/;
		String postParams = "password=" + password + "&username=" + email;



		Object[] passthrough = Request.sendPost(
				url, 
				postParams, 
				cookies);

		String response = (String) passthrough[0];
		int responseCode = (Integer) passthrough[1];
		cookies = (ArrayList<String>) passthrough[2];



		String[] gradebookCredentials = Parser.getGradebookCredentials(response);


		/*
		 * escapes if access not granted.
		 */
		if (Parser.accessGrantedGradebook(gradebookCredentials)) {
			System.out.println("Gradebook access granted!");
			gradebookLogin = 1;
		}
		else {
			System.out.println("Bad username/password 2!");
			gradebookLogin--;
			return -1;
		}



		postParams = "userId=" + gradebookCredentials[0] + "&password=" + gradebookCredentials[1];



		Object[] link = Request.sendPost("https://gradebook.pisd.edu/Pinnacle/Gradebook/link.aspx?target=InternetViewer",
				postParams,
				cookies);

		response = (String) link[0];
		responseCode = (Integer) link[1];
		cookies = (ArrayList<String>) link[2];


		// for teh cookiez

		Object[] defaultAspx = Request.sendPost("https://gradebook.pisd.edu/Pinnacle/Gradebook/Default.aspx",
				postParams,
				cookies);

		response = (String) defaultAspx[0];
		responseCode = (Integer) defaultAspx[1];
		cookies = (ArrayList<String>) defaultAspx[2];

		for (String[] args : Parser.parseStudents(response) ) {
			students.add(new Student(Integer.parseInt(args[0]) , args[1], this));
		}


		MULTIPLE_STUDENTS = students.size() > 1;

		for (Student st : students) {
			cookies.add("PinnacleWeb.StudentId=" + st.studentId);
		}


		/*
		 * retrieves the pageUniqueID from html of link.aspx.
		 * retrieves the student id from a cookie, PinnacleWeb.StudentId=###
		 * 
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
	 * Temporary code. In use because login1.mypisd.net has an expired certificate. with new portal website, should not be necessary.
	 */
	/*
	public static void acceptAllCertificates() {
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() { 
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
						return null; 
					} 
					public void checkClientTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) { 
					} 
					public void checkServerTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) { 
					} 
				} 
		}; 

		// Install the all-trusting trust manager 
		try { 
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom()); 
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory()); 
		} catch (Exception e) {
		} 
	}
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

	/*
	private List<Student> getTestStudents() {


<<<<<<< HEAD:src/app/sunstreak/yourpisd/net/YPSession.java
		class TestStudent extends Student{
=======
		public int[] getClassMatch () {
			return classMatch;
		}

		public double getCumulativeGPA(double oldCumulativeGPA, double numCredits)
		{
			double newNumCredits = numCredits+ 0.5* classMatch.length;
			DecimalFormat df = new DecimalFormat("#.########");
			return Double.parseDouble(df.format((getGPA()*0.5*classMatch.length
					+oldCumulativeGPA*numCredits)/newNumCredits));
		}
		public double getGPA () {
			if (classMatch == null)
				return -2;
>>>>>>> 4b3bb169fc61cee78e7ddb22c3d6234bad91b112:src/app/sunstreak/yourpisd/net/DataGrabber.java


			public TestStudent(int studentId, String studentName) {
				super(studentId, studentName);

				InputStream is = null;

				switch (studentId) {
				case 0:
					is = application.getResources().openRawResource(R.raw.student_0_class_grades);
					break;
				case 1:
					is = application.getResources().openRawResource(R.raw.student_1_class_grades);
					break;
				}

				if (is == null) {
					System.out.println("is = null");
					return;
				}


				Scanner sc = new Scanner(is).useDelimiter("\\A");
				String json = sc.hasNext() ? sc.next() : "";

				try {
					classList = new JSONArray(json);
					classGrades = new SparseArray<JSONObject>();

					for (int i = 0; i < classList.length(); i++) {
						classGrades.put(i, new JSONObject(classList.getJSONObject(i).toString()));
						//						for (int j = 0; j < classList.getJSONObject(i).getJSONArray("terms").length(); j++) {
						//							classGrades.get(i).put(j, classList.getJSONObject(i));
						//						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				classMatch = new int[] {0, 1, 2, 3, 4, 5, 6};

			}

			public void loadClassList() {

			}

			public JSONObject getClassGrade(int classIndex, int termIndex) {
				return classGrades.get(classIndex).optJSONArray("terms").optJSONObject(termIndex);
			}

			public int[][] loadGradeSummary() {
				/*
				InputStream is;

				switch (studentId) {
				case 0:
					is = getResources().openRawResource(R.raw.student_0_grade_summary);
					break;
				case 1:
					is = getResources().openRawResource(R.raw.student_1_grade_summary);
					break;
				default:
					return null;
				}

				Scanner sc = new Scanner(is);

				int[][] gradeSummary = new int[7][7];
				for (int i = 0; i < 7; i++) {
					for (int j = 0; j < 7; j++) {
						gradeSummary[i][j] = sc.nextInt();
					}
				}

				matchClasses(gradeSummary);

				return gradeSummary;

				return null;
			}

			public int[] getClassIds() {
				return new int[] {0, 1, 2, 3, 4, 5, 6};
			}

			public int[] getTermIds(int classId) throws JSONException {
				return new int[] {0, 1, 2, 3, 4, 5};
			}



			//			public int[][] getGradeSummary () {
			//				if (gradeSummary == null)
			//					loadGradeSummary();
			//
			//				return gradeSummary;
			//			}


			public Bitmap getStudentPicture() {

				switch (studentId) {
				case 0:
					return BitmapFactory.decodeResource(application.getResources(), R.drawable.student_0);
				case 1:
					return BitmapFactory.decodeResource(application.getResources(), R.drawable.student_1);
				default:
					return null;
				}
			}

		}

		List<Student> students = new ArrayList<Student>();
		students.add(new TestStudent(0, "Griffin, Stewie (0)"));
		students.add(new TestStudent(1, "Griffin, Meg (1)"));

		return students;
	}
	 */

	/*
	public void writeToFile() {
		writeDetailsToFile();
		writeDataToFile();
	}

	private void writeDetailsToFile() {
		String filename = "DATA_GRABBER_DETAILS";
		String string = domain.toString() + "\n" + username + "\n" + password;
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(string.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDataToFile() {
		String filename = "DATA_GRABBER_DATA";
		String string = "";
		for (Student st : students) {
			string += st.classGrades.toString() + "\n";
		}
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(string.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 */

	public boolean logout () throws MalformedURLException, IOException {
		Object[] logout = Request.sendGet("http://portal.mypisd.net/c/portal/logout", cookies);
		//String response = (String) logout[0];
		int responseCode = (Integer) logout[1];
		cookies = (ArrayList<String>) logout[2];
		
		return responseCode==302 || responseCode==200;
	}
	
}
