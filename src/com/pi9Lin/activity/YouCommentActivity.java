package com.pi9Lin.activity;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.gallary.AlbumActivity;
import com.pi9Lin.gallary.Bimp;
import com.pi9Lin.gallary.FileUtils;
import com.pi9Lin.gallary.GalleryActivity;
import com.pi9Lin.gallary.ImageItem;
import com.pi9Lin.gallary.PublicWay;
import com.pi9Lin.gallary.Res;

public class YouCommentActivity extends BaseActivity {

	private MyListView mlist;
	private LayoutInflater li;
	private RelativeLayout backward;
	private RelativeLayout share_btn;
	private TextView top_title;
	private MyBaseApr myBaseApr;
	private float rating;
	private String pingjia;
	private File tempFile;
	public ImageView up_img;
	public static Bitmap bimap ;
	private GridAdapter adapter;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_comment);
		li = LayoutInflater.from(context);
		Res.init(YouCommentActivity.this);
		bimap = BitmapFactory.decodeResource(
				getResources(),
				R.drawable.icon_addpic_unfocused);
		PublicWay.activityList.add(YouCommentActivity.this);
		initActionBar();
		findById(); // 资源初始化
		setOnClickListener();
		init();
		initList(); // 填充页面
	}

	private class MyBaseApr extends BaseAdapter {
		/**
		 * ListView包含不同Item的布局 　　我们需要做这些工作: 　　1）重写 getViewTypeCount() –
		 * 该方法返回多少个不同的布局 　　2）重写 getItemViewType(int) – 根据position返回相应的Item 　
		 * 3）根据view item的类型，在getView中创建正确的convertView
		 * */
		final int TYPE_1 = 0;
		final int TYPE_2 = 1;
		final int TYPE_3 = 2;
		final int TYPE_4 = 3;

		public MyBaseApr() {}

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
			return getViewTypeCount();
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

		private class Holder {
			RatingBar ratingBar;
			EditText editText;
			RelativeLayout upload_img;
			ImageView take_photo;
			ImageView xlwb;
			ImageView wx;
			ImageView qq;
			GridView noScrollgridview;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v = null;
			final Holder h;
			int type = getItemViewType(arg0);
			if (arg1 == null) {
				h = new Holder();
				if (type == TYPE_1) {
					v = li.inflate(R.layout.activity_comment_type1, null);
					h.ratingBar = (RatingBar) v.findViewById(R.id.RatingBar);
				} else if (type == TYPE_2) {
					v = li.inflate(R.layout.activity_comment_type2, null);
					h.editText = (EditText) v.findViewById(R.id.editText);
				} else if (type == TYPE_3) {
					v = li.inflate(R.layout.activity_comment_type3, null);
					h.noScrollgridview=(GridView)v.findViewById(R.id.noScrollgridview);
				} else if (type == TYPE_4) {
					v = li.inflate(R.layout.activity_comment_type4, null);
					h.xlwb = (ImageView) v.findViewById(R.id.xlwb);
					h.wx = (ImageView) v.findViewById(R.id.wx);
					h.qq = (ImageView) v.findViewById(R.id.qq);
				}
				v.setTag(h);
			} else {
				v = arg1;
				h = (Holder) v.getTag();
			}
			if (type == TYPE_1) {
				rating = h.ratingBar.getRating();
			} else if (type == TYPE_2) {
				pingjia = h.editText.getText().toString();
			} else if (type == TYPE_3) {
//				up_img = h.take_photo;
//				h.upload_img.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						// TODO Auto-generated method stub
//						uploadFace(YouCommentActivity.this);
//					}
//				});
				h.noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
				adapter = new GridAdapter(YouCommentActivity.this);
				adapter.update();
				h.noScrollgridview.setAdapter(adapter);
				h.noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
						if (arg2 == Bimp.tempSelectBitmap.size()) {
							//当点的是加号
							uploadFace(YouCommentActivity.this);
						} else {
							//当点的是图片
							Intent intent = new Intent(YouCommentActivity.this,
									GalleryActivity.class);
							intent.putExtra("position", "1");
							intent.putExtra("ID", arg2);
							startActivity(intent);
						}
					}
				});
			} 
//			else {
//				h.xlwb.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						// TODO Auto-generated method stub
//						msg("新浪分享");
//					}
//				});
//				h.wx.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						// TODO Auto-generated method stub
//						msg("微信分享");
//					}
//				});
//				h.qq.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						// TODO Auto-generated method stub
//						msg("QQ分享");
//					}
//				});
//			}
			return v;
		}
	}

	public void uploadFace(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.upload_face,
				null);
		Button from_local = (Button) view.findViewById(R.id.from_local);
		Button from_photo = (Button) view.findViewById(R.id.from_photo);
		Button cancle = (Button) view.findViewById(R.id.cancle);
		Dialog dialog = new Dialog(context, R.style.transparentFrameWindowStyle);
		dialog.setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		Window window = dialog.getWindow();
		// 设置显示动画
		window.setWindowAnimations(R.style.main_menu_animstyle);
		WindowManager.LayoutParams wl = window.getAttributes();
		// 获取设备屏幕大小的方法
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		wl.x = 0;
		wl.y = dm.heightPixels;
		// 以下这两句是为了保证按钮可以水平满屏
		wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
		wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
		// 设置显示位置
		dialog.onWindowAttributesChanged(wl);
		// 设置点击外围解散
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		final Dialog dd = dialog;
		/** 取消 */
		cancle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (dd != null) {
					dd.dismiss();
				}
			}
		});
		/** 图库 */
		from_local.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
//				Intent intent = new Intent(Intent.ACTION_PICK);
//				intent.setType("image/*");
//				Intent intent = new Intent(YouCommentActivity.this,Gallary.class);
//				startActivityForResult(intent, 4);
				if (dd != null) {
					dd.dismiss();
				}
				Intent intent = new Intent(YouCommentActivity.this,
						AlbumActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
		/** 拍照 */
		from_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
//				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//				if (isWriteSD()) {
//					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
//							.fromFile(new File(Environment
//									.getExternalStorageDirectory(),
//									PHOTO_FILE_NAME)));
//				}
//				startActivityForResult(intent, 6);
				photo();
				if (dd != null) {
					dd.dismiss();
				}
			}
		});
	}
	
	private static final int TAKE_PICTURE = 0x000001;

	public void photo() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(openCameraIntent, TAKE_PICTURE);
	}
	
	/** 剪裁 */
	private void crop(Uri uri) {
		// 裁剪图片意图
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		// 裁剪框的比例，1：1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// 裁剪后输出图片的尺寸大小
		intent.putExtra("outputX", 250);
		intent.putExtra("outputY", 250);
		// 图片格式
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);// 取消人脸识别
		intent.putExtra("return-data", true);// true:返回uri，false：不返回uri
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(intent, 5);
	}

	private void initList() {
		// TODO Auto-generated method stub
		myBaseApr = new MyBaseApr();
		mlist.setAdapter(myBaseApr);
	}

	private void init() {
		Intent intent = getIntent();
		String title = intent.getStringExtra("top_title");
		top_title.setText(title);
	}

	private void setOnClickListener() {
		/**
		 * 搜索
		 * */
		share_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				/** 去发表 */
				msg("去发表");
			}
		});
		/**
		 * 返回
		 * */
		backward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.slide_right_out,
						R.anim.slide_right_in);
			}
		});
	}

	private void findById() {
		// TODO Auto-generated method stub
		mlist = (MyListView) findViewById(R.id.lv);
	}
	@SuppressLint("HandlerLeak")
	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater;
		private int selectedPosition = -1;
		private boolean shape;

		public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public GridAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void update() {
			loading();
		}

		public int getCount() {
			if(Bimp.tempSelectBitmap.size() == 9){
				return 9;
			}
			return (Bimp.tempSelectBitmap.size() + 1);
		}

		public Object getItem(int arg0) {
			return null;
		}

		public long getItemId(int arg0) {
			return 0;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_published_grida,
						parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position ==Bimp.tempSelectBitmap.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.icon_addpic_unfocused));
				if (position == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(Bimp.tempSelectBitmap.get(position).getBitmap());
			}

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		public void loading() {
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (Bimp.max == Bimp.tempSelectBitmap.size()) {
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else {
							Bimp.max += 1;
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
						}
					}
				}
			}).start();
		}
	}
	
	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.layout_youcomment_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		top_title = (TextView) v.findViewById(R.id.top_title);
		backward = (RelativeLayout) v.findViewById(R.id.backward);
		share_btn = (RelativeLayout) v.findViewById(R.id.share_btn);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		/** 图库 */
		if (requestCode == 4) {
			if (data != null) {
//				Uri uri = data.getData();
//				crop(uri);
				String data1=data.getStringExtra("data0");
				up_img.setImageBitmap(decodeSampledBitmapFromResource(data1,400));
			}
		}
		/** 拍照 */
		if (requestCode == 6) {
			if (isWriteSD()) {
				tempFile = new File(Environment.getExternalStorageDirectory(),
						PHOTO_FILE_NAME);
				crop(Uri.fromFile(tempFile));
			} else {
				msg("未找到存储卡，无法存储照片");
			}
		}
		/** 剪裁 */
		if (requestCode == 5) {
			Bitmap bitmap = data.getParcelableExtra("data");
			up_img.setImageBitmap(bitmap);
		}
		if (requestCode == TAKE_PICTURE) {
			if (Bimp.tempSelectBitmap.size() < 9 && resultCode == RESULT_OK) {
				
				String fileName = String.valueOf(System.currentTimeMillis());
				Bitmap bm = (Bitmap) data.getExtras().get("data");
				FileUtils.saveBitmap(bm, fileName);
				
				ImageItem takePhoto = new ImageItem();
				takePhoto.setBitmap(bm);
				Bimp.tempSelectBitmap.add(takePhoto);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			for(int i=0;i<PublicWay.activityList.size();i++){
				if (null != PublicWay.activityList.get(i)) {
					PublicWay.activityList.get(i).finish();
				}
			}
			System.exit(0);
		}
		return true;
	}
	
}
