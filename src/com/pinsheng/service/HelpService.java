package com.pinsheng.service;

import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.http.RequestManager;
import com.android.http.RequestManager.RequestListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.google.gson.Gson;
import com.pinsheng.app.HELPApllication;
import com.pinsheng.help.HELPFragment;
import com.pinsheng.help.MainActivity;
import com.pinsheng.help.R;
import com.pinsheng.mode.Person;
import com.pinsheng.mode.RESULT;
import com.pinsheng.mode.Position;
import com.pinsheng.util.Constant;
import com.pinsheng.util.SendMessage;
import com.pinsheng.util.Util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class HelpService extends Service implements SensorEventListener,
		AMapLocationListener {
	private SensorManager sensorManager;
	private Sensor accelerateSensor;
	private NotificationManager notificationManager;
	private NotificationCompat.Builder mBduilder;
	private Notification notification;
	private SendMessage sendMsg;
//	public static LocationManagerProxy mLocationManagerProxy;
	//获取地理位置
	public static LocationClient mLocationClient;
	public static MyLocationListener mMyLocationListener;
	private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor = "bd09ll";
	
	public static int count = 0;
	private int x;

	@Override
	public void onCreate() {
		Log.i("response", "----->onCreat");
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mBduilder = new NotificationCompat.Builder(this);
		mBduilder.setSmallIcon(R.drawable.ic_launcher);
		mBduilder.setContentTitle("云互救实时保您的安全中");
		mBduilder.setContentText("快捷求救功能已开启");
		mBduilder.setTicker("云互救");
		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mBduilder.setContentIntent(contentIntent);
		notification = mBduilder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notificationManager.notify(0, notification);

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerateSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	//	mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		x=(int)(Math.random()*1000)+1;
		// 设置位置请求
				mLocationClient = new LocationClient(getApplicationContext());
				mMyLocationListener = new MyLocationListener();
				mLocationClient.registerLocationListener(mMyLocationListener);
				InitLocation();
		
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("response", "----->onstartCommand");
		flags = START_STICKY;
		if (((HELPApllication) getApplication()).isHelping()) {
			if(mLocationClient!=null){
				mLocationClient.unRegisterLocationListener(mMyLocationListener);
				mLocationClient.stop();
				mLocationClient.requestLocation();
				
			}
			mLocationClient = new LocationClient(getApplicationContext());
			mMyLocationListener = new MyLocationListener();
			mLocationClient.registerLocationListener(mMyLocationListener);
			InitLocation();
			mLocationClient.start();
		}
		sensorManager.registerListener(this, accelerateSensor,
				SensorManager.SENSOR_DELAY_GAME);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.i("response", "----->onDestroy");
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
		}
		notificationManager.cancelAll();
		if(mLocationClient!=null){
			mLocationClient.unRegisterLocationListener(mMyLocationListener);
			mLocationClient.stop();
			mLocationClient.requestLocation();
			
		}
	
		Message msg = new Message();
		msg.what = 4;
		HELPFragment.handler.sendMessage(msg);
		((HELPApllication) getApplication()).setIsHelping(false);
		((HELPApllication) getApplication()).setHelping(false);
		super.onDestroy();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {

		float[] values = event.values;

		float x = values[0];
		float y = values[1];
		float z = values[2];
		int medumValue = 11;
		PowerManager pm = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		if (pm.isScreenOn()) {
			if (Math.abs(z) > medumValue&&Math.abs(x) > medumValue&&Math.abs(y) > medumValue) {
				
				if (Util.ShakeQuickClick.isQuickClick()) {
					return;
				} else {
			//	Log.i("response", "x-------->"+x+"\ny------>"+y+"\nz--------->"+z);
					count++;
					//Log.i("response", ""+"count------>"+count);
					if (count == 8) {
						List<Person> person = ((HELPApllication) (getApplication()))
								.getPersons();
						if (person.size() == 0) {
							Util.Toast(this, "你还没设置联系人呢");
						} else {
							((Vibrator) getSystemService(VIBRATOR_SERVICE))
									.vibrate(new long[] { 500, 480, 500, 480 },
											-1);
							sendMsg = new SendMessage(getApplicationContext(),(HELPApllication)getApplication());
							
							sendMsg.send(this, person,
									((HELPApllication) (getApplication()))
											.getLocation(),
									((HELPApllication) (getApplication()))
											.getImei());
							Util.Call(this, person);
							Util.Toast(this, "检测到摇晃，发送短信和拨打电话！");
							Log.i("responce", "------->change UI");
							count = 0;
							Message msg = new Message();
							msg.what = 3;
							HELPFragment.handler.sendMessage(msg);
							((HELPApllication) getApplication())
									.setIsHelping(true);
							((HELPApllication) getApplication())
									.setHelping(true);
							
							// 设置位置请求
							if(mLocationClient!=null){
								mLocationClient.stop();
							}
							mLocationClient = new LocationClient(getApplicationContext());
							mMyLocationListener = new MyLocationListener();
							mLocationClient.registerLocationListener(mMyLocationListener);
							InitLocation();
							mLocationClient.start();

						}

					}
				}
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onLocationChanged(Location location) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		if (amapLocation != null
				&& amapLocation.getAMapException().getErrorCode() == 0) {
			Log.i("response", "维度: " + amapLocation.getLatitude());
			Log.i("response", "经度: " + amapLocation.getLongitude());
			Log.i("response",
					"识别码: " + ((HELPApllication) (getApplication())).getImei());
			((HELPApllication) getApplication()).setLocation(amapLocation
					.getAddress());
			
			upLoadZuoBiao(amapLocation.getLatitude(),
					amapLocation.getLongitude(),
					x+"",amapLocation.getAddress());
		}
	}

	private void upLoadZuoBiao(final double latitude, final double longitude, String imei,
			String address) {
		Position zuoBiao = new Position(longitude, latitude, imei, address);
		RequestManager.getInstance().post(Constant.URL_UPLOAD_POSITION,
				zuoBiao.toString(), new RequestListener() {

					@Override
					public void onSuccess(String response, String url
							) {
						RESULT result = new Gson().fromJson(response,
								RESULT.class);
						if (result.getStatus() == 200) {
							Log.i("response", "上传位置成功！");
							Util.Toast(getApplication(), "上传当前位置成功!维度: "+latitude+"经度: "+longitude);
						} else {
							Log.i("response", "上传位置失败！");
						}

					}

					@Override
					public void onRequest() {

					}

					@Override
					public void onError(String errorMsg, String url
				) {

					}
				});
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void InitLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);// 设置定位模式
		option.setCoorType(tempcoor);// 返回的定位结果是百度经纬度，默认值gcj02
		int span = 10000;

		option.setScanSpan(span);// 设置发起定位请求的间隔时间为10000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
	}
	
	class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			Log.i("response", "位置: "+location.getAddrStr()+"维度: "+location.getLatitude()+"经度"+location.getLongitude());
			
			upLoadZuoBiao(location.getLatitude(),location.getLongitude(),((HELPApllication) (getApplication())).getImei()+"",((HELPApllication) getApplication()).getLocation());
		}
		}
	}
