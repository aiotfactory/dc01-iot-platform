package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "dataCache")
public class DataCacheModel {
	@Id
	private String id;
	private String key;
	private Object value;
	private Instant createTime;
	private Instant expiredTime;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
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
