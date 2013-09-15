package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sunstreaks.mypisd.MainActivity.MainActivityFragment;
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
//		dg = getIntent().getParcelableExtra("DataGrabber");
		
		dg = ((YourPISDApplication) getApplication() ).getDataGrabber();
		
		// 7 fragments were being added to mFragments every time that this onCreate method was run.
		{
			YourPISDApplication appState = ((YourPISDApplication)this.getApplication());
			List<Fragment> mFragments = appState.mFragments;
			ClassSwipeActivity.mFragments = mFragments;
		}
		if (mFragments == null) {
			mFragments = new ArrayList<Fragment>();
			for (int i = 0; i < classCount; i++) {
				Bundle args = new Bundle();
				args.putInt(DescriptionFragment.ARG_SECTION_NUMBER, i);
				Fragment fragment = new DescriptionFragment();
				fragment.setArguments(args);
				mFragments.add(fragment);
			}
		}
		
		
		System.out.println("mFragments size = " + mFragments.size() + "and classCount = " + classCount);
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.

		mSectionsPagerAdapter = new SectionsPagerAdapter(
			       getSupportFragmentManager(), mFragments);


		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(received);
		mViewPager.setOffscreenPageLimit(7);


	}

	@Override
	protected void onPause() {
		super.onPause();
		YourPISDApplication appState = (YourPISDApplication)this.getApplication();
		appState.mFragments = this.mFragments;
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
			return ( (DescriptionFragment)fragmentList.get(position) ).getPageTitle();
			//return dg.getClassName(position);
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
		private JSONObject mClassGrade;
		private String pageTitle;
		private String teacherName;
		private View rootView;
		private boolean gradeLoaded = false;
		
		private ViewGroup linearLayout;
		private LinearLayout gradesListLayout;
		


		
		public DescriptionFragment() {
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
		}
		

		
		public String getPageTitle() {
			if (pageTitle == null)
				return Integer.toString(position);
			return pageTitle;
		}
		
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)  {

			position = getArguments().getInt(ARG_SECTION_NUMBER);
			System.out.println("position = " + position);
			
			this.pageTitle = getResources().getString(R.string.class_swipe_loading);
			
			if (mClassGradeTask != null)
				System.out.println("Grade task has been created before");
			else
				System.out.println("First time grade task");
			
			pageTitle = dg.getClassName(position); 
			
			rootView = inflater.inflate(R.layout.class_description, container, false);
			

			linearLayout =  (ViewGroup) rootView.findViewById(R.id.class_description_linear_layout);
			
			if (this.teacherName == null)
				this.teacherName = getResources().getString(R.string.teacher_name);
			
			( (TextView) rootView.findViewById(R.id.teacher) ).setText(teacherName);

			
			if (this.mClassGrade == null && !gradeLoaded) {

				mClassGradeTask = new ClassGradeTask();
				gradeLoaded = true;
				mClassGradeTask.execute(position, 0);
			}
			else {
				((ViewGroup)gradesListLayout.getParent()).removeView(gradesListLayout);
				linearLayout.addView(gradesListLayout);
			}

			return rootView;
		}
		
		@Override
		public void onPause() {
			super.onPause();
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
				System.out.println(mClassGrade);
				
				TextView teacher = (TextView) rootView.findViewById(R.id.teacher);

				try {
					teacherName = mClassGrade.getString("teacher");
					teacher.setText(teacherName);
					
					
					gradesListLayout = new LinearLayout(getActivity());
					gradesListLayout.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams overAll = new LinearLayout.LayoutParams(
		            		LinearLayout.LayoutParams.MATCH_PARENT,
		            		LinearLayout.LayoutParams.MATCH_PARENT);
					overAll.setMargins(5, 5, 5, 5);
					
					gradesListLayout.setLayoutParams(overAll);
					String average = mClassGrade.getJSONArray("terms").getJSONObject(0).optInt("average", -1)+"";
					TextView averageText = new TextView(getActivity());
					averageText.setTextSize(20);
					averageText.setText(average);
		            gradesListLayout.addView(averageText);
					JSONArray terms = mClassGrade.getJSONArray("terms");
					
					
					
					for (int category = 0;
							category < terms.getJSONObject(0).getJSONArray("categoryGrades").length();
							category++)
					{
						LinearLayout categoryLayout = new LinearLayout(getActivity());
						categoryLayout.setOrientation(LinearLayout.VERTICAL);
						
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

			            		
			            		LinearLayout innerLayout = new LinearLayout(getActivity());
					            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
					   
					            // Create a textview to display the assignment description
					            TextView descriptionView = new TextView(getActivity());
					            descriptionView.setText(" " + mClassGrade.getJSONArray("terms")
					            		.getJSONObject(0).getJSONArray("grades")
					            		.getJSONObject(i).getString("Description"));
					            //className.setBackgroundColor(Color.WHITE);
					            descriptionView.setTextSize(20);
					            
					            LinearLayout.LayoutParams descriptionLP = new LinearLayout.LayoutParams(
					            		LinearLayout.LayoutParams.WRAP_CONTENT,
					            		LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
					            
					            descriptionView.setLayoutParams(descriptionLP);
					            descriptionView.setPadding(10,10,10,10);
					            
					            TextView grade = new TextView(getActivity());
					            grade.setText(mClassGrade.getJSONArray("terms")
					            		.getJSONObject(0).getJSONArray("grades")
					            		.getJSONObject(i).optString("Grade", "") + " ");
					            grade.setTextSize(25);
					            innerLayout.addView(descriptionView);
					            innerLayout.addView(grade);
					            
					            LinearLayout.LayoutParams scoreLP = new LinearLayout.LayoutParams(
					            		LinearLayout.LayoutParams.MATCH_PARENT,
					            		LinearLayout.LayoutParams.WRAP_CONTENT);
					            
					            scoreLP.setMargins(10,10,10,0);
					            innerLayout.setLayoutParams(scoreLP);
					            
//					            innerLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dropshadow));
					            innerLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.divider));
								LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams (
										LinearLayout.LayoutParams.MATCH_PARENT,
										LinearLayout.LayoutParams.WRAP_CONTENT);
								lp.setMargins(15,20,15,0);
					            
								categoryLayout.addView(innerLayout);
								innerLayout = null;
			            	}

			            }
						
			            LinearLayout categorySummaryLayout = new LinearLayout(getActivity());
			            categorySummaryLayout.setOrientation(LinearLayout.HORIZONTAL);
			            
			            TextView categoryNameView = new TextView(getActivity());
			            categoryNameView.setTypeface(null, Typeface.BOLD);
			            categoryNameView.setText(categoryName);
			            categoryNameView.setTextSize(23);

			            LinearLayout.LayoutParams descriptionLP = new LinearLayout.LayoutParams(
			            		LinearLayout.LayoutParams.WRAP_CONTENT,
			            		LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
			            
			            categoryNameView.setLayoutParams(descriptionLP);
			            
			            TextView scoreView = new TextView(getActivity());
			            scoreView.setText(mClassGrade.getJSONArray("terms")
			            		.getJSONObject(0).getJSONArray("categoryGrades")
			            		.getJSONObject(category).optString("Letter", "") + " ");
			            scoreView.setTextSize(30);
			            categorySummaryLayout.addView(categoryNameView);
			            categorySummaryLayout.addView(scoreView);
			            
			            LinearLayout.LayoutParams categorySummaryLP = new LinearLayout.LayoutParams(
			            		LinearLayout.LayoutParams.MATCH_PARENT,
			            		LinearLayout.LayoutParams.WRAP_CONTENT);
			            
			            categorySummaryLP.setMargins(15,10,15,0);
			            categorySummaryLayout.setLayoutParams(categorySummaryLP);
			            
			            categoryLayout.addView(categorySummaryLayout, categorySummaryLP);
			            categoryLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dropshadow));
			            gradesListLayout.addView(categoryLayout);
			            
					}
					
					
					

		            linearLayout.addView(gradesListLayout);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				

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

	}

	public class GradeSummaryTask extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				return dg.loadGradeSummary();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	

	
}
