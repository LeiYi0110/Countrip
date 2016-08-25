package com.pi9Lin.navi;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.pi9Lin.activity.IndexActivity;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.IndexFragFocusImage;
import com.pi9Lin.utils.ACache;

/**
 * 旅游地图初始化界面，用于设置起点终点，发起路径计算导航等
 * 
 */
public class NaviStartActivity extends BaseActivity implements OnClickListener,
		OnMarkerClickListener, OnInfoWindowClickListener, OnMapLoadedListener,
		InfoWindowAdapter, OnMapClickListener {
	// --------------View基本控件---------------------
	private MapView mMapView;// 地图控件
	// 地图和导航核心逻辑类
	private AMap mAmap;
	private AMapNavi mAmapNavi;
	// 驾车路径规划起点，终点的list
	private List<NaviLatLng> mEndPoints = new ArrayList<NaviLatLng>();
	private List<NaviLatLng> mStartPoints = new ArrayList<NaviLatLng>();

	// 记录起点的Marker 定位
	private Marker mGPSMarker;

	private List<IndexFragFocusImage> focus_images;
	private List<Marker> maker_list;
	private ACache mCache;
	private AMapNaviListener mAmapNaviListener;
	private RelativeLayout top_back;
	private TextView top_title;
	private TextView addr;
	private TextView cost;
	private Marker currentMarker = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_navistart);
			initActionbar();
			initView(savedInstanceState); // 初始化地图
			initData();
			initMapAndNavi();
			addMarkersToMap();// 往地图上添加marker
			MainApplication.getInstance().addActivity(this);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void initActionbar() {
		// TODO Auto-generated method stub
		// 自定义ActionBar
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_nav_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		top_title = (TextView) v.findViewById(R.id.top_title);
		top_title.setText("选择地理位置");
		top_back = (RelativeLayout) v.findViewById(R.id.top_back);
		top_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.slide_right_out,
						R.anim.slide_right_in);
			}
		});
	}

	/**
	 * 在地图上添加marker
	 */
	private void addMarkersToMap() {
		// 当前位置标记
		String geoLat = preferences.getString("geoLat", null);
		String geoLng = preferences.getString("geoLng", null);

		if (geoLat != null && geoLng != null) {
			double la = Double.valueOf(geoLat).doubleValue();
			double ln = Double.valueOf(geoLng).doubleValue();
			mStartPoints.add(new NaviLatLng(la, ln));
			mGPSMarker = mAmap.addMarker(new MarkerOptions()
					.setFlat(true)
					.position(new LatLng(la, ln))
					.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
							.decodeResource(getResources(),
									R.drawable.location_marker))));
		} else {
			msg("对不起，您还没有定位");
		}

		maker_list = new ArrayList<Marker>();
		// 终(景)点标记
		for (int i = 0; i < focus_images.size(); i++) {
			Marker marker = mAmap.addMarker(new MarkerOptions()
					.title(focus_images.get(i).getName())
					.draggable(false)
					.position(
							new LatLng(focus_images.get(i).getGps()[0],
									focus_images.get(i).getGps()[1])));

			if (focus_images.get(i).getEntity_type() == 1) {
				marker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.zhu_poi));
			} else if (focus_images.get(i).getEntity_type() == 2) {
				marker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.chi_poi));
			} else if (focus_images.get(i).getEntity_type() == 3) {
				marker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.you_poi));
			}
			if (i == 0) {
				mEndPoints.add(new NaviLatLng(marker.getPosition().latitude,
						marker.getPosition().longitude));
				// 计算当前终点的路径 第一次进入就会算
				// initMaker=marker;
				// mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints,
				// null, AMapNavi.DrivingShortDistance);
			}
			maker_list.add(marker);
		}
	}

	/**
	 * 监听点击infowindow窗口事件回调 去计算路径
	 */
	@Override
	public void onInfoWindowClick(Marker marker) {
		// 自定义infowindow 点击时间不在这写
		Intent intent = new Intent(NaviStartActivity.this,
				NaviRouteActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
	}

	/**
	 * 对marker标注点 点击响应事件
	 */
	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		if (marker.equals(mGPSMarker)) {
			if (mAmap != null) {
				return false;
			}
		}
		for (int i = 0; i < maker_list.size(); i++) {
			if (marker.equals(maker_list.get(i))) {
				if (mAmap != null) {
					// 点击了哪项就把那项设为终点
					NaviLatLng naviLatLng = new NaviLatLng(
							marker.getPosition().latitude,
							marker.getPosition().longitude);
					mEndPoints.clear();
					mEndPoints.add(naviLatLng);
					// 计算当前终点的路径
					mAmapNavi.calculateDriveRoute(mStartPoints, mEndPoints,
							null, AMapNavi.DrivingShortDistance);
					// initMaker=marker;
					// 把当前marker对应的地址写到控件上
					addr.setText(focus_images.get(i).getAddress());
					currentMarker = marker;
				}
			}
		}
		return false;
	}

	private void initData() {
		Intent intent = getIntent();
		boolean detail = intent.getBooleanExtra("detail", false);
		if (detail) {
			// 详细页传过来的
			focus_images = new ArrayList<IndexFragFocusImage>();
			IndexFragFocusImage iffi = new IndexFragFocusImage();
			iffi.setEntity_type(intent.getIntExtra("en_type", -1));
			iffi.setName(intent.getStringExtra("name"));
			iffi.setGps(new double[] { intent.getDoubleExtra("geoLat", 0),
					intent.getDoubleExtra("geoLng", 0) });
			iffi.setAddress(intent.getStringExtra("address"));
			focus_images.add(iffi);
		} else {
			// 首页旅游地图 解析的是轮播图的数据
			try {
				mCache = ACache.get(this);
				String result = mCache.getAsString("mapPoints");
				getFocusJson(result);
			} catch (Exception e) {
				Log.d("json解析", "错误");
			}
		}
		// 地址
		addr = (TextView) findViewById(R.id.addr);
		addr.setText("请点击目的地图标");
	}

	/**
	 * 解析轮播图json
	 * */
	protected void getFocusJson(String result) throws Exception {
		JSONObject jsonObject = new JSONObject(result);
		JSONObject jsonObject2 = jsonObject.getJSONObject("data");
		/** 得到首页轮播图 */
		focus_images = new ArrayList<IndexFragFocusImage>();
		JSONArray array3 = jsonObject2.getJSONArray("focus_images");
		for (int i = 0; i < array3.length(); i++) {
			JSONObject jsonObject3 = (JSONObject) array3.opt(i);
			String _id = jsonObject3.getString("_id");
			String name = jsonObject3.getString("name");
			int entity_type = jsonObject3.getInt("entity_type");
			String cover = jsonObject3.getString("cover");
			String address = jsonObject3.getString("address");
			String entity_id = jsonObject3.getString("entity_id");
			JSONObject object = jsonObject3.getJSONObject("gps");
			double latitude = object.getDouble("latitude");
			double longitude = object.getDouble("longitude");
			double[] gps = { latitude, longitude };
			IndexFragFocusImage focusImage = new IndexFragFocusImage(_id, name,
					entity_type, cover, address, entity_id, gps);
			focus_images.add(focusImage);
		}
	}

	/**
	 * 初始化地图3d
	 */
	private void initView(Bundle savedInstanceState) {
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		if (mAmap == null) {
			mAmap = mMapView.getMap();
			setUpMap();
		}
	}

	private void setUpMap() {
		// TODO Auto-generated method stub
		mAmap.setOnMapLoadedListener(this);// 设置amap加载成功事件监听器
		mAmap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
		mAmap.setOnInfoWindowClickListener(this);// 设置点击infoWindow事件监听器
		mAmap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
		mAmap.setOnMapClickListener(this);
	}

	/**
	 * 初始化地图和导航相关内容
	 */
	private void initMapAndNavi() {
		// 初始语音播报资源
		setVolumeControlStream(AudioManager.STREAM_MUSIC);// 设置声音控制
		// 语音播报开始
		mAmapNavi = AMapNavi.getInstance(this);// 初始化导航引擎
	}

	/**
	 * 监听amap地图加载成功事件回调
	 */
	@Override
	public void onMapLoaded() {
		// 设置所有maker显示在当前可视区域地图中
		Builder bb = new LatLngBounds.Builder();
		for (int i = 0; i < maker_list.size(); i++) {
			bb.include(maker_list.get(i).getPosition());
		}
		// bb.include(mGPSMarker.getPosition());
		LatLngBounds bounds = bb.build();
		mAmap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
		if (maker_list.size() < 4) {
			mAmap.moveCamera(CameraUpdateFactory.zoomBy(-5));
		}
	}

	/**
	 * 控件点击事件监听
	 * */
	@Override
	public void onClick(View v) {

	}

	/**
	 * 导航回调函数
	 * 
	 * @return
	 */
	private AMapNaviListener getAMapNaviListener() {
		if (mAmapNaviListener == null) {
			mAmapNaviListener = new AMapNaviListener() {
				@Override
				public void onTrafficStatusUpdate() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onStartNavi(int arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onReCalculateRouteForYaw() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onReCalculateRouteForTrafficJam() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onLocationChange(AMapNaviLocation location) {
				}

				@Override
				public void onInitNaviSuccess() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onInitNaviFailure() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onGetNavigationText(int arg0, String arg1) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onEndEmulatorNavi() {
					// TODO Auto-generated method stub
				}

				/**
				 * 路径计算成功回调函数
				 * */
				@Override
				public void onCalculateRouteSuccess() {
					// initMaker.showInfoWindow();
					AMapNaviPath naviPath = mAmapNavi.getNaviPath();
					// double length = ((int) (naviPath.getAllLength() /
					// (double) 1000 * 10))
					// / (double) 10;
					int time = (naviPath.getAllTime() + 59) / 60;
					cost.setText(time + "分钟");
					// msg("花费时间:"+time);
				}

				@Override
				public void onCalculateRouteFailure(int arg0) {
					msg("路径规划出错");
				}

				@Override
				public void onArrivedWayPoint(int arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onArriveDestination() {
					// TODO Auto-generated method stub
				}

				@Override
				public void onGpsOpenStatus(boolean arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onNaviInfoUpdated(AMapNaviInfo arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onNaviInfoUpdate(NaviInfo arg0) {
					// TODO Auto-generated method stub
				}
			};
		}
		return mAmapNaviListener;
	}

	/**
	 * 返回键处理事件
	 * */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(NaviStartActivity.this,
					IndexActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			MainApplication.getInstance().deleteActivity(this);
			finish();
			overridePendingTransition(R.anim.slide_right_out,
					R.anim.slide_right_in);

		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		// 以上两句必须重写

		TTSController ttsManager = TTSController.getInstance(this);// 初始化语音模块
		ttsManager.init();
		AMapNavi.getInstance(this).setAMapNaviListener(ttsManager);// 设置语音模块播报
		// 以下两句逻辑是为了保证进入首页开启语音和加入导航回调
		AMapNavi.getInstance(this).setAMapNaviListener(getAMapNaviListener());
		TTSController.getInstance(this).startSpeaking();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
		// 以上两句必须重写
		// 下边逻辑是移除监听
		AMapNavi.getInstance(this)
				.removeAMapNaviListener(getAMapNaviListener());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	/**
	 * 监听自定义infowindow窗口的infocontents事件回调
	 */
	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 监听自定义infowindow窗口的infowindow事件回调
	 */
	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		View infoWindow = getLayoutInflater().inflate(
				R.layout.custom_info_window, null);
		render(marker, infoWindow);
		return infoWindow;
	}

	/**
	 * 自定义infowinfow窗口
	 */
	public void render(Marker marker, View view) {
		String title = marker.getTitle();
		TextView titleUi = ((TextView) view.findViewById(R.id.name));
		if (title != null) {
			SpannableString titleText = new SpannableString(title);
			titleText.setSpan(new ForegroundColorSpan(Color.BLACK), 0,
					titleText.length(), 0);
			// titleUi.setTextSize(15);
			titleUi.setText(titleText);
		} else {
			titleUi.setText("");
		}
		cost = (TextView) view.findViewById(R.id.cost);
	}

	@Override
	public void onMapClick(LatLng arg0) {
		// TODO Auto-generated method stub
		double ss = arg0.latitude;
		double dd = arg0.longitude;
		boolean flag = false;
		for (Marker marker : maker_list) {
			if (marker.getPosition().latitude == ss
					&& marker.getPosition().longitude == dd) {
				flag = true;
			}
		}
		if (!flag && currentMarker != null) {
			// 点击marker以外空白部分 消除infowindow
			currentMarker.hideInfoWindow();
			addr.setText("请点击目的地图标");
		}
	}

}
