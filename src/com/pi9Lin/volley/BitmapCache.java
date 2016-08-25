package com.pi9Lin.volley;

import com.android.volley.toolbox.ImageLoader.ImageCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;



public class BitmapCache implements ImageCache{
	private LruCache<String, Bitmap> mCache;
	Context context;

	public BitmapCache(Context context) {
		this.context = context;
		int maxSize = 10 * 1024 * 1024;
		mCache = new LruCache<String, Bitmap>(maxSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {

				return bitmap.getRowBytes() * bitmap.getHeight();
			}
		};
	}

	@Override
	public Bitmap getBitmap(String url) {
		return mCache.get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		mCache.put(url, bitmap);
		new Thread(new Runs(url, bitmap)).start();
	}

	class Runs implements Runnable {

		String url;
		Bitmap bitmap;

		public Runs(String url, Bitmap bitmap) {
			super();
			this.url = url;
			this.bitmap = bitmap;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			VolleyBins.saveBitmap2(bitmap, url, context);
		}

	}
}
