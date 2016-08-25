package com.pi9Lin.start;


import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.MD5.MD5;
import com.pi9Lin.activity.IndexActivity;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.data.City;
import com.pi9Lin.data.ProvinceInfo;
import com.pi9Lin.data.UserInfos;
import com.pi9Lin.database.MyDB;
import com.pi9Lin.network.NetUtils;
/**
 * 定位在准备页面做
 * 
 * */
@SuppressLint("HandlerLeak")
public class SplashActivity extends BaseActivity implements AMapLocationListener{

	/** 定位管理对象 */
	private LocationManagerProxy mLocationManagerProxy;
	private static final int GO_HOME = 1000;
	private static final int GO_GUIDE = 1001;
	private static final long SPLASH_DELAY_MILLIS = 1500;
	private MD5 md5;
	private UserInfos infos;
	
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GO_HOME:
				goHome();
				break;
			case GO_GUIDE:
				goGuide();
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash_screen);
		boolean isConnected = NetUtils.isNetworkConnected(context);
		preferences.edit().putBoolean("isLandIn", false).commit();
		mCache.put("allSave", "unLogIn");
		if (isConnected) {
			//有网
			initLandin();
		} else {
			msg("网络未连接");
			init();
		}
	}
	
	private class MyRun extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... arg0) {
			try {
				httpGetJson();
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("错误", e.getMessage());
			}
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			initLocate();// 初始化定位
			super.onPostExecute(result);
		}
	}
	
	private void initLandin() {
		// 先判断之前是否有登录记录 有则自动登录
		String name = preferences.getString("username", null);
		if (name == null) {
			// 未注册 登录肯定没有 收藏也没有
			MyRun myRun = new MyRun();
			myRun.execute();
		} else {
			// 有注册信息 1.登录
			final String passwd = preferences.getString("passwd", null);
			md5 = new MD5();
			String up_passwd = null;
			try {
				up_passwd = md5.getMD5(passwd);
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			AsyncHttpClient client = new AsyncHttpClient();
			PersistentCookieStore myCookieStore = new PersistentCookieStore(
					context);
			client.setCookieStore(myCookieStore);
			RequestParams params = new RequestParams();
			params.put("phone", name);
			params.put("pwd", up_passwd);
			String RegistPath = "http://www.xiangyouji.com.cn:3000/my/login";
			client.post(RegistPath, params, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, String arg1) {
					try {
						if (loginJson(arg1)) {
							jieXi(arg1);
							preferences.edit().putBoolean("isLandIn", true)
									.commit();
							preferences.edit()
									.putString("username", infos.getPhone())
									.commit();
							preferences.edit().putString("passwd", passwd)
									.commit();
							preferences.edit()
									.putString("nickname", infos.getNickname())
									.commit();
							preferences
									.edit()
									.putString("headerImage",
											infos.getHeaderImage()).commit();
							initSave();
						}
					} catch (Exception e) {
						System.out.println("错误:" + e.getMessage());
					}
					super.onSuccess(arg0, arg1);
				}

				@Override
				public void onFailure(Throwable arg0, String arg1) {
					msg("请检查网络");
					super.onFailure(arg0, arg1);
				}
			});
		}
	}
	
	private void initSave() {
		// 2.获取收藏的全部id和type
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);
		String url = "http://www.xiangyouji.com.cn:3000/my/getAllCollectionItems";
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				try {
					if (loginJson(arg1)) {
						// 保存收藏信息到本地
						mCache.put("allSave", arg1);
						MyRun myRun = new MyRun();
						myRun.execute();
					}
				} catch (Exception e) {
					Log.d("保存收藏", e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				super.onFailure(arg0, arg1);
			}
		});
	}
	
	private void jieXi(String arg1) throws Exception {
		// TODO Auto-generated method stub
		infos = new UserInfos();
		JSONObject jsonObject = new JSONObject(arg1);
		JSONObject data = jsonObject.getJSONObject("data");
		String _id = data.getString("_id");
		String phone = data.getString("phone");
		String nickname = data.getString("nickname");
		String pwd = data.getString("pwd");
		String headerImage = data.getString("headerImage");
		String email = data.getString("email");
		String address = data.getString("address");
		infos.set_id(_id);
		infos.setPhone(phone);
		infos.setNickname(nickname);
		infos.setPwd(pwd);
		infos.setHeaderImage(headerImage);
		infos.setEmail(email);
		infos.setAddress(address);
	}
	
	public void httpGetJson() {
		try {
			/** 输入流转字符串 */
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(
					"http://www.xiangyouji.com.cn:3000/area");
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				String string = inputStreamToString(is);
				is.close();
				/** 得到所有城市信息 */
				List<ProvinceInfo> provinceInfo = getCityData(string);
				/** 建立本地数据库 */
				MyDB db = new MyDB(context);
				SQLiteDatabase database = db.getWritableDatabase();
				database.execSQL("drop table if exists province");
				database.execSQL("drop table if exists city");
				database.execSQL("drop table if exists area");
				database.execSQL("create table province(_id varchar(60) not null,region varchar(60) not null , provinceId integer primary key, provinceName varchar(60) not null);");
				database.execSQL("create table city(cityId integer primary key,cityName varchar(60) not null , provinceId integer(6) not null,isShow integer(2) not null);");
				database.execSQL("create table area(area_id integer primary key,area_name varchar(60) not null , cityId integer(6) not null, provinceId integer(6) not null,latitude double(15) not null,longtitude double(15) not null);");
				database.beginTransaction();
				for (ProvinceInfo c : provinceInfo) {
					String sql1 = "insert into province(_id,region,provinceId,provinceName) values ('"
							+ c.get_id()
							+ "','"
							+ c.getRegion()
							+ "',"
							+ c.getProvinceId()
							+ ",'"
							+ c.getProvinceName()
							+ "');";
					database.execSQL(sql1);
					List<City> city = c.getCity();
					for (City s : city) {
						String sql2 = "insert into city(cityId,cityName,provinceId,isShow) values ("
								+ s.getCityId()
								+ ",'"
								+ s.getCityName()
								+ "',"
								+ c.getProvinceId()
								+ ","
								+ s.getIsShow()
								+ ");";
						database.execSQL(sql2);
						List<Area> area = s.getArea();
						for (Area k : area) {
							String sql3 = "insert into area(area_id,area_name,cityId,provinceId,latitude,longtitude) values ("
									+ k.getArea_id()
									+ ",'"
									+ k.getArea_name()
									+ "',"
									+ s.getCityId()
									+ ","
									+ c.getProvinceId()
									+ ","
									+ k.getGps()[0]
									+ "," + k.getGps()[1] + ");";
							database.execSQL(sql3);
						}
					}
				}
				database.setTransactionSuccessful();
				database.endTransaction();
				preferences.edit().putBoolean("isInit", true).commit();
				database.close();
				db.close();
			}

		} catch (Exception e) {
			msg("初始化出错:" + e.getMessage());
		}
	}
	
	public List<ProvinceInfo> getCityData(String string) throws Exception {
		List<ProvinceInfo> list = new ArrayList<ProvinceInfo>();
		/** 解析json数据 */
		JSONObject jsonObject = new JSONObject(string);
		JSONArray jsonObject2 = jsonObject.getJSONArray("data");
		for (int i = 0; i < jsonObject2.length(); i++) {
			try {
				ProvinceInfo provinceInfo = new ProvinceInfo();
				JSONObject jsonObject3 = (JSONObject) jsonObject2.opt(i);
				provinceInfo.set_id(jsonObject3.getString("_id"));
				provinceInfo.setRegion(jsonObject3.getString("region"));
				provinceInfo.setProvinceId(jsonObject3.getInt("provinceId"));
				provinceInfo.setProvinceName(jsonObject3
						.getString("provinceName"));
				JSONArray jsonArray = jsonObject3.getJSONArray("city");
				List<City> list2 = new ArrayList<City>();
				for (int j = 0; j < jsonArray.length(); j++) {
					City city = new City();
					JSONObject jsonObject4 = (JSONObject) jsonArray.opt(j);
					city.setCityId(jsonObject4.getInt("cityId"));
					city.setCityName(jsonObject4.getString("cityName"));
					city.setIsShow(jsonObject4.getInt("isShow"));
					JSONArray jsonArray2 = jsonObject4.getJSONArray("area");
					List<Area> a = new ArrayList<Area>();
					for (int k = 0; k < jsonArray2.length(); k++) {
						Area area = new Area();
						JSONObject jsonObject5 = (JSONObject) jsonArray2.opt(k);
						area.setArea_id(jsonObject5.getInt("area_id"));
						area.setArea_name(jsonObject5.getString("area_name"));
						JSONObject jsonObject6 = jsonObject5
								.getJSONObject("gps");
						double[] gps = { jsonObject6.getDouble("latitude"),
								jsonObject6.getDouble("longitude") };
						area.setGps(gps);
						a.add(area);
					}
					city.setArea(a);
					list2.add(city);
				}
				provinceInfo.setCity(list2);
				list.add(provinceInfo);
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return list;
	}
	
	private void goHome() {
		Intent intent = new Intent(SplashActivity.this, IndexActivity.class);
		SplashActivity.this.startActivity(intent);
		SplashActivity.this.finish();
	}

	private void goGuide() {
		Intent intent = new Intent(SplashActivity.this, GuideActivity.class);
		SplashActivity.this.startActivity(intent);
		SplashActivity.this.finish();
	}
	
	private void init() {
		// TODO Auto-generated method stub
		boolean isFirst = preferences.getBoolean("isFirst", true);
		if (!isFirst) {
			// 不是第一次
			mHandler.sendEmptyMessageDelayed(GO_HOME, SPLASH_DELAY_MILLIS);
		} else {
			// 第一次进入
			Editor editor = preferences.edit();
			editor.putBoolean("isFirst", false);
			editor.commit();
			mHandler.sendEmptyMessageDelayed(GO_GUIDE, SPLASH_DELAY_MILLIS);
		}
	}

	/**
	 * 前进到真正的主页
	 */
	public void GoToIndex() {
		Intent intent = new Intent(this, IndexActivity.class);
		startActivity(intent);
		finish();
	}
	
	/**
	 * 初始化定位
	 */
	public void initLocate() {
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
		}
		init();
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
