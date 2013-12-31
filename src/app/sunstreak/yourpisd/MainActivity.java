package app.sunstreak.yourpisd;

import java.util.Arrays;
import java.util.Locale;

import org.json.JSONArray;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import app.sunstreak.yourpisd.net.DataGrabber;
import app.sunstreak.yourpisd.net.DateHandler;


public class MainActivity extends FragmentActivity implements ActionBar.TabListener { 
	public static final int CURRENT_TERM_INDEX = TermFinder.getCurrentTermIndex();
	static int classCount;
	static RelativeLayout[] averages;
	static int[] goals;
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
	//sections of the navigation drawer
	public String[] mList = {"Profile", "Current Six Weeks", "Grade Overview", "Semester Goals"};
	public DrawerLayout mDrawerLayout;
	public ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
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
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);

		setUpTabs();
		setUpNavigationDrawer();

		AppRater.app_launched(this);

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

	}

	private void setUpTabs () {
		final ActionBar actionBar = getActionBar();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.main_section_0_title))
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(TermFinder.Term.values()[CURRENT_TERM_INDEX].name)
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.main_section_2_title))
				.setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.main_section_3_title))
				.setTabListener(this));
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}
			@Override	public void onPageScrolled(int arg0, float arg1, int arg2) {  }
			@Override	public void onPageScrollStateChanged(int arg0) {  }
		});
	}

	private void setUpNavigationDrawer () {
		//navigation drawer
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list, mList));
		class DrawerItemClickListener implements ListView.OnItemClickListener {
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				mViewPager.setCurrentItem(position);
				mDrawerLayout.closeDrawers();
			}
		}
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		mDrawerToggle = new ActionBarDrawerToggle(
				this,                  /* host Activity */
				mDrawerLayout,         /* DrawerLayout object */
				R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
				R.string.drawer_open,  /* "open drawer" description */
				R.string.drawer_close  /* "close drawer" description */
				);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}

	//fixed tab listener implemented
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());

	}
	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub

	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	@Override
	public void onPause () {
		SharedPreferences.Editor editor = getSharedPreferences(dg.getCurrentStudent().studentId + "", Context.MODE_PRIVATE).edit();
		for (int i = 0; i < goals.length; i++) {
			editor.putInt(Integer.toString(i), goals[i]);
			System.out.println(i + " " + goals[i]);
		}
		editor.commit();

		super.onPause();
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
		// Pass the event to ActionBarDrawerToggle, if it returns
		// true, then it has handled the app icon touch event
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle your other action bar items...
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
			//		case R.id.refresh:
			//			dg.clearData();
			//			Intent intentR = new Intent(this, LoginActivity.class);
			//			intentR.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			//			intentR.putExtra("Refresh", true);
			//			startActivity(intentR);
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
			return 3;
			// Semester goals removed until May 2014.
			//return 4;
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE; 
		} 

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:	return getResources().getString(R.string.main_section_0_title);
			case 1:	return TermFinder.Term.values()[CURRENT_TERM_INDEX].name;
			case 2:	return getResources().getString(R.string.main_section_2_title);
			case 3:	return getResources().getString(R.string.main_section_3_title);
			default: return "";
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
		RelativeLayout[] profileCards;
		private boolean pictureNotLoaded = true;
		public MainActivityFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			// [Hopefully] to prevent force closes.
			if (dg.getStudents().size() == 0) {
				dg.clearData();
				SharedPreferences.Editor editor = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE).edit();
				editor.putBoolean("auto_login", false);
				editor.commit();

				Intent intent = new Intent(getActivity(), LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("show", true);
				startActivity(intent);
			}

			Bundle args = getArguments();
			position = args.getInt(ARG_OBJECT);
			final Typeface robotoNew = Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf");
			int tabLayout = 0;
			switch (position) {
			case 0:
				tabLayout = R.layout.tab_new; 		break;
			case 1:
			case 2:
			case 3:
				tabLayout = R.layout.tab_summary;	break;
			}

			// Semester Goals : Shall remain dormant until May 2014.
			/*
			// Load saved goals.
			goals = new int[dg.getCurrentStudent().getClassMatch().length];
			Arrays.fill(goals, -1);
			SharedPreferences sharedPrefs = getActivity().getSharedPreferences(dg.getCurrentStudent().studentId + "", Context.MODE_PRIVATE);
			for (String key : sharedPrefs.getAll().keySet()) {
				goals[Integer.parseInt(key)] = sharedPrefs.getInt(key, -1);
			}
			 */

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
					instruct.setBackgroundResource(R.drawable.card_custom);
					instruct.addView(instructions);
					bigLayout.addView(instruct, 1);
				}

				profileCards = new RelativeLayout[dg.getStudents().size()];

				for (int i = 0; i < dg.getStudents().size(); i++) {
					profileCards[i] = new RelativeLayout(getActivity());

					ImageView profilePic = new ImageView(getActivity());
					profilePic.setId(MainActivity.id.profile_picture);
					LinearLayout.LayoutParams lpPic = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					lpPic.setMargins(5,5,0,0);
					profilePic.setLayoutParams(lpPic);
					TextView name = new TextView(getActivity());
					name.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
					// TODO use screen-specific text size.
					name.setTextSize(22);
					name.setText(dg.getStudents().get(i).name);
					name.setId(id.name);
					name.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
					name.setGravity(Gravity.CENTER);

					RelativeLayout.LayoutParams lpName = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.MATCH_PARENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					lpName.addRule(RelativeLayout.RIGHT_OF, profilePic.getId());

					double gpaValue = dg.getStudents().get(i).getGPA();
					if (!Double.isNaN(gpaValue)) {
						TextView gpa = new TextView(getActivity());
						gpa.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
						// TODO use screen-specific text size.
						gpa.setTextSize(22);
						gpa.setText(String.format("GPA: %.5f",gpaValue));
						gpa.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
						gpa.setGravity(Gravity.CENTER);

						RelativeLayout.LayoutParams lpGPA = new RelativeLayout.LayoutParams(
								RelativeLayout.LayoutParams.MATCH_PARENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						lpGPA.addRule(RelativeLayout.BELOW, id.name);
						lpGPA.addRule(RelativeLayout.RIGHT_OF, id.profile_picture);

						profileCards[i].addView(gpa, lpGPA);
					}


					profileCards[i].addView(profilePic);
					profileCards[i].addView(name, lpName);;
					profileCards[i].setOnClickListener(new StudentChooserListener(i));
					profileCards[i].setBackgroundResource(R.drawable.card_custom);

					bigLayout.addView(profileCards[i]);

					if (pictureNotLoaded) {
						StudentPictureTask spTask = new StudentPictureTask();
						spTask.execute(i);
					} else {
						Drawable picture = new BitmapDrawable(getResources(), pics[i]);
						profilePic.setImageDrawable(picture);
					}

				}

				if (dg.MULTIPLE_STUDENTS)
					colorStudents();
			}

			if (position == 1) {

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.container);

				// Add current student's name
				if (dg.MULTIPLE_STUDENTS) {
					LinearLayout studentName = (LinearLayout) inflater.inflate(R.layout.main_student_name_if_multiple_students, bigLayout, false);
					( (TextView) studentName.findViewById(R.id.name) ).setText(dg.getStudents().get(dg.studentIndex).name);
					bigLayout.addView(studentName);
				}

				int[] classMatch = dg.getCurrentStudent().getClassMatch();

				classCount = classMatch.length;
				goals = new int[classCount];
				Arrays.fill(goals, -1);

				averages = new RelativeLayout[classCount];

				JSONArray classList = dg.getCurrentStudent().getClassList();

				for (int i = 0; i < classCount; i++) {

					int jsonIndex = classMatch[i];

					averages[i] = (RelativeLayout) inflater.inflate(R.layout.main_grade_summary, bigLayout, false);
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

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.container);

				// Add current student's name
				if (dg.MULTIPLE_STUDENTS) {
					LinearLayout studentName = (LinearLayout) inflater.inflate(R.layout.main_student_name_if_multiple_students, bigLayout, false);
					( (TextView) studentName.findViewById(R.id.name) ).setText(dg.getStudents().get(dg.studentIndex).name);
					bigLayout.addView(studentName);
				}
				LinearLayout weekNames = new LinearLayout(getActivity());
				weekNames.setBackgroundResource(R.drawable.card_custom);
				TextView[] weeks = new TextView[5];
				weekNames.setPadding(15,20,0,20);
				weekNames.setGravity(Gravity.CENTER);
				for(int i = 0; i< weeks.length; i++)
				{
					weeks[i] = new TextView(getActivity());
					weeks[i].setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_header));
					System.out.println(weeks[i].getTextSize());
					switch(i) {
					case 0:		weeks[i].setText("1st"); 	break;
					case 1:		weeks[i].setText("2nd");	break;
					case 2:		weeks[i].setText("3rd");	break;
					case 3:		weeks[i].setText("Exam");	break;
					case 4:		weeks[i].setText("Avg");	break;
					}

					weeks[i].setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));


					weeks[i].setPadding(0, 0, 0, 0);
					LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams((int)((SCREEN_WIDTH-30) / 5), LayoutParams.WRAP_CONTENT);
					llp.setMargins(0, 0, 0, 0);
					weeks[i].setLayoutParams(llp);
					weeks[i].setGravity(Gravity.CENTER);
					weekNames.addView(weeks[i]);
				}

				bigLayout.addView(weekNames);
				int[] classMatch = dg.getCurrentStudent().getClassMatch();

				JSONArray classList = dg.getCurrentStudent().getClassList();

				for (int classIndex = 0; classIndex < classCount; classIndex++) {

					int jsonIndex = classMatch[classIndex];
					View classSummary = inflater.inflate(R.layout.main_grade_summary_linear_layout, bigLayout, false);
					TextView className = (TextView) classSummary.findViewById(R.id.class_name);
					className.setText(dg.getStudents().get(dg.studentIndex).getClassName(jsonIndex));

					LinearLayout summary = (LinearLayout) classSummary.findViewById(R.id.layout_six_weeks_summary);
					summary.setPadding(15,0,15,14);
					double sum = 0;
					int count = 0;
					for (int termIndex = 0; 
							termIndex < classList.optJSONObject(jsonIndex).optJSONArray("terms").length(); 
							termIndex++) {

						// Support for first semester only.
						if (termIndex >= 4)
							break;
						TextView termGrade = new TextView(getActivity());
						termGrade.setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_score));
						termGrade.setClickable(true);
						termGrade.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));

						int width = (int)((SCREEN_WIDTH - 30)/5);
						LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
						if(termIndex == 0)
							llp.setMargins(15, 0, 0, 0);
						termGrade.setLayoutParams(llp);

						int avg = classList.optJSONObject(jsonIndex).optJSONArray("terms").optJSONObject(termIndex).optInt("average", -1);

						termGrade.setOnClickListener(new ClassSwipeOpenerListener(dg.studentIndex, classIndex, termIndex));
						termGrade.setBackgroundResource(R.drawable.grade_summary_click);
						if (avg != -1) {
							termGrade.setText(avg + "");
							sum+=avg;
							count++;
						}
						termGrade.setGravity(Gravity.CENTER);
						summary.addView(termGrade);


					}

					// Display the average.
					int average = classList.optJSONObject(jsonIndex).optInt("firstSemesterAverage", -1);

					if (average != -1) {
						String averageText = Integer.toString(average);

						TextView averageGrade = new TextView(getActivity());

						int width = (int)((SCREEN_WIDTH - 30)/5);
						LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
						llp.setMargins(0, 0, 15, 0);
						averageGrade.setLayoutParams(llp);

						averageGrade.setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_score));
						averageGrade.setClickable(false);
						averageGrade.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
						averageGrade.setGravity(Gravity.CENTER);
						averageGrade.setText(averageText);
						averageGrade.setTextColor(gradeColor(average));

						summary.addView(averageGrade);
					}

					bigLayout.addView(classSummary);
				}

				TextView summaryLastUpdated = new TextView(getActivity());
				String lastUpdatedString = DateHandler.timeSince(dg.getCurrentStudent().getClassList().optJSONObject(0).optLong("summaryLastUpdated"));
				summaryLastUpdated.setText(lastUpdatedString);
				summaryLastUpdated.setPadding(10, 0, 0, 0);
				bigLayout.addView(summaryLastUpdated);

			}

			// Semester Goals : shall remain dormant until May 2014.
			/*
			if (position == 3) {

				LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.container);

				RelativeLayout helpLabel = new RelativeLayout(getActivity());
				helpLabel.setBackgroundResource(R.drawable.card_custom);
				TextView help = new TextView(getActivity());
				help.setText("Exam Grade Needed");
				help.setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_header));
				help.setTypeface(robotoNew);
				help.setGravity(Gravity.RIGHT);
				RelativeLayout.LayoutParams labelParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				RelativeLayout.LayoutParams labelParams1 = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				labelParams1.setMargins(5, 5, 5, 5);
				helpLabel.setLayoutParams(labelParams1);
				labelParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				help.setPadding(0, 20, 20, 10);
				help.setLayoutParams(labelParams);
				helpLabel.addView(help);

				TextView target = new TextView(getActivity());
				target.setText("Goal");
				target.setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_header));
				target.setTypeface(robotoNew);
				target.setGravity(Gravity.LEFT);
				RelativeLayout.LayoutParams targetParams = new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				targetParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				target.setLayoutParams(targetParams);
				target.setPadding(20, 20, 0, 10);
				helpLabel.addView(target);

				layout.addView(helpLabel);

				for (int classIndex = 0; classIndex < classCount; classIndex++) {

					int jsonIndex = dg.getCurrentStudent().getClassMatch()[classIndex];

					RelativeLayout group = new RelativeLayout(getActivity());
					group.setBackgroundResource(R.drawable.card_custom);

					TextView className = new TextView(getActivity());
					className.setText(dg.getCurrentStudent().getShortClassName(jsonIndex));
					className.setTypeface(robotoNew);
					className.setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_header)-5);
					className.setPadding(20,10,0,0);
					final TierView goal = new TierView(getActivity(), robotoNew);

					View minus = new View(getActivity());
					View plus = new View(getActivity());

					minus.setBackgroundResource(R.drawable.navigation_previous_item);
					plus.setBackgroundResource(R.drawable.navigation_next_item);

					final TextView examScore = new TextView(getActivity());
					examScore.setPadding(0,0,20,0);
					examScore.setTextSize(getResources().getDimension(R.dimen.text_size_grade_overview_score));
					examScore.setTypeface(robotoNew);
					examScore.setGravity(Gravity.RIGHT);

					// Try to retrieve data from sharedPrefs, otherwise calculate from scratch.
					if (goals[classIndex] != -1) {
						goal.setText(goals[classIndex]);
					}
					else {
						while(goal.getIndex() != 0 && dg.getCurrentStudent().examScoreRequired(classIndex, TierView.RANGES[goal.getIndex()])>100)
							goal.decrement();
						goals[classIndex] = goal.getIndex();
					}

					examScore.setText("" + dg.getCurrentStudent().examScoreRequired(classIndex, TierView.RANGES[goal.getIndex()]));

					if (Integer.parseInt(examScore.getText().toString()) > 100)
						examScore.setTextColor(getResources().getColor(R.color.red));

					class PlusMinusOnClickListener implements OnClickListener {
						int classIndex;
						int delta;
						PlusMinusOnClickListener(int classIndex, int delta) {
							this.classIndex = classIndex;
							this.delta = delta;
						}
						@Override
						public void onClick(View v) {
							if (delta==1)
								goal.increment();
							else if (delta==-1)
								goal.decrement();

							goals[classIndex] = goal.getIndex();

							examScore.setText(
									"" + dg.getCurrentStudent().examScoreRequired(
											classIndex, TierView.RANGES[goal.getIndex()]));
							if (Integer.parseInt(examScore.getText().toString()) > 100)
								examScore.setTextColor(getResources().getColor(R.color.red));
							else
								examScore.setTextColor(getResources().getColor(R.color.black));
						}
					}

					minus.setOnClickListener(new PlusMinusOnClickListener(classIndex, -1));
					plus.setOnClickListener(new PlusMinusOnClickListener(classIndex, 1));

					// Don't show classes that don't have grades in the 3rd six weeks
					// (ex. James Hannah & senior release, etc.)
					if (examScore.getText().equals("-1"))
						continue;

					// Assign position to class name
					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					className.setLayoutParams(layoutParams);
					className.setId(234254);

					// Put minus under class name
					layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.BELOW, className.getId());
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					minus.setLayoutParams(layoutParams);
					minus.setId(2345235);

					// Put goal to right of minus
					layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.RIGHT_OF, minus.getId());
					layoutParams.addRule(RelativeLayout.ALIGN_TOP, minus.getId());
					layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, minus.getId());
					layoutParams.setMargins(0, 0, 0, 0);
					layoutParams.width = getResources().getInteger(R.integer.width_exam_score);
					goal.setLayoutParams(layoutParams);
					goal.setId(123491);

					// Put plus to right of goal
					layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.RIGHT_OF, goal.getId());
					layoutParams.addRule(RelativeLayout.BELOW, className.getId());
					plus.setLayoutParams(layoutParams);

					// Assign size of plus and minus
					minus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.semester_goals_arrow_size);
					minus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.semester_goals_arrow_size);
					plus.getLayoutParams().width = (int) getResources().getDimension(R.dimen.semester_goals_arrow_size);
					plus.getLayoutParams().height = (int) getResources().getDimension(R.dimen.semester_goals_arrow_size);
					//					minus.setPadding(0, 0, 0, 30);
					layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, goal.getId());
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					examScore.setLayoutParams(layoutParams);

					group.addView(className);
					group.addView(examScore);
					group.addView(minus);
					group.addView(goal);
					group.addView(plus);
					layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.WRAP_CONTENT,
							RelativeLayout.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(5,5,5,5);
					group.setLayoutParams(layoutParams);

					layout.addView(group);

				}
			}
			 */
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
				ImageView profilePic = (ImageView) profileCards[taskStudentIndex].findViewById(MainActivity.id.profile_picture);
				profilePic.setImageBitmap(result);

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
					profileCards[i].setBackgroundResource(R.drawable.card_click_blue);
				else
					profileCards[i].setBackgroundResource(R.drawable.card_click_blue);
			}
		}

		public int gradeColor (int grade) {
			if (grade > 100)
				return getResources().getColor(R.color.black);
			if (grade >= 90)
				return getResources().getColor(R.color.green);
			if (grade >= 80)
				return getResources().getColor(R.color.yellow);

			return getResources().getColor(R.color.red);
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

	private class id {
		public static final int profile_picture = 688;
		public static final int name = 1329482;
	}

}