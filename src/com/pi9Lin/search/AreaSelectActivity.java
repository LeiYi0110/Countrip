package com.pi9Lin.search;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.bins.NoScrollGridView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.Area;
import com.pi9Lin.database.MyDB;

public class AreaSelectActivity extends BaseActivity {
	
	TextView ctnm;
	RelativeLayout backward;
	NoScrollGridView area_gridlist;
	List<Area> areas;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_areaselect);
		findById();
		initActionbar();
		setOnClickListener();
		init();
		setGridView();
	}
	
	private void initActionbar() {
		//自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.activity_areaselect_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
        backward = (RelativeLayout)v.findViewById(R.id.backward);
	}
	
	private void setGridView() {
		// TODO Auto-generated method stub
		area_gridlist.setAdapter(new MyGrid(areas));
	}
	
	private class MyGrid extends BaseAdapter{
		List<Area> areas;
		MyGrid(List<Area> areas){
			this.areas=areas;
		} 
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return areas.size();
		}
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return areas.get(arg0);
		}
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v;
			ViewHolder viewHolder;
			if(arg1==null){
				v=LayoutInflater.from(context).inflate(R.layout.item_search_gridlist, null);
				viewHolder=new ViewHolder();
				viewHolder.area_name =(TextView)v.findViewById(R.id.cityName);
				v.setTag(viewHolder);
			}else{
				v=arg1;
				viewHolder=(ViewHolder)v.getTag();
			}
			viewHolder.area_name.setText(areas.get(arg0).getArea_name());
			final int aa=arg0;
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//数据是使用Intent返回
	                Intent intent = new Intent();
	                //把返回数据存入Intent
	                intent.putExtra("area_id", areas.get(aa).getArea_id());
	                intent.putExtra("area_name", areas.get(aa).getArea_name());
	                intent.putExtra("province", areas.get(aa).getProvinceId());
	                //设置返回数据
	                setResult(RESULT_OK, intent);
	                //关闭Activity
	                finish();
				}
			});
			return v;
		}
		class ViewHolder{
			TextView area_name;
		}
	}
	
	private void init() {
		// TODO Auto-generated method stub
		Intent intent=getIntent();
		int cityId=intent.getIntExtra("cityId",-1);
		String cityName=intent.getStringExtra("cityName");
		ctnm.setText(cityName);
		if(cityId>0){
			MyDB myDB = new MyDB(context);
			SQLiteDatabase db = myDB.getWritableDatabase();
			Cursor cursor = db.rawQuery("select * from area where cityId=?",
					new String[] { cityId+"" });// 有可能定位的区名字和本地名字不符 以至找不到
			areas=new ArrayList<Area>();
			while (cursor.moveToNext()) {
				Area area=new Area();
				int area_id = cursor.getInt(cursor.getColumnIndex("area_id"));
				String area_name = cursor.getString(cursor.getColumnIndex("area_name"));
				int provinceId = cursor.getInt(cursor.getColumnIndex("provinceId"));
				area.setArea_id(area_id);
				area.setArea_name(area_name);
				area.setProvinceId(provinceId);
				areas.add(area);
			} 
			cursor.close();
			db.close();
			myDB.close();
		}
	}

	private void setOnClickListener() {
		// TODO Auto-generated method stub
		backward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
	}

	private void findById() {
		// TODO Auto-generated method stub
		ctnm=(TextView)findViewById(R.id.ctnm);
		area_gridlist=(NoScrollGridView)findViewById(R.id.area_gridlist);
	}
}
