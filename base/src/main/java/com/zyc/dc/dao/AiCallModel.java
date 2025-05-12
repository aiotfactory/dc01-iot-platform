package com.zyc.dc.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "aiCall")
public class AiCallModel {
    @Id
    private String id;
    private String userId;
    private String deviceId;
    private String ruleId;
    private String templateSystem;
    private String templateUser;
    private String actualSystem;
    private String actualUser;
    @Transient
    private List<byte[]> actualImages = new ArrayList<>();
    private List<String> actualImagesId = new ArrayList<>();
    @Transient
    private List<byte[]> actualAudios = new ArrayList<>();;
    private List<String> actualAudiosId = new ArrayList<>();;
    @Transient
    private List<byte[]> actualVideos = new ArrayList<>();
    private List<String> actualVideosId = new ArrayList<>();;
    private String actualOutput;
    private Integer tokensInput;
    private Integer tokensInputText;
    private Integer tokensInputImage;
    private Integer tokensInputAudio;
    private Integer tokensInputVideo;
    private Integer tokensOutput;
    private Integer tokensOutputText;
    private Integer tokensOutputImage;
    private Integer tokensOutputAudio;
    private Integer tokensOutputVideo;
    private Integer tokensTotal;
    private Long moneyUsed;
    private Date createTime;
    private AiCallStatus status;
    private String errorMsg;
    
    
	public Long getMoneyUsed() {
		return moneyUsed;
	}

	public void setMoneyUsed(Long moneyUsed) {
		this.moneyUsed = moneyUsed;
	}

	public List<String> getActualImagesId() {
		return actualImagesId;
	}

	public void setActualImagesId(List<String> actualImagesId) {
		this.actualImagesId = actualImagesId;
	}

	public List<String> getActualAudiosId() {
		return actualAudiosId;
	}

	public void setActualAudiosId(List<String> actualAudiosId) {
		this.actualAudiosId = actualAudiosId;
	}

	public List<String> getActualVideosId() {
		return actualVideosId;
	}

	public void setActualVideosId(List<String> actualVideosId) {
		this.actualVideosId = actualVideosId;
	}

	public List<byte[]> getActualImages() {
		return actualImages;
	}

	public void setActualImages(List<byte[]> actualImages) {
		this.actualImages = actualImages;
	}

	public List<byte[]> getActualAudios() {
		return actualAudios;
	}

	public void setActualAudios(List<byte[]> actualAudios) {
		this.actualAudios = actualAudios;
	}

	public List<byte[]> getActualVideos() {
		return actualVideos;
	}

	public void setActualVideos(List<byte[]> actualVideos) {
		this.actualVideos = actualVideos;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
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

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String aiRuleId) {
		this.ruleId = aiRuleId;
	}

	public String getTemplateSystem() {
		return templateSystem;
	}

	public void setTemplateSystem(String templateSystem) {
		this.templateSystem = templateSystem;
	}

	public String getTemplateUser() {
		return templateUser;
	}

	public void setTemplateUser(String templateUser) {
		this.templateUser = templateUser;
	}

	public String getActualSystem() {
		return actualSystem;
	}

	public void setActualSystem(String actualSystem) {
		this.actualSystem = actualSystem;
	}

	public String getActualUser() {
		return actualUser;
	}

	public void setActualUser(String actualUser) {
		this.actualUser = actualUser;
	}

	public String getActualOutput() {
		return actualOutput;
	}

	public void setActualOutput(String actualOutput) {
		this.actualOutput = actualOutput;
	}

	public Integer getTokensInput() {
		return tokensInput;
	}

	public void setTokensInput(Integer tokensInput) {
		this.tokensInput = tokensInput;
	}

	public Integer getTokensInputText() {
		return tokensInputText;
	}

	public void setTokensInputText(Integer tokensInputText) {
		this.tokensInputText = tokensInputText;
	}

	public Integer getTokensInputImage() {
		return tokensInputImage;
	}

	public void setTokensInputImage(Integer tokensInputImage) {
		this.tokensInputImage = tokensInputImage;
	}

	public Integer getTokensInputAudio() {
		return tokensInputAudio;
	}

	public void setTokensInputAudio(Integer tokensInputAudio) {
		this.tokensInputAudio = tokensInputAudio;
	}

	public Integer getTokensInputVideo() {
		return tokensInputVideo;
	}

	public void setTokensInputVideo(Integer tokensInputVideo) {
		this.tokensInputVideo = tokensInputVideo;
	}

	public Integer getTokensOutput() {
		return tokensOutput;
	}

	public void setTokensOutput(Integer tokensOutput) {
		this.tokensOutput = tokensOutput;
	}

	public Integer getTokensOutputText() {
		return tokensOutputText;
	}

	public void setTokensOutputText(Integer tokensOutputText) {
		this.tokensOutputText = tokensOutputText;
	}

	public Integer getTokensOutputImage() {
		return tokensOutputImage;
	}

	public void setTokensOutputImage(Integer tokensOutputImage) {
		this.tokensOutputImage = tokensOutputImage;
	}

	public Integer getTokensOutputAudio() {
		return tokensOutputAudio;
	}

	public void setTokensOutputAudio(Integer tokensOutputAudio) {
		this.tokensOutputAudio = tokensOutputAudio;
	}

	public Integer getTokensOutputVideo() {
		return tokensOutputVideo;
	}

	public void setTokensOutputVideo(Integer tokensOutputVideo) {
		this.tokensOutputVideo = tokensOutputVideo;
	}

	public Integer getTokensTotal() {
		return tokensTotal;
	}

	public void setTokensTotal(Integer tokensTotal) {
		this.tokensTotal = tokensTotal;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public AiCallStatus getStatus() {
		return status;
	}

	public void setStatus(AiCallStatus status) {
		this.status = status;
	}

	public enum AiCallStatus{
    	SUCCESS,
    	TIMEOUT,
    	EXCEPTION,
    	WRONG_INPUT
    }
}
