package com.zyc.dc.dao;


public class DataBatAdc {
	private int adcValue;
	private long adcTimes;
	private long adcTimesFailed;
	private int adcPinOnoff;
	
	public int getAdcValue() {
		return adcValue;
	}
	public void setAdcValue(int adcValue) {
		this.adcValue = adcValue;
	}
	public long getAdcTimes() {
		return adcTimes;
	}
	public void setAdcTimes(long adcTimes) {
		this.adcTimes = adcTimes;
	}
	public long getAdcTimesFailed() {
		return adcTimesFailed;
	}
	public void setAdcTimesFailed(long adcTimesFailed) {
		this.adcTimesFailed = adcTimesFailed;
	}
	public int getAdcPinOnoff() {
		return adcPinOnoff;
	}
	public void setAdcPinOnoff(int adcPinOnoff) {
		this.adcPinOnoff = adcPinOnoff;
	}
}
