package com.pi9Lin.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.countrip.R;
import com.pi9Lin.denglu.DengLuActivity;
import com.pi9Lin.fragment.IndexFragf;
import com.pi9Lin.fragment.IndexFragment;
import com.pi9Lin.fragment.MineFrag;
import com.pi9Lin.fragment.RoundFrag;
import com.pi9Lin.fragment.SaveFrag;
import com.pi9Lin.fragment.UnLandIn;
import com.pi9Lin.imageLoader.FileUtils;

public class IndexActivity extends BaseActivity implements OnClickListener {
	
	/**初始化 fragment*/
    IndexFragf indexFragf;
	RoundFrag roundFrag;
	MineFrag mineFrag;
	SaveFrag saveFrag;
	UnLandIn unLandIn;
	IndexFragment indexFragment;
	
	/**fragment 管理对象*/
	FragmentManager fm;
	
	/**控件资源*/
	LinearLayout to_home,to_round,to_mine,to_save;
	ImageView home_img,round_img,mine_img,save_img;
	TextView home_txt,round_txt,mine_txt,save_txt;
	
	private FileUtils fileUtils;  //缓存图片文件
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_index);
		findById();
		setOnClickListener();
		fileUtils = new FileUtils(this);
		fm=getSupportFragmentManager(); 
		// 第一次启动时选中第0个tab  
		setTabSelection(0);
	}
	
    @Override  
    public void onClick(View v) {  
        switch (v.getId()) {
        case R.id.to_home:  
            // 当点击了消息tab时，选中第1个tab  
            setTabSelection(0);  
            break;  
        case R.id.to_round:  
            // 当点击了联系人tab时，选中第2个tab  
            setTabSelection(1);  
            break;  
        case R.id.to_save:  
            // 当点击了动态tab时，选中第3个tab  
            setTabSelection(2);  
            break;  
        case R.id.to_mine:  
            // 当点击了设置tab时，选中第4个tab  
            setTabSelection(3);  
            break;  
        default:  
            break;  
        }  
    }  
    
	private void setOnClickListener() {
		// TODO Auto-generated method stub
		to_home.setOnClickListener(this);
		to_round.setOnClickListener(this);
		to_mine.setOnClickListener(this);
		to_save.setOnClickListener(this);
	}

	private void findById() {
		// TODO Auto-generated method stub
		to_home = (LinearLayout)findViewById(R.id.to_home);
		to_round = (LinearLayout)findViewById(R.id.to_round);
		to_mine = (LinearLayout)findViewById(R.id.to_mine);
		to_save = (LinearLayout)findViewById(R.id.to_save);
		home_img = (ImageView)findViewById(R.id.home_img);
		round_img = (ImageView)findViewById(R.id.round_img);
		mine_img = (ImageView)findViewById(R.id.mine_img);
		save_img = (ImageView)findViewById(R.id.save_img);
		home_txt = (TextView)findViewById(R.id.home_txt);
		round_txt = (TextView)findViewById(R.id.round_txt);
		mine_txt = (TextView)findViewById(R.id.mine_txt);
		save_txt = (TextView)findViewById(R.id.save_txt);
	}
	
    /** 
     * 根据传入的index参数来设置选中的tab页。 
     *  
     * @param index 
     *            每个tab页对应的下标。0表示首页，1表示附近，2表示收藏，3表示我的。 
     */  
    public void setTabSelection(int index) {  
        // 每次选中之前先清楚掉上次的选中状态  
        clearSelection();  
        // 开启一个Fragment事务  
        FragmentTransaction transaction = fm.beginTransaction();  
        // 先隐藏掉所有的Fragment，以防止有多个Fragment显示在界面上的情况  
        hideFragments(transaction);  
        switch (index) {  
        case 0:  
            // 当点击了消息tab时，改变控件的图片和文字颜色  
        	home_img.setImageResource(R.drawable.index);  
        	home_txt.setTextColor(getResources().getColor(R.color.press));
        	//设置顶部可见
            if (indexFragment == null) {  
                // 如果indexFrag为空，则创建一个并添加到界面上  
            	indexFragment = new IndexFragment();  
                transaction.add(R.id.content, indexFragment);
//                indexFragf = new IndexFragf();  
//                transaction.add(R.id.content, indexFragf);
            } else {  
                // 如果indexFrag不为空，则直接将它显示出来  
                transaction.show(indexFragment);  
            }  
            break;  
        case 1:  
            // 当点击了联系人tab时，改变控件的图片和文字颜色  
        	round_img.setImageResource(R.drawable.round);  
        	round_txt.setTextColor(getResources().getColor(R.color.press));
        	//设置顶部不可见
            if (roundFrag == null) {  
                // 如果roundFrag为空，则创建一个并添加到界面上  
            	roundFrag = new RoundFrag();  
                transaction.add(R.id.content, roundFrag);  
            } else {  
                // 如果roundFrag不为空，则直接将它显示出来  
                transaction.show(roundFrag);  
            }  
            break;  
        case 2:  
        	//先判断是否登陆了
        	boolean isLandIn=preferences.getBoolean("isLandIn", false);
        	if(!isLandIn){
        		Intent intent=new Intent(context, DengLuActivity.class);
        		startActivityForResult(intent, 1);
        	}else{
        		// 当点击了动态tab时，改变控件的图片和文字颜色  
        		save_img.setImageResource(R.drawable.save_img);  
        		save_txt.setTextColor(getResources().getColor(R.color.press));
        		/**每次都刷新收藏页*/
    			saveFrag = new SaveFrag();  
    			transaction.add(R.id.content, saveFrag);
        	}
            break;  
        case 3:  
        	//先判断是否登陆了
        	boolean isLandInf=preferences.getBoolean("isLandIn", false);
        	if(!isLandInf){
        		Intent intent=new Intent(context, DengLuActivity.class);
        		startActivityForResult(intent, 4);
        	}else{
        		// 当点击了设置tab时，改变控件的图片和文字颜色  
        		mine_img.setImageResource(R.drawable.mine); 
        		mine_txt.setTextColor(getResources().getColor(R.color.press));
        		//设置顶部不可见
        		if (mineFrag == null) {  
        			// 如果mineFrag为空，则创建一个并添加到界面上  
        			mineFrag = new MineFrag();  
        			transaction.add(R.id.content, mineFrag);  
        		} else {  
        			// 如果mineFrag不为空，则直接将它显示出来  
        			transaction.show(mineFrag);  
        		}
        	}
            break;
        // 进入收藏未登录情况
        case 4:
        	save_img.setImageResource(R.drawable.save_img);  
    		save_txt.setTextColor(getResources().getColor(R.color.press));
        	/**添加一个未登录页面*/
    		if (unLandIn == null) {  
        		unLandIn = new UnLandIn();  
                transaction.add(R.id.content, unLandIn);
            } else {  
                transaction.show(unLandIn);  
            }  
            break;
        // 进入我的未登录情况
        case 5:
        default:
        	if(mineFrag!=null){
        		transaction.remove(mineFrag);//把原来的我的页面删掉  因为可能重新登录
        		mineFrag=null;
        	}
        	mine_img.setImageResource(R.drawable.mine); 
    		mine_txt.setTextColor(getResources().getColor(R.color.press));
        	if (unLandIn == null) {  
        		unLandIn = new UnLandIn();  
                transaction.add(R.id.content, unLandIn);
            } else {  
                transaction.show(unLandIn);  
            }  
            break;
        }  
        transaction.commit();  
    }  
    
    /** 
     * 清除掉所有的选中状态
     */  
    private void clearSelection() {  
    	home_img.setImageResource(R.drawable.indexf);
		round_img.setImageResource(R.drawable.roundf);
		save_img.setImageResource(R.drawable.savef_img); 
		mine_img.setImageResource(R.drawable.minef);
		home_txt.setTextColor(getResources().getColor(R.color.unpress));
		round_txt.setTextColor(getResources().getColor(R.color.unpress));
		save_txt.setTextColor(getResources().getColor(R.color.unpress));
		mine_txt.setTextColor(getResources().getColor(R.color.unpress));
    }  
    
    /** 
     * 将所有的Fragment都置为隐藏状态。 
     *  
     * @param transaction 
     *            用于对Fragment执行操作的事务 
     */  
    private void hideFragments(FragmentTransaction transaction) {  
        if (indexFragment != null) {  
            transaction.hide(indexFragment);  
        }  
        if (roundFrag != null) {  
            transaction.hide(roundFrag);  
        }  
        if (saveFrag != null) {  
            transaction.hide(saveFrag);  
        }  
        if (mineFrag != null) {  
            transaction.hide(mineFrag);  
        }
        if (unLandIn != null) {  
            transaction.hide(unLandIn);  
        } 
    }  
    
    @SuppressLint("Recycle")
	@Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(arg0, arg1, arg2);
    	if(arg0==1){
    		//收藏  登录成功
    		if(arg1==RESULT_OK){
        		//设置顶部不可见
    			setTabSelection(2);
    		}else{
    			setTabSelection(4);
    		}
    	}
    	if(arg0==4){
    		//我的  登录成功
    		if(arg1==RESULT_OK){
    			setTabSelection(3);
    		}else{
    			setTabSelection(5);
    		}
    	}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	preferences.edit().putBoolean("isLandIn", false).commit();
    	preferences.edit().putString("district", null).commit();
    	preferences.edit().putString("geoLng", null).commit();
    	preferences.edit().putString("geoLat", null).commit();
    	fileUtils.deleteFile();
    	imageLoader.stop();
    }
    
    @Override
	public void onBackPressed() {
		imageLoader.stop();		// 停止加载图片
		super.onBackPressed();
	}

}
