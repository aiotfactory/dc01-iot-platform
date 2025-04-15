package com.zyc.dc.dao;

public class DataUart {
	private DataType dataType;
	private ProtocolType protocolType;
	private String rxData;
	private long rxTimes;
	private long rxLength;
	private long rxTimesFailed;
	private long rxUploadFailed;
	private long rxFifoOverTimes;
	private long rxBufFullTimes;
	private long rxBreakTimes;
	private long rxParityErrTimes;
	private long rxFrameErrTimes;
	private long txLength;
	private long txTimes;
	private long txTimesFailed;
	
	public DataType getDataType() {
		return dataType;
	}
	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
	public ProtocolType getProtocolType() {
		return protocolType;
	}
	public void setProtocolType(ProtocolType protocolType) {
		this.protocolType = protocolType;
	}
	public String getRxData() {
		return rxData;
	}
	public void setRxData(String rxData) {
		this.rxData = rxData;
	}
	public long getRxUploadFailed() {
		return rxUploadFailed;
	}
	public void setRxUploadFailed(long rxUploadFailed) {
		this.rxUploadFailed = rxUploadFailed;
	}
	public long getTxTimesFailed() {
		return txTimesFailed;
	}
	public void setTxTimesFailed(long txTimesFailed) {
		this.txTimesFailed = txTimesFailed;
	}
	public long getRxLength() {
		return rxLength;
	}
	public void setRxLength(long rxLength) {
		this.rxLength = rxLength;
	}
	public long getRxTimes() {
		return rxTimes;
	}
	public void setRxTimes(long rxTimes) {
		this.rxTimes = rxTimes;
	}
	public long getRxTimesFailed() {
		return rxTimesFailed;
	}
	public void setRxTimesFailed(long rxTimesFailed) {
		this.rxTimesFailed = rxTimesFailed;
	}
	public long getRxFifoOverTimes() {
		return rxFifoOverTimes;
	}
	public void setRxFifoOverTimes(long rxFifoOverTimes) {
		this.rxFifoOverTimes = rxFifoOverTimes;
	}
	public long getRxBufFullTimes() {
		return rxBufFullTimes;
	}
	public void setRxBufFullTimes(long rxBufFullTimes) {
		this.rxBufFullTimes = rxBufFullTimes;
	}
	public long getRxBreakTimes() {
		return rxBreakTimes;
	}
	public void setRxBreakTimes(long rxBreakTimes) {
		this.rxBreakTimes = rxBreakTimes;
	}
	public long getRxParityErrTimes() {
		return rxParityErrTimes;
	}
	public void setRxParityErrTimes(long rxParityErrTimes) {
		this.rxParityErrTimes = rxParityErrTimes;
	}
	public long getRxFrameErrTimes() {
		return rxFrameErrTimes;
	}
	public void setRxFrameErrTimes(long rxFrameErrTimes) {
		this.rxFrameErrTimes = rxFrameErrTimes;
	}
	public long getTxLength() {
		return txLength;
	}
	public void setTxLength(long txLength) {
		this.txLength = txLength;
	}
	public long getTxTimes() {
		return txTimes;
	}
	public void setTxTimes(long txTimes) {
		this.txTimes = txTimes;
	}
	
    public enum DataType{
    	DTU,
    	PROPERTY
    }
    public enum ProtocolType{
    	UART,
    	RS485
    }
}
