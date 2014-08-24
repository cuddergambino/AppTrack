package com.dopamine.apptrack.service;

import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.json.JSONObject;

import com.dopamine.apptrack.activities.mainactivity.MainActivity;
import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.PowerManager;

public class CurrentAppReader extends Thread{
	Vector<JSONObject> vector = new Vector<JSONObject>() ;
	ActivityManager activityManager;
	PowerManager powerManager;
	KeyguardManager keyguardManager;
	TrackingService trackingService;
	TriggerEstimator trigger;
	SuggestionAgent suggestionAgent;
	
	String currentApp, previousApp;
	long timeMarker1, timeMarker2;
	int currentSuggestionNumber, previousSuggestionNumber;
	boolean shouldTrack = true;
	boolean shouldSuggest = true;
	private final int waitTime = 1;		// in seconds

	public CurrentAppReader(TrackingService parentService){
		
		trackingService = parentService;
		activityManager  =  (ActivityManager) trackingService.getSystemService(Context.ACTIVITY_SERVICE);
		powerManager = (PowerManager) trackingService.getSystemService(Context.POWER_SERVICE);
		keyguardManager = (KeyguardManager) trackingService.getSystemService(Context.KEYGUARD_SERVICE);
		
//		AppInfoList list = AppInfoList.ReturnUsedAppsList(parentService);
//		AppInfo mostUsedApp = list.get(0);
//		for(AppInfo app : list){
//			if(app.getTotalTime() > mostUsedApp.getTotalTime())
//				mostUsedApp = app;
//		}
		
		suggestionAgent = new SuggestionAgent(parentService);
	}

	public void run(){
		previousApp = "";
		currentApp = getCurrentApp3();
		timeMarker1 = timeMarker2 = getCurrentTime();
		trigger = new TriggerEstimator(TrackingService.getAddictionApp(trackingService));
		
		while(true){
			try{
				
				if(shouldTrack){
					
					// App --> another app
					String foreground = getCurrentApp3();
					if( ! (foreground.equals("com.android.systemui") || currentApp.equals(foreground) )){	// skip systemui because it's used to switch between apps
						previousApp = currentApp;
						currentApp = foreground;
						timeMarker1 = timeMarker2;
						timeMarker2 = getCurrentTime();
						trackingService.handleAppChange(timeMarker1, previousApp, timeMarker2, currentApp);
					}


					// App --> turned screen off

					if( !powerManager.isScreenOn() ){
						previousApp = currentApp;
						currentApp = "";
						timeMarker1 = timeMarker2;
						timeMarker2 = getCurrentTime();
						trackingService.handleAppChange(timeMarker1, previousApp, timeMarker2, currentApp);
						
						shouldTrack = false;
					}
				}
				
				else{
					// off screen --> lock screen

					// lock screen --> app
					if( powerManager.isScreenOn() && !keyguardManager.inKeyguardRestrictedInputMode()){
						shouldTrack = true;
						
						previousApp = "";
						currentApp = getCurrentApp3();
						timeMarker1 = timeMarker2;
						timeMarker2 = getCurrentTime();
						trackingService.handleAppChange(timeMarker1, previousApp, timeMarker2, currentApp);
					}
				}
				
				
				// detect if trigger has been pulled, and suggest action
				if(shouldSuggest){
					
					int suggestionType = 1;
					previousSuggestionNumber = currentSuggestionNumber;
					currentSuggestionNumber = TriggerEstimator.currentSuggestionNumber();

					// reset trigger if inside new suggestion interval
					if(currentSuggestionNumber != previousSuggestionNumber){
						suggestionAgent.remove(suggestionType);
					}

					if( trigger.happenedThisSuggestionInterval() && !suggestionAgent.isActive()){
						suggestionAgent.suggest(suggestionType);
					}

				}


				sleep(1000 * waitTime);
			} catch(Exception e){
				e.printStackTrace();
			}

		}
	}

	// http://stackoverflow.com/questions/8061179/broadcast-receiver-to-detect-application-start/8061297#8061297
	private String getCurrentApp1() {
		// using getRunningAppProcessese
		
		List <RunningAppProcessInfo> l = activityManager.getRunningAppProcesses();
		for(RunningAppProcessInfo appInfo : l){
			if(appInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
				return appInfo.processName;
			}
		}
		
		return "";
	}
	
	private String getCurrentApp2() {
		// using getRunningTasks() and topActivity
		
		RunningTaskInfo foregroundTaskInfo = activityManager.getRunningTasks(1).get(0);
		
		String packageName = foregroundTaskInfo.topActivity.getPackageName();
		PackageManager pm = trackingService.getPackageManager();
		try {
			PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
			return packageInfo.applicationInfo.loadLabel(pm).toString();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private String getCurrentApp3(){
		// using getRunningTasks() and baseActivity
		
		List<RunningTaskInfo> services  =  activityManager.getRunningTasks(1);
		RunningTaskInfo topApp = services.get(0);
		return topApp.baseActivity.getPackageName();
	}

	private long getCurrentTime(){
		long time = System.currentTimeMillis();
		TimeZone timeZone = TimeZone.getDefault();
		time += timeZone.getOffset(time);
		return time;
	}

}


