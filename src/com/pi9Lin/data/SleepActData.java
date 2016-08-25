package com.pi9Lin.data;

import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;

public class SleepActData {
	ImageView sleep_love_img;
	ImageView first_img;
	TextView collection;
	boolean isSaved;
	int page_index=0;
	String _id;
	int area_id;
	int city_id;
	String shop_id;
	int village;
	int location_value;
	int city;
	int collection_count;
	int comment_count;
	String cover;
	String xx_desc;
	/**公共字段*/
	List<SleepImgData> imgDatas;
	double[] gps;
	String xx_name;
	String xx_telephone;
	String xx_address;
	int order;
	String title;
	List<Entity> entities;
	
	public List<Entity> getEntities() {
		return entities;
	}
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public ImageView getFirst_img() {
		return first_img;
	}
	public void setFirst_img(ImageView first_img) {
		this.first_img = first_img;
	}
	public SleepActData() {
		super();
		// TODO Auto-generated constructor stub
	}
	public ImageView getSleep_love_img() {
		return sleep_love_img;
	}
	public void setSleep_love_img(ImageView sleep_love_img) {
		this.sleep_love_img = sleep_love_img;
	}
	public String getXx_desc() {
		return xx_desc;
	}
	public void setXx_desc(String xx_desc) {
		this.xx_desc = xx_desc;
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
	public int getArea_id() {
		return area_id;
	}
	public void setArea_id(int area_id) {
		this.area_id = area_id;
	}
	public int getCity_id() {
		return city_id;
	}
	public void setCity_id(int city_id) {
		this.city_id = city_id;
	}
	public String getShop_id() {
		return shop_id;
	}
	public void setShop_id(String shop_id) {
		this.shop_id = shop_id;
	}
	public int getVillage() {
		return village;
	}
	public void setVillage(int village) {
		this.village = village;
	}
	public int getLocation_value() {
		return location_value;
	}
	public void setLocation_value(int location_value) {
		this.location_value = location_value;
	}
	public int getCity() {
		return city;
	}
	public void setCity(int city) {
		this.city = city;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public List<SleepImgData> getImgDatas() {
		return imgDatas;
	}
	public void setImgDatas(List<SleepImgData> imgDatas) {
		this.imgDatas = imgDatas;
	}
	public String getXx_name() {
		return xx_name;
	}
	public void setXx_name(String xx_name) {
		this.xx_name = xx_name;
	}
	public String getXx_telephone() {
		return xx_telephone;
	}
	public void setXx_telephone(String xx_telephone) {
		this.xx_telephone = xx_telephone;
	}
	public String getXx_address() {
		return xx_address;
	}
	public void setXx_address(String xx_address) {
		this.xx_address = xx_address;
	}
	public int getPage_index() {
		return page_index;
	}
	public void setPage_index(int page_index) {
		this.page_index = page_index;
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
	
}
