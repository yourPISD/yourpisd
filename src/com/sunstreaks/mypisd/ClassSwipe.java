package com.sunstreaks.mypisd;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import com.sunstreaks.mypisd.net.DataGrabber;
import com.sunstreaks.mypisd.net.Domain;
import com.sunstreaks.mypisd.net.IllegalUrlException;
import com.sunstreaks.mypisd.net.PISDException;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_swipe);
		received = getIntent().getExtras().getInt("classIndex");
		
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
			Locale l = Locale.getDefault();
			try {
				SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ClassSwipe.this);
		    	JSONArray gradeSummary = new JSONArray(sharedPrefs.getString("gradeSummary", "No name"));
		    	return gradeSummary.getJSONObject(position).getString("title");
			} catch (JSONException e) {
				return "Class title";
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
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.class_description, container, false);
			
			
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			//
			JSONArray classGrades = null;
			JSONArray grades;
			try {
				classGrades = new JSONArray(sharedPrefs.getString("classGrades", ""));
				grades = classGrades.getJSONArray(mViewPager.getCurrentItem());
			} catch (JSONException e) {
				// This page has not been loaded.... ever.
				try {
					DataGrabber.login();
					for (int i = 0; i < ClassSwipe.mFragments.size(); i++) {
						//hard coded for first six weeks!
						DataGrabber.getClassGrades(i, 0);
					}
					SharedPreferences.Editor editor = sharedPrefs.edit();
					classGrades = DataGrabber.getClassGrades();
					editor.putString("classGrades", classGrades.toString());
					editor.commit();
				} catch (Exception f) {
					e.printStackTrace();
				}
			}
				try {
					grades = classGrades.getJSONArray(mViewPager.getCurrentItem());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				TextView teacher = (TextView) rootView.findViewById(R.id.teacher);
				teacher.setText("Tracy Ishman");

			return rootView;
		}
	}

}
