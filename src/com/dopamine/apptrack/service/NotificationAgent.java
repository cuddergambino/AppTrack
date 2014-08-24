package com.dopamine.apptrack.service;

import com.dopamine.apptrack.FileManager;
import com.dopamine.apptrack.R;
import com.dopamine.apptrack.activities.mainactivity.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

public class NotificationAgent {
//	private Context context = MainActivity.context;
	private Context context;
	private static NotificationManager mNotifyMgr = null;
	private static NotificationCompat.Builder persistentNotificationBuilder = null;
	
	// All possible notifications
	private final static int persistentNotificationID = 999;
	private final static int suggestionNotificationID = 998;
	private final static int feedbackNotificationID = 2;
	private final static int rewardNotificationID = 2;		// purposefully same as feedback. have 1 single feedback handle
	
	// Settings
	private static boolean persistentNotificationEnabled = true;
	
	public NotificationAgent(Context c){
		context = c;
		
		// Gets an instance of the NotificationManager service
		if(mNotifyMgr == null){
			mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		
		if(persistentNotificationBuilder == null)
			persistentNotificationBuilder = newNotificationBuilder("", true);
		
	}
	
	public void updatePersistentNotification(String text){
		persistentNotificationBuilder.setContentText(text);
		
		// Builds the notification and issues it.
		if( persistentNotificationEnabled )
			mNotifyMgr.notify(persistentNotificationID, persistentNotificationBuilder.build());
//		else														// unnecessary
//			mNotifyMgr.cancel(persistentNotificationID);
	}
	
	public void updatePreferences(SharedPreferences sharedPreferences){

		// Persistent Notification
		persistentNotificationEnabled = sharedPreferences.getBoolean("persistent_notification", true);

		// causes crash since setting can be changed if service and thus mNotifyMgr hasn't been initialized
		if (!persistentNotificationEnabled)
			mNotifyMgr.cancel(persistentNotificationID);
		else{
			mNotifyMgr.notify(persistentNotificationID, persistentNotificationBuilder.build());
		}
	}
	
	private NotificationCompat.Builder newNotificationBuilder(String message, boolean opensApp){
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
													.setSmallIcon(R.drawable.ic_launcher)
													.setContentTitle(MainActivity.applicationTitleName)
													.setContentText(message);
		
		if(opensApp){
			Intent intent = new Intent(context, MainActivity.class);
			mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		}
		
		return mBuilder;
	}
	
	private NotificationCompat.Builder newNotificationBuilder(String message, String tickerMessage, boolean opensApp){
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
													.setSmallIcon(R.drawable.ic_launcher)
													.setContentTitle(MainActivity.applicationTitleName)
													.setContentText(message)
													.setTicker(tickerMessage);
		
		if(opensApp){
			Intent intent = new Intent(context, MainActivity.class);
			mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		}
		
		return mBuilder;
	}
	
	
	/////////////////////////////
	//
	//	Public Interactive Functions
	//
	/////////////////////////////

	
	public void rewardFunction(){
		String str = "You fought your addiction! +10 points!!!";
		NotificationCompat.Builder mBuilder = newNotificationBuilder(str, str, false);
		mNotifyMgr.notify(rewardNotificationID, mBuilder.build());
	}
	
	public void feedbackFunction(){
		String str = "You overcame your addiction in a difficult time";
		NotificationCompat.Builder mBuilder = newNotificationBuilder(str, str, false);
		mNotifyMgr.notify(feedbackNotificationID, mBuilder.build());
	}
	
	public void setSuggestionNotification(String message){
		NotificationCompat.Builder mBuilder = newNotificationBuilder(message, message, false);
		mNotifyMgr.notify(suggestionNotificationID, mBuilder.build());
	}

	public void removeSuggestionNotification() {
		mNotifyMgr.cancel(suggestionNotificationID);
	}
	
	
}
