package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
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
	ArrayList<Fragment> mFragments;
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
	ViewPager mViewPager;
	static int received;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_swipe);
		received = getIntent().getExtras().getInt("period");
		
		mFragments = new ArrayList<Fragment>();
		mFragments.add(new DescriptionFragment());
		mFragments.add(new DescriptionFragment());
		mFragments.add(new DescriptionFragment());
		mFragments.add(new DescriptionFragment());
		mFragments.add(new DescriptionFragment()); 
		mFragments.add(new DescriptionFragment());
		mFragments.add(new DescriptionFragment());
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		  mSectionsPagerAdapter = new SectionsPagerAdapter(
			        getSupportFragmentManager(), mFragments);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(received-1);
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
			switch (position) {
			case 0:
				return "Computer Science";
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			case 3:
				return getString(R.string.title_section3).toUpperCase(l);
			case 4:
				return getString(R.string.title_section3).toUpperCase(l);
			case 5:
				return getString(R.string.title_section3).toUpperCase(l);
			case 6:
				return getString(R.string.title_section3).toUpperCase(l);
			case 7:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
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
				TextView teacher = (TextView) rootView.findViewById(R.id.teacher);
				teacher.setText("Tracy Ishman");

			return rootView;
		}
	}

}
