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

import app.sunstreak.yourpisd.TermFinder;
import app.sunstreak.yourpisd.net.data.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


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
		if (html == null) {
			System.err.println("No html for parsing detailed grade summary");
			return;
		}
		Document doc = Jsoup.parse(html);
		if (doc == null)
			return;
		Element main = doc.getElementById("Main").getElementById("Content").getElementById("ContentMain");

		Element categoryTable = main.getElementById("Categories");
		if (categoryTable != null && categoryTable.children().size() > 0)
		{
			//For each <TR> element
			for (Element category : categoryTable.children().get(0).children())
			{
				report.getCategories().add(new GradeCategory(category.getElementsByClass("description").get(0).html().split("\n")[0].trim(), Integer.parseInt(category.getElementsByClass("percent").get(0).html().replaceAll("[^0-9]", "")) * 0.01 ));
				report.getCategories().get(report.getCategories().size() - 1).setGrade(Integer.parseInt(category.getElementsByClass("letter").get(1).child(0).child(0).child(0).html().replace("%", "")));
				//Log.d("testTag", report.getCategories().get(report.getCategories().size() - 1).getGrade() + " " + report.getCategories().get(report.getCategories().size() - 1).getWeight() + " " + report.getCategories().get(report.getCategories().size() - 1).getType());
			}
		}
	}

	/** Parses average of each term from GradeSummary.aspx.
	 * 
	 * @param html the html body of GradeSummary.aspx
	 * @param classes the mappings of class (enrollment) ID to class reports. Parses grades into here.
	 */
	@NonNull
	public static void parseGradeSummary(String html, Map<Integer, ClassReport> classes) {
		if (html == null) {
			System.err.println("No html for parsing grade summary");
			return;
		}
		Document doc = Jsoup.parse(html);
		if (doc == null)
			return;
		Element main = doc.getElementById("Main").getElementById("Content");

		// TODO: test next year
		Elements years = main.getElementById("ContentMain").getElementsByClass("calendar");

		// For each terms
		// TODO: test after 9 weeks
		int maxTermNum = 0;
		Elements terms = years.get(0).getElementsByClass("term");
		for (Element term : terms)
		{
			Elements termName = term.getElementsByTag("h2");
			if (termName.isEmpty())
				continue;

			//Search term name in the list of terms
			String tempDate = termName.get(0).getElementsByClass("term").html();
			int termNum = 0;
			for (TermFinder.Term t : TermFinder.Term.values())
			{
				if (t.name.equalsIgnoreCase(tempDate)) {
					termNum = t.ordinal();
					break;
				}
			}
			maxTermNum = Math.max(maxTermNum, termNum);

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
				Element courseInfo = courseMain.get(1).children().get(0).children().get(0);
				String name = courseInfo.html();
				String query = courseInfo.attr("href").split("\\?", 2)[1];
				String[] parts = query.split("[&=]");
				//Parse course and term id
				int courseID = -1;
				int termID = -1;
				for (int i = 0; i < parts.length - 1; i+=2)
				{
					if (parts[i].equalsIgnoreCase("Enrollment"))
						courseID = Integer.parseInt(parts[i+1]);
					if (parts[i].equalsIgnoreCase("Term"))
						termID = Integer.parseInt(parts[i+1]);
				}

				// Teacher name
				String teacher = courseMain.get(1).children().get(1).getElementsByClass("teacher").get(0).html();
				// Course grade (might be empty string if no grade) - formatted as number + %
				int grade;
				if (courseMain.get(2).children().isEmpty())
					grade = -1; //No grade exists.
				else
				{
					String teemp = courseMain.get(2).children().get(0).children().get(0).children().get(0).html();
					grade = Integer.parseInt(teemp.substring(0, teemp.length() - 1));
				}

				ClassReport report = new ClassReport(courseID, name);
				report.setPeriodNum(Integer.parseInt(period));
				report.setTeacherName(teacher);

				TermReport termReport = new TermReport(null, report, termID, false);
				report.setTerm(termNum, termReport);
				termReport.setGrade(grade);
				classes.put(report.getClassID(), report);
			}
		}

		TermFinder.setCurrentTermIndex(maxTermNum);

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
		if (html == null)
			return null;
		Document doc = Jsoup.parse(html);
		if (doc == null)
			return null;
		Element main = doc.getElementById("Main").getElementById("Navigation").getElementsByTag("li").get(0);

		//main.getElementById("ContentHeader").getElementsByClass("container").get(0).getElementsByTag("h2").get(0).html();

		//TODO multiple students
		ArrayList<Student> students = new ArrayList<>();
		Element singleID = doc.getElementById("ctl00_ctl00_ContentPlaceHolder_uxStudentId");
		Student single = new Student(Integer.parseInt(singleID.attr("value")), main.html(), sess);
		students.add(single);
		return students;
	}


	
}
