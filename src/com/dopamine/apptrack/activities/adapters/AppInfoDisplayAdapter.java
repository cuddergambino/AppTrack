package com.dopamine.apptrack.activities.adapters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.dopamine.apptrack.R;
import com.dopamine.apptrack.activities.mainactivity.MainActivity;
import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;
import com.dopamine.apptrack.appinfo.comparators.AppInfoComparator_totalTime;
import com.dopamine.apptrack.service.TrackingService;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppInfoDisplayAdapter extends ArrayAdapter<AppInfo> implements Iterable<AppInfo>{
	
	private final Context context;
	private final static int appNameFontSize = 18;

	public Comparator<AppInfo> comparator;
	
	public AppInfoDisplayAdapter(AppInfoList ail, Context context) {
		super(context, android.R.id.list);
		this.context = context;
		
		for(AppInfo app : ail)
			this.add(app);
	}
	
	public void update(AppInfoList ail){
		this.clear();
		for(AppInfo app : ail)
			this.add(app);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		AppInfo appInfo = this.getItem(position);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View rowView = inflater.inflate(R.layout.app_info_row_layout, parent, false);
		if(convertView == null){
			convertView = (View) inflater.inflate(R.layout.app_info_row_layout, null, false);
		}
		
		// Set tag
		LinearLayout row = (LinearLayout) convertView.findViewById(R.id.appInfoRow);
		row.setTag(appInfo.packageName);
		
		// Different settings if it's the addiction app
		boolean addictionRow = false;
		if( appInfo.getLabelName().equalsIgnoreCase(TrackingService.addictionAppName) )
			addictionRow = true;
		
		
		// App Icon
		ImageView appIcon = (ImageView) convertView.findViewById(R.id.icon_image);
		try {
			Drawable icon = context.getPackageManager().getApplicationIcon(appInfo.packageName);
			appIcon.setImageDrawable(icon);
		} catch (NameNotFoundException e) {
			appIcon.setImageDrawable(null);
		}
		
		// App Name
		TextView appName = (TextView) convertView.findViewById(R.id.appName);
		appName.setText( appInfo.getLabelName() );
		if( addictionRow ){
			appName.setTextColor(Color.BLACK);
			appName.setTextSize(appNameFontSize + 4);
		}
		else{
			appName.setTextColor(Color.GRAY);
			appName.setTextSize(appNameFontSize);
		}
		
		// Total Time app is used
		TextView totalTime = (TextView) convertView.findViewById(R.id.totalTime);
		totalTime.setText( appInfo.getTotalTimeInMinutes() + " minutes" );
		if( addictionRow ){
			totalTime.setTextColor(Color.BLACK);
		}
		else{
			totalTime.setTextColor(Color.GRAY);
		}
		
		// Number of times App is opened
		TextView openedCount = (TextView) convertView.findViewById(R.id.openedCount);
		long todaysStart = System.currentTimeMillis();
		todaysStart = todaysStart - (todaysStart%AppInfo.millisecondsPerDay);
		int count = 0;
		for(int i = appInfo.startTimes.size()-1; i>=0; i--){
			if(appInfo.startTimes.get(i) > todaysStart)
				count++;
			else
				break;
		}
		if(count == 0)
			openedCount.setText("");
		else
			openedCount.setText(count + (count==1 ? " open" : " opens") + " today");
		
		if( addictionRow ){
			openedCount.setTextColor(Color.BLACK);
		}
		else{
			openedCount.setTextColor(Color.GRAY);
		}
		
		
		return convertView;
	}
	
	@Override
	public Iterator<AppInfo> iterator() {
		return new AppInfoDisplayIterator(this);
	}
	
	public void sort(){
		if(comparator == null)
			comparator = new AppInfoComparator_totalTime();
		this.sort(comparator);
	}
	
	public void printToSystemOut(){
		// print info to screen
		List<AppInfo> list = new ArrayList<AppInfo>();
		for(int i = 0; i < this.getCount(); i ++){
			list.add(getItem(i));
		}
		
		System.out.println("======================================================");
		System.out.printf("%-40s %-10s %-10s\n", "App name", "Minutes", "Start-End");
		for(AppInfo node : list){
			System.out.printf("%-40s %-10s", node.packageName, ""+node.getTotalTimeInMinutes());
			for(int i = node.endTimes.size()-1; i >= node.endTimes.size()-3 && i >= 0; i--){
				System.out.print(node.startTimes.get(i) + "-" + node.endTimes.get(i) + "       ");
			}
			System.out.println();
		}
	}
	
}
