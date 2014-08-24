package com.dopamine.apptrack.activities.appoverview;

import java.util.Locale;

import com.dopamine.apptrack.R;
import com.dopamine.apptrack.R.id;
import com.dopamine.apptrack.R.layout;
import com.dopamine.apptrack.R.menu;
import com.dopamine.apptrack.R.string;
import com.dopamine.apptrack.activities.mainactivity.MainActivity;
import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;
import com.dopamine.apptrack.service.TriggerEstimator;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.GraphViewStyle;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class AppOverviewActivity extends ActionBarActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
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
		setContentView(R.layout.activity_app_overview);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_overview, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
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
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1, getIntent());
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";
		Intent intent;
		
		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber, Intent intent) {
			PlaceholderFragment fragment = new PlaceholderFragment(intent);
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment(Intent i) {
			intent = i;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_app_graph, container, false);
			
			String appPackageName = intent.getStringExtra(MainActivity.APP_PACKAGE_NAME);
			
			AppInfo app = null;
			AppInfoList list = AppInfoList.ReturnUsedAppsList(getActivity());
			for(AppInfo a : list){
				if(a.packageName.equals(appPackageName)){
					app = a;
					break;
				}
			}
					
			
			int[] singleAppBin = TriggerEstimator.generateBinData(app);
			GraphViewData[] singleAppData = new GraphViewData[singleAppBin.length];
			
			int[] allAppBin = TriggerEstimator.generateCompletePhoneBinData(getActivity());
			GraphViewData[] allAppData = new GraphViewData[allAppBin.length];
			
			for(int i = 0; i < singleAppBin.length; i++){
				singleAppData[i] = new GraphViewData(i, singleAppBin[i]);
				allAppData[i] = new GraphViewData(i, allAppBin[i]);
			}
			
			GraphViewSeriesStyle singleAppStyle = new GraphViewSeriesStyle(Color.GREEN, 1);
			GraphViewSeries singleAppUsage = new GraphViewSeries(app.getLabelName(), singleAppStyle, singleAppData);
			GraphViewSeriesStyle allAppStyle = new GraphViewSeriesStyle(Color.BLUE, 1);
			GraphViewSeries allAppUsage = new GraphViewSeries("All Apps", allAppStyle, allAppData);
			
			String title = "Daily Usage | " + app.getLabelName() + " - " + app.startTimes.size() + " opens total";
			GraphView graphView = new BarGraphView( getActivity(), title);
			
			graphView.addSeries(allAppUsage);
			graphView.addSeries(singleAppUsage);
			
			GraphViewStyle graphViewStyle = graphView.getGraphViewStyle();
			
//			graphViewStyle.setLegendSpacing(10);
			graphView.setShowLegend(true);
			graphView.setLegendAlign(LegendAlign.TOP);

			graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
				
				@Override
				public String formatLabel(double value, boolean isValueX) {
					if(isValueX){
						int hour = (int) (value%12);
						if(hour==0) hour=12;
						if(value/12 > 1)
							return hour + "pm";
						else
							return hour + "am";
					}
					else{
						return null;
					}
				}
				
				
			});
			
			graphView.setScrollable(true);
			graphView.setScalable(true);
			
			LinearLayout layout = (LinearLayout) rootView;
			layout.addView(graphView);
			
			return rootView;
		}
	}

}
