package com.pi9Lin.data;

import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;

public class Entity {
	String url;
	String entity_id;
	int order;
	int entity_type;
	List<SleepImgData> images;
	double[] gps;
	String name;
	int collected_count;
	ImageView imageView;
	TextView count_num;
	boolean isSaved;
	int page_index=0;
	
	public int getPage_index() {
		return page_index;
	}
	public void setPage_index(int page_index) {
		this.page_index = page_index;
	}
	public boolean isSaved() {
		return isSaved;
	}
	public void setSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}
	public TextView getCount_num() {
		return count_num;
	}
	public void setCount_num(TextView count_num) {
		this.count_num = count_num;
	}
	public ImageView getImageView() {
		return imageView;
	}
	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}
	public Entity() {
		super();
	}
	public Entity(String url, String entity_id, int order, int entity_type,List<SleepImgData> images,double[] gps,String name,int collected_count) {
		super();
		this.url = url;
		this.entity_id = entity_id;
		this.order = order;
		this.entity_type = entity_type;
		this.images = images;
		this.gps = gps;
		this.name = name;
		this.collected_count = collected_count;
	}
	
	public int getCollected_count() {
		return collected_count;
	}
	public void setCollected_count(int collected_count) {
		this.collected_count = collected_count;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getEntity_id() {
		return entity_id;
	}
	public void setEntity_id(String entity_id) {
		this.entity_id = entity_id;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public int getEntity_type() {
		return entity_type;
	}
	public void setEntity_type(int entity_type) {
		this.entity_type = entity_type;
	}
	
	public List<SleepImgData> getImages() {
		return images;
	}
	public void setImages(List<SleepImgData> images) {
		this.images = images;
	}
	
	public double[] getGps() {
		return gps;
	}
	public void setGps(double[] gps) {
		this.gps = gps;
	}
	@Override
	public String toString() {
		return "Entity [url=" + url + ", entity_id=" + entity_id + ", order="
				+ order + ", entity_type=" + entity_type + "]";
	}
	
}
