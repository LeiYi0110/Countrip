package com.pi9Lin.data;

import java.util.List;

public class IndexFragTheme {
	String cover;
	int order=0;
	String title;
	List<Entity> entities;
	public IndexFragTheme() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IndexFragTheme(String cover, int order, String title, List<Entity> entities) {
		super();
		this.cover = cover;
		this.order = order;
		this.title = title;
		this.entities = entities;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<Entity> getEntities() {
		return entities;
	}
	public void setEntities(List<Entity> entities) {
		this.entities = entities;
	}
	@Override
	public String toString() {
		return "IndexFragTheme [cover=" + cover + ", order=" + order
				+ ", title=" + title + ", entities=" + entities + "]";
	}
}
