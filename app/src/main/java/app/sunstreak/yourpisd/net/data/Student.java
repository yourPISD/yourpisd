package app.sunstreak.yourpisd.net.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.joda.time.DateTime;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.sunstreak.yourpisd.net.Parser;
import app.sunstreak.yourpisd.net.Session;

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

        String[] split = className.split("[\\s()\\d/]+");

        for (String aSplit : split) {
            if (aSplit.equals("AP") || aSplit.equals("IB"))
                return 5;
            if (aSplit.equals("H") || aSplit.equals("IH"))
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
    public void loadGradeSummary() throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("Student", "" + studentId);
        Parser.parseGradeSummary(session.request("/InternetViewer/GradeSummary.aspx", params), classes);
        lastUpdated = new DateTime();
    }

    public ClassReport getClassReport(int classID) {
        return classes.get(classID);
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public List<ClassReport> getSemesterClasses(boolean fall) {

        List<ClassReport> classesForSemester = new ArrayList<>();
        if (lastUpdated == null)
            throw new RuntimeException("Grade Summary has not been fetched.");

        int termOffset = fall ? 0 : ClassReport.SEMESTER_TERMS;
        for (ClassReport report : classes.values()) {
            boolean found = false;
            for (int term = 0; term < ClassReport.SEMESTER_TERMS; term++) {
                if (!report.isClassDisabledAtTerm(term + termOffset)) {
                    found = true;
                    break;
                }
            }
            if (found)
                classesForSemester.add(report);
        }
        Collections.sort(classesForSemester, new Comparator<ClassReport>() {
            @Override
            public int compare(ClassReport a, ClassReport b) {
                if (a.getPeriodNum() == b.getPeriodNum())
                    return a.getCourseName().compareTo(b.getCourseName());
                else
                    return a.getPeriodNum() - b.getPeriodNum();
            }
        });
        return classesForSemester;
    }

    private void loadStudentPicture() throws IOException {
        try {
            byte[] data = Jsoup.connect("https://gradebook.pisd.edu/Pinnacle/Gradebook/common/picture.ashx")
                    .method(Connection.Method.GET).data("studentId", "" + studentId)
                    .cookies(session.getCookies()).ignoreContentType(true).execute().bodyAsBytes();

            studentPictureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
        } catch (HttpStatusException ex) {
            ex.printStackTrace();
        }
    }

    public Bitmap getStudentPicture() {
        if (studentPictureBitmap == null)
            try {
                loadStudentPicture();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

            if (grade >= 70) {
                //Passing class.
                double classGPA = maxGPA(report.getCourseName()) - gpaDifference(grade);
                pointSum += classGPA;
                pointCount++;
            } else if (grade >= 0) {
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
        if (grade < 0)
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