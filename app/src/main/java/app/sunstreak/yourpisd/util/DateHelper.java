package app.sunstreak.yourpisd.util;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DateHelper {

    public static DateTimeFormatter dtf;
    public static PeriodFormatter pf;
    public static DateTimeFormatter humanFormat;
    public static DateTimeFormatter webFormat;
    public static DateTimeFormatter webFormatWithSpaces;
    public static DateTime startOfSchoolYear;

    static {
        dtf = DateTimeFormat.forPattern("MMM dd");
        humanFormat = DateTimeFormat.forPattern("E MMM d");
        webFormat = DateTimeFormat.forPattern("yyyy-M-dd");
        webFormatWithSpaces = DateTimeFormat.forPattern("MMM dd yyyy");
        pf = new PeriodFormatterBuilder()
                .printZeroNever()
                .appendMonths()
                .appendSuffix(" month", " months")
                .appendSeparator(", ")
                .printZeroNever()
                .appendWeeks()
                .appendSuffix(" week", " weeks")
                .appendSeparator(", ")
                .appendDays()
                .appendSuffix(" day", " days")
                .toFormatter();
        startOfSchoolYear = webFormatWithSpaces.parseDateTime("Aug 26 2013");
    }

    public static String timeSince(long millis) {
        return timeSince(new DateTime(millis));
    }

    public static String timeSince(DateTime dt) {

        StringBuilder sb = new StringBuilder();
        sb.append("Last updated ");

        Period pd = new Interval(dt.getMillis(), Instant.now().getMillis()).toPeriod();
        if (pd.getDays() > 0) {
            sb.append(pd.getDays());
            return sb.append(pd.getDays() == 1 ? " day ago" : " days ago").toString();
        }
        if (pd.getHours() > 0)
            sb.append(pd.getHours()).append(pd.getHours() == 1 ? " hour " : " hours ");
        if (pd.getMinutes() > 0)
            sb.append(pd.getMinutes()).append(pd.getMinutes() == 1 ? " minute " : " minutes ");
        if (pd.getHours() > 0 || pd.getMinutes() > 0)
            return sb.append("ago").toString();
        else
            return sb.append("less than a minute ago").toString();

    }

    /**
     *
     * @param date formatted as "MMM dd" (Short month + day)
     * @return
     */
    public static String daysRelative(String date) {
        DateTime dt = dtf.parseDateTime(date);
        return daysRelative(dt);
    }

    public static String daysRelative(DateTime dt) {
        while (dt.isBefore(startOfSchoolYear))
            dt = dt.plusYears(1);

        // if today
        if (dt.toLocalDate().isEqual(new LocalDate()))
            return "(today)";

        Period pd;
        if (dt.isBeforeNow())
            pd = new Interval(dt, new LocalDate().toDateTimeAtStartOfDay()).toPeriod();
        else
            pd = new Interval(new LocalDate().toDateTimeAtStartOfDay(), dt).toPeriod();
        StringBuilder sb = new StringBuilder("\n(");

        int compare = dt.compareTo(new DateTime());

        sb.append(pf.print(pd));
        // Compare to now.
        if (dt.isBeforeNow())
            sb.append(" ago)");
        else
            sb.append(" from now)");
        return sb.toString();
    }

    public static String toHumanDate(String webDate) {
        return webFormat.parseDateTime(webDate).toString(humanFormat);
    }

    public static boolean isAprilFools() {
        MonthDay now = MonthDay.now();
        return (now.getDayOfMonth() == 1 || now.getDayOfMonth() == 2) && now.getMonthOfYear() == 4;

    }

}
