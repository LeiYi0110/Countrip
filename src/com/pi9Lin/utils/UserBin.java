package com.pi9Lin.utils;

import java.io.Serializable;
import java.util.List;

import com.pi9Lin.data.IndexFragTheme;

public class UserBin implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private List<IndexFragTheme> list;

	public List<IndexFragTheme> getList() {
		return list;
	}

	public void setList(List<IndexFragTheme> list) {
		this.list = list;
	}
	
	
}
