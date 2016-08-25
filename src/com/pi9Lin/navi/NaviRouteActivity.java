package com.pi9Lin.navi;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.MapView;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviViewOptions;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.view.RouteOverLay;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.utils.NavUtils;

public class NaviRouteActivity extends BaseActivity implements OnMapLoadedListener{
	
	// 地图导航资源
	private AMap mAmap;
	private AMapNavi mAmapNavi;
	private RouteOverLay mRouteOverLay;
	
	private MapView mMapView;// 地图控件
	
	private boolean mIsMapLoaded = false;
	
	private Button nav_btn;
	private RelativeLayout top_back;
	private TextView top_title;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		initView(savedInstanceState);
		initActionbar();
		MainApplication.getInstance().addActivity(this);
		nav_btn=(Button)findViewById(R.id.nav_btn);
		nav_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Bundle bundle = new Bundle();
				bundle.putInt(NavUtils.THEME, AMapNaviViewOptions.BLUE_COLOR_TOPIC);
				Intent routeIntent = new Intent(NaviRouteActivity.this,
						NaviCustomActivity.class);
				routeIntent.putExtras(bundle);
				startActivity(routeIntent);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				finish();
			}
		});
	}
	
	private void initActionbar() {
        //自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.activity_nav_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
        top_title=(TextView)v.findViewById(R.id.top_title);
        top_title.setText("路径计算");
		top_back = (RelativeLayout)v.findViewById(R.id.top_back);
		top_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
	}
	
	private void initNavi() {
		// TODO Auto-generated method stub
		mAmapNavi = AMapNavi.getInstance(this);
		AMapNaviPath naviPath = mAmapNavi.getNaviPath();
		if (naviPath == null) {
			return;
		}
		// 获取路径规划线路，显示到地图上
		mRouteOverLay.setRouteInfo(naviPath);
		mRouteOverLay.addToMap();
		if (mIsMapLoaded) {
			mRouteOverLay.zoomToSpan();
		}
		//语音播报
		String calculateResult = "路径计算就绪";
		TTSController.ttsManager.playText(calculateResult);
	}

	private void initView(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mMapView = (MapView) findViewById(R.id.routemap);
		mMapView.onCreate(savedInstanceState);
		mAmap = mMapView.getMap();
		mAmap.setOnMapLoadedListener(this);
		mRouteOverLay = new RouteOverLay(mAmap, null);
	}
	
	// ------------------------------生命周期必须重写方法---------------------------
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mMapView.onResume();
		initNavi();
	}

	@Override
	public void onPause() {
		super.onPause();
		mMapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}

	@Override
	public void onMapLoaded() {
		mIsMapLoaded = true;
		if (mRouteOverLay != null) {
			mRouteOverLay.zoomToSpan();
		}
	}
	
}
