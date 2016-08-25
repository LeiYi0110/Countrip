package com.pi9Lin.pulltorefresh.pullableview;

import com.pi9Lin.pulltorefresh.pullableview.PullToRefreshLayout.OnRefreshListener;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

@SuppressLint("HandlerLeak")
public class RefreshListener implements OnRefreshListener
{

	@Override
	public void onRefresh(final PullToRefreshLayout pullToRefreshLayout)
	{	
		System.out.println("正在刷新");
		// 下拉刷新操作
		new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				System.out.println("刷新成功");
				// 千万别忘了告诉控件刷新完毕了哦！
				pullToRefreshLayout.refreshFinish(PullToRefreshLayout.SUCCEED);
			}
		}.sendEmptyMessageDelayed(0, 5000);
	}

	@Override
	public void onLoadMore(final PullToRefreshLayout pullToRefreshLayout)
	{
		// 加载操作
		new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				// 千万别忘了告诉控件加载完毕了哦！
				pullToRefreshLayout.loadmoreFinish(PullToRefreshLayout.SUCCEED);
			}
		}.sendEmptyMessageDelayed(0, 5000);
	}

}

