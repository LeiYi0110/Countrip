package com.pi9Lin.fragment;

import java.io.IOException;
import java.util.ArrayList;
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.activity.DetailActivity;
import com.pi9Lin.activity.ShowCommentActivity;
import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.RoundData;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.pulltorefresh.pullableview.PullToRefreshLayout;
import com.pi9Lin.pulltorefresh.pullableview.RefreshListener;
import com.pi9Lin.search.SearchActivity;
import com.pi9Lin.utils.ACache;
import com.pi9Lin.utils.DistanceCalculator;

@SuppressLint({ "NewApi", "ResourceAsColor", "HandlerLeak" })
public class RoundFrag extends BaseFragment {
	/**
	 * 附近Fragment
	 * */
	// 必须判断前端缓存的是否已经收藏过
	private ACache mCache;

	private View view;
	private ListView list_round;
	private String geoLng;
	private String geoLat;

	private List<RoundData> roundDatas = new ArrayList<RoundData>();
	private RelativeLayout round_search;

	// 主题栏目图片
	String[][] themeUrls;
	private Dialog dialog;
	private PullToRefreshLayout ptrl;

	// 获得所有已经收藏的项
	List<Entity> allSave; // 查看缓存

	private MyAprr apr;
	private View footer;
	private boolean finish = true;// 是否加载完成
	private LayoutInflater li;

	public String stamp;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/** 附近页开始 */
		context = getActivity();
		preferences = getConfig();
		view = inflater.inflate(R.layout.fragment_round, container, false);
		findById();
		mCache = ACache.get(context);
		getAllSave();
		li = LayoutInflater.from(context);
		initImageLoader(); // 初始化图片异步处理对象
		footer = li.inflate(R.layout.footer, null);
		init();
		initList(); // 填充页面
		initPullToRefresh();
		dialog = dialog(context, "正在加载...");
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		return view;
	}

	private void initList() {
		if (geoLng != null && geoLat != null) {
			download();
		} else {
			// 未获取定位 要求定位
			msg("您没有开启gps支持");
		}
	}

	class Holder {
		TextView sleep_location;
		RelativeLayout to_comment;
		ImageView sleep_love_img;
		ViewPager list_pic;
		TextView img_index;
		TextView distance;
		TextView collection_count;
		TextView comment_num;
		RelativeLayout love_it;
		ImageView logo;
		TextView subtitle;
	}

	private void resetViewHolder(Holder h) {
		h.sleep_location.setText(null);
		h.img_index.setText(null);
		h.distance.setText(null);
		h.comment_num.setText(null);
		h.list_pic.setAdapter(null);
		h.love_it.setOnClickListener(null);
		h.to_comment.setOnClickListener(null);
		h.sleep_love_img.setBackground(null);
		h.subtitle.setText(null);
		h.logo.setImageDrawable(null);
	}

	private boolean isSaved(String _id, int _type) {
		for (int j = 0; j < allSave.size(); j++) {
			if (allSave.get(j).getEntity_id().equals(_id)
					&& allSave.get(j).getEntity_type() == _type) {
				return true;
			}
		}
		return false;
	}

	/**
	 * listview适配器
	 * */
	private class MyAprr extends BaseAdapter implements OnScrollListener {

		double la = Double.valueOf(geoLat).doubleValue();
		double ln = Double.valueOf(geoLng).doubleValue();

		public MyAprr() {
			// TODO Auto-generated constructor stub
			list_round.setOnScrollListener(this);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return roundDatas.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return roundDatas.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v;
			final Holder h;
			if (arg1 == null) {
				v = li.inflate(R.layout.item_list_listitem, null);
				h = new Holder();
				/** 标题 */
				h.sleep_location = (TextView) v.findViewById(R.id.title);
				/** 评论 */
				h.to_comment = (RelativeLayout) v.findViewById(R.id.to_comment);
				h.comment_num = (TextView) v.findViewById(R.id.comment_num);
				/** 距离 */
				h.distance = (TextView) v.findViewById(R.id.distance_num);
				/** 点赞数 */
				h.collection_count = (TextView) v.findViewById(R.id.love_num);
				/** 点赞图片 */
				h.sleep_love_img = (ImageView) v.findViewById(R.id.love_img);
				/** 点赞 */
				h.love_it = (RelativeLayout) v.findViewById(R.id.to_love);
				/** viewpage */
				h.list_pic = (ViewPager) v.findViewById(R.id.vPage);
				/** 图片页码 */
				h.img_index = (TextView) v.findViewById(R.id.vPage_num);
				h.logo = (ImageView) v.findViewById(R.id.logo);
				h.subtitle = (TextView) v.findViewById(R.id.subtitle);
				v.setTag(h);
			} else {
				v = arg1;
				h = (Holder) v.getTag();
				resetViewHolder(h);
			}
			/**
			 * 数据
			 * */
			h.sleep_location.setText(roundDatas.get(arg0).getName());
			h.to_comment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// mark
					startActivity(new Intent(context, ShowCommentActivity.class));
				}
			});
			h.distance.setText(DistanceCalculator.GetDistance(la, ln,
					roundDatas.get(arg0).getGps()[0], roundDatas.get(arg0)
							.getGps()[1])
					+ "km");
			h.collection_count.setText(roundDatas.get(arg0)
					.getCollection_count() + "+");
			h.comment_num
					.setText(roundDatas.get(arg0).getComment_count() + "+");
			/**
			 * 先判断是否已经被收藏
			 * */
			final String en_id = roundDatas.get(arg0).getEntity_id();
			final int en_typef = roundDatas.get(arg0).getEntity_type();
			if (en_typef == 1) {
				h.logo.setImageDrawable(getImgResource(R.drawable.sleep));
				h.subtitle.setText("乡村住宿");
				stamp = "hotel";
			} else if (en_typef == 2) {
				h.logo.setImageDrawable(getImgResource(R.drawable.eat));
				h.subtitle.setText("乡村美食");
				stamp = "restaurant";
			} else if (en_typef == 3) {
				h.logo.setImageDrawable(getImgResource(R.drawable.play));
				h.subtitle.setText("乡村景点");
				stamp = "sights";
			}
			final int fff = arg0;
			roundDatas.get(arg0).setSleep_love_img(h.sleep_love_img);
			roundDatas.get(arg0).setCollection(h.collection_count);
			h.collection_count.setText(roundDatas.get(arg0)
					.getCollection_count() + "+");
			if (isSaved(en_id, en_typef)) {
				// 收藏了
				h.sleep_love_img
						.setImageDrawable(getImgResource(R.drawable.love));
				h.collection_count.setTextColor(R.color.hongse);
				roundDatas.get(arg0).setSaved(true);
			} else {
				// 未收藏
				h.sleep_love_img
						.setImageDrawable(getImgResource(R.drawable.lovef));
				roundDatas.get(arg0).setSaved(false);
			}
			/**
			 * 收藏监听
			 * */
			h.love_it.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// 先判断有没有登录
					boolean isLandIn = preferences
							.getBoolean("isLandIn", false);
					if (!isLandIn) {
						Intent intent = new Intent(context,
								DengLuActivity.class);
						startActivityForResult(intent, 3);
					} else {
						if (isSaved(en_id, en_typef)) {
							String url = "http://www.xiangyouji.com.cn:3000/my/removeCollection";
							AsyncHttpClient client = new AsyncHttpClient();
							PersistentCookieStore myCookieStore = new PersistentCookieStore(
									context);
							client.setCookieStore(myCookieStore);
							RequestParams params = new RequestParams();
							params.put("entity_type", en_typef + "");
							params.put("entity_id", en_id);
							client.post(url, params,
									new AsyncHttpResponseHandler() {
										@Override
										public void onSuccess(int arg0,
												String result) {
											try {
												if (checkJson(result)) {
													msg("取消成功");
													h.sleep_love_img
															.setImageDrawable(getImgResource(R.drawable.lovef));
													int sum = roundDatas
															.get(fff)
															.getCollection_count();
													sum--;
													h.collection_count
															.setText(sum + "+");
													h.collection_count
															.setTextColor(R.color.huise);
													roundDatas
															.get(fff)
															.setCollection_count(
																	sum);
													roundDatas.get(fff)
															.setSaved(false);
													/** 修改本地收藏缓存 */
													for (int j = 0; j < allSave
															.size(); j++) {
														if (allSave.get(j)
																.getEntity_id()
																.equals(en_id)
																&& allSave
																		.get(j)
																		.getEntity_type() == en_typef) {
															allSave.remove(j);
														}
													}
													String ss = obj2Json(allSave);
													mCache.put("allSave", ss);
												} else {
													msg("取消失败--数据问题");
												}
											} catch (Exception e) {
												// TODO Auto-generated catch
												e.printStackTrace();
											}
										}

										@Override
										public void onFailure(Throwable arg0,
												String arg1) {
											Toast.makeText(context,
													"取消失败--网络问题",
													Toast.LENGTH_SHORT).show();
											super.onFailure(arg0, arg1);
										}
									});
						} else {
							String url = "http://www.xiangyouji.com.cn:3000/my/collect";
							AsyncHttpClient client = new AsyncHttpClient();
							PersistentCookieStore myCookieStore = new PersistentCookieStore(
									context);
							client.setCookieStore(myCookieStore);
							RequestParams params = new RequestParams();
							params.put("entity_type", en_typef + "");
							params.put("entity_id", en_id);
							client.post(url, params,
									new AsyncHttpResponseHandler() {
										@Override
										public void onSuccess(int arg0,
												String result) {
											try {
												if (checkJson(result)) {
													msg("收藏成功");
													h.sleep_love_img
															.setImageDrawable(getImgResource(R.drawable.love));
													int sum = roundDatas
															.get(fff)
															.getCollection_count();
													sum++;
													h.collection_count
															.setText(sum + "+");
													h.collection_count
															.setTextColor(R.color.hongse);
													roundDatas
															.get(fff)
															.setCollection_count(
																	sum);
													roundDatas.get(fff)
															.setSaved(true);
													/** 修改本地收藏缓存 */
													Entity entity = new Entity();
													entity.setEntity_id(en_id);
													entity.setEntity_type(en_typef);
													allSave.add(entity);
													String ss = obj2Json(allSave);
													mCache.put("allSave", ss);
												} else {
													msg("收藏失败--数据问题");
												}
											} catch (Exception e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
										}

										@Override
										public void onFailure(Throwable arg0,
												String arg1) {
											Toast.makeText(context,
													"收藏失败--网络问题",
													Toast.LENGTH_SHORT).show();
											super.onFailure(arg0, arg1);
										}
									});
						}
					}
				}
			});
			/**
			 * viewpage
			 * */
			final int length = roundDatas.get(arg0).getImgDatas().size();
			MyThemeVpageApr themeApr = new MyThemeVpageApr(roundDatas.get(arg0)
					.getImgDatas(), arg0);
			h.list_pic.setAdapter(themeApr);
			h.list_pic.setCurrentItem(roundDatas.get(arg0).getPage_index());
			if (length > 0) {
				h.img_index.setText((roundDatas.get(arg0).getPage_index() + 1)
						+ "/" + length);
			} else if (length == 0) {
				h.img_index.setText("0/0");
			}
			h.list_pic.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageSelected(int arg0) {
					// TODO Auto-generated method stub
					roundDatas.get(fff).setPage_index(arg0);
					h.img_index.setText((arg0 + 1) + "/" + length);
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
			return v;
		}

		// 每次加载10条数据
		private int pageSize = 10;
		int totalCount = 10; // 从第11条开始加载
		private boolean scrolled = false;// 是否加载完成

		// 当list开始滚动时
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			// 当翻到最后一条数据时
			if (scrolled
					&& list_round.getCount() == 0
					&& finish
					|| scrolled
					&& list_round.getChildAt(list_round
							.getLastVisiblePosition()
							- list_round.getFirstVisiblePosition()) != null
					&& list_round.getChildAt(
							list_round.getLastVisiblePosition()
									- list_round.getFirstVisiblePosition())
							.getBottom() <= list_round.getMeasuredHeight()
					&& finish) {
				// 已经移动到了listview的最后
				finish = false;
				// 添加页脚
				list_round.addFooterView(footer);
				String url = "http://www.xiangyouji.com.cn:3000/nearby/longitude/"
						+ geoLng
						+ "/latitude/"
						+ geoLat
						+ "/startIndex/"
						+ totalCount + "/length/" + pageSize;
				MyTask myTask = new MyTask();
				myTask.execute(url);
				totalCount += pageSize;
			}
		}

		// 滚动状态发生改变时
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// 不滚动时保存当前滚动到的位置
			if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				scrolled = true;
			}
		}
	}

	private class MyThemeVpageApr extends PagerAdapter {

		private int mChildCount = 0;
		private View p;
		private ImageView iv;
		private List<SleepImgData> data;
		private int num;

		MyThemeVpageApr(List<SleepImgData> data, int num) {
			this.data = data;
			this.num = num;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
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
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			// container.removeView(p);
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
		public Object instantiateItem(ViewGroup container, int position) {
			// 填充viewpage数据
			p = li.inflate(R.layout.item_indexfrag_pages, null);
			iv = (ImageView) p.findViewById(R.id.image);
			String url = data.get(position).getUrl();
			int en_typef = roundDatas.get(num).getEntity_type();
			String ss=null;
			if (en_typef == 1) {
				ss = "hotel";
			} else if (en_typef == 2) {
				ss = "restaurant";
			} else if (en_typef == 3) {
				ss = "sights";
			}
			final String sss=ss;
			imageLoader.displayImage(url, iv, options, animateFirstListener);
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(context, DetailActivity.class);
					intent.putExtra("_id", roundDatas.get(num).getEntity_id());
					intent.putExtra("stamp", sss);
					intent.putExtra("en_type", roundDatas.get(num).getEntity_type());
					intent.putExtra("index", roundDatas.get(num).getPage_index());
					intent.putExtra("latitude", roundDatas.get(num).getGps()[0]);
					intent.putExtra("longitude", roundDatas.get(num).getGps()[1]);
					startActivityForResult(intent, 9);
					Activity context = getActivity();
					context.overridePendingTransition(R.anim.slide_left_in,
							R.anim.slide_left_out);
				}
			});
			container.removeView(p);
			container.addView(p);
			return p;
		}
	}

	/**
	 * 获取所有的收藏信息
	 * */
	private void getAllSave() {
		try {
			String testString = mCache.getAsString("allSave");
			if (testString.equals("unLogIn")) {
				allSave = new ArrayList<Entity>();
			} else {
				allSave = save(testString);
			}
		} catch (Exception e) {
			Log.d("获取收藏缓存错误", e.getMessage());
		}
	}

	private void initPullToRefresh() {
		// TODO Auto-generated method stub
		ptrl.setTop(round_search);
		ptrl.setOnRefreshListener(new RefreshListener(){
			@Override
			public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
				// TODO Auto-generated method stub
				initList(); // 填充页面
				new Handler(){
					@Override
					public void handleMessage(Message msg){
						// 千万别忘了告诉控件刷新完毕了哦！
						pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
					}
				}.sendEmptyMessageDelayed(0, 500);
				super.onRefresh(pullToRefreshLayout);
			}
		});
	}
	
	private void findById() {
		// TODO Auto-generated method stub
		list_round = (ListView) view.findViewById(R.id.list_round);
		round_search = (RelativeLayout) view.findViewById(R.id.round_search);
		round_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context, SearchActivity.class));
				Activity context = getActivity();
				context.overridePendingTransition(R.anim.slide_left_in,
						R.anim.slide_left_out);
			}
		});
		// 下拉刷新
		ptrl = ((PullToRefreshLayout) view.findViewById(R.id.refresh_view));
	}

	private void init() {
		/** 按经纬度获取周边列表 */
		geoLng = preferences.getString("geoLng", null);
		geoLat = preferences.getString("geoLat", null);
	}

	private void download() {
		String url = "http://www.xiangyouji.com.cn:3000/nearby/longitude/"
				+ geoLng + "/latitude/" + geoLat + "/startIndex/0/length/10";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					roundDatas = getRoundJson(arg1);// roundDatas有值了
					list_round.addFooterView(footer);
					// 填充listview数据
					apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
					list_round.setAdapter(apr);
					list_round.removeFooterView(footer);
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
				}
				dialog.dismiss();
				super.onSuccess(arg0, arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "您的网络出现问题", Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				super.onFailure(arg0, arg1);
			}
		});
	}

	private class MyTask extends AsyncTask<String, Void, String> {

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
						roundDatas.addAll(getRoundJson(result));// 已经得到一些数据
						// 让listview自动刷新
						apr.notifyDataSetChanged();
						finish = true;
					}
					// 去掉页脚
					list_round.removeFooterView(footer);
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
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
			if (jsonObject.getJSONArray("data").length() == 0) {
				msg("没有数据了");
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 解析附近json数据
	 * */
	protected List<RoundData> getRoundJson(String arg1) throws Exception {
		List<RoundData> da = new ArrayList<RoundData>();
		JSONObject jsonObject = new JSONObject(arg1);
		JSONArray array = jsonObject.getJSONArray("data");
		int length = array.length();
		for (int i = 0; i < length; i++) {
			RoundData roundData = new RoundData();
			JSONObject jsonObject2 = (JSONObject) array.opt(i);
			String _id = jsonObject2.getString("_id");
			String name = jsonObject2.getString("name");
			String address = jsonObject2.getString("address");
			String phone = jsonObject2.getString("phone");
			String cover = jsonObject2.getString("cover");
			int collection_count = jsonObject2.getInt("collection_count");
			int comment_num = jsonObject2.getInt("comment_count");
			String entity_id = jsonObject2.getString("entity_id");
			int province = jsonObject2.getInt("province");
			int area_id = jsonObject2.getInt("area_id");
			int entity_type = jsonObject2.getInt("entity_type");
			JSONObject jsonObject3 = jsonObject2.getJSONObject("gps");
			double latitude = jsonObject3.getDouble("latitude");
			double longitude = jsonObject3.getDouble("longitude");
			double[] gps = { latitude, longitude };
			roundData.setGps(gps);
			JSONArray array2 = jsonObject2.getJSONArray("images");
			List<SleepImgData> imgDatas = new ArrayList<SleepImgData>();
			for (int j = 0; j < array2.length(); j++) {
				JSONObject jsonObject4 = (JSONObject) array2.opt(j);
				SleepImgData imgData = new SleepImgData();
				String url = jsonObject4.getString("url");
				String update_time = jsonObject4.getString("update_time");
				String create_time = jsonObject4.getString("create_time");
				int order_no = jsonObject4.getInt("order");
				imgData.setUrl(url);
				imgData.setUpdate_time(update_time);
				imgData.setCreate_time(create_time);
				imgData.setOrder_no(order_no);
				imgDatas.add(imgData);
			}
			/** 初始化封面 */
			roundData.set_id(_id);
			roundData.setName(name);
			roundData.setAddress(address);
			roundData.setCover(cover);
			roundData.setEntity_id(entity_id);
			roundData.setEntity_type(entity_type);
			roundData.setProvince(province);
			roundData.setPhone(phone);
			roundData.setArea_id(area_id);
			roundData.setImgDatas(imgDatas);
			roundData.setCollection_count(collection_count);
			roundData.setComment_count(comment_num);
			roundData.setSleep_love_img(new ImageView(context));
			roundData.setCollection(new TextView(context));
			da.add(roundData);
		}
		return da;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
			// 去收藏
			msg("登录成功");
		} else if (requestCode == 2 && resultCode != Activity.RESULT_OK) {
			msg("需要登陆后才能做次操作");
		}
		/**
		 * 从详细页面回来
		 * */
		if (requestCode == 10) {
		}
		getAllSave();
		for (int i = 0; i < roundDatas.size(); i++) {
			boolean l = false;
			for (int j = 0; j < allSave.size(); j++) {
				if (allSave.get(j).getEntity_id()
						.equals(roundDatas.get(i).getEntity_id())
						&& allSave.get(j).getEntity_type() == roundDatas.get(i)
								.getEntity_type()) {
					l = true;
				}
			}
			if (l) {
				roundDatas.get(i).getSleep_love_img()
						.setImageDrawable(getImgResource(R.drawable.love));
				if (!roundDatas.get(i).isSaved()) {
					int num = roundDatas.get(i).getCollection_count() + 1;
					roundDatas.get(i).setCollection_count(num);
					roundDatas.get(i).getCollection().setText(num + "+");
				}
				// 重置
				roundDatas.get(i).setSaved(true);
			} else {
				roundDatas.get(i).getSleep_love_img()
						.setImageDrawable(getImgResource(R.drawable.lovef));
				if (roundDatas.get(i).isSaved()) {
					int num = roundDatas.get(i).getCollection_count() - 1;
					roundDatas.get(i).setCollection_count(num);
					roundDatas.get(i).getCollection().setText(num + "+");
				}
				// 重置
				roundDatas.get(i).setSaved(false);
			}
		}
	}
}
