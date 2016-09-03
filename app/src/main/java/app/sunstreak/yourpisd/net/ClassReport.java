package app.sunstreak.yourpisd.net;


import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ClassReport {
    //TODO: teacher email
    public static int NUM_TERMS = 6;

    private final Student student;
    private final String enrollment;
    private final int termIDs[];
    private final int grades[] = new int[NUM_TERMS];
    private int periodNum;
    private String courseName;
    private String teacherName;
    private GregorianCalendar lastUpdate;
    private GregorianCalendar updateTime;

    public ClassReport(Student student, String enrollment, int[] termIDs) {
        this.student = student;
        this.enrollment = enrollment;
        this.termIDs = termIDs;

        for (int i = 0; i < grades.length; i++)
            grades[i] = -1; //Invalid grade.
    }

    public boolean hasNoGrade(int term)
    {
        return grades[term] == -1;
    }

    public int getTermID(int term) {
        return termIDs[term];
    }

    public int getTermGrade(int term)
    {
        return grades[term];
    }

    public void setTermGrade(int term, int grade)
    {
        if (grade < 0)
            grades[term] = -1;
        else
            grades[term] = grade;
    }

    public void loadReport() throws IOException {
        GregorianCalendar now = new GregorianCalendar();

        updateTime.add(Calendar.HOUR, 1);
        if (lastUpdate == null || updateTime == null || updateTime.before(now)) {
            //TODO: load report.
            lastUpdate = (GregorianCalendar) now.clone();
            now.add(Calendar.HOUR, 1);
            updateTime = now;
        }
    }

    public int getPeriodNum() {
        return periodNum;
    }

    public void setPeriodNum(int periodNum) {
        this.periodNum = periodNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public GregorianCalendar getLastUpdate() {
        return lastUpdate;
    }
}
