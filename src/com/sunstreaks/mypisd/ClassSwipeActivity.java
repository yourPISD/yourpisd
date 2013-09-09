package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
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
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
	
	static DataGrabber dg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_swipe);
		
		received = getIntent().getExtras().getInt("classIndex");
		classCount = getIntent().getExtras().getInt("classCount");
		dg = getIntent().getParcelableExtra("DataGrabber");
		
		
		// 7 fragments were being added to mFragments every time that this onCreate method was run.
		if (mFragments == null) {
			mFragments = new ArrayList<Fragment>();
			for (int i = 0; i < classCount; i++) {
				mFragments.add(new DescriptionFragment());
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
		private final int position;
		private JSONObject mClassGrade;
		private String pageTitle;
		private String teacherName;
		private View rootView;
		private boolean gradeLoaded = false;
		private double random;
		private ViewGroup linearLayout;
		//private ViewGroup gradeListLayout;
		
		public DescriptionFragment() {
			this.position = classesMade++;
			if (classesMade > classCount)
				System.out.println("Something really weird is going on.");
			this.random = Math.random();
		}
		
		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
		}
		
		public DescriptionFragment(int position) {
			this.position = position;
			this.random = Math.random();
		}
		
		public String getPageTitle() {
			if (pageTitle == null)
				return Integer.toString(position);
			return pageTitle;
		}
		
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)  {
			
			
			this.pageTitle = getResources().getString(R.string.class_swipe_loading);
			
			if (mClassGradeTask != null)
				System.out.println("Grade task has been created before");
			else
				System.out.println("First time grade task");
			
//			position = mViewPager.getCurrentItem();
			System.out.println("position = " + position + " and random = " + random);
			
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



			return rootView;
		}
		
//		@Override
//		public void onPause() {
//			super.onPause();
//		}
		
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
					
					
					ScrollView sv = new ScrollView(getActivity());
					LinearLayout layout = new LinearLayout(getActivity());
					layout.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		            		LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		            
		            lp.setMargins(30, 20, 30, 0);
		            layout.setLayoutParams(
		            		new LinearLayout.LayoutParams(
		            				AbsListView.LayoutParams.MATCH_PARENT, 
		            				AbsListView.LayoutParams.MATCH_PARENT));
		            for(int i = 0; i< mClassGrade.getJSONArray("terms")
		            		.getJSONObject(0).getJSONArray("grades").length(); i++)
		            {
		            	LinearLayout innerLayout = new LinearLayout(getActivity());
			            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
			   
			            TextView className = new TextView(getActivity());
			            className.setText(mClassGrade.getJSONArray("terms")
			            		.getJSONObject(0).getJSONArray("grades")
			            		.getJSONObject(i).getString("Description"));
			            //className.setBackgroundColor(Color.WHITE);
			            className.setTextSize(20);
			            className.setLayoutParams(
			            		new LinearLayout.LayoutParams(
			            				LinearLayout.LayoutParams.WRAP_CONTENT, 
			            				LinearLayout.LayoutParams.WRAP_CONTENT, 
			            				1f));
			            TextView grade = new TextView(getActivity());
			            grade.setText(mClassGrade.getJSONArray("terms")
			            		.getJSONObject(0).getJSONArray("grades")
			            		.getJSONObject(i).optString("Grade", ""));
			            grade.setTextSize(30);
			            innerLayout.addView(className);
			            innerLayout.addView(grade);
			            innerLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.dropshadow));
			            layout.addView(innerLayout, lp);
		            }
		            linearLayout.addView(layout);
//					gradeListLayout = layout;
					
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
