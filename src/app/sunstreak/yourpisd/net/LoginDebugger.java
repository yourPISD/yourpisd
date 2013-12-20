package app.sunstreak.yourpisd.net;

import java.util.Scanner;

import app.sunstreak.yourpisd.TermFinder;

public class LoginDebugger {

	static String mEmail;
	static String mPassword;
	
	static int CURRENT_TERM_INDEX;
	
	public static void main(String[] args) throws Exception {

		if (args.length == 2) {
			mEmail = args[0];
			mPassword = args[1];
			System.out.println("|" + mEmail + "|" + mPassword + "|");
		} else {
			Scanner sc = new Scanner(System.in);
			System.out.print("Please enter username:\t");
			mEmail = sc.next();
			System.out.print("Please enter password:\t");
			mPassword = sc.next();
			sc.close();
		}
		
		
		GenericDataGrabber dg = new GenericDataGrabber();
		dg.setData(mEmail, mPassword);

		int loginSuccess = dg.login();
		System.out.println(loginSuccess);


		// Try logging into Gradebook 5 times.
		{
			String[] ptc = dg.getPassthroughCredentials();
			int loginAttempt = 0;
			int counter = 0;
			while (counter < 7 && loginAttempt != 1) {

				try {
					// Only sleep extra if student account.
					System.out.println("sleeping 3.5s");
					Thread.sleep(3500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				loginAttempt = dg.loginGradebook(ptc[0], ptc[1], mEmail, mPassword);

				// Internet connection lost
				if (loginAttempt == -10) {
					System.out.println("No internet connection");
					System.exit(0);
				}

				counter++;
			}

			// If even 7 tries was not enough and still getting NotSet.
			if (loginAttempt == -1) {
				System.out.println("7 tries not enough. Still getting NotSet");
				System.exit(0);
			}
		}

		for (GenericDataGrabber.Student st : dg.getStudents()) {
			st.loadGradeSummary();
			for (int i = 0; i < st.getClassMatch().length; i++) {
				System.out.printf("%20s", st.getClassList().optJSONObject(st.getClassMatch()[i]).getString("title") );
				for (int j = 0; j < 4; j++) {
					System.out.print("\t" + st.getClassList().optJSONObject(st.getClassMatch()[i])
							.optJSONArray("terms").optJSONObject(j).optInt("average", -1));
				}
				System.out.println("\t" + st.getClassList().optJSONObject(st.getClassMatch()[i])
						.optInt("firstSemesterAverage", -1));
			}
		}
		
		
		CURRENT_TERM_INDEX = TermFinder.getCurrentTermIndex();
		System.out.println(CURRENT_TERM_INDEX);
		
		
		
	}

}
