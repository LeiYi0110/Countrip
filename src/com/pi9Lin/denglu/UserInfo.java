package com.pi9Lin.denglu;

import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.MD5.MD5;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

@SuppressLint("NewApi")
public class UserInfo extends BaseActivity {
	
	EditText input_nickname;
	RelativeLayout eee1;
	private String phone;
	private String pwd;
	private String nickName;
	private TextView top_title;
	private RelativeLayout dl_back;
	MD5 md5;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_denglu_userinfo);
		context=getApplicationContext();
		md5=new MD5();
		findById();
		initActionbar();
		init();
		setOnClickListener();
	}
	
	private void initActionbar() {
        //自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.activity_denglu_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
        v.setBackground(null);
		top_title = (TextView)v.findViewById(R.id.top_title);
		top_title.setText("完善个人信息");
		dl_back = (RelativeLayout)v.findViewById(R.id.dl_back);
	}
	
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		eee1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkInfo();
			}
		});
		dl_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	protected void checkInfo() {
		// TODO Auto-generated method stub
		nickName=input_nickname.getText().toString();
		if(nickName==null||nickName.length()==0){
			msg("昵称为空");
			return;
		}
		String up_passwd=null;
		try {
			up_passwd=md5.getMD5(pwd);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);
		RequestParams params = new RequestParams();
		params.put("phone", phone);
		params.put("nickname", nickName);
		params.put("pwd", up_passwd);
		
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/register";
		
		client.post(RegistPath,params,new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					if(registerJson(arg1)){
						Editor editor = preferences.edit(); 
						editor.putString("nickname", nickName);
						editor.putString("username", phone);
						editor.putString("passwd", pwd);
						editor.commit();
						//数据是使用Intent返回
		                Intent intent = new Intent();
		                //把返回数据存入Intent
		                intent.putExtra("result", true);
		                //设置返回数据
		                setResult(2, intent);
		                //关闭Activity
		                finish();
					}
				} catch (Exception e) {
					System.out.println("错误:"+e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "昵称注册失败", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}

	protected boolean registerJson(String arg1) throws Exception{
		JSONObject jsonObject=new JSONObject(arg1);
		int status=jsonObject.getInt("status");
		if(status==1){
			return true;
		}
		return false;
	}

	private void findById() {
		// TODO Auto-generated method stub
		input_nickname=(EditText)findViewById(R.id.input_nickname);
		eee1=(RelativeLayout)findViewById(R.id.eee1);
	}

	private void init() {
		// TODO Auto-generated method stub
		Intent intent=getIntent();
		phone=intent.getStringExtra("phone");
		pwd=intent.getStringExtra("pwd");
	}
}
