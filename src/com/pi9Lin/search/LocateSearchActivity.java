package com.pi9Lin.search;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.data.City;
import com.pi9Lin.data.ProvinceInfo;

public class LocateSearchActivity extends BaseActivity {
	
	RelativeLayout layout_back;
	ListView lishi_sousuo;
	EditText keywd;
	private List<City> list;
	private List<ProvinceInfo> infos;
	TextView kong;
	private ImageView txt_delete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locatesearch);
		findById();
		initActionbar();
		init();
		setOnClickListener();
	}

	private void initActionbar() {
		//自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.activity_locate_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
		layout_back = (RelativeLayout)v.findViewById(R.id.layout_back);
		keywd = (EditText)v.findViewById(R.id.keywd);
		txt_delete=(ImageView)v.findViewById(R.id.txt_delete);
	}

	private void setOnClickListener() {
		// TODO Auto-generated method stub
		txt_delete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				keywd.setText("");
			}
		});
		keywd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				/**当输入关键字发生改变时 实时改变listview的内容*/
				List<City> cityInfosf = new ArrayList<City>();
				kong.setVisibility(View.GONE);
				for (int i = 0; i < list.size(); i++) {
					boolean flag = false;
					String citynm = list.get(i).getCityName();
					if (citynm.indexOf(arg0 + "") > -1) {
						flag = true;
					}
					if (flag) {
						cityInfosf.add(list.get(i));
					}
				}
				if (arg0.length()==0) {
					//输入为空 
					lishi_sousuo.setAdapter(null);
				} else {
					if (cityInfosf.size() == 0) {
						//搜索无结果
						kong.setVisibility(View.VISIBLE);
					} else {
						//搜到了
						lishi_sousuo.setAdapter(new MyApr(cityInfosf,infos));
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
			}
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
			}
		});
		layout_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
			}
		});
	}
	
	private class MyApr extends BaseAdapter {

		List<City> list;
		List<ProvinceInfo> infos;

		public MyApr(List<City> list,List<ProvinceInfo> infos) {
			super();
			this.list = list;
			this.infos = infos;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			View v=null;
			ViewHolder viewHolder;
			if(arg1==null){
				v=LayoutInflater.from(context).inflate(R.layout.item_seach_listitem, null);
				viewHolder=new ViewHolder();
				viewHolder.tv=(TextView)v.findViewById(R.id.txt_nm);
				viewHolder.pro=(TextView)v.findViewById(R.id.txt_pro);
				v.setTag(viewHolder);
			}else{
				v=arg1;
				viewHolder=(ViewHolder)v.getTag();
			}
			final int aa=arg0;
			viewHolder.tv.setText(list.get(arg0).getCityName());
			for (int i = 0; i < infos.size(); i++) {
				if(infos.get(i).getProvinceId()==list.get(arg0).getProvinceId()){
					viewHolder.pro.setText(infos.get(i).getProvinceName());
				}
			}
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//数据是使用Intent返回
	                Intent intent = new Intent(context,AreaSelectActivity.class);
	                //把返回数据存入Intent
	                intent.putExtra("cityId", list.get(aa).getCityId());
	                intent.putExtra("cityName", list.get(aa).getCityName());
	                startActivityForResult(intent, 1);
	                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
				}
			});
			return v;
		}
		class ViewHolder{
			TextView tv;
			TextView pro;
		}
	}
	
	private void init() {
		list=getCityInfos();
		infos=getProvinceInfos();
	}

	private void findById() {
		lishi_sousuo=(ListView)findViewById(R.id.lishi_sousuo); 
		kong=(TextView)findViewById(R.id.kong); 
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
		if(arg0==1&&arg1==RESULT_OK){
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
