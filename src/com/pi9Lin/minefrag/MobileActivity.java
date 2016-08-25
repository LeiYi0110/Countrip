package com.pi9Lin.minefrag;

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
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;

public class MobileActivity extends BaseActivity {
	
	EditText band_phone,input_yanma;
	TextView yanma;
	private String bphone;
	private ImageView back;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mine_mobile);
		context=getApplicationContext();
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
		
		yanma.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				checkYanma();
			}
		});
	}

	protected void checkYanma() {
		// TODO Auto-generated method stub
		bphone = band_phone.getText().toString();
		if (bphone.length() != 11) {
			msg("手机号不合法");
			return;
		}
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);  
        client.setCookieStore(myCookieStore);
		RequestParams params = new RequestParams();

		params.put("phone", bphone);
		
		String RegistPath = "http://www.xiangyouji.com.cn:3000/my/changePhone";
		
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
		                intent.putExtra("result_phone", true);
		                intent.putExtra("phone", bphone);
		                //设置返回数据
		                setResult(2, intent);
		                //关闭Activity
		                finish();
		                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
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
		band_phone=(EditText)findViewById(R.id.band_phone);
		input_yanma=(EditText)findViewById(R.id.input_yanma);
		yanma=(TextView)findViewById(R.id.yanma);
		back=(ImageView)findViewById(R.id.back);
	}
}
