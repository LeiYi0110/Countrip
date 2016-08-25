package com.pi9Lin.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.pi9Lin.adapter.MyFragmentPagerAdapter;
import com.pi9Lin.base.BaseActivity;
import com.pi9Lin.commentfrag.AllCommentFrag;
import com.pi9Lin.commentfrag.BadCommentFrag;
import com.pi9Lin.commentfrag.GoodCommentFrag;
import com.pi9Lin.countrip.R;

public class ShowCommentActivity extends BaseActivity {
	
	private TextView view1, view2, view3,barText;
	private ViewPager mPager;
	private ArrayList<Fragment> fragmentList;
	private int currIndex;//当前页卡编号 
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_showcomment);
		initTextView();
		initTextBar();  
		initViewPager(); 
	}
	
	/** 
     * 初始化ViewPager 
     */
	private void initViewPager() {
		mPager = (ViewPager)findViewById(R.id.viewpager);  
        fragmentList = new ArrayList<Fragment>();  
        Fragment allCommentFrag= new AllCommentFrag();  
        Fragment goodCommentFrag = new GoodCommentFrag();  
        Fragment badCommentFrag = new BadCommentFrag();  
      
        fragmentList.add(allCommentFrag);  
        fragmentList.add(goodCommentFrag);  
        fragmentList.add(badCommentFrag);  
      
        //给ViewPager设置适配器  
        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));  
        mPager.setCurrentItem(0);//设置当前显示标签页为第一页  
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());//页面变化时的监听器  
	}

	/** 
     * 初始化图片的位移像素 
     */
	private void initTextBar() {
    	barText = (TextView) super.findViewById(R.id.cursor);
    	Display display = getWindow().getWindowManager().getDefaultDisplay();
    	// 得到显示屏宽度
        DisplayMetrics metrics = new DisplayMetrics();
    	display.getMetrics(metrics);
    	// 1/3屏幕宽度
    	int  tabLineLength = metrics.widthPixels /3;
    	 LayoutParams lp = (LayoutParams) barText.getLayoutParams();
    	 lp.width = tabLineLength;
    	 barText.setLayoutParams(lp);
	}
	/** 
     * 初始化标签名 
     */ 
	private void initTextView() {
		// TODO Auto-generated method stub
		view1 = (TextView)findViewById(R.id.tv_guid1);  
        view2 = (TextView)findViewById(R.id.tv_guid2);  
        view3 = (TextView)findViewById(R.id.tv_guid3); 
        
        view1.setOnClickListener(new txListener(0));  
        view2.setOnClickListener(new txListener(1));  
        view3.setOnClickListener(new txListener(2));
	}
	
    public class txListener implements View.OnClickListener{  
        private int index=0;  
        public txListener(int i) {  
            index =i;  
        }  
        @Override  
        public void onClick(View v) {  
            mPager.setCurrentItem(index);  
        }  
    }
    public class MyOnPageChangeListener implements OnPageChangeListener{  
        @Override  
        public void onPageScrolled(int arg0, float arg1, int arg2) {  
            // TODO Auto-generated method stub  
        	  // 取得该控件的实例
            LinearLayout.LayoutParams ll = (android.widget.LinearLayout.LayoutParams) barText
                    .getLayoutParams();
            if(currIndex == arg0){
            	 ll.leftMargin = (int) (currIndex * barText.getWidth() + arg1
                         * barText.getWidth());
            }else if(currIndex > arg0){
            	 ll.leftMargin = (int) (currIndex * barText.getWidth() - (1 - arg1)* barText.getWidth());
            }
            barText.setLayoutParams(ll);
        }  
        @Override  
        public void onPageScrollStateChanged(int arg0) {  
            // TODO Auto-generated method stub  
        }  
        @Override  
        public void onPageSelected(int arg0) {  
            // TODO Auto-generated method stub  
            currIndex = arg0;
            if(arg0==0){
            	view1.setTextColor(getResources().getColor(R.color.press));
            	view2.setTextColor(getResources().getColor(R.color.tophui));
            	view3.setTextColor(getResources().getColor(R.color.tophui));
            }
            if(arg0==1){
            	view1.setTextColor(getResources().getColor(R.color.tophui));
            	view2.setTextColor(getResources().getColor(R.color.press));
            	view3.setTextColor(getResources().getColor(R.color.tophui));
            }
            if(arg0==2){
            	view1.setTextColor(getResources().getColor(R.color.tophui));
            	view2.setTextColor(getResources().getColor(R.color.tophui));
            	view3.setTextColor(getResources().getColor(R.color.press));
            }
        }  
    } 
}
