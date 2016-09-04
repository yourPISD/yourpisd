package app.sunstreak.yourpisd.net.data;

import app.sunstreak.yourpisd.net.data.TermReport;

public class ClassReport {
    //TODO: teacher email
    public static int NUM_TERMS = 6;

    private final int classID; //Unique ID that identifies which class this is.
    private final TermReport terms[] = new TermReport[NUM_TERMS]; //Terms in this Class.
    private int periodNum;
    private String courseName;
    private String teacherName;


    public ClassReport(int classID) {
        this.classID = classID;
    }

    public TermReport getTerm(int termNum)
    {
        return terms[termNum];
    }

    public boolean isClassDisabledAtTerm(int termNum)
    {
        return terms[termNum] == null;
    }

    public void setTerm(int termNum, TermReport term)
    {
        terms[termNum] = term;
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

    public int getClassID() {
        return classID;
    }
}
