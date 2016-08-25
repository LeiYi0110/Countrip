package com.pi9Lin.activity;

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
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.SleepActData;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.imageLoader.ImageDownLoader;
import com.pi9Lin.imageLoader.ImageDownLoader.onImageLoaderListener;
import com.pi9Lin.pulltorefresh.pullableview.PullToRefreshLayout;
import com.pi9Lin.pulltorefresh.pullableview.RefreshListener;
import com.pi9Lin.search.SearchActivity;
import com.pi9Lin.utils.ACache;
import com.pi9Lin.utils.DistanceCalculator;

@SuppressLint({ "NewApi", "ResourceAsColor", "HandlerLeak" })
public class ListViewActivity extends BaseActivity {

	private MyListView list_sleep;
	private RelativeLayout search_btn;
	private String stamp;
	private double geoLat;
	private double geoLng;
	private int area_id;
	private RelativeLayout top_back;
	private TextView top_title;
	private List<SleepActData> actDatas = new ArrayList<SleepActData>();
	private Dialog dialog;
	private View footer;
	private PullToRefreshLayout ptrl;

	// 获得所有已经收藏的项
	private ACache mCache;
	private List<Entity> allSave; // 查看缓存
	private int en_type;

	private boolean finish = true;// 是否加载完成

	private ImageDownLoader mImageDownLoader;

	private MyAprr apr;
	private boolean roundDataflag;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		if(arg0!=null){
			msg("崩溃了");
		}
		setContentView(R.layout.activity_sleep);
		context=getApplicationContext();
		findById();
		initActionbar(); // actionbar
		setOnClickListener();
		ShareSDK.initSDK(this);
		mCache = ACache.get(context);
		mImageDownLoader = new ImageDownLoader(context);
		initImageLoader(); // 初始化图片异步处理对象
		footer = LayoutInflater.from(context).inflate(R.layout.footer, null);
		getAllSave();
		receiveIntent();
		dialog = dialog(this, "正在加载...");
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}

	/**
	 * listview适配器
	 * */
	private class MyAprr extends BaseAdapter implements OnScrollListener {

		LayoutInflater inflater = LayoutInflater.from(context);
		double la;
		double ln;
		private boolean firstIn;
		private int start;
		private int end;

		public MyAprr() {
			// TODO Auto-generated constructor stub
			String lat = preferences.getString("geoLat", null);
			String lng = preferences.getString("geoLng", null);
			la = Double.valueOf(lat).doubleValue();
			ln = Double.valueOf(lng).doubleValue();
			firstIn = true;
			list_sleep.setOnScrollListener(this);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return actDatas.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return actDatas.get(arg0);
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
				v = inflater.inflate(R.layout.item_sleep_listitem, null);
				h = new Holder();
				/** 标题 */
				h.sleep_location = (TextView) v
						.findViewById(R.id.sleep_location);
				/** 评论 */
				h.to_comment = (RelativeLayout) v.findViewById(R.id.to_comment);
				/** 距离 */
				h.distance = (TextView) v.findViewById(R.id.distance);
				/** 分享 */
				h.to_share = (RelativeLayout) v.findViewById(R.id.to_share);
				/** 点赞数 */
				h.collection_count = (TextView) v
						.findViewById(R.id.collection_count);
				/** 点赞图片 */
				h.sleep_love_img = (ImageView) v
						.findViewById(R.id.sleep_love_img);
				/** 点赞 */
				h.love_it = (RelativeLayout) v.findViewById(R.id.love_it);
				/** viewpage */
				h.pager = (ViewPager) v.findViewById(R.id.list_pic);
				/** 图片页码 */
				h.img_index = (TextView) v.findViewById(R.id.img_index);
				v.setTag(h);
			} else {
				v = arg1;
				h = (Holder) v.getTag();
				resetViewHolder(h);
			}
			/**
			 * 数据
			 * */
			h.sleep_location.setText(actDatas.get(arg0).getXx_name());
			h.to_comment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// mark
					startActivity(new Intent(context, ShowCommentActivity.class));
				}
			});
			h.distance.setText(DistanceCalculator.GetDistance(la, ln, actDatas
					.get(arg0).getGps()[0], actDatas.get(arg0).getGps()[1])
					+ "km");
			h.to_share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
//					showShare();
				}
			});
			/**
			 * 先判断是否已经被收藏
			 * */
			final String en_id = actDatas.get(arg0).get_id();
			final int en_typef = en_type;
			final int fff = arg0;
			actDatas.get(arg0).setSleep_love_img(h.sleep_love_img);
			actDatas.get(arg0).setCollection(h.collection_count);
			h.collection_count.setText(actDatas.get(arg0).getCollection_count()
					+ "+");
			if (isSaved(en_id, en_typef)) {
				// 收藏了
				h.sleep_love_img
						.setImageDrawable(getImgResource(R.drawable.love));
				h.collection_count.setTextColor(R.color.hongse);
				actDatas.get(arg0).setSaved(true);
			} else {
				// 未收藏
				h.sleep_love_img
						.setImageDrawable(getImgResource(R.drawable.lovef));
				actDatas.get(arg0).setSaved(false);
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
													int sum = actDatas
															.get(fff)
															.getCollection_count();
													sum--;
													h.collection_count
															.setText(sum + "+");
													h.collection_count
															.setTextColor(R.color.huise);
													actDatas.get(fff)
															.setCollection_count(
																	sum);
													actDatas.get(fff).setSaved(
															false);
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
													int sum = actDatas
															.get(fff)
															.getCollection_count();
													sum++;
													h.collection_count
															.setText(sum + "+");
													h.collection_count
															.setTextColor(R.color.hongse);
													actDatas.get(fff)
															.setCollection_count(
																	sum);
													actDatas.get(fff).setSaved(
															true);
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
			final int length = actDatas.get(arg0).getImgDatas().size();
			final int index = arg0;
			List<View> list = new ArrayList<View>();
			for (int i = 0; i < length; i++) {
				View p = inflater.inflate(R.layout.item_indexfrag_pages, null);
				list.add(p);
				final ImageView image = (ImageView) p.findViewById(R.id.image);
				image.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context,
								YouDetailActivity.class);
						intent.putExtra("_id", actDatas.get(index).get_id());
						intent.putExtra("stamp", stamp);
						intent.putExtra("en_type", en_type);
						intent.putExtra("index", actDatas.get(index)
								.getPage_index());
						intent.putExtra("latitude", actDatas.get(index)
								.getGps()[0]);
						intent.putExtra("longitude", actDatas.get(index)
								.getGps()[1]);
						startActivityForResult(intent, 9);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
				// 解决listview刷新时数据不稳定显示
				String rr = actDatas.get(index).getImgDatas().get(i).getUrl();
				image.setTag(rr);
				// 读取图片缓存 因为读取的可能是文件缓存 所以要在异步里面执行 但是会影响程序
				String subUrl = rr.replaceAll("[^\\w]", "");
				if (mImageDownLoader.getBitmapFromMemCache(subUrl) != null) {
					// 先缓存
					Bitmap b = mImageDownLoader.getBitmapFromMemCache(subUrl);
					image.setImageBitmap(b);
				}else{
					image.setImageDrawable(getImgResource(R.drawable.empty_img));
				}
			}
			MyAdapter adapterf = new MyAdapter(actDatas.get(arg0), list);
			h.pager.setAdapter(adapterf);
			h.pager.setCurrentItem(actDatas.get(arg0).getPage_index());
			if (length > 0) {
				h.img_index.setText((actDatas.get(arg0).getPage_index() + 1)
						+ "/" + length);
			} else if (length == 0) {
				h.img_index.setText("0/0");
			}
			final int dd = arg0;
			final List<View> ss = list;
			// 绑定动作监听器：如翻页的动画
			h.pager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(final int argf) {
					// TODO Auto-generated method stub
					h.img_index.setText((argf + 1) + "/" + length);
					actDatas.get(dd).setPage_index(argf);
					if (argf > 0) {
						Bitmap bitmap = mImageDownLoader.downloadImage(actDatas
								.get(dd).getImgDatas().get(argf).getUrl(),
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
			return v;
		}

		// 每次加载10条数据
		private int pageSize = 10;
		int totalCount = 10; // 从第11条开始加载
		private boolean scrolled = false;// 是否加载完成

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int arg3) {
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
					&& list_sleep.getCount() == 0
					&& finish
					|| scrolled
					&& list_sleep.getChildAt(list_sleep
							.getLastVisiblePosition()
							- list_sleep.getFirstVisiblePosition()) != null
					&& list_sleep.getChildAt(
							list_sleep.getLastVisiblePosition()
									- list_sleep.getFirstVisiblePosition())
							.getBottom() <= list_sleep.getMeasuredHeight()
					&& finish) {

				// 已经移动到了listview的最后
				finish = false;
				// 添加页脚
				list_sleep.addFooterView(footer);
				String url = "http://www.xiangyouji.com.cn:3000/" + stamp
						+ "/area_id/" + area_id + "/startIndex/" + totalCount
						+ "/length/" + pageSize;
				if (roundDataflag) {
					url = "http://www.xiangyouji.com.cn:3000/nearby/" + stamp
							+ "_list/longitude/" + geoLng + "/latitude/"
							+ geoLat + "/startIndex/" + totalCount + "/length/"
							+ pageSize;
				}
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

	/**
	 * 异步下载网络图片 在分页的时候 首次进入加载一次
	 */
	private void showImage(int firstVisibleItem, int visibleItemCount,
			AbsListView v) {
		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
			String url = actDatas.get(i).getImgDatas().get(0).getUrl();
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

	class Holder {
		TextView sleep_location;
		RelativeLayout to_comment;
		RelativeLayout to_share;
		ImageView sleep_love_img;
		ViewPager pager;
		TextView img_index;
		TextView collection_count;
		TextView distance;
		RelativeLayout love_it;
	}

	private void resetViewHolder(Holder h) {
		h.sleep_location.setText(null);
		h.img_index.setText(null);
		h.pager.setAdapter(null);
		h.love_it.setOnClickListener(null);
		h.to_comment.setOnClickListener(null);
		h.to_share.setOnClickListener(null);
		h.sleep_love_img.setBackground(null);
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

	private void initActionbar() {
		// 自定义ActionBar
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.layout_sleep_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		top_title = (TextView) v.findViewById(R.id.top_title);
		top_back = (RelativeLayout) v.findViewById(R.id.backward);
		search_btn = (RelativeLayout) v.findViewById(R.id.search_btn);
	}

	private void setOnClickListener() {
		ptrl.setOnRefreshListener(new RefreshListener(){
			@Override
			public void onRefresh(final PullToRefreshLayout pullToRefreshLayout) {
				// TODO Auto-generated method stub
				getAllSave();
				Intent intent = getIntent();
				stamp = intent.getStringExtra("stamp");
				en_type = intent.getIntExtra("en_type", -1);
				roundDataflag = intent.getBooleanExtra("roundData", false);
				if (roundDataflag) {
					// 获取按经纬度附近的数据
					geoLat = intent.getDoubleExtra("geoLat", -1);
					geoLng = intent.getDoubleExtra("geoLng", -1);
					// 把标志归原
					String url = "http://www.xiangyouji.com.cn:3000/nearby/" + stamp
							+ "/longitude/" + geoLng + "/latitude/" + geoLat
							+ "/startIndex/0/length/10";
					AsyncHttpClient client = new AsyncHttpClient();
					client.get(url, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int arg0, String arg1) {
							/** 成功获取数据 */
							try {
								// 此时stamp改变了 所以可以用这个解析
								actDatas = jsonToString(arg1, stamp);// 已经得到一些数据 但是图片只有地址
								// 填充listview数据
								apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
								list_sleep.setAdapter(apr);
								new Handler()
								{
									@Override
									public void handleMessage(Message msg)
									{
										// 千万别忘了告诉控件刷新完毕了哦！
										pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
									}
								}.sendEmptyMessageDelayed(0, 1000);
							} catch (Exception e) {
								System.out.println("错误:" + e.getMessage());
							}
							super.onSuccess(arg0, arg1);
						}

						@Override
						public void onFailure(Throwable arg0, String arg1) {
							// TODO Auto-generated method stub
							new Handler()
							{
								@Override
								public void handleMessage(Message msg)
								{
									// 千万别忘了告诉控件刷新完毕了哦！
									pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
								}
							}.sendEmptyMessageDelayed(0, 1000);
							super.onFailure(arg0, arg1);
						}
					});
					if (stamp.equals("hotel_list")) {
						top_title.setText("住山庄");
						stamp = "hotel";
					} else if (stamp.equals("restaurant_list")) {
						top_title.setText("吃乡味");
						stamp = "restaurant";
					} else if (stamp.equals("sights_list")) {
						top_title.setText("游美景");
						stamp = "sights";
					}
				} else {
					// 按县省获取附近数据
					area_id = intent.getIntExtra("area_id", -1);
					String url = "http://www.xiangyouji.com.cn:3000/" + stamp + "/area_id/"
							+ area_id + "/startIndex/0/length/10";
					AsyncHttpClient client = new AsyncHttpClient();
					client.get(url, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int arg0, String arg1) {
							/** 成功获取数据 */
							try {
								actDatas = jsonToString(arg1, stamp);// 已经得到一些数据 但是图片只有地址
								// 填充listview数据
								apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
								list_sleep.setAdapter(apr);
								new Handler()
								{
									@Override
									public void handleMessage(Message msg)
									{
										// 千万别忘了告诉控件刷新完毕了哦！
										pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
									}
								}.sendEmptyMessageDelayed(0, 1000);
							} catch (Exception e) {
								System.out.println("错误:" + e.getMessage());
							}
							super.onSuccess(arg0, arg1);
						}

						@Override
						public void onFailure(Throwable arg0, String arg1) {
							// TODO Auto-generated method stub
							new Handler()
							{
								@Override
								public void handleMessage(Message msg)
								{
									// 千万别忘了告诉控件刷新完毕了哦！
									pullToRefreshLayout.refreshFinish(PullToRefreshLayout.FAIL);
								}
							}.sendEmptyMessageDelayed(0, 1000);
							super.onFailure(arg0, arg1);
						}
					});
					if (stamp.equals("hotel")) {
						top_title.setText("住山庄");
					} else if (stamp.equals("restaurant")) {
						top_title.setText("吃乡味");
					} else if (stamp.equals("sights")) {
						top_title.setText("游美景");
					}
				}
				
				super.onRefresh(pullToRefreshLayout);
			}
		}); // 下拉刷新
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context, SearchActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		top_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
	}

	private void findById() {
		list_sleep = (MyListView) findViewById(R.id.list_sleep);
		// 下拉刷新
		ptrl = ((PullToRefreshLayout) findViewById(R.id.refresh_view));
	}

	private void receiveIntent() {
		/**
		 * 判断该查询什么数据
		 * */
		Intent intent = getIntent();
		stamp = intent.getStringExtra("stamp");
		en_type = intent.getIntExtra("en_type", -1);
		roundDataflag = intent.getBooleanExtra("roundData", false);
		if (roundDataflag) {
			// 获取按经纬度附近的数据
			geoLat = intent.getDoubleExtra("geoLat", -1);
			geoLng = intent.getDoubleExtra("geoLng", -1);
			// 把标志归原
			downLoadGeo();
			if (stamp.equals("hotel_list")) {
				top_title.setText("住山庄");
				stamp = "hotel";
			} else if (stamp.equals("restaurant_list")) {
				top_title.setText("吃乡味");
				stamp = "restaurant";
			} else if (stamp.equals("sights_list")) {
				top_title.setText("游美景");
				stamp = "sights";
			}
		} else {
			// 按县省获取附近数据
			area_id = intent.getIntExtra("area_id", -1);
			downloadData();
			if (stamp.equals("hotel")) {
				top_title.setText("住山庄");
			} else if (stamp.equals("restaurant")) {
				top_title.setText("吃乡味");
			} else if (stamp.equals("sights")) {
				top_title.setText("游美景");
			}
		}
	}

	/**
	 * 按县省获取附近数据
	 * */
	private void downloadData() {
		String url = "http://www.xiangyouji.com.cn:3000/" + stamp + "/area_id/"
				+ area_id + "/startIndex/0/length/10";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					actDatas = jsonToString(arg1, stamp);// 已经得到一些数据 但是图片只有地址
					// 填充listview数据
					apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
					list_sleep.setAdapter(apr);
				} catch (Exception e) {
					System.out.println("12错误:" + e.getMessage());
				}
				dialog.dismiss();
				super.onSuccess(arg0, arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				// TODO Auto-generated method stub
				msg("获取县省json失败");
				dialog.dismiss();
				super.onFailure(arg0, arg1);
			}
		});
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
						actDatas.addAll(jsonToString(result, stamp));// 已经得到一些数据
						// 让listview自动刷新
						apr.notifyDataSetChanged();
						finish = true;
					}
					// 去掉页脚
					list_sleep.removeFooterView(footer);
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
	 * viewpager适配器 内部类
	 */
	private class MyAdapter extends PagerAdapter {

		private SleepActData data;
		private View p;
		private List<View> list;

		public MyAdapter(SleepActData data, List<View> list) {
			this.data = data;
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.getImgDatas().size();
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
			container.removeView(p);
			container.addView(p);
			return p;
		}
	}

	/**
	 * 获取按经纬度附近的数据
	 * */
	private void downLoadGeo() {
		String url = "http://www.xiangyouji.com.cn:3000/nearby/" + stamp
				+ "/longitude/" + geoLng + "/latitude/" + geoLat
				+ "/startIndex/0/length/10";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					// 此时stamp改变了 所以可以用这个解析
					actDatas = jsonToString(arg1, stamp);// 已经得到一些数据 但是图片只有地址
					// 填充listview数据
					apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
					list_sleep.setAdapter(apr);
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
				}
				dialog.dismiss();
				super.onSuccess(arg0, arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				// TODO Auto-generated method stub
				msg("获取经纬度json失败");
				dialog.dismiss();
				super.onFailure(arg0, arg1);
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		/**
		 * 未登录 先登录
		 * */
		if (requestCode == 3) {
			if (resultCode == Activity.RESULT_OK) {
				// 去收藏
				msg("登录成功");
			} else {
				msg("需要登陆后才能做此操作");
				return;
			}
		}
		/**
		 * 从详细页回来 判断是否被收藏
		 * */
		if (requestCode == 9) {
			// msg("详细页归来");
		}
		getAllSave();
		for (int i = 0; i < actDatas.size(); i++) {
			boolean l = false;
			for (int j = 0; j < allSave.size(); j++) {
				if (allSave.get(j).getEntity_id()
						.equals(actDatas.get(i).get_id())
						&& allSave.get(j).getEntity_type() == en_type) {
					l = true;
				}
			}
			if (l) {
				actDatas.get(i).getSleep_love_img()
						.setImageDrawable(getImgResource(R.drawable.love));
				if (!actDatas.get(i).isSaved()) {
					int num = actDatas.get(i).getCollection_count() + 1;
					actDatas.get(i).setCollection_count(num);
					actDatas.get(i).getCollection().setText(num + "+");
				}
				// 重置
				actDatas.get(i).setSaved(true);
			} else {
				actDatas.get(i).getSleep_love_img()
						.setImageDrawable(getImgResource(R.drawable.lovef));
				if (actDatas.get(i).isSaved()) {
					int num = actDatas.get(i).getCollection_count() - 1;
					actDatas.get(i).setCollection_count(num);
					actDatas.get(i).getCollection().setText(num + "+");
				}
				// 重置
				actDatas.get(i).setSaved(false);
			}
		}

	}

	/**
	 * 方法必须重写
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出程序时结束所有的下载任务
		mImageDownLoader.cancelTask();
	}
}
