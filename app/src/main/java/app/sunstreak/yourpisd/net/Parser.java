package app.sunstreak.yourpisd.net;

import android.support.annotation.NonNull;
import app.sunstreak.yourpisd.TermFinder;
import app.sunstreak.yourpisd.net.data.*;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Parser {

    // Cannot be instantiated.
    private Parser() {
    }

    /**
     * Parses the term report (assignments for a class during a grading period).
     *
     * @param html text of detailed report
     * @param report the TermReport object to store the data into.
     */
    @NonNull
    public static void parseTermReport(String html, TermReport report) {
        if (html == null) {
            System.err.println("No html for parsing detailed grade summary");
            return;
        }
        Document doc = Jsoup.parse(html);
        if (doc == null)
            return;
        Element main = doc.getElementById("Main").getElementById("Content").getElementById("ContentMain");

        //refill grade categories and assignments
        report.getCategories().clear();
        report.getAssignments().clear();

        Element categoryTable = main.getElementById("Categories");
        if (categoryTable != null && categoryTable.children().size() > 0) {
            //For each <TR> element
            for (Element category : categoryTable.children().get(0).children()) {
                report.getCategories().add(new GradeCategory(category.getElementsByClass("description").get(0).html().split("\n")[0].trim(), Integer.parseInt(category.getElementsByClass("percent").get(0).html().replaceAll("[^0-9]", "")) * 0.01));
                report.getCategories().get(report.getCategories().size() - 1).setGrade(Integer.parseInt(category.getElementsByClass("letter").get(1).child(0).child(0).child(0).html().replace("%", "")));
                //Log.d("testTag", report.getCategories().get(report.getCategories().size() - 1).getGrade() + " " + report.getCategories().get(report.getCategories().size() - 1).getWeight() + " " + report.getCategories().get(report.getCategories().size() - 1).getType());
            }

        }

        Element assignments = main.getElementById("Assignments");
        if (assignments != null && assignments.children().size() > 0)
        {
            //an assignment doesn't have to have a due date. If it doesn't, use the last parsed due date
            int month = 1;
            int day = 1;

            //for each assignment
            for (Element assignment : assignments.children().get(0).children())
            {
                String name = assignment.getElementsByClass("title").get(0).html();

                GradeCategory category = report.getCategories().get(0);
                String temp = "";
                try
                {
                    temp = assignment.getElementsByClass("category").get(0).html();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
                for (GradeCategory cc : report.getCategories())
                {
                    if (cc.getType().equals(temp ))
                    {
                        category = cc;
                        break;
                    }
                }

                final int year = 2016; //TODO: year of fall semester
                final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
                try
                {
                    month = Arrays.asList(months).indexOf(assignment.getElementsByClass("m").get(0).html()) + 1;
                    day = Integer.parseInt(assignment.getElementsByClass("m").get(0).parent().html().replaceAll("[^0-9]", ""));
                } catch (Exception e)
                {
                    //e.printStackTrace();
                }
                DateTime date = new DateTime(year, month, day, 0, 0);
                if (date.getMonthOfYear() < 7) //spring semester
                    date.plusYears(1);

                Assignment newAssignment = new Assignment(name, category, date);
                report.getAssignments().add(newAssignment);

                Elements weight = assignment.getElementsByClass("weight");
                if (weight.isEmpty())
                    newAssignment.setWeight(1);
                else
                    newAssignment.setWeight(Double.parseDouble(weight.get(0).html().replaceAll("[^0-9]", "")));

                temp = assignment.getElementsByClass("points").get(0).html();
                if (temp.isEmpty())
                    newAssignment.setGrade(-1); //TODO: Grade doesn't exist
                else
                    newAssignment.setGrade(Double.parseDouble(temp));

                newAssignment.setMaxGrade(Double.parseDouble(assignment.getElementsByClass("max").get(0).html()));


                //Log.d("testTag", "\n" + name + " === " + "\n");
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
        for (Element term : terms) {
            Elements termName = term.getElementsByTag("h2");
            if (termName.isEmpty())
                continue;

            //Search term name in the list of terms
            String tempDate = termName.get(0).getElementsByClass("term").html();
            int termNum = 0;
            for (TermFinder.Term t : TermFinder.Term.values()) {
                if (t.name.equalsIgnoreCase(tempDate)) {
                    termNum = t.ordinal();
                    break;
                }
            }
            maxTermNum = Math.max(maxTermNum, termNum);

            // For each course
            Elements courses = term.children().get(1).children().get(0).children();
            for (Element course : courses) {
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
                for (int i = 0; i < parts.length - 1; i += 2) {
                    if (parts[i].equalsIgnoreCase("Enrollment"))
                        courseID = Integer.parseInt(parts[i + 1]);
                    if (parts[i].equalsIgnoreCase("Term"))
                        termID = Integer.parseInt(parts[i + 1]);
                }

                //Add new class report as needed.
                ClassReport report;
                if (classes.containsKey(courseID))
                    report = classes.get(courseID);
                else{
                    // Teacher name
                    String teacher = courseMain.get(1).children().get(1).getElementsByClass("teacher").get(0).html();
                    report = new ClassReport(courseID, name);
                    report.setPeriodNum(Integer.parseInt(period));
                    report.setTeacherName(teacher);
                    classes.put(courseID, report);
                }

                //Create a new term as needed.
                TermReport termReport = report.getTerm(termNum);
                if (termReport == null)
                {
                    termReport = new TermReport(null, report, termID, false);
                    report.setTerm(termNum, termReport);
                    classes.put(report.getClassID(), report);
                }

                // Course grade for a term (might be empty string if no grade) - formatted as number + %
                int grade;
                if (courseMain.get(2).children().isEmpty())
                    grade = -1; //No grade exists.
                else {
                    String gradeString = courseMain.get(2).children().get(0).children().get(0).children().get(0).html();
                    grade = Integer.parseInt(gradeString.substring(0, gradeString.length() - 1));
                }
                termReport.setGrade(grade);
            }
        }

        TermFinder.setCurrentTermIndex(maxTermNum);
    }


    /**
     * Parses and returns a list of students' informations (name and INTERNAL student id) from the Gradebook.
     *
     * @param sess the session loading the user.
     * @param html the source code for ANY page in Gradebook (usually GradeSummary.aspx)
     * @return the list of students
     */
    @NonNull
    public static List<Student> parseStudents(Session sess, String html) {
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
