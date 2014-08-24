package com.dopamine.apptrack.activities.mainactivity;

import java.util.Locale;

import com.dopamine.api.Dopamine;
import com.dopamine.apptrack.R;
import com.dopamine.apptrack.UserSettingsActivity;
import com.dopamine.apptrack.R.id;
import com.dopamine.apptrack.R.layout;
import com.dopamine.apptrack.R.menu;
import com.dopamine.apptrack.R.string;
import com.dopamine.apptrack.activities.appoverview.AppOverviewActivity;
import com.dopamine.apptrack.activities.mainactivity.fragments.AppInfoListFragment;
import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;
import com.dopamine.apptrack.service.NotificationAgent;
import com.dopamine.apptrack.service.TrackingService;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

	public static final String APP_PACKAGE_NAME = "com.dopamine.apptrack.APP_PACKAGE_NAME";

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

	// ///////
	// custom local variables
	// ///////
	public static final String applicationTitleName = "App Track";
	public static Context context;
	AppInfoListFragment appInfoListFragment;
	ComponentName trackingService;
	NotificationAgent notificationAgent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

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

		actionBar.setSelectedNavigationItem(1);

		// Start service
		if (!isServiceRunning(TrackingService.class)) {
			trackingService = startService(new Intent(this, TrackingService.class));
		}

		context = getBaseContext();
		
		notificationAgent = new NotificationAgent(this);
		
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, UserSettingsActivity.class);
			startActivityForResult(intent, 0);
			return true;
		} else if (id == R.id.sortByName || id == R.id.sortByTime) {
			appInfoListFragment.sortBy(id);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		loadSettings();
	}

	public boolean isServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void loadSettings() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Enable Tracking
		boolean enableTracking = sharedPreferences.getBoolean("enable_tracking", true);
		if (enableTracking == false) {
			stopService(new Intent(this, TrackingService.class));
		} else if (!isServiceRunning(TrackingService.class)) {
			trackingService = startService(new Intent(this, TrackingService.class));
		}

		// Enable Persistent Notification
		notificationAgent.updatePreferences(sharedPreferences);

	}
	
	public void showOverview(View view){
		Intent intent = new Intent(this, AppOverviewActivity.class);
		String appPackageName = (String) view.getTag();
		intent.putExtra( APP_PACKAGE_NAME, appPackageName );
		startActivity(intent);
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

			// return different views/fragments here
			if (position == 1) {
				if (appInfoListFragment == null)
					appInfoListFragment = new AppInfoListFragment();
				return appInfoListFragment;
			} else
				return PlaceholderFragment.newInstance(position + 1);
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

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);

			final Button reinforcementButton = (Button) rootView.findViewById(R.id.reinforecement_button);
			reinforcementButton.setOnClickListener(new View.OnClickListener() {
				int i = 0;

				public void onClick(View v) {
					String result = Dopamine.reinforce("reinforcedBehavior");
					
					
					reinforcementButton.setText("Reinforcement " + i++);
				}
			});
			
			final Button trackingButton = (Button) rootView.findViewById(R.id.tracking_button);
			trackingButton.setOnClickListener(new View.OnClickListener() {
				int i = 0;

				public void onClick(View v) {
					Dopamine.track("track button pushed");
					trackingButton.setText("Track " + i++);
				}
			});
			
			final TextView textView = (TextView) rootView.findViewById(R.id.reinforecement_message);
			textView.setText("Red=reward, Blue=feedback");

			return rootView;
		}
	}

}
