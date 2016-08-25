package com.pi9Lin.commentfrag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.countrip.R;

public class AllCommentFrag extends BaseFragment {
	
	private View view;
	ListView page_list;
	
	/**测试数据*/
	int sumOfComment = 4;
	int[] resImgFace={}; 
//	int[] resUploadImg={R.drawable.up_2,R.drawable.up_2,R.drawable.up_3,R.drawable.up_1};
	String[] resLevel = { "路人甲", "一级吃货", "二级吃货", "二级吃货" };
	String[] resComment = {
			"本章内容是 全部android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com",
			"本章内容是 全部android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com",
			"本章内容是 全部android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com",
			"本章内容是 全部android.widget.RatingBar，译为评分条，版本为Android 2.2 r1，翻译来自madgoawallace2010欢迎大家访问他们的博客：http://madgoat.cn/、http://blog.csdn.net/springiscoming2008，再次感谢adgoawallace20期待你加入Android中文翻译组，联系我over140@gmail.com" };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		context=getActivity();
		view = inflater.inflate(R.layout.item_save_pagelist, container, false);
		page_list=(ListView)view.findViewById(R.id.page_list);
		page_list.setAdapter(new MyAllComApr());
		return view;
	}
	private class MyAllComApr extends BaseAdapter{
		
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return sumOfComment;
		}
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public long getItemId(int w) {
			// TODO Auto-generated method stub
			return w;
		}
		@Override
		public View getView(int index, View arg1, ViewGroup arg2) {
			/**
			 * 动态填充评论数据 ( 头像、评论、上传的图片 )
			 * */
			final int aa = index;
			if (arg1 == null) {
				arg1 = LayoutInflater.from(context).inflate(
						R.layout.item_showcomment_listitem, null);
			}
			ImageView face = (ImageView) arg1.findViewById(R.id.face);
			TextView level = (TextView) arg1.findViewById(R.id.level);
			TextView date = (TextView) arg1.findViewById(R.id.date);
			TextView comment = (TextView) arg1.findViewById(R.id.comment);
			LinearLayout upload_img = (LinearLayout) arg1
					.findViewById(R.id.upload_img);
			face.setImageDrawable(getImgResource(resImgFace[index]));
			level.setText(resLevel[index]);
			date.setText("2015-07-20");
			comment.setText(resComment[index]);
			/** 填充上传的图片 */
//			int ss = sumOfComment > 3 ? 3 : sumOfComment;
			for (int i = 0; i < sumOfComment; i++) {
				// 实例化一个线性布局的参数
				LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
				ImageView img = new ImageView(context);
//				img.setImageDrawable(getImgResource(resUploadImg[i]));
				img.setLayoutParams(lp1);
				img.setPadding(0, 0, 15, 0);
				int dd = upload_img.getChildCount();
				if(dd<3){
					upload_img.addView(img);
				}
			}
			arg1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					System.out.println("item" + aa);
				}
			});
			return arg1;
		}
		
	}
}
