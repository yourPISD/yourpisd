package app.sunstreak.yourpisd;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import app.sunstreak.yourpisd.googleutil.SlidingTabLayout;
import app.sunstreak.yourpisd.net.Session;
import app.sunstreak.yourpisd.net.data.*;
import app.sunstreak.yourpisd.util.DateHelper;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("ValidFragment")
public class ClassSwipeActivity extends ActionBarActivity {
    static List<Fragment> mFragments;
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
    static ViewPager mViewPager;

    static int studentIndex;
    static int classCount;
    static int classesMade = 0;
    static int termNum;
    static boolean doneMakingClasses;
    static Session session;

    static Student student;
    static List<ClassReport> classesForTerm;
    public SlidingTabLayout slidingTabLayout;
    static Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_class_swipe);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        spinner.setId(R.id.action_bar_spinner);
        spinner.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        toolbar.addView(spinner);

        classCount = getIntent().getExtras().getInt("classCount");
        termNum = getIntent().getExtras().getInt("termNum");
        studentIndex = getIntent().getExtras().getInt("studentIndex");

        int startIndex = getIntent().getExtras().getInt("classIndex");

        setTitle(TermFinder.Term.values()[termNum].name);

        session = ((YPApplication) getApplication()).session;
        session.studentIndex = studentIndex;
        student = session.getCurrentStudent();
        classesForTerm = student.getClassesForTerm(termNum);

        mFragments = new ArrayList<>();
        for (ClassReport report : classesForTerm) {
            Bundle args = new Bundle();

            args.putInt(DescriptionFragment.ARG_CLASS_ID, report.getClassID());
            Fragment fragment = new DescriptionFragment();
            fragment.setArguments(args);
            mFragments.add(fragment);
        }

        if (startIndex >= mFragments.size() || startIndex < 0)
            startIndex = 0;

        // Create the adapter that will return a fragment for each of the
        // primary sections of the app.

        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager(), mFragments);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        ArrayList<String> names = new ArrayList<>();
        for (ClassReport report : classesForTerm) {
            names.add(report.getCourseName());
        }

        setUpMaterialTabs(names);

        if (mFragments.size() > 0)
            mViewPager.setCurrentItem(startIndex);
        else
            spinner.setVisibility(View.INVISIBLE);
//		mViewPager.setOffscreenPageLimit(5);

    }

    private void setUpMaterialTabs(ArrayList<String> temp) {
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setBackgroundColor(getResources().getColor((R.color.blue_500)));
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.white));
        slidingTabLayout.customTitle(temp);
        slidingTabLayout.setViewPager(mViewPager);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.class_swipe_actions, menu);

        if (termNum == 0)
            menu.findItem(R.id.previous_term).setEnabled(false);
        else if (termNum == ClassReport.NUM_TERMS - 1)
            menu.findItem(R.id.next_term).setEnabled(false);

        // Create list of students in Menu.
        if (session.MULTIPLE_STUDENTS) {
            for (int i = 0; i < session.getStudents().size(); i++) {
                String name = session.getStudents().get(i).name;
                MenuItem item = menu.add(name);

                // Set the currently enabled student un-clickable.
                if (i == studentIndex)
                    item.setEnabled(false);

                item.setOnMenuItemClickListener(new StudentSelectListener(i));
                item.setVisible(true);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.log_out:
                MainActivity.UserLogoutTask logout = new MainActivity.UserLogoutTask();
                ((YPApplication) getApplication()).session = session = null;
                logout.execute(session);

                //TODO: logout on MainActivity instead
                SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
                editor.putBoolean("auto_login", false);
                editor.commit();
                intent = new Intent(this, LoginActivity.class);
                // Clear all activities between this and LoginActivity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            /*
        case R.id.refresh:
			Intent refreshIntent = new Intent(this, LoginActivity.class);
			refreshIntent.putExtra("Refresh", true);
			startActivity(refreshIntent);
			finish();
			return true;
			 */
            case R.id.previous_term:
                intent = new Intent(this, ClassSwipeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("studentIndex", studentIndex);
                intent.putExtra("classCount", classCount);
                intent.putExtra("classIndex", mViewPager.getCurrentItem());
                // Don't go into the negatives!
                intent.putExtra("termNum", Math.max(termNum - 1, 0));
                startActivity(intent);

                //			overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_up);

                return true;
            case R.id.next_term:
                intent = new Intent(this, ClassSwipeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("studentIndex", studentIndex);
                intent.putExtra("classCount", classCount);
                intent.putExtra("classIndex", mViewPager.getCurrentItem());
                // Don't go too positive!
                intent.putExtra("termNum", Math.min(termNum + 1, ClassReport.NUM_TERMS - 1));
                startActivity(intent);
                //			overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
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

        List<Fragment> fragmentList;

        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            fragmentList = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < classesForTerm.size())
                return classesForTerm.get(position).getCourseName();
            return "ERROR";
        }
    }


    /**
     * A fragment that displays grades for one class.
     */
    public static class DescriptionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_CLASS_ID = "class_id";
        public static final int ASSIGNMENT_NAME_ID = 2222;

        private TermReportTask mTermReportTask;
        private int classID;
        private TermReport mTermReport;
        private View rootView;

        @Override
        public void onPause() {
            if (mTermReportTask != null)
                mTermReportTask.cancel(true);
            super.onPause();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (session.getStudents().size() == 0) {
                session = null;
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE).edit();
                editor.putBoolean("auto_login", false);
                editor.commit();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("show", true);
                startActivity(intent);
            }

            rootView = inflater.inflate(R.layout.class_description, container, false);
//			getActivity().setProgressBarIndeterminateVisibility(true);
            ((ProgressBar) toolbar.findViewById(R.id.action_bar_spinner)).setVisibility(View.VISIBLE);

            classID = getArguments().getInt(ARG_CLASS_ID);
            ClassReport classReport = session.getCurrentStudent().getClassReport(classID);
            mTermReport = classReport.getTerm(termNum);
            mTermReportTask = new TermReportTask();
            mTermReportTask.execute(mTermReport);
            return rootView;
        }

        /**
         * Task that loads a list of term-reports in the background, and shows the first term.
         */
        @SuppressLint("ResourceAsColor")
        public class TermReportTask extends AsyncTask<TermReport, Void, Void> {

            @Override
            protected void onPostExecute(final Void result) {
                setUiElements();
            }

            @Override
            protected Void doInBackground(TermReport... loading) {
                try {
                    for (TermReport t : loading)
                        t.loadReport(session);
                    mTermReport = loading[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }

        @SuppressWarnings("ResourceType")
        public void setUiElements() {
//			getActivity().setProgressBarIndeterminateVisibility(false);
            toolbar.findViewById(R.id.action_bar_spinner).setVisibility(View.INVISIBLE);
            toolbar.getTag(R.id.action_bar_spinner);
            RelativeLayout layout = (RelativeLayout) rootView.findViewById(R.id.info);

            int lastIdAdded = R.id.teacher_name;
            TextView teacher = (TextView) layout.findViewById(R.id.teacher_name);
            TextView sixWeeksAverage = (TextView) layout.findViewById(R.id.six_weeks_average);
            teacher.setVisibility(View.VISIBLE);
            sixWeeksAverage.setVisibility(View.VISIBLE);

            class AssignmentDetailListener implements OnClickListener {

                private final Assignment view;

                AssignmentDetailListener(Assignment view) {
                    this.view = view;
                }

                @Override
                public void onClick(View arg0) {
                    // Display the information.
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(view.getName());

                    StringBuilder msg = new StringBuilder();
                    msg.append(view.getCategory() == null ? "No category" : view.getCategory().getType())
                            .append("\nDue Date: " + DateHelper.daysRelative(view.getDueDate()))
                            .append("\nWeight: x" + String.format("%.1f", view.getWeight()));
                    try {
                        builder.setMessage(msg.toString());
                    } catch (Exception e) {
                        return;
                    }

                    builder.setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.create().show();
                }
            }

            // The following line prevents force close. Idk why.
            // Maybe the extra print time somehow fixes it...
            //System.out.println(mClassGrade);
            teacher.setText(session.getCurrentStudent().getClassReport(classID).getTeacherName());

            int avg = mTermReport.getGrade();
            if (avg >= 0) {
                String average = Integer.toString(avg);
                sixWeeksAverage.setText(average);
            } else
                sixWeeksAverage.setVisibility(View.INVISIBLE);

            // Add current student's name
            if (session.MULTIPLE_STUDENTS) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                LinearLayout studentName = (LinearLayout) inflater.inflate(R.layout.main_student_name_if_multiple_students, layout, false);
                ((TextView) studentName.findViewById(R.id.name)).setText(session.getCurrentStudent().name);

                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.BELOW, lastIdAdded);
                //noinspection ResourceType
                studentName.setId(id.student_name);
                lastIdAdded = studentName.getId();
                System.out.println("Student name box ID: " + lastIdAdded + ".");

                layout.addView(studentName, lp);
            }

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            boolean hasNoCategoryGrades = false;
            for (Assignment grade : mTermReport.getAssignments())
                if (grade.getCategory().getType().equals(GradeCategory.NO_CATEGORY))
                {
                    hasNoCategoryGrades = true;
                    break;
                }

            for (GradeCategory category : mTermReport.getCategories()) {
                if (category.getType().equals(GradeCategory.NO_CATEGORY) && !hasNoCategoryGrades)
                    continue;
                LinearLayout card = new LinearLayout(getActivity());
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.card_custom);

                // Name of the category ("Daily Work", etc)
                String categoryName = category.getType();

                // for every grade in this term [any category]
                for (Assignment grade : mTermReport.getAssignments()) {
                    // only if this grade is in the category which we're looking for
                    if (category.equals(grade.getCategory())) {
                        LinearLayout innerLayout = (LinearLayout) inflater.inflate(R.layout.class_swipe_grade_view, card, false);

                        TextView descriptionView = (TextView) innerLayout.findViewById(R.id.description);
                        String description = grade.getName();
                        descriptionView.setText(description);
                        descriptionView.setId(ASSIGNMENT_NAME_ID);

                        TextView txtGrade = (TextView) innerLayout.findViewById(R.id.grade);
                        txtGrade.setText(grade.getGrade() == -1 ? "" : String.format("%.0f", grade.getGrade()));

                        innerLayout.setOnClickListener(new AssignmentDetailListener(grade));

                        card.addView(innerLayout);
                    }

                }
                /*********************
                 * Create a category summary view
                 ********************/
                LinearLayout categoryLayout = (LinearLayout) inflater.inflate(R.layout.class_swipe_category_card, card, false);

                TextView categoryNameView = (TextView) categoryLayout.findViewById(R.id.category_name);
                categoryNameView.setText(categoryName);

                TextView scoreView = (TextView) categoryLayout.findViewById(R.id.category_score);

                int categoryScore = category.getGrade();
                if (categoryScore < 0)
                    scoreView.setText("");
                else
                    scoreView.setText("" + categoryScore);
                card.addView(categoryLayout);

                /*********************
                 * Animation
                 *********************/
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_from_down_rotate);
                animation.setStartOffset(0);

                card.setId(lastIdAdded + 1);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.BELOW, lastIdAdded);
                layout.addView(card, lp);
                lastIdAdded = card.getId();
                card.startAnimation(animation);
            }
        }
    }


    class StudentSelectListener implements MenuItem.OnMenuItemClickListener {

        int menuStudentIndex;

        public StudentSelectListener(int menuStudentIndex) {
            this.menuStudentIndex = menuStudentIndex;
        }

        @Override
        public boolean onMenuItemClick(MenuItem arg0) {

            session.studentIndex = menuStudentIndex;

            Intent intent = new Intent(ClassSwipeActivity.this, MainActivity.class);
            intent.putExtra("mainActivitySection", 1);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;

        }

    }

    private class id {
        static final int student_name = 234246;
    }

}
