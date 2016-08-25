package com.pi9Lin.data;

import java.util.Arrays;

public class IndexFragFocusImage {
	String _id;
	String name;
	int entity_type;
	String cover;
	String address;
	String entity_id;
	double[] gps;
	public IndexFragFocusImage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IndexFragFocusImage(String _id, String name, int entity_type,
			String cover,String address,String entity_id,double[] gps) {
		super();
		this._id = _id;
		this.name = name;
		this.entity_type = entity_type;
		this.cover = cover;
		this.address = address;
		this.entity_id = entity_id;
		this.gps = gps;
		
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getEntity_type() {
		return entity_type;
	}
	public void setEntity_type(int entity_type) {
		this.entity_type = entity_type;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public String getEntity_id() {
		return entity_id;
	}
	public void setEntity_id(String entity_id) {
		this.entity_id = entity_id;
	}
	
	public double[] getGps() {
		return gps;
	}
	public void setGps(double[] gps) {
		this.gps = gps;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "IndexFragFocusImage [_id=" + _id + ", name=" + name
				+ ", entity_type=" + entity_type + ", cover=" + cover
				+ ", address=" + address + ", entity_id=" + entity_id
				+ ", gps=" + Arrays.toString(gps) + "]";
	}
	
}
