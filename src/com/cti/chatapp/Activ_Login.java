package com.cti.chatapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
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
import org.apache.http.message.BasicNameValuePair;

import com.cti.chatapp.object.GCMHelper;
import com.cti.chatapp.object.MySSLSocketFactory;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Activ_Login extends Activity {
	private TextView tvuser, tvpass;
	private EditText etuser, etpass;
	private Button btn;
	private Handler hh;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_applogin);
		tvuser = (TextView) findViewById(R.id.tvu);
		tvpass = (TextView) findViewById(R.id.tvp);
		etuser = (EditText) findViewById(R.id.user);
		etpass = (EditText) findViewById(R.id.pass);
		btn = (Button) findViewById(R.id.log);
		Intent in = getIntent();
		final String sInt = in.getStringExtra("imei");
		final String regInt = in.getStringExtra("regid");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				inslog(sInt,etuser.getText().toString(),etpass.getText().toString(),regInt);
			}
		});
		hh = new Handler() {
			public void handleMessage(android.os.Message msg){
				if (msg.what==1) {
					Toast.makeText(getBaseContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
				} else if (msg.what==2) {
					pd = new ProgressDialog(Activ_Login.this, R.style.MyTheme);
					pd.setCancelable(false);
					pd.show();
				}
			};
		};
	}
	
	private void inslog(final String im, final String et_user, final String et_pass, final String reg){
		new Thread(new Runnable() {
			@Override
			public void run() {
				hh.obtainMessage(2).sendToTarget();
				StringBuilder sb = new StringBuilder();
				HttpClient client = MySSLSocketFactory.getNewHttpClient();
				HttpPost post = new HttpPost(MySSLSocketFactory.URLqueryTab);
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		        pairs.add(new BasicNameValuePair("action", "login"));
		        pairs.add(new BasicNameValuePair("login",et_user));
		        try {
					post.setEntity(new UrlEncodedFormEntity(pairs));
					HttpResponse response = client.execute(post);
					StatusLine sl = response.getStatusLine();
					int statusCode = sl.getStatusCode();
					if (statusCode == 200) {
						HttpEntity entity = response.getEntity();
						InputStream content = entity.getContent();
						BufferedReader reader = new BufferedReader(new InputStreamReader(content));
						String line;
						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
						if (sb.toString().equals("ERROR")) {
							hh.obtainMessage(1, "Error de Registro").sendToTarget();
						} else if (sb.toString().equals("NO")) {
							hh.obtainMessage(1, "No Existe Usuario").sendToTarget();
						} else {
							GCMHelper gtmp = GCMHelper.getHelper(getApplicationContext());
							gtmp.insertDB(GCMHelper.table_registration, new String[]{et_user,"regidtmp","0"});
							hh.obtainMessage(1, "Usuario Registrado").sendToTarget();
							finish();
						}
					}
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					pd.dismiss();
				}
			}
		}).start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}	

}