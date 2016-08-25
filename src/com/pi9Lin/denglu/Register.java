package com.pi9Lin.denglu;


import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

@SuppressLint("NewApi")
public class Register extends BaseActivity {

	TextView user_contract, ljdl,get_yanma;
	EditText phone_num,yanma;
	RelativeLayout tijiao,rg_back;
	boolean isPhoneCheck=false ; //手机验证码是否通过
	protected boolean isYanmaCheck=false;
	private int effectTime=100;
	//计时器
	Timer timer;
	
	Dialog dialog;
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler(){  
		    @SuppressLint("NewApi")
			public void handleMessage(Message msg) {  
		          switch (msg.what) {      
		             case 1:      
		            	//倒计时
		            	effectTime--;
		            	System.out.println("fafafafa"+effectTime);
		            	get_yanma.setText(effectTime+"s后重新发送");
		            	if(effectTime<=0){
		            		timer.cancel();
		            		get_yanma.setText("获取验证码");
		            		get_yanma.setBackground(getImgResource(R.drawable.lvse_bg));
		            		get_yanma.setClickable(true);
		            	}
		                break;      
		             }      
		             super.handleMessage(msg);  
		         }    
	     	};
	private TextView top_title;
	private RelativeLayout dl_back; 
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_denglu_register);
		context = getApplicationContext();
		findById();
		initActionbar();
		initXML();//添加下划线
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
		top_title.setText("用户注册");
		dl_back = (RelativeLayout)v.findViewById(R.id.dl_back);
	}
	
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		tijiao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkYanMa();
			}
		});
		//获取验证码
		get_yanma.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getYanma();
			}
		});
		dl_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		user_contract.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				msg("用户协议");
			}
		});
		ljdl.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	/**
	 * 判断手机验证码
	 * */
	@SuppressLint("NewApi")
	protected void getYanma() {
		// TODO Auto-generated method stub
		String uphone = phone_num.getText().toString();
		if (uphone.length() != 11) {
			msg("手机号不合法");
			return;
		}
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);
		RequestParams params = new RequestParams();
		params.put("phone", uphone);
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/getCheckCode";
		client.post(RegistPath,params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					isPhoneCheck=phoneJson(arg1);
					if(isPhoneCheck){
						msg("获取验码成功");
						get_yanma.setBackground(getImgResource(R.drawable.hsbg));
						get_yanma.setClickable(false);
						timer();
					}else{
						msg("获取验码失败");
					}
				} catch (Exception e) {
					System.out.println("错误:"+e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "获取验码失败--网络问题", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}
	
	private void timer() {
		// 计时器
		TimerTask task = new TimerTask(){  
			  public void run() {  
			       Message message = new Message();      
			       message.what = 1;      
			       handler.sendMessage(message);    
			  }  
		 }; 
		 timer = new Timer(true);
		 timer.schedule(task,0,1200); 
	}
	
	public void checkYanMa(){
		String uphone = phone_num.getText().toString();
		String uyanma = yanma.getText().toString();
		if (uphone.length() != 11) {
			msg("手机号不合法");
			return;
		}
		if (uyanma.length() != 6) {
			msg("验证码错误");
			return;
		}
		
		dialog=dialog(this, "请稍等");
		dialog.setCancelable(true);
		dialog.show();
		
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);
		RequestParams params = new RequestParams();
		
		params.put("phone", uphone);
		params.put("checkCode", uyanma);
		
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/register/inputPhone";
		
		client.post(RegistPath,params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				Log.d("+++++", arg1);
				try {
					isYanmaCheck=checkJson(arg1);
					if(!isYanmaCheck){
						dialog.dismiss();
						msg("此号码已注册过");
					}else{
						dialog.dismiss();
						msg("注册成功");
						jump();
					}
				} catch (Exception e) {
					dialog.dismiss();
					System.out.println("错误:"+e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				dialog.dismiss();
				Toast.makeText(context, "网络验证失败", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}
	
	protected void jump() {
		// TODO Auto-generated method stub
		Intent intent=new Intent(context, Password.class);
		intent.putExtra("phone", phone_num.getText().toString());
		startActivityForResult(intent, 1);
	}

	public boolean phoneJson(String arg1)throws Exception{
		JSONObject jsonObject=new JSONObject(arg1);
		int status=jsonObject.getInt("status");
		JSONObject object=jsonObject.getJSONObject("data");
		effectTime=object.getInt("effectTime");
		System.out.println("滴答滴答滴答滴答滴答滴答滴答"+effectTime);
		if(status==1){
			return true;
		}
		return false;
	}

	/**
	 * 接收昵称页面回来的值
	 * 
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initXML() {
		// TODO Auto-generated method stub
		user_contract.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 下划线
	}

	private void findById() {
		// TODO Auto-generated method stub
		user_contract = (TextView) findViewById(R.id.user_contract);
		ljdl = (TextView) findViewById(R.id.ljdl);
		get_yanma = (TextView) findViewById(R.id.get_yanma);
		phone_num = (EditText) findViewById(R.id.phone_num);
		yanma = (EditText) findViewById(R.id.yanma);
		tijiao = (RelativeLayout) findViewById(R.id.tijiao);
	}
}
