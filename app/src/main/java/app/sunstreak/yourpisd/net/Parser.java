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

import android.util.Log;
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
		if (html == null)
			return;
		Document doc = Jsoup.parse(html);
		if (doc == null)
			return;
		Element main = doc.getElementById("Main").getElementById("Content");

		// For each year
		// TODO: test next year
		Elements years = main.getElementById("ContentMain").getElementsByClass("calendar");
		for (Element year : years)
		{
			// For each terms
			// TODO: test after 9 weeks
			Elements terms = year.getElementsByClass("term");
			for (Element term : terms)
			{
				Elements termName = term.getElementsByTag("h2");
				if (termName.isEmpty())
					continue;

				// Term date
				//System.out.println(termName.get(0).getElementsByClass("term").html() + " of " + termName.get(0).getElementsByClass("year").html());
				//System.out.println();

				// For each course
				Elements courses = term.children().get(1).children().get(0).children();
				for (Element course : courses)
				{
					Elements courseMain = course.getElementsByTag("tr").get(0).children();



					// Period number
					String period = courseMain.get(0).html();
					if (period.isEmpty())
						period = "0";

					// Course name
					String name = courseMain.get(1).children().get(0).children().get(0).html();

					String temp = courseMain.get(1).children().get(0).children().get(0).attr("abs:href");
					int courseId = Integer.parseInt(temp.substring(temp.indexOf("=") + 1, temp.indexOf("&")));
					// Teacher name
					String teacher = courseMain.get(1).children().get(1).getElementsByClass("teacher").get(0).html();
					// Course grade (might be empty string if no grade) - formatted as number + %
					int grade;
					if (courseMain.get(2).children().isEmpty())
						grade = 100; //TODO: what grade if no grades are assigned???
					else
					{
						String teemp = courseMain.get(2).children().get(0).children().get(0).children().get(0).html();
						grade = Integer.parseInt(teemp.substring(0, teemp.length() - 1));
					}

					ClassReport report = new ClassReport(courseId, name);
					report.setPeriodNum(Integer.parseInt(period));
					report.setTeacherName(teacher);

					TermReport termReport = new TermReport(null, report, 0, false);
					report.setTerm(0, termReport);
					termReport.setGrade(grade);
					classes.put(report.getClassID(), report);
				}
			}
		}

		//Log.v("parseTag",classes.toString());

		//Testing
		/*Object[][] rawData = {
				{1235, 1, "Evans, Gary", "Bio AP", 96},
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
		}*/
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
		if (html == null)
			return null;
		Document doc = Jsoup.parse(html);
		if (doc == null)
			return null;
		Element main = doc.getElementById("Main").getElementById("Navigation").getElementsByTag("li").get(0);

		//main.getElementById("ContentHeader").getElementsByClass("container").get(0).getElementsByTag("h2").get(0).html();

		//TODO multiple students
		ArrayList<Student> students = new ArrayList<>();
		Student single = new Student(1111, main.html(), sess); //TODO fix id
		students.add(single);
		return students;
	}


	
}
