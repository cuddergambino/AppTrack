package com.dopamine.apptrack.appinfo.comparators;

import java.util.Comparator;

import com.dopamine.apptrack.appinfo.AppInfo;

public class AppInfoComparator_alphabetical implements Comparator<AppInfo>{
	@Override
	public int compare(AppInfo a, AppInfo b) {
		return a.getLabelName().compareTo( b.getLabelName() );
	}
}