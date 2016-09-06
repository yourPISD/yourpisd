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
		TERM_0 ("1st Nine Weeks"),
		TERM_1 ("2nd Nine Weeks"),
		TERM_2 ("1st Semester Exam"),
		TERM_3 ("3rd Nine Weeks"),
		TERM_4 ("4th Nine Weeks"),
		TERM_5 ("2nd Semester Exam");
		
		public final String name;

		private Term (String name) {
			this.name = name;
		}
	}

	private static int termIndex = 0;

	public static void setCurrentTermIndex(int termIndex)
	{
		TermFinder.termIndex = termIndex;
	}

	public static int getCurrentTermIndex() {
		return termIndex;
	}
	
}
