package app.sunstreak.yourpisd.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import android.graphics.Bitmap;
import android.util.SparseArray;


public class Student {

	private YPSession session;

	public final int studentId;
	public final String name;
	JSONArray classList;
	int[][] gradeSummary;
	int[] classIds;
	int[] classMatch;
	SparseArray<JSONObject> classGrades = new SparseArray<JSONObject>();
	//		Map<Integer[], JSONObject> classGrades = new HashMap<Integer[], JSONObject>();
	Bitmap studentPictureBitmap;

	public static final int CLASS_DISABLED_DURING_TERM = -2;
	public static final int NO_GRADES_ENTERED = -1;
	public static final String[] SEMESTER_AVERAGE_KEY = {"firstSemesterAverage", "secondSemesterAverage"};

	public Student (int studentId, String studentName, YPSession session) {
		this.session = session;

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
				"https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/InternetViewerService.ashx/Init?PageUniqueId=" + session.pageUniqueId,
				session.cookies,
				requestProperties, 
				true, 
				postParams);

		String response = (String) init[0];
		int responseCode = (Integer) init[1];
		session.cookies = (ArrayList<String>) init[2];

		try {
			classList = (new JSONObject(response)).getJSONArray("classes");
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public JSONArray getClassList() {
		return classList;
	}

	public List<Integer> getClassesForTerm (int termIndex) {
		List<Integer> classesForTerm = new ArrayList<Integer>();
		if (gradeSummary == null)
			throw new RuntimeException("Grade Summary has not been fetched.");


		for (int classIndex = 0; classIndex < gradeSummary.length; classIndex++) {
			//			System.out.println(Arrays.toString(gradeSummary[classIndex]));
			int termLocation = termIndex < 4 ? termIndex + 1 : termIndex + 2;
			if (gradeSummary[classIndex][termLocation] != CLASS_DISABLED_DURING_TERM)
				classesForTerm.add(classIndex);
		}
		return classesForTerm;
	}

	/**
	 * Uses internet every time. 
	 * @throws JSONException
	 */
	public int[][] loadGradeSummary () throws JSONException {
		try {
			String classId = "" + classList.getJSONObject(0).getInt("enrollmentId");
			String termId = "" + classList.getJSONObject(0).getJSONArray("terms").getJSONObject(0).getInt("termId");

			String url = "https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/GradeSummary.aspx?" + 
					"&EnrollmentId=" + 	classId + 
					"&TermId=" + termId + 
					"&ReportType=0&StudentId=" + studentId;

			Object[] summary = Request.sendGet(url,	session.cookies);
			String response = (String) summary[0];
			int responseCode = (Integer) summary[1];
			session.cookies = (ArrayList<String>) summary[2];

			if (responseCode != 200)
				System.out.println("Response code: " + responseCode);

			/*
			 * puts averages in classList, under each term.
			 */
			Element doc = Jsoup.parse(response);
			gradeSummary = Parser.gradeSummary(doc, classList);

			matchClasses(gradeSummary);

			for (int classIndex = 0; classIndex < gradeSummary.length; classIndex++) {
				int jsonIndex = classMatch[classIndex];
				JSONArray terms = classList.getJSONObject(jsonIndex).getJSONArray("terms");

				int firstTermIndex = 0;
				int lastTermIndex = 0;

				if (terms.length() == 8) {
					// Full year course
					firstTermIndex = 0;
					lastTermIndex = 7;
				} else if (terms.length() == 4) {
					if (terms.optJSONObject(0).optString("description").equals("1st Six Weeks")) {
						// First semester course
						firstTermIndex = 0;
						lastTermIndex = 3;
					} else {
						// Second semester course
						firstTermIndex = 4;
						lastTermIndex = 7;
					}
				}

				for (int termIndex = firstTermIndex; termIndex <= lastTermIndex; termIndex++) {
					int arrayLocation = termIndex > 3 ? termIndex + 2 : termIndex + 1;
					int average = gradeSummary[classIndex][arrayLocation];
					if (average != NO_GRADES_ENTERED)
						classList.getJSONObject(jsonIndex).getJSONArray("terms").getJSONObject(termIndex - firstTermIndex)
						.put("average", average);
				}

				classList.getJSONObject(jsonIndex).put("firstSemesterAverage", gradeSummary[classIndex][5]);
				classList.getJSONObject(jsonIndex).put("secondSemesterAverage", gradeSummary[classIndex][10]);
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
			System.err.println("You didn't login!");
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

		Object[] report = Request.sendGet(url,	session.cookies);
		String response = (String) report[0];
		int responseCode = (Integer) report[1];
		session.cookies = (ArrayList<String>) report[2];

		if (responseCode != 200) {
			System.out.println("Response code: " + responseCode);
		}
		return response;
	}

	public String[] getAssignmentDetails(int classIndex, int termIndex, int assignmentId) throws MalformedURLException, IOException, JSONException {
		Object[] details = Request.sendGet(
				"https://gradebook.pisd.edu/Pinnacle/Gradebook/InternetViewer/AssignmentDetail.aspx?"
						+ "assignmentId=" + assignmentId
						+ "&H=" + session.domain.hValue
						+ "&GradebookId=" + studentId
						+ "&TermId=" + classList.getJSONObject(classMatch[classIndex]).getJSONArray("terms").getJSONObject(termIndex).getInt("termId")
						+ "&StudentId=" + studentId + "&",
						session.cookies);
		return Parser.parseAssignment((String)details[0]);
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

	public boolean hasClassDuringSemester ( int classIndex, int semesterIndex ) {
		if (gradeSummary == null)
			throw new RuntimeException ("Grade summary is null. " +
					"Operation hasClassDuringSemester() not allowed.");
		// much cryptic. so obfuscate. sorry, i'll clean it up later
		for (int i = 4 * semesterIndex + semesterIndex; i < 4 * semesterIndex + 4; i++)
			if (gradeSummary[classIndex][i + 1] != CLASS_DISABLED_DURING_TERM)
				return true;
		return false;
	}

	public boolean hasClassGrade (int classIndex, int termIndex) throws JSONException {

		int termIndexOffset = 0;
		if (gradeSummary[classIndex][3] == CLASS_DISABLED_DURING_TERM)
			termIndexOffset = 4;

		termIndex -= termIndexOffset;

		if (classGrades.indexOfKey(classIndex) < 0)
			return false;

		JSONObject classGrade = classGrades.get(classIndex);
		JSONArray terms = classGrade.getJSONArray("terms");
		JSONObject term = terms.getJSONObject(termIndex); 
		long lastUpdated = term.optLong("lastUpdated", -1);

		return lastUpdated != -1;
	}

	public JSONObject getClassGrade( int classIndex, int termIndex ) throws JSONException {

		String html = "";

		int classId = gradeSummary[classIndex][0];
		int termIndexOffset = 0;
		if (gradeSummary[classIndex][3] == CLASS_DISABLED_DURING_TERM)
			termIndexOffset = 4;

		termIndex -= termIndexOffset;

		if (hasClassGrade(classIndex, termIndex + termIndexOffset ))
			return classGrades.get(classIndex).optJSONArray("terms").optJSONObject(termIndex);


		try {

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
			classGrade = new JSONObject(classList.getJSONObject(getClassMatch()[classIndex] ).toString());

			JSONArray termGrades = Parser.detailedReport(html);
			Object[] termCategory = Parser.termCategoryGrades(html);

			JSONArray termCategoryGrades = (JSONArray) termCategory[0];
			if ((Integer)termCategory[1] != -1)
				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("average", termCategory[1]);

			classGrade.getJSONArray("terms").getJSONObject(termIndex).put("grades", termGrades);
			classGrade.getJSONArray("terms").getJSONObject(termIndex).put("categoryGrades", termCategoryGrades);

			Instant in = new Instant();
			//				String time = in.toString();
			//				System.out.println(time);
			classGrade.getJSONArray("terms").getJSONObject(termIndex).put("lastUpdated", in.getMillis());
			//				classGrade.getJSONArray("terms").getJSONObject(termIndex).put("lastUpdated", "0");

			//System.out.println("cg= " + classGrade);


			if (classGrades.indexOfKey(classIndex) < 0)
				classGrades.put(classIndex, classGrade);

			return classGrade.getJSONArray("terms").getJSONObject(termIndex);


		} catch (JSONException e) {
			System.err.println("Error: Class index = " + classIndex + 
					"; JSON index = " + getClassMatch()[classIndex] + 
					"; Term index = " + termIndex + ".");
			e.printStackTrace();
			return null;
		}

	}

	public String getClassName (int classIndex) {
		if (classList == null)
			return "null";
		else
			try {
				return Parser.toTitleCase(classList.getJSONObject(classIndex).getString("title"));
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
				session.cookies,
				requestProperties,
				true);

		studentPictureBitmap = (Bitmap) response[0];
		int responseCode = (Integer) response[1];
		//cookies = cookies;
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

		System.out.println(Arrays.toString(classIds));
		for (int i = 0; i < gradeSummary.length; i++)
			System.out.print(gradeSummary[i][0] + " ");
		System.out.println();
		
		while (classesMatched < classCount) {
			for (int i = 0; i < classIds.length; i++)
				if (classIds[i] == gradeSummary[classesMatched][0]) {
					classMatch[classesMatched] = i;
					classesMatched++;
					break;
				}
			System.out.println(Arrays.toString(classMatch));
		}

	}



	public int[] getClassMatch () {
		return classMatch;
	}

	public double getCumulativeGPA(float oldCumulativeGPA, float numCredits)
	{
		// TODO method is hardcoded for use with fall semester
		double oldSum = (double) oldCumulativeGPA * (double) numCredits;
		double newNumCredits = numCredits + getNumCredits(0);
		return (getGPA(0) * getNumCredits(0) + oldSum)/newNumCredits;
	}

	public int getNumCredits( int semesterIndex ) {
		if (classMatch == null)
			return -2;

		int pointCount = 0;
		
		for (int classIndex = 0; classIndex < classMatch.length; classIndex++) {
			int jsonIndex = classMatch[classIndex];
			int grade = classList.optJSONObject(jsonIndex).optInt(SEMESTER_AVERAGE_KEY[semesterIndex]);

			if (!(grade == NO_GRADES_ENTERED || grade == CLASS_DISABLED_DURING_TERM))
				pointCount++;
		}
		return pointCount;
	}

	public double getGPA ( int semesterIndex ) {

		if (classMatch == null)
			return -2;

		double pointSum = 0;
		int pointCount = 0;

		for (int classIndex = 0; classIndex < classMatch.length; classIndex++) {

			int jsonIndex = classMatch[classIndex];

			int grade = classList.optJSONObject(jsonIndex).optInt(SEMESTER_AVERAGE_KEY[semesterIndex]);

			if (grade == NO_GRADES_ENTERED || grade == CLASS_DISABLED_DURING_TERM)
				continue;
			// Failed class
			if (grade < 70) {
				// Do not increment pointSum because the student received a GPA of 0.
				pointCount++;
			}
			else {
				double classGPA = maxGPA(classIndex) - gpaDifference(grade);
				pointSum += classGPA;
				pointCount++;
			}
		}
		try {
			return pointSum / pointCount;
		} catch (ArithmeticException e) {
			return Double.NaN;
		}
	}

	public double maxGPA (int classIndex) {
		return maxGPA(getClassName(classMatch[classIndex]));
	}

	public static double maxGPA (String className) {
		className = className.toUpperCase();

		if (className.contains("PHYS IB SL") || className.contains("MATH STDY IB"))
			return 4.5;

		String[] split = className.split("[\\s()\\d\\/]+");

		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("AP") || split[i].equals("IB"))
				return 5;
			if (split[i].equals("H") || split[i].equals("IH"))
				return 4.5;
		}
		return 4;
	}

	public static double gpaDifference (int grade) {
		if (grade == NO_GRADES_ENTERED)
			return Double.NaN;

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
			sum = (gradeDesired - 0.5) * 4 - sum;
			return (int) Math.ceil(sum);
		} catch (Exception e) {
			// Not enough grades for calculation
			return -1;
		}
	}

}