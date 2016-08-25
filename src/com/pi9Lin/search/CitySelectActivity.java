package com.pi9Lin.search;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.bins.VerticalScrollView;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.City;
import com.pi9Lin.data.CityAndArea;
import com.pi9Lin.data.ProvinceInfo;
import com.pi9Lin.utils.MesureHightUtils;

public class CitySelectActivity extends BaseActivity {
	
	TextView gps_location;
	RelativeLayout huadong,huanan,huabei,xinan,qita;
	RelativeLayout backward,search_btn;
	ListView area_list;
	List<ProvinceInfo> list;
	String region;
	View footView;
	LinearLayout hehe;
	VerticalScrollView myvs;
	
	/**测试数据*/
	String cityName="广州";
	String[] area={"广州","深圳","佛山","东莞","惠州","珠海","广州2","深圳2","佛山2","东莞2"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		findById();
		initActionbar();
		setOnClickListener();
		init();
		area_list.addFooterView(footView);
		setListView();
	}
	
	private void initActionbar() {
		//自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.layout_search_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
        backward = (RelativeLayout)v.findViewById(R.id.backward);
        search_btn = (RelativeLayout)v.findViewById(R.id.search_btn);
	}

	private void setListView() {
		/**
		 * 根据区域填充列表数据
		 * */
		List<ProvinceInfo> infos=new ArrayList<ProvinceInfo>();
		for (ProvinceInfo p : list) {
			if(p.getRegion().equals(region)){
				infos.add(p);
			}
		}
		area_list.setAdapter(new MyApr(infos));
		MesureHightUtils.setListViewHeightBasedOnChildren(area_list);
		/** 设置listView包裹的外部布局高度 */
		int dd = MesureHightUtils.setListViewHeightBasedOnChildren1(area_list);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, dd);// 设置最后一项分隔线消失
		hehe.setLayoutParams(lp);
		area_list.setFocusable(false);
		myvs.smoothScrollTo(0, 0);
	}

	private void init() {
		Intent intent=getIntent();
		String locate=intent.getStringExtra("locate");
		gps_location.setText(locate);
		list=getData(getProvinceInfos(), getCityInfos(), getAreaInfos());
		region="southwest";
		//页面留白
		footView=LayoutInflater.from(context).inflate(R.layout.layout_search_footerview, null);
	}
	
	/**
	 * listview适配器
	 * */
	private class MyApr extends BaseAdapter{
		List<ProvinceInfo> regionList;
		MyApr(List<ProvinceInfo> regionList){
			this.regionList=regionList;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return regionList.size();
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
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v=null;
			ViewHolder viewHolder;
			if(arg1==null){
				v=LayoutInflater.from(context).inflate(R.layout.item_search_listitem, null);
				viewHolder=new ViewHolder();
				viewHolder.provinceName=(TextView)v.findViewById(R.id.provinceName);
				viewHolder.area_gridlist=(GridView)v.findViewById(R.id.area_gridlist);
				v.setTag(viewHolder);
			}else{
				v=arg1;
				viewHolder=(ViewHolder)v.getTag();
			}
			viewHolder.provinceName.setText(regionList.get(arg0).getProvinceName());
			//在这里区分 isShow=0，1
			List<City> aa = regionList.get(arg0).getCity();
			List<CityAndArea> cityAndAreas=new ArrayList<CityAndArea>();
			for (City city : aa) {
				
				if(city.getIsShow()==0){
					//直接显示区域  城市名不显示
					for (int i = 0; i < city.getArea().size(); i++) {
						CityAndArea cityAndArea=new CityAndArea();
						cityAndArea.setArea(city.getArea().get(i));
						cityAndAreas.add(cityAndArea);
					}
				}else if(city.getIsShow()==1){
					CityAndArea cityAndArea=new CityAndArea();
					cityAndArea.setCity(city);
					cityAndArea.setCity(true);
					cityAndAreas.add(cityAndArea);
				}
			}
			viewHolder.area_gridlist.setAdapter(new MyGrid(cityAndAreas));
			return v;
		}
		
		class ViewHolder{
			TextView provinceName;
			GridView area_gridlist;
		}
	}
	
	private class MyGrid extends BaseAdapter{
		List<CityAndArea> cities;
		MyGrid(List<CityAndArea> cities){
			this.cities=cities;
		} 
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return cities.size();
		}
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			View v;
			ViewHolder viewHolder;
			if(arg1==null){
				v=LayoutInflater.from(context).inflate(R.layout.item_search_gridlist, null);
				viewHolder=new ViewHolder();
				viewHolder.cityName =(TextView)v.findViewById(R.id.cityName);
				v.setTag(viewHolder);
			}else{
				v=arg1;
				viewHolder=(ViewHolder)v.getTag();
			}
			final int aa=arg0;
			if(cities.get(arg0).isCity()){
				viewHolder.cityName.setText(cities.get(arg0).getCity().getCityName());
				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//数据是使用Intent返回
						Intent intent = new Intent(context,AreaSelectActivity.class);
						//把返回数据存入Intent
						intent.putExtra("cityId", cities.get(aa).getCity().getCityId());
						intent.putExtra("cityName", cities.get(aa).getCity().getCityName());
						startActivityForResult(intent, 1);
						overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
					}
				});
			}else{
				viewHolder.cityName.setText(cities.get(arg0).getArea().getArea_name());
				v.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//数据是使用Intent返回
		                Intent intent = new Intent();
		                //把返回数据存入Intent
		                intent.putExtra("area_id", cities.get(aa).getArea().getArea_id());
		                intent.putExtra("area_name", cities.get(aa).getArea().getArea_name());
		                intent.putExtra("province", cities.get(aa).getArea().getProvinceId());
		                //设置返回数据
		                setResult(RESULT_OK, intent);
		                //关闭Activity
		                finish();
					}
				});
			}
			return v;
		}
		
		class ViewHolder{
			TextView cityName;
		}
	}
	
	public void myClick(View v){
		huadong.setBackgroundResource(R.drawable.huadong);
		huanan.setBackgroundColor(getResources().getColor(R.color.white));
		huabei.setBackgroundColor(getResources().getColor(R.color.white));
		xinan.setBackgroundColor(getResources().getColor(R.color.white));
		qita.setBackgroundResource(R.drawable.qita);
		
		if(v.getId()==R.id.huabei){
			huabei.setBackgroundColor(getResources().getColor(R.color.e3e2e2));
			region="north";
		}else if (v.getId()==R.id.huadong) {
			huadong.setBackgroundResource(R.drawable.huadongf);
			region="east";
		}else if (v.getId()==R.id.huanan) {
			huanan.setBackgroundColor(getResources().getColor(R.color.e3e2e2));
			region="south";
		}else if (v.getId()==R.id.xinan) {
			xinan.setBackgroundColor(getResources().getColor(R.color.e3e2e2));
			region="southwest";
		}else if (v.getId()==R.id.qita) {
			qita.setBackgroundResource(R.drawable.qitaf);
			region="other";
		}
		setListView();
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
		search_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(context, LocateSearchActivity.class),2);
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
			}
		});
	}

	private void findById() {
		// TODO Auto-generated method stub
		gps_location=(TextView)findViewById(R.id.gps_location);
		huadong=(RelativeLayout)findViewById(R.id.huadong);
		huanan=(RelativeLayout)findViewById(R.id.huanan);
		huabei=(RelativeLayout)findViewById(R.id.huabei);
		xinan=(RelativeLayout)findViewById(R.id.xinan);
		qita=(RelativeLayout)findViewById(R.id.qita);
		area_list=(ListView)findViewById(R.id.area_list);
		hehe=(LinearLayout)findViewById(R.id.hehe);
		myvs=(VerticalScrollView)findViewById(R.id.myvs);
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		if((arg0==1||arg0==2)&&arg1==RESULT_OK){
			//获得城市名
			int area_id=arg2.getIntExtra("area_id",-1);
			int province=arg2.getIntExtra("province",-1);
			String area_name=arg2.getStringExtra("area_name");
			if(province>0&&area_id>0){
				//数据是使用Intent返回
				Intent intent = new Intent();
				//把返回数据存入Intent
				intent.putExtra("area_id", area_id);
				intent.putExtra("province", province);
				intent.putExtra("area_name", area_name);
				//设置返回数据
				setResult(RESULT_OK, intent);
			}
			//关闭Activity
			finish();
		}
	}
	
}
