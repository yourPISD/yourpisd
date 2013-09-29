package app.sunstreak.yourpisd;

import java.util.Locale;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import app.sunstreak.yourpisd.net.DataGrabber;


public class MainActivity extends FragmentActivity {

	public static final int CURRENT_TERM_INDEX = TermFinder.getCurrentTermIndex();
	static int classCount;
	static LinearLayout[] averages;
	static DataGrabber dg;
	static Bitmap proPic;

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

		dg = (DataGrabber) getApplication();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		// Opens Grade Summary (index 1) on open.
		mViewPager.setCurrentItem(1);
		
		mViewPager.setOffscreenPageLimit(2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.log_out:
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.credits:
			Intent intentCred1 = new Intent(this, CreditActivity.class);
			startActivity(intentCred1);
			return true;
		case R.id.refresh:
			Intent refreshIntent = new Intent(this, LoginActivity.class);
			refreshIntent.putExtra("Refresh", true);
			startActivity(refreshIntent);
			finish();
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
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				// Profile
				return getResources().getString(R.string.main_section_0_title);
			case 1:
				// Why is this tab the only one that is uppercase?
				return TermFinder.Term.values()[CURRENT_TERM_INDEX].name.toUpperCase(l);
			case 2:
				// Semester Summary
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

		LinearLayout[] profileCards;

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

			classCount = dg.getStudents().get(dg.studentIndex).getClassMatch().length;

			rootView = inflater.inflate(tabLayout, container, false);

			
			if (position == 0) {
				profileCards = new LinearLayout[dg.getStudents().size()];
				
				for (int i = 0; i < dg.getStudents().size(); i++) {
					
					profileCards[i] = new LinearLayout(getActivity());
					
					
					TextView name = new TextView(getActivity());
					name.setTextSize(25);
					name.setText(dg.getStudents().get(i).name);
					
					profileCards[i].addView(name);
					
					
					
					StudentPictureTask spTask = new StudentPictureTask();
					spTask.execute(i);
				}
				
//				if (dg.getStudents().size() > 1)
					colorStudents();
				
			}
			
			if (position == 1)
			{
					    		
				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.container);



				averages = new LinearLayout[classCount];

				int[] classMatch = dg.getStudents().get(dg.studentIndex).getClassMatch();

				for (int i = 0; i < classCount; i++) {

					int jsonIndex = classMatch[i];

					
					LinearLayout card = (LinearLayout) inflater.inflate(R.layout.main_grade_summary, bigLayout, false);
					TextView className = (TextView) card.findViewById(R.id.name);
					String name = dg.getStudents().get(dg.studentIndex).getClassName(jsonIndex);
					className.setText(name);

					int avg = dg.getStudents().get(dg.studentIndex).getGradeSummary()[i][1 + CURRENT_TERM_INDEX];
					
					// No need to increase overdraw if there is nothing to display
					if (avg != -1) {
						String average = avg + "";
						TextView grade = (TextView) card.findViewById(R.id.grade);
						averages[i]=card;
						grade.setText(average);
					}
					
					card.setOnClickListener(new ClassSwipeOpenerListener(i, CURRENT_TERM_INDEX));

					

					bigLayout.addView(card);



				}

				return rootView;
			}	




			if (position == 2) {

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.layout_year_summary);

				int[] classMatch = dg.getStudents().get(dg.studentIndex).getClassMatch();
				int[][] gradeSummary = dg.getStudents().get(dg.studentIndex).getGradeSummary();

				for (int classIndex = 0; classIndex < classCount; classIndex++) {


					int jsonIndex = classMatch[classIndex];

					View classSummary = inflater.inflate(R.layout.main_grade_summary_linear_layout, bigLayout, false);
					
					TextView className = (TextView) classSummary.findViewById(R.id.class_name);
					className.setText(dg.getStudents().get(dg.studentIndex).getClassName(jsonIndex));

					LinearLayout summary = (LinearLayout) classSummary.findViewById(R.id.layout_six_weeks_summary);

					for (int termIndex = 1; termIndex < gradeSummary[classIndex].length; termIndex++) {
						
						TextView termGrade = new TextView(getActivity());
						termGrade.setTextSize(25);
						termGrade.setPadding(10, 10, 10, 10);
						termGrade.setClickable(true);
						termGrade.setOnClickListener(new ClassSwipeOpenerListener(classIndex, termIndex - 1));
						termGrade.setBackgroundResource(R.drawable.dropshadow_white_to_blue);
						
						int avg = gradeSummary[classIndex][termIndex];
						if (avg == -1)
							continue;
						String average = avg + "";
						termGrade.setText(average);
						summary.addView(termGrade);


					}

					bigLayout.addView(classSummary);
				}
			}

			return rootView;

		}
		public class StudentPictureTask extends AsyncTask<Integer, Void, Bitmap> {

			int taskStudentIndex;
			
			@Override
			protected Bitmap doInBackground(Integer... args) {	
				taskStudentIndex = args[0];
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


				
				lv.addView(profileCards[taskStudentIndex], taskStudentIndex + 1);
			}

		}

		
		
		class ClassSwipeOpenerListener implements OnClickListener {
			
			int classIndex;
			int termIndex;
			
			ClassSwipeOpenerListener (int classIndex, int termIndex) {
				this.classIndex = classIndex;
				this.termIndex = termIndex;
			}
			
			public void onClick(View arg0) {
				Intent intent = new Intent (getActivity(), ClassSwipeActivity.class);
				intent.putExtra("classCount", classCount);
				intent.putExtra("classIndex", classIndex);
				intent.putExtra("termIndex", termIndex);
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
	
}
