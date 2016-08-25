package com.pi9Lin.adapter;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class LvAdapter extends BaseAdapter {

	private List<View> mList;  
  
    public LvAdapter(List<View> mList) {  
        this.mList = mList;  
    }  
    
  
    @Override  
    public int getCount() {  
        return mList.size();  
    }  
  
    @Override  
    public Object getItem(int position) {  
        return mList.get(position);  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int arg0, View arg1, ViewGroup parent) {  
    	if (arg1 == null) {
			arg1 = mList.get(arg0);
		}
		return arg1;    
    } 

}
