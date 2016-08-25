package com.pi9Lin.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pi9Lin.base.BaseFragment;
import com.pi9Lin.countrip.R;

public class UnLandIn extends BaseFragment {
	
	View view;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = getActivity();
		view = inflater.inflate(R.layout.fragment_unlandin, container, false);
		return view;
	}
}
