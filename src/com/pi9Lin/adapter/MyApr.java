package com.pi9Lin.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 被用在景点列表SleepActivity中
 * */
public class MyApr extends PagerAdapter {

	ImageView[] list;
	Context context;

	public MyApr(Context context,ImageView[] arg1) {
		// TODO Auto-generated constructor stub
		list = arg1;
		this.context=context;
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
		/**解决子view有两个parentview问题  在listview嵌套vpager时出现*/
            ViewGroup p = (ViewGroup) list[position].getParent(); 
            if (p != null) { 
                p.removeAllViewsInLayout(); 
            } 
		container.addView(list[position]);
		final int aa=position;
		list[position].setOnClickListener(new OnClickListener() {
			
			@SuppressLint("ShowToast")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(context, ""+aa, 2).show();
			}
		});
		return list[position];
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		container.removeView(list[position]);
	}

}
