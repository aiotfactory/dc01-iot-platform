package com.zyc.dc.dao;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "lockInfo")
public class LockInfoModel { 
    @Id
    private String id;
	private Long restartTimes;
	private Integer tm7705Status;
	private Integer sht30Status;
	private Integer aht20Status;
	private Date updateTime;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
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
	public Integer getAht20Status() {
		return aht20Status;
	}
	public void setAht20Status(Integer aht20Status) {
		this.aht20Status = aht20Status;
	}
}