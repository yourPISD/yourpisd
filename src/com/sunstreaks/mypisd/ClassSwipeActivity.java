package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunstreaks.mypisd.net.DataGrabber;

@SuppressLint("ValidFragment")
public class ClassSwipeActivity extends FragmentActivity {
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
	

	static int received;
	static int classCount;
	static int classesMade = 0;
	static boolean doneMakingClasses;
	
	static DataGrabber dg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_swipe);
		
		received = getIntent().getExtras().getInt("classIndex");
		classCount = getIntent().getExtras().getInt("classCount");
		
		dg = ((DataGrabber) getApplication() );
		
		// 7 fragments were being added to mFragments every time that this onCreate method was run.
//		{
//			YourPISDApplication appState = ((YourPISDApplication)this.getApplication());
//			List<Fragment> mFragments = appState.mFragments;
//			ClassSwipeActivity.mFragments = mFragments;
//		}
//		if (mFragments == null) {
			mFragments = new ArrayList<Fragment>();
			for (int i = 0; i < classCount; i++) {
				Bundle args = new Bundle();
				args.putInt(DescriptionFragment.ARG_SECTION_NUMBER, i);
				Fragment fragment = new DescriptionFragment();
				fragment.setArguments(args);
				mFragments.add(fragment);
			}
//		}
		
		
		System.out.println("mFragments size = " + mFragments.size() + " and classCount = " + classCount);
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.

		mSectionsPagerAdapter = new SectionsPagerAdapter(
			       getSupportFragmentManager(), mFragments);


		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(received);
		mViewPager.setOffscreenPageLimit(7);
//		PagerTitleStrip classes = (PagerTitleStrip)findViewById(R.id.pager_title_strip);
//		classes.setTextSize(, 10);

	}

	@Override
	protected void onPause() {
		super.onPause();
//		OldYourPISDApplication appState = (OldYourPISDApplication)this.getApplication();
////		appState.mFragments = this.mFragments;
//		appState.setDataGrabber(dg);
	}
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.class_swipe, menu);
		return true;
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
	        Fragment fragment = fragmentList.get(position);        
	        return fragment;
	    }

		@Override
		public int getCount() {
		    return fragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			//return ( (DescriptionFragment)fragmentList.get(position) ).getPageTitle();
			return dg.getClassName(dg.getClassMatch()[position]);
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
		public static final String ARG_SECTION_NUMBER = "section_number";
		private ClassGradeTask mClassGradeTask;
		private int position;
		private int classIndex;
		private final int TERM_INDEX = 0;
		private JSONObject mClassGrade;
//		private String pageTitle;
		private String teacherName;
		private View rootView;
		private boolean gradeLoaded = false;
		
//		private ViewGroup linearLayout;
//		private LinearLayout gradesListLayout;
		


		
		public DescriptionFragment() {
			
		}
		
//		@Override
//		public void onAttach(Activity activity) {
//			super.onAttach(activity);
//		}
		

		
//		public String getPageTitle() {
//			if (pageTitle == null)
//				return Integer.toString(position);
//			return pageTitle;
//		}
		
		
		@Override
		public void onPause () {
			if (mClassGradeTask != null)
				mClassGradeTask.cancel(true);
			super.onPause();
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)  {

			position = getArguments().getInt(ARG_SECTION_NUMBER);
			classIndex = dg.getClassMatch()[position];
			System.out.println("position = " + position);
			
//			this.pageTitle = getResources().getString(R.string.class_swipe_loading);	
			//pageTitle = dg.getClassName(classIndex); 
			
			rootView = inflater.inflate(R.layout.class_description, container, false);
			

//			linearLayout =  (ViewGroup) rootView.findViewById(R.id.class_description_linear_layout);
			
//			if (this.teacherName == null)
//				this.teacherName = getResources().getString(R.string.teacher_name);
//			
//			( (TextView) rootView.findViewById(R.id.teacher_name) ).setText(teacherName);

			if ( dg.hasClassGrade(classIndex) ) {
				mClassGrade = dg.getClassGrade(classIndex);
				setUiElements();
			} else {
				mClassGradeTask = new ClassGradeTask();
				mClassGradeTask.execute(classIndex, TERM_INDEX);
			}

			
			/*
			if (this.mClassGrade == null && !gradeLoaded) {

				mClassGradeTask = new ClassGradeTask();
				gradeLoaded = true;
				mClassGradeTask.execute(classIndex, TERM_INDEX);
			}
			else {
//				((ViewGroup)gradesListLayout.getParent()).removeView(gradesListLayout);
//				linearLayout.addView(gradesListLayout);
			}
			*/

			return rootView;
		}
		

		
		@SuppressLint("ResourceAsColor")
		public class ClassGradeTask extends AsyncTask<Integer, Void, JSONObject> {
			
			@Override
			protected void onPreExecute () {
				System.out.println("ClassGradeTask starting");
			}
			
			@Override
			protected void onPostExecute (JSONObject result) {
				mClassGrade = result;
				System.out.println("ClassGradeTask finished");
				
				dg.putClassGrade(classIndex, mClassGrade);
				
				setUiElements();
			}
			
			@Override
			protected JSONObject doInBackground(Integer... integers) {
				try {
					return dg.getClassGrade(integers[0], integers[1]);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}

		public void setUiElements () {
			
			TextView teacher = (TextView) rootView.findViewById(R.id.teacher_name);
			TextView sixWeeksAverage = (TextView) rootView.findViewById(R.id.six_weeks_average);

			LinearLayout classDescriptionLinearLayout = (LinearLayout) rootView.findViewById(R.id.class_description_linear_layout);
			
			try {
				teacherName = mClassGrade.getString("teacher");
				teacher.setText(teacherName);
								
				int avg = mClassGrade.getJSONArray("terms").getJSONObject(0).optInt("average", -1);
				String average = avg == -1 ? "" : "" + avg;
				sixWeeksAverage.setText(average);

				JSONArray terms = mClassGrade.getJSONArray("terms");
				
				for (int category = 0;
						category < terms.getJSONObject(0).getJSONArray("categoryGrades").length();
						category++)
				{

					
					LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					LinearLayout categoryLayout = (LinearLayout) inflater.inflate(R.layout.class_swipe_category_card, classDescriptionLinearLayout, false);
					LinearLayout gradesListLayout = (LinearLayout) categoryLayout.findViewById(R.id.layout_grades_list);
							
					// Name of the category ("Daily Work", etc)
					String categoryName = mClassGrade.getJSONArray("terms").getJSONObject(0)
							.getJSONArray("categoryGrades").getJSONObject(category).getString("Category");
					
					// for every grade in this term [any category]
		            for(int i = 0; i< mClassGrade.getJSONArray("terms")
		            		.getJSONObject(0).getJSONArray("grades").length(); i++)
		            {
		            	// only if this grade is in the category which we're looking for
		            	if (mClassGrade.getJSONArray("terms").getJSONObject(0)
		            			.getJSONArray("grades").getJSONObject(i).getString("Category")
		            			.equals(categoryName))
		            	{
		            		LinearLayout innerLayout = (LinearLayout) inflater.inflate(R.layout.class_swipe_grade_view, categoryLayout, false);

		            		TextView descriptionView = (TextView) innerLayout.findViewById(R.id.description);
				            String description = "" + mClassGrade.getJSONArray("terms")
				            		.getJSONObject(0).getJSONArray("grades")
				            		.getJSONObject(i).getString("Description");
				            descriptionView.setText(description);

				            TextView grade = (TextView) innerLayout.findViewById(R.id.grade);
				            String gradeValue = mClassGrade.getJSONArray("terms")
				            		.getJSONObject(0).getJSONArray("grades")
				            		.getJSONObject(i).optString("Grade", "") + "";
				            grade.setText(gradeValue);

							gradesListLayout.addView(innerLayout);
		            	}

		            }
					

		            
		            TextView categoryNameView = (TextView) categoryLayout.findViewById(R.id.category_name);
		            categoryNameView.setText(categoryName);
		            
		            TextView scoreView = (TextView) categoryLayout.findViewById(R.id.category_score);
		            String categoryScore = mClassGrade.getJSONArray("terms")
		            		.getJSONObject(0).getJSONArray("categoryGrades")
		            		.getJSONObject(category).optString("Letter", "") + "";
		            scoreView.setText(categoryScore);

		            classDescriptionLinearLayout.addView(categoryLayout);
		            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
		            animation.setStartOffset(0);
		            categoryLayout.startAnimation(animation);
				}
				

				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}

	}


	

	
}
