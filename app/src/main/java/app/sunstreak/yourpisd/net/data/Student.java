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

package app.sunstreak.yourpisd.net.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

import org.joda.time.DateTime;

import app.sunstreak.yourpisd.net.Parser;
import app.sunstreak.yourpisd.net.Session;
import app.sunstreak.yourpisd.util.Request;

public class Student {

	private final Session session;

	public final int studentId;
	public final String name;
	private final Map<Integer, ClassReport> classes = new HashMap<>();

	private DateTime lastUpdated = null;

	Bitmap studentPictureBitmap;

	public static double maxGPA(String className) {
		className = className.toUpperCase();

		if (className.contains("PHYS IB SL")
				|| className.contains("MATH STDY IB"))
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

	public Student(int studentId, String studentName, Session session) {
		this.session = session;

		this.studentId = studentId;
		name = studentName.substring(studentName.indexOf(",") + 2,
				studentName.indexOf("("))
				+ studentName.substring(0, studentName.indexOf(","));
	}

	public List<ClassReport> getClassList() {
		ArrayList<ClassReport> list = new ArrayList<>(classes.values());
		Collections.sort(list, new Comparator<ClassReport>() {
			@Override
			public int compare(ClassReport a, ClassReport b) {
				if (a.getPeriodNum() == b.getPeriodNum())
					return a.getCourseName().compareTo(b.getCourseName());
				else
					return a.getPeriodNum() - b.getPeriodNum();
			}
		});
		return list;
	}

	public List<ClassReport> getClassesForTerm(int termNum) {
		List<ClassReport> classesForTerm = new ArrayList<>();
		if (lastUpdated == null)
			throw new RuntimeException("Grade Summary has not been fetched.");

		for (ClassReport report : classes.values()) {
			if (!report.isClassDisabledAtTerm(termNum))
				classesForTerm.add(report);
		}
		Collections.sort(classesForTerm, new Comparator<ClassReport>() {
			@Override
			public int compare(ClassReport a, ClassReport b) {
				if (a.getPeriodNum() == b.getPeriodNum())
					return a.getCourseName().compareTo(b.getCourseName());
				else
					return a.getPeriodNum() - b.getPeriodNum();
			}
		});
		return classesForTerm;
	}

	/**
	 * Loads the grade summary for the student.
	 */
	public void loadGradeSummary() throws IOException
	{
		//FIXME: parse grade summary
		Map<String, String> params = new HashMap<>();
		params.put("Student", ""+studentId);
		Parser.parseGradeSummary(session.request("InternetViewer/GradeSummary.aspx", params), classes);
		lastUpdated = new DateTime();
	}

	public ClassReport getClassReport(int classID) {
		return classes.get(classID);
	}

	private void loadStudentPicture() {
		session.request("common/picture.ashx")
		ArrayList<String[]> requestProperties = new ArrayList<String[]>();
		requestProperties.add(new String[] { "Content-Type", "image/jpeg" });

		Object[] response = Request.getBitmap(
				"https://gradebook.pisd.edu/Pinnacle/Gradebook/common/picture.ashx?studentId="
						+ studentId, session.cookies, requestProperties, true);

		studentPictureBitmap = (Bitmap) response[0];
		int responseCode = (Integer) response[1];
		// cookies = cookies;
	}

	public Bitmap getStudentPicture() {
		if (studentPictureBitmap == null)
			loadStudentPicture();

		return studentPictureBitmap;
	}

	public double getCumulativeGPA(float oldCumulativeGPA, float numCredits) {
		// Averages given GPA with spring semester grades.
		int SPRING_SEMESTER = 1;
		double oldSum = (double) oldCumulativeGPA * (double) numCredits;
		double newNumCredits = numCredits + getNumCredits(SPRING_SEMESTER);
		return (getGPA(SPRING_SEMESTER) * getNumCredits(SPRING_SEMESTER) + oldSum)
				/ newNumCredits;
	}

	public int getNumCredits(int semesterIndex) {
		return getClassesForTerm(semesterIndex * ClassReport.SEMESTER_TERMS).size();
	}

	public double getGPA(int semesterIndex) {

		double pointSum = 0;
		int pointCount = 0;

		for (ClassReport report : getClassesForTerm(semesterIndex * ClassReport.SEMESTER_TERMS)) {
			int grade = report.calculateAverage(semesterIndex == 0);

			if (grade >= 70)
			{
				//Passing class.
				double classGPA = maxGPA(report.getCourseName()) - gpaDifference(grade);
				pointSum += classGPA;
				pointCount++;
			}
			else if (grade >= 0)
			{
				//Failing class
				pointCount++;
			}
			//No grade.
		}
		if (pointCount == 0)
			return Double.NaN;
		else
			return pointSum / pointCount;
	}



	public static double gpaDifference(int grade) {
		if (grade == NO_GRADES_ENTERED)
			return Double.NaN;

		if (grade <= 100 & grade >= 97)
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

//	public int examScoreRequired(int classIndex, int gradeDesired) {
//			double sum = 0;
//			for (int i = 0; i < 3; i++) {
//				sum += classes.getJSONObject(classMatch[classIndex])
//						.getJSONArray("terms").getJSONObject(i)
//						.getInt("average");
//			}
//			sum = (gradeDesired - 0.5) * 4 - sum;
//			return (int) Math.ceil(sum);
//	}


}