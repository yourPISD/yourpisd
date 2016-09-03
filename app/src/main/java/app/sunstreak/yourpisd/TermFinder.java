/**
 * This file is part of yourPISD.
 *
 *  yourPISD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  yourPISD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with yourPISD.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.sunstreak.yourpisd;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TermFinder {

	// TODO Hardcoded for 2016-2017 school year
	public enum Term {
		TERM_0 ("1st Nine Weeks", "8/22/2016","10/14/2016"),
		TERM_1 ("2nd Nine Weeks", "10/14/2016", "12/15/2016"),
		TERM_3 ("1st Semester Exam", "12/15/2016", "1/3/2017"),
		TERM_4 ("3rd Nine Weeks", "1/3/2017", "3/21/2017"),
		TERM_5 ("4th Nine Weeks", "3/21/2017", "6/1/2017"),
		TERM_7 ("2nd Semester Exam", "6/1/2017", "6/2/2017");
		
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

	public static int getCurrentTermIndex() {
		
		Date d = Calendar.getInstance().getTime();
		Term[] vals = Term.values();

		if (d.compareTo(vals[0].startDate) < 0)
			return 0;
		else if (d.compareTo(vals[vals.length - 1].startDate) > 0)
			return vals.length - 1;

		for (int i = 0; i < vals.length; i++) {
			if (d.compareTo(vals[i].startDate) >= 0 && d.compareTo(vals[i].endDate) <= 0)
				return i;
		}

		//Shouldn't get over here.
		return 0;
	}
	
}
