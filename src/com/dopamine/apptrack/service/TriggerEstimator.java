package com.dopamine.apptrack.service;

import android.content.Context;

import com.dopamine.apptrack.activities.mainactivity.MainActivity;
import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;

import weka.estimators.KernelEstimator;


/*
 * 				  suggestion1       suggestion2
 * 				_______^_______   _______^_______
 * 			   |               | |				 |
 * 				bin1 bin2 bin3     bin4 bin5 bin6
 */

public class TriggerEstimator {
	
//	private final static int binPeriod = 5; // in minutes
	private final static int binPeriod = 60; // in minutes
	public static long millisecondsPerDay = 1000*60*60*24;
	private static long millisecondsPerBin = binPeriod*60*1000;
	public static int binCount = 24*60 / binPeriod;
	
	private int [] bin = new int[binCount];
	
	private final static int suggestionInterval = 60;	// in minutes
	private final static int binsPerSuggestionInterval = suggestionInterval/binPeriod;
	private final static int suggestionCount = 24*60 / suggestionInterval;
	
	private KernelEstimator[] suggestionKernels = new KernelEstimator[suggestionCount];
	private double[] triggerValue = new double[suggestionCount];
	
	public TriggerEstimator(AppInfo app){
		
		// Generate app usage bin data over 24 hour period
		bin = generateBinData(app);
		
		// Initialize kernel estimators with precision h=.01 
		for(int i = 0; i < suggestionCount; i++)
			suggestionKernels[i] = new KernelEstimator(.01);
		
		// Add bin values (bin number = data, bin value = weight) to suggestion kernel
		for(int i = 0; i < bin.length; i++){
			int suggestionKernel = i / binsPerSuggestionInterval;
			suggestionKernels[suggestionKernel].addValue(i, bin[i]);
		}
		
		// Create beginning trigger for app usage
		for(int i = 0; i < suggestionCount; i++){
			double average = 0;
			for(int j = i * binsPerSuggestionInterval; j < (i+1)*binsPerSuggestionInterval; j++){
				average+=bin[j];
			}
			average/=binsPerSuggestionInterval;
			
			triggerValue[i] = average * .33;
		}
		
		// debug suggestion times
		for(int i = 0; i < binCount; i++){
			int suggestionNumber = i / binsPerSuggestionInterval;
			if(suggestionKernels[suggestionNumber].getProbability(i) > triggerValue[suggestionNumber])
				System.out.println("Suggestion: " + suggestionNumber + "\tAddictionBin: " + i);
		}
	}
	
	public boolean happenedThisSuggestionInterval(){
		int binNumber = currentBinNumber();
		int suggestionNumber = currentSuggestionNumber();
		if( suggestionKernels[suggestionNumber].getProbability(binNumber) > triggerValue[suggestionNumber])
			return true;
		else
			return false;
	}
	
	public static int currentBinNumber(){
		long time = System.currentTimeMillis();
		
		return (int) ((time % millisecondsPerDay) / millisecondsPerBin);
	}
	
	public static int currentSuggestionNumber(){
		return currentBinNumber() / binsPerSuggestionInterval;
	}
	
	public static int[] generateBinData(AppInfo app){
		int bin[] = new int[binCount];
		
		for(int i = 0; i < app.startTimes.size(); i++){
			long start = app.startTimes.get(i);
			long end = app.endTimes.get(i);
			int day = (int) (start / millisecondsPerDay);
			int binNumber = (int) (( start % millisecondsPerDay ) / (millisecondsPerBin));
			
//			// merge hicups/multiple usages per bin into one bin
//			if(i < app.startTimes.size()-1){
//				long nextStart = app.startTimes.get(i+1);
//				int nextTimesDay = (int) (nextStart / millisecondsPerDay);
//				int nextTimesBinNumber = (int) (( nextStart % millisecondsPerDay ) / (millisecondsPerBin));
//				if(day==nextTimesDay && binNumber==nextTimesBinNumber)
//					continue;
//			}
//			
//			// count bins in between start and end
//			long duration = end%millisecondsPerDay - binNumber*millisecondsPerBin;
//			while(duration > millisecondsPerBin){
//				binNumber++;
//				if(binNumber>bin.length) binNumber%=bin.length;
//				bin[binNumber]++;
//				duration -= millisecondsPerBin;
//			}
			
			bin[binNumber]++;
			
		}
		
		return bin;
	}
	
	public static int[] generateCompletePhoneBinData(Context c){
		AppInfoList list = AppInfoList.ReturnUsedAppsList(c);
		int bin[] = new int[binCount];
		
		for(AppInfo app : list){
			for(int i = 0; i < app.startTimes.size(); i++){
				long start = app.startTimes.get(i);
				long end = app.endTimes.get(i);
				int day = (int) (start / millisecondsPerDay);
				int binNumber = (int) (( start % millisecondsPerDay ) / (millisecondsPerBin));
				bin[binNumber]++;
			}
		}
		
		return bin;
	}
}
