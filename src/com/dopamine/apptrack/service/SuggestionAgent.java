package com.dopamine.apptrack.service;

import android.content.Context;

public class SuggestionAgent {
	boolean active = false;
	
	// Method 1: notification to go for walk
	
	
	// Variables
	Context context;
	NotificationAgent notificationAgent;
	
	public SuggestionAgent(Context c){
		context = c;
		notificationAgent = new NotificationAgent(context);
//		notificationAgent.setSuggestionNotification("Suggestion agent active");
	}
	
	public void suggest(int option){
		active = true;
		
		switch(option){
		case 1:
			method1();
			break;

		default:
			break;
		}
	}
	
	public void remove(int option){
		active = false;
		
		switch(option){
		case 1:
			notificationAgent.removeSuggestionNotification();
			break;
			
		default:
			break;
		}
		
		
	}
	
	private void method1(){
		notificationAgent.setSuggestionNotification("Want to go for a walk?");
	}
	
	
	
	
	
	
	public boolean isActive(){
		return active;
	}
	
}
