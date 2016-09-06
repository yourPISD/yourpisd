package app.sunstreak.yourpisd;

import app.sunstreak.yourpisd.net.data.ClassReport;

public enum Semester {
    FALL, SPRING;

    public static Semester findSemester(int term)
    {
        if (term < ClassReport.SEMESTER_TERMS)
            return FALL;
        else
            return SPRING;
    }
}
