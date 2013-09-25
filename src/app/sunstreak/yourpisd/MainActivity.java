package app.sunstreak.yourpisd;

import java.util.Locale;

import android.content.Context;
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


public class MainActivity extends FragmentActivity implements View.OnClickListener {

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

		// Makes sure that there IS a datagrabber to insert. Now, what if there were multiple
		// versions of DataGrabber....? I wonder what would happen.
		//		Intent intent = getIntent();
		//		{
		//			DataGrabber myDG = (DataGrabber) intent.getParcelableExtra("DataGrabber");
		//			dg = myDG;
		//		}
		dg = ( (DataGrabber) getApplication() );

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		// Opens Grade Summary (index 1) on open.
		mViewPager.setCurrentItem(1);
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
				return "Profile";
			case 1:
				return TermFinder.Term.values()[CURRENT_TERM_INDEX].name.toUpperCase(l);
			case 2:
				return "Semester Summary";
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

		LinearLayout card;

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

			if (position == 1)
			{
					    		
				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.container);



				int[][] gradeSummary = dg.getGradeSummary();

				classCount = gradeSummary.length;
				averages = new LinearLayout[classCount];

				int[] classMatch = new int[classCount];
				int classesMatched = 0;

				while (classesMatched < classCount) {
					for (int i = classesMatched; i < dg.getClassIds().length; i++) {
						if (dg.getClassIds()[i] == gradeSummary[classesMatched][0]) {
							classMatch[classesMatched] = i;
							classesMatched++;
							break;
						}
					}
				}

				dg.setClassMatch(classMatch);

				for (int i = 0; i < classCount; i++) {

					int jsonIndex = classMatch[i];

					
					LinearLayout card = (LinearLayout) inflater.inflate(R.layout.main_grade_summary, bigLayout, false);
					TextView className = (TextView) card.findViewById(R.id.name);
					String name = dg.getClassName(jsonIndex);
					className.setText(name);

					int avg = gradeSummary[i][1 + 0];
					String average = avg == -1 ? "" : avg + "";
					TextView grade = (TextView) card.findViewById(R.id.grade);
					averages[i]=card;
					
					card.setOnClickListener(new ClassSwipeOpenerListener(i, CURRENT_TERM_INDEX));

					grade.setText(average);

					bigLayout.addView(card);




				}

				return rootView;
			}	


			if (position == 0) {
				StudentPictureTask spTask = new StudentPictureTask();
				spTask.execute();
			}

			if (position == 2) {

				LinearLayout bigLayout = (LinearLayout) rootView.findViewById(R.id.layout_year_summary);

				int[] classMatch = dg.getClassMatch();
				int[][] gradeSummary = dg.getGradeSummary();

				for (int classIndex = 0; classIndex < classCount; classIndex++) {


					int jsonIndex = classMatch[classIndex];

					View classSummary = inflater.inflate(R.layout.main_grade_summary_linear_layout, bigLayout, false);

					TextView className = (TextView) classSummary.findViewById(R.id.class_name);

					className.setText(dg.getClassName(jsonIndex));

					LinearLayout summary = (LinearLayout) classSummary.findViewById(R.id.layout_six_weeks_summary);

					for (int termIndex = 1; termIndex < gradeSummary[classIndex].length; termIndex++) {


		
						TextView termGrade = new TextView(getActivity());
						termGrade.setTextSize(25);
						termGrade.setPadding(10, 10, 10, 10);
						termGrade.setClickable(true);
						

						
						termGrade.setOnClickListener(new ClassSwipeOpenerListener(classIndex, termIndex - 1));
						
						int avg = gradeSummary[classIndex][termIndex];
						System.out.println(avg);
						String average = avg == -1 ? "" : "" + avg;
						termGrade.setText(average);
						summary.addView(termGrade);


					}

					bigLayout.addView(classSummary);
				}
			}

			return rootView;

		}
		public class StudentPictureTask extends AsyncTask<Void, Void, Bitmap> {

			@Override
			protected Bitmap doInBackground(Void... arg0) {	
				return dg.getStudentPicture();

			}

			@Override
			protected void onPostExecute (final Bitmap result) {
				LinearLayout lv = (LinearLayout)rootView.findViewById(R.id.overall);
				ImageView profilePic = new ImageView(getActivity());

				card = new LinearLayout(getActivity());

				Drawable picture = new BitmapDrawable(getResources(), result);
				profilePic.setImageDrawable(picture);
				LinearLayout.LayoutParams profileLP = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				profileLP.setMargins(10, 10, 10, 10);
				profilePic.setLayoutParams(profileLP);


				card.addView(profilePic);
				card.setBackgroundDrawable(getResources().getDrawable(R.drawable.dropshadow));
				StudentNameTask snTask = new StudentNameTask();
				snTask.execute();
				lv.addView(card);
			}

		}
		public class StudentNameTask extends AsyncTask<Void, Void, String> {

			@Override
			protected String doInBackground(Void... arg0) {	
				return dg.getStudentName();

			}

			@Override
			protected void onPostExecute (final String result) {
				TextView name = new TextView(getActivity());
				name.setTextSize(25);
				name.setText(result);
				card.addView(name);
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

	}



	@Override
	public void onClick(View v) {
		System.out.println(v.getId());
		Intent intent = new Intent (this, ClassSwipeActivity.class);
		intent.putExtra("classCount", classCount);
		intent.putExtra("classIndex", v.getId());
		intent.putExtra("termIndex", CURRENT_TERM_INDEX);
		startActivity(intent);
	}


	
}
