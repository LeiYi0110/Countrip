package com.pi9Lin.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
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
import com.pi9Lin.bins.VerticalScrollView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.NearBy;
import com.pi9Lin.data.SleepActData;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.imageLoader.ImageDownLoader;
import com.pi9Lin.imageLoader.ImageDownLoader.onImageLoaderListener;
import com.pi9Lin.navi.NaviStartActivity;
import com.pi9Lin.utils.ACache;
import com.pi9Lin.utils.DistanceCalculator;

/**
 * 景点详情Activity
 * 
 * */
@SuppressLint("NewApi")
public class YouDetailActivity extends BaseActivity {

	ViewPager you_vPager;
	TextView price, you_img_index, comment_sum, you_txt, zhu_txt, chi_txt,
			detail_title, desc,view_all;
	ImageView save_it, to_book, to_comment, you_img, zhu_img, chi_img,
			map_static,second,img_tel;
	VerticalScrollView mysv;
	final int MAX = 5;// 便利设施布局每行最多的个数
	private RelativeLayout top_back, to_share,seeAll;
	String stamp; // 标签
	String _id; // id

	protected TextView detail_addr;
	protected TextView title;
	protected TextView distance;
	protected TextView telephone;
	FrameLayout round_you, round_zhu, round_chi;
	private MyAdapter adapter;
	private int index = 0;
	private LinearLayout show_tel;

	private ImageDownLoader mImageDownLoader;

	/** 经纬度 */
	Double geoLat;
	Double geoLng;

	/** 用来接收轮播图片 */
	ImageView[] lbt_img;

	/** 测试数据 */
	int[] resBLSSImg = { R.drawable.wifi, R.drawable.airconditioner,
			R.drawable.elevater, R.drawable.alarm, R.drawable.tablephone,
			R.drawable.tablephone, R.drawable.tablephone };
	String[] resBLSSTxt = { "wifi", "空调", "电梯", "叫醒服务", "座机", "座机2", "座机3" };
	boolean[] isHave = { true, true, true, true, true, true, true };
	int sumOfComment = 4;
	int[] resImgFace = {  };
	String[] resLevel = { "路人甲", "一级吃货", "二级吃货", "二级吃货" };
	String[] resComment = {
			"本章内容是 android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com",
			"本章内容是 android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com",
			"本章内容是 android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com",
			"本章内容是 android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com" };
	
	private Dialog dialog;

	private boolean flag;
	private int en_type;

	protected SleepActData actData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_you_detail);
		findById();
		initActionBar();
		mImageDownLoader = new ImageDownLoader(context);
		getStampAndID();
		getAllSave();
		ss();
		initVPager(); 	// 顶部轮播图
		downloadData(); // 顶部轮播图
		setOnClickListener();
		ShareSDK.initSDK(this); // 分享
//		initComment();
		dialog = dialog(this, "正在加载...");
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
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

	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.layout_youdetail_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		detail_title = (TextView) v.findViewById(R.id.top_title);
		top_back = (RelativeLayout) v.findViewById(R.id.top_back);
		to_share = (RelativeLayout) v.findViewById(R.id.share_btn);
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
						jieXi(arg1, stamp);
						initTxt();
						getGeo();
						dialog.dismiss();
					} else {
						msg("错了：" + arg1);
					}
				} catch (Exception e) {
					dialog.dismiss();
					msg("错误:" + e.getMessage());
				}
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

	private void jieXi(String s, String stamp) throws Exception {
		actData = new SleepActData();
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
		actData.set_id(_id);
		actData.setArea_id(area_id);
		actData.setCity_id(city_id);
		actData.setShop_id(shop_id);
		actData.setXx_name(restaurant_name);
		actData.setXx_address(restaurant_address);
		actData.setXx_telephone(restaurant_telephone);
		actData.setXx_desc(restaurant_desc);
		actData.setCity(city);
		// actData.setVillage(village);
		// actData.setLocation_value(location_value);
		actData.setCover(cover);
		actData.setImgDatas(imgDatas);
		actData.setGps(gps);
		setViewpage();
	}

	protected void setViewpage() {
		if (adapter == null) {
			return;
		}
		adapter.notifyDataSetChanged();
		you_vPager.setCurrentItem(index);
		you_img_index.setText((index + 1) + "/" + actData.getImgDatas().size());
		// 绑定动作监听器：如翻页的动画
		you_vPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				you_img_index.setText((arg0 + 1) + "/"
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
	}

	protected void initTxt() {
		// TODO Auto-generated method stub
		detail_title.setText(actData.getXx_name());
		title.setText(actData.getXx_name());
		detail_addr.setText(actData.getXx_address());
		double la = Double.valueOf(preferences.getString("geoLat", null))
				.doubleValue();
		double ln = Double.valueOf(preferences.getString("geoLng", null))
				.doubleValue();
		distance.setText(DistanceCalculator.GetDistance(la, ln,
				actData.getGps()[0], actData.getGps()[1])
				+ "km"); // 距离我的位置??km
		desc.setText(actData.getXx_desc()); // 景点介绍
		seeAll.setOnClickListener(new OnClickListener() {
			Boolean flagg = true;
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (flagg) {
					flagg = false;
					desc.setEllipsize(null); // 展开
					desc.setSingleLine(flagg);
					view_all.setText("收起");
				} else {
					flagg = true;
					desc.setEllipsize(TextUtils.TruncateAt.END); // 收缩
					desc.setLines(6);
					view_all.setText("查看全部");
				}
			}
		});
		//第二张图
		Bitmap bitmap = mImageDownLoader.downloadImage(actData
				.getImgDatas().get(1).getUrl(),
				new onImageLoaderListener() {
					@Override
					public void onImageLoader(Bitmap bitmap, String url) {
						if (bitmap != null) {
							BitmapDrawable bd = new BitmapDrawable(
									getResources(), bitmap);
							second.setBackground(bd);
						}
					}
				});

		BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
		second.setBackground(bd);
		/**
		 * 电话
		 * */
		final String phone1=actData.getXx_telephone();
		if(phone1.equals("")||phone1==null){
			show_tel.setVisibility(View.GONE);
			img_tel.setVisibility(View.GONE);
		}else{
			show_tel.setVisibility(View.VISIBLE);
			img_tel.setVisibility(View.VISIBLE);
			telephone.setText(phone1);
			img_tel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent phoneIntent = new Intent("android.intent.action.CALL",
							Uri.parse("tel:" + phone1));
					startActivity(phoneIntent);
				}
			});
		}
	}

	/**
	 * 获取标签和id
	 * */
	private void getStampAndID() {
		Intent intent = getIntent();
		stamp = intent.getStringExtra("stamp");
		_id = intent.getStringExtra("_id"); // 这里的_id其实是en_id
		en_type = intent.getIntExtra("en_type", 0);
		index = intent.getIntExtra("index", 0);
	}

	/**
	 * 获取经纬度
	 * */
	private void getGeo() {
		geoLat = actData.getGps()[0];
		geoLng = actData.getGps()[1];
		initMap(); // 静态地图
		geoGetCount(); // 附近
	}

	/**
	 * 按经纬度获取附件吃、住、玩的数量
	 * */
	private void geoGetCount() {
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
							chi_txt.setText("吃+" + ss.getCount());
						} else if (indexf == 1) {
							you_txt.setText("游+" + ss.getCount());
						} else if (indexf == 2) {
							zhu_txt.setText("住+" + ss.getCount());
						}
						// /**异步去获取图片*/
						threadGetGeoImg(ss.getCover(), indexf);
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
	}

	public void threadGetGeoImg(String url, int index) {
		final int xx = index;
		final ImageView[] res = { chi_img, you_img, zhu_img };
		Bitmap bitmap = mImageDownLoader.downloadImage(url,
				new onImageLoaderListener() {
					@Override
					public void onImageLoader(Bitmap bitmap, String url) {
						if (res[xx] != null && bitmap != null) {
//							BitmapDrawable bd = new BitmapDrawable(
//									getResources(), bitmap);
//							res[xx].setBackground(bd);
							res[xx].setImageBitmap(bitmap);
						}
					}
				});
		if (bitmap != null) {
//			BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
//			res[index].setBackground(bd);
			res[xx].setImageBitmap(bitmap);
		} else {
//			res[index].setBackground(getImgResource(R.drawable.empty_img));
			res[xx].setImageDrawable(getImgResource(R.drawable.empty_img));
		}
	}

	/**
	 * 生成地图
	 * */
	private void initMap() {
		// TODO Auto-generated method stub
		String gps = geoLng + "," + geoLat;
		String url = "http://restapi.amap.com/v3/staticmap?scale=2&location="
				+ gps + "&zoom=17&labels=" + actData.getXx_name()
				+ ",2,0,30,0xFFFFFF,0x008000:" + gps
				+ "&size=440*280&markers=large,0xffa500,B:" + gps
				+ "&key=ee95e52bf08006f63fd29bcfbcf21df0";
		Bitmap bitmap = mImageDownLoader.downloadImage(url,
				new onImageLoaderListener() {
					@Override
					public void onImageLoader(Bitmap bitmap, String url) {
						if (bitmap != null) {
							BitmapDrawable bd = new BitmapDrawable(
									getResources(), bitmap);
							map_static.setBackground(bd);
						} else {
							Log.d("图片没有缓存", "生成地图时");
						}
					}
				});
		if (bitmap != null) {
			BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
			map_static.setBackground(bd);
		} else {
			map_static.setBackground(getImgResource(R.drawable.empty_img));
		}
	}

	/**
	 * 动态生成评论布局
	 * */
	private void initComment() {
		comment_sum.setText(sumOfComment + "条评价");
		/** 设置适配器时添加列表数据 */
//		list_comment.setAdapter(new MyListComment());
//		MesureHightUtils.setListViewHeightBasedOnChildren(list_comment);
//		/** 设置listView包裹的外部布局高度 */
//		int dd = MesureHightUtils
//				.setListViewHeightBasedOnChildren1(list_comment);
//		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//				LayoutParams.MATCH_PARENT, dd);// 设置最后一项分隔线消失
//		hehe.setLayoutParams(lp);
//		/** 解决listview与scrollview滑动冲突 */
//		list_comment.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				mysv.requestDisallowInterceptTouchEvent(false);// 触摸事件交给父控件
//				return false;
//			}
//		});
	}

	/**
	 * 评论列表适配器
	 * */
	private class MyListComment extends BaseAdapter {
		@Override
		public int getCount() { 
			// TODO Auto-generated method stub
			return sumOfComment > 3 ? 3 : sumOfComment;
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
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			/**
			 * 动态填充评论数据 ( 头像、评论、上传的图片 )
			 * */
			final int aa = arg0;
			if (arg1 == null) {
				arg1 = LayoutInflater.from(context).inflate(
						R.layout.item_youdetail_listitem, null);
			}
			ImageView face = (ImageView) arg1.findViewById(R.id.face);
			TextView level = (TextView) arg1.findViewById(R.id.level);
			TextView date = (TextView) arg1.findViewById(R.id.date);
			TextView comment = (TextView) arg1.findViewById(R.id.comment);
			LinearLayout upload_img = (LinearLayout) arg1.findViewById(R.id.upload_img);
			face.setImageDrawable(getImgResource(resImgFace[arg0]));
			level.setText(resLevel[arg0]);
			date.setText("2015-07-20");
			comment.setText(resComment[arg0]);
			/** 填充上传的图片 */
			int ss = sumOfComment > 3 ? 3 : sumOfComment;
			for (int i = 0; i < ss; i++) {
				// 实例化一个线性布局的参数
				LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				ImageView img = new ImageView(context);
//				img.setImageDrawable(getImgResource(resUploadImg[i]));
				img.setLayoutParams(lp1);
				img.setPadding(0, 0, 15, 0);
				int dd = upload_img.getChildCount();
				if (dd < 3) {
					upload_img.addView(img);
				}
			}
			arg1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					msg("item" + aa);
				}
			});
			arg1.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					// TODO Auto-generated method stub
					return false;
				}
			});
			return arg1;
		}
	}

	private void setOnClickListener() {
		/** 回退 */
		top_back.setOnClickListener(new OnClickListener() {
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
		/** 收藏 */
		save_it.setOnClickListener(new OnClickListener() {
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
												save_it.setImageDrawable(getImgResource(R.drawable.lovef));
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
												save_it.setImageDrawable(getImgResource(R.drawable.love));
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
//		/** 预定房间 */
//		to_book.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				startActivity(new Intent(context, YouBookActivity.class));
//			}
//		});
//		/** 去评论 */
//		to_comment.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//				startActivity(new Intent(context, YouCommentActivity.class));
//			}
//		});
		/** 分享 */
		to_share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				/** 去分享 */
//				showShare();
			}
		});
		round_you.setOnClickListener(new OnClickListener() {
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
		round_zhu.setOnClickListener(new OnClickListener() {
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
		round_chi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
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
		map_static.setOnClickListener(new OnClickListener() {
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
	}

	private void initVPager() {
		actData = null;
		adapter = new MyAdapter();
		you_vPager.setAdapter(adapter);
		you_img_index.setText("0/0");
	}

	/**
	 * viewpager适配器 内部类
	 */
	private class MyAdapter extends PagerAdapter {

		private LayoutInflater li = LayoutInflater.from(context);
		private View p;
		private ImageView image;
		private int mChildCount = 0;

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
			if (actData == null) {
				return 0;
			} else {
				return actData.getImgDatas().size();
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
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
			p = li.inflate(R.layout.item_indexfrag_pages, null);
			image = (ImageView) p.findViewById(R.id.image);
			image.setBackground(getImgResource(R.drawable.empty_img));

			Bitmap bitmap = mImageDownLoader.downloadImage(actData
					.getImgDatas().get(position).getUrl(),
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

			BitmapDrawable bd = new BitmapDrawable(getResources(), bitmap);
			image.setBackground(bd);
			container.removeView(p);
			container.addView(p);

			return p;
		}
	}

	private void findById() {
		// TODO Auto-generated method stub
		you_vPager = (ViewPager) findViewById(R.id.you_vPager);
		price = (TextView) findViewById(R.id.price);
		you_txt = (TextView) findViewById(R.id.you_txt);
		zhu_txt = (TextView) findViewById(R.id.zhu_txt);
		chi_txt = (TextView) findViewById(R.id.chi_txt);
		detail_addr = (TextView) findViewById(R.id.detail_addr);
		title = (TextView) findViewById(R.id.title);
		distance = (TextView) findViewById(R.id.distance);
		you_img_index = (TextView) findViewById(R.id.you_img_index);
		desc = (TextView) findViewById(R.id.desc);
		telephone = (TextView) findViewById(R.id.telephone);
		save_it = (ImageView) findViewById(R.id.save_it);
		mysv = (VerticalScrollView) findViewById(R.id.mysv);
//		to_book = (ImageView) findViewById(R.id.to_book);
//		to_comment = (ImageView) findViewById(R.id.to_comment);
		map_static = (ImageView) findViewById(R.id.map_static);
		you_img = (ImageView) findViewById(R.id.you_img);
		zhu_img = (ImageView) findViewById(R.id.zhu_img);
		chi_img = (ImageView) findViewById(R.id.chi_img);
		second = (ImageView) findViewById(R.id.second);
		img_tel = (ImageView) findViewById(R.id.img_tel);
		view_all = (TextView) findViewById(R.id.view_all);
		round_you = (FrameLayout) findViewById(R.id.round_you);
		round_zhu = (FrameLayout) findViewById(R.id.round_zhu);
		round_chi = (FrameLayout) findViewById(R.id.round_chi);
		seeAll = (RelativeLayout) findViewById(R.id.seeAll);
		show_tel = (LinearLayout) findViewById(R.id.show_tel);
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
		} else {
			msg("需要登陆后才能做此操作");
		}
	}

}
