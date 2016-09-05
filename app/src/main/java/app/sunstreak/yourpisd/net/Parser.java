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
//		if (html == null)
//			return;
//		Document doc = Jsoup.parse(html);

		//TODO: parse grade summary
		//Testing
		Object[][] rawData = {
				{1235, 1, "Evans, Gary", "Bio AP", 97},
				{1236, 2, "Evans, Gary", "Bio Lab H", 97},
				{1237, 3, "Nancy, Carolyn", "Calculus BC", 95},
				{1238, 4, "Lee, Sarah", "Computer Science AP", 89},
				{1239, 5, "Simoul, Bryan", "AP US History", 90},
				{1240, 6, "Gray, Stephen", "English 3 AP", 92},
				{1241, 7, "Brians, Emily", "Physics H", 95},
		};

		for (Object[] classData : rawData) {
			ClassReport report = new ClassReport((int) classData[0], (String) classData[3]);
			report.setPeriodNum((int) classData[1]);
			report.setTeacherName((String) classData[2]);

			TermReport term = new TermReport(null, report, 0, false);
			report.setTerm(0, term);
			term.setGrade((int) classData[4]);
			classes.put(report.getClassID(), report);
		}
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
		//Testing data
		ArrayList<Student> students = new ArrayList<>();
		Student single = new Student(1111, "Doe, John C. (245643)", sess);
		students.add(single);
		return students;
	}


	
}
