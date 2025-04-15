package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "moduleInfo")
public class ModuleInfoModel { 
    @Id
    private String id;
    private String deviceId;
	private Integer moduleTypeId;
	private Boolean allowForward;
	private Instant uploadTime;
	private Long uploadTimes;
	private Instant modifyTime;
	private Instant requestTime;
	private Long requestTimes;
	private Object upload;
	

	public Object getUpload() {
		return upload;
	}
	public void setUpload(Object upload) {
		this.upload = upload;
	}
	public Long getUploadTimes() {
		return uploadTimes;
	}
	public void setUploadTimes(Long uploadTimes) {
		this.uploadTimes = uploadTimes;
	}
	public Instant getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Instant requestTime) {
		this.requestTime = requestTime;
	}
	public Long getRequestTimes() {
		return requestTimes;
	}
	public void setRequestTimes(Long requestTimes) {
		this.requestTimes = requestTimes;
	}
	public Boolean getAllowForward() {
		return allowForward;
	}
	public void setAllowForward(Boolean allowForward) {
		this.allowForward = allowForward;
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
	public Instant getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Instant modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Instant getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Instant uploadTime) {
		this.uploadTime = uploadTime;
	}
}