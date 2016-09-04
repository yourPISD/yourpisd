package app.sunstreak.yourpisd.net.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.sunstreak.yourpisd.net.Parser;
import app.sunstreak.yourpisd.net.Session;

/**
 * Created by Henry on 9/3/2016.
 */
public class TermReport {
    private final Student student;
    private final ClassReport classGrades;
    private final int termID;
    private int grade = -1;
    private ArrayList<GradeCategory> categories = new ArrayList<>();
    private ArrayList<Assignment> assignments = new ArrayList<>();

    private GregorianCalendar lastUpdate; //Time when we last update the detailed assignments
    private GregorianCalendar updateTime; //Time when we will need to update details again.

    public TermReport(Student student, ClassReport classGrades, int termID) {
        this.student = student;
        this.classGrades = classGrades;
        this.termID = termID;
    }

    public void loadReport(Session session) throws IOException {
        GregorianCalendar now = new GregorianCalendar();

        updateTime.add(Calendar.HOUR, 1);
        if (lastUpdate == null || updateTime == null || updateTime.before(now)) {
            Map<String, String> params = new HashMap<>();
            params.put("Student", "" + student.studentId);
            params.put("Enrollment", "" + classGrades.getClassID());
            params.put("Term", "" + termID);
            Parser.parseTermReport(session.request("/InternetViewer/StudentAssignments.aspx", params), this);
            lastUpdate = (GregorianCalendar) now.clone();
            now.add(Calendar.HOUR, 1);
            updateTime = now;
        }
    }

    public List<GradeCategory> getCategories()
    {
        return categories;
    }

    public Student getStudent() {
        return student;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }
}
