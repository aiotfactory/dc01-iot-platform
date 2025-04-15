package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "userSession")
public class UserSessionModel {
	@Id
	private String id;
	private String userId;
	private String login;
	private String password;
	private Instant createTime;
	private Instant lastReadTime;
	private Instant lastWriteTime;
	private Instant expiredTime;
	private boolean isRemember;
	
	public Instant getExpiredTime() {
		return expiredTime;
	}
	public void setExpiredTime(Instant expiredTime) {
		this.expiredTime = expiredTime;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public boolean isRemember() {
		return isRemember;
	}
	public void setRemember(boolean isRemember) {
		this.isRemember = isRemember;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public Instant getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Instant createTime) {
		this.createTime = createTime;
	}
	public Instant getLastReadTime() {
		return lastReadTime;
	}
	public void setLastReadTime(Instant lastReadTime) {
		this.lastReadTime = lastReadTime;
	}
	public Instant getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(Instant lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
}
