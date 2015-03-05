package com.cti.chatapp;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.cti.chatapp.object.GCMHelper;
import com.cti.chatapp.object.Obj_GCM;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

public class Serv_ChatSocket extends Service {
	public Socket mSocket;
	private String myIMEI;
	private GCMHelper gcm_helper;
	private Map<String, String> conversac;
	private Calendar cal;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		ArrayList<Obj_GCM> obj = gcm_helper.consDB(GCMHelper.table_registration);
		myIMEI = obj.get(0).getLogin();
		attemptLogin();
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gcm_helper = GCMHelper.getHelper(this);
		cal=Calendar.getInstance();
		conversac = new HashMap<String, String>();
		try {
            mSocket = IO.socket("http://contacto123.sytes.net:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
		mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("login", onLogin);
        mSocket.on("logout", onLogout);
        mSocket.on("new conver", onNewConver);
        mSocket.on("new message", onNewMessage);
        mSocket.on("make_leave_room", onMakeLeaveRoom);
        mSocket.connect();
	}

	@Override
	public void onDestroy() {
		mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("login", onLogin);
        mSocket.off("logout", onLogout);
        mSocket.off("new conver", onNewConver);
        mSocket.off("new message", onNewMessage);
        mSocket.off("make_leave_room", onMakeLeaveRoom);
        mSocket.disconnect();
		super.onDestroy();
	}
	private void attemptLogin() {
        // perform the user login attempt.
		System.out.println("attempt Login: "+myIMEI);
        mSocket.emit("add user", new Object[]{myIMEI}, new Ack() {
			@Override
			public void call(Object... args) {
				String alias = (String) args[0];
				int type = (Integer) args[1];
				if (type==0) {
					mSocket.emit("queryUsers", null, new Ack() {
						@Override
						public void call(Object... args) {
							JSONArray userslist = (JSONArray) args[0];
							gcm_helper.deleteDB(GCMHelper.table_users);
							for (int i = 0; i < userslist.length(); i++) {
								try {																		
									JSONObject jo = userslist.getJSONObject(i);
									String[] arrst = new String[]{jo.getString("login"),jo.getString("alias"),jo.getString("regid"),jo.getString("chat_online"),jo.getString("last_seen")};
									gcm_helper.insertDB(GCMHelper.table_users, arrst);
									System.out.println("Object: "+jo.getString("login")+" "+jo.getString("chat_online"));
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}
					});
				}				
				System.out.println("add user: "+alias+" "+type);
			}
		});
    }
    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        	System.out.println("onConnectError: "+R.string.error_connect);
//        	Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
        }
    };
    private Emitter.Listener onNewConver = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        	JSONArray arr_conversac = (JSONArray) args[0];
        	try {
				JSONObject jo = arr_conversac.getJSONObject(0);
				conversac.put(jo.getString("username"), jo.getString("token"));
				mSocket.emit("add room", new Object[]{jo.getString("token")});
			} catch (JSONException e) {
				e.printStackTrace();
			}
        	System.out.println("onNewConver: "+R.string.error_connect);
//        	Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
        }
    };
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        	JSONArray arr_conversac = (JSONArray) args[0];
        	try {
				JSONObject jo = arr_conversac.getJSONObject(0);				
				gcm_helper.insertDB(GCMHelper.table_im, new String[]{jo.getString("username"),jo.getString("message"),"1",String.valueOf(cal.getTimeInMillis()/1000)});				
//				mSocket.emit("add room", new Object[]{jo.getString("token")});
			} catch (JSONException e) {
				e.printStackTrace();
			}
        	System.out.println("onNewConver: "+R.string.error_connect);
//        	Toast.makeText(getApplicationContext(), R.string.error_connect, Toast.LENGTH_LONG).show();
        }
    };
    private Emitter.Listener onMakeLeaveRoom = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        	JSONArray arr_data = (JSONArray) args[0];
        	try {
				JSONObject jo = arr_data.getJSONObject(0);				
				mSocket.emit("leave room", new Object[]{jo.getString("room")});
				conversac.remove(jo.getString("username"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
        	
        }
    };
    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    String message;
//                    try {
//                        message = data.getString("message");
//                    } catch (JSONException e) {
//                        return;
//                    }
//                    String extimei = "111";
//					String msg = message;
//					String fecha = String.valueOf(System.currentTimeMillis());
//					long insid = gcm_helper.insertDB(GCMHelper.table_im, new String[]{extimei,msg,"1",fecha});
//					MessagesFragment.adapter.add(new Obj_GCM(String.valueOf(insid),extimei,msg,1,fecha,0));
//					MessagesFragment.adapter.notifyDataSetChanged();
//					
////                    removeTyping(username);
//                }
//            });
        }
    };
    private Emitter.Listener onLogout = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    JSONObject data = (JSONObject) args[0];
//                    String message;
//                    try {
//                        message = data.getString("message");
//                    } catch (JSONException e) {
//                        return;
//                    }
//                    String extimei = "111";
//					String msg = message;
//					String fecha = String.valueOf(System.currentTimeMillis());
//					long insid = gcm_helper.insertDB(GCMHelper.table_im, new String[]{extimei,msg,"1",fecha});
//					MessagesFragment.adapter.add(new Obj_GCM(String.valueOf(insid),extimei,msg,1,fecha,0));
//					MessagesFragment.adapter.notifyDataSetChanged();
//					
////                    removeTyping(username);
//                }
//            });
        }
    };

}
