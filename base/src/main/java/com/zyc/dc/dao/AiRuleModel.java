package com.zyc.dc.dao;

import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "aiRule")
public class AiRuleModel {
    @Id
    private String id;
	private String userId;
	private List<String> deviceNos;
	private List<Date[]> exeTime;
	private String name;
	private Integer triggerVariableCount;
	private Integer limitTimesPerDay;
	private Integer triggerTimesPerDay;
	private String sysMsg;
	private String userMsg;
	private String expectMsg;
	private Boolean expectStatus;
    private Date createTime;
    private Date modifyTime;
    private Date triggerTime;
    private AiRuleModelStatus status;
    
    public String getExpectMsg() {
		return expectMsg;
	}

	public void setExpectMsg(String expectMsg) {
		this.expectMsg = expectMsg;
	}

	public Boolean getExpectStatus() {
		return expectStatus;
	}

	public void setExpectStatus(Boolean expectStatus) {
		this.expectStatus = expectStatus;
	}

	public Integer getLimitTimesPerDay() {
		return limitTimesPerDay;
	}

	public void setLimitTimesPerDay(Integer limitTimesPerDay) {
		this.limitTimesPerDay = limitTimesPerDay;
	}

	public Integer getTriggerTimesPerDay() {
		return triggerTimesPerDay;
	}

	public void setTriggerTimesPerDay(Integer triggerTimesPerDay) {
		this.triggerTimesPerDay = triggerTimesPerDay;
	}

	public Date getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Date triggerTime) {
		this.triggerTime = triggerTime;
	}

	public String getSysMsg() {
		return sysMsg;
	}

	public void setSysMsg(String sysMsg) {
		this.sysMsg = sysMsg;
	}

	public String getUserMsg() {
		return userMsg;
	}

	public void setUserMsg(String userMsg) {
		this.userMsg = userMsg;
	}

	public List<String> getDeviceNos() {
		return deviceNos;
	}

	public void setDeviceNos(List<String> deviceNos) {
		this.deviceNos = deviceNos;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<Date[]> getExeTime() {
		return exeTime;
	}

	public void setExeTime(List<Date[]> exeTime) {
		this.exeTime = exeTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}



	public Integer getTriggerVariableCount() {
		return triggerVariableCount;
	}

	public void setTriggerVariableCount(Integer triggerVariableCount) {
		this.triggerVariableCount = triggerVariableCount;
	}



	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public AiRuleModelStatus getStatus() {
		return status;
	}

	public void setStatus(AiRuleModelStatus status) {
		this.status = status;
	}

	public enum AiRuleModelStatus{
    	ENABLED,
    	DISABLED
    }
}
