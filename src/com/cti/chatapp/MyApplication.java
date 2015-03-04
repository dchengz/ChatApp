package com.cti.chatapp;

import java.io.IOException;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	formUri = "http://contacto123.sytes.net/david/trackerapp/ajax/error_log.php",
	mode = ReportingInteractionMode.TOAST,
	httpMethod = org.acra.sender.HttpSender.Method.POST,
	customReportContent = { ReportField.PACKAGE_NAME, ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.BRAND,
			ReportField.STACK_TRACE, ReportField.LOGCAT },
	resToastText = R.string.crash_toast_text
)

public class MyApplication extends Application {
	String url;
	private static boolean activityVisible;
	TelephonyManager tm;
	private static String immei; 

	@Override
	public void onCreate() {
		super.onCreate();
		tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		ACRA.init(this);
		ACRA.getErrorReporter().putCustomData("IMEI", tm.getDeviceId());
		activityVisible=false;
		immei = tm.getDeviceId();
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}
	public static void activityResumed(final Context contxt) {
		activityVisible = true;
//		new AsyncTask<String, Void, String>() {
//			@Override
//			protected String doInBackground(String... params) {
//				String status = null;
//				try {
//					status=AppMethods.cons_chat(contxt, params[0], params[1], "1");
//				} catch (IllegalStateException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				return status;
//			}
//			@Override
//			protected void onPostExecute(String result) {				
//			}
//		}.execute(immei,"chat_set");
	}
	public static void activityPaused(final Context contxt) {
		activityVisible = false;
//		new AsyncTask<String, Void, String>() {
//			@Override
//			protected String doInBackground(String... params) {
//				String status = null;
//				try {
//					status=AppMethods.cons_chat(contxt, params[0], params[1], "0");
//				} catch (IllegalStateException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				return status;
//			}
//			@Override
//			protected void onPostExecute(String result) {				
//			}
//		}.execute(immei,"chat_set");
	}
}