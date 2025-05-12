package com.zyc.dc.dao;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "userSession")
public class UserSessionModel {
	@Id
	private String id;
	private String userId;
	private String login;
	private String password;
	private Date createTime;
	private Date lastReadTime;
	private Date lastWriteTime;
	private Date expiredTime;
	private boolean isRemember;
	
	public Date getExpiredTime() {
		return expiredTime;
	}
	public void setExpiredTime(Date expiredTime) {
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
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getLastReadTime() {
		return lastReadTime;
	}
	public void setLastReadTime(Date lastReadTime) {
		this.lastReadTime = lastReadTime;
	}
	public Date getLastWriteTime() {
		return lastWriteTime;
	}
	public void setLastWriteTime(Date lastWriteTime) {
		this.lastWriteTime = lastWriteTime;
	}
}
