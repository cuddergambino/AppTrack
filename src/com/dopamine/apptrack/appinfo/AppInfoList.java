package com.dopamine.apptrack.appinfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.dopamine.apptrack.FileManager;

import android.content.Context;
import android.widget.Toast;

public class AppInfoList extends ArrayList<AppInfo>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final Context context;
	
	private static HashMap<String, String> ignoredApps;
	private static String[] ignoredAppNames = {"com.sec.android.app.launcher", "com.android.systemui", "Not tracking", ""};
	public Comparator<AppInfo> comparator;
	
	public static AppInfo addictionAppInfo;
	
	public AppInfoList(Context context) {
		this.context = context;
		
		ignoredApps = new HashMap<String, String>();
		for(String app : ignoredAppNames)
			ignoredApps.put(app, "");
	}
	
	public static AppInfoList ReturnUsedAppsList(Context c){
		AppInfoList l = new AppInfoList(c);
		List<AppInfo> list = FileManager.jsonlogTOlist(c);
		for(AppInfo app : list)
			l.add(app);
		return l;
	}
	
	public static AppInfo getAppInfo(Context c, String name){
		AppInfoList list = ReturnUsedAppsList(c);
		for(AppInfo app : list){
			if(app.getLabelName().equalsIgnoreCase(name) || app.packageName.equalsIgnoreCase(name))
				return app;
		}
		return null;
	}
	
	@Override
	public boolean add(AppInfo app){
		if( !isIgnored(app.packageName) ){
			app.context = context;
			super.add(app);
			return true;
		}
		else
			return false;
	}
	
	public static boolean isIgnored(String name){
		if(ignoredApps == null)
			for(String app : ignoredAppNames)
				ignoredApps.put(app, "");
		
		if(ignoredApps.containsKey(name))
			return true;
		else
			return false;
	}

}
