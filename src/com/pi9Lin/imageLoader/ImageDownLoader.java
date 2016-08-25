package com.pi9Lin.imageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;


@SuppressLint({ "HandlerLeak", "NewApi" })
public class ImageDownLoader {

	/**
	 * 记录所有正在下载或等待下载的任务。
	 */
	public List<MyTask> taskCollection;
	/**
	 * 缓存Image的类，当存储Image的大小大于LruCache设定的值，系统自动释放内存
	 */
	private LruCache<String, Bitmap> mMemoryCache;
	/**
	 * 操作文件相关类对象的引用
	 */
	public FileUtils fileUtils;
	/**
	 * 下载Image的线程池
	 */
	private ExecutorService mImageThreadPool = null;
	/**
	 * 单例网络请求实例
	 * */
	private static HttpClient httpClient;
	private static int screenWidth;

	public ImageDownLoader(Context context) {
		screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		// 获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int mCacheSize = maxMemory / 8;
		// 给LruCache分配1/8 4M
		mMemoryCache = new LruCache<String, Bitmap>(mCacheSize) {
			// 必须重写此方法，来测量Bitmap的大小
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}
		};
		fileUtils = new FileUtils(context);
		taskCollection = new ArrayList<MyTask>();
	}

	/**
	 * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
	 */
	public ExecutorService getThreadPool() {
		if (mImageThreadPool == null) {
			synchronized (ExecutorService.class) {
				if (mImageThreadPool == null) {
					// 为了下载图片更加的流畅，我们用了5个线程来下载图片
					mImageThreadPool = Executors.newFixedThreadPool(5);
				}
			}
		}
		return mImageThreadPool;
	}

	/**
	 * 添加Bitmap到内存缓存
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemCache(key) == null && bitmap != null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * 从内存缓存中获取一个Bitmap
	 */
	public Bitmap getBitmapFromMemCache(String key) {
		return mMemoryCache.get(key);
	}

	/**
	 * 最核心的部分   提供图片url 返回bitmap和url
	 * 先从内存缓存中获取Bitmap,如果没有就从SD卡或者手机缓存中获取，SD卡或者手机缓存 没有就去下载
	 */
	public Bitmap downloadImage(final String url,final onImageLoaderListener listener) {
		// 替换Url中非字母和非数字的字符，这里比较重要，因为我们用Url作为文件名，比如我们的Url
		// 是Http://xiaanming/abc.jpg;用这个作为图片名称，系统会认为xiaanming为一个目录，
		// 我们没有创建此目录保存文件就会保存
		Bitmap bitmap = showCacheBitmap(url);
		if (bitmap != null) {
			return bitmap;
		} else {
			MyTask task = new MyTask(url, listener);
			taskCollection.add(task);
			task.execute(url);
//			final Handler handler = new Handler(){
//				 @Override
//				 public void handleMessage(Message msg) {
//					 super.handleMessage(msg);
//					 listener.onImageLoader((Bitmap)msg.obj, url);
//				 }
//			};
//			getThreadPool().submit(new Runnable() {
//				 @Override
//				 public void run() {
//					 Message msg = handler.obtainMessage();
//					 msg.obj = saveBitmapFormUrl(url);
//					 handler.sendMessage(msg);
//				 }
//			});
		}
		return null;
	}

	public class MyTask extends AsyncTask<String, Void, Bitmap> {

		String url;
		onImageLoaderListener listener;

		public MyTask(String url, onImageLoaderListener listener) {
			// TODO Auto-generated constructor stub
			this.url = url;
			this.listener = listener;
		}

		@Override
		protected Bitmap doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			return saveBitmapFormUrl(arg0[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			taskCollection.remove(MyTask.this);
			listener.onImageLoader(result, url);
			super.onPostExecute(result);
		}
	}

	/**
	 * 获取Bitmap, 内存中没有就去手机或者sd卡中获取，这一步在getView中会调用，比较关键的一步
	 */
	public Bitmap showCacheBitmap(String url) {
		String subUrl=url.replaceAll("[^\\w]", "");
		if (getBitmapFromMemCache(subUrl) != null) {
			 // 先缓存
			 return getBitmapFromMemCache(subUrl);
		 }
		 else if (fileUtils.isFileExists(subUrl)
		 && fileUtils.getFileSize(subUrl) != 0) {
			 // 再SD卡
			 Bitmap bitmap = fileUtils.getBitmap(subUrl);
			 // 将Bitmap 加入内存缓存
			 addBitmapToMemoryCache(subUrl, bitmap);
			 return bitmap;
		 }
		 return null;
	}

	/**
	 * 从Url中网络去下载Bitmap
	 */
	private Bitmap saveBitmapFormUrl(String url) {
		try {
			InputStream inputStream = getHTTpInfo(url, "GET", "");
			String subUrl = url.replaceAll("[^\\w]", "");
			if (inputStream != null) {
				byte[] data = readStream(inputStream);
				Bitmap bitmap = decodeSampledBitmap(data);
				if (bitmap == null) {
					System.out.println("+++++++++bitmap=null");
				}
				// 保存在SD卡或者手机目录
				fileUtils.saveBitmap(subUrl, bitmap);
				// 将Bitmap 加入内存缓存
				addBitmapToMemoryCache(subUrl, bitmap);
				return bitmap;
			} else {
				System.out.println("+++++++++inputStream=null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 计算缩放比例
	 * */
	public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth) {
		// 源图片的高度和宽度
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = widthRatio;
		}
		return inSampleSize;
	}

	/**
	 * 从网络下载的压缩后的图片 防止oom BitmapFactory.decodeByteArray
	 * */
	public static Bitmap decodeSampledBitmap(byte[] data) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		//为位图设置100K的缓存
		options.inTempStorage = new byte[100 * 1024];
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		//设置图片可以被回收，创建Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收
		options.inPurgeable = true;
		options.inSampleSize = calculateInSampleSize(options, screenWidth);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	/**
	 * 得到图片字节流 数组大小
	 */
	public static byte[] readStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
		}
		outStream.close();
		inStream.close();
		return outStream.toByteArray();
	}

	/**
	 * 网络连接
	 * */
	public InputStream getHTTpInfo(String urlString, String fun, String parm) {
		try {
			HttpClient client = getHttpClient();
			HttpGet get = new HttpGet(urlString);
			HttpResponse response = client.execute(get);
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
	 * 单例网络请求实例
	 * */
	private static synchronized HttpClient getHttpClient() {
		if (httpClient == null) {
			final HttpParams httpParams = new BasicHttpParams();
			// timeout: get connections from connection pool
			ConnManagerParams.setTimeout(httpParams, 11000);
			// timeout: connect to the server
			HttpConnectionParams.setConnectionTimeout(httpParams, 11000);
			// timeout: transfer data from server
			HttpConnectionParams.setSoTimeout(httpParams, 11000);
			// set max connections per host
			ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
					new ConnPerRouteBean(11000));
			// set max total connections
			ConnManagerParams.setMaxTotalConnections(httpParams, 11000);
			// use expect-continue handshake
			HttpProtocolParams.setUseExpectContinue(httpParams, true);
			// disable stale check
			HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
			HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
			HttpClientParams.setRedirecting(httpParams, false);
			// set user agent
			String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
			HttpProtocolParams.setUserAgent(httpParams, userAgent);
			// disable Nagle algorithm
			HttpConnectionParams.setTcpNoDelay(httpParams, true); // nagle算法默认是打开的，会引起delay的问题；所以要手工关掉。
			HttpConnectionParams.setSocketBufferSize(httpParams, 11000);
			// scheme: http and https
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));
			ClientConnectionManager manager = new ThreadSafeClientConnManager(
					httpParams, schemeRegistry);
			httpClient = new DefaultHttpClient(manager, httpParams);
		}
		return httpClient;
	}

	/**
	 * 取消正在下载的任务
	 */
	public synchronized void cancelTask() {
		if (mImageThreadPool != null) {
			mImageThreadPool.shutdownNow();
			mImageThreadPool = null;
		}
		if (taskCollection != null) {
			for (MyTask task : taskCollection) {
				task.cancel(true);
			}
		}
	}

	/**
	 * 异步下载图片的回调接口
	 */
	public interface onImageLoaderListener {
		void onImageLoader(Bitmap bitmap, String url);
	}
	
	/** 
     * 根据ImageView获适当的压缩的宽和高 
     *  
     * @param imageView 
     * @return 
     */  
    public static ImageSize getImageViewSize(ImageView imageView)  
    {  
  
        ImageSize imageSize = new ImageSize();  
        DisplayMetrics displayMetrics = imageView.getContext().getResources()  
                .getDisplayMetrics();  
  
        LayoutParams lp = imageView.getLayoutParams();  
  
        int width = imageView.getWidth();// 获取imageview的实际宽度  
        if (width <= 0)  
        {  
            width = lp.width;// 获取imageview在layout中声明的宽度  
        }  
        if (width <= 0)  
        {  
            width = imageView.getMaxWidth();// 检查最大值  
//            width = getImageViewFieldValue(imageView, "mMaxWidth");  
        }  
        if (width <= 0)  
        {  
            width = displayMetrics.widthPixels;  
        }  
  
        int height = imageView.getHeight();// 获取imageview的实际高度  
        if (height <= 0)  
        {  
            height = lp.height;// 获取imageview在layout中声明的宽度  
        }  
        if (height <= 0)  
        {  
        	height = imageView.getMaxHeight();
//            height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值  
        }  
        if (height <= 0)  
        {  
            height = displayMetrics.heightPixels;  
        }  
        imageSize.width = width;  
        imageSize.height = height;  
  
        return imageSize;  
    }  
  
    public static class ImageSize  
    {  
        int width;  
        int height;  
    } 
	
}
