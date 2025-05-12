package com.zyc.dc.dao;

public class DataUltrasonic{
	
	private long totalMeasureTimes;
	private int historyMin;
	private int historyMax;
	private int max;
	private int min;
	private int latest;
	private int avg;
	private int measureTimes;
	private String values;
	private DataUltrasonicType type;
	
	


	public int getHistoryMin() {
		return historyMin;
	}


	public void setHistoryMin(int historyMin) {
		this.historyMin = historyMin;
	}


	public int getHistoryMax() {
		return historyMax;
	}


	public void setHistoryMax(int historyMax) {
		this.historyMax = historyMax;
	}


	public long getTotalMeasureTimes() {
		return totalMeasureTimes;
	}


	public void setTotalMeasureTimes(long totalMeasureTimes) {
		this.totalMeasureTimes = totalMeasureTimes;
	}


	public int getMeasureTimes() {
		return measureTimes;
	}


	public void setMeasureTimes(int measureTimes) {
		this.measureTimes = measureTimes;
	}




	public int getMax() {
		return max;
	}


	public void setMax(int max) {
		this.max = max;
	}


	public int getMin() {
		return min;
	}


	public void setMin(int min) {
		this.min = min;
	}


	public int getLatest() {
		return latest;
	}


	public void setLatest(int latest) {
		this.latest = latest;
	}


	public int getAvg() {
		return avg;
	}


	public void setAvg(int avg) {
		this.avg = avg;
	}


	public String getValues() {
		return values;
	}


	public void setValues(String values) {
		this.values = values;
	}


	public DataUltrasonicType getType() {
		return type;
	}


	public void setType(DataUltrasonicType type) {
		this.type = type;
	}


	public enum DataUltrasonicType{
    	UPLOAD,
    	REQUEST,
    	TRIGGER
    }
}
