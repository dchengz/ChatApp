package com.cti.chatapp;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cti.chatapp.object.GCMHelper;
import com.cti.chatapp.object.Obj_GCM;
import com.cti.chatapp.frame.MessagesFragment;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class Activity_SockChat extends Activity implements MessagesFragment.OnFragmentInteractionListener {
	private EditText msgEdit;
    private Button sendBtn;
    private String profileId, profileName, login, myIMEI;
    GCMHelper gcm_helper;
    public static boolean isInit = false;
    public static ActionBar actionBar;
    private Calendar cal;
    private Socket mSocket;					// get socket from service
    private String mUsername;
    private static final int REQUEST_LOGIN = 0;

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
		String statuslogin = gcm_helper.consStatusbyLogin(login);
		if (!statuslogin.equals("online")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			long ddat = Long.valueOf(statuslogin);
			cal.setTimeInMillis(1000*ddat);
//			statusimei="Ultima visita "+cal.YEAR+"/"+cal.MONTH+"/"+cal.DAY_OF_MONTH+" "+cal.HOUR_OF_DAY+":"+cal.MINUTE;
			statuslogin="Ultima visita "+sdf.format(cal.getTime());
		}
		actionBar.setSubtitle(statuslogin);
		actionBar.setTitle(profileName);
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
		cal=Calendar.getInstance();
		setContentView(R.layout.activity_chat);
		gcm_helper = GCMHelper.getHelper(this);
//		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//		myIMEI = tm.getDeviceId();
		
        profileId = getIntent().getStringExtra("ID");
        login = getIntent().getStringExtra("login");
        myIMEI = getIntent().getStringExtra("myLog");
        profileName = gcm_helper.consRegidbyImei(login, "alias");
        
        System.out.println("prof: "+profileName+" "+login+" "+profileId);
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
				long idmsg = gcm_helper.insertDB(GCMHelper.table_im, new String[]{login,msg,"0",fecha});
				mSocket.emit("new message", new Object[]{msg,idmsg});
				gcm_helper.updateDB(GCMHelper.table_im, new String[]{"1"}, new String[]{String.valueOf(idmsg)});
				MessagesFragment.adapter.add(new Obj_GCM(String.valueOf(idmsg),"111",msg,0,fecha,1));
				MessagesFragment.adapter.notifyDataSetChanged();
//				if (idmsg!=-1) {
//					if (MessagesFragment.adapter!=null) {
//						MessagesFragment.adapter.add(new Obj_GCM(String.valueOf(idmsg),imei,msg,0,fecha,0));
//						MessagesFragment.adapter.notifyDataSetChanged();
//					}
//					try {
//						jo.put("type", "chat");
//						jo.put("imei", myIMEI);
//						jo.put("msg", msg);
//						jo.put("idmsg", idmsg);
//						jo.put("fecha", fecha);
//					} catch (JSONException e) {
//						e.printStackTrace();
//					}
//					ja.put(profileId);
//					
//					new AsyncTask<String, Void, String>() {
//						@Override
//						protected String doInBackground(String... params) {
//							String isSent = null;
//							try {
//								GcmMethods.sendGcmNotification(ja, jo);
//								isSent=params[0];
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							return isSent;
//						}
//						@Override
//						protected void onPostExecute(String result) {
//							if (result != null) {
//								gcm_helper.updateDB(GCMHelper.table_im, new String[]{"1"}, new String[]{result});
//								if (MessagesFragment.adapter!=null) {
//									MessagesFragment.adapter.updateViewed(Integer.valueOf(result), Integer.valueOf("1"));
//									MessagesFragment.adapter.notifyDataSetChanged();
//								}
//							}
//						}						
//					}.execute(String.valueOf(idmsg));					
//				} else {
//					Toast.makeText(getBaseContext(), "Insert error", Toast.LENGTH_LONG).show();
//				}				
                msgEdit.setText(null);
            }
        });
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	public String getProfileEmail() {
		return login;
	}

	private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", myIMEI);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            System.out.println("onLogin "+myIMEI);
//            finish();
        }
    };
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    String extimei = "111";
					String msg = message;
					String fecha = String.valueOf(System.currentTimeMillis());
					long insid = gcm_helper.insertDB(GCMHelper.table_im, new String[]{extimei,msg,"1",fecha});
					MessagesFragment.adapter.add(new Obj_GCM(String.valueOf(insid),extimei,msg,1,fecha,0));
					MessagesFragment.adapter.notifyDataSetChanged();
					
//                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
                    actionBar.setSubtitle("online");

//                    addLog(getResources().getString(R.string.message_user_joined, username));
//                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
                    actionBar.setSubtitle("offline");

//                    addLog(getResources().getString(R.string.message_user_left, username));
//                    addParticipantsLog(numUsers);
//                    removeTyping(username);
                }
            });
        }
    };

}
