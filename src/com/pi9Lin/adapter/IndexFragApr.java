package com.pi9Lin.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class IndexFragApr extends PagerAdapter {
	
	ImageView[] list;
	
	public IndexFragApr(ImageView[] arg1) {
		// TODO Auto-generated constructor stub
		list = arg1;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(list[position]);
		return list[position];
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		container.removeView(list[position]);
	}

}
