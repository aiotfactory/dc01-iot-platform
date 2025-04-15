package com.zyc.dc.dao;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "moduleRuleLog")
public class ModuleRuleLogModel {
    @Id
    private String id;
    @Indexed
	private String deviceId;
    @Indexed
	private Integer moduleTypeId;
    @Indexed
	private Instant uploadTime;
    @Indexed
	private Integer ruleId;
    private String ruleName;
    private String ruleOperator;
	private String reason;
	private Integer reasonId;
    @Indexed
	private ModuleRuleLogModelStatusEnum status;
    
    
	public Integer getModuleTypeId() {
		return moduleTypeId;
	}

	public void setModuleTypeId(Integer moduleTypeId) {
		this.moduleTypeId = moduleTypeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Instant getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(Instant uploadTime) {
		this.uploadTime = uploadTime;
	}

	public Integer getRuleId() {
		return ruleId;
	}

	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleOperator() {
		return ruleOperator;
	}

	public void setRuleOperator(String ruleOperator) {
		this.ruleOperator = ruleOperator;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getReasonId() {
		return reasonId;
	}

	public void setReasonId(Integer reasonId) {
		this.reasonId = reasonId;
	}

	public ModuleRuleLogModelStatusEnum getStatus() {
		return status;
	}

	public void setStatus(ModuleRuleLogModelStatusEnum status) {
		this.status = status;
	}

	public enum ModuleRuleLogModelStatusEnum{
    	INIT,
    	CANCEL,
    	PROCESS,
    	DONE,
    	PENDING
    }
}