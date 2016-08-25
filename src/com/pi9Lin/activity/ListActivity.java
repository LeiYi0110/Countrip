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
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.IndexFragTheme;
import com.pi9Lin.data.SleepActData;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.search.SearchActivity;
import com.pi9Lin.utils.DistanceCalculator;

@SuppressLint("ResourceAsColor")
public class ListActivity extends BaseActivity {
	
	private TextView top_title;
	private RelativeLayout top_back;
	private RelativeLayout search_btn;
	private MyBaseApr myBaseApr;
	private MyBaseAprf myBaseAprf;
	private MyListView mlist;
	private List<SleepActData> actDatas = new ArrayList<SleepActData>();
	private LayoutInflater li;
	private View footer;
	private String stamp=null;
	private int en_type;
	private boolean roundDataflag;
	private double geoLat;
	private double geoLng;
	private int area_id;
	private Dialog dialog;
	private boolean finish = true;// 是否加载完成
	private int order;
	private List<IndexFragTheme> themes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		li = LayoutInflater.from(context);
		footer = li.inflate(R.layout.footer, null);
		setContentView(R.layout.activity_list);
		getAllSave();
		initActionbar();
		findById(); // 资源初始化
		setOnClickListener();
		initImageLoader(); // 初始化图片异步处理对象
		initList(); // 填充页面
		dialog = dialog(this, "正在加载...");
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		receiveIntent(); //	接收数据  异步下载
	}
	
	private void receiveIntent() {
		/**
		 * 判断该查询什么数据
		 * */
		Intent intent = getIntent();
		stamp = intent.getStringExtra("stamp");
		en_type = intent.getIntExtra("en_type", -1);
		order=intent.getIntExtra("order", -1);
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
			if(order>=0){
				//栏目第一张图跳过来的
				try {
					themes=getThemesJson();
					Log.d("dddd2", themes.get(0).getEntities().size()+"");
					String title=themes.get(order).getTitle();
					top_title.setText(title);
					mlist.addFooterView(footer);
					myBaseAprf=new MyBaseAprf();
					mlist.setAdapter(myBaseAprf);
					mlist.removeFooterView(footer);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					msg("错误哈哈:"+e.getMessage());
				}
				dialog.dismiss();
			}else{
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
					myBaseApr.notifyDataSetChanged();// 通知listview更新
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
					myBaseApr.notifyDataSetChanged();// 通知listview更新
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
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

	private void setOnClickListener() {
		/**
		 * 搜索
		 * */
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(context, SearchActivity.class));
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		/**
		 * 返回
		 * */
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
		// TODO Auto-generated method stub
		mlist = (MyListView)findViewById(R.id.lv);
	}

	private void initList() {
		// TODO Auto-generated method stub
		try {
			mlist.addFooterView(footer);
			myBaseApr = new MyBaseApr();
			mlist.setAdapter(myBaseApr);
			mlist.removeFooterView(footer);
		} catch (Exception e) {
			// TODO: handle exception
		}
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
	
	private class MyBaseAprf extends BaseAdapter{
		
		double la;
		double ln;
		
		public MyBaseAprf() {
			// TODO Auto-generated constructor stub
			String lat = preferences.getString("geoLat", null);
			String lng = preferences.getString("geoLng", null);
			la = Double.valueOf(lat).doubleValue();
			ln = Double.valueOf(lng).doubleValue();
			mlist.setOnScrollListener(null);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return themes.get(order).getEntities().size();
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
		
		private void resetViewHolder(Holder h) {
			h.vPage.setAdapter(null);
			h.logo.setImageDrawable(null);
			h.love_img.setImageDrawable(null);
			h.title.setText(null);
			h.subtitle.setText(null);
			h.love_num.setText(null);
			h.comment_num.setText(null);
			h.distance_num.setText(null);
			h.vPage_num.setText(null);
			h.to_love.setOnClickListener(null);
			h.to_comment.setOnClickListener(null);
		}
		
		private class Holder{
			ViewPager vPage;
			ImageView logo;
			TextView title;
			TextView subtitle;
			RelativeLayout to_love;
			RelativeLayout to_comment;
			ImageView love_img;
			TextView love_num;
			TextView comment_num;
			TextView distance_num;
			TextView vPage_num;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v;
			final Holder h;
			if(arg1==null){
				v=li.inflate(R.layout.item_list_listitem, null);
				h = new Holder();
				h.vPage=(ViewPager)v.findViewById(R.id.vPage);
				h.logo=(ImageView)v.findViewById(R.id.logo);
				h.title=(TextView)v.findViewById(R.id.title);
				h.subtitle=(TextView)v.findViewById(R.id.subtitle);
				h.to_love=(RelativeLayout)v.findViewById(R.id.to_love);
				h.to_comment=(RelativeLayout)v.findViewById(R.id.to_comment);
				h.love_img=(ImageView)v.findViewById(R.id.love_img);
				h.love_num=(TextView)v.findViewById(R.id.love_num);
				h.comment_num=(TextView)v.findViewById(R.id.comment_num);
				h.distance_num=(TextView)v.findViewById(R.id.distance_num);
				h.vPage_num=(TextView)v.findViewById(R.id.vPage_num);
				v.setTag(h);
			}else{
				v = arg1;
				h = (Holder) v.getTag();
				resetViewHolder(h);
			}
			h.title.setText(themes.get(order).getEntities().get(arg0).getName());
			en_type=themes.get(order).getEntities().get(arg0).getEntity_type();
			if (en_type == 1) {
				h.logo.setImageDrawable(getImgResource(R.drawable.sleep));
				h.subtitle.setText("乡村住宿");
				stamp = "hotel";
			} else if (en_type == 2) {
				h.logo.setImageDrawable(getImgResource(R.drawable.eat));
				h.subtitle.setText("乡村美食");
				stamp = "restaurant";
			} else if (en_type == 3) {
				h.logo.setImageDrawable(getImgResource(R.drawable.play));
				h.subtitle.setText("乡村景点");
				stamp = "sights";
			}
			h.distance_num.setText(DistanceCalculator.GetDistance(la, ln, themes
					.get(order).getEntities().get(arg0).getGps()[0],themes
					.get(order).getEntities().get(arg0).getGps()[1])
					+ "km");
			h.to_comment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// mark
					startActivity(new Intent(context, ShowCommentActivity.class));
				}
			});
			h.comment_num.setText("0+");
			/**
			 * 先判断是否已经被收藏
			 * */
			final String en_id = themes.get(order).getEntities().get(arg0).getEntity_id();
			final int en_typef = en_type;
			final int fff = arg0;
			themes.get(order).getEntities().get(arg0).setImageView(h.love_img);
			themes.get(order).getEntities().get(arg0).setCount_num(h.love_num);
			h.love_num.setText(themes.get(order).getEntities().get(arg0).getCollected_count()
					+ "+");
			if (isSaved(en_id, en_typef)) {
				// 收藏了
				h.love_img.setImageDrawable(getImgResource(R.drawable.love));
				h.love_num.setTextColor(R.color.hongse);
				themes.get(order).getEntities().get(arg0).setSaved(true);
			} else {
				// 未收藏
				h.love_img.setImageDrawable(getImgResource(R.drawable.lovef));
				h.love_num.setTextColor(R.color.huise);
				themes.get(order).getEntities().get(arg0).setSaved(false);
			}
			/**
			 * 收藏监听
			 * */
			h.to_love.setOnClickListener(new OnClickListener() {

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
													h.love_img
															.setImageDrawable(getImgResource(R.drawable.lovef));
													int sum = themes.get(order).getEntities()
															.get(fff)
															.getCollected_count();
													sum--;
													h.love_num
															.setText(sum + "+");
													h.love_num
															.setTextColor(R.color.huise);
													themes.get(order).getEntities().get(fff)
															.setCollected_count(
																	sum);
													themes.get(order).getEntities().get(fff).setSaved(
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
													h.love_img
															.setImageDrawable(getImgResource(R.drawable.love));
													int sum = themes.get(order).getEntities()
															.get(fff)
															.getCollected_count();
													sum++;
													h.love_num
															.setText(sum + "+");
													h.love_num
															.setTextColor(R.color.hongse);
													themes.get(order).getEntities().get(fff)
													.setCollected_count(
															sum);
													themes.get(order).getEntities().get(fff).setSaved(
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
			final int length = themes.get(order).getEntities().get(arg0).getImages().size();
			MyThemeVpageAprf themeAprf = new MyThemeVpageAprf(themes.get(order).getEntities(),arg0);
			h.vPage.setAdapter(themeAprf);
			h.vPage.setCurrentItem(themes.get(order).getEntities().get(arg0).getPage_index());
			if (length > 0) {
				h.vPage_num.setText((themes.get(order).getEntities().get(arg0).getPage_index() + 1)
						+ "/" + length);
			} else if (length == 0) {
				h.vPage_num.setText("0/0");
			}
			h.vPage.setOnPageChangeListener(new OnPageChangeListener() {
				
				@Override
				public void onPageSelected(int arg0) {
					// TODO Auto-generated method stub
					themes.get(order).getEntities().get(fff).setPage_index(arg0);
					h.vPage_num.setText((arg0+1)+"/"+length);
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
	}
	
	private class MyBaseApr extends BaseAdapter implements OnScrollListener{
		
		double la;
		double ln;
		
		public MyBaseApr() {
			// TODO Auto-generated constructor stub
			String lat = preferences.getString("geoLat", null);
			String lng = preferences.getString("geoLng", null);
			la = Double.valueOf(lat).doubleValue();
			ln = Double.valueOf(lng).doubleValue();
			mlist.setOnScrollListener(this);
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
		
		private void resetViewHolder(Holder h) {
			h.vPage.setAdapter(null);
			h.logo.setImageDrawable(null);
			h.love_img.setImageDrawable(null);
			h.title.setText(null);
			h.subtitle.setText(null);
			h.love_num.setText(null);
			h.comment_num.setText(null);
			h.distance_num.setText(null);
			h.vPage_num.setText(null);
			h.to_love.setOnClickListener(null);
			h.to_comment.setOnClickListener(null);
		}
		
		private class Holder{
			ViewPager vPage;
			ImageView logo;
			TextView title;
			TextView subtitle;
			RelativeLayout to_love;
			RelativeLayout to_comment;
			RelativeLayout distance;
			ImageView love_img;
			TextView love_num;
			TextView comment_num;
			TextView distance_num;
			TextView vPage_num;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v;
			final Holder h;
			if(arg1==null){
				v=li.inflate(R.layout.item_list_listitem, null);
				h = new Holder();
				h.vPage=(ViewPager)v.findViewById(R.id.vPage);
				h.logo=(ImageView)v.findViewById(R.id.logo);
				h.title=(TextView)v.findViewById(R.id.title);
				h.subtitle=(TextView)v.findViewById(R.id.subtitle);
				h.to_love=(RelativeLayout)v.findViewById(R.id.to_love);
				h.distance=(RelativeLayout)v.findViewById(R.id.distance);
				h.to_comment=(RelativeLayout)v.findViewById(R.id.to_comment);
				h.love_img=(ImageView)v.findViewById(R.id.love_img);
				h.love_num=(TextView)v.findViewById(R.id.love_num);
				h.comment_num=(TextView)v.findViewById(R.id.comment_num);
				h.distance_num=(TextView)v.findViewById(R.id.distance_num);
				h.vPage_num=(TextView)v.findViewById(R.id.vPage_num);
				v.setTag(h);
			}else{
				v = arg1;
				h = (Holder) v.getTag();
				resetViewHolder(h);
			}
			h.title.setText(actDatas.get(arg0).getXx_name());
			if (stamp.equals("hotel")) { //可能是按照entity_type来
				h.logo.setImageDrawable(getImgResource(R.drawable.sleep));
				h.subtitle.setText("乡村住宿");
			} else if (stamp.equals("restaurant")) {
				h.logo.setImageDrawable(getImgResource(R.drawable.eat));
				h.subtitle.setText("乡村美食");
			} else if (stamp.equals("sights")) {
				h.logo.setImageDrawable(getImgResource(R.drawable.play));
				h.subtitle.setText("乡村景点");
			}
			if(actDatas.get(arg0).getArea_id()==area_id){
				h.distance.setVisibility(View.GONE);
			}else{
				h.distance.setVisibility(View.VISIBLE);
				h.distance_num.setText(DistanceCalculator.GetDistance(la, ln, actDatas
						.get(arg0).getGps()[0], actDatas.get(arg0).getGps()[1])
						+ "km");
			}
			h.to_comment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// mark
//					startActivity(new Intent(context, ShowCommentActivity.class));
					msg("评论功能正在赶来...");
				}
			});
			h.comment_num.setText(actDatas.get(arg0).getComment_count()+"+");
			/**
			 * 先判断是否已经被收藏
			 * */
			final String en_id = actDatas.get(arg0).get_id();
			final int en_typef = en_type;
			final int fff = arg0;
			actDatas.get(arg0).setSleep_love_img(h.love_img);
			actDatas.get(arg0).setCollection(h.love_num);
			h.love_num.setText(actDatas.get(arg0).getCollection_count()
					+ "+");
			if (isSaved(en_id, en_typef)) {
				// 收藏了
				h.love_img.setImageDrawable(getImgResource(R.drawable.love));
				h.love_num.setTextColor(R.color.hongse);
				actDatas.get(arg0).setSaved(true);
			} else {
				// 未收藏
				h.love_img.setImageDrawable(getImgResource(R.drawable.lovef));
				h.love_num.setTextColor(R.color.huise);
				actDatas.get(arg0).setSaved(false);
			}
			/**
			 * 收藏监听
			 * */
			h.to_love.setOnClickListener(new OnClickListener() {

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
													h.love_img
															.setImageDrawable(getImgResource(R.drawable.lovef));
													int sum = actDatas
															.get(fff)
															.getCollection_count();
													sum--;
													h.love_num
															.setText(sum + "+");
													h.love_num
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
													h.love_img
															.setImageDrawable(getImgResource(R.drawable.love));
													int sum = actDatas
															.get(fff)
															.getCollection_count();
													sum++;
													h.love_num
															.setText(sum + "+");
													h.love_num
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
			MyThemeVpageApr themeApr = new MyThemeVpageApr(actDatas.get(arg0).getImgDatas(),arg0);
			h.vPage.setAdapter(themeApr);
			h.vPage.setCurrentItem(actDatas.get(arg0).getPage_index());
			if (length > 0) {
				h.vPage_num.setText((actDatas.get(arg0).getPage_index() + 1)
						+ "/" + length);
			} else if (length == 0) {
				h.vPage_num.setText("0/0");
			}
			h.vPage.setOnPageChangeListener(new OnPageChangeListener() {
				
				@Override
				public void onPageSelected(int arg0) {
					// TODO Auto-generated method stub
					actDatas.get(fff).setPage_index(arg0);
					h.vPage_num.setText((arg0+1)+"/"+length);
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
		public void onScroll(AbsListView arg0, int firstVisibleItem, int visibleItemCount, int arg3) {
			// 当翻到最后一条数据时
			if (scrolled
					&& mlist.getChildAt(mlist
							.getLastVisiblePosition()
							- mlist.getFirstVisiblePosition()) != null
					&& mlist.getChildAt(
							mlist.getLastVisiblePosition()
									- mlist.getFirstVisiblePosition())
							.getBottom() <= mlist.getMeasuredHeight()
					&& finish) {

				// 已经移动到了listview的最后
				finish = false;
				// 添加页脚
				mlist.addFooterView(footer);
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
						actDatas.addAll(jsonToString(result, stamp));// 已经得到一些数据
						// 让listview自动刷新
						myBaseApr.notifyDataSetChanged();
						finish = true;
					}
					// 去掉页脚
					mlist.removeFooterView(footer);
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
	
	private class MyThemeVpageAprf extends PagerAdapter{
		
		private int mChildCount = 0;
		private View p;
		private ImageView iv;
		private List<Entity> data;
		int index;
		
		MyThemeVpageAprf(List<Entity> data,int index){
			this.data=data;
			this.index=index;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.get(index).getImages().size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==arg1;
		}
		
		@Override
		public void notifyDataSetChanged() {
			mChildCount = getCount();
			super.notifyDataSetChanged();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
//			container.removeView(p);
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
		public Object instantiateItem(ViewGroup container,final int position) {
			// 填充viewpage数据
			p = li.inflate(R.layout.item_indexfrag_pages, null);
			iv = (ImageView) p.findViewById(R.id.image);
			String url = data.get(index).getImages().get(position).getUrl();
			imageLoader.displayImage(url, iv, options, animateFirstListener);
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(context,
							DetailActivity.class);
					intent.putExtra("_id",data.get(index).getEntity_id());
					intent.putExtra("stamp",stamp);
					intent.putExtra("en_type",en_type);
					intent.putExtra("index",data.get(index).getPage_index());
					intent.putExtra("latitude",data.get(index).getGps()[0]);
					intent.putExtra("longitude",data.get(index).getGps()[1]);
					startActivityForResult(intent, 9);
					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				}
			});
			container.removeView(p);
			container.addView(p);
			return p;
		}
	} 
	
	private class MyThemeVpageApr extends PagerAdapter{
		
		private int mChildCount = 0;
		private View p;
		private ImageView iv;
		private List<SleepImgData> data;
		private int num;
		
		MyThemeVpageApr(List<SleepImgData> data,int num){
			this.data=data;
			this.num=num;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0==arg1;
		}
		
		@Override
		public void notifyDataSetChanged() {
			mChildCount = getCount();
			super.notifyDataSetChanged();
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
//			container.removeView(p);
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
			imageLoader.displayImage(url, iv, options, animateFirstListener);
			iv.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(context,
							DetailActivity.class);
					intent.putExtra("_id", actDatas.get(num).get_id());
					intent.putExtra("stamp", stamp);
					intent.putExtra("en_type", en_type);
					intent.putExtra("index", actDatas.get(num)
							.getPage_index());
					intent.putExtra("latitude", actDatas.get(num)
							.getGps()[0]);
					intent.putExtra("longitude", actDatas.get(num)
							.getGps()[1]);
					startActivityForResult(intent, 9);
					overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				}
			});
			container.removeView(p);
			container.addView(p);
			return p;
		}
		
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
	 * 解析栏目json
	 * */
	protected List<IndexFragTheme> getThemesJson() throws Exception {
		/** 获取全省精选的吃、住、游三个主题栏目 */
		List<IndexFragTheme> datas=new ArrayList<IndexFragTheme>();
		JSONArray array = mCache.getAsJSONArray("testJsonArray");
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
				Entity entity=new Entity();
				entity.setUrl(url);
				entity.setEntity_id(entity_id);
				entity.setOrder(orderf);
				entity.setEntity_type(entity_type);
				entity.setImages(imgDatas);
				entity.setGps(gps);
				entity.setName(name);
				entities.add(entity);
			}
			IndexFragTheme theme = new IndexFragTheme(cover, order, title,
					entities);
			datas.add(theme);
		}
		return datas;
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
}
