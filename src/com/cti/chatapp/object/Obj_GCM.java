package com.cti.chatapp.object;

public class Obj_GCM {
	private String id, regid, login;
	private String title, message, f_id, date, chdate;
	private String imei, alias, xlat, xlon;
	private int flag, viewed;
	
	public Obj_GCM(String id, String imei, String message, int flag, String date, int viewed) {
		super();
		this.id = id;
		this.imei = imei;
		this.message = message;
		this.flag = flag;
		this.date = date;
		this.viewed = viewed;
	}
	public Obj_GCM(){		
	}
	public String getId() {
		return id;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRegid() {
		return regid;
	}
	public void setRegid(String regid) {
		this.regid = regid;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getF_id() {
		return f_id;
	}
	public void setF_id(String f_id) {
		this.f_id = f_id;
	}
	public int getViewed() {
		return viewed;
	}
	public void setViewed(int viewed) {
		this.viewed = viewed;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getXlat() {
		return xlat;
	}
	public void setXlat(String xlat) {
		this.xlat = xlat;
	}
	public String getXlon() {
		return xlon;
	}
	public void setXlon(String xlon) {
		this.xlon = xlon;
	}
	public String getChdate() {
		return chdate;
	}
	public void setChdate(String chdate) {
		this.chdate = chdate;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
}