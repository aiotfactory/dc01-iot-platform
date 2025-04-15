package com.zyc.dc.constant;

import java.util.Map;

import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.service.module.OperateSpec;

public class ResultType {
	private int errorType;
	private String errorString;
	private DataCommModel dataComm;
	private OperateSpec operateSpec;
	private byte[] responseBytes;
	private String responseStr;
	private String hexContent;
	
	public ResultType(int errorType,String errorString,String responseStr)
	{
		this.errorString=errorString;
		this.errorType=errorType;
		this.responseStr=responseStr;
	}
	public ResultType(int errorType,String errorString)
	{
		this.errorString=errorString;
		this.errorType=errorType;
	}
	public ResultType(int errorType)
	{
		this.errorType=errorType;
	}
	public int getErrorType() {
		return errorType;
	}
	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}
	public String getErrorString() {
		return errorString;
	}
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}
	public String getHexContent() {
		return this.hexContent;
	}

	public DataCommModel getDataComm() {
		return dataComm;
	}
	public void setDataComm(DataCommModel dataComm) {
		this.dataComm = dataComm;
		if((dataComm!=null)&&(dataComm.getUpload()!=null))
		{
			@SuppressWarnings("unchecked")
			Map<String,String> hexMap=(Map<String,String>)dataComm.getUpload();
			hexContent=hexMap.get("hexContent");
		}
	}
	public String getResponseStr() {
		return responseStr;
	}
	public void setResponseStr(String responseStr) {
		this.responseStr = responseStr;
	}
	public OperateSpec getOperateSpec() {
		return operateSpec;
	}
	public void setOperateSpec(OperateSpec operateSpec) {
		this.operateSpec = operateSpec;
	}
	public byte[] getResponseBytes() {
		return responseBytes;
	}
	public void setResponseBytes(byte[] responseBytes) {
		this.responseBytes = responseBytes;
	}
}
