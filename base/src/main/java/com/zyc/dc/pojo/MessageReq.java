package com.zyc.dc.pojo;

import java.time.Instant;
import java.util.Arrays;

import com.zyc.dc.service.MiscUtil;

public class MessageReq {
	
    private Long devicetype,command,flag,packSeq,deviceFirmwareType;
    private byte[] data,rawData;
    private String mac,clientIp;
    private Long errorType;
    private Long restartTimes;
    private Long operate;
    private Integer csq;
    private Instant now;

    public MessageReq(String clientIp,Long devicetype,Long deviceFirmwareType,Long command,Long flag,Long packSeq,String mac,Integer csq,Long restartTimes,byte[] data,Long errorType,byte[] rawData) 
    {
        this.command = command;
        this.flag = flag;
        this.packSeq = packSeq;
        this.data = data;
        this.rawData = rawData;
        this.mac = mac;
        this.csq=csq;
        this.clientIp=clientIp;
        this.errorType=0L;
        this.restartTimes=restartTimes;
        this.devicetype=devicetype;
        this.deviceFirmwareType=deviceFirmwareType;
        this.now=Instant.now();
    }
    public MessageReq(Long errorType) {
    	this.errorType=errorType;
        this.now=Instant.now();
    }
    public MessageReq(byte[] data) {
        this.data = data;
        this.now=Instant.now();
    }
    public String toString() {
    	byte[] tempdata=data;
    	if(tempdata.length>100)
    		tempdata=Arrays.copyOf(tempdata, 100);
    	String datastr=MiscUtil.bytesToHex(tempdata);
    	if(data.length>100)
    		datastr=datastr+"...";
    	String retstr="messagereq device type "+devicetype+" firmware type "+ deviceFirmwareType+" mac "+mac+" csq "+csq+" command "+command+" flag "+flag+" seq "+packSeq+" data len "+data.length+" data "+datastr;
    	return retstr;
    }
    
	public Long getOperate() {
		return operate;
	}
	public void setOperate(Long operate) {
		this.operate = operate;
	}
	public Long getDevicetype() {
		return devicetype;
	}
	public void setDevicetype(Long devicetype) {
		this.devicetype = devicetype;
	}
	public Long getCommand() {
		return command;
	}
	public void setCommand(Long command) {
		this.command = command;
	}
	public Long getFlag() {
		return flag;
	}
	public void setFlag(Long flag) {
		this.flag = flag;
	}
	public Long getPackSeq() {
		return packSeq;
	}
	public void setPackSeq(Long packSeq) {
		this.packSeq = packSeq;
	}
	public Integer getCsq() {
		return csq;
	}
	public void setCsq(Integer csq) {
		this.csq = csq;
	}
	public Long getDeviceFirmwareType() {
		return deviceFirmwareType;
	}
	public void setDeviceFirmwareType(Long deviceFirmwareType) {
		this.deviceFirmwareType = deviceFirmwareType;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public byte[] getRawData() {
		return rawData;
	}
	public void setRawData(byte[] rawData) {
		this.rawData = rawData;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}
	public Long getErrorType() {
		return errorType;
	}
	public void setErrorType(Long errorType) {
		this.errorType = errorType;
	}
	public Long getRestartTimes() {
		return restartTimes;
	}
	public void setRestartTimes(Long restartTimes) {
		this.restartTimes = restartTimes;
	}
	public Instant getNow() {
		return now;
	}
	public void setNow(Instant now) {
		this.now = now;
	}
}
