package app.sunstreak.yourpisd.net;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.SparseArray;

public class AttendanceData {
	public static final String[] COLS = {"Unexcused Absence", "Excused Absence",
		"School Absence", "Unexcused Tardy", "Excused Tardy"};
	public static final int NUM_COLS = COLS.length;
	/**
	 * 1 Jan 2014
	 */
	public static String START_OF_SPRING_SEMESTER = "Wed+1%2F1%2F2014";
	/**
	 * 11 June 2014
	 */
	public static String END_OF_SPRING_SEMESTER = "Wed+6%2F11%2F2014";

	Session session;

	SparseArray<String> classNames;
	TreeMap<String,List<AttendanceEvent>> eventsByDate;
	SparseArray<AttendancePeriod> eventsByPeriod;
	Element doc;

	AttendanceData (Session session, String html) {
		eventsByDate = new TreeMap<String, List<AttendanceEvent>>();
		eventsByPeriod = new SparseArray<AttendancePeriod>();
		classNames = new SparseArray<String>();

		this.session = session;

		doc = Jsoup.parse(html);

		if (doc.getElementsByTag("title").get(0).text().trim().equals("Error")) {
			throw new RuntimeException ("Gradebook error: " + toString());
		}

		parseValidation();
	}

	public TreeMap<String, List<AttendanceEvent>> getEventsByDate() {
		if (eventsByDate == null)
			throw new IllegalStateException();
		return eventsByDate;
	}

	public SparseArray<AttendancePeriod> getEventsByPeriod() {
		if (eventsByPeriod == null)
			throw new IllegalStateException();
		return eventsByPeriod;
	}

	public void parseDetailedView() {
		Element table = doc.getElementById("AttendanceDetaild");
		List<Integer> classes = new ArrayList<Integer>();
		{
			Elements classesElements = table.child(0).child(1).children();
			Pattern p = Pattern.compile("(.+)" + Pattern.quote("(") + "(\\d+)" + Pattern.quote(")"));
			for (int i = 1; i < classesElements.size(); i++) {
				String className = classesElements.get(i).text();
				Matcher m = p.matcher(className);
				if (m.find()) {
					int period = Integer.parseInt(m.group(2));
					classes.add(period);
					classNames.append(period, m.group(1));
				} else {
					throw new RuntimeException(String.format("Class %s does not have a period", className));
				}
			}
		}
		Elements attendanceDays = table.child(1).children();
		for (Element attendanceDay : attendanceDays) {
			String day = attendanceDay.children().get(0).text();
			Elements periods = attendanceDay.children();
			for (int i = 1; i < periods.size(); i++) {
				Element period  = periods.get(i);
				if (period.hasText()) {
					//int periodInt = Integer.parseInt(period.text());

					AttendanceEvent event = new AttendanceEvent(day, period.text(), classes.get(i-1));
					AttendancePeriod periodObj;
					if (eventsByPeriod.indexOfKey(event.period) >= 0)
						periodObj = eventsByPeriod.get(event.period);
					else {
						periodObj = new AttendancePeriod(classes.get(i-1), classNames.get(i-1));
						eventsByPeriod.append(event.period, periodObj);
					}
					//System.out.println(event);
					periodObj.add(event);

					List<AttendanceEvent> eventsOnDay;
					if (eventsByDate.containsKey(day)) {
						eventsOnDay = eventsByDate.get(day);
					} else {
						eventsOnDay = new ArrayList<AttendanceEvent>();
						eventsByDate.put(day, eventsOnDay);
					}
					eventsOnDay.add(event);
				}
			}
		}
	}



	public void printDetailedView () {
		System.out.println("Events by Period:");
		for (int i = 0; i < eventsByPeriod.size(); i++) {
			int period = eventsByPeriod.keyAt(i);
			System.out.printf("Period %d:\n", period);
			for (AttendanceEvent e : eventsByPeriod.get(period)) {
				System.out.println(e);
			}
		}

		System.out.println("\nEvents by Date:");

		for (Map.Entry<String,List<AttendanceEvent>> entry : eventsByDate.entrySet()) {
			String date = entry.getKey();
			System.out.printf("%s:\n", date);
			for (AttendanceEvent e : entry.getValue()) {
				System.out.println(e);
			}
		}
	}

	public void parseValidation () {
		session.viewState = doc.getElementById("__VIEWSTATE").attr("value");

		Element event = doc.getElementById("__EVENTVALIDATION");
		if (event != null)
			session.eventValidation = event.attr("value");

		session.pageUniqueId = Parser.pageUniqueId(doc);
	}

	public String toString() {
		return String.format("View State: %s\nEvent validation: %s", 
				session.viewState, session.eventValidation);
	}

	public static class AttendanceEvent {
		public static final String[] ATTENDANCE_CODE_DOES_NOT_COUNT_TOWARDS_EXEMPTIONS = new String[]
				{"ABC",	"ACI", "ACL", "ACR", "ADN",
			"AEC", "AEE", "AFT", "AIS", "AOC",
			"ARH", "ASO", "ATS", "NS"};

		String date;
		boolean isAbsence; // True if absence, false if tardy
		boolean countsAgainstExemptions;
		int period;

		public AttendanceEvent (String date, String code, int period) {
			this.date = date;
			this.period = period;

			code = code.substring(code.indexOf("-")+1);
			isAbsence = !code.equals("T");

			countsAgainstExemptions = true;
			for (String codeThatDoesNotCount : ATTENDANCE_CODE_DOES_NOT_COUNT_TOWARDS_EXEMPTIONS) {
				if (code.equals(codeThatDoesNotCount)) {
					countsAgainstExemptions = false;
					break;
				}
			}
		}

		public boolean isAbsence() { return isAbsence; 	}

		public boolean countsAgainstExemptions() { return countsAgainstExemptions; }


		public int getPeriod() {
			return period;
		}

		public String toString() {
			return String.format("%s: Period %d: %s %s", date, period, isAbsence?"A":"T", countsAgainstExemptions?":(":":)");
		}
	}

	public static class AttendancePeriod extends ArrayList<AttendanceEvent> {
		private int period;
		private String name;

		public static final int TARDIES_INDEX = 0;
		public static final int ABSENCES_INDEX = 1;
		public static final int SCHOOL_ABSENCES_INDEX = 2;

		public AttendancePeriod(int period, String name) {
			this.period = period;
			this.name = name;
		}

		public String getClassName() {
			return name;
		}

		public int[] getAttendanceTotals () {
			int tardies = 0;
			int goodAbs = 0;
			int badAbs = 0;

			for (AttendanceEvent e : this) {
				if (e.isAbsence()) {
					if (e.countsAgainstExemptions())
						badAbs++;
					else
						goodAbs++;
				} else {
					tardies++;
				}
			}

			return new int[]{tardies, goodAbs, badAbs};
		}

	}

}