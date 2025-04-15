package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "otaDownload")
public class OTADownloadModel {
	@Id
	private String id;
	private String userId;
	private String deviceId;
	private String otaRecordId;
    @Indexed
	private String token;
	private String md5;
	private String rsp;
	private Instant createTime;
	private Instant expiredTime;
	
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getRsp() {
		return rsp;
	}
	public void setRsp(String rsp) {
		this.rsp = rsp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getOtaRecordId() {
		return otaRecordId;
	}
	public void setOtaRecordId(String otaRecordId) {
		this.otaRecordId = otaRecordId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Instant getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Instant createTime) {
		this.createTime = createTime;
	}
	public Instant getExpiredTime() {
		return expiredTime;
	}
	public void setExpiredTime(Instant expiredTime) {
		this.expiredTime = expiredTime;
	}
	
}
