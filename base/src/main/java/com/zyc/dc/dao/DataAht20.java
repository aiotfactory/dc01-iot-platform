package com.zyc.dc.dao;

public class DataAht20 {
	private Integer temperature,temperatureMax,temperatureMin;
	private Integer humidity,humidityMax,humidityMin;
	private Integer flag;
	private Integer status;
	private Long timesSuccess,timesTotal;
	public Integer getTemperatureMax() {
		return temperatureMax;
	}
	public void setTemperatureMax(Integer temperatureMax) {
		this.temperatureMax = temperatureMax;
	}
	public Integer getTemperatureMin() {
		return temperatureMin;
	}
	public void setTemperatureMin(Integer temperatureMin) {
		this.temperatureMin = temperatureMin;
	}
	public Integer getHumidityMax() {
		return humidityMax;
	}
	public void setHumidityMax(Integer humidityMax) {
		this.humidityMax = humidityMax;
	}
	public Integer getHumidityMin() {
		return humidityMin;
	}
	public void setHumidityMin(Integer humidityMin) {
		this.humidityMin = humidityMin;
	}
	public Long getTimesSuccess() {
		return timesSuccess;
	}
	public void setTimesSuccess(Long timesSuccess) {
		this.timesSuccess = timesSuccess;
	}
	public Long getTimesTotal() {
		return timesTotal;
	}
	public void setTimesTotal(Long timesTotal) {
		this.timesTotal = timesTotal;
	}
	public Integer getFlag() {
		return flag;
	}
	public void setFlag(Integer flag) {
		this.flag = flag;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getTemperature() {
		return temperature;
	}
	public void setTemperature(Integer temperature) {
		this.temperature = temperature;
	}
	public Integer getHumidity() {
		return humidity;
	}
	public void setHumidity(Integer humidity) {
		this.humidity = humidity;
	}
}
