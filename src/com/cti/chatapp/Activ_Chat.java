package com.cti.chatapp;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cti.chatapp.object.GCMHelper;
import com.cti.chatapp.object.GcmMethods;
import com.cti.chatapp.object.Obj_GCM;
import com.cti.chatapp.frame.MessagesFragment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Activ_Chat extends Activity implements MessagesFragment.OnFragmentInteractionListener {
	private EditText msgEdit;
    private Button sendBtn;
    private String profileId, profileName, imei, myIMEI;
    private GCMHelper gcm_helper;
    public static boolean isInit = false;
    public static ActionBar actionBar;
    private Calendar cal;
    private boolean wasWriting;    

	@Override
	protected void onStart() {
		isInit=true;
		super.onStart();
		if (!MyApplication.isActivityVisible()) {
			MyApplication.activityResumed(getApplicationContext());
			System.out.println("ON START "+MyApplication.isActivityVisible());
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		String statusimei = gcm_helper.consStatusbyImei(imei);
		if (!statusimei.equals("online")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			long ddat = Long.valueOf(statusimei);
			cal.setTimeInMillis(1000*ddat);
//			statusimei="Ultima visita "+cal.YEAR+"/"+cal.MONTH+"/"+cal.DAY_OF_MONTH+" "+cal.HOUR_OF_DAY+":"+cal.MINUTE;
			statusimei="Ultima visita "+sdf.format(cal.getTime());
		}
		actionBar.setSubtitle(statusimei);
		actionBar.setTitle(profileName);
//		new AsyncTask<String, Void, String>() {
//			@Override
//			protected String doInBackground(String... params) {
//				String status = null;
//				try {
//					status=AppMethods.cons_chat(getBaseContext(), params[0], params[1], null);
//				} catch (IllegalStateException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				return status;
//			}
//			@Override
//			protected void onPostExecute(String result) {
//				if (result!=null) {
//					if (result.equals("1")) {
//						actionBar.setSubtitle("online");
//					} else if (result.equals("0")) {
//						actionBar.setSubtitle("offline");
//					}
//				}
//			}			
//		}.execute(imei,"chat_stat");
	}

	@Override
	protected void onStop() {
		isInit=false;
		super.onStop();
		if (!Activ_ChatUsers.isInit && MyApplication.isActivityVisible()) {
			MyApplication.activityPaused(getApplicationContext());
			System.out.println("ON STOP "+MyApplication.isActivityVisible());
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wasWriting=false;
		cal=Calendar.getInstance();
		setContentView(R.layout.activity_chat);
		gcm_helper = GCMHelper.getHelper(this);
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		myIMEI = tm.getDeviceId();		
        
        profileId = getIntent().getStringExtra("ID");
        imei = getIntent().getStringExtra("imei");
        profileName = gcm_helper.consRegidbyImei(imei, "alias");        
        
        System.out.println("prof: "+profileName+" "+imei+" "+profileId);
        msgEdit = (EditText) findViewById(R.id.msg_edit);
        sendBtn = (Button) findViewById(R.id.send_btn);
        actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setTitle(profileName);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	String msg = msgEdit.getText().toString();
            	if (msg.equals("")) {
					return;
				}
            	String fecha = String.valueOf(System.currentTimeMillis());
            	final JSONArray ja = new JSONArray();
				final JSONObject jo = new JSONObject();
				long idmsg = gcm_helper.insertDB(GCMHelper.table_im, new String[]{imei,msg,"0",fecha});
				if (idmsg!=-1) {
					if (MessagesFragment.adapter!=null) {
						MessagesFragment.adapter.add(new Obj_GCM(String.valueOf(idmsg),imei,msg,0,fecha,0));
						MessagesFragment.adapter.notifyDataSetChanged();
					}
					try {
						jo.put("type", "chat");
						jo.put("imei", myIMEI);
						jo.put("msg", msg);
						jo.put("idmsg", idmsg);
						jo.put("fecha", fecha);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					ja.put(profileId);
					
					new AsyncTask<String, Void, String>() {
						@Override
						protected String doInBackground(String... params) {
							String isSent = null;
							try {
								GcmMethods.sendGcmNotification(ja, jo);
								isSent=params[0];
							} catch (IOException e) {
								e.printStackTrace();
							}
							return isSent;
						}
						@Override
						protected void onPostExecute(String result) {
							if (result != null) {
								gcm_helper.updateDB(GCMHelper.table_im, new String[]{"1"}, new String[]{result});
								if (MessagesFragment.adapter!=null) {
									MessagesFragment.adapter.updateViewed(Integer.valueOf(result), Integer.valueOf("1"));
									MessagesFragment.adapter.notifyDataSetChanged();
								}
							}
						}						
					}.execute(String.valueOf(idmsg));					
				} else {
					Toast.makeText(getBaseContext(), "Insert error", Toast.LENGTH_LONG).show();
				}				
                msgEdit.setText(null);
            }
        });
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	public String getProfileEmail() {
		return imei;
	}

}
