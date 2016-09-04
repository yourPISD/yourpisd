package app.sunstreak.yourpisd.net.data;


public class ClassReport {
    //TODO: teacher email
    public static int SEMESTER_TERMS = 3;
    public static int NUM_TERMS = SEMESTER_TERMS * 2;

    private final int classID; //Unique ID that identifies which class this is.
    private final TermReport terms[] = new TermReport[NUM_TERMS]; //Terms in this Class.
    private int periodNum;
    private int average = -1;
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

    public int calculateAverage(boolean fallSem)
    {
        //TODO: find the average? and correct weighting
        final double TERM_WEIGHT = .4;
        final double EXAM_WEIGHT = .2;

        int off = 0;
        if (!fallSem)
            off += SEMESTER_TERMS;

        double grade = 0;
        double weight = 0;
        for (int i = 0; i < SEMESTER_TERMS; i++)
        {
            TermReport term = terms[i + off];
            if (term != null)
            {
                grade += term.getGrade(); //TODO: compute grade.
                weight += term.isExam() ? EXAM_WEIGHT : TERM_WEIGHT;
            }
        }

        if (weight == 0)
            return Student.NO_GRADES_ENTERED;
        else
            return (int)Math.round(grade / weight);
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
