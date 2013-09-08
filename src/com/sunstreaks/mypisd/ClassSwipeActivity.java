package com.sunstreaks.mypisd;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

import com.sunstreaks.mypisd.net.DataGrabber;

@SuppressLint("ValidFragment")
public class ClassSwipeActivity extends FragmentActivity {
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
	static ViewPager mViewPager;
	

	static int received;
	static int classCount;
	
	static DataGrabber dg;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_class_swipe);
		
		received = getIntent().getExtras().getInt("classIndex");
		classCount = getIntent().getExtras().getInt("classCount");
		dg = getIntent().getParcelableExtra("DataGrabber");

		for (int i = 0; i < classCount; i++) {
			mFragments.add(new DescriptionFragment(getResources().getString(R.string.class_swipe_loading)));
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
			//return ( (DescriptionFragment)fragmentList.get(position) ).getClassName();
			return dg.getClassName(position);
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
		private String className;
		private String teacherName;
		private View rootView;
		
		public DescriptionFragment() {
		}
		
		public DescriptionFragment(String className) {
			this.className = className;
		}
		
		public void setClassName(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return className;
		}
		
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState)  {
			if (savedInstanceState != null)
				System.out.println("Why am i doing this a second time?");
			else
				System.out.println("Oncreateview first time");
			
			position = mViewPager.getCurrentItem();
			System.out.println("position = " + position);
			
			rootView = inflater.inflate(R.layout.class_description, container, false);
			
			if (this.teacherName == null)
				this.teacherName = getResources().getString(R.string.teacher_name);
			
			( (TextView) rootView.findViewById(R.id.teacher) ).setText(teacherName);
			if (this.mClassGrade == null) {
				mClassGradeTask = new ClassGradeTask();
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
