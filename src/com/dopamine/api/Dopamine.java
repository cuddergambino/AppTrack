package com.dopamine.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.audiofx.BassBoost.Settings;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Created by pradeepbk4u on 4/18/14.
 */
public class Dopamine {
	public static volatile String resultFunction, result;
	public static JSONArray arguments = new JSONArray();
	private static Context context;
	
	// Data objects
	public static String appID, key, token, versionID, build;
	private static ArrayList<String> rewardFunctions, feedbackFunctions;
	private static ArrayList<SimpleEntry<String, Object>> identity;
	private static ArrayList<SimpleEntry<String, Object>> metaData, persistentMetaData;

	private Dopamine() {
		
	}

	public static void init(Context c) throws IOException{
		context = c;
		if(identity == null){
			identity = new ArrayList<SimpleEntry<String,Object>>();
		}
		identity.add( new SimpleEntry<String, Object>("DEVICE_ID", getDeviceID()) );
		
		setBuild();
		
		URIBuilder uri = new URIBuilder(appID);
		DopamineRequest initRequest = new DopamineRequest();
		try {
			initRequest.execute(getInitRequest(), uri.getURI(URIBuilder.URI.INIT));
			initRequest.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		if(initRequest.error !=null || initRequest.error != "")
			throw new IOException(initRequest.error);
		
		
	}

	public static String reinforce(String eventName) {
		URIBuilder uri = new URIBuilder(appID);
		DopamineRequest dr = new DopamineRequest();
		try {
			dr.execute(getReinforceRequest(eventName), uri.getURI(URIBuilder.URI.REWARD));
			dr.get();
			resultFunction = dr.resultFunction;
			arguments = dr.arguments;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
		return resultFunction;
	}
	
	public static void track(String eventName) {
		URIBuilder uri = new URIBuilder(appID);
		new DopamineRequest().execute(getTrackRequest(eventName), uri.getURI(URIBuilder.URI.TRACK));
	}

	// ///////////////////////////////////
	//
	// Request functions
	//
	// ///////////////////////////////////

	private static JSONObject getBaseRequest() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("key", key);
			jsonObject.put("token", token);
			jsonObject.put("versionID", versionID);
			jsonObject.put("identity", simpleEntryListToJSONArray(identity));
			jsonObject.put("build", build);
			
			long utcTime = System.currentTimeMillis();
			long localTime = utcTime + TimeZone.getDefault().getOffset(utcTime);
			jsonObject.put("UTC", utcTime/1000);
			jsonObject.put("localTime", localTime/1000);

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return jsonObject;
	}

	private static String getInitRequest() {
		JSONObject jsonObject = getBaseRequest();

		if (jsonObject != null) {
			try {
				jsonObject.put("rewardFunctions", listToJSONArray(rewardFunctions));
				jsonObject.put("feedbackFunctions", listToJSONArray(feedbackFunctions));

			} catch (JSONException e) {
				e.printStackTrace();
				return "0";
			}
		} else {
			// Error
		}

		System.out.println(jsonObject.toString());
		return jsonObject.toString();
	}

	private static String getTrackRequest(String eventName) {
		JSONObject jsonObject = getBaseRequest();

		if (jsonObject != null) {
			try {
				jsonObject.put("eventName", eventName);
				jsonObject.put("metaData", simpleEntryListToJSONArray(metaData));
				jsonObject.accumulate("metaData", simpleEntryListToJSONArray(persistentMetaData));
				
				if(metaData != null) metaData.clear();

				System.out.println("Tracking JSON:\n" + jsonObject);
			} catch (JSONException e) {
				e.printStackTrace();
				return "0";
			}
		} else {
			// Error
		}

		return jsonObject.toString();
	}

	private static String getReinforceRequest(String eventName) {
		JSONObject jsonObject = getBaseRequest();

		if (jsonObject != null) {
			try {
				jsonObject.put("eventName", eventName);
				jsonObject.put("metaData", simpleEntryListToJSONArray(metaData));
				jsonObject.accumulate("metaData", simpleEntryListToJSONArray(persistentMetaData));
				
				if(metaData != null) metaData.clear();
				
			} catch (JSONException e) {
				e.printStackTrace();
				return "0";
			}
		} else {
			// Error
		}

		return jsonObject.toString();
	}

	// Request helper functions
	// //////////////////////////
	private static JSONArray listToJSONArray(ArrayList<String> list) {
		JSONArray array = new JSONArray();
		for (String s : list)
			array.put(s);
		return array;
	}

	private static JSONArray simpleEntryListToJSONArray(ArrayList<SimpleEntry<String, Object>> list) {
		// create JSONObject to combine entries with identical keys into a single JSONObject inside of the JSONArray
		JSONObject obj = new JSONObject();
		try {

			for (SimpleEntry<String, Object> entry : list) {
				obj.accumulate(entry.getKey(), entry.getValue());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		JSONArray array = new JSONArray();
		Iterator<String> it = obj.keys();
		while (it.hasNext()) {
			try {
				String[] name = { it.next() };
				// create individual objects for each metadata key
				array.put(new JSONObject(obj, name));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return array;
	}

	//////////////////////////////////////
	//
	// Setter functions
	//
	//////////////////////////////////////

	public static void setAppID(String id) {
		appID = id;
	}

	public static void setKey(String key) {
		Dopamine.key = key;
	}

	public static void setToken(String token) {
		Dopamine.token = token;
	}

	public static void setVersionID(String id) {
		versionID = id;
	}
	
	public static void setConfig(String appID, String key, String token, String versionID){
		setAppID(appID);
		setKey(key);
		setToken(token);
		setVersionID(versionID);
	}

	private static String setBuild() {
		StringBuilder builder = new StringBuilder();
		
		Collections.sort(rewardFunctions);
		Collections.sort(feedbackFunctions);
		
		for (String reward : rewardFunctions) {
			builder.append(reward);
		}

		for (String feedback : feedbackFunctions) {
			builder.append(feedback);
		}

		build = sha1(builder.toString());
		return build;
	}
	public static String sha1(String s) {
		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		digest.reset();
		byte[] data = digest.digest(s.getBytes());
		return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,
				data));
	}

	public static void addRewardFunctions(String... names) {
		if (rewardFunctions == null)
			rewardFunctions = new ArrayList<String>();

		for(String name : names)
			rewardFunctions.add(name);
	}

	public static void addFeedbackFunctions(String... names) {
		if (feedbackFunctions == null)
			feedbackFunctions = new ArrayList<String>();

		for(String name : names)
			feedbackFunctions.add(name);
	}

	public static void setIdentity(String IDType, String uniqueID) {
		if (identity == null)
			identity = new ArrayList<SimpleEntry<String, Object>>();

		identity.add(new SimpleEntry<String, Object>(IDType, uniqueID));
	}
	public static void clearIdentity(String IDType){
		if(identity == null)
			return;

		for(int i = 0; i < identity.size(); i++){
			SimpleEntry< String, Object> entry = identity.get(i);
			if( entry.getKey().equalsIgnoreCase(IDType) ){
				identity.remove(i);
				return;
			}
		}

	}

	public static void addMetaData(String key, Object value) {
		if (metaData == null)
			metaData = new ArrayList<SimpleEntry<String, Object>>();

		metaData.add(new SimpleEntry<String, Object>(key, value));
	}

	public static void addPersistentMetaData(String key, Object value) {
		if (persistentMetaData == null)
			persistentMetaData = new ArrayList<SimpleEntry<String, Object>>();

		persistentMetaData.add(new SimpleEntry<String, Object>(key, value));
	}
	
	public static void clearPersistentMetaData(String key){
		if (persistentMetaData == null)
			return;
		
		for(int i = 0; i < persistentMetaData.size(); i++){
			SimpleEntry<String, Object> entry = persistentMetaData.get(i);
			if( entry.getKey().equalsIgnoreCase(key)){
				persistentMetaData.remove(i);
				return;
			}
		}
	}
	
	
	//////////////////////////////////////
	//
	// Setter Helper functions
	//
	//////////////////////////////////////
	
	
	public static String getDeviceID() {

		/*String Return_DeviceID = USERNAME_and_PASSWORD.getString(DeviceID_key,"Guest");
		return Return_DeviceID;*/

		TelephonyManager TelephonyMgr = (TelephonyManager) context.getApplicationContext().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
		String m_szImei = TelephonyMgr.getDeviceId(); // Requires
		// READ_PHONE_STATE

		// 2 compute DEVICE ID
		String m_szDevIDShort = "35"
				+ // we make this look like a valid IMEI
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10
				+ Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
				+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10
				+ Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
				+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10
				+ Build.TAGS.length() % 10 + Build.TYPE.length() % 10
				+ Build.USER.length() % 10; // 13 digits
		// 3 android ID - unreliable
		String m_szAndroidID = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
		// 4 wifi manager, read MAC address - requires
		// android.permission.ACCESS_WIFI_STATE or comes as null
		WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
		// 5 Bluetooth MAC address android.permission.BLUETOOTH required
		BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
		m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		String m_szBTMAC = m_BluetoothAdapter.getAddress();
		System.out.println("m_szBTMAC "+m_szBTMAC);

		// 6 SUM THE IDs
		String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID+ m_szWLANMAC + m_szBTMAC;
		System.out.println("m_szLongID "+m_szLongID);
		MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		m.update(m_szLongID.getBytes(), 0, m_szLongID.length());
		byte p_md5Data[] = m.digest();

		String m_szUniqueID = new String();
		for (int i = 0; i < p_md5Data.length; i++) {
			int b = (0xFF & p_md5Data[i]);
			// if it is a single digit, make sure it have 0 in front (proper
			// padding)
			if (b <= 0xF)
				m_szUniqueID += "0";
			// add number to string
			m_szUniqueID += Integer.toHexString(b);
		}
		m_szUniqueID = m_szUniqueID.toUpperCase();

		return m_szUniqueID;

	}
}
