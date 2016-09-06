package app.sunstreak.yourpisd.net.data;

import org.joda.time.DateTime;

/**
 * This represents one class assignment.
 */
public class Assignment {

    private final String name;
    private final GradeCategory category;
    private final DateTime dueDate;

    private double weight;
    private double grade;
    private double maxGrade;

    public Assignment(String name, GradeCategory category, DateTime dueDate) {
        this.name = name;
        this.category = category;
        this.dueDate = dueDate;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getMaxGrade() {
        return maxGrade;
    }

    public void setMaxGrade(double maxGrade) {
        this.maxGrade = maxGrade;
    }

    public String getName() {
        return name;
    }

    public GradeCategory getCategory() {
        return category;
    }

    public DateTime getDueDate() {
        return dueDate;
    }
}
