package app.sunstreak.yourpisd.net;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import android.app.Application;
import android.graphics.Bitmap;


public class DataGrabber /*implements Parcelable*/ extends Application {

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
	JSONArray classList = null;
	int[][] gradeSummary = null;
	// Class -> Term
	//	Date[][] lastUpdated;
	int studentId = 0;
	int[] classIds;
	/**
	 * key is of the form {classIndex, termIndex}.
	 */
	Map<Integer[], JSONObject> classGrades = new HashMap<Integer[], JSONObject>();
	//	SparseArray<JSONObject> classGrades = new SparseArray<JSONObject>();
	String studentName = "";
	Bitmap studentPictureBitmap;

	int[] classMatch;

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
		classList = null;
		gradeSummary = null;
		studentId = 0;
		classIds = null;
		classGrades = new HashMap<Integer[], JSONObject>();
		studentName = "";
		studentPictureBitmap = null;
	}

	public void setData (Domain domain, String username, String password) {
		//	public DataGrabber (Domain domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;

		/*
		 * to accept expired certificate of login1.mypisd.net
		 */
		//acceptAllCertificates();
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
			throws MalformedURLException, IllegalUrlException, IOException, PISDException, InterruptedException, ExecutionException {

		String response;
		int responseCode;

		switch (domain.index) {
		case 0:	// Parent
			Object[] cookieAuth = Request.sendPost(
					domain.loginAddress, 
					"password=" + URLEncoder.encode(password,"UTF-8") + "&username=" + URLEncoder.encode(username,"UTF-8") + "&Submit=Login", 
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
		case 1: // Student
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
					//{"Content-Length","75"},
					{"Content-Type","application/x-www-form-urlencoded"},
					//{"Cookie","JSESSIONID=22DDAFA488B9D839F082FE26EFE8B38B"},
					{"Host","sso.portal.mypisd.net"},
					{"Origin","https://sso.portal.mypisd.net"},
					{"Referer","https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin"}
			};

			ArrayList<String[]> rp = new ArrayList<String[]>(java.util.Arrays.asList(requestProperties));

			String lt = Parser.portalLt(response);

			String postParams = "username=" + URLEncoder.encode(username, "UTF-8") + 
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
	 * @return boolean success
	 * @throws MalformedURLException
	 * @throws IllegalUrlException
	 * @throws IOException
	 * @throws PISDException
	 */
	public boolean loginGradebook(String userType, String uID, String email, String password) throws MalformedURLException, IllegalUrlException, IOException, PISDException {




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
			return false;
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

		response = (String) link[0];
		responseCode = (Integer) link[1];
		cookies = (ArrayList<String>) link[2];



		/*
		 * retrieves the pageUniqueID from html of link.aspx.
		 * retrieves the student id from a cookie, PinnacleWeb.StudentId=###
		 * 
		 */
		pageUniqueId = Parser.pageUniqueId(response);
		// throws PISDException
		studentId = Parser.studentIdPinnacle(cookies);

		if (pageUniqueId == null) {
			System.out.println("Some error. pageUniqueId is null");
			return false;
		}

		postParams = "{\"studentId\":\"" + studentId + "\"}";


		/*
		 * get the JSON with list of classes, terms, and reports
		 */

		ArrayList<String[]> requestProperties = new ArrayList<String[]>();

		//required for json files
		requestProperties.add(new String[] {"Content-Type", "application/json"});




		Object[] init = Request.sendPost(
				"https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/InternetViewerService.ashx/Init?PageUniqueId=" + pageUniqueId,
				cookies,
				requestProperties, 
				true, 
				postParams);

		response = (String) init[0];
		responseCode = (Integer) init[1];
		cookies = (ArrayList<String>) init[2];

		//Store the JSON.
		try {
			setClassList(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}


		return true;
	}

	public void setClassList(String json) throws JSONException {

		JSONObject j = new JSONObject(json);
		classList = j.getJSONArray("classes");
	}


	//Stuff to be implemented
	//	public String getLastUpdated() {
	//		
	//	}
	//	

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

	//	
	//	public int[] getDetailedReport( int classId, int termId ) {
	//		
	//	}

	public String getDetailedReport (int classId, int termId, int studentId) throws MalformedURLException, IllegalUrlException, IOException {


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

	/**
	 * Uses internet every time.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * 
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

			if (responseCode != 200) {
				System.out.println("Response code: " + responseCode);
			}

			/*
			 * puts averages in classList, under each term.
			 */
			Element doc = Jsoup.parse(response);
			gradeSummary = Parser.gradeSummary(doc, classList);
			studentName = Parser.studentName(doc);

			return gradeSummary;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalUrlException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int[][] getGradeSummary () {
		if (gradeSummary == null)
			try {
				loadGradeSummary();
			} catch (JSONException e) {
				return null;
			}
		return gradeSummary;
	}

	public void putClassGrade (int classIndex, int termIndex, JSONObject classGrade) {
		classGrades.put(new Integer[] {classIndex, termIndex}, classGrade);
	}

	//	public JSONObject getClassGrade (int termIndex, int classIndex) {
	//		return classGrades.get(new Integer[] {termIndex, classIndex});
	//	}

	public boolean hasClassGrade (int classIndex, int termIndex) {
		return classGrades.containsKey(new Integer[] {classIndex, termIndex});
	}

	public JSONObject getClassGrade( int classIndex, int termIndex )  {

		String html = "";

		try {
			if (classGrades.get(classIndex) != null)
				return classGrades.get(classIndex);


			int classId = getClassIds()[classIndex];
			int termId = getTermIds(classId)[termIndex];

			html = getDetailedReport(classId, termId, studentId);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalUrlException e) {
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
			classGrade = classList.getJSONObject( classIndex );

			JSONArray termGrades = Parser.detailedReport(html);
			Object[] termCategory = Parser.termCategoryGrades(html);
			JSONArray termCategoryGrades = (JSONArray) termCategory[0];

			if ((Integer)termCategory[1] != -1)
				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("average", termCategory[1]);

			classGrade.getJSONArray("terms").getJSONObject(termIndex).put("grades", termGrades);
			classGrade.getJSONArray("terms").getJSONObject(termIndex).put("categoryGrades", termCategoryGrades);
			
			classGrades.put(new Integer[] {classIndex, termIndex}, classGrade);
			return classGrade;
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}


	//	public Map<Integer,JSONObject> getAllClassGrades() throws JSONException, InterruptedException, ExecutionException, MalformedURLException, IllegalUrlException, IOException {
	//		if (classList == null)
	//			return null;
	//		if (classIds == null)
	//			getClassIds();
	////		if (classGrades == null)
	////			classGrades = classList;
	//		
	//		for (int i = 0; i < classIds.length; i++) {
	//			for (int j = 0; j < getTermIds(classIds[i]).length; j++) {
	//				getClassGrade (i , j);
	//			}
	//		}
	//		
	//		return classGrades;
	//	}

	/*
	public JSONArray getAllClassGrades () throws JSONException {

//		if (classList==null)
//			try {
//				login();
//			} catch (Exception e) {
//				e.printStackTrace();
//				return null;
//			}


		classGrades = classList;

		//makes sure that classIds is not null.
		getClassIds();
		// fetch each class
		for (int i = 0; i < classIds.length; i++) {
			int classId = classIds[i];
			int[] termIds = getTermIds(classId);
			JSONArray grades = new JSONArray();

			// fetch each term 
			for (int j = 0; j < termIds.length; j++) {
				int termId = termIds[j];
				String html = getDetailedReport(classId, termId, studentId);
				//Parse the teacher name. Only do this once per class.
				if (j==0) {
					String[] teacher = Parser.teacher(html);
					// System.out.println(Arrays.toString(teacher));
					// put the teacher and teacherEmail into the class object.
					classGrades.getJSONObject(i).put("teacher", teacher[0]);
					classGrades.getJSONObject(i).put("teacherEmail", teacher[1]);
				}

				JSONArray termGrades = Parser.detailedReport(html);
				Object[] termCategory = Parser.termCategoryGrades(html);
				JSONArray termCategoryGrades = (JSONArray) termCategory[0];

				if ((Double)termCategory[1] != -1)
					classGrades.getJSONObject(i).getJSONArray("terms").getJSONObject(j).put("average", termCategory[1]);
				classGrades.getJSONObject(i).getJSONArray("terms").getJSONObject(j).put("grades", termGrades);
				classGrades.getJSONObject(i).getJSONArray("terms").getJSONObject(j).put("categoryGrades", termCategoryGrades);
			}

		}

		Date d = Calendar.getInstance().getTime();

		// Gives a last updated time for each CLASS, not each TERM.
		lastUpdated = new Date[classIds.length][];
		for (int i = 0; i < lastUpdated.length; i++) {
			lastUpdated[i] = new Date[getTermCount(i)];
			for (int j = 0; j < lastUpdated[i].length; j++) {
				lastUpdated[i][j] = d;
			}
		}

		// Possibly do it this way? The only problem is inconsistent term count.
		// java.util.Arrays.fill(lastUpdated, d);
		return classGrades;
	}
	 */

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

	public String getStudentName() {
		return studentName;
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

	public void setClassMatch (int[] classMatch) {
		this.classMatch = classMatch;
	}

	public int[] getClassMatch () {
		return classMatch;
	}

}
