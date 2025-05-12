package com.zyc.dc.dao;

public class DataThermal{
	private int times;
	private int maxCurrent;
	private int minCurrent;
	private int avgCurrent;
	private int maxI,maxJ;
	private int minI,minJ;
	private int maxHistory;
	private int minHistory;
	private int avgMaxHistory;
	private int avgMinHistory;
	private String pixels;
	

	public String getPixels() {
		return pixels;
	}
	public void setPixels(String pixels) {
		this.pixels = pixels;
	}
	public int getTimes() {
		return times;
	}
	public void setTimes(int times) {
		this.times = times;
	}
	public int getMaxCurrent() {
		return maxCurrent;
	}
	public void setMaxCurrent(int maxCurrent) {
		this.maxCurrent = maxCurrent;
	}
	public int getMinCurrent() {
		return minCurrent;
	}
	public void setMinCurrent(int minCurrent) {
		this.minCurrent = minCurrent;
	}
	public int getAvgCurrent() {
		return avgCurrent;
	}
	public void setAvgCurrent(int avgCurrent) {
		this.avgCurrent = avgCurrent;
	}
	public int getMaxI() {
		return maxI;
	}
	public void setMaxI(int maxI) {
		this.maxI = maxI;
	}
	public int getMaxJ() {
		return maxJ;
	}
	public void setMaxJ(int maxJ) {
		this.maxJ = maxJ;
	}
	public int getMinI() {
		return minI;
	}
	public void setMinI(int minI) {
		this.minI = minI;
	}
	public int getMinJ() {
		return minJ;
	}
	public void setMinJ(int minJ) {
		this.minJ = minJ;
	}
	public int getMaxHistory() {
		return maxHistory;
	}
	public void setMaxHistory(int maxHistory) {
		this.maxHistory = maxHistory;
	}
	public int getMinHistory() {
		return minHistory;
	}
	public void setMinHistory(int minHistory) {
		this.minHistory = minHistory;
	}
	public int getAvgMaxHistory() {
		return avgMaxHistory;
	}
	public void setAvgMaxHistory(int avgMaxHistory) {
		this.avgMaxHistory = avgMaxHistory;
	}
	public int getAvgMinHistory() {
		return avgMinHistory;
	}
	public void setAvgMinHistory(int avgMinHistory) {
		this.avgMinHistory = avgMinHistory;
	}

}
