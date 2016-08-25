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
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.MD5.MD5;
import com.pi9Lin.adapter.MyStartApr;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.data.City;
import com.pi9Lin.data.ProvinceInfo;
import com.pi9Lin.data.UserInfos;
import com.pi9Lin.database.MyDB;
import com.pi9Lin.network.NetUtils;

public class StartActivity extends BaseActivity {

	private ViewPager yindaoye;
	LinearLayout ll_dots;
	ImageView[] dots;
	TextView txt_start;
	List<View> pagelist;
	int sum;
	RelativeLayout to_start;// 最后一个引导页的跳转按钮
	private UserInfos infos;
	MD5 md5;
	int[] resImg = { R.drawable.yindao_bg1, R.drawable.yindao_bg2,
			R.drawable.yindao_bg3 };
	private boolean isConnected;
	private Builder builders;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);
		md5 = new MD5();
		isConnected = NetUtils.isNetworkConnected(context);
		if (isConnected) {
			isFirstIn();
		} else {
			builders = new AlertDialog.Builder(this);
			builders.setTitle("抱歉，网络连接失败，是否进行网络设置？");
			builders.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// 关闭当前activity
							finish();
						}
					});
			builders.setPositiveButton("去联网",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// 进入无线网络配置界面
							startActivityForResult(new Intent(
									Settings.ACTION_WIRELESS_SETTINGS),1);
						}
					});
			builders.setNeutralButton("重试",
					new DialogInterface.OnClickListener() {// 设置忽略按钮
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (NetUtils.isNetworkConnected(context)) {
								isFirstIn();
							} else {
								builders.show();
							}
						}
					});
			builders.show();
		}
	}

	private void initDots() {
		// TODO Auto-generated method stub
		sum = resImg.length + 1;
		dots = new ImageView[sum];
		for (int i = 0; i < sum; i++) {
			ImageView imageView = new ImageView(context);
			if (i == 0) {
				// 第一个点为白色实心
				imageView.setImageDrawable(getImgResource(R.drawable.shixin));
			} else {
				// 之后为空心
				imageView.setImageDrawable(getImgResource(R.drawable.kongxin));
			}
			imageView.setPadding(0, 0, 20, 0);
			imageView.setId(i);
			imageView.setMinimumWidth(30);
			imageView.setMaxHeight(30);
			dots[i] = imageView;
			ll_dots.addView(imageView);
		}// for
	}

	@SuppressLint("NewApi")
	private void initVPage() {
		// TODO Auto-generated method stub
		pagelist = new ArrayList<View>();
		for (int i = 0; i < resImg.length; i++) {
			/**
			 * 换成添加布局
			 * */
			View view = LayoutInflater.from(context).inflate(
					R.layout.activity_start, null);
			FrameLayout f = (FrameLayout) view.findViewById(R.id.ffff);
			f.setBackground(getImgResource(resImg[i]));
			txt_start = (TextView) view.findViewById(R.id.txt_start);
			if (i == 0) {
				txt_start.setText("新奇独特的场景，总有您意想不到的惊喜。");
			}
			if (i == 1) {
				txt_start.setText("无论在哪里旅行，您都可以吃到当地最特色的美食。");
			}
			if (i == 2) {
				txt_start.setText("从别墅到木屋，各式各样的独特房源任您挑选。");
			}
			pagelist.add(view);
		}
		/**
		 * 添加最后一个引导页面
		 * */
		View view = LayoutInflater.from(context).inflate(
				R.layout.layout_start_last, null);
		ImageView home_img, round_img, mine_img, save_img;
		TextView home_txt, round_txt, mine_txt, save_txt;

		home_img = (ImageView) view.findViewById(R.id.home_img);
		save_img = (ImageView) view.findViewById(R.id.save_img);
		round_img = (ImageView) view.findViewById(R.id.round_img);
		mine_img = (ImageView) view.findViewById(R.id.mine_img);
		home_txt = (TextView) view.findViewById(R.id.home_txt);
		round_txt = (TextView) view.findViewById(R.id.round_txt);
		mine_txt = (TextView) view.findViewById(R.id.mine_txt);
		save_txt = (TextView) view.findViewById(R.id.save_txt);
		to_start = (RelativeLayout) view.findViewById(R.id.to_start);

		home_img.setImageDrawable(getImgResource(R.drawable.index));
		round_img.setImageDrawable(getImgResource(R.drawable.round));
		mine_img.setImageDrawable(getImgResource(R.drawable.mine));
		save_img.setImageDrawable(getImgResource(R.drawable.save_img));

		home_txt.setTextColor(getResources().getColor(R.color.press));
		round_txt.setTextColor(getResources().getColor(R.color.press));
		mine_txt.setTextColor(getResources().getColor(R.color.press));
		save_txt.setTextColor(getResources().getColor(R.color.press));

		pagelist.add(view);
		yindaoye.setAdapter(new MyStartApr(pagelist));
		yindaoye.setCurrentItem(0);
		yindaoye.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				for (int i = 0; i < sum; i++) {
					dots[i].setImageDrawable(getImgResource(R.drawable.kongxin));
				}
				dots[arg0].setImageDrawable(getImgResource(R.drawable.shixin));
				ll_dots.setVisibility(View.VISIBLE);
				if (arg0 == (sum - 1)) {
					ll_dots.setVisibility(View.GONE);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	private void isFirstIn() {
		// TODO Auto-generated method stub
		boolean isFirst = preferences.getBoolean("isFirst", true);
		if (!isFirst) {
			initLandin();// 初始化本地登录
			new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					httpGetJson(); // 获取行政区域信息
				}
			}).start();
			Intent intent = new Intent(context, PrepareActivity.class);
			startActivity(intent);
			StartActivity.this.finish();
		} else {
			/**
			 * 第一次进入
			 * */
			Editor editor = preferences.edit();
			editor.putBoolean("isFirst", false);
			editor.commit();
			/**
			 * 异步去解析城市信息
			 * */
			MyRun myRun = new MyRun();
			myRun.execute();
			/**
			 * 加载引导页
			 * */
			findById();
			initDots();
			initVPage();
			mCache.put("allSave", "unLogIn");
		}
	}

	/**
	 * 初始化本地收藏
	 * */
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

	private void initLandin() {
		// 先判断之前是否有登录记录 有则自动登录
		preferences.edit().putBoolean("isLandIn", false).commit();
		mCache.put("allSave", "unLogIn");
		String name = preferences.getString("username", null);
		if (name == null) {
			// 未注册 登录肯定没有 收藏也没有
		} else {
			// 有注册信息 1.登录
			final String passwd = preferences.getString("passwd", null);
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

	private void findById() {
		// TODO Auto-generated method stub
		yindaoye = (ViewPager) findViewById(R.id.yindaoye);
		ll_dots = (LinearLayout) findViewById(R.id.ll_dots);
	}

	/**
	 * 异步解析城市数据
	 * */
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
			msg("初始化完成！");
			/** 设置监听 */
			to_start.setBackgroundColor(getResources().getColor(R.color.press));
			to_start.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					/** 跳转到主页面 */
					Intent intent = new Intent(context, PrepareActivity.class);
					startActivity(intent);
					StartActivity.this.finish();
				}
			});
			super.onPostExecute(result);
		}
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

	/**
	 * 解析字符串得到城市信息
	 * 
	 * */
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

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 1 ) {
			builders.show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
