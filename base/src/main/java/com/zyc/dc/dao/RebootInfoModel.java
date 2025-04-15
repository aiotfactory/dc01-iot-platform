package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rebootInfo")
public class RebootInfoModel { 
    @Id
    private String id;
    @Indexed
    private String deviceId;
	private Long runtimeRestartTimes;
	private Long runtimeRestartReason;
	private Long runtimeRestartReasonTimes;
	private Long runtimeRestartReasonSeconds;
	private Integer runtimeResetReason;
	private Instant uploadTime;
	
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
	public Integer getRuntimeResetReason() {
		return runtimeResetReason;
	}
	public void setRuntimeResetReason(Integer runtimeResetReason) {
		this.runtimeResetReason = runtimeResetReason;
	}
	public Long getRuntimeRestartTimes() {
		return runtimeRestartTimes;
	}
	public void setRuntimeRestartTimes(Long runtimeRestartTimes) {
		this.runtimeRestartTimes = runtimeRestartTimes;
	}
	public Long getRuntimeRestartReason() {
		return runtimeRestartReason;
	}
	public void setRuntimeRestartReason(Long runtimeRestartReason) {
		this.runtimeRestartReason = runtimeRestartReason;
	}
	public Long getRuntimeRestartReasonTimes() {
		return runtimeRestartReasonTimes;
	}
	public void setRuntimeRestartReasonTimes(Long runtimeRestartReasonTimes) {
		this.runtimeRestartReasonTimes = runtimeRestartReasonTimes;
	}
	public Long getRuntimeRestartReasonSeconds() {
		return runtimeRestartReasonSeconds;
	}
	public void setRuntimeRestartReasonSeconds(Long runtimeRestartReasonSeconds) {
		this.runtimeRestartReasonSeconds = runtimeRestartReasonSeconds;
	}
	public Instant getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Instant uploadTime) {
		this.uploadTime = uploadTime;
	}
}