package com.cti.chatapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cti.chatapp.object.AppMethods;
import com.cti.chatapp.object.GCMHelper;
import com.cti.chatapp.object.Obj_GCM;

public class Activ_ChatUsers extends ListActivity {
	public static MyArrayAdapterUsers mAdapter = null;
	public static boolean isInit = false;
	public static String myIMEI;
	private GCMHelper gcm_helper;
	private Calendar cal;
	private Handler hh;

	@Override
	protected void onStart() {
		isInit = true;
		super.onStart();
		if (!MyApplication.isActivityVisible()) {
			MyApplication.activityResumed(getApplicationContext());
			System.out.println("ON START "+MyApplication.isActivityVisible());
		}
//		new AsyncTask<String, Void, JSONArray>() {
//			@Override
//			protected JSONArray doInBackground(String... params) {
//				JSONArray status = null;
//				try {
//					status=AppMethods.chat_allstat(params[0]);
//				} catch (IOException e) {
//					e.printStackTrace();
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//				return status;
//			}
//			@Override
//			protected void onPostExecute(JSONArray jsonArray) {
//				if (jsonArray!=null) {
//					try {
//						JSONObject jo = new JSONObject();
//						for (int i = 0; i < jsonArray.length(); i++) {
//							jo = jsonArray.getJSONObject(i);
//							String imeii = jo.getString("imei");
//							String[] data = new String[]{jo.getString("sta"),jo.getString("fec")};
//							gcm_helper.updateChat(data, new String[]{imeii});
//							if (isInit) {
//								mAdapter.update(imeii, data);
//							}
//						}
//						if (isInit) {
//							mAdapter.notifyDataSetChanged();
//						}
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//				}
//			}			
//		}.execute(myIMEI);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == R.id.action_login) {
			if (isServRunning()) {
				hh.obtainMessage(1, "Servicio detenido").sendToTarget();
				stopService(new Intent(this, Serv_ChatSocket.class));
			} else {
				ArrayList<Obj_GCM> obj = gcm_helper.consDB(GCMHelper.table_registration);
				if (obj.size()>0) {
					Intent i = new Intent(this, Serv_ChatSocket.class);
					System.out.println("chatusers: login="+obj.get(0).getLogin());
					i.putExtra("logintxt", obj.get(0).getLogin());
					startService(i);
					hh.obtainMessage(1, "Servicio iniciado").sendToTarget();
				} else {
					startActivity(new Intent(this, Activ_Login.class));
				}
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private Boolean isServRunning() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		Boolean bool = false;
		for (RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
			if (service.service.getClassName().equals("com.cti.chatapp.Serv_ChatSocket")) {
				bool = true; break;
			}
		}
		return bool;
	}
	@Override
	protected void onStop() {
		isInit = false;
		super.onStop();
		if (MyApplication.isActivityVisible() && !Activ_Chat.isInit) {
			MyApplication.activityPaused(getApplicationContext());
			System.out.println("ON STOP "+MyApplication.isActivityVisible());
		}
	}
	@Override
	protected void onDestroy() {
		mAdapter=null;
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (isServRunning()) {
			hh.obtainMessage(1, "SERV RUNNING").sendToTarget();
		} else {
			hh.obtainMessage(1, "SERV NOT RUN").sendToTarget();
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cal=Calendar.getInstance();
		hh = new Handler() {
			public void handleMessage(android.os.Message msg){
				if (msg.what==1) {
					Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
				} else if (msg.what==2) {
				}
			};
		};
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		myIMEI = tm.getDeviceId();
		gcm_helper = GCMHelper.getHelper(this);
		ArrayList<Obj_GCM> objarr = gcm_helper.consDB(GCMHelper.table_users);
		mAdapter = new MyArrayAdapterUsers(this, objarr);
		setListAdapter(mAdapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		v.setBackgroundColor(Color.TRANSPARENT);
		Obj_GCM o = mAdapter.getItem(position);
		String regid = o.getRegid();
		Intent i = new Intent(this, Activity_SockChat.class);
		isInit = true;	
		i.putExtra("ID", regid);
		i.putExtra("login", o.getLogin());
		i.putExtra("myLog", myIMEI);
		startActivity(i);
	}
	public class MyArrayAdapterUsers extends ArrayAdapter<Obj_GCM> {
		private Context context;
		private ArrayList<Obj_GCM> gcm_arr;
		
		public MyArrayAdapterUsers(Context context, ArrayList<Obj_GCM> objarr) {
			super(context, R.layout.activity_chatusers, objarr);
			this.context=context;
			this.gcm_arr = objarr;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Obj_GCM ob = gcm_arr.get(position);
			String alias = ob.getAlias();
			String login = ob.getLogin();
			if (convertView==null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.activity_chatusers, parent, false);
			}
			TextView tv_name1 = (TextView) convertView.findViewById(R.id.tv_name1);
			TextView tv_name2 = (TextView) convertView.findViewById(R.id.tv_name2);
			TextView tv_date3 = (TextView) convertView.findViewById(R.id.tv_date3);
			tv_name1.setText(alias);
			if (ob.getFlag()==0) {
				tv_name2.setText("offline");
			} else {
				tv_name2.setText("online");
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd\nHH:mm");
			long ddat = Long.valueOf(ob.getChdate());
			cal.setTimeInMillis(1000*ddat);
			tv_date3.setText(sdf.format(cal.getTime()));
//			if (ob.getViewed()==0) {
//				View v = (View) tv_name1.getParent();
//				v.setBackgroundColor(Color.YELLOW);
//				tv_name1.setTypeface(null, Typeface.BOLD);
//			}
			convertView.setPadding(8, 12, 8, 12);
			return convertView;
		}
		public void update(String login, String[] data) {
			for (int i = 0; i < gcm_arr.size(); i++) {
				System.out.println(gcm_arr.get(i).getId()+" viewed:"+gcm_arr.get(i).getViewed());
				if (gcm_arr.get(i).getLogin().equals(login)) {
					gcm_arr.get(i).setFlag(Integer.valueOf(data[0]));
					gcm_arr.get(i).setChdate(data[1]);
					break;
				}
			}
		}
	}
}
