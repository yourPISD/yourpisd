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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.SparseArray;


public class Parser {



	public static final String LOGIN_FAILURE_TITLE = "myPISD - Login";
	public static final String GRADEBOOK_BAD_USERNAME = "NotSet";

	// Cannot be instantiated.
	private Parser() { }

	/**
	 * returns value of pageUniqueId from html
	 * String must contain the following block:
	 * <input type="hidden" name="PageUniqueId" id="PageUniqueId" value="8bdc977f-ccaf-4a18-b4dd-20d1406fad6a" />
	 */
	public static String pageUniqueId (String html) {
		return pageUniqueId(Jsoup.parse(html));
	}
	
	/**
	 * returns value of pageUniqueId from html
	 * String must contain the following block:
	 * <input type="hidden" name="PageUniqueId" id="PageUniqueId" value="8bdc977f-ccaf-4a18-b4dd-20d1406fad6a" />
	 */
	public static String pageUniqueId (Element doc) {
		Elements inputElements = doc.getElementsByTag("input");

		for (Element e: inputElements)
			if (e.attr("name").equals("PageUniqueId"))
				return e.attr("value");

		return null;
	}

	/*
	 * must take in html for [Domain].loginAddress
	 */
	public static boolean accessGrantedEditure (String html) {
		Element doc = Jsoup.parse(html);
		String title = doc.getElementsByTag("title").text();
		return !title.equals(LOGIN_FAILURE_TITLE) ? true : false;
	}

	/*
	 * must take in html for "https://parentviewer.pisd.edu/EP/PIV_Passthrough.aspx?action=trans&uT=" + userType + "&uID=" + uID
	 */
	public static boolean accessGrantedGradebook (String[] gradebookCredentials) {
		System.out.println(Arrays.deepToString(gradebookCredentials));
		return  ! (gradebookCredentials[0].equals(GRADEBOOK_BAD_USERNAME));
	}


	public static String[] getGradebookCredentials (String html) {
		Element doc = Jsoup.parse(html);
		Elements userIdElements = doc.getElementsByAttributeValue("name", "userId");
		Elements passwords = doc.getElementsByAttributeValue("name", "password");
		String userId = userIdElements.attr("value");
		String password = passwords.attr("value");
		return new String[] {userId, password};
	}

	/**
	 * must take html from [Domain].portalAddress or [Domain].portalAddress/myclasses
	 */
	public static String[] passthroughCredentials (String html) {
		Element doc = Jsoup.parse(html);
		String uT = doc.getElementsByAttributeValue("name","uT").attr("value");
		String uID = doc.getElementsByAttributeValue("name","uID").attr("value");
		return new String[] {uT, uID};
	}

	public static int studentIdPinnacle (ArrayList<String> cookies) {
		//		System.out.println("Cookies size = " + cookies.size());
		for (String c : cookies)
			if (c.substring(0,c.indexOf('=')).equals("PinnacleWeb.StudentId"))
				return Integer.parseInt(c.substring(c.indexOf('=')+1));
		throw new RuntimeException ("Student ID not found. Cookies: " + cookies.toString());
	}

	public static JSONArray detailedReport (String html) throws JSONException {
		Element doc = Jsoup.parse(html);
		//		System.out.println(html);
		Element assignments = doc.getElementsByAttributeValue("id", "Assignments").get(0);
		Elements tableRows = assignments.getElementsByTag("tbody").get(0).getElementsByTag("tr");

		JSONArray grades = new JSONArray();

		for (Element tr : tableRows) {
			JSONObject assignment = new JSONObject();

			Elements columns = tr.getElementsByTag("td");

			for (int i = 0; i < columns.size(); i++) {
				String value = columns.get(i).text();
				// do not store empty values!
				if (value.equals(""))
					continue;
				// first try to cast as int.
				try {
					assignment.putOpt(assignmentTableHeader(i), Integer.parseInt(value));
					// if not int, try double
				} catch (NumberFormatException e) {
					try {
						assignment.putOpt(assignmentTableHeader(i), Double.parseDouble(value));
						// if not double, use string
					} catch (NumberFormatException f) {
						assignment.putOpt(assignmentTableHeader(i), value);
					}
				}
			}

			String assignmentDetailLink = tr.getElementsByTag("a").get(0).attr("href");
			Matcher matcher = Pattern.compile(".+" +
					"assignmentId=(\\d+)" +
					"&H=S" +
					"&GradebookId=(\\d+)" +
					"&TermId=\\d+" +
					"&StudentId=\\d+&")
					.matcher(assignmentDetailLink);
			matcher.find();
			int assignmentId = Integer.parseInt(matcher.group(1));
			int gradebookId = Integer.parseInt(matcher.group(2));
			assignment.put("assignmentId", assignmentId);
			assignment.put("gradebookId", gradebookId);
			grades.put(assignment);
		}
		//		System.out.println((grades));
		return grades;
	}

	/**
	 * Reads assignment view page and returns teacher name.
	 * 
	 * Parses from this table:
	 * 
	 * <table id='classStandardInfo'> <tbody> <tr>  
	 * <td>       <div class='classInfoHeader'>Kapur, Sidharth (226344)</div>2013-08-29   <td>    
	 * <table>    
	 * 		<tr>      <th style='width:1%'>Course:</th>      <td><a href='javascript:ClassDetails.getClassDetails(2976981);' id='ClassTitle'>CHEM  AP(00)</a></td></tr>    
	 * 		<tr>      <th>Term:</th>      <td>1st Six Weeks</td>     </tr>
	 * 		<tr>      <th>Teacher:</th>      <td><a href="mailto:Nicole.Lyssy@pisd.edu" title="Nicole.Lyssy@pisd.edu">Lyssy, Carol</a></td>     </tr>
	 * </table>
	 * <td>  </tr> </tbody></table>
	 */
	public static String[] teacher (String html)  {
		Element doc = Jsoup.parse(html);
		Element classStandardInfo = doc.getElementById("classStandardInfo");
		// teacher is the third row in this table
		Element teacher = classStandardInfo.getElementsByTag("table").get(0).getElementsByTag("tr").get(3).getElementsByTag("td").get(0);
		//		System.out.println(teacher);
		String email = "";
		try {
			email = teacher.getElementsByTag("a").get(0).attr("title");
		} catch (IndexOutOfBoundsException e) {
			// Senior release teacher have NO email. The <a> tag does not exist.
		}
		String teacherName = teacher.text();
		return new String[] {teacherName, email};
	}

	public static Object[] termCategoryGrades (String html) throws JSONException {
		JSONArray termCategoryGrades = new JSONArray();

		Element doc = Jsoup.parse(html);
		Element categoryTable = doc.getElementById("Category");
		Elements rows = categoryTable.getElementsByTag("tbody").get(0).getElementsByTag("tr");



		for (Element row : rows) {
			JSONObject category = new JSONObject();
			Elements columns = row.getElementsByTag("td");
			for (int i = 0; i < columns.size(); i++) {

				String value = columns.get(i).text();
				// do not store empty values!
				if (value.equals(""))
					continue;
				// first try to cast as int.
				try {
					category.putOpt(categoryTableHeader(i), Integer.parseInt(value));
					// if not int, try double
				} catch (NumberFormatException e) {
					try {
						category.putOpt(categoryTableHeader(i), Double.parseDouble(value));
						// if not double, use string
					} catch (NumberFormatException f) {
						category.putOpt(categoryTableHeader(i), value);
					}
				}
			}
			termCategoryGrades.put(category);

		}

		// The average for the six weeks is 
		int average = -1;
		try {
			Element finalGrade = doc.getElementById("finalGrade");
			average = Integer.parseInt(finalGrade.getElementsByTag("td").get(3).text());
		} catch (NullPointerException e) {
			// Let average be -1
		}

		return new Object[] {termCategoryGrades, average};
	}

	/*
	 * We should probably replace this with an Enum
	 */
	public static String assignmentTableHeader (int column) {
		// starts at one (because Number is a th, not a td)
		switch (column+1) {
		// if we return null, the machine won't put it
		case 0:
			return null;
			//return "Number";
		case 1:
			return "Description";
		case 2:
			return "Due Date";
		case 3:
			return "Category";
		case 4:
			return "Grade";
		case 5:
			return "Max";
		case 6:
			return "Letter";
		case 7:
			return "Comments";
		default:
			return null;
		}
	}

	public static String categoryTableHeader (int column) {
		switch (column) {
		case 0:
			return "Category";
		case 1:
			return "Weight";
		case 2:
			return "Points / Max pts.";
		case 3:
			return "Percent";
		case 4:
			return "Letter";
		default:
			return null;
		}
	}

	/** Parses average of each term from GradeSummary.aspx.
	 * NOTICE: Does not work for second semester classes in which the second semester schedule
	 *  is different from the first semester schedule.
	 * 
	 * @param html source of GradeSummary.aspx
	 * @param classList classList as returned by Init.aspx
	 * @throws JSONException
	 * @return 	 [
	 * 		[classId, avg0, avg1, ...],
	 * 		[classId, avg0, avg1, ...],
	 * ]
	 */
	public static int[][] gradeSummary (String html, JSONArray classList) {
		Element doc = Jsoup.parse(html);
		return gradeSummary(doc, classList);
	}

	/** Parses average of each term from GradeSummary.aspx.
	 * NOTICE: Does not work for second semester classes in which the second semester schedule
	 *  is different from the first semester schedule.
	 * 
	 * @param doc the Jsoup element of GradeSummary.aspx
	 * @param classList classList as returned by Init.aspx
	 * @throws JSONException
	 * @return 	 [
	 * 		[classId, avg0, avg1, ...],
	 * 		[classId, avg0, avg1, ...],
	 * ]
	 */
	public static int[][] gradeSummary (Element doc, JSONArray classList) {

		List<int[]> gradeSummary = new ArrayList<int[]>();

		Element reportTable = doc.getElementsByClass("reportTable").get(0).getElementsByTag("tbody").get(0);
		Elements rows = reportTable.getElementsByTag("tr");
		int rowIndex = 0;

		while (rowIndex < rows.size() ) {

			int[] classAverages = new int[11];
			Arrays.fill(classAverages, -3);

			Element row = rows.get(rowIndex);
			Elements columns = row.getElementsByTag("td");

			classAverages[0] = getClassId(row);

			for (int col = 0; col < 10; col++) {
				Element column = columns.get(col);
				String text = column.text();

				// -2 for disabled class
				if (column.attr("class").equals("disabledCell"))
					text = "-2";
				classAverages[col+1] = text.equals("") ? -1 : Integer.parseInt(text);
			}
			gradeSummary.add( classAverages );
			rowIndex++;
		}

		/*
		 * [
		 * 		[classId, avg0, avg1, ...],
		 * 		[classId, avg0, avg1, ...],
		 * ]
		 */
		int[][] result = new int[gradeSummary.size()][];
		for (int i = 0; i < result.length; i++) {
			result[i] = new int[gradeSummary.get(i).length];
			for (int j = 0; j < result[i].length; j++)
				result[i][j] = gradeSummary.get(i)[j];
		}
		return result;
	}

	/**
	 * 
	 * @param A Jsoup Element that contains the html for a row in GradeSummary
	 * @return 
	 */
	public static int getClassId (Element row) {
		Elements th = row.getElementsByTag("th");
		String href = th.get(0).getElementsByTag("a").get(0).attr("href");
		// href="javascript:ClassDetails.getClassDetails(2976981);"
		return Integer.parseInt(href.substring(40, 47));
	}

	/**
	 * @param html Source code of any Gradebook page.
	 * @return the full name (Last, First) of the student and ID number.
	 */
	public static String studentName (String html) {
		return studentName(Jsoup.parse(html));
	}

	/**
	 * @param doc The Jsoup element from any Gradebook page.
	 * @return the full name (Last, First) of the student and ID number.
	 */
	public static String studentName (Element doc) {
		Element studentName = doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxMultiple");
		return studentName.text();
	}

	/**
	 * 
	 * @param html html source code of https://sso.portal.mypisd.net/cas/login?service=http%3A%2F%2Fportal.mypisd.net%2Fc%2Fportal%2Flogin
	 * @return the value embedded in the <input type="hidden" name="lt" value=""> block
	 */
	public static String portalLt (String html) {
		Element doc = Jsoup.parse(html);
		Elements inputTags = doc.getElementsByTag("input");
		//Shortcut
		//		if (inputTags.get(4).attr("name").equals("lt"))
		//			return inputTags.get(4).attr("value");
		//		else {
		for (Element tag : inputTags) {
			if (tag.attr("name").equals("lt"))
				return tag.attr("value");
		}
		//		}
		return null;
	}


	/**
	 * 
	 * @param html the source code for ANY page in Gradebook (usually Default.aspx)
	 * @return
	 */
	public static List<String[]> parseStudents (String html) {
		List<String[]> list = new ArrayList<String[]>();

		Element doc = Jsoup.parse(html);
		Element studentList = doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxStudentlist");


		// Only one student
		if (studentList.text().isEmpty()) {
			// {studentId, studentName}
			list.add(new String[] {doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxStudentId").attr("value"), 
					doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxMultiple").text()});
			return list;
		}
		// Multiple students
		else {
			for (Element a : studentList.getElementsByTag("a")) {
				String name = a.text();
				String onClick = a.attr("onClick");
				String studentId = onClick.substring(onClick.indexOf('\'') + 1, onClick.lastIndexOf('\''));
				list.add(new String[] {studentId, name});
			}
			return list;
		}
	}

	public static String[] parseAssignment (String html) throws JSONException {
		//Debug code
		/*
		Element doc = Jsoup.parse(html);
		System.out.println(doc);
		Element assignment = doc.getElementById("Assignment");
		System.out.println(assignment);
		Elements tds = assignment.getElementsByTag("td");
		System.out.println(tds);
		 */
		Elements tds = Jsoup.parse(html).getElementById("Assignment").getElementsByTag("td");
		//		JSONObject ass = new JSONObject();
		//		ass.put("assignedDate", tds.get(3).text());
		//		ass.put("dueDate", tds.get(5).text());
		//		ass.put("weight", tds.get(9).text());
		//	{assignedDate, dueDate, weight}
		return new String[] {tds.get(3).text(), tds.get(5).text(), tds.get(9).text()};
	}


	public static String toTitleCase (String str) {
		StringBuilder sb = new StringBuilder(str.length());
		boolean capitalize = false;
		for (int charNum = 0; charNum < str.length(); charNum++) {

			capitalize = (charNum == 0 ||
					! Character.isLetter(str.charAt(charNum - 1)) ||
					( str.substring(charNum - 1, charNum + 1).equals("AP") ||
							str.substring(charNum - 1, charNum + 1).equals("IB") ||
							str.substring(charNum - 1, charNum + 1).equals("IH")) &&
							(charNum + 2 >= str.length() || ! Character.isLetter(str.charAt(charNum + 1)) )
					);

			if (capitalize)
				sb.append(Character.toUpperCase(str.charAt(charNum)));
			else
				sb.append(Character.toLowerCase(str.charAt(charNum)));
		}
		return sb.toString();
	}

	public static AttendanceData parseAttendanceSummaryWithoutData (String html) {
		return new AttendanceData(html);
	}
	
	/*
	private static AttendanceData parseAttendanceSummaryValidation (Element doc) {
		return new AttendanceData();
	}
	*/
	
	public static AttendanceData parseAttendanceSummary (String html) {
		AttendanceData ad = new AttendanceData(html);
		//ad.parseSummary();
		ad.parseDetailedView();
		return ad;
	}
	
	public static class AttendanceData {
		public static final String[] COLS = {"Unexcused Absence", "Excused Absence",
			"School Absence", "Unexcused Tardy", "Excused Tardy"};
		public static final int NUM_COLS = COLS.length;
		/**
		 * 1 Jan 2014
		 */
		public static String START_OF_SPRING_SEMESTER = "Wed+1%2F1%2F2014";
		/**
		 * 11 June 2014
		 */
		public static String END_OF_SPRING_SEMESTER = "Wed+6%2F11%2F2014";
		
		public static class AttendanceEvent {
			public static final String[] ATTENDANCE_CODE_DOES_NOT_COUNT_TOWARDS_EXEMPTIONS = new String[]
					{"ABC",	"ACI", "ACL", "ACR", "ADN",
					"AEC", "AEE", "AFT", "AIS", "AOC",
					"ARH", "ASO", "ATS", "NS"};
			
			String date;
			boolean isAbsence; // True if absence, false if tardy
			boolean countsAgainstExemptions;
			int period;
			
			public AttendanceEvent (String date, String code, int period) {
				this.date = date;
				this.period = period;
				
				code = code.substring(code.indexOf("-")+1);
				isAbsence = !code.equals("T");
				
				countsAgainstExemptions = true;
				for (String codeThatDoesNotCount : ATTENDANCE_CODE_DOES_NOT_COUNT_TOWARDS_EXEMPTIONS) {
					if (code.equals(codeThatDoesNotCount)) {
						countsAgainstExemptions = false;
						break;
					}
				}
			}
			
			public boolean isAbsence() { return isAbsence; 	}
			
			public boolean countsAgainstExemptions() { return countsAgainstExemptions; }
			
			
			public int getPeriod() {
				return period;
			}
			
			public String toString() {
				return String.format("%s: Period %d: %s %s", date, period, isAbsence?"A":"T", countsAgainstExemptions?":(":":)");
			}
		}
		
		SparseArray<String> classNames;
		TreeMap<String,List<AttendanceEvent>> eventsByDate;
		SparseArray<List<AttendanceEvent>> eventsByPeriod;
		String viewState;
		String eventValidation;
		//int[][] attendanceSummary;
		//String[] classNames;
		String pageUniqueId;
		Element doc;
		
		AttendanceData () {	}
		
		AttendanceData (String html) {
			eventsByDate = new TreeMap<String, List<AttendanceEvent>>();
			eventsByPeriod = new SparseArray<List<AttendanceEvent>>();
			classNames = new SparseArray<String>();
			
			doc = Jsoup.parse(html);
			
			if (doc.getElementsByTag("title").get(0).text().trim().equals("Error")) {
				throw new RuntimeException ("Gradebook error");
			}
			
			parseValidation();
			
			pageUniqueId = pageUniqueId(doc);
		}
		
		public TreeMap<String, List<AttendanceEvent>> getEventsByDate() {
			if (eventsByDate == null)
				throw new IllegalStateException();
			return eventsByDate;
		}
		
		public SparseArray<List<AttendanceEvent>> getEventsByPeriod() {
			if (eventsByPeriod == null)
				throw new IllegalStateException();
			return eventsByPeriod;
		}
		
		/*
		private void parseSummary() {
			Element table = doc.getElementById("AttendanceSummaryd");
			
			Element classNamesElement = table.getElementsByTag("thead").get(0).getElementsByTag("tr").get(1);
			Element tableBody = table.child(1);
			int numClasses = classNamesElement.children().size() - 1;
			//System.out.println(numClasses);
			attendanceSummary = new int[numClasses][AttendanceData.NUM_COLS];
			classNames = new String[numClasses];
			
			for (int i = 1; i <= numClasses; i++) {
				classNames[i-1] = classNamesElement.child(i).text();
			}
			
			for (int colIndex = 0; colIndex < AttendanceData.NUM_COLS; colIndex++) {
				Element row = tableBody.child(colIndex);
				for (int classIndex = 1; classIndex <= numClasses; classIndex++) {
					
					int num;
					try {
						num = Integer.parseInt(row.child(classIndex).text());
					} catch(NumberFormatException e) {
						num = 0;
					}
					
					attendanceSummary[classIndex - 1][colIndex] = num;
				}
			}
		}
		*/
		
		private void parseDetailedView() {
			Element table = doc.getElementById("AttendanceDetaild");
			List<Integer> classes = new ArrayList<Integer>();
			{
				Elements classesElements = table.child(0).child(1).children();
				Pattern p = Pattern.compile("(.+)" + Pattern.quote("(") + "(\\d+)" + Pattern.quote(")"));
				for (int i = 1; i < classesElements.size(); i++) {
					String className = classesElements.get(i).text();
					Matcher m = p.matcher(className);
					if (m.find()) {
						int period = Integer.parseInt(m.group(2));
						classes.add(period);
						classNames.append(period, m.group(1));
					} else {
						throw new RuntimeException(String.format("Class %s does not have a period", className));
					}
				}
			}
			{
				Elements attendanceDays = table.child(1).children();
				for (Element attendanceDay : attendanceDays) {
					String day = attendanceDay.children().get(0).text();
					Elements periods = attendanceDay.children();
					for (int i = 1; i < periods.size(); i++) {
						Element period  = periods.get(i);
						if (period.hasText()) {
							AttendanceEvent event = new AttendanceEvent(day, period.text(), classes.get(i-1));
							List<AttendanceEvent> eventsOnPeriod;
							if (eventsByPeriod.indexOfKey(event.period) >= 0)
								eventsOnPeriod = eventsByPeriod.get(event.period);
							else {
								eventsOnPeriod = new ArrayList<AttendanceEvent>();
								eventsByPeriod.append(event.period, eventsOnPeriod);
							}
							//System.out.println(event);
							eventsOnPeriod.add(event);
							
							List<AttendanceEvent> eventsOnDay;
							if (eventsByDate.containsKey(day)) {
								eventsOnDay = eventsByDate.get(day);
							} else {
								eventsOnDay = new ArrayList<AttendanceEvent>();
								eventsByDate.put(day, eventsOnDay);
							}
							eventsOnDay.add(event);
						}
					}
				}
			}
		}
		
		public void printDetailedView () {
			System.out.println("Events by Period:");
			for (int i = 0; i < eventsByPeriod.size(); i++) {
				int period = eventsByPeriod.keyAt(i);
				System.out.printf("Period %d:\n", period);
				for (AttendanceEvent e : eventsByPeriod.get(period)) {
					System.out.println(e);
				}
			}
			
			System.out.println("\nEvents by Date:");
			
			for (Map.Entry<String,List<AttendanceEvent>> entry : eventsByDate.entrySet()) {
				String date = entry.getKey();
				System.out.printf("%s:\n", date);
				for (AttendanceEvent e : entry.getValue()) {
					System.out.println(e);
				}
			}
		}
		
		public void parseValidation () {
			viewState = doc.getElementById("__VIEWSTATE").attr("value");
			Element event = doc.getElementById("__EVENTVALIDATION");
			if (event != null)
				eventValidation = event.attr("value");
		}
		
		public String toString() {
			return String.format("View State: %s\nEvent validation: %s", viewState, eventValidation);
		}
	}
	
}
