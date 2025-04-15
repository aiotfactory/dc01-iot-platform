package com.zyc.dc.dao;

public class DataSpl06 {
	private Integer pressure,pressureMax,pressureMin;
	private Integer temperature,temperatureMax,temperatureMin;
	private Integer flag;
	private Long timesSuccess,timesTotal;
	public Integer getPressure() {
		return pressure;
	}
	public void setPressure(Integer pressure) {
		this.pressure = pressure;
	}
	public Integer getPressureMax() {
		return pressureMax;
	}
	public void setPressureMax(Integer pressureMax) {
		this.pressureMax = pressureMax;
	}
	public Integer getPressureMin() {
		return pressureMin;
	}
	public void setPressureMin(Integer pressureMin) {
		this.pressureMin = pressureMin;
	}
	public Integer getTemperature() {
		return temperature;
	}
	public void setTemperature(Integer temperature) {
		this.temperature = temperature;
	}
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
	public Integer getFlag() {
		return flag;
	}
	public void setFlag(Integer flag) {
		this.flag = flag;
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
}
