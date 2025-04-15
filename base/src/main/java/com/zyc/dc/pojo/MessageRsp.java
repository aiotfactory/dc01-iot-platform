package com.zyc.dc.pojo;

import java.util.Arrays;
import com.zyc.dc.service.MiscUtil;


public class MessageRsp {
    private Long command,flag,packSeq,errorType;
    private byte[] data;
    private String mac;
    private MessageReq msgReq;
    public MessageRsp(String mac,Long command,Long flag,Long packSeq,byte[] data,MessageReq msgReq,Long errorType) 
    {
        this.command = command;
        this.flag = flag;
        this.packSeq = packSeq;
        this.data = data;
        this.mac=mac;
        this.msgReq=msgReq;
        this.errorType=errorType;
    }
    public MessageRsp(Long errorType) {
        this.command = 0L;
        this.flag = 0L;
        this.packSeq = 0L;
        this.data = null;
        this.mac=null;
        this.msgReq=null;
        this.errorType=errorType;
    }
    public String getStringData()
    {
    	if(data!=null)
    		return new String(data);
    	return "";
    }
    public String toString() {
    	byte[] tempRaw=data;
    	if(tempRaw==null)
    		tempRaw=new byte[0];
    	byte[] tempData=tempRaw;
    	if(tempData.length>500)
    		tempData=Arrays.copyOf(tempData, 500);
    	String datastr=MiscUtil.bytesToHex(tempData);
    	if((tempRaw!=null)&&(tempRaw.length>500))
    		datastr=datastr+"...";
    	String retstr="messagersp mac "+mac+" command "+command+" flag "+flag+" seq "+packSeq+" data len "+tempRaw.length+" error "+errorType+" data "+datastr;
    	return retstr;
    }
	public Long getCommand() {
		return command;
	}
	public void setCommand(Long command) {
		this.command = command;
	}
	public Long getFlag() {
		return flag;
	}
	public void setFlag(Long flag) {
		this.flag = flag;
	}
	public Long getPackSeq() {
		return packSeq;
	}
	public void setPackSeq(Long packSeq) {
		this.packSeq = packSeq;
	}
	public Long getErrorType() {
		return errorType;
	}
	public void setErrorType(Long errorType) {
		this.errorType = errorType;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public MessageReq getMsgReq() {
		return msgReq;
	}
	public void setMsgReq(MessageReq msgReq) {
		this.msgReq = msgReq;
	}
}
