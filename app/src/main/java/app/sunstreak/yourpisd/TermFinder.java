package app.sunstreak.yourpisd;

public class TermFinder {

    public enum Term {
        TERM_0("1st Nine Weeks"),
        TERM_1("2nd Nine Weeks"),
        TERM_2("1st Semester Exam"),
        TERM_3("3rd Nine Weeks"),
        TERM_4("4th Nine Weeks"),
        TERM_5("2nd Semester Exam");

        public final String name;

        Term(String name) {
            this.name = name;
        }

        public boolean isExam()
        {
            return name.contains("Semester Exam");
        }
    }

    private static int termIndex = 0;

    public static void setCurrentTermIndex(int termIndex) {
        TermFinder.termIndex = termIndex;
    }

    public static int getCurrentTermIndex() {
        return termIndex;
    }


}
