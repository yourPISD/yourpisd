package com.sunstreaks.mypisd.net;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public final class DataGrabber {

	static Domain domain;
	static String username;
	static String password;
	static String[] passthroughCredentials;
	static String[] gradebookCredentials;
	static String pageUniqueId;
	// 1= success. 0 = not tried; <0 = failure.
	static int editureLogin = 0;
	static int gradebookLogin = 0;
	
	static ArrayList<String> cookies = new ArrayList<String>();
	static JSONArray classList = null;
	// Class -> Term
	static Date[][] lastUpdated;
	static int studentId = 0;
	static int[] classIds;
	static JSONArray classGrades = null;
	
	public static void main(String args[]) throws Exception  {
		long startTime = System.currentTimeMillis();
		
		String username = "sidharth.kapur.1";
		String password = "{\"pass\":0}";
		
		DataGrabber d = new DataGrabber(Domain.PLANO_WEST, username, password);
		d.login();
		
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime + "ms");
	}
	

	public DataGrabber (Domain domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
		
		/*
		 * to accept expired certificate of login1.mypisd.net
		*/
		acceptAllCertificates();
	}

	public static void login(/*Domain dom, String username, String password*/)
			throws MalformedURLException, IllegalUrlException, IOException, PISDException {
		

		
		Object[] cookieAuth = Request.sendPost(
				domain.loginAddress, 
				"password=" + URLEncoder.encode(password,"UTF-8") + "&username=" + URLEncoder.encode(username,"UTF-8") + "&Submit=Login", 
				cookies);
		
		String response = (String) cookieAuth[0];
		int responseCode = (Integer) cookieAuth[1];
		cookies = (ArrayList<String>) cookieAuth[2];
//		System.out.println(response);
		
		if (Parser.accessGrantedEditure(response)) {
			System.out.println("Editure access granted!");
			editureLogin = 1;
		}
		else {
			System.out.println("Bad username/password 1!");
			System.out.println(response);
			editureLogin = -1;
			return; /* false; */
		}

		
		
		// Go to myClasses in order to find login information for Gradebook. Sidharth will clean this up later.
		if (domain != Domain.PARENT) {
			Object[] myClasses = Request.sendGet(domain.portalAddress + "/myclasses", 	cookies);
			response = (String) myClasses[0];
			responseCode = (Integer) myClasses[1];
			cookies = (ArrayList<String>) myClasses[2];
		}
			
		passthroughCredentials = Parser.passthroughCredentials(response);
		
		/*
		boolean loginAttempt = false;
		int counter = 0;
		do {
			if (counter > 0) {
				try {
					Thread.sleep(3000);
					System.out.println("trying again");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			loginAttempt = loginGradebook(passthroughCredentials[0], passthroughCredentials[1], username, password);
			counter++;
		} while (counter < 5 && loginAttempt == false);
		return loginAttempt;
		*/
	}
	
	/*
	 * userType is P (parent) or S (student)
	 */
	public static boolean loginGradebook(String userType, String uID, String email, String password) throws MalformedURLException, IllegalUrlException, IOException, PISDException {

		// commented reques paramater is allowed for parent account, not allowed for student account. never required.
		Object[] passthrough = Request.sendPost(
				"https://parentviewer.pisd.edu/EP/PIV_Passthrough.aspx?action=trans&uT=" + userType + "&uID=" + uID /*+ "&submit=Login+to+Parent+Viewer"*/, 
				"password=" + password + "&username=" + email, 
				cookies);
		
		String response = (String) passthrough[0];
		int responseCode = (Integer) passthrough[1];
		cookies = (ArrayList<String>) passthrough[2];
//		System.out.println(response);
		

		
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
		
		
		
		String postParams = "userId=" + gradebookCredentials[0] + "&password=" + gradebookCredentials[1];
		Object[] link = Request.sendPost("https://gradebook.pisd.edu/Pinnacle/Gradebook/link.aspx?target=InternetViewer",
				postParams,
				cookies);

		response = (String) link[0];
		responseCode = (Integer) link[1];
		cookies = (ArrayList<String>) link[2];
		

		System.out.println("Cookies after link: " + cookies);
		
		// perhaps this is where we get our StudentId cookie!
		
		Object[] defaultAspx = Request.sendPost("https://gradebook.pisd.edu/Pinnacle/Gradebook/Default.aspx",
				postParams,
				cookies);

		response = (String) link[0];
		responseCode = (Integer) link[1];
		cookies = (ArrayList<String>) link[2];
		

		System.out.println("Cookies after Default.aspx: " + cookies);

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
				postParams,
				cookies,
				requestProperties, true);
		
		response = (String) init[0];
		responseCode = (Integer) init[1];
		cookies = (ArrayList<String>) init[2];

		//Store the JSON.
		try {
			setClassList(response);
		} catch (JSONException e) {
			e.printStackTrace();
		}

//		System.out.println("JSON: " + response);
		
		return true;
	}
	
	public static void setClassList(String json) throws JSONException {
		
		JSONObject j = new JSONObject(json);
		classList = j.getJSONArray("classes");
		
	}

	
	//Stuff to be implemented
//	public String getLastUpdated() {
//		
//	}
//	
	@SuppressWarnings("finally")
	public static int[] getClassIds() {
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
		} finally {
			return classIds;
		}
	}
//	
	public static int[] getTermIds( int classId ) throws JSONException {
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
	
	public static int getTermCount (int index) throws JSONException {
		return classList.getJSONObject(index).getJSONArray("terms").length();
	}
	
//	
//	public int[] getDetailedReport( int classId, int termId ) {
//		
//	}

	public static String getDetailedReport (int classId, int termId, int studentId) {
		try {
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
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalUrlException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static JSONArray getGradeSummary () throws JSONException {
		
		String classId = classList.getJSONObject(0).getString("enrollmentId");
		String termId = classList.getJSONObject(0).getJSONArray("terms").getJSONObject(0).getString("termId");
		
		try {
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
			classList = Parser.gradeSummary(response, classList);

			return classList;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalUrlException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void getClassGrades( int classIndex, int termIndex ) throws JSONException {
		
		
		int classId = getClassIds()[classIndex];
		int termId = getTermIds(classId)[termIndex];
		
		String html = getDetailedReport(classId, termId, studentId);
		
		
		//Parse the teacher name if not already there.
		try {
			classGrades.getJSONObject(classIndex).getString("teacher");
		} catch (JSONException e) {
			// Teacher was not found.
			String[] teacher = Parser.teacher(html);
			classGrades.getJSONObject(classIndex).put("teacher", teacher[0]);
			classGrades.getJSONObject(classIndex).put("teacherEmail", teacher[1]);
		}

		
		JSONArray termGrades = Parser.detailedReport(html);
		Object[] termCategory = Parser.termCategoryGrades(html);
		JSONArray termCategoryGrades = (JSONArray) termCategory[0];
		
		if ((Double)termCategory[1] != -1)
			classGrades.getJSONObject(classIndex).getJSONArray("terms").getJSONObject(termIndex).put("average", termCategory[1]);
		classGrades.getJSONObject(classIndex).getJSONArray("terms").getJSONObject(termIndex).put("grades", termGrades);
		classGrades.getJSONObject(classIndex).getJSONArray("terms").getJSONObject(termIndex).put("categoryGrades", termCategoryGrades);
		
	}
	
	
	public static JSONArray getAllClassGrades() throws JSONException {
		if (classList == null)
			return null;
		if (classIds == null)
			getClassIds();
		if (classGrades == null)
			classGrades = classList;
		
		for (int i = 0; i < classIds.length; i++) {
			for (int j = 0; j < getTermIds(classIds[i]).length; j++) {
				getClassGrades (i , j);
			}
		}
		
		return classGrades;
	}
	
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

	/*
	 * Temporary code. In use because login1.mypisd.net has an expired certificate. with new portal website, should not be necessary.
	 */
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
	
	public static void printCookies() {
		System.out.println();
		System.out.println("Cookies:");
		for (String c : cookies)
			System.out.println(c);
		System.out.println();
	}
	

	public static int getEditureLogin() {
		return editureLogin;
	}
	
	public static int getGradebookLogin() {
		return gradebookLogin;
	}
	
	public static String[] getGradebookCredentials() {
		return gradebookCredentials;
	}
	
	public static String[] getPassthroughCredentials() {
		return passthroughCredentials;
	}
	

	public static JSONArray getClassGrades () {
		return classGrades;
	}
	
}
