package com.pi9Lin.data;

import java.util.Arrays;

public class Area {
	int area_id;
	String area_name;
	int cityId;
	int provinceId;
	double[] gps;
	
	public Area() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Area(int area_id, String area_name,int cityId,int provinceId,double[] gps) {
		super();
		this.area_id = area_id;
		this.area_name = area_name;
		this.cityId = cityId;
		this.provinceId = provinceId;
		this.gps = gps;
	}
	
	
	
	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public int getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}

	public int getArea_id() {
		return area_id;
	}

	public void setArea_id(int area_id) {
		this.area_id = area_id;
	}

	public String getArea_name() {
		return area_name;
	}

	public void setArea_name(String area_name) {
		this.area_name = area_name;
	}

	public double[] getGps() {
		return gps;
	}

	public void setGps(double[] gps) {
		this.gps = gps;
	}

	@Override
	public String toString() {
		return "Area [area_id=" + area_id + ", area_name=" + area_name
				+ ", cityId=" + cityId + ", provinceId=" + provinceId
				+ ", gps=" + Arrays.toString(gps) + "]";
	}
}
