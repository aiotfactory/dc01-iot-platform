package com.zyc.dc.dao;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "moduleInfo")
public class ModuleInfoModel { 
    @Id
    private String id;
    private String deviceId;
	private Integer moduleTypeId;
	private Boolean allowForward;
	private Date uploadTime;
	private Long uploadTimes;
	private Date modifyTime;
	private Date requestTime;
	private Long requestTimes;
	private Long validUploadTimes;
	private Object upload;
	

	public Long getValidUploadTimes() {
		return validUploadTimes;
	}
	public void incValidUploadTimes() {
		if(validUploadTimes==null)
			validUploadTimes=1L;
		else
			validUploadTimes=validUploadTimes+1;
	}
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
	public Date getRequestTime() {
		return requestTime;
	}
	public void setRequestTime(Date requestTime) {
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
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
}