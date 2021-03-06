package com.pinsheng.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.http.RequestManager;
import com.baidu.mapapi.SDKInitializer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pinsheng.mode.Person;
import com.pinsheng.mode.SendMsgHistory;
import com.pinsheng.service.HelpService;
import com.pinsheng.util.Util;
import com.umeng.analytics.MobclickAgent;

/**
 * 重写Application
 */
public class HELPApllication extends Application {

	private boolean isShakeOpen;
	private List<Person> persons;
	private List<SendMsgHistory> sendHistory;
	private SharedPreferences sp;
	private String imei;
	private String location;
	private TelephonyManager telManager;
	private double weidu;
	private double jingdu;
	private boolean isHelping;
	
	public List<SendMsgHistory> getSendHistory() {
		return sendHistory;
	}

	public void setSendHistory(List<SendMsgHistory> sendHistory) {
		this.sendHistory = sendHistory;
	}

	public boolean isHelping() {
		return isHelping;
	}

	public void setHelping(boolean isHelping) {
		this.isHelping = isHelping;
	}

	public double getWeidu() {
		return weidu;
	}

	public void setWeidu(double weidu) {
		this.weidu = weidu;
	}

	public double getJingdu() {
		return jingdu;
	}

	public void setJingdu(double jingdu) {
		this.jingdu = jingdu;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MobclickAgent.updateOnlineConfig(this);
		SDKInitializer.initialize(this); 
		RequestManager.getInstance().init(this);
		sp = Util.getPreference(this);
		
		
		persons = new Gson().fromJson(sp.getString("person", ""),
				new TypeToken<List<Person>>() {
				}.getType());
		if (persons == null) {
			persons = new ArrayList<Person>();
		}
		//得到已发送短信记录
		sendHistory = new Gson().fromJson(sp.getString("sendHistory", ""), new TypeToken<List<SendMsgHistory>>(){
		}.getType());
		if(sendHistory ==null){
			sendHistory = new ArrayList<SendMsgHistory>();
		}
		isShakeOpen = sp.getBoolean("isshakeable2", false);
		isHelping=sp.getBoolean("isHelping", false);
		if (isShakeOpen) {
			startService(new Intent(this, HelpService.class));
		}
		telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		imei = telManager.getSubscriberId();
		Log.i("response", imei);
		
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public void setShakeOpen(boolean isOpen) {
		this.isShakeOpen = isOpen;
		sp.edit().putBoolean("isshakeable2", isOpen).commit();
	}
	public void setIsHelping(boolean isHelping){
		this.isHelping=isHelping;
		sp.edit().putBoolean("isHelping", isHelping).commit();
	}
	
	
	public boolean isShakeOpen() {
		return isShakeOpen;
	}

	public List<Person> getPersons() {
		return persons;
	}

	public void setPersons(List<Person> persons) {
		this.persons = persons;
	}

	private void savePersons() {
		SharedPreferences sp = Util.getPreference(this);
		sp.edit().putString("person", new Gson().toJson(persons)).commit();
	}
	
	public  void saveSendHistory(){
		SharedPreferences sp = Util.getPreference(this);
		sp.edit().putString("sendHistory", new Gson().toJson(sendHistory)).commit();
		
	}

	public void addPerson(Person person) {
		for (Person temp : persons) {
			if (temp.equals(person)) {
				temp.updateData(person);
				savePersons();
				return;
			}
		}
		persons.add(person);
		savePersons();
	}

	public Person findPerson(String name, String tel) {
		Person newperson = new Person();
		newperson.setName(name);
		newperson.setTel(tel);
		for (Person temp : persons) {
			if (temp.equals(newperson)) {
				return temp;
			}
		}
		return null;
	}

	public void deletPerson(Person person) {
		for (Person temp : persons) {
			if (temp.equals(person)) {
				persons.remove(temp);
				savePersons();
				return;
			}
		}
	}

	public void cleanPersonPhone() {
		for (Person temp : persons) {
			temp.setPhone(false);
		}
	}

	public void insteadPerson(Person person) {
		for (Person temp : persons) {
			if (temp.getName().equals("")) {
				temp.updateData(person);
				savePersons();
				return;
			}
		}
	}

	public boolean JudgeHavePeople(Person person) {
		for (Person temp : persons) {
			if (temp.equals(person)) {
				temp.updateData(person);
				savePersons();
				return true;
			}
		}
		return false;
	}

}