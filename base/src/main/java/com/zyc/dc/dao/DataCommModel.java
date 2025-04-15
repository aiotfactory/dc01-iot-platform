package com.zyc.dc.dao;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.zyc.dc.service.module.OperateRequest;

@Document(collection = "dataComm")
public class DataCommModel {
    @Id
    private String id;
	private String deviceId;
	private String deviceNo;
	private Integer moduleTypeId;
	private OperateRequest request;
	private Instant requestTime;
	private Object upload;
	private Instant uploadTime;
	private Long command;
	private Integer operate;
	private Long deviceRequestPack;
	private Long deviceUploadPack;
	private String info;
	private DataCommErrorType errorType;
	private DataCommType dataCommType;
	private DataCommSource dataCommSource;
	
    
	public Long getDeviceRequestPack() {
		return deviceRequestPack;
	}
	public void setDeviceRequestPack(Long deviceRequestPack) {
		this.deviceRequestPack = deviceRequestPack;
	}
	public Long getDeviceUploadPack() {
		return deviceUploadPack;
	}
	public void setDeviceUploadPack(Long deviceUploadPack) {
		this.deviceUploadPack = deviceUploadPack;
	}
	public String getDeviceNo() {
		return deviceNo;
	}
	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}
	public Long getCommand() {
		return command;
	}
	public void setCommand(Long command) {
		this.command = command;
	}

	public Integer getOperate() {
		return operate;
	}
	public void setOperate(Integer operate) {
		this.operate = operate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public Integer getModuleTypeId() {
		return moduleTypeId;
	}
	public void setModuleTypeId(Integer moduleTypeId) {
		this.moduleTypeId = moduleTypeId;
	}

	public OperateRequest getRequest() {
		return request;
	}
	public void setRequest(OperateRequest request) {
		this.request = request;
	}
	public Instant getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Instant requestTime) {
		this.requestTime = requestTime;
	}
	public Object getUpload() {
		return upload;
	}
	public void setUpload(Object upload) {
		this.upload = upload;
	}
	public Instant getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Instant uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public DataCommErrorType getErrorType() {
		return errorType;
	}
	public void setErrorType(DataCommErrorType errorType) {
		this.errorType = errorType;
	}
	public DataCommType getDataCommType() {
		return dataCommType;
	}
	public void setDataCommType(DataCommType dataCommType) {
		this.dataCommType = dataCommType;
	}
	public DataCommSource getDataCommSource() {
		return dataCommSource;
	}
	public void setDataCommSource(DataCommSource dataCommSource) {
		this.dataCommSource = dataCommSource;
	}
	public enum DataCommErrorType
    {
    	OK,
    	MODULE_UNAVAILABLE,
    	DEVICE_NOT_EXIST,
    	DEVICE_NOT_ONLINE,
    	DEVICE_REQUEST_TIMEOUT,
    	DEVICE_EXCEPTION,
    }
	public enum DataCommType
    {
    	REQUEST_UPLOAD,
    	PERIOD_UPLOAD,
    	INTERRUPT_UPLOAD,
    	DIRTY_UPLOAD,
    }
    public enum DataCommSource
    {
    	CLOUD_COMMAND,
    	CLOUD_WEB,
    	CLIENT_API,
    	DEVICE_AUTO
    }
}
