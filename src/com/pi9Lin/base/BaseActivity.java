package com.pi9Lin.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.data.City;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.NearBy;
import com.pi9Lin.data.ProvinceInfo;
import com.pi9Lin.data.SleepActData;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.database.MyDB;
import com.pi9Lin.utils.ACache;

/**
 * 基类 存放公共方法
 * */
public class BaseActivity extends FragmentActivity {
	public Context context;
	public SharedPreferences preferences;
	/** popwindow */
	public PopupWindow popuWindow1;
	public View contentView1;
	public LinearLayout cancel;
	protected static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	protected DisplayImageOptions options;

	protected ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	protected ACache mCache;
	
	protected List<Entity> allSave; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		context = getApplicationContext();
		preferences = getConfig();
		mCache = ACache.get(context);
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * 获取所有的收藏信息
	 * */
	protected void getAllSave() {
		try {
			String testString = mCache.getAsString("allSave");
			if (testString.equals("unLogIn")) {
				allSave = new ArrayList<Entity>();
			} else {
				allSave = save(testString);
			}
		} catch (Exception e) {
			Log.d("获取收藏缓存错误", "未登录无缓存");
		}
	}
	
	/**
	 * 获得屏幕高度
	 * */
	public int getWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	/**
	 * 对象反编译为json
	 * */
	public String obj2Json(List<Entity> list) throws Exception {
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject object = new JSONObject();
			object.put("entity_id", list.get(i).getEntity_id());
			object.put("entity_type", list.get(i).getEntity_type());
			array.put(i, object);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("data", array);
		return jsonObject.toString();
	}

	/**
	 * 等待提示
	 * */
	public Dialog dialog(Context context, String string) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);
		TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);
		tipTextView.setText(string);// 设置加载信息
		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

		loadingDialog.setCancelable(false);// 不可以用“返回键”取消
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
		return loadingDialog;
	}

	/**
	 * 检验json
	 * */
	public boolean checkJson(String arg1) throws Exception {
		JSONObject jsonObject = new JSONObject(arg1);
		int status = jsonObject.getInt("status");
		if (status == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 获取本地配置
	 * */
	public SharedPreferences getConfig() {
		return context.getSharedPreferences("RecordSets", Context.MODE_PRIVATE);
	}

	/**
	 * 去掉定位结果中的市字
	 * */
	public String stringToCitynm(String string) {
		String[] ss = string.split("区");
		return ss[0];
	}

	/** 从本地以消耗内存最小方式获取图片资源 */
	public BitmapDrawable getImgResource(int r) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		InputStream is = getResources().openRawResource(r);
		Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
		BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
		return bd;
	}

	/**
	 * 打印函数
	 * */
	public void msg(String s) {
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 分享popwin
	 * */
	public void initPopuWindow1(View parent) {
		if (popuWindow1 == null) {
			LayoutInflater mLayoutInflater = LayoutInflater.from(this);
			contentView1 = mLayoutInflater.inflate(
					R.layout.layout_share_popwindow, null);
			popuWindow1 = new PopupWindow(contentView1,
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		// 添加返回键使弹窗消失 点击空白处不消失
		contentView1.setFocusable(true); // 这个很重要
		contentView1.setFocusableInTouchMode(true);
		cancel = (LinearLayout) contentView1.findViewById(R.id.cancel);
		// ColorDrawable cd = new ColorDrawable(0xb0000000);
		// popuWindow1.setBackgroundDrawable(cd);
		// popuWindow1.setBackgroundDrawable(new PaintDrawable());//添加一个空白的背景
		// 产生背景变暗效果
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.3f;
		getWindow().setAttributes(lp);
		popuWindow1.setOutsideTouchable(false);
		popuWindow1.setFocusable(true);
		popuWindow1.setAnimationStyle(R.style.PopupAnimation);// 设置窗口显示的动画效果
		popuWindow1.showAtLocation((View) parent.getParent(), Gravity.BOTTOM,
				0, 0);
		popuWindow1.update();
		popuWindow1.setOnDismissListener(new OnDismissListener() {
			// 在dismiss中恢复透明度
			public void onDismiss() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1f;
				getWindow().setAttributes(lp);
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				popuWindow1.dismiss();
				popuWindow1 = null;
			}
		});
		// 重写onKeyListener
		contentView1.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					popuWindow1.dismiss();
					popuWindow1 = null;
					return true;
				}
				return false;
			}
		});
	}

	public void showShare(String title,String desc) {
		// ShareSDK.initSDK(this);
		OnekeyShare oks = new OnekeyShare();
		// 关闭sso授权
		// oks.disableSSOWhenAuthorize();
		// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
		// oks.setNotification(R.drawable.ic_launcher,
		// getString(R.string.app_name));
		// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
		oks.setTitle(title);
		// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
		oks.setTitleUrl("http://www.xiangyouji.com.cn/");
		// text是分享文本，所有平台都需要这个字段
		oks.setText(desc);
		// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
		// oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
		// url仅在微信（包括好友和朋友圈）中使用
		oks.setUrl("http://www.xiangyouji.com.cn/");
		// comment是我对这条分享的评论，仅在人人网和QQ空间使用
		oks.setComment("我是测试评论文本");
		// site是分享此内容的网站名称，仅在QQ空间使用
		oks.setSite(getString(R.string.app_name));
		// siteUrl是分享此内容的网站地址，仅在QQ空间使用
		oks.setSiteUrl("http://www.xiangyouji.com.cn/");
		// 启动分享GUI
		oks.show(this);
	}

	/**
	 * 从本地数据库读取省表信息
	 * */
	public List<ProvinceInfo> getProvinceInfos() {
		List<ProvinceInfo> infos = new ArrayList<ProvinceInfo>();
		MyDB myDB = new MyDB(context);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from province", null);
		while (cursor.moveToNext()) {
			ProvinceInfo info = new ProvinceInfo();
			info.set_id(cursor.getString(cursor.getColumnIndex("_id")));
			info.setRegion(cursor.getString(cursor.getColumnIndex("region")));
			info.setProvinceId(cursor.getInt(cursor
					.getColumnIndex("provinceId")));
			info.setProvinceName(cursor.getString(cursor
					.getColumnIndex("provinceName")));
			infos.add(info);
		}
		cursor.close();
		db.close();
		myDB.close();
		return infos;
	}

	/**
	 * 从本地数据库读取市表信息
	 * */
	public List<City> getCityInfos() {
		List<City> infos = new ArrayList<City>();
		MyDB myDB = new MyDB(context);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from city", null);
		while (cursor.moveToNext()) {
			City info = new City();
			info.setCityId(cursor.getInt(cursor.getColumnIndex("cityId")));
			info.setProvinceId(cursor.getInt(cursor
					.getColumnIndex("provinceId")));
			info.setCityName(cursor.getString(cursor.getColumnIndex("cityName")));
			info.setIsShow(cursor.getInt(cursor.getColumnIndex("isShow")));
			infos.add(info);
		}
		cursor.close();
		db.close();
		myDB.close();
		return infos;
	}

	/**
	 * 从本地数据库读取区域表信息 区域表连接整个数据表 是核心表
	 * */
	public List<Area> getAreaInfos() {
		List<Area> infos = new ArrayList<Area>();
		MyDB myDB = new MyDB(context);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from area", null);
		while (cursor.moveToNext()) {
			Area info = new Area();
			info.setArea_id(cursor.getInt(cursor.getColumnIndex("area_id")));
			info.setCityId(cursor.getInt(cursor.getColumnIndex("cityId")));
			info.setProvinceId(cursor.getInt(cursor
					.getColumnIndex("provinceId")));
			info.setArea_name(cursor.getString(cursor
					.getColumnIndex("area_name")));
			double[] gps = {
					cursor.getDouble(cursor.getColumnIndex("latitude")),
					cursor.getDouble(cursor.getColumnIndex("longtitude")) };
			info.setGps(gps);
			infos.add(info);
		}
		cursor.close();
		db.close();
		myDB.close();
		return infos;
	}

	/**
	 * 快速从本地读取离线数据 全国所有城市信息
	 * 
	 * */
	public List<ProvinceInfo> getData(List<ProvinceInfo> p, List<City> c,
			List<Area> a) {
		/** 先把每个市的区装好 */
		for (City city : c) {
			List<Area> temp = new ArrayList<Area>();
			for (Area area : a) {
				if (area.getCityId() == city.getCityId()) {
					temp.add(area);
				}
			}
			city.setArea(temp);
		}
		/** 再把每个省的市装好 */
		for (ProvinceInfo province : p) {
			List<City> temp = new ArrayList<City>();
			for (City city : c) {
				if (city.getProvinceId() == province.getProvinceId()) {
					temp.add(city);
				}
			}
			province.setCity(temp);
		}
		return p;
	}

	/**
	 * 输入流转化为字符串
	 * */
	public String inputStreamToString(InputStream inputStream) {
		String outString = "";
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String lenString = "";
			while ((lenString = bufferedReader.readLine()) != null) {
				outString += lenString;
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("异常:" + e.getMessage());
		}
		return outString;
	}

	/**
	 * 解析按县搜吃、住、玩的json
	 * */
	public List<SleepActData> jsonToString(String string, String stamp)
			throws Exception {
		JSONObject jsonObject = new JSONObject(string);
		JSONArray array = jsonObject.getJSONArray("data");
		List<SleepActData> actDatas = new ArrayList<SleepActData>();
		for (int i = 0; i < array.length(); i++) {
			SleepActData actData = new SleepActData();
			JSONObject jsonObject2 = (JSONObject) array.opt(i);
			String _id = jsonObject2.getString("_id");
			int area_id = jsonObject2.getInt("area_id");
			int city_id = jsonObject2.getInt("city_id");
			String shop_id = jsonObject2.getString("shop_id");
			int star_count = jsonObject2.getInt("star_count");
			int location_value = jsonObject2.getInt("service_value");
			int city = jsonObject2.getInt("city");
			int collection_count = jsonObject2.getInt("collection_count");
			int comment_count = jsonObject2.getInt("comment_count");
			String restaurant_name = null;
			String restaurant_telephone = null;
			String restaurant_address = null;
			if (stamp.equals("hotel")) {
				restaurant_name = jsonObject2.getString("hotel_name");
				restaurant_telephone = jsonObject2.getString("hotel_telephone");
				restaurant_address = jsonObject2.getString("hotel_address");
			} else if (stamp.equals("restaurant")) {
				restaurant_name = jsonObject2.getString("restaurant_name");
				restaurant_telephone = jsonObject2
						.getString("restaurant_telephone");
				restaurant_address = jsonObject2
						.getString("restaurant_address");
			} else if (stamp.equals("sights")) {
				restaurant_name = jsonObject2.getString("sights_name");
				restaurant_telephone = jsonObject2
						.getString("sights_telephone");
				restaurant_address = jsonObject2.getString("sights_address");
			}
			String cover = jsonObject2.getString("cover");
			JSONArray array2 = jsonObject2.getJSONArray("images");
			List<SleepImgData> imgDatas = new ArrayList<SleepImgData>();
			for (int j = 0; j < array2.length(); j++) {
				SleepImgData imgData = new SleepImgData();
				JSONObject jsonObject3 = (JSONObject) array2.opt(j);
				String url = jsonObject3.getString("url");
				String update_time = jsonObject3.getString("update_time");
				String create_time = jsonObject3.getString("create_time");
				int order_no = jsonObject3.getInt("order");
				imgData.setUrl(url);
				imgData.setUpdate_time(update_time);
				imgData.setCreate_time(create_time);
				imgData.setOrder_no(order_no);
				imgDatas.add(imgData);
			}
			JSONObject jsonObject3 = jsonObject2.getJSONObject("gps");
			double latitude = jsonObject3.getDouble("latitude");
			double longitude = jsonObject3.getDouble("longitude");
			double[] gps = { latitude, longitude };
			actData.set_id(_id);
			actData.setArea_id(area_id);
			actData.setCity_id(city_id);
			actData.setShop_id(shop_id);
			actData.setXx_name(restaurant_name);
			actData.setXx_address(restaurant_address);
			actData.setXx_telephone(restaurant_telephone);
			actData.setCity(city);
			actData.setVillage(star_count);
			actData.setLocation_value(location_value);
			actData.setCover(cover);
			actData.setImgDatas(imgDatas);
			actData.setGps(gps);
			actData.setCollection_count(collection_count);
			actData.setComment_count(comment_count);
			actData.setSleep_love_img(new ImageView(context)); // 保证点赞图片不会出现空指针
			actData.setCollection(new TextView(context));
			actDatas.add(actData);
		}
		return actDatas;
	}

	/**
	 * 解析按经纬度搜吃、住、玩的json
	 * */
	public List<SleepActData> geoJsonToString(String string, String stamp)
			throws Exception {
		JSONObject jsonObject = new JSONObject(string);
		JSONArray array = jsonObject.getJSONArray("data");
		List<SleepActData> actDatas = new ArrayList<SleepActData>();
		for (int i = 0; i < array.length(); i++) {
			SleepActData actData = new SleepActData();
			JSONObject jsonObject2 = (JSONObject) array.opt(i);
			String _id = jsonObject2.getString("_id");
			int area_id = jsonObject2.getInt("area_id");
			int city_id = jsonObject2.getInt("city_id");
			String shop_id = jsonObject2.getString("shop_id");
			int service_value = jsonObject2.getInt("service_value");
			int city = jsonObject2.getInt("city");
			String restaurant_name = null;
			String restaurant_telephone = null;
			String restaurant_address = null;
			String restaurant_desc = null;
			if (stamp.equals("hotel_list")) {
				restaurant_name = jsonObject2.getString("hotel_name");
				restaurant_telephone = jsonObject2.getString("hotel_telephone");
				restaurant_address = jsonObject2.getString("hotel_address");
				restaurant_desc = jsonObject2.getString("hotel_desc");
			} else if (stamp.equals("restaurant_list")) {
				restaurant_name = jsonObject2.getString("restaurant_name");
				restaurant_telephone = jsonObject2
						.getString("restaurant_telephone");
				restaurant_address = jsonObject2
						.getString("restaurant_address");
				restaurant_desc = jsonObject2.getString("restaurant_desc");
			} else if (stamp.equals("sights_list")) {
				restaurant_name = jsonObject2.getString("sights_name");
				restaurant_telephone = jsonObject2
						.getString("sights_telephone");
				restaurant_address = jsonObject2.getString("sights_address");
				restaurant_desc = jsonObject2.getString("sights_desc");
			}
			JSONObject geoObject = jsonObject2.getJSONObject("gps");
			double latitude = geoObject.getDouble("latitude");
			double longitude = geoObject.getDouble("longitude");
			double[] ss = { latitude, longitude };
			actData.setGps(ss);
			String cover = jsonObject2.getString("cover");
			JSONArray array2 = jsonObject2.getJSONArray("images");
			List<SleepImgData> imgDatas = new ArrayList<SleepImgData>();
			for (int j = 0; j < array2.length(); j++) {
				SleepImgData imgData = new SleepImgData();
				JSONObject jsonObject3 = (JSONObject) array2.opt(j);
				String url = jsonObject3.getString("url");
				String update_time = jsonObject3.getString("update_time");
				String create_time = jsonObject3.getString("create_time");
				int order_no = jsonObject3.getInt("order");
				imgData.setUrl(url);
				imgData.setUpdate_time(update_time);
				imgData.setCreate_time(create_time);
				imgData.setOrder_no(order_no);
				imgDatas.add(imgData);
			}
			actData.setXx_desc(restaurant_desc);
			actData.set_id(_id);
			actData.setArea_id(area_id);
			actData.setCity_id(city_id);
			actData.setShop_id(shop_id);
			actData.setXx_name(restaurant_name);
			actData.setXx_address(restaurant_address);
			actData.setXx_telephone(restaurant_telephone);
			actData.setCity(city);
			actData.setLocation_value(service_value);
			actData.setCover(cover);
			actData.setImgDatas(imgDatas);
			actData.setCollection(new TextView(context));
			actData.setSleep_love_img(new ImageView(context));
			actDatas.add(actData);
		}
		return actDatas;
	}

	/**
	 * 异步去获取图片 同时做了本地缓存
	 * */
	public Bitmap getBitmap(String urlString) {
		try {
			Bitmap bitmap = null;
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setDoInput(true);
			connection.connect();
			/** 从连接对象上获得输入流 */
			InputStream inputStream = connection.getInputStream();
			if (inputStream != null) {
				// String path = getImgCachePath();
				// String filename = System.currentTimeMillis() + ".jpg";
				// saveDownloadImg(path, filename, inputStream);
				bitmap = BitmapFactory.decodeStream(inputStream);
				inputStream.close();
				return bitmap;
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println("错误:" + e.getMessage());
		}
		return null;
	}

	/**
	 * 解析按经纬获取附近json
	 * */
	public NearBy geoJsonToString(String s) throws Exception {
		NearBy nearBy = new NearBy();
		JSONObject jsonObject = new JSONObject(s);
		JSONObject jsonObject2 = jsonObject.getJSONObject("data");
		String cover = jsonObject2.getString("cover");
		int count = jsonObject2.getInt("count");
		nearBy.setCount(count);
		nearBy.setCover(cover);
		return nearBy;
	}

	/**
	 * 按_id获取详细json
	 * */
	public SleepActData detailJsonToString(String s, String stamp)
			throws Exception {
		SleepActData actData = new SleepActData();
		JSONObject jsonObject = new JSONObject(s);
		JSONObject jsonObject2 = jsonObject.getJSONObject("data");
		String _id = jsonObject2.getString("_id");
		int area_id = jsonObject2.getInt("area_id");
		int city_id = jsonObject2.getInt("city_id");
		String shop_id = jsonObject2.getString("shop_id");
		// int village=jsonObject2.getInt("village");
		// int location_value=jsonObject2.getInt("location_value");
		int city = jsonObject2.getInt("city");
		String restaurant_name = null;
		String restaurant_telephone = null;
		String restaurant_address = null;
		if (stamp.equals("hotel")) {
			restaurant_name = jsonObject2.getString("hotel_name");
			restaurant_telephone = jsonObject2.getString("hotel_telephone");
			restaurant_address = jsonObject2.getString("hotel_address");
		} else if (stamp.equals("restaurant")) {
			restaurant_name = jsonObject2.getString("restaurant_name");
			restaurant_telephone = jsonObject2
					.getString("restaurant_telephone");
			restaurant_address = jsonObject2.getString("restaurant_address");
		} else if (stamp.equals("sights")) {
			restaurant_name = jsonObject2.getString("sights_name");
			restaurant_telephone = jsonObject2.getString("sights_telephone");
			restaurant_address = jsonObject2.getString("sights_address");
		}
		String cover = jsonObject2.getString("cover");
		JSONArray array2 = jsonObject2.getJSONArray("images");
		List<SleepImgData> imgDatas = new ArrayList<SleepImgData>();
		for (int j = 0; j < array2.length(); j++) {
			SleepImgData imgData = new SleepImgData();
			JSONObject jsonObject3 = (JSONObject) array2.opt(j);
			String url = jsonObject3.getString("url");
			String update_time = jsonObject3.getString("update_time");
			String create_time = jsonObject3.getString("create_time");
			int order_no = jsonObject3.getInt("order");
			imgData.setUrl(url);
			imgData.setUpdate_time(update_time);
			imgData.setCreate_time(create_time);
			imgData.setOrder_no(order_no);
			imgDatas.add(imgData);
		}
		JSONObject jsonObject3 = jsonObject2.getJSONObject("gps");
		double latitude = jsonObject3.getDouble("latitude");
		double longitude = jsonObject3.getDouble("longitude");
		double[] gps = { latitude, longitude };
		actData.set_id(_id);
		actData.setArea_id(area_id);
		actData.setCity_id(city_id);
		actData.setShop_id(shop_id);
		actData.setXx_name(restaurant_name);
		actData.setXx_address(restaurant_address);
		actData.setXx_telephone(restaurant_telephone);
		actData.setCity(city);
		// actData.setVillage(village);
		// actData.setLocation_value(location_value);
		actData.setCover(cover);
		actData.setImgDatas(imgDatas);
		actData.setGps(gps);
		return actData;
	}

	/**
	 * 解析收藏
	 * */
	public List<Entity> save(String data) throws Exception {
		JSONObject jsonObject = new JSONObject(data);
		JSONArray array = jsonObject.getJSONArray("data");
		List<Entity> list = new ArrayList<Entity>();
		for (int i = 0; i < array.length(); i++) {
			Entity entity = new Entity();
			JSONObject object = (JSONObject) array.opt(i);
			String entity_id = object.getString("entity_id");
			int entity_type = object.getInt("entity_type");
			entity.setEntity_id(entity_id);
			entity.setEntity_type(entity_type);
			list.add(entity);
		}
		return list;
	}

	/**
	 * 异步去收藏
	 * */
	public void threadToSave(int entity_type, String entity_id) {
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();
		params.put("entity_type", entity_type + "");// ???????
		params.put("entity_id", entity_id);
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/collect";
		client.post(RegistPath, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				try {
					msg("收藏成功");
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "网络有错误", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}

	/**
	 * 判断图片文件是否存在
	 * */
	public File isImgExist(String urlString) {
		String path = getImgCachePath();
		String fnameString = getUrlFileName(urlString);
		File file = new File(path + "/" + fnameString);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	/**
	 * 获得图片缓存地址
	 * */
	public String getImgCachePath() {
		if (isWriteSD()) {
			String pathString = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/Countrip";
			if (!new File(pathString).exists()) {
				new File(pathString).mkdirs();
			}
			return pathString;
		} else {
			String pathString = context.getCacheDir().getAbsolutePath();
			if (!new File(pathString).exists()) {
				new File(pathString).mkdirs();
			}
			return pathString;
		}
	}

	/**
	 * 判断sd卡是否可写(有用)
	 * */
	protected boolean isWriteSD() {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * 获取图片文件名
	 * */
	public String getUrlFileName(String urlString) {
		String outString = "";
		int lastIndex = urlString.lastIndexOf("/");
		outString = urlString.substring(lastIndex + 1);
		return outString;
	}

	/**
	 * 从本地路径把读出压缩后的图片 防止oom
	 * */
	public static Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	/**
	 * 网络连接
	 * */
	public InputStream getHTTpInfo(String urlString, String fun, String parm)
			throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 1000 * 15);
			HttpGet get = new HttpGet(urlString);
			HttpResponse response;

			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();

				return entity.getContent();
			} else {
				return null;
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 保存图片
	 * */
	public void saveDownLoadImg(String filepath, String filename,
			InputStream inputStream) throws Exception {
		File file = new File(filepath);
		if (!file.exists()) {
			file.mkdirs();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(new File(file,
				filename));
		byte[] arrs = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(arrs)) != -1) {
			fileOutputStream.write(arrs, 0, len);
		}
		fileOutputStream.close();
	}

	/**
	 * 对图片进行压缩
	 * */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// TODO Auto-generated method stub
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			final int widthRadio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRadio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		// 第一次解析将inJustDecodeBounds设置为true，来获取图片大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 从网络下载的压缩后的图片 防止oom
	 * */
	public static Bitmap decodeSampledBitmapFromResource(InputStream in,
			int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeStream(in);
	}

	public boolean loginJson(String arg1) throws Exception {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(arg1);
		int status = jsonObject.getInt("status");
		if (status == 1) {
			return true;
		}
		return false;
	}

	/**
	 * 获得listview当前滚动的位置
	 * */
	public int getScrollY(ListView l) {
		View c = l.getChildAt(0);
		if (c == null) {
			return 0;
		}
		int firstVisiblePosition = l.getFirstVisiblePosition();
		int top = c.getTop();
		return -top + firstVisiblePosition * c.getHeight();
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					displayedImages.add(imageUri);
				}
				FadeInBitmapDisplayer.animate(imageView, 500);
			}
		}
	}

	public void initImageLoader() {
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.empty_img)
				.showImageForEmptyUri(R.drawable.empty_img)
				.showImageOnFail(R.drawable.empty_img).cacheInMemory(true)
				.cacheOnDisc(true).build();
	}

}
