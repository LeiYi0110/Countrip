package com.pi9Lin.denglu;


import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

public class Password extends BaseActivity {
	
	EditText passwd_one,passwd_two;
	RelativeLayout tijiao,rg_back;
	private String phone;
	private TextView top_title;
	private RelativeLayout top_back;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_denglu_passwd);
		context=getApplicationContext();
		findById();
		initActionbar();
		init();
		setOnClickListener();
	}
	
	private void initActionbar() {
		// TODO Auto-generated method stub
        //自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.activity_denglu_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
		top_title = (TextView)v.findViewById(R.id.top_title);
		top_title.setText("密码");
		top_back = (RelativeLayout)v.findViewById(R.id.dl_back);
	}
	
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		top_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		tijiao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkInfo();
			}
		});
	}
	
	private void checkInfo() {
		String upass_one = passwd_one.getText().toString();
		String upass_two = passwd_two.getText().toString();
		if (!upass_one.equals(upass_two)) {
			msg("两次密码不同");
			return;
		}else{
			if(upass_one.length()==0){
				msg("密码不能为空");
				return;
			}
		}
		Intent intent=new Intent(context, UserInfo.class);
		intent.putExtra("phone", phone);
		intent.putExtra("pwd", upass_one);
		startActivityForResult(intent, 1);
	}
	
	private void init() {
		// TODO Auto-generated method stub
		Intent intent=getIntent();
		phone=intent.getStringExtra("phone");
	}

	private void findById() {
		// TODO Auto-generated method stub
		passwd_one=(EditText)findViewById(R.id.passwd_one);
		passwd_two=(EditText)findViewById(R.id.passwd_two);
		tijiao=(RelativeLayout)findViewById(R.id.tijiao);
	}
	
	@Override
	protected void onActivityResult(int arg0, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode==2){
			//数据是使用Intent返回
			Intent intent = new Intent();
			if(data.getBooleanExtra("result", false)){
                //把返回数据存入Intent
                intent.putExtra("result", true);
			}
			//设置返回数据
			setResult(2, intent);
			//关闭Activity
			finish();
		}
		super.onActivityResult(arg0, resultCode, data);
	}
	
}
