package app.sunstreak.yourpisd.net.data;

/**
 * Represents a grade category
 */
public class GradeCategory {
    public static String NO_CATEGORY = "No category";

    private final String type;
    private final double weight;
    private int grade = -1;

    public GradeCategory(String type, double weight) {
        this.type = type;
        this.weight = weight;
    }

    public String getType() {
        return type;
    }

    public double getWeight() {
        return weight;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GradeCategory that = (GradeCategory) o;

        if (Double.compare(that.weight, weight) != 0) return false;
        return type != null ? type.equals(that.type) : that.type == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = type != null ? type.hashCode() : 0;
        temp = Double.doubleToLongBits(weight);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
