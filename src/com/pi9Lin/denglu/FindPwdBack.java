package com.pi9Lin.denglu;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

public class FindPwdBack extends BaseActivity {
	
	EditText phone_num,pwd_one,pwd_two;
	TextView get_yanma;
	RelativeLayout eee1,fpwb_back;
	private boolean isPhoneCheck;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_findpwdback);
		context=getApplicationContext();
		findById();
		setOnClickListener();
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
		get_yanma.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkYanma();
			}
		});
		fpwb_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	protected void checkInfo() {
		// TODO Auto-generated method stub
		if(!isPhoneCheck){
			msg("请先通过手机验证");
			return;
		}
		String upass_one = pwd_one.getText().toString();
		String upass_two = pwd_two.getText().toString();
		if (!upass_one.equals(upass_two)) {
			msg("两次密码不同");
			return;
		}else{
			if(upass_one.length()==0){
				msg("密码不能为空");
				return;
			}
		}
        //数据是使用Intent返回
        Intent intent = new Intent();
        //把返回数据存入Intent
        intent.putExtra("result_fpb", true);
        //设置返回数据
        setResult(3, intent);
        //关闭Activity
        finish();
	}

	protected void checkYanma() {
		// TODO Auto-generated method stub
		String uphone = phone_num.getText().toString();
		if (uphone.length() != 11) {
			msg("手机号不合法");
			return;
		}
		AsyncHttpClient client = new AsyncHttpClient();
		RequestParams params = new RequestParams();

		params.put("phone", uphone);
		params.put("checkCode", "123");
		//有错误!!!!!!!!!!
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/changePwd";
		
		client.post(RegistPath,params, new AsyncHttpResponseHandler() {
			
			@Override
			public void onSuccess(int arg0, String arg1) {
				/** 成功获取数据 */
				try {
					msg("验证成功");
					isPhoneCheck=phoneJson(arg1);
				} catch (Exception e) {
					System.out.println("错误:"+e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				Toast.makeText(context, "验证失败", Toast.LENGTH_SHORT).show();
				super.onFailure(arg0, arg1);
			}
		});
	}

	protected boolean phoneJson(String arg1)throws Exception {
		// TODO Auto-generated method stub
		JSONObject jsonObject=new JSONObject(arg1);
		int status=jsonObject.getInt("status");
		if(status==1){
			return true;
		}
		return false;
	}

	private void findById() {
		// TODO Auto-generated method stub
		phone_num=(EditText)findViewById(R.id.phone_num);
		pwd_one=(EditText)findViewById(R.id.pwd_one);
		pwd_two=(EditText)findViewById(R.id.pwd_two);
		get_yanma=(TextView)findViewById(R.id.get_yanma);
		eee1=(RelativeLayout)findViewById(R.id.eee1);
	}
}
