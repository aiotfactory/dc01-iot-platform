package com.zyc.dc.dao;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.zyc.dc.dao.DeviceModel.DeviceType;

@Document(collection = "otaRecord")
public class OTARecordModel {
    @Id
    private String id;
    @Indexed
	private String userId;
    private String name;
    private DeviceType deviceType;
    private String deviceFirmWareTargetVersion;
    private Long deviceFirmWareVersion;
    private List<String> deviceNoListInclude;
    private List<String> deviceNoListExclude;
	private String firmWareName;
	private String firmWareOriginalName;
	private String firmWareId;
	private String firmWareMd5;
	private Integer firmWareSize;
	private Integer downloadTimes;
	private Instant firmwareTime;
	private Instant createTime;
	private Instant modifyTime;
	private OTAUpgradeStrategy upgradeStrategy;
	private OTAUpgradeScope upgradeScope;
	private String comments;
	
    public String getComments() {
		return comments;
	}
	public void setComments(String comments) {
		this.comments = comments;
	}
	public String getFirmWareName() {
		return firmWareName;
	}
	public void setFirmWareName(String firmWareName) {
		this.firmWareName = firmWareName;
	}
	public String getFirmWareOriginalName() {
		return firmWareOriginalName;
	}
	public void setFirmWareOriginalName(String firmWareOriginalName) {
		this.firmWareOriginalName = firmWareOriginalName;
	}
	public Instant getFirmwareTime() {
		return firmwareTime;
	}
	public void setFirmwareTime(Instant firmwareTime) {
		this.firmwareTime = firmwareTime;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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

	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	public String getDeviceFirmWareTargetVersion() {
		return deviceFirmWareTargetVersion;
	}
	public void setDeviceFirmWareTargetVersion(String deviceFirmWareTargetVersion) {
		this.deviceFirmWareTargetVersion = deviceFirmWareTargetVersion;
	}
	public Long getDeviceFirmWareVersion() {
		return deviceFirmWareVersion;
	}
	public void setDeviceFirmWareVersion(Long deviceFirmWareVersion) {
		this.deviceFirmWareVersion = deviceFirmWareVersion;
	}
	public List<String> getDeviceNoListInclude() {
		return deviceNoListInclude;
	}
	public void setDeviceNoListInclude(List<String> deviceNoListInclude) {
		this.deviceNoListInclude = deviceNoListInclude;
	}
	public List<String> getDeviceNoListExclude() {
		return deviceNoListExclude;
	}
	public void setDeviceNoListExclude(List<String> deviceNoListExclude) {
		this.deviceNoListExclude = deviceNoListExclude;
	}
	public String getFirmWareId() {
		return firmWareId;
	}
	public void setFirmWareId(String firmWareId) {
		this.firmWareId = firmWareId;
	}
	public String getFirmWareMd5() {
		return firmWareMd5;
	}
	public void setFirmWareMd5(String firmWareMd5) {
		this.firmWareMd5 = firmWareMd5;
	}
	public Integer getFirmWareSize() {
		return firmWareSize;
	}
	public void setFirmWareSize(Integer firmWareSize) {
		this.firmWareSize = firmWareSize;
	}
	public Integer getDownloadTimes() {
		return downloadTimes;
	}
	public void setDownloadTimes(Integer downloadTimes) {
		this.downloadTimes = downloadTimes;
	}
	public Instant getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Instant createTime) {
		this.createTime = createTime;
	}
	public Instant getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Instant modifyTime) {
		this.modifyTime = modifyTime;
	}
	public OTAUpgradeStrategy getUpgradeStrategy() {
		return upgradeStrategy;
	}
	public void setUpgradeStrategy(OTAUpgradeStrategy upgradeStrategy) {
		this.upgradeStrategy = upgradeStrategy;
	}
	public OTAUpgradeScope getUpgradeScope() {
		return upgradeScope;
	}
	public void setUpgradeScope(OTAUpgradeScope upgradeScope) {
		this.upgradeScope = upgradeScope;
	}
	public enum OTAUpgradeStrategy{
    	FORCE_SAME,
    	IGNORE_SAME
    }
    public enum OTAUpgradeScope{
    	NONE,
    	ALL,
    	EXCEPT_EXCLUDE,
    	INCLUDE_ONLY
    }
}
