package com.pinsheng.map;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.android.http.LoadControler;
import com.android.http.RequestManager;
import com.android.http.RequestManager.RequestListener;
import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.pinsheng.app.HELPApllication;
import com.pinsheng.help.R;
import com.pinsheng.mode.DangerousaAll;
import com.pinsheng.mode.DangerousaAll.Data;
import com.pinsheng.util.Constant;
import com.pinsheng.util.Util;

public class SearchFragment extends Fragment implements OnItemClickListener,
OnRefreshListener2<ListView>, OnClickListener{
	private PullToRefreshListView mListView;
	private ProgressBar mPro;
	private EditText mEdit;
	private TextView mTextSearch;

	private MyAdapter adapter;
	private Intent intent;

	private LoadControler mLoadControler = null;
	private LinearLayout.LayoutParams lps;

	private Data[] dangerous;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v =inflater.inflate(R.layout.activity_search, container, false);
		initData();
		initView(v);
		PostDangerousPeople(false);
		return v;
	}
	private void initView(View v) {
		mPro = (ProgressBar) v.findViewById(R.id.search_gonepro);
		mTextSearch = (TextView) v.findViewById(R.id.search_text);
		mTextSearch.setOnClickListener(this);
		int lp = mTextSearch.getLayoutParams().width;

		mEdit = (EditText) v.findViewById(R.id.search_edit);
		lps = new LinearLayout.LayoutParams(
				Util.getScreenWidth(getActivity()) - lp
						- Util.dip2px(getActivity(), 32), Util.dip2px(
						getActivity(), 32));
		lps.leftMargin = Util.dip2px(getActivity(), 8);
		mEdit.setLayoutParams(lps);

		mListView = (PullToRefreshListView) v.findViewById(R.id.pullToRefresh);
		mListView.setMode(Mode.PULL_FROM_START);
		mListView.getLoadingLayoutProxy(true, false);
		mListView.setAdapter(adapter);
		mListView.setOnRefreshListener(this);
		mListView.setOnItemClickListener(this);
	}
	private void initData() {
		adapter = new MyAdapter(dangerous, getActivity());
		intent = new Intent();
	}
	private void PostDangerousPeople(final boolean isPull) {
		mLoadControler = RequestManager.getInstance().post(
				Constant.URL_AROUD_DANGEROUS, "{}", new RequestListener() {

					@Override
					public void onSuccess(String response, String url
							) {
						Log.i("cao", "my:" + response);
						DangerousaAll danger = new Gson().fromJson(response,
								DangerousaAll.class);
						switch (danger.getStatus()) {
						case 200:
							dangerous = danger.getData();
							if (dangerous != null) {
								if (!isPull) {
									mListView.setVisibility(View.VISIBLE);
									mPro.setVisibility(View.GONE);
								} else {
									mListView.onRefreshComplete();
								}
								Sorting();
							} else {
								if (!isPull) {
									mListView.setVisibility(View.VISIBLE);
									mPro.setVisibility(View.GONE);
								} else {
									mListView.onRefreshComplete();
								}
								Util.Toast(getActivity(), "没有受害者!");
							}
							break;

						default:
							dangerous = null;
							if (!isPull) {
								mListView.setVisibility(View.VISIBLE);
								mPro.setVisibility(View.GONE);
							} else {
								Util.Toast(getActivity(), "没有受害者!");
								mListView.onRefreshComplete();
							}
							break;
						}
						adapter.RefreshList(dangerous);
					}

					private void Sorting() {
						for (int i = 0; i < dangerous.length - 1; i++) {
							for (int j = 0; j < dangerous.length - 1 - i; j++) {
								Data temp;
								int distance1 = (int) Util.Distance(
										((HELPApllication) (getActivity().getApplication())).getJingdu(),
										((HELPApllication) (getActivity().getApplication())).getWeidu(),
										dangerous[j].getJingdu(), dangerous[j].getWeidu());
								int distance2 = (int) Util.Distance(
										((HELPApllication) (getActivity().getApplication())).getJingdu(),
										((HELPApllication) (getActivity().getApplication())).getWeidu(),
										dangerous[j + 1].getJingdu(),
										dangerous[j + 1].getWeidu());
								if (distance1 > distance2) {
									temp = dangerous[j];
									dangerous[j] = dangerous[j + 1];
									dangerous[j + 1] = temp;
								}
							}
						}
					}

					@Override
					public void onRequest() {
						if (!isPull) {
							mListView.setVisibility(View.GONE);
							mPro.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onError(String errorMsg, String url
						) {
						if (!isPull) {
							mListView.setVisibility(View.GONE);
							mPro.setVisibility(View.VISIBLE);
						} else {
							Util.Toast(getActivity(), "加载失败！");
							mListView.onRefreshComplete();
						}
					}
				});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_text:
			if (TextUtils.isEmpty(mEdit.getText().toString())) {
				Util.Toast(getActivity(), "请输入搜索内容！");
			} else {
				boolean isHave = false;
				for (int i = 0; i < dangerous.length; i++) {
					if (mEdit.getText().toString()
							.equals(dangerous[i].getImei())) {
						Data[] data = new Data[1];
						data[0] = dangerous[i];
						adapter.RefreshList(data);
						isHave = true;
						break;
					}
				}
				if (!isHave) {
					Util.Toast(getActivity(), "不存在这个imei");
				}
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
		PostDangerousPeople(true);
	}

	@Override
	public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Data data = (Data) adapter.getItem(position - 1);
		intent.setClass(getActivity(), MapActivity.class);
		intent.putExtra("IMEI", data.getImei());
		if (data.getAddress() != null) {
			intent.putExtra("LOCATION", data.getAddress());
		} else {
			intent.putExtra("LOCATION", "未获取到地理位置");
		}
		startActivity(intent);
	}
	
}
class MyAdapter extends BaseAdapter {
	private Data[] dangerous;
	private Context context;

	public MyAdapter(Data[] dangerous, Context context) {
		this.dangerous = dangerous;
		this.context = context;
	}

	public void RefreshList(Data[] dangerous) {
		this.dangerous = dangerous;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (dangerous != null) {
			return dangerous.length;
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int arg0) {
		return dangerous[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rootView;
		ViewHolder viewHolder;
		Log.i("cao",
				"jingdu1:"
						+ ((HELPApllication) (context.getApplicationContext()))
								.getJingdu());
		Log.i("cao",
				"weidu1:"
						+ ((HELPApllication) (context.getApplicationContext()))
								.getWeidu());
		Log.i("cao", "jingdu2:" + dangerous[position].getJingdu());
		Log.i("cao", "weidu2:" + dangerous[position].getWeidu());
		String distances = null;
		int distance = (int) Util.Distance(((HELPApllication) (context
				.getApplicationContext())).getJingdu(),
				((HELPApllication) (context.getApplicationContext()))
						.getWeidu(), dangerous[position].getJingdu(),
				dangerous[position].getWeidu());
		;
		if (distance > 1000) {
			distances = (int) (distance / 1000) + "千";
		} else {
			distances = distance + "";
		}
		if (convertView == null) {
			viewHolder = new ViewHolder();
			rootView = LayoutInflater.from(context).inflate(
					R.layout.item_search, parent, false);
			viewHolder.location = (TextView) rootView
					.findViewById(R.id.search_item_location);
			viewHolder.time = (TextView) rootView
					.findViewById(R.id.search_item_time);
			viewHolder.distance = (TextView) rootView
					.findViewById(R.id.search_item_distance);
			rootView.setTag(viewHolder);
		} else {
			rootView = convertView;
			viewHolder = (ViewHolder) rootView.getTag();
		}
		if (dangerous[position].getAddress() != null) {
			viewHolder.location
					.setText(context.getString(R.string.search_location,
							dangerous[position].getAddress()));
		} else {
			viewHolder.location.setText(context.getString(
					R.string.search_location, "暂未获取到位置"));
		}
		if (dangerous[position].getTime() != 0) {

			viewHolder.time.setText(Util.getTime(dangerous[position].getTime()));
		} else {
			viewHolder.time.setText(context.getString(R.string.search_time,
					"暂未获取到时间"));
		}
		if (distance != 0) {
			viewHolder.distance.setText(context.getString(
					R.string.search_distance, distances));
		} else {
			viewHolder.distance.setText(context.getString(
					R.string.search_distance, "暂未获取到距离"));
		}
		return rootView;
	}

	private class ViewHolder {
		private TextView location;
		private TextView time;
		private TextView distance;
	}
}
