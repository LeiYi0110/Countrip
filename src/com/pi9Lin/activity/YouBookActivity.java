package com.pi9Lin.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.bins.MyListView;
import com.pi9Lin.countrip.R;

public class YouBookActivity extends BaseActivity {
	
	private LayoutInflater li;
	private RelativeLayout backward;
	private MyListView lv;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_you_book);
		li = LayoutInflater.from(context);
		initActionBar();
		findById();
		setOnClickListener();
		initList();
	}
	
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		backward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
	}

	private void initActionBar() {
		// TODO Auto-generated method stub
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.layout_youbook_top);// 自定义ActionBar布局
		View v = actionBar.getCustomView();
		backward = (RelativeLayout) v.findViewById(R.id.backward);
	}
	
	private void initList() {
		// TODO Auto-generated method stub
		lv.setAdapter(new MyBaseApr());
	}
	
	private class MyBaseApr extends BaseAdapter{
		/**
		 * ListView包含不同Item的布局 　　我们需要做这些工作: 　　1）重写 getViewTypeCount() –
		 * 该方法返回多少个不同的布局 　　2）重写 getItemViewType(int) – 根据position返回相应的Item 　
		 * 3）根据view item的类型，在getView中创建正确的convertView
		 * */
		final int TYPE_1 = 0;
		final int TYPE_2 = 1;
		final int TYPE_3 = 2;
		
		public MyBaseApr(){
		}
		
		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			if (position == 0) {
				return TYPE_1;
			} else if (position == 1) {
				return TYPE_2;
			} else{
				return TYPE_3;
			}
		}
		
		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return 3;
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
		
		private class Holder{
			TextView time_of_in;
			TextView time_of_out;
			TextView change;
			ImageView img_hot;
			TextView txt_title;
			TextView txt_detail;
			ImageView item_img;
			RelativeLayout want_book;
		}
		
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v = null;
			final Holder h;
			int type = getItemViewType(arg0);
			if(arg1==null){
				h=new Holder();
				if (type == TYPE_1) {
					v = li.inflate(R.layout.activity_book_type1, null);
					h.time_of_in = (TextView) v.findViewById(R.id.time_of_in);
					h.time_of_out = (TextView) v.findViewById(R.id.time_of_out);
					h.change = (TextView) v.findViewById(R.id.change);
				}else if(type == TYPE_2){
					v = li.inflate(R.layout.item_youbook_title, null);
					h.img_hot=(ImageView)v.findViewById(R.id.img_hot);
					h.txt_title=(TextView)v.findViewById(R.id.txt_title);
					h.txt_detail=(TextView)v.findViewById(R.id.txt_detail);
				}else if(type==TYPE_3){
					v = li.inflate(R.layout.item_youbook_listitem, null);
					h.item_img=(ImageView)v.findViewById(R.id.item_img);
					h.want_book=(RelativeLayout)v.findViewById(R.id.want_book);
				}
				v.setTag(h);
			}else{
				v = arg1;
				h = (Holder) v.getTag();
			}
			if(type == TYPE_2) {
				h.img_hot.setImageDrawable(getImgResource(R.drawable.app_icon));
				//mark
			}
			return v;
		}
	}
	
	private void findById() {
		// TODO Auto-generated method stub
		lv=(MyListView)findViewById(R.id.lv);
	}
}
