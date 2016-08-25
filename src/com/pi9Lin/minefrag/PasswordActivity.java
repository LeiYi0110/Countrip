package com.pi9Lin.minefrag;

import java.security.NoSuchAlgorithmException;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.MD5.MD5;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

public class PasswordActivity extends BaseActivity {
	
	EditText old_pwd,pwd_one,pwd_two;
	TextView tijiao;
	String opwd,npwd,npwd2;
	ImageView back;
	MD5 md5;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mine_password);
		context=getApplicationContext();
		md5=new MD5();
		findById();
		setOnClickListener();
	}

	private void setOnClickListener() {
		// TODO Auto-generated method stub
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
				overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
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

	protected void checkInfo() {
		opwd=old_pwd.getText().toString();
		npwd=pwd_one.getText().toString();
		npwd2=pwd_two.getText().toString();
		if(opwd.length()==0||npwd.length()==0){
			msg("密码不能为空");
		}
		if(!npwd.equals(npwd2)){
			msg("两次输入的密码不一致");
			return;
		}
		//加密
		String up_oldpasswd=null;
		String up_newpasswd=null;
		try {
			up_oldpasswd=md5.getMD5(opwd);
			up_newpasswd=md5.getMD5(npwd);
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);  
        client.setCookieStore(myCookieStore);
		RequestParams params = new RequestParams();

		params.put("oldPwd", up_oldpasswd);
		params.put("newPwd", up_newpasswd);
		
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/changePwd";
		
		client.post(RegistPath,params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					if(checkJson(arg1)){
						msg("验证成功");
						//数据是使用Intent返回
		                Intent intent = new Intent();
		                //把返回数据存入Intent
		                intent.putExtra("result_pwd", true);
		                //设置返回数据
		                setResult(3, intent);
		                //关闭Activity
		                finish();
		                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
					}else{
						msg("原始密码错误");
					}
				} catch (Exception e) {
					System.out.println("错误:"+e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "网络出错", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}

	private void findById() {
		// TODO Auto-generated method stub
		old_pwd=(EditText)findViewById(R.id.old_pwd);
		pwd_one=(EditText)findViewById(R.id.pwd_one);
		pwd_two=(EditText)findViewById(R.id.pwd_two);
		tijiao=(TextView)findViewById(R.id.tijiao);
		back=(ImageView)findViewById(R.id.back);
	}
}
