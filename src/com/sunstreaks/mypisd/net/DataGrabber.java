package com.sunstreaks.mypisd.net;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class DataGrabber {

	Domain domain;
	String username;
	String password;
	
	ArrayList<String> cookies = new ArrayList<String>();
	JSONArray classList = null;
	Date lastUpdated;
	int studentId = 0;
	String pageUniqueId;
	JSONArray classGrades = null;
	
/*
	public static void main(String args[]) throws MalformedURLException, IllegalUrlException, IOException, JSONException {
		
		
		 //to accept expired certificate of login1.mypisd.net
		
		acceptAllCertificates();
		
		long startTime = System.currentTimeMillis();
		
		String username = "sidharth.kapur.1";
		String password = "{\"pass\":0}";

		
		DataGrabber d = new DataGrabber();
//		d.login(Domain.PARENT, "poonamkapur2000@yahoo.com", "NikooMe2");
		d.login(Domain.PLANO_WEST, username, password);

		System.out.println(d.getClassGrades());

		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime + "ms");
		

	}
*/
	public DataGrabber (Domain domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
	}

	public boolean login(/*Domain dom, String username, String password*/) throws MalformedURLException, IllegalUrlException, IOException {
		

		/*
		 * to accept expired certificate of login1.mypisd.net
		*/
		acceptAllCertificates();
		
		Object[] cookieAuth = Request.sendPost(
				domain.loginAddress, 
				"password=" + URLEncoder.encode(password,"UTF-8") + "&username=" + URLEncoder.encode(username,"UTF-8") + "&Submit=Login", 
				cookies);
		
		String response = (String) cookieAuth[0];
		int responseCode = (Integer) cookieAuth[1];
		cookies = (ArrayList<String>) cookieAuth[2];
//		System.out.println(response);
		
		if (Parser.accessGrantedEditure(response))
			System.out.println("Editure access granted!");
		else {
			System.out.println("Bad username/password!");
			System.out.println(response);
			return false;
		}

		
		
		// Go to myClasses in order to find login information for Gradebook. Sidharth will clean this up later.
		if (domain != Domain.PARENT) {
			Object[] myClasses = Request.sendGet(domain.portalAddress + "/myclasses", 	cookies);
			response = (String) myClasses[0];
			responseCode = (Integer) myClasses[1];
			cookies = (ArrayList<String>) myClasses[2];
		}
			
		String[] passthroughCredentials = Parser.passthroughCredentials(response);
		
		
		boolean loginAttempt = false;
		int counter = 0;
		do {
			if (counter > 0) {
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			loginAttempt = loginGradebook(passthroughCredentials[0], passthroughCredentials[1], username, password);
			counter++;
		} while (counter < 5 && loginAttempt == false);
		return loginAttempt;
	}
	
	/*
	 * userType is P (parent) or S (student)
	 */
	public boolean loginGradebook(String userType, String uID, String email, String password) throws MalformedURLException, IllegalUrlException, IOException {

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
		if (Parser.accessGrantedGradebook(gradebookCredentials))
			System.out.println("Gradebook access granted!");
		else {
			System.out.println("Bad username/password!");
			return false;
		}
		
		
		
		String postParams = "userId=" + gradebookCredentials[0] + "&password=" + gradebookCredentials[1];
		Object[] link = Request.sendPost("https://gradebook.pisd.edu/Pinnacle/Gradebook/link.aspx?target=InternetViewer",
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
		studentId = Parser.studentIdPinnacle(cookies);
		if (studentId == -1) {
			System.out.println("Cookie parsing error. studentId is -1");
			return false;
		}
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
	
	public void setClassList(String json) throws JSONException {
		
		JSONObject j = new JSONObject(json);
		classList = j.getJSONArray("classes");
		
	}

	
	//Stuff to be implemented
//	public String getLastUpdated() {
//		
//	}
//	
	@SuppressWarnings("finally")
	public int[] getClassIds() {
		int[] classIds = null;
		
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
//	
//	public int[] getDetailedReport( int classId, int termId ) {
//		
//	}

	public String getDetailedReport (int classId, int termId, int studentId) {
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
	

	public JSONArray getClassGrades () throws JSONException {
		
		if (classList==null)
			try {
				login();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalUrlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		classGrades = classList;

		// fetch each class
		for (int i = 0; i < getClassIds().length; i++) {
			int classId = getClassIds()[i];
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
				JSONArray termCategoryGrades = Parser.termCategoryGrades(html);
				
				classGrades.getJSONObject(i).getJSONArray("terms").getJSONObject(j).put("grades", termGrades);
				classGrades.getJSONObject(i).getJSONArray("terms").getJSONObject(j).put("categoryGrades", termCategoryGrades);
			}

		}
		return classGrades;
	}


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
	
	public void printCookies() {
		System.out.println();
		System.out.println("Cookies:");
		for (String c : cookies)
			System.out.println(c);
		System.out.println();
	}
	

	
	
}
