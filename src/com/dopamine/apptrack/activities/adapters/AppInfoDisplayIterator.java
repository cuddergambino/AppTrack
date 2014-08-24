package com.dopamine.apptrack.activities.adapters;

import java.util.Iterator;

import com.dopamine.apptrack.appinfo.AppInfo;

public class AppInfoDisplayIterator implements Iterator<AppInfo>{

	private final AppInfoDisplayAdapter appInfoDisplayAdapter;
	private int currentIndex = 0;
	
	public AppInfoDisplayIterator(AppInfoDisplayAdapter adapter){
		appInfoDisplayAdapter = adapter;
	}
	
	@Override
	public boolean hasNext() {
		return currentIndex < appInfoDisplayAdapter.getCount();
	}

	@Override
	public AppInfo next() {
		return appInfoDisplayAdapter.getItem(currentIndex++);
	}

	@Override
	public void remove() {
		appInfoDisplayAdapter.remove( appInfoDisplayAdapter.getItem(currentIndex) );
	}

}
