package com.cti.chatapp.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.widget.Spinner;

public class GcmMethods {
	public static void confirmNotif(Context context, String imei, String id, int val){
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
//		HttpPost post = new HttpPost(MySSLSocketFactory.URLupdMess);
		System.out.println("GCMnotif");
		HttpPost post = new HttpPost("http://contacto123.sytes.net/david/trackerapp/gcm/notif_stat.php");
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("imei", imei));
        pairs.add(new BasicNameValuePair("idmsg", id));
        if (val==0) {
        	pairs.add(new BasicNameValuePair("action", "rec"));
		} else {
			pairs.add(new BasicNameValuePair("action", "deliv"));
		}
        try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
			client.execute(post);
		} catch (IOException e) {
//				e.printStackTrace();
		}
	}
	public static void sendGcmNotification(JSONArray arrayString, JSONObject data) throws IOException{
		final String GOOGLE_API_KEY = "AIzaSyDWxu2LOnymSMRGL9wFMkDn5GybKb503YU";
		final String GCM_URL = "https://android.googleapis.com/gcm/send";
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
		HttpPost post = new HttpPost(GCM_URL);
		post.addHeader("Authorization", "key="+GOOGLE_API_KEY);
		post.addHeader("Content-Type", "application/json");
		try {
        	JSONObject jo = new JSONObject();
    		jo.put("registration_ids", arrayString);
    		jo.put("data", data);
    		System.out.println(jo.toString());
        	post.setEntity(new ByteArrayEntity(jo.toString().getBytes("UTF8")));
			HttpResponse response = client.execute(post);
			StatusLine sl = response.getStatusLine();
			int statusCode = sl.getStatusCode();
			System.out.println("statuscode:"+statusCode);
//			if (statusCode == 200) {
				StringBuilder sb = new StringBuilder();
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				System.out.println(sb.toString());
//			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public static void sendBroadcast(Context context, String lat, String lon, String time){
		Intent paramsIntent = new Intent();
        paramsIntent.setAction("com.cti.nexummobileapp.SEND_PARAMS");
        paramsIntent.putExtra("lat", lat);
        paramsIntent.putExtra("lon", lon);
        paramsIntent.putExtra("time", time);
        context.sendBroadcast(paramsIntent);
	}
	public static void sendNotification(String destregid, String action, String regid) throws IOException{
		try {
			final JSONArray ja = new JSONArray();
			final JSONObject jo = new JSONObject();
			jo.put("type", action);
			if (regid!=null) {
				jo.put("regid", regid);
			}
			ja.put(destregid);
			sendGcmNotification(ja, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	public static void sendChatNotification(String destregid, String action, String[] data) throws IOException{
		try {
			final JSONArray ja = new JSONArray();
			final JSONObject jo = new JSONObject();
			jo.put("type", action);
			jo.put("msgid", data[0]);
			jo.put("status", data[1]);
			ja.put(destregid);
			sendGcmNotification(ja, jo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
