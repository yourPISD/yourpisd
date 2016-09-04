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
import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import app.sunstreak.yourpisd.net.data.ClassReport;
import app.sunstreak.yourpisd.net.data.ParseException;
import app.sunstreak.yourpisd.net.data.Student;
import app.sunstreak.yourpisd.net.data.TermReport;


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

	/**
	 * Parses the term report (assignments for a class during a grading period).
	 *
	 * @param html text of detailed report
	 * @param report the TermReport object to store the data into.
     */
	@NonNull
	public static void parseTermReport(String html, TermReport report)
	{
		//TODO: parsing logic here..
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
	 * 
	 * @param html the html body of GradeSummary.aspx
	 * @return a list of class reports
	 * @throws ParseException if the html does not match expected format.
	 */
	@NonNull
	public static List<ClassReport> parseGradeSummary(String html) throws ParseException{
		Document doc = Jsoup.parse(html);
		List<ClassReport> classes = new ArrayList<>();
		//TODO: parse grade summary
		return classes;
	}

	/**
	 * 
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
	 * Parses and returns a list of students' informations (name and INTERNAL student id) from the Gradebook.
	 *
	 * @param sess the session loading the user.
	 * @param html the source code for ANY page in Gradebook (usually GradeSummary.aspx)
	 * @return the list of students
	 */
	@NonNull
	public static List<Student> parseStudents (Session sess, String html) {
		//FIXME: parsing students.
//		List<String[]> list = new ArrayList<String[]>();
//
//		Element doc = Jsoup.parse(html);
//		Element studentList = doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxStudentlist");
//
//
//		// Only one student
//		if (studentList.text().isEmpty()) {
//			// {studentId, studentName}
//			list.add(new String[] {doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxStudentId").attr("value"),
//					doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxMultiple").text()});
//			return list;
//		}
//		// Multiple students
//		else {
//			for (Element a : studentList.getElementsByTag("a")) {
//				String name = a.text();
//				String onClick = a.attr("onClick");
//				String studentId = onClick.substring(onClick.indexOf('\'') + 1, onClick.lastIndexOf('\''));
//				list.add(new String[] {studentId, name});
//			}
//			return list;
//		}
		return new ArrayList<>();
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


	
}
