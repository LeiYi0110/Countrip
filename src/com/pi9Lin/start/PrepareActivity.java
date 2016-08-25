package com.pi9Lin.start;


import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.view.Window;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.pi9Lin.activity.IndexActivity;
import com.pi9Lin.base.BaseActivity;
/**
 * 定位在准备页面做
 * 
 * */
public class PrepareActivity extends BaseActivity implements AMapLocationListener{

	/** 定位管理对象 */
	private LocationManagerProxy mLocationManagerProxy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		initLocate();// 初始化定位
	}
	/**
	 * 初始化定位
	 */
	private void initLocate() {
		// TODO Auto-generated method stub
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用destroy()方法
		// 其中如果间隔时间为-1，则定位只定一次
		mLocationManagerProxy.requestLocationData(
				LocationProviderProxy.AMapNetwork, -1, 15, this);
		/** 不启用GPS 基于网络的定位 */
		mLocationManagerProxy.setGpsEnable(false);
	}

	private void finishPrepare() {
		Intent intent=new Intent(context, IndexActivity.class);
		startActivity(intent);
		finish();
	}

	/**
	 * 此方法已经废弃
	 */
	@Override
	public void onLocationChanged(Location amapLocation) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub
		if (amapLocation != null
				&& amapLocation.getAMapException().getErrorCode() == 0) {
			// 获取位置信息
			Double geoLat = amapLocation.getLatitude();
			Double geoLng = amapLocation.getLongitude();
			Editor editor = preferences.edit();
			editor.putString("geoLat", geoLat + "");
			editor.putString("geoLng", geoLng + "");
//			editor.putString("address", amapLocation.getAddress());
//			editor.putString("province", amapLocation.getProvince());
//			editor.putString("city", amapLocation.getCity());
			editor.putString("district", amapLocation.getDistrict());
			editor.commit();
			/** 跳转到首页 */
			finishPrepare();
		} else {
			msg("定位失败，请检查网络");
			/** 跳转到首页 */
			finishPrepare();
		}
	}

	/**
	 * 停止定位，并销毁定位资源
	 * */
	private void stopLocation() {
		if (mLocationManagerProxy != null) {
			mLocationManagerProxy.removeUpdates(this);
			mLocationManagerProxy.destroy();// bug 使用了废弃的方法
		}
		mLocationManagerProxy = null;
	}
	
	/**
	 * 方法必须重写
	 */
	@Override
	protected void onPause() {
		super.onPause();
		stopLocation();// 停止定位
	}
}

