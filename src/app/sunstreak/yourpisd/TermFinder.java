package app.sunstreak.yourpisd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.text.format.DateFormat;

public class TermFinder {

	public enum Term {
		TERM_0 ("1st Six Weeks", "8/19/2013","10/1/2013"),
		TERM_1 ("2nd Six Weeks", "10/2/2013", "11/7/2013"),
		TERM_2 ("3rd Six Weeks", "11/8/2013", "12/20/2013"),
		TERM_3 ("1st Semester Exam", "12/20/2013", "12/20/2013"),
		TERM_4 ("4th Six Weeks", "12/21/2013", "2/21/2014"),
		TERM_5 ("5th Six Weeks", "2/22/2014", "4/17/2014"),
		TERM_6 ("6th Six Weeks", "4/18/2014", "6/6/2014"),
		TERM_7 ("2nd Semester Exam", "6/6/2014", "6/6/2014");
		

        
		public final String name;
		public Date startDate;
		public Date endDate;

		private Term (String name, String startDate, String endDate) {

			this.name = name;
			try {
				this.startDate = dfm.parse(startDate);
				this.endDate = dfm.parse(endDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static final SimpleDateFormat dfm = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

	
	static int getCurrentTermIndex() {
		
		Date d = Calendar.getInstance().getTime();
		
		for (int i = 0; i < Term.values().length; i++) {
			if (d.compareTo(Term.values()[i].startDate) >= 0 && d.compareTo(Term.values()[i].endDate) <= 0)
				return i;
		}
		
		return 0;
	}
	
}
