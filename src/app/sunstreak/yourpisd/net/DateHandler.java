package app.sunstreak.yourpisd.net;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.Period;

public class DateHandler {

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
	
}
