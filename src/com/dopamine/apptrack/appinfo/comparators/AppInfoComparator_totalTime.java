package com.dopamine.apptrack.appinfo.comparators;

import java.util.Comparator;

import com.dopamine.apptrack.appinfo.AppInfo;

public class AppInfoComparator_totalTime implements Comparator<AppInfo>{
	@Override
	public int compare(AppInfo a, AppInfo b) {
		return - (int) (a.getTotalTime() - b.getTotalTime()); 	// negative for decreasing order
	}
}