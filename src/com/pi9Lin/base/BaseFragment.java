package com.pi9Lin.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.data.DataAll;
import com.pi9Lin.data.Entity;
import com.pi9Lin.database.MyDB;

/**
 * 所有碎片基类
 * 
 * */
public class BaseFragment extends Fragment {

	DataAll dataAll;
	protected Context context;
	protected SharedPreferences preferences;
	protected static final String PHOTO_FILE_NAME = "temp_photo.jpg";
	protected ImageLoader imageLoader=ImageLoader.getInstance();
	protected DisplayImageOptions options;
	protected ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	/**
	 * 解析收藏
	 * */
	protected List<Entity> save(String data) throws Exception {
		JSONObject jsonObject = new JSONObject(data);
		JSONArray array = jsonObject.getJSONArray("data");
		List<Entity> list = new ArrayList<Entity>();
		for (int i = 0; i < array.length(); i++) {
			Entity entity = new Entity();
			JSONObject object = (JSONObject) array.opt(i);
			String entity_id = object.getString("entity_id");
			int entity_type = object.getInt("entity_type");
			entity.setEntity_id(entity_id);
			entity.setEntity_type(entity_type);
			list.add(entity);
		}
		return list;
	}

	/**
	 * 从本地数据库读取区域表信息 区域表连接整个数据表 是核心表
	 * */
	public List<Area> getAreaInfos() {
		List<Area> infos = new ArrayList<Area>();
		MyDB myDB = new MyDB(context);
		SQLiteDatabase db = myDB.getWritableDatabase();
		Cursor cursor = db.rawQuery("select * from area", null);
		while (cursor.moveToNext()) {
			Area info = new Area();
			info.setArea_id(cursor.getInt(cursor.getColumnIndex("area_id")));
			info.setCityId(cursor.getInt(cursor.getColumnIndex("cityId")));
			info.setProvinceId(cursor.getInt(cursor
					.getColumnIndex("provinceId")));
			info.setArea_name(cursor.getString(cursor
					.getColumnIndex("area_name")));
			double[] gps = {
					cursor.getDouble(cursor.getColumnIndex("latitude")),
					cursor.getDouble(cursor.getColumnIndex("longtitude")) };
			info.setGps(gps);
			infos.add(info);
		}
		cursor.close();
		db.close();
		myDB.close();
		return infos;
	}

	public String obj2Json(List<Entity> list) throws Exception {
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject object = new JSONObject();
			object.put("entity_id", list.get(i).getEntity_id());
			object.put("entity_type", list.get(i).getEntity_type());
			array.put(i, object);
		}
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("data", array);
		return jsonObject.toString();
	}

	/**
	 * 去掉定位结果中的市字
	 * */
	public String stringToCitynm(String string) {
		String[] ss = string.split("市");
		return ss[0];
	}

	/**
	 * 输入流转化为字符串
	 * */
	public String inputStreamToString(InputStream inputStream) {
		String outString = "";
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String lenString = "";
			while ((lenString = bufferedReader.readLine()) != null) {
				outString += lenString;
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("异常:" + e.getMessage());
		}
		return outString;
	}

	/**
	 * 从本地路径把读出压缩后的图片 防止oom
	 * */
	public static Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(pathName, options);
	}

	/**
	 * 保存图片
	 * */
	public void saveDownLoadImg(String filepath, String filename,
			InputStream inputStream) throws Exception {
		File file = new File(filepath);
		if (!file.exists()) {
			file.mkdirs();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(new File(file,
				filename));
		byte[] arrs = new byte[1024];
		int len = 0;
		while ((len = inputStream.read(arrs)) != -1) {
			fileOutputStream.write(arrs, 0, len);
		}
		fileOutputStream.close();
	}

	/**
	 * 获得图片缓存地址
	 * */
	public String getImgCachePath() {
		if (isWriteSD()) {
			String pathString = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/Countrip";
			if (!new File(pathString).exists()) {
				new File(pathString).mkdirs();
			}
			return pathString;
		} else {
			String pathString = context.getCacheDir().getAbsolutePath();
			if (!new File(pathString).exists()) {
				new File(pathString).mkdirs();
			}
			return pathString;
		}
	}

	/**
	 * 网络连接
	 * */
	public InputStream getHTTpInfo(String urlString, String fun, String parm)
			throws Exception {
		try {
			HttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 1000 * 15);
			HttpGet get = new HttpGet(urlString);
			HttpResponse response;

			response = client.execute(get);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = response.getEntity();
				return entity.getContent();
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

	/**
	 * 等待提示
	 * */
	public Dialog dialog(Context context, String string) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);
		TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);
		tipTextView.setText(string);// 设置加载信息
		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

		loadingDialog.setCancelable(false);// 不可以用“返回键”取消
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
		return loadingDialog;
	}

	/**
	 * 加载头像提示
	 * */
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
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
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
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK);
				intent.setType("image/*");
				startActivityForResult(intent, 4);
				if (dd != null) {
					dd.dismiss();
				}
			}
		});
		/** 拍照 */
		from_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				if (hasSdcard()) {
					intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri
							.fromFile(new File(Environment
									.getExternalStorageDirectory(),
									PHOTO_FILE_NAME)));
				}
				startActivityForResult(intent, 6);
				if (dd != null) {
					dd.dismiss();
				}
			}
		});
	}

	// 判断sd卡是否可读
	public boolean hasSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 打印函数
	 * */
	public void msg(String s) {
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 获取本地配置
	 * */
	public SharedPreferences getConfig() {
		return context.getSharedPreferences("RecordSets", Context.MODE_PRIVATE);
	}

	/** 从本地以消耗内存最小方式获取图片资源 */
	public BitmapDrawable getImgResource(int r) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		InputStream is = getResources().openRawResource(r);
		Bitmap bm = BitmapFactory.decodeStream(is, null, opt);
		BitmapDrawable bd = new BitmapDrawable(getResources(), bm);
		return bd;
	}

	/**
	 * 异步去获取图片 先保存在本地 从本地读取图片 同时做了本地缓存
	 * */
	public Bitmap getBitmap(String urlString, String GG, int i) {
		try {
			URL url = new URL(urlString);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(5000);
			connection.setDoInput(true);
			connection.connect();
			/** 从连接对象上获得输入流 */
			InputStream inputStream = connection.getInputStream();
			if (inputStream != null) {
				/** 保存的文件名 */
				String path = getImgCachePath();
				String filename = getUrlFileName(urlString);
				saveDownloadImg(path, filename, inputStream);
				saveJsonImg(path + "/" + filename, GG, i);
				inputStream.close();
				return getImgByPath(path + "/" + filename, 4);
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println("错误:" + e.getMessage());
		}
		return null;
	}

	public boolean saveBitmap2file(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.JPEG;
		int quality = 100;
		OutputStream stream = null;
		try {
			stream = new FileOutputStream(getImgCachePath() + "/" + filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bmp.compress(format, quality, stream);
	}

	/**
	 * 获取图片文件名
	 * */
	public String getUrlFileName(String urlString) {
		String outString = "";
		int lastIndex = urlString.lastIndexOf("/");
		outString = urlString.substring(lastIndex + 1);
		return outString;
	}

	/**
	 * 判断图片文件是否存在
	 * */
	public File isImgExist(String urlString) {
		String path = getImgCachePath();
		String fnameString = getUrlFileName(urlString);
		File file = new File(path + "/" + fnameString);
		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	/**
	 * 保存图片本地地址
	 * */
	private void saveJsonImg(String path, String GG, int i) {
		try {
			OutputStream outputStream = context.openFileOutput(GG + i + ".txt",
					Context.MODE_PRIVATE);
			outputStream.write(path.getBytes());
			outputStream.close();
		} catch (Exception e) {
			msg("缓存失败:" + e.getMessage());
		}
	}

	/**
	 * 从网络上下载图片先保存到本地 用时间戳做文件名
	 * */
	private void saveDownloadImg(String path, String filename,
			InputStream inputStream) throws Exception {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		FileOutputStream fileOutputStream = new FileOutputStream(new File(file,
				filename));
		byte[] arrs = new byte[1024];// 1k
		int len = 0;
		while ((len = inputStream.read(arrs)) != -1) {
			fileOutputStream.write(arrs, 0, len);
		}
		fileOutputStream.close();
	}

	/**
	 * 判断sd卡是否可写(有用)
	 * */
	protected boolean isWriteSD() {
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 * 通过本地路径把流文件转化为压缩图片
	 * */
	public static Bitmap getImgByPath(String path, int reqWidth) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	/**
	 * 对图片进行压缩
	 * */
	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth) {
		// TODO Auto-generated method stub
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			final int widthRadio = Math.round((float) width / (float) reqWidth);
			inSampleSize = widthRadio;
		}
		return inSampleSize;
	}

	/**
	 * 解析收藏
	 * */
	public List<Entity> getAllSave(String data) throws Exception {
		JSONObject jsonObject = new JSONObject(data);
		JSONArray array = jsonObject.getJSONArray("data");
		List<Entity> list = new ArrayList<Entity>();
		for (int i = 0; i < array.length(); i++) {
			Entity entity = new Entity();
			JSONObject object = (JSONObject) array.opt(i);
			String entity_id = object.getString("entity_id");
			int entity_type = object.getInt("entity_type");
			entity.setEntity_id(entity_id);
			entity.setEntity_type(entity_type);
			list.add(entity);
		}
		return list;
	}

	/**
	 * 检验json
	 * */
	public boolean checkJson(String arg1) throws Exception {
		JSONObject jsonObject = new JSONObject(arg1);
		int status = jsonObject.getInt("status");
		if (status == 1) {
			return true;
		}
		return false;
	}

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) {
					displayedImages.add(imageUri);
				}
				FadeInBitmapDisplayer.animate(imageView, 500);
			}
		}
	}
	
	public void initImageLoader(){
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.empty_img)			
		.showImageForEmptyUri(R.drawable.empty_img)	
		.showImageOnFail(R.drawable.empty_img)		
		.cacheInMemory(true)						
		.cacheOnDisc(true)							
		.build();
	}
	
}
