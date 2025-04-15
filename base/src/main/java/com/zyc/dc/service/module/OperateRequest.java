package com.zyc.dc.service.module;

import com.zyc.dc.dao.DataCommModel;

public class OperateRequest {
	private String rawString;
	private DataCommModel.DataCommSource sourceType;
	private String ip;
	public String getRawString() {
		return rawString;
	}
	public void setRawString(String rawString) {
		this.rawString = rawString;
	}
	public DataCommModel.DataCommSource getSourceType() {
		return sourceType;
	}
	public void setSourceType(DataCommModel.DataCommSource sourceType) {
		this.sourceType = sourceType;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
}
