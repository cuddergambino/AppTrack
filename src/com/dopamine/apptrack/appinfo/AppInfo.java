package com.dopamine.apptrack.appinfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.JsonReader;

public class AppInfo{
	public Context context;
	
	public static String[] jsonFieldNames = {"appName", "startTimes", "endTimes"};
	
	public String packageName;
	public List<Long> startTimes;
	public List<Long> endTimes;
	public static final long millisecondsPerDay = 1000*60*60*24;
	
	public AppInfo(String name, long start, long end){
		this.packageName = name;
		startTimes = new ArrayList<Long>();
		startTimes.add(start);
		endTimes = new ArrayList<Long>();
		endTimes.add(end);
	}
	
	public AppInfo(String packageName){
		this.packageName = packageName;
		startTimes = new ArrayList<Long>();
		endTimes = new ArrayList<Long>();
	}
	
	public AppInfo(JsonReader reader) throws IOException{
		startTimes = new ArrayList<Long>();
		endTimes = new ArrayList<Long>();
		
		reader.beginObject();
		while(reader.hasNext()){
			String fieldName = reader.nextName();
			if(fieldName.equals(jsonFieldNames[0])){
				packageName = reader.nextString();
			} else if(fieldName.equals(jsonFieldNames[1])){
				reader.beginArray();
				while(reader.hasNext()){startTimes.add( reader.nextLong() );}
				reader.endArray();
			} else if(fieldName.equals(jsonFieldNames[2])){
				reader.beginArray();
				while(reader.hasNext()){endTimes.add( reader.nextLong() );}
				reader.endArray();
			} else{
				reader.skipValue();
			}
		}
		reader.endObject();
	}
	
	@Override
	public String toString(){
		return packageName;
	}
	
	public String getLabelName(){
		try{
			PackageManager pm = context.getPackageManager();
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
			return pm.getApplicationLabel(ai).toString();
		} catch(Exception e){
			e.printStackTrace();
			return getLastName();
		}
	}
	
	public double getTotalTimeInMinutes(){
		long time = (long)0;
		for(int i = 0; i < endTimes.size(); i++){
			time += endTimes.get(i) - startTimes.get(i);
		}
		
		return milisecondsToMinutes(time);
	}
	
	public long getTotalTime(){
		long time = (long)0;
		for(int i = 0; i < endTimes.size(); i++){
			time += endTimes.get(i) - startTimes.get(i);
		}
		return time;
	}
	
	public String getLastName(){
		int beginning = packageName.lastIndexOf(".");
		String temp = packageName.substring(beginning+1);
		return Character.toUpperCase(temp.charAt(0)) + temp.substring(1);
	}
	
	
	////////////////////////////
	//
	// 	helper functions
	//
	////////////////////////////
	public static double milisecondsToMinutes(long l){
		double minutes = l/1000.0/60.0;
		return roundToTwoDecimalPlaces(minutes);
	}
	public static double roundToTwoDecimalPlaces(double d){
		int hundredTimes = (int) (d*100);
		return hundredTimes/100.0;
	}
}
