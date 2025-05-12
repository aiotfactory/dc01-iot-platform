package com.zyc.dc.dao;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deviceTCPHost")
public class DeviceTCPHostModel {
    @Id
    private String id;
	private String host;
	private Integer port;
	private Date time;
	private Integer failedTimes;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getFailedTimes() {
		return failedTimes;
	}
	public void setFailedTimes(Integer failedTimes) {
		this.failedTimes = failedTimes;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
}