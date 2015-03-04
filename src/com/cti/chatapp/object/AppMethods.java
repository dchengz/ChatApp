package com.cti.chatapp.object;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.cti.chatapp.object.MySSLSocketFactory;

public class AppMethods {
	public static final int NOTIFICATION_ID = 1;
	private static final String SENDurl = "http://contacto123.sytes.net/david/trackerapp/ajax/newdata.php";
	public static final String Download_ID = "DOWNLOAD_ID";
	private static Object locksend = new Object();
	public static Object lockparams = new Object();
	
	public static final JSONArray chat_allstat(String imei) throws IOException, JSONException {
		StringBuilder sb = new StringBuilder();
		HttpClient client = MySSLSocketFactory.getNewHttpClient();
		HttpPost post = new HttpPost(MySSLSocketFactory.URLqueryTab);
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("imei", imei));
		pairs.add(new BasicNameValuePair("action", "chat_allstat"));
		post.setEntity(new UrlEncodedFormEntity(pairs));
		HttpResponse response = client.execute(post);
		StatusLine sl = response.getStatusLine();
		int statusCode = sl.getStatusCode();
		JSONArray jsonArray;
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			InputStream content = entity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			jsonArray = new JSONArray(sb.toString());
			return jsonArray;
		} else {
			return null;
		}
	}
}
