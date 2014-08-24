package com.dopamine.apptrack;

import com.dopamine.apptrack.activities.mainactivity.fragments.UserSettingsFragment;

import android.app.Activity;
import android.os.Bundle;

public class UserSettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new UserSettingsFragment())
				.commit();
	}

}
