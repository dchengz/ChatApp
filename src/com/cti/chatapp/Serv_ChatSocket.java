package com.cti.chatapp;

import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import com.cti.chatapp.MyApplication;
import com.cti.chatapp.Activ_ChatUsers;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Ack;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class Serv_ChatSocket extends Service {
	private Socket mSocket;
	private String myIMEI;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		myIMEI=intent.getStringExtra("logintxt");
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
            mSocket = IO.socket("http://contacto123.sytes.net:3000");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
		mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.connect();
        attemptLogin();
	}

	@Override
	public void onDestroy() {
		mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.disconnect();
		super.onDestroy();
	}
	private void attemptLogin() {
        // perform the user login attempt.
		System.out.println("attempt Login");
        mSocket.emit("add user", new Object[]{myIMEI}, new Ack() {
			@Override
			public void call(Object... args) {
				String alias = (String) args[0];
				String type = (String) args[1];
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

}
