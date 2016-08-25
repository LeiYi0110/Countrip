package com.pi9Lin.denglu;

import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.pi9Lin.data.UserInfos;
import com.pi9Lin.utils.ACache;
import com.pi9Lin.utils.QQUtils;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQAuth;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

@SuppressLint("NewApi")
public class DengLuActivity extends BaseActivity {
	
	EditText txt_mobile,txt_passwd;
	TextView forget_passwd,zhuce,denglu,top_title;
	ImageView qq_denglu;
	public static String mAppid;
//	private TextView mUserInfo;
//	private ImageView mUserLogo;
	public static QQAuth mQQAuth;
	private UserInfo mInfo;
	private Tencent mTencent;
	private final String APP_ID = "1104658473";// 测试时使用，真正发布的时候要换成自己的APP_ID
//	private final String APP_ID = "222222";
	private ACache mCache;
	RelativeLayout dl_back;
	MD5 md5;
	
	private UserInfos infos;
	
	/**测试*/
	ImageView qq_haha;
	
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_denglu);
		context=getApplicationContext();
		mCache = ACache.get(this);
		md5=new MD5();
		findById();
		initActionbar();
		setOnClickListener();
		initViews();/**qq第三方登录*/
	}
	
	private void initActionbar() {
		// TODO Auto-generated method stub
        //自定义ActionBar  
        final ActionBar actionBar = getActionBar();  
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);  
        actionBar.setCustomView(R.layout.activity_denglu_top);//自定义ActionBar布局  
        View v=actionBar.getCustomView();
        v.setBackground(null);
		top_title = (TextView)v.findViewById(R.id.top_title);
		top_title.setText("登录");
		dl_back = (RelativeLayout)v.findViewById(R.id.dl_back);
	}
	
	private void setOnClickListener() {
		/**返回*/
		dl_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		/**找回密码*/ 
		forget_passwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(context, FindPwdBack.class),2);
			}
		});
		/**马上注册*/
		zhuce.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivityForResult(new Intent(context, Register.class),1);
			}
		});
		/**立即登录*/
		denglu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final String phone=txt_mobile.getText().toString();
				final String passwd=txt_passwd.getText().toString();
				if(phone.length()==0||passwd.length()==0){
					msg("用户名或密码为空");
					return;
				}
				String up_passwd=null;
				try {
					up_passwd=md5.getMD5(passwd);
				} catch (NoSuchAlgorithmException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AsyncHttpClient client = new AsyncHttpClient();
				PersistentCookieStore myCookieStore = new PersistentCookieStore(context);  
	            client.setCookieStore(myCookieStore); 
				RequestParams params = new RequestParams();

				params.put("phone", phone);
				params.put("pwd", up_passwd);
				
				String RegistPath = "http://www.xiangyouji.com.cn:3000/my/login";
				
				client.post(RegistPath,params, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int arg0, String arg1) {
						/** 成功获取数据 */
						try {
//							Log.d("登录返回", arg1);
							if(loginJson(arg1)){
								jieXi(arg1);
								preferences.edit().putBoolean("isLandIn", true).commit();
								preferences.edit().putString("username", infos.getPhone()).commit();
								preferences.edit().putString("passwd", passwd).commit();
								preferences.edit().putString("nickname", infos.getNickname()).commit();
								preferences.edit().putString("headerImage", infos.getHeaderImage()).commit();
								msg("正在登录中...");
								initSave();
							}
						} catch (Exception e) {
							System.out.println("错误:"+e.getMessage());
						}
						super.onSuccess(arg0, arg1);
					}

					@Override
					public void onFailure(Throwable arg0, String arg1) {
						Toast.makeText(context, "用户名或密码不正确", Toast.LENGTH_SHORT).show();
						super.onFailure(arg0, arg1);
					}
				});
			}
		});

	}
	
	private void jieXi(String arg1) throws Exception{
		// TODO Auto-generated method stub
		infos=new UserInfos();
		JSONObject jsonObject=new JSONObject(arg1);
		JSONObject data=jsonObject.getJSONObject("data");
		String _id=data.getString("_id");
		String phone=data.getString("phone");
		String nickname=data.getString("nickname");
		String pwd=data.getString("pwd");
		String headerImage=data.getString("headerImage");
		String email=data.getString("email");
		String address=data.getString("address");
		infos.set_id(_id);
		infos.setPhone(phone);
		infos.setNickname(nickname);
		infos.setPwd(pwd);
		infos.setHeaderImage(headerImage);
		infos.setEmail(email);
		infos.setAddress(address);
	}
	
	/**
	 * 初始化本地收藏
	 * */
	private void initSave() {
		// 2.获取收藏的全部id和type
		AsyncHttpClient client = new AsyncHttpClient();
		PersistentCookieStore myCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(myCookieStore);
		String url = "http://www.xiangyouji.com.cn:3000/my/getAllCollectionItems";
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, String arg1) {
				try {
					if (loginJson(arg1)) {
						// 保存收藏信息到本地
						mCache.put("allSave", arg1);
						//结束页面
		                Intent intent = new Intent();
		                //把返回数据存入Intent
		                setResult(RESULT_OK, intent);
		                //关闭Activity
		                finish();
					}
				} catch (Exception e) {
					Log.d("保存收藏", e.getMessage());
				}
				super.onSuccess(arg0, arg1);
			}
			@Override
			public void onFailure(Throwable arg0, String arg1) {
				super.onFailure(arg0, arg1);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		if(arg1==2){
			if(arg2.getBooleanExtra("result", false)){
				msg("注册成功");
				//成功之后跳到相应页面
			}else{
				msg("注册失败");
			}
		}else if(arg1==3){
			if(arg2.getBooleanExtra("result_fpb", false)){
				msg("修改成功");
			}
		}
		super.onActivityResult(arg0, arg1, arg2);
	}
	
	private void findById() {
		// TODO Auto-generated method stub
		txt_mobile=(EditText)findViewById(R.id.txt_mobile);
		txt_passwd=(EditText)findViewById(R.id.txt_passwd);
		forget_passwd=(TextView)findViewById(R.id.forget_passwd);
		zhuce=(TextView)findViewById(R.id.zhuce);
		denglu=(TextView)findViewById(R.id.denglu);
		qq_denglu=(ImageView)findViewById(R.id.qq_denglu);
		qq_haha=(ImageView)findViewById(R.id.qq_haha);
		dl_back=(RelativeLayout)findViewById(R.id.dl_back);
	}

	/**
	 * QQ第三方登录代码
	 * */
	@Override
	protected void onStart() {
		final Context context = DengLuActivity.this;
		final Context ctxContext = context.getApplicationContext();
		mAppid = APP_ID;
		mQQAuth = QQAuth.createInstance(mAppid, ctxContext);
		mTencent = Tencent.createInstance(mAppid, context);
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	protected void initViews(){
		OnClickListener listener = new NewClickListener();
		/**qq第三方登录*/
		qq_denglu.setOnClickListener(listener);
	}
	
	class NewClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Context context = v.getContext();
			Register register=new Register();
			switch (v.getId()) {
			case R.id.qq_denglu:
				onClickLogin();
				return;
			}
			if (register != null) {
				Intent intent = new Intent(context, register.getClass());
				context.startActivity(intent);
			}
		}
	}
	
	private void onClickLogin() {
		if (!mQQAuth.isSessionValid()) {
			IUiListener listener = new BaseUiListener() {
				@Override
				protected void doComplete(JSONObject values) {
					updateUserInfo();
				}
			};
			mQQAuth.login(this, "all", listener);
			// mTencent.loginWithOEM(this, "all",
			// listener,"10000144","10000144","xxxx");
			mTencent.login(this, "all", listener);
		} else {
			mQQAuth.logout(this);
			updateUserInfo();
		}
	}
	
	private class BaseUiListener implements IUiListener {
		
		/**
		 * 返回id等登录信息
		 * */
		@Override
		public void onComplete(Object response) {
//			QQUtils.showResultDialog(DengLuActivity.this, response.toString(),
//					"登录成功");
//			"openid": "768ED84C9B7A6B424ED3D15D5E2D74C8",
//			"access_token": "5B1BBEE7E6732A39B1310A295616845E",
//			"pay_token": "1C4C7DAE8C92FF68E2A01F7707B6CA7F",
//			preferences.edit().putString("username", "").commit();
//			preferences.edit().putString("passwd", passwd).commit();
			
			JSONObject object=(JSONObject)response;
//			Log.d("哈哈", object.toString());
			
			doComplete((JSONObject) response);
		}
		/**
		 * 返回个人信息
		 * */
		protected void doComplete(JSONObject values) {

		}

		@Override
		public void onError(UiError e) {
			QQUtils.toastMessage(DengLuActivity.this, "onError: " + e.errorDetail);
			QQUtils.dismissDialog();
		}

		@Override
		public void onCancel() {
			QQUtils.toastMessage(DengLuActivity.this, "onCancel: ");
			QQUtils.dismissDialog();
		}
	}
	
	private void updateUserInfo() {
		if (mQQAuth != null && mQQAuth.isSessionValid()) {
			IUiListener listener = new IUiListener() {

				@Override
				public void onError(UiError e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete(final Object response) {
					try {
						String nknm=((JSONObject) response).getString("nickname");
						Log.d("哈哈", nknm);
						preferences.edit().putBoolean("isLandIn", true).commit();
						preferences.edit().putString("nickname", nknm).commit();
						preferences.edit().putString("headerImage", ((JSONObject) response).getString("figureurl_qq_2")).commit();
						msg("正在登录中...");
						
						initSave();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//					Message msg = new Message();
//					msg.obj = response;
//					msg.what = 0;
//					mHandler.sendMessage(msg);
//					new Thread() {
//
//						@Override
//						public void run() {
//							JSONObject json = (JSONObject) response;
//							if (json.has("figureurl")) {
//								Bitmap bitmap = null;
//								try {
//									bitmap = QQUtils.getbitmap(json
//											.getString("figureurl_qq_2"));
//								} catch (JSONException e) {
//
//								}
//								Message msg = new Message();
//								msg.obj = bitmap;
//								msg.what = 1;
//								mHandler.sendMessage(msg);
//							}
//						}
//
//					}.start();
				}

				@Override
				public void onCancel() {
				}
			};
			mInfo = new UserInfo(this, mQQAuth.getQQToken());
			mInfo.getUserInfo(listener);

		} else {
//			mUserInfo.setText("");
//			mUserInfo.setVisibility(android.view.View.GONE);
//			mUserLogo.setVisibility(android.view.View.GONE);
			msg("取消登录");
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				JSONObject response = (JSONObject) msg.obj;
				if (response.has("nickname")) {
					try {
//						mUserInfo.setVisibility(android.view.View.VISIBLE);
//						mUserInfo.setText(response.getString("nickname"));
						msg(response.getString("nickname"));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else if (msg.what == 1) {
				Bitmap bitmap = (Bitmap) msg.obj;
//				mUserLogo.setImageBitmap(bitmap);
//				mUserLogo.setVisibility(android.view.View.VISIBLE);
				qq_haha.setImageBitmap(bitmap);
			}
		}

	};
	
	
}
