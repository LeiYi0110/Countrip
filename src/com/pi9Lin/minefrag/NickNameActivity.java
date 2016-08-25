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

public class NickNameActivity extends BaseActivity {
	
	EditText change_nicknm;
	TextView tijiao;
	protected String nicknm;
	ImageView back;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_mine_changenick);
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
		
		tijiao.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				nicknm=change_nicknm.getText().toString();
				if(nicknm==null||nicknm.length()==0){
					msg("请输入正确的昵称");
					return;
				}
				AsyncHttpClient client = new AsyncHttpClient();
				PersistentCookieStore myCookieStore = new PersistentCookieStore(context);  
	            client.setCookieStore(myCookieStore);
				RequestParams params = new RequestParams();

				params.put("nickname", nicknm);
				
				String RegistPath = "http://www.xiangyouji.com.cn:3000/my/changeNickname";
				
				client.post(RegistPath,params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int arg0, String arg1) {
						/** 成功获取数据 */
						try {
							if(checkJson(arg1)){
					            //数据是使用Intent返回
				                Intent intent = new Intent();
				                //把返回数据存入Intent
				                intent.putExtra("result_nicknm", true);
				                intent.putExtra("nickname", nicknm);
				                //设置返回数据
				                setResult(1, intent);
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
		});
	}

	private void findById() {
		// TODO Auto-generated method stub
		change_nicknm=(EditText)findViewById(R.id.change_nicknm);
		tijiao=(TextView)findViewById(R.id.tijiao);
		back=(ImageView)findViewById(R.id.back);
	}
}
