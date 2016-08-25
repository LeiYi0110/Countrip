package com.pi9Lin.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

public class ImagePagerActivity extends BaseActivity {

	private static final String STATE_POSITION = "STATE_POSITION";

	DisplayImageOptions options;

	ViewPager pager;
	
	
	
	TextView indicator;

	private String[] imageUrls;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ac_image_pager);
		initImageLoader(); // 初始化图片异步处理对象
		Bundle bundle = getIntent().getExtras();
		imageUrls = bundle.getStringArray("images");
		// 当前显示View的位置
		int pagerPosition = bundle.getInt("position", 0);
		
		// 如果之前有保存用户数据
		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		pager = (ViewPager) findViewById(R.id.pager);
		indicator = (TextView) findViewById(R.id.indicator);
		pager.setAdapter(new ImagePagerAdapter(imageUrls));
		pager.setCurrentItem(pagerPosition); // 显示当前位置的View
		indicator.setText((pagerPosition+1)+"/"+imageUrls.length);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				indicator.setText((arg0+1)+"/"+imageUrls.length);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// 保存用户数据
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private String[] images;
		private LayoutInflater inflater;

		ImagePagerAdapter(String[] images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public Object instantiateItem(ViewGroup view,final int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image,
					view, false);
			imageLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					//结束页面
	                Intent intent = new Intent();
	                intent.putExtra("position", position);
	                //把返回数据存入Intent
	                setResult(RESULT_OK, intent);
	                //关闭Activity
	                finish();
				}
			});
			ImageView imageView = (ImageView) imageLayout
					.findViewById(R.id.image);
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);
			imageLoader.displayImage(images[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) { // 获取图片失败类型
							case IO_ERROR: // 文件I/O错误
								message = "Input/Output error";
								break;
							case DECODING_ERROR: // 解码错误
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED: // 网络延迟
								message = "网络延迟";
								break;
							case OUT_OF_MEMORY: // 内存不足
								message = "内存不足";
								break;
							case UNKNOWN: // 原因不明
								message = "Unknown error";
								break;
							}
							Toast.makeText(ImagePagerActivity.this, message,
									Toast.LENGTH_SHORT).show();

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE); // 不显示圆形进度条
						}
					});

			((ViewPager) view).addView(imageLayout, 0); // 将图片增加到ViewPager
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}
	}
}