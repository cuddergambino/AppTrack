package com.dopamine.apptrack.activities.mainactivity.fragments;

import com.dopamine.apptrack.activities.adapters.AppInfoDisplayAdapter;
import com.dopamine.apptrack.appinfo.AppInfoList;
import com.dopamine.apptrack.appinfo.comparators.AppInfoComparator_alphabetical;
import com.dopamine.apptrack.appinfo.comparators.AppInfoComparator_totalTime;
import com.dopamine.apptrack.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AppInfoListFragment extends ListFragment{
	
	public AppInfoDisplayAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Context c = getActivity();
		adapter = new AppInfoDisplayAdapter(AppInfoList.ReturnUsedAppsList(c), c);
		adapter.sort();
		setListAdapter(adapter);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View rootView = inflater.inflate(R.layout.list_fragment, container, false);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		adapter.update( AppInfoList.ReturnUsedAppsList(getActivity()) );
		adapter.sort();
	}
	
	public void sortBy(int id){
		if(id == R.id.sortByName)
			adapter.comparator = new AppInfoComparator_alphabetical();
		else if(id == R.id.sortByTime)
			adapter.comparator = new AppInfoComparator_totalTime();
		adapter.sort();
	}

}