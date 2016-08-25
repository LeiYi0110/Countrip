package com.pi9Lin.fragment;


import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.pi9Lin.activity.IndexActivity;
import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.countrip.R;
import com.pi9Lin.imageLoader.ImageDownLoader;
import com.pi9Lin.imageLoader.ImageDownLoader.onImageLoaderListener;
import com.pi9Lin.minefrag.MobileActivity;
import com.pi9Lin.minefrag.NickNameActivity;
import com.pi9Lin.minefrag.PasswordActivity;
import com.pi9Lin.utils.ACache;
import com.pi9Lin.utils.FaceUtils;

public class MineFrag extends BaseFragment {
	/**
	 * 我的Fragment
	 * 
	 * */
	private View view;
	ListView list_mine;
	ImageView mine_img;
	TextView mine_version;
	
	private File tempFile;
	
	private ImageDownLoader mImageDownLoader;
	private ACache mCache;
	/**
	 * 测试数据
	 * */
	String[] items={"昵称","电话","修改密码"};
	String[] items_value=new String[3];
	String version="v1.0版本";
	public TextView[] showTxt;
	Dialog dialog;
	RelativeLayout logout;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		/** 我的页开始 */
		context=getActivity();
		preferences=getConfig();
		mImageDownLoader = new ImageDownLoader(context);
		mCache = ACache.get(context);
		view = inflater.inflate(R.layout.fragment_mine, container, false);
		findById();
		setOnClickListener();
		init();
		initFace();
		initListView();
		return view;
	}
	
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		mine_img.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				uploadFace(context);
			}
		});
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				logout();
			}
		});
	}

	private void initFace() {
		// TODO Auto-generated method stub
		String url=preferences.getString("headerImage", "");
		if(!url.equals("")){
			Bitmap bitmap = mImageDownLoader.downloadImage(url, new onImageLoaderListener() {
				@Override
				public void onImageLoader(Bitmap bitmap, String url) {
					if (bitmap != null) {
						BitmapDrawable bd = new BitmapDrawable(getResources(),
								bitmap);
						mine_img.setImageBitmap(FaceUtils.toRoundBitmap(bd.getBitmap()));
					}
				}
			});
			if(bitmap!=null){
				BitmapDrawable bd = new BitmapDrawable(getResources(),
						bitmap);
				mine_img.setImageBitmap(FaceUtils.toRoundBitmap(bd.getBitmap()));
			}else{
				BitmapDrawable bd = getImgResource(R.drawable.moren);
				mine_img.setImageBitmap(FaceUtils.toRoundBitmap(bd.getBitmap()));
			}
		}else{
			BitmapDrawable bd = getImgResource(R.drawable.moren);
			mine_img.setImageBitmap(FaceUtils.toRoundBitmap(bd.getBitmap()));
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		showTxt=new TextView[items.length];
		String nicknm=preferences.getString("nickname", "");
		items_value[0]=nicknm;
		String phone=preferences.getString("username", null);
		//处理一下电话
		if(phone!=null&&phone.length()>0){
			String s=phone.substring(0, 3);
			String d=phone.substring(7, 11);
			items_value[1]=s+"****"+d;
		}
	}

	/**
	 * 填充列表数据
	 * */
	private void initListView() {
//		mine_version.setText(version);
		list_mine.setAdapter(new MineListApr());
	}
	
	private void findById() {
		list_mine=(ListView)view.findViewById(R.id.list_mine);
		mine_img=(ImageView)view.findViewById(R.id.mine_img);
		mine_version=(TextView)view.findViewById(R.id.mine_version);
		logout=(RelativeLayout)view.findViewById(R.id.logout);
	}
	
	private class MineListApr extends BaseAdapter{
		@Override
		public int getCount() {
			return items.length;
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
		public View getView(int index, View cv, ViewGroup arg2) {
			View v;
			Holder holder;
			if(cv==null){
				v=LayoutInflater.from(context).inflate(R.layout.item_mine_listitem, null);
				holder=new Holder();
				holder.list_name=(TextView)v.findViewById(R.id.list_name);
				holder.show=(TextView)v.findViewById(R.id.show);
				v.setTag(holder);
			}else{
				v=cv;
				holder=(Holder)v.getTag();
			}
			showTxt[index]=holder.show; //保存每个txt项
			holder.list_name.setText(items[index]);
			holder.show.setText(items_value[index]);
			final int aa=index;
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(aa==0){
						startActivityForResult(new Intent(context, NickNameActivity.class),1);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
					if(aa==1){
						startActivityForResult(new Intent(context, MobileActivity .class),2);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
					if(aa==2){
						startActivityForResult(new Intent(context, PasswordActivity .class),3);
						Activity context = getActivity();
						context.overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				}
			});
			return v;
		}
		
	}
	class Holder{
		TextView list_name;
		TextView show;
	}
	/**
	 * 退出
	 * */
	public void logout(){
		//退出登录
		dialog=dialog(context,"正在登出..");
		dialog.show();
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);  
        client.setCookieStore(myCookieStore);
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/logout";
		client.post(RegistPath,new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				try {
					if(checkJson(arg1)){
						Editor editor = preferences.edit(); 
						editor.putString("username", null);
						editor.putString("passwd", null);
						editor.putString("nickname", null);
						editor.putString("headerImage", null);
						editor.putBoolean("isLandIn", false);
						editor.commit();
						mCache.put("allSave", "unLogIn");
						//mark
 						IndexActivity activity=(IndexActivity)getActivity();
						activity.setTabSelection(5);
					}
				} catch (Exception e) {
					System.out.println("错误:"+e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "退出错误--网络有错误", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(2000);
					dialog.dismiss();
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}).start();
		
	}
	/**剪裁*/
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
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode==1&&resultCode==1){
			if(data.getBooleanExtra("result_nicknm", false)){
				msg("修改成功");
				showTxt[0].setText(data.getStringExtra("nickname"));
			}
		}
		if(requestCode==2&&resultCode==2){
			if(data.getBooleanExtra("result_phone", false)){
				msg("修改成功");
				showTxt[1].setText(data.getStringExtra("phone"));
			}
		}
		if(requestCode==3&&resultCode==3){
			if(data.getBooleanExtra("result_pwd", false)){
				msg("修改成功");
			}
		}
		/**图库*/
		if(requestCode==4){
			if (data != null) {
				Uri uri = data.getData();
				crop(uri);
			}
		}
		/**剪裁*/
		if(requestCode==5){
			try {
				Bitmap bitmap = data.getParcelableExtra("data");
				mine_img.setImageBitmap(FaceUtils.toRoundBitmap(bitmap));
				//把得到的bitmap对象存到file中
				if(saveBitmap2file(bitmap,PHOTO_FILE_NAME)){
					File file=new File(getImgCachePath(), PHOTO_FILE_NAME);
					/**上传*/
					AsyncHttpClient client = new AsyncHttpClient();
					PersistentCookieStore myCookieStore = new PersistentCookieStore(context);  
		            client.setCookieStore(myCookieStore); 
					RequestParams params = new RequestParams();
					params.put("upload", file);
					String pathString="http://www.xiangyouji.com.cn:3000/my/uploadHeaderImage";
					client.post(pathString, params, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int arg0, String arg1) {
							// TODO Auto-generated method stub
							try {
								if(checkJson(arg1)){
									msg("头像上传成功");
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							super.onSuccess(arg0, arg1);
						}
						@Override
						public void onFailure(Throwable arg0, String arg1) {
							// TODO Auto-generated method stub
							msg("网络问题");
							super.onFailure(arg0, arg1);
						}
					});
				}else{
					msg("头像上传失败");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/**拍照*/
		if(requestCode==6){
			if (hasSdcard()) {
				tempFile = new File(Environment.getExternalStorageDirectory(),
						PHOTO_FILE_NAME);
				crop(Uri.fromFile(tempFile));
			} else {
				msg("未找到存储卡，无法存储照片");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
