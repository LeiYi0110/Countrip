package com.pi9Lin.bins;

import com.pi9Lin.pulltorefresh.pullableview.Pullable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

@SuppressLint("HandlerLeak")
/**
 * 解决ViewPager与ScrollView手势冲突的问题 的ScrollView
 * 
 * */
public class VerticalScrollView extends ScrollView implements Pullable {
	private float xDistance, yDistance, xLast, yLast;

	private OnScrollListener onScrollListener;
	/**
	 * 主要是用在用户手指离开MyScrollView，MyScrollView还在继续滑动，我们用来保存Y的距离，然后做比较
	 */
	private int lastScrollY;
	
	private boolean canPullDown=false;
	
	/**
	 * 构造方法
	 * */
	public VerticalScrollView(Context context) {
		super(context);
	}

	public VerticalScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	/**
	 * 可下拉刷新的判断
	 * */
	@Override
	public boolean canPullDown() {
		if (getScrollY() == 0&&canPullDown)
			return true;
		else
			return false;
	}
	/**
	 * 可上拉加载的判断
	 * */
	@Override
	public boolean canPullUp() {
//		if (canPullDown&&getScrollY() >= (getChildAt(0).getHeight() - getMeasuredHeight()))
//			return true;
//		else
			return false;
	}

	/**
	 * 设置滚动接口
	 * 
	 * @param onScrollListener
	 */
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}

	/**
	 * 用于用户手指离开MyScrollView的时候获取MyScrollView滚动的Y距离，然后回调给onScroll方法中
	 */
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			int scrollY = VerticalScrollView.this.getScrollY();

			// 此时的距离和记录下的距离不相等，在隔5毫秒给handler发送消息
			if (lastScrollY != scrollY) {
				lastScrollY = scrollY;
				handler.sendMessageDelayed(handler.obtainMessage(), 5);
			}
			if (onScrollListener != null) {
				onScrollListener.onScroll(scrollY);
			}

		};

	};

	/**
	 * 重写onTouchEvent， 当用户的手在MyScrollView上面的时候，
	 * 直接将MyScrollView滑动的Y方向距离回调给onScroll方法中，当用户抬起手的时候，
	 * MyScrollView可能还在滑动，所以当用户抬起手我们隔5毫秒给handler发送消息，在handler处理
	 * MyScrollView滑动的距离
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (onScrollListener != null) {
			onScrollListener.onScroll(lastScrollY = this.getScrollY());
		}
		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			handler.sendMessageDelayed(handler.obtainMessage(), 5);
			break;
		}
		return super.onTouchEvent(ev);
	}

	/**
	 * 
	 * 滚动的回调接口
	 * 
	 */
	public interface OnScrollListener {
		/**
		 * 回调方法， 返回MyScrollView滑动的Y方向距离
		 */
		public void onScroll(int scrollY);
	}

	/**
	 * 解决ViewPager与ScrollView手势冲突的问题
	 */
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
				canPullDown=false;
				return false;
			}else{
				canPullDown=true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}

}
