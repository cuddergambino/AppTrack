package com.dopamine.apptrack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.dopamine.apptrack.appinfo.AppInfo;
import com.dopamine.apptrack.appinfo.AppInfoList;

import android.content.Context;
import android.os.Environment;
import android.util.JsonReader;
import android.util.JsonWriter;

public class FileManager {

	public static String addictionName = "facebook";
	private static String logFilename = "log.data";
	private static String externalLogFilename = "chronotrackerLog.json";

	public static String getFileValue(String fileName, Context context) {

		try {
			StringBuffer outStringBuf = new StringBuffer();
			String inputLine = "";
			FileInputStream fIn = context.openFileInput(fileName);
			InputStreamReader isr = new InputStreamReader(fIn);
			BufferedReader inBuff = new BufferedReader(isr);
			while ((inputLine = inBuff.readLine()) != null) {
				outStringBuf.append(inputLine);
				outStringBuf.append("\n");
			}
			inBuff.close();
			return outStringBuf.toString();
		} catch (IOException e) {
			return null;
		}
	}

	public static boolean appendFileValue(String fileName, String value,
			Context context) {
		return writeToPrivateFile(fileName, value, context, Context.MODE_APPEND);
	}

	public static boolean setFileValue(String fileName, String value,
			Context context) {
		return writeToPrivateFile(fileName, value, context,
				Context.MODE_PRIVATE);
	}

	public static boolean writeToPrivateFile(String fileName, String value,
			Context context, int writeOrAppendMode) {
		try {
			FileOutputStream fOut = context.openFileOutput(fileName,
					writeOrAppendMode);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);

			osw.write(value);
			osw.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public static boolean writeToPublicFile(String absoluteFileName,
			String value, Context context) {
		try {
			File file = new File(absoluteFileName);
			FileOutputStream foStream = new FileOutputStream(file);
			OutputStreamWriter osWriter = new OutputStreamWriter(foStream);
			osWriter.write(value);
			osWriter.close();
		} catch (Exception e) {
			return false;
		}

		return true;

	}

	public static void deleteFile(String fileName, Context context) {
		context.deleteFile(fileName);
	}

	public static void appendToJSONlog(JSONObject jobj, Context context) {
		try {
			FileOutputStream fOut = context.openFileOutput(logFilename,
					Context.MODE_APPEND);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);

			JsonWriter writer = new JsonWriter(osw);

			writer.beginObject();
			writer.name(AppInfo.jsonFieldNames[0]).value(
					jobj.getString(AppInfo.jsonFieldNames[0]));
			writer.name(AppInfo.jsonFieldNames[1]).value(
					jobj.getLong(AppInfo.jsonFieldNames[1]));
			writer.name(AppInfo.jsonFieldNames[2]).value(
					jobj.getLong(AppInfo.jsonFieldNames[2]));
			writer.endObject();

			writer.close();

			System.out.println(getFileValue(logFilename, context));

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void overwriteJSONlog(AppInfoList list, Context context) {
		try {
			deleteFile(logFilename, context);

			FileOutputStream fOut = context.openFileOutput(logFilename,
					Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);

			JsonWriter writer = new JsonWriter(osw);
			writer.setIndent("  ");

			writer.beginArray();
			for (AppInfo appData : list) {
				// JSON AppData Object
				writer.beginObject();

				// name
				writer.name(AppInfo.jsonFieldNames[0]).value(
						appData.packageName);

				// start times
				writer.name(AppInfo.jsonFieldNames[1]);
				writer.beginArray();
				for (long l : appData.startTimes) {
					writer.value(l);
				}
				writer.endArray();

				// end times
				writer.name(AppInfo.jsonFieldNames[2]);
				writer.beginArray();
				for (long l : appData.endTimes) {
					writer.value(l);
				}
				writer.endArray();

				writer.endObject();
				// end JSON AppData Object
			}
			writer.endArray();

			writer.close();

			// System.out.println(getFileValue(logFilename, context));

			File externalFile = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
					externalLogFilename);
			writeToPublicFile(externalFile.getAbsolutePath(),
					getFileValue(logFilename, context), context);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static List<AppInfo> jsonlogTOlist(Context context) {
		// deleteFile(logFilename, context);
		// writeToPrivateFile(logFilename,
		// "[{\"appName\":\"com.example.chronotracker\",\"startTimes\":[1403563461733,1403563499145,1403563515471,1403563791458,1403563825820,1403641140499,1403646607148],\"endTimes\":[1403563465746,1403563503277,1403563519544,1403563795470,1403563829825,1403645448812,1403646611157]},{\"appName\":\"com.android.mms\",\"startTimes\":[1403563473791,1403563490937,1403563523593,1403563630003,1403570452098,1403572298021,1403573837925,1403576795693,1403582770294,1403587800872,1403592222735,1403633278423,1403634026152,1403634591090,1403634840854,1403637542901,1403638169748],\"endTimes\":[1403563485916,1403563497039,1403563585803,1403563632075,1403572295994,1403572414344,1403575222597,1403577350306,1403587794706,1403588050246,1403592271022,1403633754769,1403634440156,1403634756417,1403634977311,1403638123589,1403641128370]},{\"appName\":\"com.sec.android.app.clockpackage\",\"startTimes\":[1403572295994,1403591771230,1403591854477,1403625423137,1403625601847,1403626502145,1403629086318],\"endTimes\":[1403572298021,1403591781275,1403591862562,1403625453296,1403625612116,1403626560477,1403629094426]},{\"appName\":\"com.google.android.apps.maps\",\"startTimes\":[1403572416368,1403572575967],\"endTimes\":[1403572424415,1403573456992]},{\"appName\":\"com.android.contacts\",\"startTimes\":[1403572426441,1403572436525],\"endTimes\":[1403572432478,1403572573914]},{\"appName\":\"com.android.phone\",\"startTimes\":[1403572432478,1403577350306,1403579270127],\"endTimes\":[1403572436525,1403577687173,1403579821597]},{\"appName\":\"com.snapchat.android\",\"startTimes\":[1403573792346,1403576771608,1403638123589],\"endTimes\":[1403573837925,1403576795693,1403638169748]},{\"appName\":\"com.evernote\",\"startTimes\":[1403575224638,1403629102569,1403629776255,1403633759164,1403634442477,1403634758573,1403634979402,1403635255706],\"endTimes\":[1403576771608,1403629137996,1403633278423,1403634024094,1403634588960,1403634838803,1403635077848,1403637542901]},{\"appName\":\"com.facebook.katana\",\"startTimes\":[1403577695211,1403577850018,1403592122081],\"endTimes\":[1403577719307,1403578262792,1403592158197]},{\"appName\":\"com.sec.android.app.sbrowser\",\"startTimes\":[1403577719307,1403578262792,1403578355528,1403587794706,1403592158197,1403592271022],\"endTimes\":[1403577850018,1403578321134,1403578634876,1403587798756,1403592222735,1403592353540]},{\"appName\":\"com.android.settings\",\"startTimes\":[1403578321134,1403578345323,1403629137996],\"endTimes\":[1403578339190,1403578353474,1403629213642]},{\"appName\":\"com.whatsapp\",\"startTimes\":[1403578649784,1403579018517],\"endTimes\":[1403579008323,1403579270127]},{\"appName\":\"com.google.android.talk\",\"startTimes\":[1403579008323,1403581353873],\"endTimes\":[1403579016467,1403581468340]},{\"appName\":\"com.sec.android.app.music\",\"startTimes\":[1403629213642,1403635077848],\"endTimes\":[1403629774188,1403635255706]}]",
		// context, Context.MODE_PRIVATE);
		List<AppInfo> appdataArray = new ArrayList<AppInfo>();

		FileInputStream fIn;
		try {
			fIn = context.openFileInput(logFilename);
		} catch (FileNotFoundException e1) {
			File externalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), externalLogFilename);
			try {
				fIn = new FileInputStream(externalFile);
			} catch (FileNotFoundException e) {
				return appdataArray; // return empty list
			}
		}

		InputStreamReader isr = new InputStreamReader(fIn);
		JsonReader reader = new JsonReader(isr);

		try {
			reader.beginArray();
			while (reader.hasNext()) {
				appdataArray.add(new AppInfo(reader));
			}
			reader.endArray();

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<AppInfo>(); // return empty list
		}

		return appdataArray;
	}

}