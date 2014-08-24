package com.dopamine.apptrack.activities.mainactivity.fragments;

import com.dopamine.apptrack.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class UserSettingsFragment extends PreferenceFragment{
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	  // TODO Auto-generated method stub
	  super.onCreate(savedInstanceState);
	  
	  // Load the preferences from an XML resource
	  addPreferencesFromResource(R.xml.preferences);
	  
	 }
	
}
