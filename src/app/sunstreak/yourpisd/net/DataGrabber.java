package app.sunstreak.yourpisd.net;


import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseArray;
import app.sunstreak.yourpisd.R;

public class DataGrabber /*implements Parcelable*/ extends Application {



	public Domain getDomain() {
		return domain;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public class Student {

		public final int studentId;
		public final String name;
		JSONArray classList;
		int[] classIds;
		int[] classMatch;
		SparseArray<JSONObject> classGrades = new SparseArray<JSONObject>();
		//		Map<Integer[], JSONObject> classGrades = new HashMap<Integer[], JSONObject>();
		Bitmap studentPictureBitmap;

		public Student (int studentId, String studentName) {
			this.studentId = studentId;
			String tempName = studentName;
			name = tempName.substring(tempName.indexOf(",") + 2, tempName.indexOf("("))
					+ tempName.substring(0, tempName.indexOf(","));
		}

		public void loadClassList() throws IOException {

			String postParams = "{\"studentId\":\"" + studentId + "\"}";

			ArrayList<String[]> requestProperties = new ArrayList<String[]>();
			requestProperties.add(new String[] {"Content-Type", "application/json"});

			Object[] init = Request.sendPost(
					"https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/InternetViewerService.ashx/Init?PageUniqueId=" + pageUniqueId,
					cookies,
					requestProperties, 
					true, 
					postParams);

			String response = (String) init[0];
			int responseCode = (Integer) init[1];
			cookies = (ArrayList<String>) init[2];

			try {
				classList = (new JSONObject(response)).getJSONArray("classes");
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		public JSONArray getClassList() {
			return classList;
		}

		/**
		 * Uses internet every time. 
		 * @throws JSONException
		 */
		public int[][] loadGradeSummary () throws JSONException {
			try {
				String classId = classList.getJSONObject(0).getString("enrollmentId");
				String termId = classList.getJSONObject(0).getJSONArray("terms").getJSONObject(0).getString("termId");

				String url = "https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/GradeSummary.aspx?" + 
						"&EnrollmentId=" + 	classId + 
						"&TermId=" + termId + 
						"&ReportType=0&StudentId=" + studentId;



				Object[] summary = Request.sendGet(url,	cookies);
				String response = (String) summary[0];
				int responseCode = (Integer) summary[1];
				cookies = (ArrayList<String>) summary[2];

				if (responseCode != 200)
					System.out.println("Response code: " + responseCode);

				/*
				 * puts averages in classList, under each term.
				 */
				Element doc = Jsoup.parse(response);
				int[][] gradeSummary = Parser.gradeSummary(doc, classList);

				matchClasses(gradeSummary);

				for (int classIndex = 0; classIndex < gradeSummary.length; classIndex++) {
					int jsonIndex = classMatch[classIndex];
					for (int termIndex = 0; termIndex < gradeSummary[classIndex].length - 1; termIndex++) {
						int average = gradeSummary[classIndex][termIndex + 1];
						if (average != -1)
							classList.getJSONObject(jsonIndex).getJSONArray("terms").getJSONObject(termIndex)
							.put("average", average);
					}
				}

				// Last updated time of summary --> goes in this awkward place
				classList.getJSONObject(0).put("summaryLastUpdated", new Instant().getMillis());

				return gradeSummary;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}


		public int[] getClassIds() {
			if (classIds != null)
				return classIds;

			if (classList == null) {
				System.out.println("You didn't login!");
				return classIds;
			}
			try {
				classIds = new int[classList.length()];
				for (int i = 0; i < classList.length(); i++) {
					classIds[i] = classList.getJSONObject(i).getInt("classId");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return classIds;
		}
		//	
		public int[] getTermIds( int classId ) throws JSONException {
			for (int i = 0; i < classList.length(); i++) {
				if (classList.getJSONObject(i).getInt("classId") == classId) {
					JSONArray terms = classList.getJSONObject(i).getJSONArray("terms");
					int[] termIds = new int[terms.length()];
					for (int j = 0; j < terms.length(); j++) {
						termIds[j] = terms.getJSONObject(j).getInt("termId");
					}
					return termIds;
				}
			}
			//if class not found.
			return null;
		}

		public int getTermCount (int index) throws JSONException {
			return classList.getJSONObject(index).getJSONArray("terms").length();
		}


		private String getDetailedReport (int classId, int termId, int studentId) throws MalformedURLException, IOException {

			String url = "https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/StudentAssignments.aspx?" + 
					"&EnrollmentId=" + 	classId + 
					"&TermId=" + termId + 
					"&ReportType=0&StudentId=" + studentId;

			Object[] report = Request.sendGet(url,	cookies);
			String response = (String) report[0];
			int responseCode = (Integer) report[1];
			cookies = (ArrayList<String>) report[2];

			if (responseCode != 200) {
				System.out.println("Response code: " + responseCode);
			}
			return response;
		}

		public boolean hasGradeSummary() {
			return classList.optJSONObject(0).optLong("summaryLastUpdated", -1) != -1;
		}

		//		public int[][] getGradeSummary () {
		//
		//			if (!hasGradeSummary())
		//				try {
		//					loadGradeSummary();
		//				} catch (JSONException e) {
		//					return null;
		//				}
		//		return gradeSummary;
		//		}

		public boolean hasClassGrade (int classIndex, int termIndex) {
			return classGrades.indexOfKey(classIndex) > 0 
					&& 
					classGrades.get(classIndex).optJSONArray("terms")
					.optJSONObject(termIndex).optLong("lastUpdated", -1) != -1;
		}

		public JSONObject getClassGrade( int classIndex, int termIndex )  {

			String html = "";


			if (hasClassGrade(classIndex, termIndex))
				return classGrades.get(classIndex).optJSONArray("terms").optJSONObject(termIndex);


			try {
				int classId = getClassIds()[classIndex];
				int termId = getTermIds(classId)[termIndex];

				html = getDetailedReport(classId, termId, studentId);

			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			//Parse the teacher name if not already there.
			try {
				classList.getJSONObject(classIndex).getString("teacher");
			} catch (JSONException e) {
				// Teacher was not found.
				String[] teacher = Parser.teacher(html);
				try {
					classList.getJSONObject(classIndex).put("teacher", teacher[0]);
					classList.getJSONObject(classIndex).put("teacherEmail", teacher[1]);
				} catch (JSONException f) {
					e.printStackTrace();
				}
			}

			JSONObject classGrade; 

			try {
				classGrade = new JSONObject(classList.getJSONObject( classIndex ).toString());

				JSONArray termGrades = Parser.detailedReport(html);
				Object[] termCategory = Parser.termCategoryGrades(html);
				JSONArray termCategoryGrades = (JSONArray) termCategory[0];

				if ((Integer)termCategory[1] != -1)
					classGrade.getJSONArray("terms").getJSONObject(termIndex).put("average", termCategory[1].toString());

				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("grades", termGrades);
				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("categoryGrades", termCategoryGrades);

				Instant in = new Instant();
				//				String time = in.toString();
				//				System.out.println(time);
				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("lastUpdated", in.getMillis());
				//				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("lastUpdated", "0");

				System.out.println("cg= " + classGrade);


				if (classGrades.indexOfKey(classIndex) < 0)
					classGrades.put(classIndex, classGrade);


				//				classGrades.get(classIndex).getJSONArray("terms").put(termIndex, classGrade);
				return classGrade.getJSONArray("terms").getJSONObject(termIndex);


			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

		}

		public String getClassName (int classIndex) {
			if (classList == null)
				return "null";
			else
				try {
					return classList.getJSONObject(classIndex).getString("title");
				} catch (JSONException e) {
					e.printStackTrace();
					return "jsonException";
				}
		}
		
		public String getShortClassName (int classIndex) {
			String name = getClassName(classIndex);
			if (name.indexOf('(') != -1)
				return name.substring(0, name.indexOf('('));
			return name;
		}

		private void loadStudentPicture() {
			ArrayList<String[]> requestProperties = new ArrayList<String[]>();
			requestProperties.add(new String[] {"Content-Type", "image/jpeg"} );


			Object[] response = Request.getBitmap("https://gradebook.pisd.edu/Pinnacle/Gradebook/common/picture.ashx?studentId=" + studentId, 
					cookies,
					requestProperties,
					true);

			studentPictureBitmap = (Bitmap) response[0];
			int responseCode = (Integer) response[1];
			cookies = (ArrayList<String>) cookies;
		}

		public Bitmap getStudentPicture() {
			if (studentPictureBitmap == null)
				loadStudentPicture();

			return studentPictureBitmap;
		}

		public void matchClasses(int[][] gradeSummary) {

			getClassIds();

			//			int[][] gradeSummary = getGradeSummary();

			int classCount = gradeSummary.length;


			classMatch = new int[classCount];
			int classesMatched = 0;

			while (classesMatched < classCount) {
				for (int i = classesMatched; i < classIds.length; i++) {
					if (classIds[i] == gradeSummary[classesMatched][0]) {
						classMatch[classesMatched] = i;
						classesMatched++;
						break;
					}
				}
			}
		}



		public int[] getClassMatch () {
			return classMatch;
		}

		public double getGPA () {
			if (classMatch == null)
				return -2;

			double pointSum = 0;
			int pointCount = 0;

			for (int classIndex = 0; classIndex < classMatch.length; classIndex++) {

				int jsonIndex = classMatch[classIndex];

				double sum = 0;
				double count = 0;
				for (int termIndex = 0; termIndex < 4; termIndex++) {
					if (classList.optJSONObject(jsonIndex).optJSONArray("terms").optJSONObject(termIndex).optInt("average", -1) != -1) {
						sum += classList.optJSONObject(jsonIndex).optJSONArray("terms").optJSONObject(termIndex).optInt("average");
						count++;
					}
				}
				if (count > 0) {
					int grade = (int) Math.round (sum / count);
					// Failed class
					if (grade < 70) {
						// Do not increment pointSum because the student received a GPA of 0.
						pointCount++;
					}
					else {
						pointCount++;
						double classGPA = maxGPA(classIndex) - gpaDifference(grade);
						pointSum += classGPA;
					}
				}
			}

			return pointSum / pointCount;
		}

		public double maxGPA (int classIndex) {
			return maxGPA(getClassName(classMatch[classIndex]));
		}

		public double maxGPA (String className) {
			if (className.contains("PHYS IB SL") || className.contains("MATH STDY IB"))
				return 4.5;

			String[] split = className.split("[\\s()\\d\\/]+");

			for (int i = split.length - 1; i >= 0; i--) {
				if (split[i].equals("AP") || split[i].equals("IB"))
					return 5;
				if (split[i].equals("H") || split[i].equals("IH"))
					return 4.5;
			}
			return 4;
		}

		public double gpaDifference (int grade) {
			if (grade<= 100 & grade>= 97)
				return 0;
			if (grade >= 93)
				return 0.2;
			if (grade >= 90)
				return 0.4;
			if (grade >= 87)
				return 0.6;
			if (grade >= 83)
				return 0.8;
			if (grade >= 80)
				return 1.0;
			if (grade >= 77)
				return 1.2;
			if (grade >= 73)
				return 1.4;
			if (grade >= 71)
				return 1.6;
			if (grade == 70)
				return 2;

			// Grade below 70 or above 100
			return -1;
		}

		public int examScoreRequired (int classIndex, int gradeDesired) {
			if (classMatch==null)
				throw new RuntimeException("Class match is null!");
			try {
				double sum = 0;
				for (int i = 0; i < 3; i++) {
					sum += classList.getJSONObject(classMatch[classIndex]).getJSONArray("terms")
							.getJSONObject(i).getInt("average");
				}
				sum = ((double)gradeDesired - 0.5) * 4 - sum;
				return (int) Math.ceil(sum);
			} catch (Exception e) {
				e.printStackTrace();
				return -1;
			}
		}

	}

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

	public void setData (Domain domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
	}

	public void setData (String username, String password) {
		this.username = username;
		this.password = password;

		// Find out whether student or parent using the username.
		if (username.equals("test")) {
			this.domain = Domain.TEST;
			students = getTestStudents();
			passthroughCredentials = new String[] {"", ""};
			gradebookCredentials = new String[] {"", ""};
			MULTIPLE_STUDENTS = true;
		}
		else if (username.contains("@mypisd.net") || !username.contains("@"))
			this.domain = Domain.STUDENT;
		else
			this.domain = Domain.PARENT;
	}

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
			};

			ArrayList<String[]> rp1 = new ArrayList<String[]>(java.util.Arrays.asList(requestProperties1));

			postParams = 
					"__LASTFOCUS=" +
							"&__EVENTTARGET=" +
							"&__EVENTARGUMENT=" +
							"&__VIEWSTATE=%2FwEPDwULLTEwNjY5NzA4NTBkZMM%2FuYdqyffE27bFnREF10B%2FRqD4" +
							"&__SCROLLPOSITIONX=0" +
							"&__SCROLLPOSITIONY=0" +
							"&__EVENTVALIDATION=%2FwEWBAK6wtGnBgLEhsriDQLHoumWCgLyjYGEDNS0X%2BIS%2B22%2FGghXXv5nzic%2Bj46b" +
							"&ctl00%24ContentPlaceHolder1%24portalLogin%24UserName=" + URLEncoder.encode(username, "UTF-8") +
							"&ctl00%24ContentPlaceHolder1%24portalLogin%24Password=" + URLEncoder.encode(password, "UTF-8") +
							"&ctl00%24ContentPlaceHolder1%24portalLogin%24LoginButton=Login";
			Object[] login = Request.sendPost(
					"https://parentviewer.pisd.edu/Login.aspx", 
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

		ConnectivityManager connMgr = (ConnectivityManager) 
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isConnected())
			return -10;

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
			students.add(new Student(Integer.parseInt(args[0]) , args[1]));
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

	private List<Student> getTestStudents() {


		class TestStudent extends Student{


			public TestStudent(int studentId, String studentName) {
				super(studentId, studentName);

				InputStream is = null;

				switch (studentId) {
				case 0:
					is = getResources().openRawResource(R.raw.student_0_class_grades);
					break;
				case 1:
					is = getResources().openRawResource(R.raw.student_1_class_grades);
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
				 */
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
					return BitmapFactory.decodeResource(getResources(), R.drawable.student_0);
				case 1:
					return BitmapFactory.decodeResource(getResources(), R.drawable.student_1);
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
}
