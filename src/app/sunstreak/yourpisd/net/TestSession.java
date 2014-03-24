package app.sunstreak.yourpisd.net;

import java.util.ArrayList;

public class TestSession extends Session {
	public TestSession() {
		domain = Domain.TEST;
		this.domain = Domain.TEST;
		students = new ArrayList<Student>();
		//students = getTestStudents();
		passthroughCredentials = new String[] {"", ""};
		gradebookCredentials = new String[] {"", ""};
		MULTIPLE_STUDENTS = true;
	}
	
	public int login () throws InterruptedException {
		Thread.sleep(500);
		System.out.println("Test login");
		editureLogin = 1;
		return 1;
	}
	
	public int loginGradebook(String userType, String uID, String email, String password) {
		try {
			Thread.sleep(500);
			System.out.println("Test login gradebook");
		} catch (InterruptedException e) {
		}
		gradebookLogin = 1;
		return 1;
	}
	
	/*
	private List<Student> getTestStudents() {


<<<<<<< HEAD:src/app/sunstreak/yourpisd/net/YPSession.java
		class TestStudent extends Student{
=======
		public int[] getClassMatch () {
			return classMatch;
		}

		public double getCumulativeGPA(double oldCumulativeGPA, double numCredits)
		{
			double newNumCredits = numCredits+ 0.5* classMatch.length;
			DecimalFormat df = new DecimalFormat("#.########");
			return Double.parseDouble(df.format((getGPA()*0.5*classMatch.length
					+oldCumulativeGPA*numCredits)/newNumCredits));
		}
		public double getGPA () {
			if (classMatch == null)
				return -2;
>>>>>>> 4b3bb169fc61cee78e7ddb22c3d6234bad91b112:src/app/sunstreak/yourpisd/net/DataGrabber.java


			public TestStudent(int studentId, String studentName) {
				super(studentId, studentName);

				InputStream is = null;

				switch (studentId) {
				case 0:
					is = application.getResources().openRawResource(R.raw.student_0_class_grades);
					break;
				case 1:
					is = application.getResources().openRawResource(R.raw.student_1_class_grades);
					break;
				}

				if (is == null) {
					System.out.println("is = null");
					return;
				}


				Scanner sc = new Scanner(is).useDelimiter("\\A");
				String json = sc.hasNext() ? sc.next() : "";

				try {
					classList = new JSONArray(json);
					classGrades = new SparseArray<JSONObject>();

					for (int i = 0; i < classList.length(); i++) {
						classGrades.put(i, new JSONObject(classList.getJSONObject(i).toString()));
						//						for (int j = 0; j < classList.getJSONObject(i).getJSONArray("terms").length(); j++) {
						//							classGrades.get(i).put(j, classList.getJSONObject(i));
						//						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
					return;
				}

				classMatch = new int[] {0, 1, 2, 3, 4, 5, 6};

			}

			public void loadClassList() {

			}

			public JSONObject getClassGrade(int classIndex, int termIndex) {
				return classGrades.get(classIndex).optJSONArray("terms").optJSONObject(termIndex);
			}

			public int[][] loadGradeSummary() {
				/*
				InputStream is;

				switch (studentId) {
				case 0:
					is = getResources().openRawResource(R.raw.student_0_grade_summary);
					break;
				case 1:
					is = getResources().openRawResource(R.raw.student_1_grade_summary);
					break;
				default:
					return null;
				}

				Scanner sc = new Scanner(is);

				int[][] gradeSummary = new int[7][7];
				for (int i = 0; i < 7; i++) {
					for (int j = 0; j < 7; j++) {
						gradeSummary[i][j] = sc.nextInt();
					}
				}

				matchClasses(gradeSummary);

				return gradeSummary;

				return null;
			}

			public int[] getClassIds() {
				return new int[] {0, 1, 2, 3, 4, 5, 6};
			}

			public int[] getTermIds(int classId) throws JSONException {
				return new int[] {0, 1, 2, 3, 4, 5};
			}



			//			public int[][] getGradeSummary () {
			//				if (gradeSummary == null)
			//					loadGradeSummary();
			//
			//				return gradeSummary;
			//			}


			public Bitmap getStudentPicture() {

				switch (studentId) {
				case 0:
					return BitmapFactory.decodeResource(application.getResources(), R.drawable.student_0);
				case 1:
					return BitmapFactory.decodeResource(application.getResources(), R.drawable.student_1);
				default:
					return null;
				}
			}

		}

		List<Student> students = new ArrayList<Student>();
		students.add(new TestStudent(0, "Griffin, Stewie (0)"));
		students.add(new TestStudent(1, "Griffin, Meg (1)"));

		return students;
	}
	 */

	/*
	public void writeToFile() {
		writeDetailsToFile();
		writeDataToFile();
	}

	private void writeDetailsToFile() {
		String filename = "DATA_GRABBER_DETAILS";
		String string = domain.toString() + "\n" + username + "\n" + password;
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(string.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeDataToFile() {
		String filename = "DATA_GRABBER_DATA";
		String string = "";
		for (Student st : students) {
			string += st.classGrades.toString() + "\n";
		}
		FileOutputStream outputStream;

		try {
			outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
			outputStream.write(string.getBytes());
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 */
}
