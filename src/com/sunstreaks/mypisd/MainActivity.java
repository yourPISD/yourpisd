//THIS WAS EDITED WITHIN ECLIPSE AS AN TEST
package com.sunstreaks.mypisd;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {

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
	static Course[] courses = new Course[7];
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_OBJECT, position);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment  {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_OBJECT = "object";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			Bundle args = getArguments();
		    int position = args.getInt(ARG_OBJECT);

		    int tabLayout = 0;
		    switch (position) {
		    case 0:
			tabLayout = R.layout.tab_new;
			break;
		    case 1:
			tabLayout = R.layout.tab_summary;
			break;

		    }

		    View rootView = inflater.inflate(tabLayout, container, false);
		    if(position == 1)//on page 2 with grade summaries
		    try {
		    	SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
		    	JSONArray classGrades = new JSONArray(sharedPrefs.getString("classGrades", ""));
		    	
		    	TextView[] classTextViews = new TextView[classGrades.length()];
		    	RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.container);
		    	for (int i = 0; i < classGrades.length(); i++) {
		    		classTextViews[i] = new TextView(getActivity());
		    		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)classTextViews[i].getLayoutParams();
		    		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		    		params.addRule(RelativeLayout.BELOW, R.id.textView2);
		    		
		    		classTextViews[i].setText(classGrades.getJSONObject(i).getString("name"));
		    		
		    		classTextViews[i].setLayoutParams(params); //causes layout update
		    		rl.addView(classTextViews[i]);
		    	}
		    	rootView = rl;
//		    	classes[0] = (TextView)rootView.findViewById(R.id.class1);
//		    	x1.setText("Computer Science");
//		    	Button y1 = (Button)rootView.findViewById(R.id.button1);
//		    	y1.setText("97");
//		    	y1.setOnClickListener(new OnClickListener() {
//		    	    public void onClick(View v)
//		    	    {
//		    	        Intent startSwipe = new Intent(getActivity(), ClassSwipe.class);
//		    	        startSwipe.putExtra("period", 1);
//		    	        startActivity(startSwipe);
//		    	    } 
//		    	});
//		    	TextView x2 = (TextView)rootView.findViewById(R.id.class2);
//		    	x2.setText("Calculus BC");
//		    	Button y2 = (Button)rootView.findViewById(R.id.button2);
//		    	y2.setText("97");
//		    	y2.setOnClickListener(new OnClickListener() {
//		    	    public void onClick(View v)
//		    	    {
//		    	        Intent startSwipe = new Intent(getActivity(), ClassSwipe.class);
//		    	        startSwipe.putExtra("period", 2);
//		    	        startActivity(startSwipe);
//		    	    } 
//		    	});
		    } catch (JSONException e) {
		    	e.printStackTrace();
		    }
		    return rootView;
		}

		
	}

}
