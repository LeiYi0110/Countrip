package com.pi9Lin.data;

import java.util.List;

public class City{
	int cityId;
	String cityName;
	List<Area> area;
	int provinceId;
	int isShow;
	public City() {
		super();
		// TODO Auto-generated constructor stub
	}
	public City(int cityId, String cityName, List<Area> area,int provinceId,int isShow) {
		super();
		this.cityId = cityId;
		this.cityName = cityName;
		this.area = area;
		this.provinceId = provinceId;
		this.isShow = isShow;
	}
	
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public List<Area> getArea() {
		return area;
	}
	public void setArea(List<Area> area) {
		this.area = area;
	}
	
	public int getIsShow() {
		return isShow;
	}
	public void setIsShow(int isShow) {
		this.isShow = isShow;
	}
	@Override
	public String toString() {
		return "City [cityId=" + cityId + ", cityName=" + cityName + ", area="
				+ area + ", provinceId=" + provinceId + ", isShow=" + isShow
				+ "]";
	}
} 
