package com.sunstreaks.mypisd.net;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;


public class DataGrabber implements Parcelable {

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
	JSONArray gradeSummary = null;
	// Class -> Term
//	Date[][] lastUpdated;
	int studentId = 0;
	int[] classIds;
	JSONArray classGrades = null;
	
	RequestTask mRequestTask;

	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	
    private DataGrabber(Parcel in) {
    	this.domain = Domain.values()[in.readInt()];	//in.readInt() is the index of the domain
    	this.username = in.readString();
    	this.password = in.readString();
    	this.passthroughCredentials = in.createStringArray();
    	this.gradebookCredentials = in.createStringArray();
    	this.pageUniqueId = in.readString();
    	this.editureLogin = in.readInt();
    	this.gradebookLogin = in.readInt();
    	this.cookies = in.createStringArrayList();
    	
    	try {												// recreate the json array
			this.classList = new JSONArray(in.readString());
			this.gradeSummary = new JSONArray(in.readString());
		} catch (JSONException e) {
			e.printStackTrace();
			this.classList = null;
			this.gradeSummary = null;
		}
    	
    	this.studentId = in.readInt();
    	this.classIds = in.createIntArray();
    	
    	String cg = in.readString();
    	try {												// recreate the json array
			this.classGrades = new JSONArray(cg);
		} catch (JSONException e) {
			// Should ONLY happen if the readString is null.
			System.out.println("CG= " + cg);
			System.out.println("Empty string read. Making null JSONArray classGrades.");
			this.classGrades = null;
		} catch (NullPointerException e) {
			// We maybe need a better way to deal with a null list.
			e.printStackTrace();
		}
    	
    	
    	acceptAllCertificates();
    }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(domain.index);
		dest.writeString(username);
		dest.writeString(password);
		dest.writeStringArray(passthroughCredentials);
		dest.writeStringArray(gradebookCredentials);
		dest.writeString(pageUniqueId);
		dest.writeInt(editureLogin);
		dest.writeInt(gradebookLogin);
		dest.writeStringList(cookies);
		dest.writeString(classList.toString());
		dest.writeString(gradeSummary.toString());
		dest.writeInt(studentId);
		dest.writeIntArray(classIds);
		if (classGrades != null)
			dest.writeString(classGrades.toString());
		else
			dest.writeString("");
		
	}
	
    public static final Parcelable.Creator<DataGrabber> CREATOR
    	= new Parcelable.Creator<DataGrabber>() {
    		public DataGrabber createFromParcel(Parcel in) {
    			return new DataGrabber(in);
    		}

    		public DataGrabber[] newArray(int size) {
    			return new DataGrabber[size];
    		}
    };
	

	
    	/*
	public static void main(String args[]) throws Exception  {
		long startTime = System.currentTimeMillis();
		
		String username = "sidharth.kapur.1";
		String password = "{\"pass\":0}";
		
		DataGrabber d = new DataGrabber(Domain.PLANO_WEST, username, password);
		d.login();
		
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime + "ms");
	}
	*/

	public DataGrabber (Domain domain, String username, String password) {
		this.domain = domain;
		this.username = username;
		this.password = password;
		
		/*
		 * to accept expired certificate of login1.mypisd.net
		*/
		acceptAllCertificates();
	}

	public void login(/*Domain dom, String username, String password*/)
			throws MalformedURLException, IllegalUrlException, IOException, PISDException, InterruptedException, ExecutionException {
		
//		mRequestTask = new RequestTask();
//		mRequestTask.execute(
//				"POST",					// Request Type
//				domain.loginAddress,	// String url
//				cookies,					// ArrayList<String> cookies
//				null,					// ArrayList<String> requestProperties
//				"password=" + URLEncoder.encode(password,"UTF-8") + "&username=" + URLEncoder.encode(username,"UTF-8") + "&Submit=Login"
//				);
//		Object[] cookieAuth = (Object[]) mRequestTask.get();		
		
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
	public boolean loginGradebook(String userType, String uID, String email, String password) throws MalformedURLException, IllegalUrlException, IOException, PISDException, InterruptedException, ExecutionException {

		
//		mRequestTask = new RequestTask();
//		mRequestTask.execute(
//				"POST",											// Request Type
//				"https://parentviewer.pisd.edu/EP/PIV_Passthrough.aspx?action=trans&uT=" + userType + "&uID=" + uID,	
//																// String url
//				cookies,										// ArrayList<String> cookies
//				null,											// ArrayList<String> requestProperties
//				"password=" + password + "&username=" + email	// String postParams
//				);
//		Object[] passthrough = (Object[]) mRequestTask.get();	
		
		// commented request paramater is allowed for parent account, not allowed for student account. never required.
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
		
//		mRequestTask = new RequestTask();
//		mRequestTask.execute(
//				"POST",					// Request Type
//				"https://gradebook.pisd.edu/Pinnacle/Gradebook/link.aspx?target=InternetViewer",	
//										// String url
//				cookies,				// ArrayList<String> cookies
//				null,					// ArrayList<String> requestProperties
//				postParams				// String postParams
//				);
//		Object[] link = (Object[]) mRequestTask.get();	
		
		Object[] link = Request.sendPost("https://gradebook.pisd.edu/Pinnacle/Gradebook/link.aspx?target=InternetViewer",
				postParams,
				cookies);

		response = (String) link[0];
		responseCode = (Integer) link[1];
		cookies = (ArrayList<String>) link[2];
		

		System.out.println("Cookies after link: " + cookies);
		
		// perhaps this is where we get our StudentId cookie!
		
		
//		mRequestTask = new RequestTask();
//		mRequestTask.execute(
//				"POST",					// Request Type
//				"https://gradebook.pisd.edu/Pinnacle/Gradebook/Default.aspx",	
//										// String url
//				cookies,				// ArrayList<String> cookies
//				null,					// ArrayList<String> requestProperties
//				postParams				// String postParams
//				);
//		Object[] defaultAspx = (Object[]) mRequestTask.get();	
		
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

//		mRequestTask = new RequestTask();
//		mRequestTask.execute(
//				"POST",					// Request Type
//				"https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/InternetViewerService.ashx/Init?PageUniqueId=" + pageUniqueId,	
//										// String url
//				cookies,				// ArrayList<String> cookies
//				requestProperties,		// ArrayList<String> requestProperties
//				postParams				// String postParams
//				);
//		Object[] init = (Object[]) mRequestTask.get();	
		
		
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

	public String getDetailedReport (int classId, int termId, int studentId) throws InterruptedException, ExecutionException {

			
		String url = "https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/StudentAssignments.aspx?" + 
				"&EnrollmentId=" + 	classId + 
				"&TermId=" + termId + 
				"&ReportType=0&StudentId=" + studentId;
		

		mRequestTask = new RequestTask();
		mRequestTask.execute(
				"GET",		// Request Type
				url,		// String url
				cookies,	// ArrayList<String> cookies
				null		// ArrayList<String> requestProperties
				);
		Object[] report = (Object[]) mRequestTask.get();	
		
		
//			Object[] report = Request.sendGet(url,	cookies);
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
	public JSONArray loadGradeSummary () throws JSONException {
		try {
			String classId = classList.getJSONObject(0).getString("enrollmentId");
			String termId = classList.getJSONObject(0).getJSONArray("terms").getJSONObject(0).getString("termId");
	
			String url = "https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/GradeSummary.aspx?" + 
					"&EnrollmentId=" + 	classId + 
					"&TermId=" + termId + 
					"&ReportType=0&StudentId=" + studentId;
			
	//		mRequestTask = new RequestTask();
	//		mRequestTask.execute(
	//				"GET",		// Request Type
	//				url,		// String url
	//				cookies,	// ArrayList<String> cookies
	//				null		// ArrayList<String> requestProperties
	//				);
	//		Object[] summary = (Object[]) mRequestTask.get();	
	
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
			gradeSummary = Parser.gradeSummary(response, classList);
	
			return gradeSummary;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalUrlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONArray getGradeSummary () {
		return gradeSummary;
	}

	public void getClassGrades( int classIndex, int termIndex ) throws JSONException, InterruptedException, ExecutionException {
		
		if (classGrades == null)
			classGrades = classList;
		
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
	
	
	public JSONArray getAllClassGrades() throws JSONException, InterruptedException, ExecutionException {
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
	
	public void printCookies() {
		System.out.println();
		System.out.println("Cookies:");
		for (String c : cookies)
			System.out.println(c);
		System.out.println();
	}
	

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
	

	public JSONArray getClassGrades () {
		return classGrades;
	}


	public class LoginTask extends AsyncTask<Void, Void, Void> {

		protected void onPreExecute() {
			System.out.println("on Pre Execute");
		}
		
		protected void onPostExecute() {
			System.out.println("on Post Execute");
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try {
				System.out.println("Process started");
				login();
				System.out.println("Process ended");
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(java.util.Arrays.toString(passthroughCredentials));
			return null;
		}
		
	}
	
	public class LoginGradebookTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				return loginGradebook(params[0], params[1], params[2], params[3]);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
	}
	
	public class DetailedReportTask extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			try {
				getDetailedReport(params[0], params[1], params[2]);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
}
