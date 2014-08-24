package com.dopamine.apptrack.service;

import java.io.IOException;

import com.dopamine.api.Dopamine;
import com.dopamine.apptrack.FileManager;
import com.dopamine.apptrack.R;
import com.dopamine.apptrack.activities.mainactivity.MainActivity;
import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class TrackingService extends Service {
	
	// variables
	private static CurrentAppReader activityReader = null;
	private static NotificationAgent notificationAgent = null;
	
	private static AppInfo addictionApp;
	public static final String addictionAppName = "facebook";
	
	String replacementAppPackageName = "com.mailboxapp";
	
	AppInfoList appInfoList;
	
	
	public TrackingService() {
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Starting Service", Toast.LENGTH_SHORT).show();
		notificationAgent = new NotificationAgent(this);
		appInfoList = AppInfoList.ReturnUsedAppsList(getApplicationContext());
		
		if(activityReader == null){
			activityReader = new CurrentAppReader(this);
			activityReader.start();
		}
		
		configureDopamine();
		
		return START_STICKY;
	}
	
	private void configureDopamine(){

		// signed up info on akashdes@usc.edu
		//		Dopamine.setIdentity("email", "akashdes@usc.edu");	// can use custom Identity or if none is 
		//		Dopamine.setIdentity("mac", "AB:CD:EF:GH:IJ");		// specified one will be automatically generated
		Dopamine.setAppID("53bf3dfbf572f3b63ee628de");
		Dopamine.setToken("493245694786310253bf3dfbf572f3b63ee628de");
		Dopamine.setKey("db07887eec605bff3a9ae5ae5374152ced642ed5");
		Dopamine.setVersionID("apptrack");


		Dopamine.addRewardFunctions("rewardFunction");
		Dopamine.addFeedbackFunctions("feedbackFunction");

		try {
			Dopamine.init(this.getApplicationContext());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		FileManager.overwriteJSONlog(appInfoList, this.getApplicationContext());
		Toast.makeText(this, "Stopped Service", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		//throw new UnsupportedOperationException("Not yet implemented");
		return null;
	}
	
	/////////////////////////////
	//
	//	App Tracking Functions
	//
	/////////////////////////////
	
	public void handleAppChange(long time1, String previousAppPkgName, long time2, String currentAppPkgName){
		// Persistent Notification
		notificationAgent.updatePersistentNotification(currentAppPkgName);
		// Log app usage time
		addStartAndEndTime(previousAppPkgName, time1, time2);
		
		if( !AppInfoList.isIgnored(currentAppPkgName) ){
			// track app change on UseDopamine
			Dopamine.addPersistentMetaData("appPackageName", currentAppPkgName);
			Dopamine.track("app change");
			Dopamine.clearPersistentMetaData("appPackageName");

			// reinforce if Replacement App is opened
			if(currentAppPkgName.equalsIgnoreCase(replacementAppPackageName)){
				String result = Dopamine.reinforce("ReplacementAppOpened");
				if(result.equals("feedbackFunction"))
					notificationAgent.feedbackFunction();
				else if(result.equals("rewardFunction"))
					notificationAgent.rewardFunction();
			}
		}
	}
	
	private void addStartAndEndTime(String appPackageName, long startTime, long endTime){
		if( AppInfoList.isIgnored(appPackageName) )
			return;
		
			// if data for the app already exists, add start and end times
		boolean hasHistory = false;
		for(AppInfo appData : appInfoList){
			if(appData.packageName.equals(appPackageName)){
				hasHistory = true;
				appData.startTimes.add(startTime);
				appData.endTimes.add(endTime);
				break;
			}
		}

			// if data for the app doesn't exist, create a new JSON object and add it to the list
		if( !hasHistory){
			AppInfo appData = new AppInfo(appPackageName, startTime, endTime);
			appInfoList.add(appData);
		}
		
		// write updated list to file
		FileManager.overwriteJSONlog(appInfoList, this.getApplicationContext());
	}
	
	public static AppInfo getAddictionApp(Context c){
		if(addictionApp == null)
			addictionApp = AppInfoList.getAppInfo(c, TrackingService.addictionAppName);
		
		return addictionApp;
	}
	
}
