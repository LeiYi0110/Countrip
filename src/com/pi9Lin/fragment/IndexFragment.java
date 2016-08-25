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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.pi9Lin.activity.DetailActivity;
import com.pi9Lin.activity.ListActivity;
import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.IndexFragFocusImage;
import com.pi9Lin.data.IndexFragTheme;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.data.WeatherInfo;
import com.pi9Lin.database.MyDB;
import com.pi9Lin.navi.NaviStartActivity;
import com.pi9Lin.pulltorefresh.pullableview.PullToRefreshLayout;
import com.pi9Lin.pulltorefresh.pullableview.RefreshListener;
import com.pi9Lin.search.CitySelectActivity;
import com.pi9Lin.search.SearchActivity;
import com.pi9Lin.utils.ACache;

@SuppressLint("HandlerLeak")
public class IndexFragment extends BaseFragment {

	private View view;
	private MyListView mlist;
	private MyBaseApr myBaseApr;
	private MyViewPageApr pageApr;
	private List<View> pages;
	private Timer timer; // 计时器
	private ViewPager vPager;
	private TextView vPager_index;
	private int area_id = 1952; // 默认区域id 和 省id
	private int province = 19;
	private ACache mCache;
	private List<IndexFragFocusImage> focus_images;
	private LayoutInflater li;
	private WeatherInfo weather; // 天气数据
	private TextView txt_weather;
	private ImageView img_weather;
	private LinearLayout to_locate;
	private List<IndexFragTheme> themes; // 动态获取的首页特色栏目数据
	private String districtf;
	private TextView location;
	private RelativeLayout serchbar;
	private RelativeLayout telephone;
	private RelativeLayout top;
	private RelativeLayout gotop;
	private PullToRefreshLayout ptrl;
	private boolean finish = true;// 是否加载完成
	private View footer;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (pages.size() > 0) {
					/** 轮播图自动播放 */
					int ii = vPager.getCurrentItem() + 1;
					if (ii == pages.size()) {
						ii = 0;
					}
					vPager.setCurrentItem(ii);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** 首页开始 */
		context = getActivity();
		preferences = getConfig();
		view = inflater.inflate(R.layout.fragment_indexf, container, false);
		mCache = ACache.get(context);
		li = LayoutInflater.from(context);
		footer = li.inflate(R.layout.footer, null);
		initImageLoader(); // 初始化图片异步处理对象
		findById(); // 资源初始化
		init(); // 初始化数据
		setOnClickListener();
		initPullToRefresh();
		initList(); // 填充页面
		downJsonById(); // 下载json 更新页面
		return view;
	}

	private void initPullToRefresh() {
		// TODO Auto-generated method stub
		ptrl.setTop(top);
		ptrl.setOnRefreshListener(new RefreshListener() {
			@Override
			public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
				// TODO Auto-generated method stub
				try {
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
					initList(); // 填充页面
					downJsonById(); // 下载json 更新页面
					new Handler() {
						@Override
						public void handleMessage(Message msg) {
							// 千万别忘了告诉控件刷新完毕了哦！
							pullToRefreshLayout
									.refreshFinish(PullToRefreshLayout.SUCCEED);
						}
					}.sendEmptyMessageDelayed(0, 500);
				} catch (Exception e) {
					// TODO: handle exception
				}
				super.onRefresh(pullToRefreshLayout);
			}
		});
	}

	private void init() {
		// TODO Auto-generated method stub
		districtf = preferences.getString("district", null);
		if (districtf == null) {
			districtf = "福田区";
		} else {
			// 定位成功了 有网
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

	private void setOnClickListener() {
		// TODO Auto-generated method stub
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
		/**
		 * 滑动到顶
		 * */
		gotop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (!mlist.isStackFromBottom()) {
//					mlist.setStackFromBottom(true);
					mlist.smoothScrollToPosition(0);
				}
				mlist.setStackFromBottom(false);
				gotop.setVisibility(View.GONE);
			}
		});
		gotop.setVisibility(View.GONE);
	}

	private void downJsonById() {
		// TODO Auto-generated method stub
		String url = "http://www.xiangyouji.com.cn:3000/home/area_id/"
				+ area_id + "/province/" + province + "/startIndex/0/length/10";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String result) {
				try {
					mCache.put("mapPoints", result); // 储存县周边的点
					getFocusJson(result); // 解析轮播图json
					themes = getThemesJson(result); // 解析栏目json
					Log.d("dddd", themes.get(0).getEntities().get(0).getImages().size()+"");
					myBaseApr.notifyDataSetChanged();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Log.d("哈哈2", e.getMessage());
				}
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Log.d("text", "网络失败");
				super.onFailure(arg0, arg1);
			}
		});
	}

	private void initList() {
		// TODO Auto-generated method stub
		pages = new ArrayList<View>();
		themes = new ArrayList<IndexFragTheme>();
		// 增加listview的页脚必须在设置适配器之前，需要提前设置一次
		mlist.addFooterView(footer);
		myBaseApr = new MyBaseApr();
		mlist.setAdapter(myBaseApr);
		// 然后再次将页脚删除掉
		mlist.removeFooterView(footer);
	}

	private void findById() {
		// TODO Auto-generated method stub
		mlist = (MyListView) view.findViewById(R.id.lv);
		to_locate = (LinearLayout) view.findViewById(R.id.to_locate);
		location = (TextView) view.findViewById(R.id.location);
		serchbar = (RelativeLayout) view.findViewById(R.id.serchbar);
		telephone = (RelativeLayout) view.findViewById(R.id.telephone);
		top = (RelativeLayout) view.findViewById(R.id.ab);
		gotop = (RelativeLayout) view.findViewById(R.id.gotop);
		ptrl = (PullToRefreshLayout) view.findViewById(R.id.refresh_view);
	}

	private class MyBaseApr extends BaseAdapter implements OnScrollListener {
		/**
		 * ListView包含不同Item的布局 　　我们需要做这些工作: 　　1）重写 getViewTypeCount() –
		 * 该方法返回多少个不同的布局 　　2）重写 getItemViewType(int) – 根据position返回相应的Item 　
		 * 3）根据view item的类型，在getView中创建正确的convertView
		 * */
		final int TYPE_1 = 0;
		final int TYPE_2 = 1;
		final int TYPE_3 = 2;
		final int TYPE_4 = 3;

		MyBaseApr() {
			mlist.setOnScrollListener(this);
		}

		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			if (position == 0) {
				return TYPE_1;
			} else if (position == 1) {
				return TYPE_2;
			} else if (position == 2) {
				return TYPE_3;
			} else {
				return TYPE_4;
			}
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 4;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 3 + themes.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(final int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v = null;
			final Holder h;
			int type = getItemViewType(arg0);
			if (arg1 == null) {
				h = new Holder();
				if (type == TYPE_1) {
					// 顶部轮播图
					v = li.inflate(R.layout.fragment_index_vpage, null);
					h.vPage = (ViewPager) v.findViewById(R.id.vPage);
					h.xiabiao = (TextView) v.findViewById(R.id.xiabiao);
				} else if (type == TYPE_2) {
					// 天气
					v = li.inflate(R.layout.fragment_index_weather, null);
					h.xiabiao = (TextView) v.findViewById(R.id.txt_weather);
					h.imageView = (ImageView) v.findViewById(R.id.img_weather);
					h.play = (LinearLayout) v.findViewById(R.id.ditu);
				} else if (type == TYPE_3) {
					// 图标
					v = li.inflate(R.layout.fragment_index_tubiao, null);
					h.play = (LinearLayout) v.findViewById(R.id.play);
					h.eat = (LinearLayout) v.findViewById(R.id.eat);
					h.sleep = (LinearLayout) v.findViewById(R.id.sleep);
				} else if (type == TYPE_4 && themes.size() > 0) {
					// 主题栏目
					v = li.inflate(R.layout.item_index_hotspot, null);
					h.vPage = (ViewPager) v.findViewById(R.id.remenpic);
				}
				v.setTag(h);
			} else {
				v = arg1;
				h = (Holder) v.getTag();
			}
			if (type == TYPE_1) {
				pageApr = new MyViewPageApr();
				h.vPage.setAdapter(pageApr);
				vPager = h.vPage;
				vPager_index = h.xiabiao;
			} else if (type == TYPE_2) {
				txt_weather = h.xiabiao;
				img_weather = h.imageView;
				h.play.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						startActivity(new Intent(context,
								NaviStartActivity.class));
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in,
								R.anim.slide_left_out);
					}
				});
			} else if (type == TYPE_3) {
				h.play.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("stamp", "sights");
						intent.putExtra("en_type", 3);
						intent.putExtra("area_id", area_id);
						startActivity(intent);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in,
								R.anim.slide_left_out);
					}
				});
				h.eat.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("stamp", "restaurant");
						intent.putExtra("en_type", 2);
						intent.putExtra("area_id", area_id);
						startActivity(intent);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in,
								R.anim.slide_left_out);
					}
				});
				h.sleep.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("stamp", "hotel");
						intent.putExtra("en_type", 1);
						intent.putExtra("area_id", area_id);
						startActivity(intent);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in,
								R.anim.slide_left_out);
					}
				});
			} else if (type == TYPE_4 && themes.size() > 0) {
				final int fff = arg0 - 3;
				MyThemeVpageApr themeApr = new MyThemeVpageApr(themes.get(fff)
						.getEntities(), fff);
				h.vPage.setAdapter(themeApr);
				h.vPage.setCurrentItem(themes.get(fff).getOrder());
				h.vPage.setOnPageChangeListener(new OnPageChangeListener() {
					@Override
					public void onPageSelected(int arg0) {
						// TODO Auto-generated method stub
						themes.get(fff).setOrder(arg0);
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
			return v;
		}

		private class Holder {
			ViewPager vPage;
			TextView xiabiao;
			LinearLayout play;
			LinearLayout eat;
			LinearLayout sleep;
			ImageView imageView;
		}

		// 每次加载10条数据
		private int pageSize = 10;
		int totalCount = 10; // 从第11条开始加载
		private boolean scrolled = false;// 是否加载完成
		int haha=0;
		
		@Override
		public void onScroll(AbsListView arg0, int firstVisibleItem, int arg2, int arg3) {
			// TODO Auto-generated method stub
			haha=firstVisibleItem;
			if (haha>4) {
				gotop.setVisibility(View.VISIBLE);
			}else{
				gotop.setVisibility(View.GONE);
			}
			if (scrolled
					&& mlist.getCount() == 0
					&& finish
					|| scrolled
					&& mlist.getChildAt(mlist.getLastVisiblePosition()
							- mlist.getFirstVisiblePosition()) != null
					&& mlist.getChildAt(
							mlist.getLastVisiblePosition()
									- mlist.getFirstVisiblePosition())
							.getBottom() <= mlist.getMeasuredHeight() && finish) {
				// 已经移动到了listview的最后
				finish = false;
				// 添加页脚
				mlist.addFooterView(footer);
				String url = "http://www.xiangyouji.com.cn:3000/home/area_id/"
						+ area_id + "/province/" + province + "/startIndex/"
						+ totalCount + "/length/" + pageSize;
				MyTaskf myTaskf = new MyTaskf();
				myTaskf.execute(url);
				totalCount += pageSize;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView arg0, int scrollState) {
			// TODO Auto-generated method stub
			if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				scrolled = true;
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
					if (pageJson(result)) {
						themes.addAll(getThemesJson(result));// 已经得到一些数据
						// 让listview自动刷新
						myBaseApr.notifyDataSetChanged();
						finish = true;
					}
					// 去掉页脚
					mlist.removeFooterView(footer);
				} catch (Exception e) {
					System.out.println("错误...:" + e.getMessage());
				}
			}
			super.onPostExecute(result);
		}
	}

	private boolean pageJson(String arg1) throws Exception {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(arg1);
		int status = jsonObject.getInt("status");
		if (status == 1) {
			JSONObject jsonObject2 = jsonObject.getJSONObject("data");
			JSONArray array = jsonObject2.getJSONArray("themes");
			if (array.length() == 0) {
				msg("没有数据了");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 主题栏目
	 * */
	private class MyThemeVpageApr extends PagerAdapter {

		private int mChildCount = 0;
		private View p;
		private ImageView iv;
		private List<Entity> data;
		private int num;

		MyThemeVpageApr(List<Entity> data, int num) {
			this.data = data;
			this.num = num;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (themes.size() == 0) {
				return 0;
			}
			return data.size() + 1;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
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
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			// 填充viewpage数据
			p = li.inflate(R.layout.item_indexfrag_pages, null);
			iv = (ImageView) p.findViewById(R.id.image);

			if (position == 0) {
				((TextView) p.findViewById(R.id.remenspotf)).setText(themes
						.get(num).getTitle());
				String url = themes.get(num).getCover();
				imageLoader
						.displayImage(url, iv, options, animateFirstListener);
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("order", num);
						startActivity(intent);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in,
								R.anim.slide_left_out);
					}
				});
			} else if (position > 0) {
				String url = themes.get(num).getEntities().get(position - 1)
						.getUrl();
				imageLoader
						.displayImage(url, iv, options, animateFirstListener);
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						String en_id = themes.get(num).getEntities()
								.get(position - 1).getEntity_id();
						int en_type = themes.get(num).getEntities()
								.get(position - 1).getEntity_type();
						double[] gps = themes.get(num).getEntities()
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
								DetailActivity.class);
						intent.putExtra("_id", en_id);
						intent.putExtra("stamp", stamp);
						intent.putExtra("en_type", en_type);
						intent.putExtra("latitude", gps[0]);
						intent.putExtra("longitude", gps[1]);
						startActivity(intent);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in,
								R.anim.slide_left_out);
					}
				});
			}
			container.removeView(p);
			container.addView(p);
			return p;
		}

	}

	private class MyViewPageApr extends PagerAdapter {

		private int mChildCount = 0;
		private View p;
		private ImageView iv;

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return pages.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
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
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
		}

		@Override
		public Object instantiateItem(final ViewGroup container,
				final int position) {
			// 填充viewpage数据
			p = pages.get(position);
			iv = (ImageView) p.findViewById(R.id.image);
			if (position > 0) {
				imageLoader.displayImage(null, iv, options,
						animateFirstListener);
			}
			container.removeView(p);
			container.addView(p);
			return p;
		}
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

	/**
	 * 解析轮播图json
	 * */
	private void getFocusJson(String result) throws Exception {
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
		initWeather();
	}

	/**
	 * 天气查询
	 * */
	private void initWeather() {
		double la = focus_images.get(0).getGps()[0];
		double ln = focus_images.get(0).getGps()[1];
		if (la != 0 && ln != 0) {
			String gps = ln + "," + la;
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

	/**
	 * 填充轮播图数据 只要把pages的值赋好
	 * */
	private void initLBT() {
		final int length = focus_images.size();
		pages = new ArrayList<View>();
		for (int i = 0; i < length; i++) {
			View p = li.inflate(R.layout.item_indexfrag_pages, null);
			final int position = i;
			pages.add(p);
			final ImageView image = (ImageView) p.findViewById(R.id.image);
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
					Intent intent = new Intent(context, DetailActivity.class);
					intent.putExtra("_id", en_id);
					intent.putExtra("stamp", stamp);
					intent.putExtra("en_type", en_type);
					startActivity(intent);
					Activity context = getActivity();
					context.overridePendingTransition(R.anim.slide_left_in,
							R.anim.slide_left_out);
				}
			});
			String url = focus_images.get(i).getCover();
			image.setTag(url);
			if (i == 0) {
				// 首先把第一张图显示
				imageLoader.displayImage(url, image, options,
						animateFirstListener);
			}
		}
		pageApr.notifyDataSetChanged(); // 更新数据
		vPager.setCurrentItem(0);
		if (length > 0) {
			vPager_index.setText("1/" + focus_images.size());
		}
		// 先监听
		vPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				vPager_index.setText((arg0 + 1) + "/" + focus_images.size());
				if (arg0 > 0) {
					ImageView img = (ImageView) pages.get(arg0).findViewById(
							R.id.image);
					String url = (String) img.getTag();
					imageLoader.displayImage(url, img, options,
							animateFirstListener);
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
		timer(); // 自动播放
	}

	/**
	 * 解析百度天气json
	 * */
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

	private void showInfo() {
		String citynm = stringToCitynm(location.getText().toString());
		Map<String, String> map = weather.getWeather_data().get(0);
		String weatherInfo = map.get("weather");
		String temperature = map.get("temperature");
		txt_weather.setText(citynm + " " + weatherInfo + " " + temperature);
		String imgUrl = weather.getWeather_data().get(0).get("dayPictureUrl");
		imageLoader.displayImage(imgUrl, img_weather, options,
				animateFirstListener);
	}

	/**
	 * 解析栏目json
	 * */
	protected List<IndexFragTheme> getThemesJson(String result)
			throws Exception {
		List<IndexFragTheme> datas = new ArrayList<IndexFragTheme>();
		JSONObject jsonObject = new JSONObject(result);
		JSONObject jsonObject2 = jsonObject.getJSONObject("data");
		/** 获取全省精选的吃、住、游三个主题栏目 */
		JSONArray array = jsonObject2.getJSONArray("themes");
		if (themes != null && themes.size() > 0) {
			JSONArray testJsonArray = mCache.getAsJSONArray("testJsonArray");
			for (int i = 0; i < array.length(); i++) {
				JSONObject dd = (JSONObject) array.opt(i);
				testJsonArray.put(dd);
			}
			mCache.put("testJsonArray", testJsonArray);
		} else {
			mCache.put("testJsonArray", array);
		}
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonObject3 = (JSONObject) array.opt(i);
			String cover = jsonObject3.getString("cover");
			String title = jsonObject3.getString("title");
			JSONArray array2 = jsonObject3.getJSONArray("entities");
			List<Entity> entities = new ArrayList<Entity>();
			for (int j = 0; j < array2.length(); j++) {
				JSONObject jsonObject5 = (JSONObject) array2.opt(j);
				String url = jsonObject5.getString("url");
				String name = jsonObject5.getString("name");
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
					JSONObject jsonObject6 = (JSONObject) array3.opt(k);
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
				Entity entity = new Entity();
				entity.setUrl(url);
				entity.setEntity_id(entity_id);
				entity.setOrder(orderf);
				entity.setEntity_type(entity_type);
				entity.setImages(imgDatas);
				entity.setGps(gps);
				entity.setName(name);
				entities.add(entity);
			}
			IndexFragTheme theme = new IndexFragTheme(cover, 0, title, entities);
			datas.add(theme);
		}
		return datas;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		try {
			if (requestCode == 7 && resultCode == Activity.RESULT_OK) {
				area_id = data.getIntExtra("area_id", -1);
				province = data.getIntExtra("province", -1);
				String area_name = data.getStringExtra("area_name");
				msg("您移驾到：" + area_name);
				location.setText(area_name); // 定位？？
				if (timer != null) {
					timer.cancel();
					timer = null;
				}
				initList();
				downJsonById();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
