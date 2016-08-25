package com.pi9Lin.data;

import java.util.List;


public class ProvinceInfo {
	String _id;
	List<City> city;
	String region;
	int provinceId;
	String provinceName;
	public ProvinceInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ProvinceInfo(String _id, List<City> city, String region,
			int provinceId, String provinceName) {
		super();
		this._id = _id;
		this.city = city;
		this.region = region;
		this.provinceId = provinceId;
		this.provinceName = provinceName;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public List<City> getCity() {
		return city;
	}
	public void setCity(List<City> city) {
		this.city = city;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public int getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(int provinceId) {
		this.provinceId = provinceId;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	@Override
	public String toString() {
		return "ProvinceInfo [_id=" + _id + ", city=" + city + ", region="
				+ region + ", provinceId=" + provinceId + ", provinceName="
				+ provinceName + "]";
	}
	
}



