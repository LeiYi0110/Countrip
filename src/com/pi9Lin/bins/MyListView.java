package com.pi9Lin.bins;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

import com.pi9Lin.pulltorefresh.pullableview.Pullable;

// 下拉刷新

public class MyListView extends ListView implements Pullable{
	// 滑动距离及坐标
	private float xDistance, yDistance, xLast, yLast;
	
	private boolean canPull=false;
	
    public MyListView(Context context) {  
        super(context);  
    }  
  
    public MyListView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }
    
    public MyListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
    
	@Override
	public boolean canPullDown()
	{
		if (getCount() == 0)
		{
			// 没有item的时候也可以下拉刷新
			return true;
		}
		else if (getFirstVisiblePosition()==0&&getChildAt(0).getTop()>=0&&canPull)
		{
			// 滑到ListView的顶部了
			return true;
		} 
		else
			return false;
	}

	@Override
	public boolean canPullUp()
	{
//		if (getCount() == 0&&canPull)
//		{
//			// 没有item的时候也可以上拉加载
//			return true;
//		} else if (getLastVisiblePosition() == (getCount() - 1)&&canPull)
//		{
//			// 滑到底部了
//			if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
//					&& getChildAt(
//							getLastVisiblePosition()
//									- getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
//				return true;
//		}
		return false;
	}
    
	/**
	 * 解决嵌套vpager冲突问题
	 * */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			xLast = ev.getX();
			yLast = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();

			xDistance += Math.abs(curX - xLast);
			yDistance += Math.abs(curY - yLast);
			xLast = curX;
			yLast = curY;

			if (xDistance > yDistance+3) {
				canPull=false;
				return false; // 表示向下传递事件
			}else{
				canPull=true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
}
