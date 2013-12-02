package app.sunstreak.yourpisd.net;

import org.joda.time.LocalDate;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class DateHandler {

	public static DateTimeFormatter dtf;
	public static PeriodFormatter pf;
	public static DateTime startOfSchoolYear;

	static {
		dtf = DateTimeFormat.forPattern("MMM dd");
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
		startOfSchoolYear = DateTimeFormat.forPattern("MMM dd yyyy").parseDateTime("Aug 26 2013");
	}

	public static String timeSince (long millis) {
		return timeSince(new DateTime(millis));
	}

	public static String timeSince (DateTime dt) {

		StringBuilder sb = new StringBuilder();
		sb.append("Last updated ");

		Period pd = new Interval(dt.getMillis(), Instant.now().getMillis()).toPeriod();
		if (pd.getDays() > 0) {
			sb.append(pd.getDays());
			return sb.append(" days ago").toString();
		}
		if (pd.getHours() > 0)
			sb.append(pd.getHours() + "hours ");
		if (pd.getMinutes() > 0) {
			sb.append(pd.getMinutes() + " minutes");
			return sb.append(" ago").toString();
		}
		return sb.append("less than a minute ago").toString();

	}
	/**
	 * 
	 * @param date formatted as "MMM dd" (Short month + day)
	 * @return
	 */
	public static String daysRelative (String date) {
		DateTime dt = dtf.parseDateTime(date);
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

}
