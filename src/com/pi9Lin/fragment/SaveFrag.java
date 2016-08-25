package com.pi9Lin.fragment;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
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
import com.pi9Lin.activity.DetailActivity;
import com.pi9Lin.activity.IndexActivity;
import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.SaveData;
import com.pi9Lin.pulltorefresh.pullableview.PullToRefreshLayout;
import com.pi9Lin.pulltorefresh.pullableview.RefreshListener;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class SaveFrag extends BaseFragment {

	private View view;
	private MyListView list_save;
	private MyAprr apr;
	private List<SaveData> saveDatas = new ArrayList<SaveData>();
	private AlertDialog.Builder builder;
	private View footer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// 登录成功才会显示本碎片
		context = getActivity();
		view = inflater.inflate(R.layout.fragment_save, container, false);
		footer = LayoutInflater.from(context).inflate(R.layout.footer, null);
		initImageLoader(); // 初始化图片异步处理对象
		findById();
		setOnClickListener();
		init();
		return view;
	}

	private void setOnClickListener() {
		// TODO Auto-generated method stub
		// 直接定义了下拉刷新的监听
		((PullToRefreshLayout) view.findViewById(R.id.refresh_view))
				.setOnRefreshListener(new RefreshListener() {
					@Override
					public void onRefresh(
							final PullToRefreshLayout pullToRefreshLayout) {
						// TODO Auto-generated method stub
						init();
						new Handler() {
							@Override
							public void handleMessage(Message msg) {
								// 千万别忘了告诉控件刷新完毕了哦！
								pullToRefreshLayout
										.refreshFinish(PullToRefreshLayout.SUCCEED);
							}
						}.sendEmptyMessageDelayed(0, 1000);
						super.onRefresh(pullToRefreshLayout);
					}
				});
	}

	/**
	 * listview适配器
	 * */
	private class MyAprr extends BaseAdapter implements OnScrollListener {

		LayoutInflater inflater = LayoutInflater.from(context);

		public MyAprr() {
			// TODO Auto-generated constructor stub
			list_save.setOnScrollListener(this);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return saveDatas.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return saveDatas.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v = null;
			ViewHolder viewHolder;
			if (arg1 == null) {
				v = inflater
						.inflate(R.layout.item_save_pagelist_listitem, null);
				viewHolder = new ViewHolder();
				viewHolder.textView = (TextView) v.findViewById(R.id.lll);
				viewHolder.item_tiao = (RelativeLayout) v
						.findViewById(R.id.item_tiao);
				viewHolder.imageView = (ImageView) v.findViewById(R.id.kkk);
				v.setTag(viewHolder);// 将viewholder存储在view中
			} else {
				v = arg1;
				viewHolder = (ViewHolder) v.getTag();
			}

			viewHolder.textView.setText(saveDatas.get(arg0).getName());
			final int index = arg0;
			viewHolder.item_tiao.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// 这里不能用get_id的值
					String _id = saveDatas.get(index).getEntity_id();
					int en_type = saveDatas.get(index).getEntity_type();
					double[] gps = saveDatas.get(index).getGps();
					String stamp = null;
					if (en_type == 1) {
						stamp = "hotel";
					} else if (en_type == 2) {
						stamp = "restaurant";
					} else if (en_type == 3) {
						stamp = "sights";
					}
					Intent intent = new Intent(context, DetailActivity.class);
					intent.putExtra("_id", _id);
					intent.putExtra("en_type", en_type);
					intent.putExtra("stamp", stamp);
					intent.putExtra("latitude", gps[0]);
					intent.putExtra("longitude", gps[1]);
					startActivityForResult(intent, 11);
					Activity context = getActivity();
					context.overridePendingTransition(R.anim.slide_left_in,
							R.anim.slide_left_out);
				}
			});
			viewHolder.item_tiao
					.setOnLongClickListener(new OnLongClickListener() {
						@Override
						public boolean onLongClick(View arg0) {
							// TODO Auto-generated method stub
							// 通过AlertDialog.Builder这个类来实例化我们的一个AlertDialog的对象
							builder = new AlertDialog.Builder(context);
							// 设置Title的图标
							builder.setIcon(R.drawable.app_icon);
							// 设置Title的内容
							builder.setTitle("取消收藏");
							// 设置Content来显示一个信息
							builder.setMessage("确定删除吗？");
							// 设置一个PositiveButton
							builder.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String url = "http://www.xiangyouji.com.cn:3000/my/removeCollection";
											AsyncHttpClient client = new AsyncHttpClient();
											PersistentCookieStore myCookieStore = new PersistentCookieStore(
													context);
											client.setCookieStore(myCookieStore);
											RequestParams params = new RequestParams();
											params.put("entity_type", saveDatas
													.get(index)
													.getEntity_type()
													+ "");
											params.put("entity_id", saveDatas
													.get(index).getEntity_id());
											client.post(
													url,
													params,
													new AsyncHttpResponseHandler() {
														@Override
														public void onSuccess(
																int arg0,
																String result) {
															try {
																if (checkJson(result)) {
																	msg("取消成功");
																	/** 修改本地收藏缓存 */
																	IndexActivity activity = (IndexActivity) getActivity();
																	activity.setTabSelection(2);
																} else {
																	msg("取消失败--数据问题");
																}
															} catch (Exception e) {
																// TODO
																// Auto-generated
																// catch
																e.printStackTrace();
															}
														}

														@Override
														public void onFailure(
																Throwable arg0,
																String arg1) {
															Toast.makeText(
																	context,
																	"取消失败--网络问题",
																	Toast.LENGTH_SHORT)
																	.show();
															super.onFailure(
																	arg0, arg1);
														}
													});
										}
									});
							builder.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											builder = null;
										}
									});
							// 显示出该对话框
							builder.show();
							return true;
						}
					});
			final ImageView mImageView = viewHolder.imageView;
			// 解决listview刷新时数据不稳定显示
			String rr = saveDatas.get(index).getCover();
			imageLoader.displayImage(rr, mImageView, options, animateFirstListener);
			return v;
		}

		class ViewHolder {
			TextView textView;
			RelativeLayout item_tiao;
			ImageView imageView;
		}

		// 每次加载5条数据
		private int pageSize = 5;
		int totalCount = 4; // 从第5条开始加载
		private boolean finish = true;// 是否加载完成
		private boolean scrolled = false;// 是否触摸屏幕

		@Override
		public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
			// 当翻到最后一条数据时
			if (scrolled
					&& list_save.getChildAt(list_save.getLastVisiblePosition()
							- list_save.getFirstVisiblePosition()) != null
					&& list_save.getChildAt(
							list_save.getLastVisiblePosition()
									- list_save.getFirstVisiblePosition())
							.getBottom() <= list_save.getMeasuredHeight()
					&& finish) {
				
				list_save.addFooterView(footer);
				// 已经移动到了listview的最后
				finish = false;
				String url = "http://www.xiangyouji.com.cn:3000/my/getCollectionMix/startIndex/"
						+ totalCount + "/length/" + pageSize;
				AsyncHttpClient client = new AsyncHttpClient();
				PersistentCookieStore myCookieStore = new PersistentCookieStore(
						context);
				client.setCookieStore(myCookieStore);
				client.get(url, new AsyncHttpResponseHandler() {
					public void onSuccess(int arg0, String arg1) {
						try {
							if (pageJson(arg1)) {
								// 获取数据后再填到listview中
								// 1.解析
								saveDatas.addAll(saveJsonToString(arg1));
								apr.notifyDataSetChanged();
								finish = true;
							}
						} catch (Exception e) {
							System.out.println("错误:" + e.getMessage());
						}
						list_save.removeFooterView(footer);
					};

					public void onFailure(Throwable arg0, String arg1) {
						msg("网络错误");
						list_save.removeFooterView(footer);
					};
				});
				totalCount += pageSize;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView arg0, int arg1) {
			// TODO Auto-generated method stub
			if (arg1 == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				scrolled = true;
			}
		}

	}

	/**
	 * 首先获得住的收藏数据 entity_type=1
	 * */
	private void init() {
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);

		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/getCollectionMix/startIndex/0/length/4";
		client.get(RegistPath, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, String arg1) {
				try {
					if (checkJson(arg1)) {
						// 获取数据后再填到listview中
						// 1.解析
						saveDatas = saveJsonToString(arg1);
						if (saveDatas.size() > 0) {
							// 填充listview数据
							// 添加页脚
							list_save.addFooterView(footer);
							apr = new MyAprr(); // 整个页面由一个总的数据封装类actDatas填充列表
							list_save.setAdapter(apr);
							list_save.removeFooterView(footer);
						}
					}
				} catch (Exception e) {
					System.out.println("错误:" + e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				msg("获取分页收藏失败");
				super.onFailure(arg0, arg1);
			}

		});
	}

	private boolean pageJson(String arg1) throws Exception {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject(arg1);
		int status = jsonObject.getInt("status");
		if (status == 1) {
			if (jsonObject.getJSONArray("data").length() == 0) {
				return false;
			}
			return true;
		}
		return false;
	}

	protected List<SaveData> saveJsonToString(String arg1) throws Exception {
		List<SaveData> datas = new ArrayList<SaveData>();
		JSONObject object = new JSONObject(arg1);
		JSONArray array = object.getJSONArray("data");
		for (int i = 0; i < array.length(); i++) {
			SaveData data = new SaveData();
			JSONObject object2 = (JSONObject) array.opt(i);
			String _id = object2.getString("_id");
			String userId = object2.getString("userId");
			String entity_id = object2.getString("entity_id");
			String insert_date = object2.getString("insert_date");
			String name = object2.getString("name");
			String address = object2.getString("address");
			String cover = object2.getString("cover");
			String phone = object2.getString("phone");
			int entity_type = object2.getInt("entity_type");
			int province = object2.getInt("province");
			int area_id = object2.getInt("area_id");
			JSONObject gps = object2.getJSONObject("gps");
			double latitude = gps.getDouble("latitude");
			double longitude = gps.getDouble("longitude");
			double[] aa = { latitude, longitude };
			data.set_id(_id);
			data.setUserId(userId);
			data.setEntity_id(entity_id);
			data.setInsert_date(insert_date);
			data.setName(name);
			data.setAddress(address);
			data.setCover(cover);
			data.setPhone(phone);
			data.setEntity_type(entity_type);
			data.setProvince(province);
			data.setArea_id(area_id);
			data.setGps(aa);
			datas.add(data);
		}
		return datas;
	}

	private void findById() {
		// TODO Auto-generated method stub
		list_save = (MyListView) view.findViewById(R.id.list_save);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 11) {
			IndexActivity activity = (IndexActivity) getActivity();
			activity.setTabSelection(2);
		}
	}

}
