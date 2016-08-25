package com.pi9Lin.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.activity.DetailActivity;
import com.pi9Lin.activity.ShowCommentActivity;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Entity;
import com.pi9Lin.data.RoundData;
import com.pi9Lin.data.SleepImgData;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.utils.DistanceCalculator;

@SuppressLint({ "NewApi", "ResourceAsColor" })
public class SearchActivity extends BaseActivity {

	EditText keywd;
	RelativeLayout sousuo, layout_back;
	TextView ssls;
	ListView lishi_sousuo;
	List<RoundData> search_result = new ArrayList<RoundData>();
	ImageView txt_delete;
	protected Dialog dialog;
	private MyAprr apr;
	private View footer;
	private boolean finish = true;
	protected String key;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		ShareSDK.initSDK(this);
		initImageLoader(); // 初始化图片异步处理对象
		getAllSave();
		findById();
		initActionbar();
		setOnClickListener();
	}

	/**
	 * listview适配器
	 * */
	private class MyAprr extends BaseAdapter implements OnScrollListener {

		LayoutInflater inflater = LayoutInflater.from(context);
		double la;
		double ln;

		public MyAprr() {
			// TODO Auto-generated constructor stub
			String lat = preferences.getString("geoLat", null);
			String lng = preferences.getString("geoLng", null);
			la = Double.valueOf(lat).doubleValue();
			ln = Double.valueOf(lng).doubleValue();
			lishi_sousuo.setOnScrollListener(this);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return search_result.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return search_result.get(arg0);
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
				/** 点赞图片 */
				h.sleep_love_img = (ImageView) v
						.findViewById(R.id.sleep_love_img);
				/** 点赞数 */
				h.collection_count = (TextView) v
						.findViewById(R.id.collection_count);
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

			h.sleep_location.setText(search_result.get(arg0).getName());
			h.to_comment.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// mark
					startActivity(new Intent(context, ShowCommentActivity.class));
				}
			});
			h.distance.setText(DistanceCalculator.GetDistance(la, ln, search_result
					.get(arg0).getGps()[0], search_result.get(arg0).getGps()[1])
					+ "km");
			h.to_share.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
//					showShare();
				}
			});
			/**
			 * 先判断是否已经被收藏
			 * */
			final String en_id = search_result.get(arg0).getEntity_id();
			final int en_typef = search_result.get(arg0).getEntity_type();
			final int fff = arg0;
			search_result.get(arg0).setSleep_love_img(h.sleep_love_img);
			search_result.get(arg0).setCollection(h.collection_count);
			h.collection_count.setText(search_result.get(arg0).getCollection_count() + "+");
			if (isSaved(en_id, en_typef)) {
				// 收藏了
				h.sleep_love_img.setImageDrawable(getImgResource(R.drawable.love));
				h.collection_count.setTextColor(R.color.hongse);
				search_result.get(arg0).setSaved(true);
			} else {
				// 未收藏
				h.sleep_love_img.setImageDrawable(getImgResource(R.drawable.lovef));
				search_result.get(arg0).setSaved(false);
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
						Intent intent = new Intent(context,DengLuActivity.class);
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
													int sum = search_result
															.get(fff)
															.getCollection_count();
													sum--;
													h.collection_count
															.setText(sum + "+");
													h.collection_count
															.setTextColor(R.color.huise);
													search_result
															.get(fff)
															.setCollection_count(
																	sum);
													search_result.get(fff)
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
													int sum = search_result
															.get(fff)
															.getCollection_count();
													sum++;
													h.collection_count
															.setText(sum + "+");
													h.collection_count
															.setTextColor(R.color.hongse);
													search_result
															.get(fff)
															.setCollection_count(
																	sum);
													search_result.get(fff)
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
			final int length = search_result.get(arg0).getImgDatas().size();
			final int index = arg0;
			List<View> list = new ArrayList<View>();
			for (int i = 0; i < length; i++) {
				View p = inflater.inflate(R.layout.item_indexfrag_pages, null);
				list.add(p);
				final ImageView image = (ImageView) p.findViewById(R.id.image);
				image.setBackground(getImgResource(R.drawable.empty_img));
				image.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						int en_type = search_result.get(index).getEntity_type();
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
						intent.putExtra("_id", search_result.get(index)
								.getEntity_id());
						intent.putExtra("stamp", stamp);
						intent.putExtra("en_type", search_result.get(index)
								.getEntity_type());
						intent.putExtra("index", search_result.get(index)
								.getPage_index());
						intent.putExtra("latitude", search_result.get(index)
								.getGps()[0]);
						intent.putExtra("longitude", search_result.get(index)
								.getGps()[1]);
						startActivityForResult(intent, 9);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
				
				String rr = search_result.get(index).getImgDatas().get(i).getUrl();
				image.setTag(rr);
				// 读取图片缓存 因为读取的可能是文件缓存 所以要在异步里面执行 但是会影响程序
				imageLoader.displayImage(rr, image, options, animateFirstListener);
			}
			MyAdapter adapterf = new MyAdapter(search_result.get(arg0), list);
			h.pager.setAdapter(adapterf);
			h.pager.setCurrentItem(search_result.get(arg0).getPage_index());
			if (length > 0) {
				h.img_index
						.setText((search_result.get(arg0).getPage_index() + 1)
								+ "/" + length);
			} else if (length == 0) {
				h.img_index.setText("0/0");
			}
			final int dd = arg0;
			// 绑定动作监听器：如翻页的动画 
			h.pager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int arg0) {
					// TODO Auto-generated method stub
					h.img_index.setText((arg0 + 1) + "/" + length);
					search_result.get(dd).setPage_index(arg0);
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
		private int totalCount = 10; // 从第11条开始加载
		private boolean scrolled = false;// 是否加载完成

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int arg3) {
			// TODO Auto-generated method stub
			// 当翻到最后一条数据时
			if (scrolled
					&& lishi_sousuo.getCount() == 0
					&& finish
					|| scrolled
					&& lishi_sousuo.getChildAt(lishi_sousuo
							.getLastVisiblePosition()
							- lishi_sousuo.getFirstVisiblePosition()) != null
					&& lishi_sousuo.getChildAt(
							lishi_sousuo.getLastVisiblePosition()
									- lishi_sousuo.getFirstVisiblePosition())
							.getBottom() <= lishi_sousuo.getMeasuredHeight()
					&& finish) {

				// 已经移动到了listview的最后
				finish = false;
				// 添加页脚
				lishi_sousuo.addFooterView(footer);
				String url = "http://www.xiangyouji.com.cn:3000/search/keyword/"
						+ key
						+ "/startIndex/"
						+ totalCount
						+ "/length/"
						+ pageSize;
				MyTask myTask = new MyTask();
				myTask.execute(url);
				totalCount += pageSize;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				scrolled = true;
			}
		}
	}
	
	/**
	 * viewpager适配器 内部类
	 */
	private class MyAdapter extends PagerAdapter {

		private RoundData data;
		private View p;
		private ImageView image;
		private List<View> list;

		public MyAdapter(RoundData data, List<View> list) {
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
			image = (ImageView) p.findViewById(R.id.image);
			if (position > 0) {
				imageLoader.displayImage(data.getImgDatas().get(position).getUrl(), image, options, animateFirstListener);
			} 
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

	class Holder {
		TextView sleep_location;
		TextView collection_count;
		TextView distance;
		RelativeLayout to_comment;
		RelativeLayout to_share;
		ImageView sleep_love_img;
		ViewPager pager;
		TextView img_index;
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

	private void setOnClickListener() {
		// TODO Auto-generated method stub
		txt_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				keywd.setText("");
			}
		});
		keywd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				if (arg0.length() == 0) {
					txt_delete.setVisibility(View.GONE);
//					lishi_sousuo.setAdapter(null);
				} else {
					txt_delete.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});
		sousuo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				key = keywd.getText().toString();
				if (!key.equals("")) {
					lishi_sousuo.setAdapter(null);
					ssls.setVisibility(View.GONE);
					dialog = dialog(SearchActivity.this, "请稍候...");
					dialog.setCancelable(true);
					dialog.show();
					String url = "http://www.xiangyouji.com.cn:3000/search/keyword/"
							+ key + "/startIndex/0/length/10";
					MyRun myRun = new MyRun();
					myRun.execute(url);
				} else {
					msg("输入为空");
				}
			}
		});
		layout_back.setOnClickListener(new OnClickListener() {
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
		ssls = (TextView) findViewById(R.id.ssls);
		lishi_sousuo = (ListView) findViewById(R.id.lishi_sousuo);
	}

	private void initActionbar() {
		// 自定义ActionBar
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.activity_location_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		layout_back = (RelativeLayout) v.findViewById(R.id.layout_back);
		sousuo = (RelativeLayout) v.findViewById(R.id.sousuo);
		keywd = (EditText) v.findViewById(R.id.keywd);
		txt_delete = (ImageView) v.findViewById(R.id.txt_delete);
	}

	class MyRun extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... arg0) {
			String outString = "";
			try {
				HttpGet get = new HttpGet(arg0[0]);
				HttpClient client = new DefaultHttpClient();
				HttpResponse re = client.execute(get);
				if (re.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity result = re.getEntity();
					InputStream inputStream = result.getContent();
					InputStreamReader inputStreamReader = new InputStreamReader(
							inputStream, "utf-8");
					BufferedReader bufferedReader = new BufferedReader(
							inputStreamReader);
					String lenString = "";
					while ((lenString = bufferedReader.readLine()) != null) {
						outString += lenString;
					}
					bufferedReader.close();
					inputStreamReader.close();
					inputStream.close();
				} else {
					msg("获取搜索结果失败");
				}
			} catch (Exception e) {
				System.out.println("异常:" + e.getMessage());
				dialog.dismiss();
			}
			return outString;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			// 输出结果
			try {
				/** 得到结果并解析成对象 */
				if (checkJson(result)) {
					search_result = searchJsonToObject(result);
					if (search_result.size() == 0) {
						msg("无搜索结果");
						dialog.dismiss();
						return;
					}
					footer = LayoutInflater.from(context).inflate(
							R.layout.footer, null);
					// 填充listview数据
					lishi_sousuo.addFooterView(footer);
					apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
					lishi_sousuo.setAdapter(apr);
					lishi_sousuo.removeFooterView(footer);
					// apr.notifyDataSetChanged();
					// 滚动监听事件
//					lishi_sousuo.setOnScrollListener(new MyOnScrollListener());
				}
			} catch (Exception e) {
				System.out.println("异常:" + e.getMessage());
			}
			dialog.dismiss();
			super.onPostExecute(result);
		}
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
						search_result.addAll(searchJsonToObject(result));// 已经得到一些数据
						// 让listview自动刷新
						apr.notifyDataSetChanged();
						finish = true;
					}
					// 去掉页脚
					lishi_sousuo.removeFooterView(footer);
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

	public List<RoundData> searchJsonToObject(String result) throws Exception {
		// TODO Auto-generated method stub
		JSONObject object = new JSONObject(result);
		JSONArray array = object.getJSONArray("data");
		List<RoundData> datas = new ArrayList<RoundData>();
		for (int i = 0; i < array.length(); i++) {
			RoundData data = new RoundData();
			JSONObject object2 = (JSONObject) array.opt(i);
			String _id = object2.getString("_id");
			String entity_id = object2.getString("entity_id");
			String address = object2.getString("address");
			int province = object2.getInt("province");
			int area_id = object2.getInt("area_id");
			int collection_count = object2.getInt("collection_count");
			int entity_type = object2.getInt("entity_type");
			String phone = object2.getString("phone");
			String name = object2.getString("name");
			String cover = object2.getString("cover");
			JSONObject gps = object2.getJSONObject("gps");
			double latitude = gps.getDouble("latitude");
			double longitude = gps.getDouble("longitude");
			double[] haha = { latitude, longitude };
			JSONArray array2 = object2.getJSONArray("images");
			List<SleepImgData> li = new ArrayList<SleepImgData>();
			for (int j = 0; j < array2.length(); j++) {
				JSONObject object3 = (JSONObject) array2.opt(j);
				String url = object3.getString("url");
				String update_time = object3.getString("update_time");
				String create_time = object3.getString("create_time");
				int order_no = object3.getInt("order");
				SleepImgData imgData = new SleepImgData();
				imgData.setCreate_time(create_time);
				imgData.setUrl(url);
				imgData.setUpdate_time(update_time);
				imgData.setOrder_no(order_no);
				li.add(imgData);
			}
			data.set_id(_id);
			data.setEntity_id(entity_id);
			data.setAddress(address);
			data.setPhone(phone);
			data.setProvince(province);
			data.setArea_id(area_id);
			data.setImgDatas(li);
			data.setName(name);
			data.setEntity_type(entity_type);
			data.setCover(cover);
			data.setGps(haha);
			data.setCollection_count(collection_count);
			data.setSleep_love_img(new ImageView(context));
			data.setCollection(new TextView(context));
			datas.add(data);
		}
		return datas;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 3) {
			if (resultCode == Activity.RESULT_OK) {
				// 去收藏
				msg("登录成功");
			} else {
				msg("需要登陆后才能做次操作");
			}
		}
		/**
		 * 从详细页回来 判断是否被收藏
		 * */
		if (requestCode == 9) {
		}
		getAllSave();
		for (int i = 0; i < search_result.size(); i++) {
			boolean l = false;
			for (int j = 0; j < allSave.size(); j++) {
				if (allSave.get(j).getEntity_id()
						.equals(search_result.get(i).getEntity_id())
						&& allSave.get(j).getEntity_type() == search_result
								.get(i).getEntity_type()) {
					l = true;
				}
			}
			if (l) {
				search_result.get(i).getSleep_love_img()
						.setImageDrawable(getImgResource(R.drawable.love));
				if (!search_result.get(i).isSaved()) {
					int num = search_result.get(i).getCollection_count() + 1;
					search_result.get(i).setCollection_count(num);
					search_result.get(i).getCollection().setText(num + "+");
				}
				// 重置
				search_result.get(i).setSaved(true);
			} else {
				search_result.get(i).getSleep_love_img()
						.setImageDrawable(getImgResource(R.drawable.lovef));
				if (search_result.get(i).isSaved()) {
					int num = search_result.get(i).getCollection_count() - 1;
					search_result.get(i).setCollection_count(num);
					search_result.get(i).getCollection().setText(num + "+");
				}
				// 重置
				search_result.get(i).setSaved(false);
			}
		}
	}
}
