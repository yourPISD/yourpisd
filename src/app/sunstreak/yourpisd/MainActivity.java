package app.sunstreak.yourpisd;

import java.util.Locale;

import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import app.sunstreak.yourpisd.net.DataGrabber;
import app.sunstreak.yourpisd.net.DateHandler;


public class MainActivity extends FragmentActivity { 
	public static final int CURRENT_TERM_INDEX = TermFinder.getCurrentTermIndex();
	static int classCount;
	static LinearLayout[] averages;
	static DataGrabber dg;
	static Bitmap proPic;
	static int SCREEN_HEIGHT;
	static int SCREEN_WIDTH;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find the screen height/width
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		SCREEN_HEIGHT = displaymetrics.heightPixels;
		SCREEN_WIDTH = displaymetrics.widthPixels;

		dg = (DataGrabber) getApplication();


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);


		// For parents with multiple students, show the profile cards first.
		// If we are coming back from ClassSwipeActivity, go to requested section (should be section #1).
		if (dg.MULTIPLE_STUDENTS) {
			if (getIntent().hasExtra("mainActivitySection") )
				mViewPager.setCurrentItem(getIntent().getExtras().getInt("mainActivitySection"));
			else
				mViewPager.setCurrentItem(0);
		}
		// Otherwise, show the current six weeks grades list.
		else
			mViewPager.setCurrentItem(1);

		mViewPager.setOffscreenPageLimit(2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity, menu);

		// Create list of students in Menu.
		if (dg.MULTIPLE_STUDENTS) {
			for (int i = 0; i < dg.getStudents().size(); i++) {
				String name = dg.getStudents().get(i).name;
				MenuItem item = menu.add(name);

				// Set the currently enabled student un-clickable.
				if (i == dg.studentIndex)
					item.setEnabled(false);

				item.setOnMenuItemClickListener(new StudentChooserListener(i));
				item.setVisible(true);
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.log_out:
			dg.clearData();
			SharedPreferences.Editor editor = getSharedPreferences("LoginActivity", Context.MODE_PRIVATE).edit();
			editor.putBoolean("auto_login", false);
			editor.commit();

			Intent intent = new Intent(this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("show", true);
			startActivity(intent);
			return true;
		case R.id.credits:
			Intent intentCred1 = new Intent(this, CreditActivity.class);
			startActivity(intentCred1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new MainActivityFragment();
			Bundle args = new Bundle();
			args.putInt(MainActivityFragment.ARG_OBJECT, position);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 3;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE; 
		} 

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getResources().getString(R.string.main_section_0_title);
			case 1:
				return TermFinder.Term.values()[CURRENT_TERM_INDEX].name;
			case 2:
				return getResources().getString(R.string.main_section_2_title);
			default:
				return null;
			}
		}



	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class MainActivityFragment extends Fragment  {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */

		public static final String ARG_OBJECT = "object";
		private View rootView;
		private int position;
		LinearLayout[] profileCards = new LinearLayout[dg.getStudents().size()];
		private boolean pictureNotLoaded = true;
		public MainActivityFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Bundle args = getArguments();
			position = args.getInt(ARG_OBJECT);

			int tabLayout = 0;
			switch (position) {
			case 0:
				tabLayout = R.layout.tab_new;
				break;
			case 1:
				tabLayout = R.layout.tab_summary;
				break;
			case 2:
				tabLayout = R.layout.tab_year_summary;
				break;
			}



			rootView = inflater.inflate(tabLayout, container, false);


			if (position == 0) {

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.overall);

				if (dg.MULTIPLE_STUDENTS) {
					TextView instructions = new TextView(getActivity());
					LinearLayout instruct = new LinearLayout(getActivity());

					instructions.setPadding(15, 15, 15, 15);
					instructions.setTypeface(Typeface.createFromAsset(getActivity().getAssets()
							,"Roboto-Light.ttf"));
					instructions.setText(R.string.welcome_multiple_students);
					instruct.setBackgroundResource(R.drawable.dropshadow);
					// Han can you format this better?
					instruct.addView(instructions);
					bigLayout.addView(instruct, 1);
				}

				profileCards = new LinearLayout[dg.getStudents().size()];

				for (int i = 0; i < dg.getStudents().size(); i++) {


					//					LinearLayout box = new LinearLayout(getActivity());
					//					box.setOrientation(LinearLayout.VERTICAL);
					profileCards[i] = new LinearLayout(getActivity());

					TextView name = new TextView(getActivity());
					name.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
					name.setTextSize(25);

					Spanned profileCardText = Html.fromHtml (dg.getStudents().get(i).name 
							+ "<br><br>GPA: " 
							+ String.format("%9f",dg.getStudents().get(i).getGPA()));

					name.setText(profileCardText);

					profileCards[i].addView(name);
					profileCards[i].setGravity(Gravity.CENTER_VERTICAL);
					profileCards[i].setOnClickListener(new StudentChooserListener(i));

					profileCards[i].setBackgroundResource(R.drawable.dropshadow);

					bigLayout.addView(profileCards[i]);

					if(pictureNotLoaded)
					{
						StudentPictureTask spTask = new StudentPictureTask();
						spTask.execute(i);
					}
					else
					{
						LinearLayout lv = (LinearLayout)rootView.findViewById(R.id.overall);
						ImageView profilePic = new ImageView(getActivity());


						Drawable picture = new BitmapDrawable(getResources(), pics[i]);
						profilePic.setImageDrawable(picture);
						LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
						profileLP.setMargins(10, 10, 10, 10);
						profilePic.setLayoutParams(profileLP);

						profileCards[i].addView(profilePic, 0);
					}

				}

				if (dg.MULTIPLE_STUDENTS)
					colorStudents();
			}

			if (position == 1)
			{

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.container);

				// Add current student's name
				if (dg.MULTIPLE_STUDENTS) {
					LinearLayout studentName = (LinearLayout) inflater.inflate(R.layout.main_student_name_if_multiple_students, bigLayout, false);
					( (TextView) studentName.findViewById(R.id.name) ).setText(dg.getStudents().get(dg.studentIndex).name);
					bigLayout.addView(studentName);
				}

				//				int[][] gradeSummary = dg.getCurrentStudent().getGradeSummary();

				int[] classMatch = dg.getCurrentStudent().getClassMatch();

				//				classCount = gradeSummary.length;
				classCount = classMatch.length;

				averages = new LinearLayout[classCount];

				JSONArray classList = dg.getCurrentStudent().getClassList();

				for (int i = 0; i < classCount; i++) {

					int jsonIndex = classMatch[i];

					averages[i] = (LinearLayout) inflater.inflate(R.layout.main_grade_summary, bigLayout, false);
					TextView className = (TextView) averages[i].findViewById(R.id.name);
					String name = dg.getStudents().get(dg.studentIndex).getClassName(jsonIndex);
					className.setText(name);

					int avg = classList.optJSONObject(jsonIndex).optJSONArray("terms")
							.optJSONObject(CURRENT_TERM_INDEX).optInt("average", -1);

					// No need to increase overdraw if there is nothing to display
					if (avg != -1) {
						String average = avg + "";
						TextView grade = (TextView) averages[i].findViewById(R.id.grade);
						grade.setText(average);
					}

					averages[i].setOnClickListener(new ClassSwipeOpenerListener(dg.studentIndex, i, CURRENT_TERM_INDEX));

					bigLayout.addView(averages[i]);
				}

				TextView summaryLastUpdated = new TextView(getActivity());
				String lastUpdatedString = DateHandler.timeSince(dg.getCurrentStudent().getClassList().optJSONObject(0).optLong("summaryLastUpdated"));
				summaryLastUpdated.setText(lastUpdatedString);
				summaryLastUpdated.setPadding(10, 0, 0, 0);
				bigLayout.addView(summaryLastUpdated);

				return rootView;
			}	


			if (position == 2) {

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.layout_year_summary);

				// Add current student's name
				if (dg.MULTIPLE_STUDENTS) {
					LinearLayout studentName = (LinearLayout) inflater.inflate(R.layout.main_student_name_if_multiple_students, bigLayout, false);
					( (TextView) studentName.findViewById(R.id.name) ).setText(dg.getStudents().get(dg.studentIndex).name);
					bigLayout.addView(studentName);
				}
				LinearLayout weekNames = new LinearLayout(getActivity());
				weekNames.setBackgroundResource(R.drawable.dropshadow);
				TextView[] weeks = new TextView[5];
				//				weekNames.setPadding(25, 5, 15, 20);
				weekNames.setPadding(15,20,0,20);
				weekNames.setGravity(Gravity.CENTER);
				for(int i = 0; i< weeks.length; i++)
				{
					weeks[i] = new TextView(getActivity());
					//					weeks[i].setTextSize(25);
					weeks[i].setTextSize(SCREEN_WIDTH / 30);
					switch(i) {
					case 0:
						weeks[i].setText("1st");
						break;
					case 1:
						weeks[i].setText("2nd");
						break;
					case 2:
						weeks[i].setText("3rd");
						break;
					case 3:
						weeks[i].setText("Exam");
						break;
					case 4:
						weeks[i].setText("Avg");
						break;
					}

					weeks[i].setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));


					//					weeks[i].setPadding(5, 5, 5, 5);
					weeks[i].setPadding(0, 0, 0, 0);
					LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams((int)((SCREEN_WIDTH-30) / 5), LayoutParams.WRAP_CONTENT);
					//				    llp.setMargins(0, 0, 5, 0);
					llp.setMargins(0, 0, 0, 0);
					weeks[i].setLayoutParams(llp);
					weeks[i].setGravity(Gravity.CENTER);
					weekNames.addView(weeks[i]);
				}

				bigLayout.addView(weekNames);
				int[] classMatch = dg.getCurrentStudent().getClassMatch();
				//				int[][] gradeSummary = dg.getStudents().get(dg.studentIndex).getGradeSummary();

				JSONArray classList = dg.getCurrentStudent().getClassList();

				for (int classIndex = 0; classIndex < classCount; classIndex++) {

					int jsonIndex = classMatch[classIndex];

					View classSummary = inflater.inflate(R.layout.main_grade_summary_linear_layout, bigLayout, false);

					TextView className = (TextView) classSummary.findViewById(R.id.class_name);

					className.setText(dg.getStudents().get(dg.studentIndex).getClassName(jsonIndex));


					LinearLayout summary = (LinearLayout) classSummary.findViewById(R.id.layout_six_weeks_summary);
					//					summary.setPadding(20, 5, 15, 10);
					summary.setPadding(15,0,15,18);
					double sum = 0;
					int count = 0;
					for (int termIndex = 0; 
							termIndex < classList.optJSONObject(jsonIndex).optJSONArray("terms").length(); 
							termIndex++) {

						// Support for first semester only.
						if (termIndex >= 4)
							break;

						TextView termGrade = new TextView(getActivity());
						termGrade.setTextSize(SCREEN_WIDTH / 20);
						termGrade.setClickable(true);
						termGrade.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
						//						termGrade.setOnClickListener(new ClassSwipeOpenerListener(dg.studentIndex, classIndex, termIndex - 1));
						//						termGrade.setBackgroundResource(R.drawable.grade_summary_click);
						//						termGrade.setPadding(5, 5, 5, 5);

						int width = (int)((SCREEN_WIDTH - 30)/5);
						//						System.out.println(width);
						LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
						llp.setMargins(0, 0, 0, 0);
						termGrade.setLayoutParams(llp);

						int avg = classList.optJSONObject(jsonIndex).optJSONArray("terms").optJSONObject(termIndex).optInt("average", -1);

						termGrade.setOnClickListener(new ClassSwipeOpenerListener(dg.studentIndex, classIndex, termIndex));
						termGrade.setBackgroundResource(R.drawable.grade_summary_click);
						if (avg != -1) {
							termGrade.setText(avg + "");
							sum+=avg;
							count++;
						}
						//might be too excessive to do this for every six weeks
//						if(avg>=90)
//							termGrade.setTextColor(getResources().getColor(R.color.green));
//						else
//							termGrade.setTextColor(getResources().getColor(R.color.red));
						termGrade.setGravity(Gravity.CENTER);
						summary.addView(termGrade);


					}

					// Display the average.
					if(count > 0) {
						double average = sum/count;
						String averageText = Math.round(average)+"";
						TextView termGrade = new TextView(getActivity());
						
						int width = (int)((SCREEN_WIDTH - 30)/5);
						LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
						llp.setMargins(0, 0, 0, 0);
						termGrade.setLayoutParams(llp);
						
						termGrade.setTextSize(SCREEN_WIDTH / 20);
						termGrade.setClickable(true);
						termGrade.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
						termGrade.setGravity(Gravity.CENTER);
						termGrade.setText(averageText);
						//color coded grades yay first comment lol
						if(Integer.parseInt(averageText)>=90)
							termGrade.setTextColor(getResources().getColor(R.color.green));
						else
							termGrade.setTextColor(getResources().getColor(R.color.red));
						
						summary.addView(termGrade);
					}

					bigLayout.addView(classSummary);
				}

				TextView summaryLastUpdated = new TextView(getActivity());
				String lastUpdatedString = DateHandler.timeSince(dg.getCurrentStudent().getClassList().optJSONObject(0).optLong("summaryLastUpdated"));
				summaryLastUpdated.setText(lastUpdatedString);
				summaryLastUpdated.setPadding(10, 0, 0, 0);
				bigLayout.addView(summaryLastUpdated);

			}
			return rootView;
		}

		static Bitmap[] pics = new Bitmap[dg.getStudents().size()];

		public class StudentPictureTask extends AsyncTask<Integer, Void, Bitmap> {

			int taskStudentIndex;

			@Override
			protected Bitmap doInBackground(Integer... args) {	
				taskStudentIndex = args[0];
				pics[taskStudentIndex]= dg.getStudents().get(taskStudentIndex).getStudentPicture();
				return dg.getStudents().get(taskStudentIndex).getStudentPicture();
			}

			@Override
			protected void onPostExecute (final Bitmap result) {
				LinearLayout lv = (LinearLayout)rootView.findViewById(R.id.overall);
				ImageView profilePic = new ImageView(getActivity());


				Drawable picture = new BitmapDrawable(getResources(), result);
				profilePic.setImageDrawable(picture);
				LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				profileLP.setMargins(10, 10, 10, 10);
				profilePic.setLayoutParams(profileLP);

				profileCards[taskStudentIndex].addView(profilePic, 0);

				pictureNotLoaded = false;
			}
		}

		class ClassSwipeOpenerListener implements OnClickListener {

			int studentIndex;
			int classIndex;
			int termIndex;

			ClassSwipeOpenerListener (int studentIndex, int classIndex, int termIndex) {
				this.studentIndex = studentIndex;
				this.classIndex = classIndex;
				this.termIndex = termIndex;
			}

			public void onClick(View arg0) {
				Intent intent = new Intent (getActivity(), ClassSwipeActivity.class);
				intent.putExtra("studentIndex", this.studentIndex);
				intent.putExtra("classCount", classCount);
				intent.putExtra("classIndex", this.classIndex);
				intent.putExtra("termIndex", this.termIndex);
				startActivity(intent);
			}
		}



		class StudentChooserListener implements OnClickListener {
			int studentIndex;

			StudentChooserListener(int studentIndex) {
				this.studentIndex = studentIndex;
			}

			@Override
			public void onClick(View v) {
				dg.studentIndex = this.studentIndex;
				colorStudents();

				((MainActivity)getActivity()).refresh();
			}

		}


		public void colorStudents() {
			for (int i = 0; i < profileCards.length; i++) {
				// Display the chosen student in a different color.
				if (i == dg.studentIndex)
					profileCards[i].setBackgroundResource(R.drawable.dropshadow_yellow_to_blue);
				else
					profileCards[i].setBackgroundResource(R.drawable.dropshadow_white_to_blue);
			}
		}

	}

	public void refresh() {
		this.mSectionsPagerAdapter.notifyDataSetChanged();
		invalidateOptionsMenu();
	}

	public int screenHeight() {
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size); 
		return size.x;
	}

	public int screenWidth() {
		WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size); 
		return size.y;
	}

	class StudentChooserListener implements OnMenuItemClickListener {

		int studentIndex;

		StudentChooserListener (int studentIndex) {
			this.studentIndex = studentIndex;
		}

		@Override
		public boolean onMenuItemClick(MenuItem arg0) {
			dg.studentIndex = this.studentIndex;

			refresh();
			return true;
		}
	}
}