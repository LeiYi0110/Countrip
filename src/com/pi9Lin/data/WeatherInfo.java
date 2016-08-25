package com.pi9Lin.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherInfo {
	String currentCity;
	String pm25;
	List<Map<String,String>> index=new ArrayList<Map<String,String>>();
	List<Map<String,String>> weather_data=new ArrayList<Map<String,String>>();
	
	public WeatherInfo(String currentCity, String pm25,
			List<Map<String, String>> index,
			List<Map<String, String>> weather_data) {
		super();
		this.currentCity = currentCity;
		this.pm25 = pm25;
		this.index = index;
		this.weather_data = weather_data;
	}

	public WeatherInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getCurrentCity() {
		return currentCity;
	}

	public void setCurrentCity(String currentCity) {
		this.currentCity = currentCity;
	}

	public String getPm25() {
		return pm25;
	}

	public void setPm25(String pm25) {
		this.pm25 = pm25;
	}

	public List<Map<String, String>> getIndex() {
		return index;
	}

	public void setIndex(List<Map<String, String>> index) {
		this.index = index;
	}

	public List<Map<String, String>> getWeather_data() {
		return weather_data;
	}

	public void setWeather_data(List<Map<String, String>> weather_data) {
		this.weather_data = weather_data;
	}

	@Override
	public String toString() {
		return "WeatherInfo [currentCity=" + currentCity + ", pm25=" + pm25
				+ ", index=" + index + ", weather_data=" + weather_data + "]";
	}
	
	
}
