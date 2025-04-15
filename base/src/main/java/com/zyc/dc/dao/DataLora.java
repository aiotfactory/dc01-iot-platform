package com.zyc.dc.dao;

public class DataLora {
	private String uploadDatatype;
	private byte[] rxData;
	private long txTimes;
	private long txTimesTimeout;
	private long rxTimes;
	private long rxTimesTimeout;
	private long rxTimesError;
	
	public String getUploadDatatype() {
		return uploadDatatype;
	}
	public void setUploadDatatype(String uploadDatatype) {
		this.uploadDatatype = uploadDatatype;
	}
	public byte[] getRxData() {
		return rxData;
	}
	public void setRxData(byte[] rxData) {
		this.rxData = rxData;
	}
	public long getTxTimes() {
		return txTimes;
	}
	public void setTxTimes(long txTimes) {
		this.txTimes = txTimes;
	}
	public long getTxTimesTimeout() {
		return txTimesTimeout;
	}
	public void setTxTimesTimeout(long txTimesTimeout) {
		this.txTimesTimeout = txTimesTimeout;
	}
	public long getRxTimes() {
		return rxTimes;
	}
	public void setRxTimes(long rxTimes) {
		this.rxTimes = rxTimes;
	}
	public long getRxTimesTimeout() {
		return rxTimesTimeout;
	}
	public void setRxTimesTimeout(long rxTimesTimeout) {
		this.rxTimesTimeout = rxTimesTimeout;
	}
	public long getRxTimesError() {
		return rxTimesError;
	}
	public void setRxTimesError(long rxTimesError) {
		this.rxTimesError = rxTimesError;
	}	
}
