package com.zyc.dc.dao;

public class DataCamera {
	private String picContentBase64;
	private String picName;
	private int picSize;
	private String picId;
	
	public String getPicContentBase64() {
		return picContentBase64;
	}
	public void setPicContentBase64(String picContentBase64) {
		this.picContentBase64 = picContentBase64;
	}
	public String getPicName() {
		return picName;
	}
	public void setPicName(String picName) {
		this.picName = picName;
	}
	public int getPicSize() {
		return picSize;
	}
	public void setPicSize(int picSize) {
		this.picSize = picSize;
	}
	public String getPicId() {
		return picId;
	}
	public void setPicId(String picId) {
		this.picId = picId;
	}
}
