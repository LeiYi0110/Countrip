package com.pi9Lin.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.pi9Lin.data.NearBy;
import com.pi9Lin.data.SleepActData;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.navi.NaviStartActivity;
import com.pi9Lin.utils.DistanceCalculator;

public class DetailActivity extends BaseActivity {
	
	private LayoutInflater li;
	private RelativeLayout backward;
	private RelativeLayout share_btn;
	private TextView top_title;
	private MyListView mlist;
	private MyBaseApr myBaseApr;
	private String stamp;
	private String _id;
	
	
	private int en_type;
	private int index;
	private boolean flag;
	private SleepActData actData;
	private MyViewPageApr pageApr;
	private Dialog dialog;
	private ImageView save_it;
	private String[] imageUrls=null;
	public TextView xiabiao;
	public ViewPager vPage;
	private RelativeLayout to_comment;
	private RelativeLayout to_book;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		li = LayoutInflater.from(context);
		setContentView(R.layout.act_detail);
		getAllSave();
		ShareSDK.initSDK(this); // 分享
		init();
		initActionBar();
		findById(); // 资源初始化
		setOnClickListener();
		initImageLoader(); // 初始化图片异步处理对象
		initList(); // 填充页面
		downloadData();
		dialog = dialog(this, "正在加载...");
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
	}
	
	/**
	 * 获取吃住玩详细
	 * */
	private void downloadData() {
		String url = "http://www.xiangyouji.com.cn:3000/" + stamp + "/id/"
				+ _id + "";
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				try {
					if (checkJson(arg1)) {
						actData=jieXi(arg1, stamp);
						int length = actData.getImgDatas().size();
						imageUrls=new String[length];
						for (int i = 0; i < length; i++) {
							imageUrls[i]=actData.getImgDatas().get(i).getUrl();
						}
						myBaseApr.notifyDataSetChanged();
					} else {
						msg("错误:" + arg1);
					}
				} catch (Exception e) {
					msg("错误:" + e.getMessage());
				}
				dialog.dismiss();
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}
	
	private SleepActData jieXi(String s, String stamp) throws Exception {
		SleepActData data = new SleepActData();
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
		String restaurant_desc = null;
		if (stamp.equals("hotel")) {
			restaurant_name = jsonObject2.getString("hotel_name");
			restaurant_telephone = jsonObject2.getString("hotel_telephone");
			restaurant_address = jsonObject2.getString("hotel_address");
			restaurant_desc = jsonObject2.getString("hotel_desc");
		} else if (stamp.equals("restaurant")) {
			restaurant_name = jsonObject2.getString("restaurant_name");
			restaurant_telephone = jsonObject2
					.getString("restaurant_telephone");
			restaurant_address = jsonObject2.getString("restaurant_address");
			restaurant_desc = jsonObject2.getString("restaurant_desc");
		} else if (stamp.equals("sights")) {
			restaurant_name = jsonObject2.getString("sights_name");
			restaurant_telephone = jsonObject2.getString("sights_telephone");
			restaurant_address = jsonObject2.getString("sights_address");
			restaurant_desc = jsonObject2.getString("sights_desc");
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
		data.set_id(_id);
		data.setArea_id(area_id);
		data.setCity_id(city_id);
		data.setShop_id(shop_id);
		data.setXx_name(restaurant_name);
		data.setXx_address(restaurant_address);
		data.setXx_telephone(restaurant_telephone);
		data.setXx_desc(restaurant_desc);
		data.setCity(city);
		// data.setVillage(village);
		// data.setLocation_value(location_value);
		data.setCover(cover);
		data.setImgDatas(imgDatas);
		data.setGps(gps);
		return data;
	}
	/**
	 * 获取标签和id
	 * */
	private void init() {
		Intent intent = getIntent();
		stamp = intent.getStringExtra("stamp");
		_id = intent.getStringExtra("_id"); // 这里的_id其实是en_id
		en_type = intent.getIntExtra("en_type", 0);
		index = intent.getIntExtra("index", 0);
		Log.d("_id", _id);
		Log.d("en_type", en_type+"");
		Log.d("index", index+"");
		Log.d("stamp", stamp);
		String en_id = _id;
		int en_type = 0;
		if (stamp.equals("hotel")) {
			en_type = 1;
		} else if (stamp.equals("restaurant")) {
			en_type = 2;
		} else if (stamp.equals("sights")) {
			en_type = 3;
		}
		flag = false;
		for (int j = 0; j < allSave.size(); j++) {
			if (allSave.get(j).getEntity_id().equals(en_id)
					&& allSave.get(j).getEntity_type() == en_type) {
				flag = true;
			}
		}
	}
	
	private void initList() {
		// TODO Auto-generated method stub
		myBaseApr = new MyBaseApr();
		mlist.setAdapter(myBaseApr);
	}

	private class MyBaseApr extends BaseAdapter{
		/**
		 * ListView包含不同Item的布局 　　我们需要做这些工作: 　　1）重写 getViewTypeCount() –
		 * 该方法返回多少个不同的布局 　　2）重写 getItemViewType(int) – 根据position返回相应的Item 　
		 * 3）根据view item的类型，在getView中创建正确的convertView
		 * */
		final int TYPE_1 = 0;
		final int TYPE_2 = 1;
		final int TYPE_3 = 2;
		final int TYPE_4 = 3;
		final int TYPE_5 = 4;
		
		double la;
		double ln;
		
		public MyBaseApr(){
			String lat = preferences.getString("geoLat", null);
			String lng = preferences.getString("geoLng", null);
			la = Double.valueOf(lat).doubleValue();
			ln = Double.valueOf(lng).doubleValue();
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
			} else if (position == 3) {
				return TYPE_4;
			}else{
				return TYPE_5;
			}
		}
		
		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 5;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if(actData==null){
				return 0;
			}else{
				return getViewTypeCount();
			}
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
		
		private class Holder{
			ViewPager vPage;
			TextView xiabiao;
			ImageView love;
			TextView title;
			TextView distance;
			TextView telephone;
			TextView address;
			RelativeLayout seeAll;
			ImageView tel;
			ImageView you_img;
			ImageView zhu_img;
			ImageView chi_img;
			ImageView guoshu_img;
			TextView you_txt;
			TextView zhu_txt;
			TextView chi_txt;
			LinearLayout show_tel;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v = null;
			final Holder h;
			int type = getItemViewType(arg0);
			if(arg1==null){
				h=new Holder();
				if (type == TYPE_1) {
					v = li.inflate(R.layout.item_detail_top, null);
					h.vPage = (ViewPager) v.findViewById(R.id.vPage);
					h.xiabiao = (TextView) v.findViewById(R.id.detail_num);
					h.love = (ImageView) v.findViewById(R.id.love);
					save_it=h.love;
					xiabiao=h.xiabiao;
					vPage=h.vPage;
				}else if(type == TYPE_2){
					v = li.inflate(R.layout.activity_detail_2, null);
					h.title=(TextView)v.findViewById(R.id.title);
					h.distance=(TextView)v.findViewById(R.id.distance);
					h.telephone=(TextView)v.findViewById(R.id.telephone);
					h.address=(TextView)v.findViewById(R.id.address);
					h.tel=(ImageView)v.findViewById(R.id.tel);
					h.show_tel=(LinearLayout)v.findViewById(R.id.show_tel);
				}else if(type==TYPE_3){
					v = li.inflate(R.layout.activity_detail_3, null);
					h.love=(ImageView)v.findViewById(R.id.map_static);
				}else if(type==TYPE_4){
					v = li.inflate(R.layout.layout_youdetail_introduce, null);
					h.title=(TextView)v.findViewById(R.id.desc);
					h.xiabiao=(TextView)v.findViewById(R.id.view_all);
					h.seeAll=(RelativeLayout)v.findViewById(R.id.seeAll);
					h.love=(ImageView)v.findViewById(R.id.second);
				}else if(type==TYPE_5){
					v = li.inflate(R.layout.layout_youdetail_round, null);
					h.you_img=(ImageView)v.findViewById(R.id.you_img);
					h.zhu_img=(ImageView)v.findViewById(R.id.zhu_img);
					h.chi_img=(ImageView)v.findViewById(R.id.chi_img);
					h.guoshu_img=(ImageView)v.findViewById(R.id.guoshu_img);
					h.you_txt=(TextView)v.findViewById(R.id.you_txt);
					h.zhu_txt=(TextView)v.findViewById(R.id.zhu_txt);
					h.chi_txt=(TextView)v.findViewById(R.id.chi_txt);
				}
				v.setTag(h);
			}else{
				v = arg1;
				h = (Holder)v.getTag();
			}
			if(type == TYPE_1){
				pageApr = new MyViewPageApr();
				h.vPage.setAdapter(pageApr);
				h.vPage.setCurrentItem(index);
				h.xiabiao.setText((index + 1) + "/" + actData.getImgDatas().size());
				// 绑定动作监听器：如翻页的动画
				h.vPage.setOnPageChangeListener(new OnPageChangeListener() {
					@Override
					public void onPageSelected(int arg0) {
						// TODO Auto-generated method stub
						index=arg0;
						h.xiabiao.setText((arg0 + 1) + "/"
								+ actData.getImgDatas().size());
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
				if (flag) {
					// 有收藏
					h.love.setImageDrawable(getImgResource(R.drawable.love));
				} else {
					h.love.setImageDrawable(getImgResource(R.drawable.lovef));
				}
				/** 收藏 */
				h.love.setOnClickListener(new OnClickListener() {
					boolean flagff = flag;

					@Override
					public void onClick(View arg0) {
						// 先判断有没有登录
						boolean isLandIn = preferences.getBoolean("isLandIn", false);
						if (!isLandIn) {
							Intent intent = new Intent(context, DengLuActivity.class);
							startActivityForResult(intent, 8);
						} else {
							if (flagff) {
								String url = "http://www.xiangyouji.com.cn:3000/my/removeCollection";
								AsyncHttpClient client = new AsyncHttpClient();
								PersistentCookieStore myCookieStore = new PersistentCookieStore(
										context);
								client.setCookieStore(myCookieStore);
								RequestParams params = new RequestParams();
								params.put("entity_type", en_type + "");
								params.put("entity_id", _id);
								client.post(url, params,
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(int arg0,
													String result) {
												try {
													if (checkJson(result)) {
														msg("取消成功");
														h.love.setImageDrawable(getImgResource(R.drawable.lovef));
														flagff = false;
														/** 修改本地收藏缓存 */
														for (int j = 0; j < allSave
																.size(); j++) {
															if (allSave.get(j)
																	.getEntity_id()
																	.equals(_id)
																	&& allSave
																			.get(j)
																			.getEntity_type() == en_type) {
																allSave.remove(j);
															}
														}
														String ss = obj2Json(allSave);
														mCache.put("allSave", ss);
													} else {
														msg("取消失败--数据问题");
													}
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}

											@Override
											public void onFailure(Throwable arg0,
													String arg1) {
												Toast.makeText(context, "取消失败--网络问题",
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
								params.put("entity_type", en_type + "");
								params.put("entity_id", _id);
								client.post(url, params,
										new AsyncHttpResponseHandler() {
											@Override
											public void onSuccess(int arg0,
													String result) {
												try {
													if (checkJson(result)) {
														msg("收藏成功");
														h.love.setImageDrawable(getImgResource(R.drawable.love));
														flagff = true;
														/** 修改本地收藏缓存 */
														Entity entity = new Entity();
														entity.setEntity_id(_id);
														entity.setEntity_type(en_type);
														allSave.add(entity);
														String ss = obj2Json(allSave);
														mCache.put("allSave", ss);
													} else {
														msg("收藏失败--数据问题");
													}
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}

											@Override
											public void onFailure(Throwable arg0,
													String arg1) {
												Toast.makeText(context, "收藏失败--网络问题",
														Toast.LENGTH_SHORT).show();
												super.onFailure(arg0, arg1);
											}
										});
							}
						}
					}
				});
			}else if(type==TYPE_2){
				top_title.setText(actData.getXx_name());
				h.title.setText(actData.getXx_name());
				h.address.setText(actData.getXx_address());
				h.distance.setText(DistanceCalculator.GetDistance(la, ln,
						actData.getGps()[0], actData.getGps()[1])
						+ "km");
				final String phone1=actData.getXx_telephone();
				if(phone1.equals("")||phone1==null){
					h.show_tel.setVisibility(View.GONE);
					h.tel.setVisibility(View.GONE);
				}else{
					h.show_tel.setVisibility(View.VISIBLE);
					h.tel.setVisibility(View.VISIBLE);
					h.telephone.setText(phone1);
					h.tel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							// TODO Auto-generated method stub
							Intent phoneIntent = new Intent("android.intent.action.CALL",
									Uri.parse("tel:" + phone1));
							startActivity(phoneIntent);
						}
					});
				}
			}else if(type==TYPE_3){
				final double geoLat = actData.getGps()[0];
				final double geoLng = actData.getGps()[1];
				String gps = geoLng + "," + geoLat;
				String url = "http://restapi.amap.com/v3/staticmap?scale=2&location="
						+ gps + "&zoom=12&labels=" + actData.getXx_name()
						+ ",2,0,30,0xFFFFFF,0x008000:" + gps
						+ "&size=440*280&markers=large,0xffa500,B:" + gps
						+ "&key=ee95e52bf08006f63fd29bcfbcf21df0";
				imageLoader.displayImage(url, h.love, options,animateFirstListener);
				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, NaviStartActivity.class);
						intent.putExtra("detail", true);
						intent.putExtra("geoLat", geoLat);
						intent.putExtra("geoLng", geoLng);
						intent.putExtra("en_type", en_type);
						intent.putExtra("en_id", _id);
						intent.putExtra("address", actData.getXx_address());
						intent.putExtra("name", actData.getXx_name());
						startActivity(intent);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
			}else if(type==TYPE_4){
				h.title.setText(actData.getXx_desc());
				h.seeAll.setOnClickListener(new OnClickListener() {
					Boolean flagg = true;
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						if (flagg) {
							flagg = false;
							h.title.setEllipsize(null); // 展开
							h.title.setSingleLine(flagg);
							h.xiabiao.setText("收起");
						} else {
							flagg = true;
							h.title.setEllipsize(TextUtils.TruncateAt.END); // 收缩
							h.title.setLines(6);
							h.xiabiao.setText("查看全部");
						}
					}
				});
				String url=actData.getImgDatas().get(1).getUrl();//第二张图
				imageLoader.displayImage(url, h.love, options,animateFirstListener);
			}else if(type==TYPE_5){
				final double geoLat = actData.getGps()[0];
				final double geoLng = actData.getGps()[1];
				final String[] dd = { "restaurant_count", "sights_count", "hotel_count" };
				AsyncHttpClient client = new AsyncHttpClient();
				for (int i = 0; i < dd.length; i++) {
					final int indexf = i;
					String url = "http://www.xiangyouji.com.cn:3000/nearby/" + dd[i]
							+ "/longitude/" + geoLng + "/latitude/" + geoLat + "";
					client.get(url, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int arg0, String arg1) {
							/** 成功获取附近吃的数量数据 */
							try {
								NearBy ss = geoJsonToString(arg1);
								if (indexf == 0) {
									h.chi_txt.setText("吃" + ss.getCount());
									imageLoader.displayImage(ss.getCover(), h.chi_img, options,animateFirstListener);
									if(ss.getCount()==0){
										h.chi_img.setImageDrawable(getImgResource(R.drawable.fjc));
									}
								} else if (indexf == 1) {
									h.you_txt.setText("游" + ss.getCount());
									imageLoader.displayImage(ss.getCover(), h.you_img, options,animateFirstListener);
									if(ss.getCount()==0){
										h.you_img.setImageDrawable(getImgResource(R.drawable.fjy));
									}
								} else if (indexf == 2) {
									h.zhu_txt.setText("住" + ss.getCount());
									imageLoader.displayImage(ss.getCover(), h.zhu_img, options,animateFirstListener);
									if(ss.getCount()==0){
										h.zhu_img.setImageDrawable(getImgResource(R.drawable.fjz));
									}
								}
							} catch (Exception e) {
								System.out.println("错误:" + e.getMessage());
							}
							super.onSuccess(arg0, arg1);
						}

						@Override
						public void onFailure(Throwable arg0, String arg1) {
							// TODO Auto-generated method stub
							Toast.makeText(context, "您的网络出现问题", Toast.LENGTH_SHORT)
									.show();
							super.onFailure(arg0, arg1);
						}
					});
				}
				h.chi_img.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("roundData", true);
						intent.putExtra("stamp", "restaurant_list");
						intent.putExtra("geoLat", geoLat);
						intent.putExtra("geoLng", geoLng);
						intent.putExtra("en_type", 2);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
				h.zhu_img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("roundData", true);
						intent.putExtra("stamp", "hotel_list");
						intent.putExtra("geoLat", geoLat);
						intent.putExtra("geoLng", geoLng);
						intent.putExtra("en_type", 1);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
				h.you_img.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, ListActivity.class);
						intent.putExtra("roundData", true);
						intent.putExtra("stamp", "sights_list");
						intent.putExtra("geoLat", geoLat);
						intent.putExtra("geoLng", geoLng);
						intent.putExtra("en_type", 3);
						startActivity(intent);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
				h.guoshu_img.setImageDrawable(getImgResource(R.drawable.guoshu));
				h.guoshu_img.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						msg("这个可以有，敬请期待...");
					}
				});
			}
			return v;
		}
	}
	
	private class MyViewPageApr extends PagerAdapter {

		private int mChildCount = 0;
		private View p;
		private ImageView iv;

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return actData.getImgDatas().size();
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
			iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(context, ImagePagerActivity.class);
					intent.putExtra("images", imageUrls);
					intent.putExtra("position", position);
					startActivityForResult(intent, 1);
				}
			});
			String url=actData.getImgDatas().get(position).getUrl();
			imageLoader.displayImage(url, iv, options,animateFirstListener);
			container.removeView(p);
			container.addView(p);
			return p;
		}
	}
	
	private void setOnClickListener() {
		/**
		 * 搜索
		 * */
		share_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				/** 去分享 */
				if(actData!=null){
					String title=actData.getTitle();
					String desc=actData.getXx_desc();
					showShare(title,desc);
				}
			}
		});
		/**
		 * 返回
		 * */
		backward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 数据是使用Intent返回
				Intent intent = new Intent();
				// 把返回数据存入Intent
				intent.putExtra("result", "My name is linjiqin");
				// 设置返回数据
				setResult(RESULT_CANCELED, intent);
				// 关闭Activity
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
		to_comment.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, YouCommentActivity.class);
				intent.putExtra("top_title", actData.getXx_name());
				startActivity(intent);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
		to_book.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, YouBookActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
	}
	
	private void findById() {
		// TODO Auto-generated method stub
		mlist = (MyListView)findViewById(R.id.lv);
		to_comment=(RelativeLayout)findViewById(R.id.to_comment);
		to_book=(RelativeLayout)findViewById(R.id.to_book);
	}
	
	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.layout_youdetail_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		top_title = (TextView) v.findViewById(R.id.top_title);
		backward = (RelativeLayout) v.findViewById(R.id.backward);
		share_btn = (RelativeLayout) v.findViewById(R.id.share_btn);
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		if (arg0 == 8 && arg1 == RESULT_OK) {
			// 登录成功
			msg("登录成功");
			getAllSave();
			ss();
		} 
		
		else if(arg0 == 8 && arg1 != RESULT_OK) {
			msg("需要登陆后才能做此操作");
		}
		
		if(arg0==1 && arg1 == RESULT_OK){
			int position=arg2.getIntExtra("position", -1);
			if(position>-1){
				xiabiao.setText((position+1)+"/"+actData.getImgDatas().size());
				vPage.setCurrentItem(position);
			}
		}
	}
	
	private void ss() {
		// TODO Auto-generated method stub
		String en_id = _id;
		int en_type = 0;
		if (stamp.equals("hotel")) {
			en_type = 1;
		} else if (stamp.equals("restaurant")) {
			en_type = 2;
		} else if (stamp.equals("sights")) {
			en_type = 3;
		}
		flag = false;
		for (int j = 0; j < allSave.size(); j++) {
			if (allSave.get(j).getEntity_id().equals(en_id)
					&& allSave.get(j).getEntity_type() == en_type) {
				flag = true;
			}
		}
		if (flag) {
			// 有收藏
			save_it.setImageDrawable(getImgResource(R.drawable.love));
		} else {
			save_it.setImageDrawable(getImgResource(R.drawable.lovef));
		}
	}
	
}
