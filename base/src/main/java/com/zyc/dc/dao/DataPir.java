package com.zyc.dc.dao;


public class DataPir{
	private int type;
	private long triggerTimesTotal;
	private long triggerTimesFromPreviousUpload;
	private long stateKeepSeconds;
	private int pirStatus;
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public long getTriggerTimesTotal() {
		return triggerTimesTotal;
	}
	public void setTriggerTimesTotal(long triggerTimesTotal) {
		this.triggerTimesTotal = triggerTimesTotal;
	}
	public long getTriggerTimesFromPreviousUpload() {
		return triggerTimesFromPreviousUpload;
	}
	public void setTriggerTimesFromPreviousUpload(long triggerTimesFromPreviousUpload) {
		this.triggerTimesFromPreviousUpload = triggerTimesFromPreviousUpload;
	}
	public long getStateKeepSeconds() {
		return stateKeepSeconds;
	}
	public void setStateKeepSeconds(long stateKeepSeconds) {
		this.stateKeepSeconds = stateKeepSeconds;
	}
	public int getPirStatus() {
		return pirStatus;
	}
	public void setPirStatus(int pirStatus) {
		this.pirStatus = pirStatus;
	}
}
