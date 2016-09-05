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

	// Cannot be instantiated.
	private Parser() { }

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

	/** Parses average of each term from GradeSummary.aspx.
	 * 
	 * @param html the html body of GradeSummary.aspx
	 * @param classes the mappings of class (enrollment) ID to class reports. Parses grades into here.
	 */
	@NonNull
	public static void parseGradeSummary(String html, Map<Integer, ClassReport> classes) {
		Document doc = Jsoup.parse(html);
		//TODO: parse grade summary
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
