package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lockInfo")
public class LockInfoModel { 
    @Id
    private String id;
	private Long restartTimes;
	private Integer tm7705Status;
	private Integer sht30Status;
	private Instant updateTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Instant getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Instant updateTime) {
		this.updateTime = updateTime;
	}
	public Integer getSht30Status() {
		return sht30Status;
	}
	public void setSht30Status(Integer sht30Status) {
		this.sht30Status = sht30Status;
	}
	public Long getRestartTimes() {
		return restartTimes;
	}
	public void setRestartTimes(Long restartTimes) {
		this.restartTimes = restartTimes;
	}
	public Integer getTm7705Status() {
		return tm7705Status;
	}
	public void setTm7705Status(Integer tm7705Status) {
		this.tm7705Status = tm7705Status;
	}
}