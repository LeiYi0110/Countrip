package com.pi9Lin.start;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.activity.IndexActivity;
import com.pi9Lin.adapter.MyStartApr;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

public class GuideActivity extends BaseActivity {
	
	private ViewPager yindaoye;
	private LinearLayout ll_dots;
	private int sum;
	private ImageView[] dots;
	private List<View> pagelist;
	private TextView txt_start;
	private RelativeLayout to_start;
	private int[] resImg = { R.drawable.yindao_bg1, R.drawable.yindao_bg2,
			R.drawable.yindao_bg3 };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);
		findById();
		initDots();
		initVPage();
		setOnClickListener();
	}
	
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		to_start.setBackgroundColor(getResources().getColor(R.color.press));
		to_start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/** 跳转到主页面 */
				Intent intent = new Intent(context, IndexActivity.class);
				startActivity(intent);
				GuideActivity.this.finish();
			}
		});
	}

	@SuppressLint("NewApi")
	private void initVPage() {
		// TODO Auto-generated method stub
		pagelist = new ArrayList<View>();
		for (int i = 0; i < resImg.length; i++) {
			/**
			 * 换成添加布局
			 * */
			View view = LayoutInflater.from(context).inflate(
					R.layout.activity_start, null);
			FrameLayout f = (FrameLayout) view.findViewById(R.id.ffff);
			f.setBackground(getImgResource(resImg[i]));
			txt_start = (TextView) view.findViewById(R.id.txt_start);
			if (i == 0) {
				txt_start.setText("新奇独特的场景，总有您意想不到的惊喜。");
			}
			if (i == 1) {
				txt_start.setText("无论在哪里旅行，您都可以吃到当地最特色的美食。");
			}
			if (i == 2) {
				txt_start.setText("从别墅到木屋，各式各样的独特房源任您挑选。");
			}
			pagelist.add(view);
		}
		/**
		 * 添加最后一个引导页面
		 * */
		View view = LayoutInflater.from(context).inflate(
				R.layout.layout_start_last, null);
		ImageView home_img, round_img, mine_img, save_img;
		TextView home_txt, round_txt, mine_txt, save_txt;

		home_img = (ImageView) view.findViewById(R.id.home_img);
		save_img = (ImageView) view.findViewById(R.id.save_img);
		round_img = (ImageView) view.findViewById(R.id.round_img);
		mine_img = (ImageView) view.findViewById(R.id.mine_img);
		home_txt = (TextView) view.findViewById(R.id.home_txt);
		round_txt = (TextView) view.findViewById(R.id.round_txt);
		mine_txt = (TextView) view.findViewById(R.id.mine_txt);
		save_txt = (TextView) view.findViewById(R.id.save_txt);
		to_start = (RelativeLayout) view.findViewById(R.id.to_start);

		home_img.setImageDrawable(getImgResource(R.drawable.index));
		round_img.setImageDrawable(getImgResource(R.drawable.round));
		mine_img.setImageDrawable(getImgResource(R.drawable.mine));
		save_img.setImageDrawable(getImgResource(R.drawable.save_img));

		home_txt.setTextColor(getResources().getColor(R.color.press));
		round_txt.setTextColor(getResources().getColor(R.color.press));
		mine_txt.setTextColor(getResources().getColor(R.color.press));
		save_txt.setTextColor(getResources().getColor(R.color.press));

		pagelist.add(view);
		yindaoye.setAdapter(new MyStartApr(pagelist));
		yindaoye.setCurrentItem(0);
		yindaoye.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				for (int i = 0; i < sum; i++) {
					dots[i].setImageDrawable(getImgResource(R.drawable.kongxin));
				}
				dots[arg0].setImageDrawable(getImgResource(R.drawable.shixin));
				ll_dots.setVisibility(View.VISIBLE);
				if (arg0 == (sum - 1)) {
					ll_dots.setVisibility(View.GONE);
				}
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
	
	private void initDots() {
		// TODO Auto-generated method stub
		sum = resImg.length + 1;
		dots = new ImageView[sum];
		for (int i = 0; i < sum; i++) {
			ImageView imageView = new ImageView(context);
			if (i == 0) {
				// 第一个点为白色实心
				imageView.setImageDrawable(getImgResource(R.drawable.shixin));
			} else {
				// 之后为空心
				imageView.setImageDrawable(getImgResource(R.drawable.kongxin));
			}
			imageView.setPadding(0, 0, 20, 0);
			imageView.setId(i);
			imageView.setMinimumWidth(30);
			imageView.setMaxHeight(30);
			dots[i] = imageView;
			ll_dots.addView(imageView);
		}// for
	}
	
	private void findById() {
		// TODO Auto-generated method stub
		yindaoye = (ViewPager) findViewById(R.id.yindaoye);
		ll_dots = (LinearLayout) findViewById(R.id.ll_dots);
	}
}
