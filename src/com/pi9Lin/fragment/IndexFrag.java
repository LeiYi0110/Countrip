package com.pi9Lin.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pi9Lin.activity.ListViewActivity;
import com.pi9Lin.activity.YouDetailActivity;
import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.bins.VerticalScrollView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.IndexFragFocusImage;
import com.pi9Lin.data.IndexFragTheme;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.data.WeatherInfo;
import com.pi9Lin.database.MyDB;
import com.pi9Lin.imageLoader.ImageDownLoader;
import com.pi9Lin.imageLoader.ImageDownLoader.onImageLoaderListener;
import com.pi9Lin.navi.NaviStartActivity;
import com.pi9Lin.pulltorefresh.pullableview.PullToRefreshLayout;
import com.pi9Lin.pulltorefresh.pullableview.RefreshListener;
import com.pi9Lin.search.CitySelectActivity;
import com.pi9Lin.search.SearchActivity;
import com.pi9Lin.utils.ACache;
import com.pi9Lin.utils.MesureHightUtils;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class IndexFrag extends BaseFragment {
	
	/**
	 * 加入主题栏目分页
	 * */
	
	private View view;
	private SharedPreferences preferences;
	private ViewPager vPager;
	private MyListView round_hotspots;
	private RelativeLayout serchbar;
	private RelativeLayout telephone;
	private LinearLayout to_locate;
	private LinearLayout sleep;
	private LinearLayout eat;
	private LinearLayout play;
	private LinearLayout zixun;
	private LinearLayout ditu;
	private LinearLayout hehe;
	private TextView index_txt;
	private TextView txt_weather;
	private TextView location;
	private ImageView img_weather;
	private MyAdapter adapter;
	private MyAprr apr;
	private VerticalScrollView myvs;
	// 主题栏目图片
	String[][] themeUrls;
	// 计时器
	Timer timer;
	// 提示等待
	Dialog dialog;
	// 天气数据
	private WeatherInfo weather;

	/** 默认区域id 和 省 id */
	int area_id = 1952;
	int province = 19;

	private List<IndexFragFocusImage> focus_images;

	private List<IndexFragTheme> themes = new ArrayList<IndexFragTheme>(); // 动态获取的首页特色栏目数据

	private ImageDownLoader mImageDownLoader;

	private ACache mCache;

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (focus_images.size() > 0) {
					/** 轮播图自动播放 */
					int ii = vPager.getCurrentItem() + 1;
					if (ii == focus_images.size()) {
						ii = 0;
					}
					vPager.setCurrentItem(ii);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private String districtf;

	private View footer;
	
	private boolean finish = true;// 是否加载完成
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** 首页开始 */
		context = getActivity();
		preferences = getConfig();
		mCache = ACache.get(context);
		mImageDownLoader = new ImageDownLoader(context);
		view = inflater.inflate(R.layout.fragment_index, container, false);
		footer = LayoutInflater.from(context).inflate(R.layout.footer, null);
//		dialog = new Dialog(context, R.style.Dialog_Fullscreen);
//		dialog.setContentView(R.layout.layout_index_adpopuwin);
//		dialog.setCanceledOnTouchOutside(false);
//		dialog.show();
		findById();
		initId(); // 有网、允许定位则刷新 否则为默认
		setOnClickListener();
		initWeather();
		downJsonById();
		return view;
	}

	/**
	 * 天气查询
	 * */
	private void initWeather() {
		String geoLng = preferences.getString("geoLng", null);
		String geoLat = preferences.getString("geoLat", null);
		if (geoLng != null && geoLat != null) {
			String gps = geoLng + "," + geoLat;
			String url = "http://api.map.baidu.com/telematics/v3/weather?location="
					+ gps + "&output=json&ak=KSIg8tOZeMeWOy8sRIDMOXHT";
			AsyncHttpClient client = new AsyncHttpClient();
			client.get(url, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, String arg1) {
					try {
						weather = getWeatherInfo(arg1);
						showInfo();
					} catch (Exception e) {
						Log.d("百度天气错误1", e.getMessage());
					}
					super.onSuccess(arg0, arg1);
				}

				@Override
				public void onFailure(Throwable arg0, String arg1) {
					// TODO Auto-generated method stub
					Log.d("百度天气错误2", "获取天气数据失败");
					super.onFailure(arg0, arg1);
				}
			});
		} else {
			// 未获取定位 要求定位
			msg("需要您手动开启定位");
		}
	}

	protected void showInfo() {
		String citynm = stringToCitynm(weather.getCurrentCity());
		Map<String, String> map = weather.getWeather_data().get(0);
		String weatherInfo = map.get("weather");
		String temperature = map.get("temperature");
		txt_weather.setText(citynm + " " + weatherInfo + " " + temperature);
		String imgUrl = weather.getWeather_data().get(0).get("dayPictureUrl");
		Bitmap bitmap = mImageDownLoader.downloadImage(imgUrl,
				new onImageLoaderListener() {
					@Override
					public void onImageLoader(Bitmap bitmap, String url) {
						if (img_weather != null && bitmap != null) {
							img_weather.setImageBitmap(bitmap);
						}
					}
				});
		if (bitmap != null) {
			BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
			img_weather.setBackground(bd);
		} else {
			img_weather.setImageDrawable(getImgResource(R.drawable.empty_img));
		}
	}

	// 解析百度天气json
	private WeatherInfo getWeatherInfo(String string) throws Exception {
		WeatherInfo weatherInfo = new WeatherInfo();
		JSONObject jsonObject = new JSONObject(string);
		JSONArray jsonArray = jsonObject.getJSONArray("results");
		JSONObject jsonObject2 = (JSONObject) jsonArray.opt(0);
		weatherInfo.setCurrentCity(jsonObject2.getString("currentCity"));
		weatherInfo.setPm25(jsonObject2.getString("pm25"));
		// 解析index项
		JSONArray array1 = jsonObject2.getJSONArray("index");
		for (int i = 0; i < array1.length(); i++) {
			Map<String, String> map = new HashMap<String, String>();
			JSONObject oj = ((JSONObject) array1.opt(i));
			map.put("title", oj.getString("title"));
			map.put("zs", oj.getString("zs"));
			map.put("tipt", oj.getString("tipt"));
			map.put("des", oj.getString("des"));
			weatherInfo.getIndex().add(map);
		}
		// 解析weather_data项
		JSONArray array2 = jsonObject2.getJSONArray("weather_data");
		for (int i = 0; i < array2.length(); i++) {
			Map<String, String> map = new HashMap<String, String>();
			JSONObject oj = ((JSONObject) array2.opt(i));
			map.put("date", oj.getString("date"));
			map.put("dayPictureUrl", oj.getString("dayPictureUrl"));
			map.put("nightPictureUrl", oj.getString("nightPictureUrl"));
			map.put("weather", oj.getString("weather"));
			map.put("wind", oj.getString("wind"));
			map.put("temperature", oj.getString("temperature"));
			weatherInfo.getWeather_data().add(map);
		}
		return weatherInfo;
	}

	private void timer() {
		// 计时器
		TimerTask task = new TimerTask() {
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);
			}
		};
		timer = new Timer(true);
		timer.schedule(task, 3000, 3000);
	}

	private void initLBT() {
		LayoutInflater li = LayoutInflater.from(context);
		final int length = focus_images.size();
		List<View> list = new ArrayList<View>();
		for (int i = 0; i < length; i++) {
			View p = li.inflate(R.layout.item_indexfrag_pages, null);
			final int position = i;
			list.add(p);
			final ImageView image = (ImageView) p.findViewById(R.id.image);
			image.setBackground(getImgResource(R.drawable.empty_img));
			image.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String en_id = focus_images.get(position).getEntity_id();
					int en_type = focus_images.get(position).getEntity_type();
					String stamp = null;
					if (en_type == 1) {
						stamp = "hotel";
					} else if (en_type == 2) {
						stamp = "restaurant";
					} else if (en_type == 3) {
						stamp = "sights";
					}
					Intent intent = new Intent(context, YouDetailActivity.class);
					intent.putExtra("_id", en_id);
					intent.putExtra("stamp", stamp);
					intent.putExtra("en_type", en_type);
					startActivity(intent);
					Activity context = getActivity();
					context.overridePendingTransition(R.anim.slide_left_in,
							R.anim.slide_left_out);
				}
			});

			if (i == 0) {
				String url = focus_images.get(i).getCover();
				Bitmap bitmap = mImageDownLoader.downloadImage(url,
						new onImageLoaderListener() {
							@Override
							public void onImageLoader(Bitmap bitmap, String url) {
								if (bitmap != null) {
									BitmapDrawable bd = new BitmapDrawable(
											getResources(), bitmap);
									image.setBackground(bd);
								}
							}
						});
				if (bitmap != null) {
					BitmapDrawable bd = new BitmapDrawable(getResources(),
							bitmap);
					image.setBackground(bd);
				}
			}
		}
		// 创建适配器， 把组装完的组件传递进去
		adapter = new MyAdapter(focus_images, list);
		vPager.setAdapter(adapter);
	}

	/**
	 * 按县省id获取首页数据
	 * */
	private void downJsonById() {
		String url = "http://www.xiangyouji.com.cn:3000/home/area_id/"
				+ area_id + "/province/" + province + "";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String result) {
				try {
					mCache.put("mapPoints", result); // 储存县周边的点
					getFocusJson(result);// 解析轮播图json
					initThemes(result); // 解析栏目json
//					dialog.dismiss();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Log.d("text", "首页json获取失败");
//				dialog.dismiss();
				super.onFailure(arg0, arg1);
			}
		});
	}

	/**
	 * 主题栏目listview适配器
	 * */
	private class MyAprr extends BaseAdapter implements OnScrollListener {

		LayoutInflater li = LayoutInflater.from(context);

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return themes.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return themes.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v;
			Holder h;
			if (arg1 == null) {
				v = li.inflate(R.layout.item_index_hotspot, null);
				h = new Holder();
				h.pager = (ViewPager) v.findViewById(R.id.remenpic);
				v.setTag(h);
			} else {
				v = arg1;
				h = (Holder) v.getTag();
			}
			final int index = arg0;
			int l = themes.get(arg0).getEntities().size() + 1;
			List<View> list = new ArrayList<View>();
			for (int i = 0; i < l; i++) {
				final int position = i;
				View p = li.inflate(R.layout.item_indexfrag_pages, null);
				list.add(p);
				final ImageView image = (ImageView) p.findViewById(R.id.image);
				image.setBackground(getImgResource(R.drawable.empty_img));
				if (i == 0) {
					((TextView) p.findViewById(R.id.remenspotf)).setText(themes
							.get(arg0).getTitle());
				}
				if (i > 0) {
					image.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							String en_id = themes.get(index).getEntities()
									.get(position - 1).getEntity_id();
							int en_type = themes.get(index).getEntities()
									.get(position - 1).getEntity_type();
							double[] gps = themes.get(index).getEntities()
									.get(position - 1).getGps();
							String stamp = null;
							if (en_type == 1) {
								stamp = "hotel";
							} else if (en_type == 2) {
								stamp = "restaurant";
							} else if (en_type == 3) {
								stamp = "sights";
							}
							Intent intent = new Intent(context,
									YouDetailActivity.class);
							intent.putExtra("_id", en_id);
							intent.putExtra("stamp", stamp);
							intent.putExtra("en_type", en_type);
							intent.putExtra("latitude", gps[0]);
							intent.putExtra("longitude", gps[1]);
							startActivity(intent);
							Activity context = getActivity();
							context.overridePendingTransition(
									R.anim.slide_left_in, R.anim.slide_left_out);
						}
					});
				}

				String url = null;
				if (i == 0) {
					url = themes.get(index).getCover();
				} else if (i > 0) {
					url = themes.get(index).getEntities().get(i - 1).getUrl();
				}
				image.setTag(url);
				// 读取图片缓存 因为读取的可能是文件缓存 所以要在异步里面执行 但是会影响程序
				String subUrl = url.replaceAll("[^\\w]", "");
				if (mImageDownLoader.getBitmapFromMemCache(subUrl) != null) {
					// 先缓存
					Bitmap b = mImageDownLoader.getBitmapFromMemCache(subUrl);
					image.setImageBitmap(b);
				} else {
					image.setImageDrawable(getImgResource(R.drawable.empty_img));
				}
			}

			// 创建适配器， 把组装完的组件传递进去
			MyApt apt = new MyApt(arg0, list);
			h.pager.setAdapter(apt);
			h.pager.setCurrentItem(0);
			final int dd = arg0;
			final List<View> ss = list;
			// 绑定动作监听器：如翻页的动画
			h.pager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(final int argf) {
					// TODO Auto-generated method stub
					if (argf > 0) {
						Bitmap bitmap = mImageDownLoader.downloadImage(themes
								.get(dd).getEntities().get(argf).getUrl(),
								new onImageLoaderListener() {
									@Override
									public void onImageLoader(Bitmap bitmap,
											String url) {
										if (bitmap != null) {
											((ImageView) ss.get(argf)
													.findViewById(R.id.image))
													.setImageBitmap(bitmap);
										}
									}
								});
						if (bitmap != null) {
							((ImageView) ss.get(argf).findViewById(R.id.image))
									.setImageBitmap(bitmap);
						}
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
			apt.notifyDataSetChanged();
			return v;
		}

		class Holder {
			ViewPager pager;
		}

		// 每次加载10条数据
		private int pageSize = 10;
		int totalCount = 10; // 从第11条开始加载
		private boolean scrolled = false;// 是否加载完成
		private boolean firstIn;
		private int start;
		private int end;
		
		/**
		 * ListView滚动的时候调用的方法，刚开始显示ListView也会调用此方法
		 */
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub
			start = firstVisibleItem;
			end = visibleItemCount;
			if (firstIn && visibleItemCount > 0) {
				// TODO：visibleItemCount > 0 这个条件不能少，不然预加载没效果！！！
				// 首次进入页面，并且界面上面的Item已经加载出来了，才去加载一个屏幕Item的的图片
				showImage(start, end, view);
				firstIn = false;
			}
			// 当翻到最后一条数据时
			if (scrolled
					&& round_hotspots.getCount() == 0
					&& finish
					|| scrolled
					&& round_hotspots.getChildAt(round_hotspots
							.getLastVisiblePosition()
							- round_hotspots.getFirstVisiblePosition()) != null
					&& round_hotspots.getChildAt(
							round_hotspots.getLastVisiblePosition()
									- round_hotspots.getFirstVisiblePosition())
							.getBottom() <= round_hotspots.getMeasuredHeight()
					&& finish) {

				// 已经移动到了listview的最后
				finish = false;
				// 添加页脚
				round_hotspots.addFooterView(footer);
				String url = "http://www.xiangyouji.com.cn:3000/home/area_id/"
						+ area_id + "/province/" + province + "/startIndex/" + totalCount
						+ "/length/" + pageSize;
				MyTaskf myTaskf = new MyTaskf();
				myTaskf.execute(url);
				totalCount += pageSize;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				scrolled = true;
			}
			if (scrollState == SCROLL_STATE_IDLE && finish) {
				showImage(start, end, view);
			} else {
				mImageDownLoader.cancelTask();
			}
		}
	}
	
	private class MyTaskf extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			SystemClock.sleep(1500);// 休眠1秒 模拟等待

			try {
				HttpClient client = new DefaultHttpClient();
				client.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 1000 * 15);
				HttpGet get = new HttpGet(arg0[0]);
				HttpResponse response;

				response = client.execute(get);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					return inputStreamToString(entity.getContent());
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

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if (result != null) {
				/** 成功获取数据 */
				try {
					if (checkJson(result)) {
						themes.addAll(getThemesJson(result));// 已经得到一些数据
						// 让listview自动刷新
						apr.notifyDataSetChanged();
						finish = true;
					}
					// 去掉页脚
					round_hotspots.removeFooterView(footer);
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
				}
			}
			super.onPostExecute(result);
		}
	}
	/**
	 * 异步下载网络图片 在分页的时候 首次进入加载一次
	 */
	private void showImage(int firstVisibleItem, int visibleItemCount,
			AbsListView v) {
		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
			String url = themes.get(i).getCover();
			final ImageView image = (ImageView) v.findViewWithTag(url);
			Bitmap bitmap = mImageDownLoader.downloadImage(url,
					new onImageLoaderListener() {
						@Override
						public void onImageLoader(Bitmap bitmap, String url) {
							if (bitmap != null) {
								image.setImageBitmap(bitmap);
							}
						}
					});
			if (bitmap != null) {
				image.setImageBitmap(bitmap);
			}
		}
	}
	
	/**
	 * 显示主题栏目
	 * */
	protected void initThemes(String result) {
		try {
			themes=getThemesJson(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 填充listview数据
		apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
		round_hotspots.setAdapter(apr);
		MesureHightUtils.setListViewHeightBasedOnChildren(round_hotspots);
		/** 设置listView包裹的外部布局高度 */
		int dd = MesureHightUtils
				.setListViewHeightBasedOnChildren1(round_hotspots);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, dd);// 设置最后一项分隔线消失
		hehe.setLayoutParams(lp);
		round_hotspots.setFocusable(false);
		myvs.smoothScrollTo(0, 0);
	}

	/**
	 * 解析栏目json
	 * */
	protected List<IndexFragTheme> getThemesJson(String result) throws Exception {
		List<IndexFragTheme> datas=new ArrayList<IndexFragTheme>();
		JSONObject jsonObject = new JSONObject(result);
		JSONObject jsonObject2 = jsonObject.getJSONObject("data");
		/** 获取全省精选的吃、住、游三个主题栏目 */
		JSONArray array = jsonObject2.getJSONArray("themes");
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonObject3 = (JSONObject) array.opt(i);
			String cover = jsonObject3.getString("cover");
			int order = jsonObject3.getInt("order");
			String title = jsonObject3.getString("title");
			JSONArray array2 = jsonObject3.getJSONArray("entities");
			List<Entity> entities = new ArrayList<Entity>();
			for (int j = 0; j < array2.length(); j++) {
				JSONObject jsonObject5 = (JSONObject) array2.opt(j);
				String url = jsonObject5.getString("url");
				String entity_id = jsonObject5.getString("entity_id");
				int orderf = jsonObject5.getInt("order");
				int entity_type = jsonObject5.getInt("entity_type");
				JSONObject jsonObject4 = jsonObject5.getJSONObject("gps");
				double latitude = jsonObject4.getDouble("latitude");
				double longitude = jsonObject4.getDouble("longitude");
				double[] gps = { latitude, longitude };
				JSONArray array3 = jsonObject5.getJSONArray("images");
				List<SleepImgData> imgDatas = new ArrayList<SleepImgData>();
				for (int k = 0; k < array3.length(); k++) {
					SleepImgData imgData = new SleepImgData();
					JSONObject jsonObject6 = (JSONObject) array3.opt(j);
					String urll = jsonObject6.getString("url");
					String update_time = jsonObject6.getString("update_time");
					String create_time = jsonObject6.getString("create_time");
					int order_no = jsonObject6.getInt("order");
					imgData.setUrl(urll);
					imgData.setUpdate_time(update_time);
					imgData.setCreate_time(create_time);
					imgData.setOrder_no(order_no);
					imgDatas.add(imgData);
				}
//				Entity entity = new Entity(url, entity_id, orderf, entity_type,
//						imgDatas, gps);
//				entities.add(entity);
			}
			IndexFragTheme theme = new IndexFragTheme(cover, order, title,
					entities);
			datas.add(theme);
		}
		return datas;
	}

	/**
	 * 解析轮播图json
	 * */
	protected void getFocusJson(String result) throws Exception {
		focus_images = new ArrayList<IndexFragFocusImage>();
		JSONObject jsonObject = new JSONObject(result);
		JSONObject jsonObject2 = jsonObject.getJSONObject("data");
		/** 得到首页轮播图 */
		JSONArray array3 = jsonObject2.getJSONArray("focus_images");
		for (int i = 0; i < array3.length(); i++) {
			JSONObject jsonObject3 = (JSONObject) array3.opt(i);
			String _id = jsonObject3.getString("_id");
			String address = jsonObject3.getString("address");
			String name = jsonObject3.getString("name");
			int entity_type = jsonObject3.getInt("entity_type");
			String cover = jsonObject3.getString("cover");
			String entity_id = jsonObject3.getString("entity_id");
			JSONObject object = jsonObject3.getJSONObject("gps");
			double latitude = object.getDouble("latitude");
			double longitude = object.getDouble("longitude");
			double[] gps = { latitude, longitude };
			IndexFragFocusImage focusImage = new IndexFragFocusImage(_id, name,
					entity_type, cover, address, entity_id, gps);
			focus_images.add(focusImage);
		}
		initLBT();
		vPager.setCurrentItem(0);
		if (focus_images.size() > 0) {
			index_txt.setText("1/" + focus_images.size());
		}
		// 自动播放
		timer();
		// 绑定动作监听器：如翻页的动画
		vPager.setOnPageChangeListener(new MyListener());
	}

	private void initId() {
		districtf = preferences.getString("district", null);
		if (districtf == null) {
			districtf = "福田区";
		} else {
			// 定位成功了
			MyDB myDB = new MyDB(context);
			SQLiteDatabase db = myDB.getWritableDatabase();
			Cursor cursor = db.rawQuery("select * from area where area_name=?",
					new String[] { districtf });// 有可能定位的区名字和本地名字不符 以至找不到
			if (cursor.moveToFirst()) {
				area_id = cursor.getInt(cursor.getColumnIndex("area_id"));
				province = cursor.getInt(cursor.getColumnIndex("provinceId"));
			} else {
				// 使用默认
				Log.d("区名错误", "区名匹配出错，使用默认");
				districtf = "福田区";
			}
			cursor.close();
			db.close();
			myDB.close();
		}
		// 把定位显示到界面
		location.setText(districtf);
	}

	/**
	 * 每个主题item的viewpage适配器
	 * */
	private class MyApt extends PagerAdapter {

		private View p;
		private ImageView image;
		private int index;
		private int mChildCount = 0;
		private List<View> list;

		/**
		 * 构造函数
		 * */
		public MyApt(int index, List<View> list) {
			// TODO Auto-generated constructor stub
			this.index = index;
			this.list = list;
		}

		@Override
		public void notifyDataSetChanged() {
			mChildCount = getCount();
			super.notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object) {
			if (mChildCount > 0) {
				mChildCount--;
				return POSITION_NONE;
			}
			return super.getItemPosition(object);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (themes.size() == 0) {
				return 0;
			}
			return (themes.get(index).getEntities().size() + 1);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// container.removeView(p);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			// 填充viewpage数据
			// p = li.inflate(R.layout.item_indexfrag_pages, null);
			// image = (ImageView) p.findViewById(R.id.image);
			// image.setBackground(getImgResource(R.drawable.empty_img));
			// if (position == 0) {
			// ((TextView) p.findViewById(R.id.remenspotf)).setText(themes
			// .get(index).getTitle());
			// }
			// if (position > 0) {
			// image.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View arg0) {
			// // TODO Auto-generated method stub
			// String en_id =
			// themes.get(index).getEntities().get(position-1).getEntity_id();
			// int en_type =
			// themes.get(index).getEntities().get(position-1).getEntity_type();
			// double[] gps =
			// themes.get(index).getEntities().get(position-1).getGps();
			// String stamp = null;
			// if (en_type == 1) {
			// stamp = "hotel";
			// } else if (en_type == 2) {
			// stamp = "restaurant";
			// } else if (en_type == 3) {
			// stamp = "sights";
			// }
			// Intent intent = new Intent(context,
			// YouDetailActivity.class);
			// intent.putExtra("_id", en_id);
			// intent.putExtra("stamp", stamp);
			// intent.putExtra("en_type", en_type);
			// intent.putExtra("latitude", gps[0]);
			// intent.putExtra("longitude", gps[1]);
			// startActivity(intent);
			// }
			// });
			// }
			p = list.get(position);
			if (position > 0) {
				image = (ImageView) p.findViewById(R.id.image);
				String url = themes.get(index).getCover();
				if (position > 0) {
					url = themes.get(index).getEntities().get(position - 1)
							.getUrl();
				}
				Bitmap bitmap = mImageDownLoader.downloadImage(url,
						new onImageLoaderListener() {
							@Override
							public void onImageLoader(Bitmap bitmap, String url) {
								if (bitmap != null) {
									BitmapDrawable bd = new BitmapDrawable(
											getResources(), bitmap);
									image.setBackground(bd);
									container.removeView(p);
									container.addView(p);
								}
							}
						});

				if (bitmap != null) {
					BitmapDrawable bd = new BitmapDrawable(getResources(),
							bitmap);
					image.setBackground(bd);
					container.removeView(p);
					container.addView(p);
				}

			} else {
				container.removeView(p);
				container.addView(p);
			}

			return p;
		}

	}

	/**
	 * 轮播图viewpager适配器
	 */
	private class MyAdapter extends PagerAdapter {

		private List<IndexFragFocusImage> data;
		private View p;
		private ImageView image;
		private int mChildCount = 0;
		private List<View> list;

		/**
		 * 构造函数
		 * */
		public MyAdapter(List<IndexFragFocusImage> data, List<View> list) {
			this.data = data;
			this.list = list;
		}

		@Override
		public void notifyDataSetChanged() {
			mChildCount = getCount();
			super.notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object) {
			if (mChildCount > 0) {
				mChildCount--;
				return POSITION_NONE;
			}
			return super.getItemPosition(object);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// container.removeView(p);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {

			// 填充viewpage数据
			p = list.get(position);
			image = (ImageView) p.findViewById(R.id.image);
			// p = li.inflate(R.layout.item_indexfrag_pages, null);
			// image = (ImageView) p.findViewById(R.id.image);
			// image.setBackground(getImgResource(R.drawable.empty_img));
			// image.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View arg0) {
			// // TODO Auto-generated method stub
			// String en_id = data.get(position).getEntity_id();
			// int en_type = data.get(position).getEntity_type();
			// String stamp = null;
			// if (en_type == 1) {
			// stamp = "hotel";
			// } else if (en_type == 2) {
			// stamp = "restaurant";
			// } else if (en_type == 3) {
			// stamp = "sights";
			// }
			// Intent intent = new Intent(context, YouDetailActivity.class);
			// intent.putExtra("_id", en_id);
			// intent.putExtra("stamp", stamp);
			// intent.putExtra("en_type", en_type);
			// startActivity(intent);
			// }
			// });
			if (position > 0) {
				Bitmap bitmap = mImageDownLoader.downloadImage(
						data.get(position).getCover(),
						new onImageLoaderListener() {
							@Override
							public void onImageLoader(Bitmap bitmap, String url) {
								if (bitmap != null) {
									BitmapDrawable bd = new BitmapDrawable(
											getResources(), bitmap);
									image.setBackground(bd);
									container.removeView(p);
									container.addView(p);
								}
							}
						});

				if (bitmap != null) {
					BitmapDrawable bd = new BitmapDrawable(getResources(),
							bitmap);
					image.setBackground(bd);
					container.removeView(p);
					container.addView(p);
				}
			} else {
				container.removeView(p);
				container.addView(p);
			}
			return p;
		}
	}

	/**
	 * 动作监听器
	 */
	private class MyListener implements OnPageChangeListener {
		@Override
		public void onPageScrollStateChanged(int state) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int position) {
			index_txt.setText((position + 1) + "/" + focus_images.size());
		}
	}

	private void findById() {
		vPager = (ViewPager) view.findViewById(R.id.vPager);
		round_hotspots = (MyListView) view.findViewById(R.id.round_hotspots);
		to_locate = (LinearLayout) view.findViewById(R.id.to_locate);
		sleep = (LinearLayout) view.findViewById(R.id.sleep);
		eat = (LinearLayout) view.findViewById(R.id.eat);
		play = (LinearLayout) view.findViewById(R.id.play);
		zixun = (LinearLayout) view.findViewById(R.id.zixun);
		ditu = (LinearLayout) view.findViewById(R.id.ditu);
		hehe = (LinearLayout) view.findViewById(R.id.hehe);
		index_txt = (TextView) view.findViewById(R.id.index_txt);
		txt_weather = (TextView) view.findViewById(R.id.txt_weather);
		location = (TextView) view.findViewById(R.id.location);
		serchbar = (RelativeLayout) view.findViewById(R.id.serchbar);
		telephone = (RelativeLayout) view.findViewById(R.id.telephone);
		img_weather = (ImageView) view.findViewById(R.id.img_weather);
		myvs = (VerticalScrollView) view.findViewById(R.id.myvs);
		// 直接定义了下拉刷新的监听
		((PullToRefreshLayout) view.findViewById(R.id.refresh_view))
				.setOnRefreshListener(new RefreshListener() {
					@Override
					public void onRefresh(
							final PullToRefreshLayout pullToRefreshLayout) {
						// TODO Auto-generated method stub
						List<Area> aaa = getAreaInfos();
						for (Area area : aaa) {
							if (area.getArea_id() == area_id) {
								location.setText(area.getArea_name());
							}
						}
						if (timer != null) {
							timer.cancel();
							timer = null;
						}
						mImageDownLoader.cancelTask();
						String url = "http://www.xiangyouji.com.cn:3000/home/area_id/"
								+ area_id + "/province/" + province + "";
						AsyncHttpClient client = new AsyncHttpClient();
						client.get(url, new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(int arg0, String result) {
								try {
									mCache.put("mapPoints", result); // 储存县周边的点
									getFocusJson(result);// 解析轮播图json
									getThemesJson(result); // 解析栏目json
									new Handler() {
										@Override
										public void handleMessage(Message msg) {
											// 千万别忘了告诉控件刷新完毕了哦！
											pullToRefreshLayout
													.refreshFinish(PullToRefreshLayout.SUCCEED);
										}
									}.sendEmptyMessageDelayed(0, 1000);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

							@Override
							public void onFailure(Throwable arg0, String arg1) {
								new Handler() {
									@Override
									public void handleMessage(Message msg) {
										// 千万别忘了告诉控件刷新完毕了哦！
										pullToRefreshLayout
												.refreshFinish(PullToRefreshLayout.FAIL);
									}
								}.sendEmptyMessageDelayed(0, 1000);
								super.onFailure(arg0, arg1);
							}
						});

						super.onRefresh(pullToRefreshLayout);
					}
				});
	}

	private void setOnClickListener() {
		/**
		 * 住山庄页面
		 * */
		sleep.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, ListViewActivity.class);
				intent.putExtra("stamp", "hotel");
				intent.putExtra("en_type", 1);
				intent.putExtra("area_id", area_id);
				startActivity(intent);
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		/**
		 * 吃乡味页面
		 * */
		eat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, ListViewActivity.class);
				intent.putExtra("stamp", "restaurant");
				intent.putExtra("en_type", 2);
				intent.putExtra("area_id", area_id);
				startActivity(intent);
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		/**
		 * 游乡景页面
		 * */
		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(context, ListViewActivity.class);
				intent.putExtra("stamp", "sights");
				intent.putExtra("en_type", 3);
				intent.putExtra("area_id", area_id);
				startActivity(intent);
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		/**
		 * 资讯页面
		 * */
		zixun.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				msg("资讯");
			}
		});
		/**
		 * 地图
		 * */
		ditu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context, NaviStartActivity.class));
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		/**
		 * 地理位置
		 * */
		to_locate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, CitySelectActivity.class);
				intent.putExtra("locate", districtf);
				startActivityForResult(intent, 7);
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		/**
		 * 搜索
		 * */
		serchbar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context, SearchActivity.class));
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		/**
		 * 打电话
		 * */
		telephone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String phone = "0755-88283088";
				Intent phoneIntent = new Intent("android.intent.action.CALL",
						Uri.parse("tel:" + phone));
				startActivity(phoneIntent);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
			area_id = data.getIntExtra("area_id", -1);
			province = data.getIntExtra("province", -1);
			String area_name = data.getStringExtra("area_name");
			location.setText(area_name); // 定位？？
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			mImageDownLoader.cancelTask();
			round_hotspots.setAdapter(null);
			downJsonById();
		}
	}

	@Override
	public void onStop() {
		mImageDownLoader.cancelTask();
		super.onStop();
	}

}
