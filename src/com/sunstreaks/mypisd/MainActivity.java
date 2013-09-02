//THIS WAS EDITED WITHIN ECLIPSE AS AN TEST
package com.sunstreaks.mypisd;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout.LayoutParams;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

	static int classCount;
	static Button[] buttons;
	
	
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
//		    	{
//		    	TextView classGradesJson = (TextView) getActivity().findViewById(R.id.class_grades);
//		    	SharedPreferences sharedPrefs = getActivity().getPreferences(Context.MODE_PRIVATE);
//		    	String classGrades = sharedPrefs.getString("classGrades", "class grades go here");
//		    	classGradesJson.setText(classGrades);
//		    	}
		    	break;
		    case 1:
		    	tabLayout = R.layout.tab_summary;
		    	break;
		    }
		    

		    
		    View rootView = inflater.inflate(tabLayout, container, false);
		    
		    if (position == 1)
		    	try {
		    	
		    		ScrollView sv = new ScrollView(getActivity());
		    		
		    		
		    		
		    		LinearLayout layout = new LinearLayout(getActivity());
		    		 
		            layout.setOrientation(LinearLayout.VERTICAL);
		            layout.setLayoutParams(new LinearLayout.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
		     
		     
		            TextView title = new TextView(getActivity());
		            LinearLayout.LayoutParams lll = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
		            lll.gravity = Gravity.CENTER_HORIZONTAL;
		            title.setLayoutParams(lll);
		            title.setText("Grade Summary");
		            title.setId(900);
		            title.setTextSize(30);
		            layout.addView(title);
		            
		            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
		            		LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		            
		            lp.setMargins(30, 20, 30, 0);
		            
		            LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
		            		LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		            lp1.setMargins(0, 0, 0, 20);
		            
		            
			    	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			    	JSONArray gradeSummary;
			    	{
			    		String source = sharedPrefs.getString("gradeSummary", "[]");
			    		gradeSummary = new JSONArray(source);
			    	}
		            
			    	classCount = gradeSummary.length();
			    	buttons = new Button[classCount];
			    	
		            for (int i = 0; i < classCount ; i++) {
		            	
		            	JSONObject course = gradeSummary.getJSONObject(i);
		            	
		            	String name = " " + course.getString("title");
		            	
		            	LinearLayout innerLayout = new LinearLayout(getActivity());
			            innerLayout.setOrientation(LinearLayout.HORIZONTAL);
			            
			            TextView className = new TextView(getActivity());
			            className.setText(name);
			            className.setTextSize(20);
			            className.setLayoutParams(
			            		new LinearLayout.LayoutParams(
			            				LinearLayout.LayoutParams.WRAP_CONTENT, 
			            				LinearLayout.LayoutParams.WRAP_CONTENT, 
			            				1f));
			            
			            innerLayout.addView(className);
			            
			            //Hard coded for first six weeks average.
			            JSONObject term = course.getJSONArray("terms").getJSONObject(0);
			            int average = term.optInt("average", -1);
			            
			            buttons[i] = new Button(getActivity());
			            buttons[i].setText(average + "");
			            buttons[i].setId(i);
			            buttons[i].setOnClickListener((View.OnClickListener)getActivity());
			            innerLayout.addView(buttons[i]);
			            if (i % 2 == 0)
			            	innerLayout.setBackgroundColor(getResources().getColor(R.color.light_blue));
			            else
			            	innerLayout.setBackgroundColor(Color.WHITE);
			            layout.addView(innerLayout, lp);
			            
		            }
		            
		            sv.addView(layout, lp1);
		            return sv;
		    	} catch (JSONException e) {
		    	e.printStackTrace();
		    }
		    return rootView;
		}

		
	}

	@Override
	public void onClick(View v) {
		System.out.println(v.getId());
		Intent intent = new Intent (this, ClassSwipe.class);
		intent.putExtra("classCount", classCount);
		intent.putExtra("classIndex", v.getId());
		startActivity(intent);
	}

}
