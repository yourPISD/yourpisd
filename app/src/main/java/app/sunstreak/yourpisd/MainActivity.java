package app.sunstreak.yourpisd;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.List;

import app.sunstreak.yourpisd.googleutil.SlidingTabLayout;
import app.sunstreak.yourpisd.net.Session;
import app.sunstreak.yourpisd.net.data.ClassReport;
import app.sunstreak.yourpisd.util.DateHelper;
import app.sunstreak.yourpisd.util.RandomStuff;
import app.sunstreak.yourpisd.view.MyTextView;

/**
 * MainActivity displays the summary fragments which shows the user their average grade in each
 * class, profile card with gpa, and overall semester averages
 */
public class MainActivity extends ActionBarActivity {

    static RelativeLayout[] layoutAverages;
    static int[] goals;
    static Session session;
    static int SCREEN_HEIGHT;
    static int SCREEN_WIDTH;

    static SummaryFragment mSummaryFragment;
    static YPMainFragment[] mFragments;
    public static final int NUM_FRAGMENTS = 3;
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
    public String[] mList = {"Profile", "Current Nine Weeks", "Grade Overview"/*
                                                                             * ,
																			 * "Semester Goals"
																			 */};
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    public SlidingTabLayout slidingTabLayout;
    public Toolbar toolbar;

    @Override
    protected void onRestart() {
        super.onRestart();

        //Login if session has been cleared.
        if (((YPApplication) getApplication()).session == null)
            login(false);
    }

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
        if (session == null)
        {
            login(false);
            return;
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the app.
        FragmentManager fm = getSupportFragmentManager();
        mSectionsPagerAdapter = new SectionsPagerAdapter(fm);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);

        setUpNavigationDrawer();
        AppRater.app_launched(this);
        mFragments = new YPMainFragment[NUM_FRAGMENTS];

        //Using current term, determine which semester to open first.
        currentSummaryFragment = (TermFinder.getCurrentTermIndex() < ClassReport.SEMESTER_TERMS) ? 0 : 1;

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
        // Otherwise, show the current nine weeks grades list.
        else
            mViewPager.setCurrentItem(1);

        isAttendanceLoaded = false;

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
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.white));
        slidingTabLayout.setViewPager(mViewPager);
    }

    private void setUpNavigationDrawer() {
        // navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
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
        if (session != null && session.MULTIPLE_STUDENTS) {
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
    protected void onStop() {
        super.onStop();

        //Only log out if we are not exiting internally.
        YPApplication app = (YPApplication)getApplication();
        if (app.startingInternal)
            app.startingInternal = false;
        else
            logout();
    }

    private void logout()
    {
        if (session != null)
        {
            UserLogoutTask logout = new UserLogoutTask();
            logout.execute(session);
            ((YPApplication) getApplication()).session = session = null;
        }
    }

    private void login(boolean userIntervention)
    {
        logout();

        if (userIntervention)
        {
            Editor editor = getSharedPreferences(
                    "LoginActivity", Context.MODE_PRIVATE).edit();
            editor.putBoolean("auto_login", false);
            editor.commit();
        }
        // attendanceTask.cancel(true);
        // attendanceTask = null;

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                login(true);
                return true;
            case R.id.credits:
                ((YPApplication)getApplication()).startingInternal = true;
                Intent intentCred1 = new Intent(this, CreditActivity.class);
                ((YPApplication)getApplication()).startingInternal = true;
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

    public static class UserLogoutTask extends AsyncTask<Session, Void, Void> {

        @Override
        protected Void doInBackground(Session... sessions) {
            if (sessions[0] != null)
                sessions[0].logout();
            return null;
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
                    return TermFinder.Term.values()[TermFinder
                            .getCurrentTermIndex()].name;
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
                {"1st", "2nd", "Exam", "Avg"},
                {"3rd", "4th", "Exam", "Avg"}};
        public static final String[] PAGE_TITLE = {"Fall Semester",
                "Spring Semester"};
        public static final String[] PAGE_TITLE_SHORT = {"Fall", "Spring"};
        private View rootView;
        private int semesterNum;
        private Semester semester;

        @Override
        public String getPageTitle() {
            return PAGE_TITLE[semesterNum];
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            Bundle args = getArguments();
            semesterNum = args.getInt(ARG_SEMESTER_NUM);
            semester = semesterNum == 0 ? Semester.FALL : Semester.SPRING;

            rootView = inflater.inflate(R.layout.tab_summary, container, false);

            LinearLayout bigLayout = (LinearLayout) rootView
                    .findViewById(R.id.container);

            // Add current student's name
            if (session == null)
                return rootView;
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
            TextView[] terms = new MyTextView[COLUMN_HEADERS[semesterNum].length];
            weekNames.setGravity(Gravity.CENTER);

            for (int i = 0; i < terms.length; i++) {
                terms[i] = new MyTextView(getActivity());
                terms[i].setTextSize(getResources().getDimension(
                        R.dimen.text_size_grade_overview_header));
                terms[i].setText(COLUMN_HEADERS[semesterNum][i]);

                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                terms[i].setLayoutParams(llp);
                terms[i].setTextSize(18);
                terms[i].setGravity(Gravity.CENTER);
                weekNames.addView(terms[i]);
            }

            bigLayout.addView(weekNames);

            //TODO: reload semester grades.
            final int termOff = ClassReport.SEMESTER_TERMS * semesterNum;
            List<ClassReport> classList = session.getCurrentStudent().getSemesterClasses(semester);
            int i = 0;
            for (ClassReport report : classList) {
                View classSummary = inflater.inflate(R.layout.main_grade_summary_linear_layout,
                        bigLayout, false);
                TextView className = (TextView) classSummary.findViewById(R.id.class_name);
                LinearLayout summary = (LinearLayout) classSummary.findViewById(R.id.layout_six_weeks_summary);

                className.setText(report.getCourseName());


                for (int term = 0; term < ClassReport.SEMESTER_TERMS; term++) {
                    TextView termGrade = new MyTextView(getActivity());
                    termGrade.setTextSize(getResources().getDimension(
                            R.dimen.text_size_grade_overview_score));
                    termGrade.setClickable(true);
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            1f);
                    termGrade.setTextSize(29);
                    termGrade.setLayoutParams(llp);

                    //TODO: do the following procedure for canceled classes, and no more.
//                    termGrade.setBackgroundColor(getResources().getColor(
//                            R.color.disabledCell));
//                    termGrade.setClickable(false);
                    int avg = -1;
                    if (!report.isClassDisabledAtTerm(term + termOff))
                        avg = report.getTerm(term + termOff).getGrade();

                    final int classIndex = i;
                    final int termIndex = term;
                    termGrade.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getActivity(),
                                    ClassSwipeActivity.class);
                            intent.putExtra("studentIndex", session.studentIndex);
                            intent.putExtra("classIndex", classIndex);
                            intent.putExtra("termNum", termIndex);
                            ((YPApplication)getActivity().getApplication()).startingInternal = true;
                            startActivity(intent);
                        }
                    });
                    termGrade.setText(avg == -1 ? "" : avg + "");
                    termGrade.setGravity(Gravity.CENTER);
                    summary.addView(termGrade);
                }

                // Display the sememster average.
                int average = report.calculateAverage(semester);
                if (average != -1) {
                    String averageText = Integer.toString(average);

                    TextView averageGrade = new MyTextView(getActivity());
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    averageGrade.setLayoutParams(llp);

                    averageGrade.setTextSize(29);
                    averageGrade.setClickable(false);
                    averageGrade.setGravity(Gravity.CENTER);
                    averageGrade.setText(averageText);
                    averageGrade.setTextColor(getResources().getColor(
                            RandomStuff.gradeColor(average)));

                    summary.addView(averageGrade);
                }

                summary.setPadding(5, 0, 5, 0);
                bigLayout.addView(classSummary);
                i++;
            }

            View empty = new View(getActivity());
            empty.setLayoutParams(new LinearLayout.LayoutParams(0, 20, 1f));
            bigLayout.addView(empty);
            bigLayout.setWeightSum(1);

            Button toggleSemester = new Button(getActivity());
//            toggleSemester.setBackgroundResource(R.drawable.card_click_blue);
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

//                            .setCustomAnimations(R.anim.card_flip_right_in,
//                                    R.anim.card_flip_right_out,
//                                    R.anim.card_flip_left_in,
//                                    R.anim.card_flip_left_out)
                            .setCustomAnimations(R.anim.slide_in_up,
                                    R.anim.slide_out_down)

                            // Replace any fragments currently in the container
                            // view with a fragment
                            // representing the next page (indicated by the
                            // just-incremented currentPage
                            // variable).
                            .replace(R.id.fragment_holder, newFragment)

                            // Add this transaction to the back stack, allowing
                            // users to press Back
                            // to get to the front of the card.
//                            .addToBackStack(null)

                            // Commit the transaction.
                            .commit();
                    mSummaryFragment = newFragment;
                }
            });
            bigLayout.addView(toggleSemester);

            TextView summaryLastUpdated = new MyTextView(getActivity());
            String lastUpdatedString = DateHelper.timeSince(session
                    .getCurrentStudent().getLastUpdated());
            summaryLastUpdated.setText(lastUpdatedString);
            summaryLastUpdated.setPadding(10, 0, 0, 0);
            bigLayout.addView(summaryLastUpdated);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, 0);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
            rootView.setLayoutParams(params);
            return rootView;
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
     * A fragment that displays the profile page or the current term grades
     */
    public static class MainActivityFragment extends YPMainFragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        public static final String ARG_OBJECT = "object";
        private View rootView;
        private int position;
        private int termNum = TermFinder
                .getCurrentTermIndex();
        LinearLayout[] profileCards;

        @Override
        public String getPageTitle() {
            switch (position) {
                case 0: {
                    return getResources().getString(R.string.main_section_0_title);
                }

                case 1:
                    return TermFinder.Term.values()[TermFinder
                            .getCurrentTermIndex()].name;
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
            if (session != null && session.getStudents().size() == 0) {
                session = null;
                Editor editor = getActivity()
                        .getSharedPreferences("LoginActivity",
                                Context.MODE_PRIVATE).edit();
                editor.putBoolean("auto_login", false);
                editor.commit();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                ((YPApplication)getActivity().getApplication()).startingInternal = true;
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

            if (session == null)
                return rootView;

            switch (position) {
                case 0:
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

                    profileCards = new LinearLayout[session.getStudents().size()];

                    for (int i = 0; i < session.getStudents().size(); i++) {


                        profileCards[i] = (LinearLayout) inflater.inflate(
                                R.layout.profile_card, bigLayout, false);
                        TextView name = (TextView) profileCards[i]
                                .findViewById(R.id.name);
                        name.setText(session.getStudents().get(i).name);
                        for (Semester sem : Semester.values()) {
                            final double gpaValue = session.getStudents().get(i)
                                    .getGPA(sem);

                            TextView txtGPA = (TextView) profileCards[i].findViewById(
                                    sem == Semester.FALL ? R.id.gpaFall : R.id.gpaSpring);
                            if (!Double.isNaN(gpaValue))
                                txtGPA.setText(String.format("%s GPA: %.4f",
                                                SummaryFragment.PAGE_TITLE_SHORT[sem.ordinal()],
                                                gpaValue));
                            else
                                txtGPA.setText(String.format("%s GPA: ---",
                                        SummaryFragment.PAGE_TITLE_SHORT[sem.ordinal()]));
                        }

                        // profileCards[i].addView(profilePic);
                        // profileCards[i].addView(name, lpName);
                        // profileCards[i].setOnClickListener(new
                        // StudentChooserListener(i));
                        // profileCards[i].setBackgroundResource(R.drawable.card_custom);

                        bigLayout.addView(profileCards[i]);

                        StudentPictureTask spTask = new StudentPictureTask();
                        spTask.execute(i);

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

                    return rootView;
                case 1:
                    bigLayout = (LinearLayout) rootView
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

                    List<ClassReport> classList = session.getCurrentStudent().getClassesForTerm(termNum);
                    int classCount = classList.size();
                    goals = new int[classCount];
                    Arrays.fill(goals, -1);
                    layoutAverages = new RelativeLayout[classCount];

                    for (int i = 0; i < classCount; i++) {
                        ClassReport report = classList.get(i);

                        // Skip classes that don't exist in current term.
                        // TODO: check if above comment is correct
                        if (report.isClassDisabledAtTerm(termNum))
                            continue;

                        layoutAverages[i] = (RelativeLayout) inflater.inflate(
                                R.layout.main_grade_summary, bigLayout, false);
                        TextView className = (TextView) layoutAverages[i].findViewById(R.id.name);
                        className.setText(report.getCourseName());

                        int avg = report.getTerm(termNum).getGrade();
                        // Only set the grade text if the average is not empty
                        if (avg >= 0) {
                            String average = avg + "";
                            TextView grade = (TextView) layoutAverages[i]
                                    .findViewById(R.id.grade);
                            grade.setText(average);
                        }

                        final int ind = i;
                        layoutAverages[i].setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(getActivity(),
                                        ClassSwipeActivity.class);
                                intent.putExtra("studentIndex", session.studentIndex);
                                intent.putExtra("classIndex", ind);
                                intent.putExtra("termNum", TermFinder
                                        .getCurrentTermIndex());
                                ((YPApplication)getActivity().getApplication()).startingInternal = true;
                                startActivity(intent);
                            }
                        });

                        bigLayout.addView(layoutAverages[i]);
                    }

                    //TODO: Extract or use current date.
                    TextView summaryLastUpdated = new MyTextView(getActivity());
                    summaryLastUpdated.setText(DateHelper.timeSince(new DateTime()));
                    summaryLastUpdated.setPadding(10, 0, 0, 0);
                    bigLayout.addView(summaryLastUpdated);

                    return rootView;
                case 2:
                    throw new RuntimeException(
                        "This position should instantiated as SummaryFragment,"
                                + " not MainActivityFragment.");
                default:
                    throw new InternalError("Illegal index");
            }
        }

        public class StudentPictureTask extends
                AsyncTask<Integer, Void, Bitmap> {

            int taskStudentIndex;

            @Override
            protected Bitmap doInBackground(Integer... args) {
                if (session == null)
                    return null;
                taskStudentIndex = args[0];
                Bitmap profile = session.getStudents()
                        .get(taskStudentIndex).getStudentPicture();

                DisplayMetrics metrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

                double scale = 70.0 * metrics.density / profile.getWidth();
                profile = Bitmap.createScaledBitmap(profile, (int)(scale * profile.getWidth()),
                        (int)(scale * profile.getHeight()), false);
                return profile;
            }

            @Override
            protected void onPostExecute(final Bitmap result) {
                ImageView profilePic = (ImageView) profileCards[taskStudentIndex]
                        .findViewById(R.id.profilePic);
                profilePic.setImageBitmap(result);
            }
        }

        public void colorStudents() {
            if (session == null)
                return;
            for (int i = 0; i < profileCards.length; i++) {
                // Display the chosen student in a different color.
                if (i == session.studentIndex)
                    profileCards[i]
                            .setBackgroundResource(R.drawable.card_click_blue);
                else
                    profileCards[i]
                            .setBackgroundResource(R.drawable.card_blue); //TODO: is this correct?
            }
        }
    }

    public void refresh() {
        this.mSectionsPagerAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    class StudentChooserListener implements OnMenuItemClickListener {

        int studentIndex;

        StudentChooserListener(int studentIndex) {
            this.studentIndex = studentIndex;
        }

        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            if (session != null)
                session.studentIndex = this.studentIndex;
            refresh();
            return true;
        }
    }


}