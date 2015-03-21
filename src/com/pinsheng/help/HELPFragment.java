package com.pinsheng.help;

import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.android.http.RequestManager;
import com.android.http.RequestManager.RequestListener;
import com.google.gson.Gson;
import com.pinsheng.app.HELPActivity;
import com.pinsheng.app.HELPApllication;
import com.pinsheng.dialog.DialogSetupFragment;
import com.pinsheng.map.SearchActivity;
import com.pinsheng.mode.Person;
import com.pinsheng.mode.RESULT;
import com.pinsheng.mode.UnUpload;
import com.pinsheng.service.HelpService;
import com.pinsheng.util.Constant;
import com.pinsheng.util.SendMessage;
import com.pinsheng.util.Util;
import com.pinsheng.view.CircleView;
import com.pinsheng.view.CircleView.OnCircleViewClickListener;
import com.slider.slider_switch.Slider_Switch;
import com.slider.slider_switch.Slider_Switch.OnChangedListener;

public class HELPFragment extends Fragment implements OnClickListener,
		OnCircleViewClickListener, AMapLocationListener {
	public static TextView mLocationText;
	private ImageView mImageView;
	private ImageView center_button_img;
	public static CircleView mMainImageView;
	private SendMessage sendMsg;
	private FragmentManager mFragmentManager;
	private Intent intent;
	private Slider_Switch shake_switch;
	private RotateAnimation rotateAnimation;
	public static boolean isHelping = false;

	// 获取地理位置
	private LocationManagerProxy mLocationManagerProxy;
	private static String str = null;
	private static final String ADDTAG = "0";
	private static final String PEOPLETAG = "1";
	private Message msg;

	private HelpService helpService;

	public static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				mLocationText.setText(str + "(点我刷新！)");
				break;
			case 2:
				mLocationText.setText("无法获取 (点我刷新)");
				break;
			case 3:
				isHelping = true;
				if (mMainImageView != null) {
					mMainImageView.getmMainButton().setImageResource(
							R.drawable.main_helps_request_help);
					mMainImageView.getImgprogressImageView().setVisibility(
							View.GONE);
				} else {

				}

				break;
			case 4:
				isHelping = false;
				if (mMainImageView != null) {
					mMainImageView.getmMainButton().setImageResource(
							R.drawable.main_helps_btn);
					mMainImageView.getImgprogressImageView().setVisibility(
							View.GONE);
				} else {
					
				}
				
				break;
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getFragmentManager();
		intent = new Intent();
		initAnimation();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_helps, container, false);
		initView(v);
		if (((HELPApllication) getActivity().getApplication()).isHelping()) {
			Message msg = new Message();
			msg.what = 3;
			handler.sendMessage(msg);
		}
		return v;
	}

	@Override
	public void onClick(View view) {
		if (!isHelping) {
			switch (view.getId()) {
			case R.id.main_address_text:

				mLocationText.setText(" 获取中...");
				if (!Util.isNetWorkAvilable(getActivity())) {
					Util.Toast(getActivity(), "网络异常,无法获取你所处的位置！");
					mLocationText.setText("无法获取 (点我刷新)");
				} else {
					// 设置位置请求
					mLocationManagerProxy.removeUpdates(this);
					mLocationManagerProxy = LocationManagerProxy
							.getInstance(getActivity());
					mLocationManagerProxy.setGpsEnable(false);
					mLocationManagerProxy.requestLocationData(
							LocationProviderProxy.AMapNetwork, -1, 15, this);
				}
				break;
		/*	case R.id.img_earth:
				intent = new Intent();
				intent.setClass(getActivity(), SearchActivity.class);
				startActivity(intent);
				break;*/

			default:
				break;
			}
		} else {
			Util.Toast(getActivity(), "请先取消求救！");
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i("response", "madan");
	}

	@Override
	public void OnProgressAnimation(ImageView imageView) {
		if (!isHelping) {
			imageView.setVisibility(View.VISIBLE);
			imageView.startAnimation(rotateAnimation);
		}

	}

	@Override
	public void OnCancleAnimation(ImageView imageView) {
		if (!isHelping) {
			rotateAnimation.cancel();
			imageView.clearAnimation();
			imageView.setVisibility(View.GONE);
		}

	}

	@Override
	public void OnPersonClick(Person person) {
		if (!isHelping) {
			if (Util.QuickClick.isQuickClick()) {
				return;
			} else {
				new DialogSetupFragment(person).show(mFragmentManager,
						PEOPLETAG);
			}
		} else {
			Util.Toast(getActivity(), "请先取消求救！");
		}

	}

	@Override
	public void OnMainClick() {
		Log.i("response", "---->OnMainClick");
		if (!isHelping) {
			List<Person> person = ((HELPApllication) (getActivity()
					.getApplication())).getPersons();
			
			  ((Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE))
			  .vibrate(new long[] { 500, 480, 500, 480 }, -1);
			 
			Util.Toast(getActivity(), "救命！！！");
			if (person.size() == 0) {
				Util.Toast(getActivity(), "你还没设置联系人呢...");
			} else {
				((Vibrator) getActivity().getSystemService(
						Context.VIBRATOR_SERVICE)).vibrate(new long[] { 500,
						480, 500, 480 }, -1);
				sendMsg = new SendMessage(getActivity(),(HELPApllication) getActivity().getApplication());
				sendMsg.send(getActivity(), person, ((MainActivity) getActivity())
						.getWXApplication().getLocation(),((HELPApllication) getActivity().getApplication()).getImei());
				Util.Call(getActivity(), person);
			}
			Message msg = new Message();

			msg.what = 3;
			handler.sendMessage(msg);
			((HELPApllication) getActivity().getApplication()).setHelping(true);
			((HELPApllication) getActivity().getApplication())
					.setIsHelping(true);
			// 开始上传位置
			if (!shake_switch.isChecked()) {
				getActivity().startService(
						new Intent(getActivity(), HelpService.class));
				shake_switch.setChecked(true);
				((HELPApllication) (getActivity().getApplication()))
						.setShakeOpen(true);
			} else {
				getActivity().startService(
						new Intent(getActivity(), HelpService.class));
			}

		}

	}

	@Override
	public void OnMainRequestHelpClick() {
		Log.i("response", "---->OnMainRequestHelpclick");
		if (isHelping) {

			/*if (HelpService.mLocationManagerProxy != null) {
				Log.i("response", "HelpService.mLocationManagerProxy!=null");
				// HelpService.mLocationManagerProxy.removeUpdates();
				HelpService.mLocationManagerProxy.destroy();
			}*/
			if(HelpService.mLocationClient!=null&&HelpService.mMyLocationListener!=null){
				Log.i("response", "HelpService.mLocationClient!=null&&HelpService.mMyLocationListener!=null");
				HelpService.mLocationClient.unRegisterLocationListener(HelpService.mMyLocationListener);
				HelpService.mLocationClient.stop();
				HelpService.mLocationClient.requestLocation();
			}
			if (mLocationManagerProxy == null) {
				// 初始化的时候设置位置请求
				mLocationManagerProxy = LocationManagerProxy
						.getInstance(getActivity());
				mLocationManagerProxy.setGpsEnable(false);
				if (!Util.isNetWorkAvilable(getActivity())) {
					mLocationText.setText("无法获取 (点我刷新)");
				} else {
					mLocationManagerProxy.requestLocationData(
							LocationProviderProxy.AMapNetwork, -1, 15, this);
				}
			}
			
			mMainImageView.getmMainButton()
			.setImageResource(
					R.drawable.main_helps_btn);
			isHelping = false;
			((HELPApllication) getActivity()
			.getApplication()).setHelping(false);
			((HELPApllication) getActivity()
			.getApplication()).setIsHelping(false);
			UnUpload uu = new UnUpload(((HELPApllication) getActivity()
					.getApplication()).getImei());
			RequestManager.getInstance().post(
					Constant.URL_REQUEST_UPLOAD_POSITION, uu.toString(),
					new RequestListener() {

						@Override
						public void onSuccess(String response, String url
						) {
							RESULT result = new Gson().fromJson(response,
									RESULT.class);
							if (result.getStatus() == 200) {
								Log.i("response", "取消求救成功！");
								Util.Toast(getActivity(), "取消求救成功！");

							} else {
							}
						}

						@Override
						public void onRequest() {

						}

						@Override
						public void onError(String errorMsg, String url) {
						}
					});

		}
	}

	@Override
	public void OnAddClick() {
		if (!isHelping) {
			if (Util.QuickClick.isQuickClick()) {
				return;
			} else {
				new DialogSetupFragment().show(mFragmentManager, ADDTAG);
			}
		} else {
			Util.Toast(getActivity(), "请先取消求救！");
		}

	}

	private void initView(View view) {
		shake_switch = (Slider_Switch) view.findViewById(R.id.shake_switch);
		mLocationText = (TextView) view.findViewById(R.id.main_address_text);
		mImageView = (ImageView) view.findViewById(R.id.img_earth);
		mMainImageView = (CircleView) view.findViewById(R.id.help_img);
		mLocationText.setText("获取中...");

		if (((HELPApllication) getActivity().getApplication()).isHelping()) {
			isHelping = true;
			Message msg = new Message();
			msg.what = 3;
			handler.sendMessage(msg);
			mLocationText.setText(((HELPApllication) getActivity()
					.getApplication()).getLocation());
		} else {
			// 设置位置请求
			mLocationManagerProxy = LocationManagerProxy
					.getInstance(getActivity());
			mLocationManagerProxy.setGpsEnable(false);
			if (!Util.isNetWorkAvilable(getActivity())) {
				mLocationText.setText("无法获取 (点我刷新)");
			} else {
				mLocationManagerProxy.requestLocationData(
						LocationProviderProxy.AMapNetwork, -1, 15, this);
			}
		}

		mMainImageView.setOnCircleViewClickListener(this);
		mLocationText.setOnClickListener(this);
		mImageView.setOnClickListener(this);
		shake_switch.setChecked(((HELPApllication) (getActivity()
				.getApplication())).isShakeOpen());
		if (((HELPApllication) (getActivity().getApplication())).isShakeOpen()) {
			Log.i("response", "----->shakeOpen");
		}
		shake_switch.setOnChangedListener(new OnChangedListener() {

			@Override
			public void OnChanged(Slider_Switch Slider, boolean checkState) {
				if (checkState) {
					Log.i("response", "---->shakeOpen");
					getActivity().startService(
							new Intent(getActivity(), HelpService.class));

				} else {
					Log.i("response", "---->shakeClose");
					getActivity().stopService(
							new Intent(getActivity(), HelpService.class));
				}

				((HELPApllication) (getActivity().getApplication()))
						.setShakeOpen(checkState);
			}
		});
		refreshPerson();
	}

	private void initAnimation() {
		rotateAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateAnimation.setDuration(500);
	}

	public void refreshPerson() {
		mMainImageView.removeAllPerson();
		mMainImageView.addPerson(((HELPApllication) getActivity()
				.getApplication()).getPersons());
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
		Log.i("response", "onLocationChanged");
		msg = new Message();
		if (amapLocation != null
				&& amapLocation.getAMapException().getErrorCode() == 0) {
			str = amapLocation.getAddress();
			((HELPApllication) getActivity().getApplication())
					.setLocation(amapLocation.getAddress());
			((HELPActivity) getActivity()).getWXApplication().setWeidu(
					amapLocation.getLatitude());
			((HELPActivity) getActivity()).getWXApplication().setJingdu(
					amapLocation.getLongitude());
			Log.i("response", ((HELPActivity) getActivity()).getWXApplication()
					.getWeidu()
					+ "\n"
					+ ((HELPActivity) getActivity()).getWXApplication()
							.getJingdu());
			msg.what = 1;
		} else {
			msg.what = 2;
		}
		handler.sendMessage(msg);
	}

}
