package com.pi9Lin.data;

import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;

public class RoundData {
	ImageView sleep_love_img;
	TextView collection;
	boolean isSaved;
	int collection_count;
	int comment_count;
	int page_index=0;
	String _id;
	int province;
	String phone;
	int area_id;
	String name;
	String address;
	int entity_type;
	String cover;
	String entity_id;
	double[] gps;
	List<SleepImgData> imgDatas;
	ImageView coverImgs;
	ImageView[] insideImgs;
	
	public ImageView getSleep_love_img() {
		return sleep_love_img;
	}
	public void setSleep_love_img(ImageView sleep_love_img) {
		this.sleep_love_img = sleep_love_img;
	}
	public int getPage_index() {
		return page_index;
	}
	public void setPage_index(int page_index) {
		this.page_index = page_index;
	}
	public ImageView getCoverImgs() {
		return coverImgs;
	}
	public void setCoverImgs(ImageView coverImgs) {
		this.coverImgs = coverImgs;
	}
	public ImageView[] getInsideImgs() {
		return insideImgs;
	}
	public void setInsideImgs(ImageView[] insideImgs) {
		this.insideImgs = insideImgs;
	}
	public List<SleepImgData> getImgDatas() {
		return imgDatas;
	}
	public void setImgDatas(List<SleepImgData> imgDatas) {
		this.imgDatas = imgDatas;
	}
	public double[] getGps() {
		return gps;
	}
	public void setGps(double[] gps) {
		this.gps = gps;
	}
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public int getProvince() {
		return province;
	}
	public void setProvince(int province) {
		this.province = province;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public int getArea_id() {
		return area_id;
	}
	public void setArea_id(int area_id) {
		this.area_id = area_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
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
	public TextView getCollection() {
		return collection;
	}
	public void setCollection(TextView collection) {
		this.collection = collection;
	}
	public boolean isSaved() {
		return isSaved;
	}
	public void setSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}
	public int getCollection_count() {
		return collection_count;
	}
	public void setCollection_count(int collection_count) {
		this.collection_count = collection_count;
	}
	public int getComment_count() {
		return comment_count;
	}
	public void setComment_count(int comment_count) {
		this.comment_count = comment_count;
	}
	
}
