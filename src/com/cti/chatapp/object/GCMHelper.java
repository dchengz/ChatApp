package com.cti.chatapp.object;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GCMHelper extends SQLiteOpenHelper {
	private static AtomicInteger mWriteCounter = new AtomicInteger();
	private static GCMHelper instance;
	
	public static synchronized GCMHelper getHelper (Context context) {
		if (instance == null) {
			instance = new GCMHelper(context);
		}
		return instance;
	}
	
	private SQLiteDatabase mDatabase;
	
	private static final int DATABASE_VERSION = 1;
	private static final String dbName="GCMdb";
	public static final String table_registration="register";
	public static final String table_inbox="inbox";
	public static final String table_users="users";
	public static final String table_im="im";
	private static final String GCM_REGISTRATION = "CREATE TABLE IF NOT EXISTS "+table_registration+" ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "login TEXT, "
			+ "regid TEXT, "
			+ "flag INTEGER default 0);";
	private static final String GCM_INBOX = "CREATE TABLE IF NOT EXISTS "+table_inbox+" ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "title TEXT, "
			+ "message TEXT, "
			+ "f_id TEXT, "
			+ "date TEXT, "
			+ "viewed TEXT);";
	private static final String GCM_USERS = "CREATE TABLE IF NOT EXISTS "+table_users+" ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "imei TEXT, "
			+ "alias TEXT, "
			+ "regid TEXT, "
			+ "xlat TEXT, "
			+ "xlon TEXT, "
			+ "date TEXT, "
			+ "chat_state TEXT default 0, "
			+ "chat_date TEXT default 0);";
	private static final String GCM_IM = "CREATE TABLE IF NOT EXISTS "+table_im+" ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ "imei TEXT, "
			+ "message TEXT, "
			+ "flag INTEGER default 0, "
			+ "viewed INTEGER default 0, "
			+ "date TEXT);";
	public GCMHelper(Context context) {
		super(context, dbName, null, DATABASE_VERSION);
	}
	private synchronized SQLiteDatabase openDatabase() {
        if(mWriteCounter.incrementAndGet() == 1) {
            mDatabase = instance.getWritableDatabase();
        }
        return mDatabase;
    }
    private synchronized void closeDatabase() {
        if(mWriteCounter.decrementAndGet() == 0) {
            mDatabase.close();
        }
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(GCM_REGISTRATION);
		db.execSQL(GCM_INBOX);
		db.execSQL(GCM_USERS);
		db.execSQL(GCM_IM);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		db.execSQL("DROP TABLE IF EXISTS "+table_inbox);
		db.execSQL("DROP TABLE IF EXISTS "+table_users);
		db.execSQL("DROP TABLE IF EXISTS "+table_im);
//		onCreate(db);
		db.execSQL(GCM_USERS);
		db.execSQL(GCM_IM);
	}
	public long insertDB(String table, String... s){
		long li = 0;
		ContentValues cv = new ContentValues();
		if(table.equals(table_registration)){
			cv.put("login", s[0]);
			cv.put("regid", s[1]);
			cv.put("flag", s[2]);
		} else if(table.equals(table_inbox)){
			cv.put("title", s[0]);
			cv.put("message", s[1]);
			cv.put("f_id", s[2]);
			cv.put("date", s[3]);
			cv.put("viewed", s[4]);
		} else if(table.equals(table_im)){
			cv.put("imei", s[0]);
			cv.put("message", s[1]);
			cv.put("flag", s[2]);
			cv.put("date", s[3]);
		} else {
			cv.put("imei", s[0]);
			cv.put("alias", s[1]);
			cv.put("regid", s[2]);
			cv.put("xlat", s[3]);
			cv.put("xlon", s[4]);
			cv.put("date", s[5]);
		}
		openDatabase();
		li = mDatabase.insert(table, null, cv);
		closeDatabase();
		return li;
	}
	public String consStatusbyImei(String imei){
		openDatabase();
		Cursor c = mDatabase.rawQuery("SELECT chat_state,chat_date FROM "+table_users+" WHERE imei = ?", new String[]{imei});
		String answer = null;
		if (c.moveToFirst()) {
			if (Integer.valueOf(c.getString(c.getColumnIndex("chat_state")))==0) {
				answer=c.getString(c.getColumnIndex("chat_date"));
			} else {
				answer="online";
			}
		}
		c.close();
		closeDatabase();
		return answer;
	}
	public String consRegidbyImei(String imei, String whichone){
		openDatabase();
		Cursor c = mDatabase.rawQuery("SELECT regid,alias FROM "+table_users+" WHERE imei = ?", new String[]{imei});
		String regid = null;
		if (c.moveToFirst()) {
			if (whichone.equals("reg")) {
				regid=c.getString(c.getColumnIndex("regid"));
			} else {
				regid=c.getString(c.getColumnIndex("alias"));
			}
		}
		c.close();
		closeDatabase();
		return regid;
	}
	public ArrayList<Obj_GCM> consMsgs(String imei) {
		String qry = "SELECT _id,imei,message,flag,viewed,date FROM "+table_im+" WHERE imei = ? ORDER BY _id asc";
//		String qry = "SELECT _id,imei,message,flag,viewed,date FROM "+table_im+" ORDER BY _id desc";
		openDatabase();
		Cursor c = mDatabase.rawQuery(qry, new String[]{imei});
		ArrayList<Obj_GCM> armo = new ArrayList<Obj_GCM>();
		if (c.moveToFirst()) {
			do {
				Obj_GCM mo = new Obj_GCM();
				mo.setId(c.getString(c.getColumnIndex("_id")));
				mo.setImei(c.getString(c.getColumnIndex("imei")));
				mo.setMessage(c.getString(c.getColumnIndex("message")));
				mo.setFlag(Integer.valueOf(c.getString(c.getColumnIndex("flag"))));
				mo.setViewed(Integer.valueOf(c.getString(c.getColumnIndex("viewed"))));
				mo.setDate(c.getString(c.getColumnIndex("date")));
				armo.add(mo);
			} while (c.moveToNext());
			c.close();
			closeDatabase();
			return armo;
		} else {
			c.close();
			closeDatabase();
			return armo;
		}
	}
	public ArrayList<Obj_GCM> consDB(String table) {
		String qry;
		if (table.equals(table_registration)) {
			qry="SELECT _id,login,regid,flag FROM "+table+" ORDER BY _id";
		} else if(table.equals(table_inbox)) {
			qry="SELECT _id,title,message,f_id,date,viewed FROM "+table+" ORDER BY _id desc";
		} else if(table.equals(table_im)) {
			qry="SELECT _id,imei,message,flag,viewed,date FROM "+table+" ORDER BY _id desc";
		} else {
			qry="SELECT _id,imei,alias,regid,xlat,xlon,date,chat_state,chat_date FROM "+table+" ORDER BY _id desc";
		}
		openDatabase();
		Cursor c = mDatabase.rawQuery(qry, null);
		ArrayList<Obj_GCM> armo = new ArrayList<Obj_GCM>();
		if (c.moveToFirst()) {
			do {
				Obj_GCM mo = new Obj_GCM();
				mo.setId(c.getString(c.getColumnIndex("_id")));
				if (table.equals(table_registration)) {
					mo.setLogin(c.getString(c.getColumnIndex("login")));
					mo.setRegid(c.getString(c.getColumnIndex("regid")));
					mo.setFlag(Integer.valueOf(c.getString(c.getColumnIndex("flag"))));
				} else if(table.equals(table_inbox)) {
					mo.setTitle(c.getString(c.getColumnIndex("title")));
					mo.setMessage(c.getString(c.getColumnIndex("message")));
					mo.setF_id(c.getString(c.getColumnIndex("f_id")));
					mo.setDate(c.getString(c.getColumnIndex("date")));
					mo.setViewed(Integer.valueOf(c.getString(c.getColumnIndex("viewed"))));
				} else if(table.equals(table_im)) {
					mo.setImei(c.getString(c.getColumnIndex("imei")));
					mo.setMessage(c.getString(c.getColumnIndex("message")));
					mo.setFlag(Integer.valueOf(c.getString(c.getColumnIndex("flag"))));
					mo.setViewed(Integer.valueOf(c.getString(c.getColumnIndex("viewed"))));
					mo.setDate(c.getString(c.getColumnIndex("date")));
				} else {
					mo.setImei(c.getString(c.getColumnIndex("imei")));
					mo.setAlias(c.getString(c.getColumnIndex("alias")));
					mo.setRegid(c.getString(c.getColumnIndex("regid")));
					mo.setXlat(c.getString(c.getColumnIndex("xlat")));
					mo.setXlon(c.getString(c.getColumnIndex("xlon")));
					mo.setDate(c.getString(c.getColumnIndex("date")));
					mo.setFlag(Integer.valueOf(c.getString(c.getColumnIndex("chat_state"))));
					mo.setChdate(c.getString(c.getColumnIndex("chat_date")));
				}
				armo.add(mo);
			} while (c.moveToNext());
			c.close();
			closeDatabase();
			return armo;
		} else {
			c.close();
			closeDatabase();
			return armo;
		}
	}
	public void deleteDB(String table){
		openDatabase();
		mDatabase.delete(table, null, null);
		closeDatabase();
	}
	public void updateChat(String[] values, String[] whereArgs) {
		ContentValues cv = new ContentValues();
		String whereClause = null;
		cv.put("chat_state", values[0]);
		cv.put("chat_date", values[1]);
		whereClause = "imei=?";
		openDatabase();
		mDatabase.update(table_users, cv, whereClause, whereArgs);
		closeDatabase();
	}
	public void updateDB(String table, String[] values, String[] whereArgs) {
		ContentValues cv = new ContentValues();
		String whereClause = null;
		if (table.equals(table_registration)) {
			cv.put("flag", values[0]);
		} else if(table.equals(table_users)){
			cv.put("xlat", values[0]);
			cv.put("xlon", values[1]);
			cv.put("date", values[2]);
		} else if(table.equals(table_im)){
			cv.put("viewed", values[0]);
			whereClause = "_id=?";
		} else if(table.equals(table_inbox)){
			cv.put("viewed", values[0]);
			whereClause = "_id=?";
		}
		openDatabase();
		mDatabase.update(table, cv, whereClause, whereArgs);
		closeDatabase();
	}
}
