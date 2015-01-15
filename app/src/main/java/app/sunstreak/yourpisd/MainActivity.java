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

import java.util.Arrays;

import org.json.JSONArray;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import app.sunstreak.yourpisd.googleutil.SlidingTabLayout;
import app.sunstreak.yourpisd.net.Session;
import app.sunstreak.yourpisd.net.Student;
import app.sunstreak.yourpisd.util.DateHelper;
import app.sunstreak.yourpisd.util.RandomStuff;
import app.sunstreak.yourpisd.view.MyTextView;

public class MainActivity extends ActionBarActivity implements
        ActionBar.TabListener {

    public static boolean isTroll;

    public static final int CURRENT_TERM_INDEX = TermFinder
            .getCurrentTermIndex();
    static int classCount;
    static RelativeLayout[] averages;
    static int[] goals;
    static Session session;
    static Bitmap proPic;
    static int SCREEN_HEIGHT;
    static int SCREEN_WIDTH;

    static SummaryFragment mSummaryFragment;
    static YPMainFragment[] mFragments;
    public static final int NUM_FRAGMENTS = 3;
    public static final int NUM_SUMMARY_FRAGMENTS = 2;
    public static final int SUMMARY_FRAGMENT_POSITION = 2;
    public static final int ATTENDANCE_FRAGMENT_POSITION = 3;
    static int currentSummaryFragment;

    // private static AttendanceTask attendanceTask;
    private static boolean isAttendanceLoaded;

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
    // sections of the navigation drawer
    public String[] mList = {"Profile", "Current Six Weeks", "Grade Overview"/*
                                                                             * ,
																			 * "Semester Goals"
																			 */};
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    public SlidingTabLayout slidingTabLayout;
    public Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        // Find the screen height/width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        SCREEN_HEIGHT = displaymetrics.heightPixels;
        SCREEN_WIDTH = displaymetrics.widthPixels;
        session = ((YPApplication) getApplication()).session;

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        FragmentManager fm = getSupportFragmentManager();
        mSectionsPagerAdapter = new SectionsPagerAdapter(fm);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        /*
        Deprecated as of 1/13/2015. @Han Li
         */
//		setUpTabs();
        setUpNavigationDrawer();
        AppRater.app_launched(this);
        mFragments = new YPMainFragment[NUM_FRAGMENTS];
        // Opens Spring semester summary by default.
        currentSummaryFragment = 1;

        for (int position = 0; position < NUM_FRAGMENTS; position++) {
            if (position == SUMMARY_FRAGMENT_POSITION) {
                mFragments[position] = new PlaceholderFragment();
            }
            // else if (position == ATTENDANCE_FRAGMENT_POSITION) {
            // mFragments[position] = new AttendanceFragment();
            // }
            else {
                mFragments[position] = new MainActivityFragment();
                Bundle args = new Bundle();
                args.putInt(MainActivityFragment.ARG_OBJECT, position);
                mFragments[position].setArguments(args);
            }
        }
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //
        setUpMaterialTabs();
        // For parents with multiple students, show the profile cards first.
        // If we are coming back from ClassSwipeActivity, go to requested
        // section (should be section #1).
        if (session.MULTIPLE_STUDENTS) {
            if (getIntent().hasExtra("mainActivitySection"))
                mViewPager.setCurrentItem(getIntent().getExtras().getInt(
                        "mainActivitySection"));
            else
                mViewPager.setCurrentItem(0);
        }
        // Otherwise, show the current six weeks grades list.
        else
            mViewPager.setCurrentItem(1);

        isAttendanceLoaded = false;
        // attendanceTask = new AttendanceTask();
        // attendanceTask.execute();

        MyTextView.typeface = Typeface.createFromAsset(getAssets(),
                "Roboto-Light.ttf");
        if (DateHelper.isAprilFools()) {
            setUpTroll();
        }
    }

    private void setUpTroll() {
        MyTextView.typeface = Typeface.createFromAsset(getAssets(),
                "Comic-Sans.ttf");
    }

    private void setUpMaterialTabs() {
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setDistributeEvenly(true);
//        slidingTabLayout.setScrollBarSize(5);
        slidingTabLayout.setBackgroundColor(getResources().getColor((R.color.blue_500)));
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.green_500));
        slidingTabLayout.setViewPager(mViewPager);
    }

    private void setUpTabs() {
        final ActionBar actionBar = getActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar
                .addTab(actionBar
                        .newTab()
                        .setText(
                                getResources().getString(
                                        R.string.main_section_0_title))
                        .setTabListener(this));
        actionBar.addTab(actionBar.newTab()
                .setText(TermFinder.Term.values()[CURRENT_TERM_INDEX].name)
                .setTabListener(this));
        actionBar
                .addTab(actionBar
                        .newTab()
                        .setText(
                                getResources().getString(
                                        R.string.main_section_2_title))
                        .setTabListener(this));
        // actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.main_section_3_title))
        // .setTabListener(this));
        mViewPager
                .setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // on changing the page
                        // make respected tab selected
                        actionBar.setSelectedNavigationItem(position);
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
    }

    private void setUpNavigationDrawer() {
        // navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list, mList));
        class DrawerItemClickListener implements ListView.OnItemClickListener {
            @Override
            public void onItemClick(AdapterView parent, View view,
                                    int position, long id) {
                mViewPager.setCurrentItem(position);
                mDrawerLayout.closeDrawers();
            }
        }
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                toolbar, /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description */
                R.string.drawer_close /* "close drawer" description */
        );
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
//		getActionBar().setDisplayHomeAsUpEnabled(true);
//		getActionBar().setHomeButtonEnabled(true);
    }

    // fixed tab listener implemented
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
    public void onPause() {
        // Disabled while no goals.
		/*
		 * SharedPreferences.Editor editor =
		 * getSharedPreferences(session.getCurrentStudent().studentId + "",
		 * Context.MODE_PRIVATE).edit(); for (int i = 0; i < goals.length; i++)
		 * { editor.putInt(Integer.toString(i), goals[i]); } editor.commit();
		 */
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity, menu);

        // Create list of students in Menu.
        if (session.MULTIPLE_STUDENTS) {
            for (int i = 0; i < session.getStudents().size(); i++) {
                String name = session.getStudents().get(i).name;
                MenuItem item = menu.add(name);

                // Set the currently enabled student un-clickable.
                if (i == session.studentIndex)
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
                session = null;
                // attendanceTask.cancel(true);
                // attendanceTask = null;
                Editor editor = getSharedPreferences(
                        "LoginActivity", Context.MODE_PRIVATE).edit();
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
            // case R.id.refresh:
            // dg.clearData();
            // Intent intentR = new Intent(this, LoginActivity.class);
            // intentR.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // intentR.putExtra("Refresh", true);
            // startActivity(intentR);
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
            // Return a MainActivityFragment (defined as a static inner class
            // below) with the page number as its lone argument.
            return mFragments[position];
        }

        @Override
        public int getCount() {
            return NUM_FRAGMENTS;
            // Semester goals removed until May 2014.
            // return 4;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public CharSequence getPageTitle(int position) {
//			return ((YPMainFragment) getItem(position)).getPageTitle();

            switch (position) {
                case 0:
                    return
                            getResources().getString(R.string.main_section_0_title);
                case 1:
                    return TermFinder.Term.values()[CURRENT_TERM_INDEX].name;
                case 2:
                    return getResources().getString(R.string.main_section_2_title);
                case 3:
                    return
                            getResources().getString(R.string.main_section_3_title);
                default:
                    return "";
            }

        }

    }

    public static abstract class YPMainFragment extends Fragment {
        public abstract String getPageTitle();
    }

    public static class PlaceholderFragment extends YPMainFragment {
        @Override
        public String getPageTitle() {
            return "";
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.main_summary_fragment_holder, container, false);

            mSummaryFragment = new SummaryFragment();
            Bundle args = new Bundle();
            args.putInt(SummaryFragment.ARG_SEMESTER_NUM,
                    currentSummaryFragment);
            mSummaryFragment.setArguments(args);

            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_holder, mSummaryFragment)
                    .addToBackStack(null).commit();

            return rootView;
        }
    }

    public static class SummaryFragment extends YPMainFragment {

        public static final String ARG_SEMESTER_NUM = "semester_number";
        public static final String[][] COLUMN_HEADERS = {
                {"1st", "2nd", "3rd", "Exam", "Avg"},
                {"4th", "5th", "6th", "Exam", "Avg"}};
        public static final String[] PAGE_TITLE = {"Fall Semester",
                "Spring Semester"};
        public static final String[] PAGE_TITLE_SHORT = {"Fall", "Spring"};
        private View rootView;
        private int semesterNum;

        @Override
        public String getPageTitle() {
            return PAGE_TITLE[semesterNum];
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Bundle args = getArguments();
            semesterNum = args.getInt(ARG_SEMESTER_NUM);
//			getActivity().getActionBar().getTabAt(SUMMARY_FRAGMENT_POSITION)
//					.setText(getPageTitle());

            rootView = inflater.inflate(R.layout.tab_summary, container, false);

            LinearLayout bigLayout = (LinearLayout) rootView
                    .findViewById(R.id.container);

            // Add current student's name
            if (session.MULTIPLE_STUDENTS) {
                LinearLayout studentName = (LinearLayout) inflater.inflate(
                        R.layout.main_student_name_if_multiple_students,
                        bigLayout, false);
                ((TextView) studentName.findViewById(R.id.name))
                        .setText(session.getStudents()
                                .get(session.studentIndex).name);
                bigLayout.addView(studentName);
            }
            LinearLayout weekNames = new LinearLayout(getActivity());
            weekNames.setBackgroundResource(R.drawable.card_custom);
            TextView[] weeks = new MyTextView[5];
            weekNames.setPadding(15, 20, 0, 20);
            weekNames.setGravity(Gravity.CENTER);

            for (int i = 0; i < weeks.length; i++) {
                weeks[i] = new MyTextView(getActivity());
                weeks[i].setTextSize(getResources().getDimension(
                        R.dimen.text_size_grade_overview_header));
                weeks[i].setText(COLUMN_HEADERS[semesterNum][i]);

                weeks[i].setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                        (SCREEN_WIDTH - 30) / 5,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                llp.setMargins(0, 0, 0, 0);
                weeks[i].setLayoutParams(llp);
                weeks[i].setGravity(Gravity.CENTER);
                weekNames.addView(weeks[i]);
            }

            bigLayout.addView(weekNames);
            int[] classMatch = session.getCurrentStudent().getClassMatch();

            JSONArray classList = session.getCurrentStudent().getClassList();

            // TODO Will break for people who change courses in middle of
            // semester.
            int realClassIndex = 0;

            for (int classIndex = 0; classIndex < classCount; classIndex++) {

                if (!session.getCurrentStudent().hasClassDuringSemester(
                        classIndex, semesterNum))
                    continue;

                int jsonIndex = classMatch[classIndex];
                View classSummary = inflater.inflate(
                        R.layout.main_grade_summary_linear_layout, bigLayout,
                        false);
                TextView className = (TextView) classSummary
                        .findViewById(R.id.class_name);
                className.setText(session.getStudents()
                        .get(session.studentIndex).getClassName(jsonIndex));

                LinearLayout summary = (LinearLayout) classSummary
                        .findViewById(R.id.layout_six_weeks_summary);
                summary.setPadding(15, 0, 15, 14);

                for (int termIndex = 4 * semesterNum; termIndex < 4 + 4 * semesterNum; termIndex++) {

                    TextView termGrade = new MyTextView(getActivity());
                    termGrade.setTextSize(getResources().getDimension(
                            R.dimen.text_size_grade_overview_score));
                    termGrade.setClickable(true);

                    int width = (SCREEN_WIDTH - 30) / 5;
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                            width,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (termIndex == 0)
                        llp.setMargins(15, 0, 0, 0);
                    termGrade.setLayoutParams(llp);

                    int avg;
                    JSONArray terms = classList.optJSONObject(jsonIndex)
                            .optJSONArray("terms");
                    if (terms.length() <= 4)
                        avg = terms.optJSONObject(termIndex % 4).optInt(
                                "average", -1);
                    else
                        avg = terms.optJSONObject(termIndex).optInt("average",
                                -1);

                    if (avg == Student.CLASS_DISABLED_DURING_TERM) {
                        termGrade.setBackgroundColor(getResources().getColor(
                                R.color.disabledCell));
                        termGrade.setClickable(false);
                    } else {
                        termGrade
                                .setOnClickListener(new ClassSwipeOpenerListener(
                                        session.studentIndex, realClassIndex,
                                        termIndex));
                        termGrade
                                .setBackgroundResource(R.drawable.grade_summary_click);
                        if (avg != -1)
                            termGrade.setText(avg + "");
                    }

                    termGrade.setGravity(Gravity.CENTER);
                    summary.addView(termGrade);

                }

                // Display the average.
                int average;
                if (semesterNum == 0)
                    average = classList.optJSONObject(jsonIndex).optInt(
                            "firstSemesterAverage", -1);
                else
                    average = classList.optJSONObject(jsonIndex).optInt(
                            "secondSemesterAverage", -1);

                if (average != -1) {
                    String averageText = Integer.toString(average);

                    TextView averageGrade = new MyTextView(getActivity());

                    int width = (SCREEN_WIDTH - 30) / 5;
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                            width,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    llp.setMargins(0, 0, 15, 0);
                    averageGrade.setLayoutParams(llp);

                    averageGrade.setTextSize(getResources().getDimension(
                            R.dimen.text_size_grade_overview_score));
                    averageGrade.setClickable(false);
                    averageGrade.setGravity(Gravity.CENTER);
                    averageGrade.setText(averageText);
                    averageGrade.setTextColor(getResources().getColor(
                            RandomStuff.gradeColor(average)));

                    summary.addView(averageGrade);
                }

                bigLayout.addView(classSummary);
                realClassIndex++;

            }
            Button toggleSemester = new Button(getActivity());

            toggleSemester.setBackgroundResource(R.drawable.card_click_blue);
            toggleSemester.setText("View "
                    + PAGE_TITLE[Math.abs(currentSummaryFragment - 1)]);
            toggleSemester.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    // Toggle between spring/fall semester
                    currentSummaryFragment = Math
                            .abs(currentSummaryFragment - 1);
                    SummaryFragment newFragment = new SummaryFragment();
                    Bundle args = new Bundle();
                    args.putInt(SummaryFragment.ARG_SEMESTER_NUM,
                            currentSummaryFragment);
                    newFragment.setArguments(args);

                    getFragmentManager().beginTransaction()
                            // Replace the default fragment animations with animator
                            // resources representing
                            // rotations when switching to the back of the card, as well
                            // as animator
                            // resources representing rotations when flipping back to
                            // the front (e.g. when
                            // the system Back button is pressed).

                            .setCustomAnimations(R.anim.card_flip_right_in,
                                    R.anim.card_flip_right_out,
                                    R.anim.card_flip_left_in,
                                    R.anim.card_flip_left_out)

                                    // Replace any fragments currently in the container
                                    // view with a fragment
                                    // representing the next page (indicated by the
                                    // just-incremented currentPage
                                    // variable).
                            .replace(R.id.fragment_holder, newFragment)

                                    // Add this transaction to the back stack, allowing
                                    // users to press Back
                                    // to get to the front of the card.
                            .addToBackStack(null)

                                    // Commit the transaction.
                            .commit();
                    mSummaryFragment = newFragment;
                }
            });
            bigLayout.addView(toggleSemester);

            TextView summaryLastUpdated = new MyTextView(getActivity());
            String lastUpdatedString = DateHelper.timeSince(session
                    .getCurrentStudent().getClassList().optJSONObject(0)
                    .optLong("summaryLastUpdated"));
            summaryLastUpdated.setText(lastUpdatedString);
            summaryLastUpdated.setPadding(10, 0, 0, 0);
            bigLayout.addView(summaryLastUpdated);

            return rootView;
        }

        class ClassSwipeOpenerListener implements OnClickListener {

            int studentIndex;
            int classIndex;
            int termIndex;

            ClassSwipeOpenerListener(int studentIndex, int classIndex,
                                     int termIndex) {
                this.studentIndex = studentIndex;
                this.classIndex = classIndex;
                this.termIndex = termIndex;
            }

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(),
                        ClassSwipeActivity.class);
                intent.putExtra("studentIndex", this.studentIndex);
                intent.putExtra("classCount", classCount);
                intent.putExtra("classIndex", this.classIndex);
                intent.putExtra("termIndex", this.termIndex);
                startActivity(intent);
            }
        }

    }

    // public static class AttendanceFragment extends YPMainFragment {
    //
    // //private LinearLayout rootView;
    // private FrameLayout placeHolder;
    // private LinearLayout viewByPeriodLayout;
    // private LinearLayout viewByDateLayout;
    // private AttendanceData data;
    //
    // private static final int VIEW_BY_PERIOD = -1324;
    // private static final int VIEW_BY_DATE = -134245;
    //
    // @Override
    // public String getPageTitle() {
    // return "Attendance";
    // }
    //
    // @Override
    // public View onCreateView(LayoutInflater inflater, ViewGroup container,
    // Bundle savedInstanceState) {
    // View rootView = inflater.inflate(R.layout.attendance, container, false);
    //
    // placeHolder =
    // (FrameLayout)rootView.findViewById(R.id.attendance_placeholder);
    //
    // return rootView;
    // }
    //
    // public void setAttendanceData (AttendanceData data) {
    // if (data == null) {
    // System.err.println("Attendance Data is null.");
    // } else {
    // this.data = data;
    // showAttendanceByPeriod();
    // }
    // }
    //
    // public void showAttendanceByPeriod () {
    // // View has already been created.
    // if (data == null) {
    // // wait for data to be received, then it will be set.
    // return;
    // }
    //
    // if (viewByPeriodLayout != null) {
    // setLayoutInPlaceholder(viewByPeriodLayout);
    // return;
    // }
    //
    // LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService
    // (Context.LAYOUT_INFLATER_SERVICE);
    //
    // viewByPeriodLayout = new TableLayout(getActivity());
    //
    // SparseArray<AttendancePeriod> arr = data.getEventsByPeriod();
    //
    // for (int i = 0; i < arr.size(); i++) {
    //
    // TableRow row = (TableRow) inflater.inflate(
    // R.layout.activity_main_attendance_period_row, viewByPeriodLayout);
    //
    // TextView className = (TextView) row.findViewById(R.id.class_name);
    // TextView tardiesView = (TextView) row.findViewById(R.id.num_tardies);
    // TextView absencesView = (TextView) row.findViewById(R.id.num_absences);
    // TextView schoolAbsencesView = (TextView)
    // row.findViewById(R.id.num_school_absences);
    //
    // AttendancePeriod pd = arr.valueAt(i);
    // int[] attendanceTotals = pd.getAttendanceTotals();
    //
    // className.setText(pd.getClassName());
    // tardiesView.setText("" +
    // attendanceTotals[AttendancePeriod.TARDIES_INDEX]);
    // absencesView.setText("" +
    // attendanceTotals[AttendancePeriod.ABSENCES_INDEX]);
    // schoolAbsencesView.setText("" +
    // attendanceTotals[AttendancePeriod.SCHOOL_ABSENCES_INDEX]);
    //
    // viewByPeriodLayout.addView(row);
    // }
    //
    // // Put the view in the placeholder
    // setLayoutInPlaceholder(viewByPeriodLayout);
    // }
    //
    // private void setLayoutInPlaceholder(View view) {
    // // if exactly one view is in placeholder AND the view is the existing
    // view,
    // // does nothing.
    // // Else, removes all views and switches views out.
    // if (placeHolder.getChildCount() != 1 || placeHolder.getChildAt(0) !=
    // view) {
    // placeHolder.removeAllViews();
    // placeHolder.addView(view);
    // }
    // }
    //
    // public void showAttendanceByDate () {
    // if (data == null) {
    // // wait for data to be received, then it will be set.
    // return;
    // }
    //
    // // View has already been created
    // if (viewByDateLayout != null) {
    // setLayoutInPlaceholder(viewByDateLayout);
    // return;
    // }
    //
    // viewByDateLayout = new LinearLayout(getActivity());
    // viewByDateLayout.setOrientation(LinearLayout.VERTICAL);
    //
    // for (Entry<String, List<AttendanceEvent>> entry :
    // data.getEventsByDate().entrySet()) {
    // String date = entry.getKey();
    // TextView dateView = new MyTextView(getActivity());
    // dateView.setText(DateHelper.toHumanDate(date));
    // dateView.setBackgroundResource(R.drawable.card_custom);
    // dateView.setTextSize(30);
    // viewByDateLayout.addView(dateView);
    //
    // TextView eventView = new MyTextView(getActivity());
    // StringBuilder sb = new StringBuilder();
    // for (AttendanceEvent e : entry.getValue()) {
    // if (!e.isAbsence())
    // sb.append("<font color=\"yellow\">"); // tardy
    // else if (e.countsAgainstExemptions())
    // sb.append("<font color=\"red\">"); // bad absence
    // else
    // sb.append("<font color=\"green\">"); // good absence
    // sb.append(e.getPeriod());
    // sb.append("</font>");
    // sb.append(" ");
    // }
    // eventView.setText(Html.fromHtml(sb.toString()));
    // eventView.setBackgroundResource(R.drawable.card_custom);
    // eventView.setTextSize(23);
    // viewByDateLayout.addView(eventView);
    //
    // }
    //
    // // Put the view in the placeholder
    // setLayoutInPlaceholder(viewByDateLayout);
    // }
    //
    //
    // }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class MainActivityFragment extends YPMainFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public static final String ARG_OBJECT = "object";
        private View rootView;
        private int position;
        RelativeLayout[] profileCards;
        private boolean pictureNotLoaded = true;

        @Override
        public String getPageTitle() {
            switch (position) {
                case 0: {
                    return getResources().getString(R.string.main_section_0_title);
                }

                case 1:
                    return TermFinder.Term.values()[CURRENT_TERM_INDEX].name;
                // case 2: return
                // getResources().getString(R.string.main_section_2_title);
                // case 3: return
                // getResources().getString(R.string.main_section_3_title);
                default:
                    return "";
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // [Hopefully] to prevent force closes.
            if (session.getStudents().size() == 0) {
                session = null;
                Editor editor = getActivity()
                        .getSharedPreferences("LoginActivity",
                                Context.MODE_PRIVATE).edit();
                editor.putBoolean("auto_login", false);
                editor.commit();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("show", true);
                startActivity(intent);
            }

            Bundle args = getArguments();
            position = args.getInt(ARG_OBJECT);
            int tabLayout = 0;
            switch (position) {
                case 0:
                    tabLayout = R.layout.tab_new;
                    break;
                case 1:
                case 2:
                case 3:
                    tabLayout = R.layout.tab_summary;
                    break;
            }

            // Semester Goals : Shall remain dormant until May 2014.
			/*
			 * // Load saved goals. goals = new
			 * int[dg.getCurrentStudent().getClassMatch().length];
			 * Arrays.fill(goals, -1); SharedPreferences sharedPrefs =
			 * getActivity
			 * ().getSharedPreferences(dg.getCurrentStudent().studentId + "",
			 * Context.MODE_PRIVATE); for (String key :
			 * sharedPrefs.getAll().keySet()) { goals[Integer.parseInt(key)] =
			 * sharedPrefs.getInt(key, -1); }
			 */

            rootView = inflater.inflate(tabLayout, container, false);

            if (position == 0) {

                LinearLayout bigLayout = (LinearLayout) rootView
                        .findViewById(R.id.overall);

                if (session.MULTIPLE_STUDENTS) {
                    TextView instructions = new MyTextView(getActivity());
                    LinearLayout instruct = new LinearLayout(getActivity());

                    instructions.setPadding(15, 15, 15, 15);
                    instructions.setText(R.string.welcome_multiple_students);
                    instruct.setBackgroundResource(R.drawable.card_custom);
                    instruct.addView(instructions);
                    bigLayout.addView(instruct, 1);
                }

                profileCards = new RelativeLayout[session.getStudents().size()];

                for (int i = 0; i < session.getStudents().size(); i++) {
                    // profileCards[i] = new RelativeLayout(getActivity());
                    //
                    // ImageView profilePic = new ImageView(getActivity());
                    // profilePic.setId(MainActivity.id.profile_picture);
                    // LinearLayout.LayoutParams lpPic = new
                    // LinearLayout.LayoutParams(
                    // android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    // android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                    // lpPic.setMargins(5,5,0,0);
                    // profilePic.setLayoutParams(lpPic);
                    // TextView name = new MyTextView(getActivity());
                    // name.setTypeface(Typeface.createFromAsset(getActivity().getAssets(),"Roboto-Light.ttf"));
                    // // TODO use screen-specific text size.
                    // name.setTextSize(22);
                    // name.setText(session.getStudents().get(i).name);
                    // name.setId(id.name);
                    // name.setLayoutParams(new
                    // LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                    // android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
                    // name.setGravity(Gravity.CENTER);
                    //
                    // RelativeLayout.LayoutParams lpName = new
                    // RelativeLayout.LayoutParams(
                    // android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    // android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                    // lpName.addRule(RelativeLayout.RIGHT_OF,
                    // profilePic.getId());
                    //

                    profileCards[i] = (RelativeLayout) inflater.inflate(
                            R.layout.profile_card, bigLayout, false);
                    ImageView profilePic = (ImageView) profileCards[i]
                            .findViewById(R.id.profilePic);
                    TextView name = (TextView) profileCards[i]
                            .findViewById(R.id.name);
                    name.setText(session.getStudents().get(i).name);
                    int lastId = R.id.name;

                    for (int semesterNum = 0; semesterNum < 2; semesterNum++) {
                        final double gpaValue = session.getStudents().get(i)
                                .getGPA(semesterNum);

                        if (!Double.isNaN(gpaValue)) {
                            TextView gpa;
                            if (semesterNum == 0) {
                                gpa = (TextView) profileCards[i]
                                        .findViewById(R.id.gpaFall);

                            } else
                                gpa = (TextView) profileCards[i]
                                        .findViewById(R.id.gpaSpring);
                            gpa.setText(String
                                    .format("%s GPA: %.4f",
                                            SummaryFragment.PAGE_TITLE_SHORT[semesterNum],
                                            gpaValue));

                            lastId = gpa.getId();
                        }
                    }

                    // profileCards[i].addView(profilePic);
                    // profileCards[i].addView(name, lpName);
                    // profileCards[i].setOnClickListener(new
                    // StudentChooserListener(i));
                    // profileCards[i].setBackgroundResource(R.drawable.card_custom);

                    bigLayout.addView(profileCards[i]);

                    if (pictureNotLoaded) {
                        StudentPictureTask spTask = new StudentPictureTask();
                        spTask.execute(i);
                    } else {
                        Drawable picture = new BitmapDrawable(getResources(),
                                pics[i]);
                        profilePic.setImageDrawable(picture);
                    }

                    RelativeLayout gpaCalc = (RelativeLayout) inflater.inflate(
                            R.layout.main_gpa_calc, bigLayout, false);
                    final TextView actualGPA = (TextView) gpaCalc
                            .findViewById(R.id.actualGPA);

                    Button calculate = (Button) gpaCalc
                            .findViewById(R.id.calculate);

                    final EditText oldCumulativeGPA = (EditText) gpaCalc
                            .findViewById(R.id.cumulativeGPA);

                    final EditText numCredits = (EditText) gpaCalc
                            .findViewById(R.id.numCredits);

                    SharedPreferences sharedPrefs = getActivity()
                            .getPreferences(Context.MODE_PRIVATE);
                    float spGPA = sharedPrefs
                            .getFloat("oldCumulativeGPA"
                                            + session.getStudents().get(i).studentId,
                                    Float.NaN);

                    if (!Float.isNaN(spGPA)) {
                        oldCumulativeGPA.setText(Float.toString(spGPA));
                        float spCredits = sharedPrefs.getFloat("numCredits"
                                        + session.getStudents().get(i).studentId,
                                Float.NaN);
                        if (!Float.isNaN(spCredits)) {
                            numCredits.setText(Float.toString(spCredits));
                            double cumGPA = session.getStudents().get(i)
                                    .getCumulativeGPA(spGPA, spCredits);
                            actualGPA.setText(String.format("%.4f", cumGPA));
                        }
                    }
                    final int studentIndex = i;
                    calculate.setOnClickListener(new OnClickListener() {

                        private final int mStudentIndex = studentIndex;

                        @Override
                        public void onClick(View bigLayout) {
                            float oldGPA;
                            float cred;
                            try {
                                oldGPA = Float.parseFloat(oldCumulativeGPA
                                        .getText().toString());
                            } catch (NumberFormatException e) {
                                oldGPA = 0;
                            }
                            try {
                                cred = Float.parseFloat(numCredits.getText()
                                        .toString());
                            } catch (NumberFormatException e) {
                                cred = 0;
                            }

                            if ((oldGPA + "").equals("0.0")
                                    || (cred + "").equals("0.0"))
                                actualGPA.setText("Please Fill Out the Above");
                            else {
                                SharedPreferences sharedPrefs = getActivity()
                                        .getPreferences(Context.MODE_PRIVATE);
                                Editor editor = sharedPrefs.edit();
                                editor.putFloat(
                                        "oldCumulativeGPA"
                                                + session.getStudents().get(
                                                mStudentIndex).studentId,
                                        oldGPA);
                                editor.putFloat(
                                        "numCredits"
                                                + session.getStudents().get(
                                                mStudentIndex).studentId,
                                        cred);
                                editor.commit();
                                double legitGPA = session.getStudents()
                                        .get(studentIndex)
                                        .getCumulativeGPA(oldGPA, cred);
                                actualGPA.setText(String.format("%.4f",
                                        legitGPA));
                            }

                            // Hide the keyboard
                            InputMethodManager imm = (InputMethodManager) getActivity()
                                    .getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    numCredits.getWindowToken(), 0);
                        }
                    });
                    ImageButton help = (ImageButton) gpaCalc
                            .findViewById(R.id.help);
                    help.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(
                                    getActivity());
                            builder.setTitle("Help");

                            try {
                                builder.setMessage(R.string.gpa_help);
                            } catch (Exception e) {
                                return;
                            }

                            builder.setCancelable(false).setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                            AlertDialog diag = builder.create();
                            diag.show();
                        }
                    });
                    bigLayout.addView(gpaCalc);
                }

                if (session.MULTIPLE_STUDENTS)
                    colorStudents();

            }

            if (position == 1) {

                LinearLayout bigLayout = (LinearLayout) rootView
                        .findViewById(R.id.container);

                // Add current student's name
                if (session.MULTIPLE_STUDENTS) {
                    LinearLayout studentName = (LinearLayout) inflater.inflate(
                            R.layout.main_student_name_if_multiple_students,
                            bigLayout, false);
                    ((TextView) studentName.findViewById(R.id.name))
                            .setText(session.getStudents().get(
                                    session.studentIndex).name);
                    bigLayout.addView(studentName);
                }

                int[] classMatch = session.getCurrentStudent().getClassMatch();

                classCount = classMatch.length;
                goals = new int[classCount];
                Arrays.fill(goals, -1);

                averages = new RelativeLayout[classCount];

                JSONArray classList = session.getCurrentStudent()
                        .getClassList();

                int realClassIndex = 0;

                for (int classIndex = 0; classIndex < classCount; classIndex++) {

                    // Skip classes that don't exist in Semester 1 [Spring
                    // Semester]
                    if (!session.getCurrentStudent().hasClassDuringSemester(
                            classIndex, 1))
                        continue;

                    int jsonIndex = classMatch[classIndex];

                    averages[classIndex] = (RelativeLayout) inflater.inflate(
                            R.layout.main_grade_summary, bigLayout, false);
                    TextView className = (TextView) averages[classIndex]
                            .findViewById(R.id.name);
                    String name = session.getStudents()
                            .get(session.studentIndex).getClassName(jsonIndex);
                    className.setText(name);

                    // For 2nd-semester-only classes whose 4th six weeks is
                    // located as position 0
                    int avg;
                    if (classList.optJSONObject(jsonIndex)
                            .optJSONArray("terms").length() <= 4)
                        avg = classList.optJSONObject(jsonIndex)
                                .optJSONArray("terms")
                                .optJSONObject(CURRENT_TERM_INDEX % 4)
                                .optInt("average", -1);
                    else
                        avg = classList.optJSONObject(jsonIndex)
                                .optJSONArray("terms")
                                .optJSONObject(CURRENT_TERM_INDEX)
                                .optInt("average", -1);

                    // Only set the grade text if the average is not empty
                    if (avg != Student.NO_GRADES_ENTERED) {
                        String average = avg + "";
                        TextView grade = (TextView) averages[classIndex]
                                .findViewById(R.id.grade);
                        grade.setText(average);
                    }

                    averages[classIndex]
                            .setOnClickListener(new ClassSwipeOpenerListener(
                                    session.studentIndex, realClassIndex++,
                                    CURRENT_TERM_INDEX));

                    bigLayout.addView(averages[classIndex]);
                }

                TextView summaryLastUpdated = new MyTextView(getActivity());
                String lastUpdatedString = DateHelper.timeSince(session
                        .getCurrentStudent().getClassList().optJSONObject(0)
                        .optLong("summaryLastUpdated"));
                summaryLastUpdated.setText(lastUpdatedString);
                summaryLastUpdated.setPadding(10, 0, 0, 0);
                bigLayout.addView(summaryLastUpdated);

                return rootView;
            }

            if (position == 2) {
                throw new RuntimeException(
                        "This position should instantiated as SummaryFragment,"
                                + " not MainActivityFragment.");
            }

            // if (position == 3) {
            // throw new
            // RuntimeException("This position should be instantiated as AttendanceFragment,"
            // +
            // "not MainActivityFragment.");
            // }

            // Semester Goals : shall remain dormant until May 2014.
			/*
			 * if (position == 3) {
			 * 
			 * LinearLayout layout = (LinearLayout)
			 * rootView.findViewById(R.id.container);
			 * 
			 * RelativeLayout helpLabel = new RelativeLayout(getActivity());
			 * helpLabel.setBackgroundResource(R.drawable.card_custom); TextView
			 * help = new MyTextView(getActivity());
			 * help.setText("Exam Grade Needed");
			 * help.setTextSize(getResources()
			 * .getDimension(R.dimen.text_size_grade_overview_header));
			 * help.setTypeface(robotoNew); help.setGravity(Gravity.RIGHT);
			 * RelativeLayout.LayoutParams labelParams = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * RelativeLayout.LayoutParams labelParams1 = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * labelParams1.setMargins(5, 5, 5, 5);
			 * helpLabel.setLayoutParams(labelParams1);
			 * labelParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			 * help.setPadding(0, 20, 20, 10);
			 * help.setLayoutParams(labelParams); helpLabel.addView(help);
			 * 
			 * TextView target = new MyTextView(getActivity());
			 * target.setText("Goal");
			 * target.setTextSize(getResources().getDimension
			 * (R.dimen.text_size_grade_overview_header));
			 * target.setTypeface(robotoNew); target.setGravity(Gravity.LEFT);
			 * RelativeLayout.LayoutParams targetParams = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * targetParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			 * target.setLayoutParams(targetParams); target.setPadding(20, 20,
			 * 0, 10); helpLabel.addView(target);
			 * 
			 * layout.addView(helpLabel);
			 * 
			 * for (int classIndex = 0; classIndex < classCount; classIndex++) {
			 * 
			 * int jsonIndex =
			 * dg.getCurrentStudent().getClassMatch()[classIndex];
			 * 
			 * RelativeLayout group = new RelativeLayout(getActivity());
			 * group.setBackgroundResource(R.drawable.card_custom);
			 * 
			 * TextView className = new MyTextView(getActivity());
			 * className.setText
			 * (dg.getCurrentStudent().getShortClassName(jsonIndex));
			 * className.setTypeface(robotoNew);
			 * className.setTextSize(getResources
			 * ().getDimension(R.dimen.text_size_grade_overview_header)-5);
			 * className.setPadding(20,10,0,0); final TierView goal = new
			 * TierView(getActivity(), robotoNew);
			 * 
			 * View minus = new View(getActivity()); View plus = new
			 * View(getActivity());
			 * 
			 * minus.setBackgroundResource(R.drawable.navigation_previous_item);
			 * plus.setBackgroundResource(R.drawable.navigation_next_item);
			 * 
			 * final TextView examScore = new MyTextView(getActivity());
			 * examScore.setPadding(0,0,20,0);
			 * examScore.setTextSize(getResources
			 * ().getDimension(R.dimen.text_size_grade_overview_score));
			 * examScore.setTypeface(robotoNew);
			 * examScore.setGravity(Gravity.RIGHT);
			 * 
			 * // Try to retrieve data from sharedPrefs, otherwise calculate
			 * from scratch. if (goals[classIndex] != -1) {
			 * goal.setText(goals[classIndex]); } else { while(goal.getIndex()
			 * != 0 && dg.getCurrentStudent().examScoreRequired(classIndex,
			 * TierView.RANGES[goal.getIndex()])>100) goal.decrement();
			 * goals[classIndex] = goal.getIndex(); }
			 * 
			 * examScore.setText("" +
			 * dg.getCurrentStudent().examScoreRequired(classIndex,
			 * TierView.RANGES[goal.getIndex()]));
			 * 
			 * if (Integer.parseInt(examScore.getText().toString()) > 100)
			 * examScore.setTextColor(getResources().getColor(R.color.red));
			 * 
			 * class PlusMinusOnClickListener implements OnClickListener { int
			 * classIndex; int delta; PlusMinusOnClickListener(int classIndex,
			 * int delta) { this.classIndex = classIndex; this.delta = delta; }
			 * 
			 * @Override public void onClick(View v) { if (delta==1)
			 * goal.increment(); else if (delta==-1) goal.decrement();
			 * 
			 * goals[classIndex] = goal.getIndex();
			 * 
			 * examScore.setText( "" + dg.getCurrentStudent().examScoreRequired(
			 * classIndex, TierView.RANGES[goal.getIndex()])); if
			 * (Integer.parseInt(examScore.getText().toString()) > 100)
			 * examScore.setTextColor(getResources().getColor(R.color.red));
			 * else
			 * examScore.setTextColor(getResources().getColor(R.color.black)); }
			 * }
			 * 
			 * minus.setOnClickListener(new PlusMinusOnClickListener(classIndex,
			 * -1)); plus.setOnClickListener(new
			 * PlusMinusOnClickListener(classIndex, 1));
			 * 
			 * // Don't show classes that don't have grades in the 3rd six weeks
			 * // (ex. James Hannah & senior release, etc.) if
			 * (examScore.getText().equals("-1")) continue;
			 * 
			 * // Assign position to class name RelativeLayout.LayoutParams
			 * layoutParams = new RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			 * layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			 * className.setLayoutParams(layoutParams); className.setId(234254);
			 * 
			 * // Put minus under class name layoutParams = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * layoutParams.addRule(RelativeLayout.BELOW, className.getId());
			 * layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			 * minus.setLayoutParams(layoutParams); minus.setId(2345235);
			 * 
			 * // Put goal to right of minus layoutParams = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * layoutParams.addRule(RelativeLayout.RIGHT_OF, minus.getId());
			 * layoutParams.addRule(RelativeLayout.ALIGN_TOP, minus.getId());
			 * layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, minus.getId());
			 * layoutParams.setMargins(0, 0, 0, 0); layoutParams.width =
			 * getResources().getInteger(R.integer.width_exam_score);
			 * goal.setLayoutParams(layoutParams); goal.setId(123491);
			 * 
			 * // Put plus to right of goal layoutParams = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * layoutParams.addRule(RelativeLayout.RIGHT_OF, goal.getId());
			 * layoutParams.addRule(RelativeLayout.BELOW, className.getId());
			 * plus.setLayoutParams(layoutParams);
			 * 
			 * // Assign size of plus and minus minus.getLayoutParams().width =
			 * (int)
			 * getResources().getDimension(R.dimen.semester_goals_arrow_size);
			 * minus.getLayoutParams().height = (int)
			 * getResources().getDimension(R.dimen.semester_goals_arrow_size);
			 * plus.getLayoutParams().width = (int)
			 * getResources().getDimension(R.dimen.semester_goals_arrow_size);
			 * plus.getLayoutParams().height = (int)
			 * getResources().getDimension(R.dimen.semester_goals_arrow_size);
			 * // minus.setPadding(0, 0, 0, 30); layoutParams = new
			 * RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, goal.getId());
			 * layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			 * examScore.setLayoutParams(layoutParams);
			 * 
			 * group.addView(className); group.addView(examScore);
			 * group.addView(minus); group.addView(goal); group.addView(plus);
			 * layoutParams = new RelativeLayout.LayoutParams(
			 * RelativeLayout.LayoutParams.WRAP_CONTENT,
			 * RelativeLayout.LayoutParams.WRAP_CONTENT);
			 * layoutParams.setMargins(5,5,5,5);
			 * group.setLayoutParams(layoutParams);
			 * 
			 * layout.addView(group);
			 * 
			 * } }
			 */
            return rootView;
        }

        static Bitmap[] pics = new Bitmap[session.getStudents().size()];

        public class StudentPictureTask extends
                AsyncTask<Integer, Void, Bitmap> {

            int taskStudentIndex;

            @Override
            protected Bitmap doInBackground(Integer... args) {
                taskStudentIndex = args[0];
                pics[taskStudentIndex] = session.getStudents()
                        .get(taskStudentIndex).getStudentPicture();
                return session.getStudents().get(taskStudentIndex)
                        .getStudentPicture();
            }

            @Override
            protected void onPostExecute(final Bitmap result) {
                ImageView profilePic = (ImageView) profileCards[taskStudentIndex]
                        .findViewById(R.id.profilePic);
                profilePic.setImageBitmap(result);

                pictureNotLoaded = false;
            }
        }

        class StudentChooserListener implements OnClickListener {
            int studentIndex;

            StudentChooserListener(int studentIndex) {
                this.studentIndex = studentIndex;
            }

            @Override
            public void onClick(View v) {
                session.studentIndex = this.studentIndex;
                colorStudents();

                ((MainActivity) getActivity()).refresh();
            }

        }

        public void colorStudents() {
            for (int i = 0; i < profileCards.length; i++) {
                // Display the chosen student in a different color.
                if (i == session.studentIndex)
                    profileCards[i]
                            .setBackgroundResource(R.drawable.card_click_blue);
                else
                    profileCards[i]
                            .setBackgroundResource(R.drawable.card_click_blue);
            }
        }

        class ClassSwipeOpenerListener implements OnClickListener {

            int studentIndex;
            int classIndex;
            int termIndex;

            ClassSwipeOpenerListener(int studentIndex, int classIndex,
                                     int termIndex) {
                this.studentIndex = studentIndex;
                this.classIndex = classIndex;
                this.termIndex = termIndex;
            }

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getActivity(),
                        ClassSwipeActivity.class);
                intent.putExtra("studentIndex", this.studentIndex);
                intent.putExtra("classCount", classCount);
                intent.putExtra("classIndex", this.classIndex);
                intent.putExtra("termIndex", this.termIndex);
                startActivity(intent);
            }
        }
    }

    public void refresh() {
        this.mSectionsPagerAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    public int screenHeight() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public int screenWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    class StudentChooserListener implements OnMenuItemClickListener {

        int studentIndex;

        StudentChooserListener(int studentIndex) {
            this.studentIndex = studentIndex;
        }

        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            session.studentIndex = this.studentIndex;

            refresh();
            return true;
        }
    }

    // public void onRadioButtonClicked (View view) {
    // // Is the button now checked?
    // boolean checked = ((RadioButton) view).isChecked();
    //
    // // Check which radio button was clicked
    // switch(view.getId()) {
    // case R.id.attendance_by_period:
    // if (checked) {
    // ((AttendanceFragment)mFragments[ATTENDANCE_FRAGMENT_POSITION]).showAttendanceByPeriod();
    // }
    // break;
    // case R.id.attendance_by_date:
    // if (checked) {
    // ((AttendanceFragment)mFragments[ATTENDANCE_FRAGMENT_POSITION]).showAttendanceByDate();
    // }
    // break;
    // }
    // }
    //
    // class AttendanceTask extends AsyncTask<Integer, Integer, AttendanceData>
    // {
    //
    // int viewType;
    //
    // @Override
    // protected AttendanceData doInBackground(Integer... args) {
    // if (args.length > 0)
    // viewType = args[0];
    // try {
    // return session.getCurrentStudent().loadAttendanceSummary();
    // } catch (IOException e) {
    // e.printStackTrace();
    // return null;
    // } catch (JSONException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }
    //
    // @Override
    // protected void onPostExecute(final AttendanceData result) {
    // ((AttendanceFragment)mFragments[ATTENDANCE_FRAGMENT_POSITION]).setAttendanceData(result);
    // }
    //
    // }

}