package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;

import com.sunstreaks.mypisd.net.DataGrabber;


import android.annotation.SuppressLint;
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
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class ClassSwipe extends FragmentActivity {
	static List<Fragment> mFragments = new ArrayList<Fragment>();
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
	static String test = "Ishman";
	String test2 = "OMGOMG";
	static ViewPager mViewPager;
	static int received;
	static DataGrabber dg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_swipe);
		received = getIntent().getExtras().getInt("classIndex");
		
		{	// let's hope this doesn't accidentally create multiple versions of dg.
			DataGrabber myDG = getIntent().getParcelableExtra("DataGrabber");
			if (myDG != null)
				dg = myDG;
		}
		
		for (int i = 0; i < getIntent().getExtras().getInt("classCount"); i++) {
			mFragments.add(new DescriptionFragment());
		}
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
			        getSupportFragmentManager(), mFragments);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(received);
		mViewPager.setOffscreenPageLimit(1);
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
//			Locale l = Locale.getDefault();
			try {
//				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ClassSwipe.this);
//		    	JSONArray gradeSummary = new JSONArray(sharedPrefs.getString("gradeSummary", "No name"));		    	
//		    	return gradeSummary.getJSONObject(position).getString("title");
		    	JSONArray gradeSummary = dg.getGradeSummary();
		    	if (gradeSummary == null) {
		    		GradeSummaryTask gsTask = new GradeSummaryTask();
		    		gsTask.execute();
		    		gradeSummary = gsTask.get();
		    	}
		    	return gradeSummary.getJSONObject(position).getString("title");
			} catch (JSONException e) {
				return "Class title";
			} catch (InterruptedException e) {
				e.printStackTrace();
				return "Interrupted exception";
			} catch (ExecutionException e) {
				e.printStackTrace();
				return "Execution exception";
			}
		}
	}
	

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DescriptionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";
		public DescriptionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)  {
			View rootView = inflater.inflate(
					R.layout.class_description, container, false);
			
			
//			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			//
			JSONArray classGrades = null;
			JSONArray grades;
			try {
//				classGrades = new JSONArray(sharedPrefs.getString("classGrades", ""));
				classGrades = dg.getClassGrades();
				grades = classGrades.getJSONArray(mViewPager.getCurrentItem());
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
			
				// This page has not been loaded.... ever.
				try {
					//dg.login();
					for (int i = 0; i < ClassSwipe.mFragments.size(); i++) {
						//hard coded for first six weeks!
						ClassGradesTask cgTask = new ClassGradesTask();
						cgTask.execute(i, 0);
						cgTask.get();
						System.out.println("Loop run");
					}
//					SharedPreferences.Editor editor = sharedPrefs.edit();
					classGrades = dg.getClassGrades();
//					editor.putString("classGrades", classGrades.toString());
//					editor.commit();
				} catch (Exception f) {
					e.printStackTrace();
				}
			}
				try {
					grades = classGrades.getJSONArray(mViewPager.getCurrentItem());
					TextView teacher = (TextView) rootView.findViewById(R.id.teacher);
					teacher.setText(dg.getClassGrades().getJSONObject(mViewPager.getCurrentItem())
						.getString("teacher"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			return rootView;
		}
		
		
		public class ClassGradesTask extends AsyncTask<Integer, Void, Boolean> {

			@Override
			protected Boolean doInBackground(Integer... args) {
				
				try {
					dg.getClassGrades(args[0], args[1]);
					return true;
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				} catch (ExecutionException e) {
					e.printStackTrace();
					return false;
				}
				
			}
			
		}
	}

	public class GradeSummaryTask extends AsyncTask<Void, Void, JSONArray> {

		@Override
		protected JSONArray doInBackground(Void... params) {
			try {
				return dg.loadGradeSummary();
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
	}
	

	
}
