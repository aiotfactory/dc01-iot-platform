package com.zyc.dc.dao;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deviceConfigElement")
public class DeviceConfigElementModel {
    @Id
    private String id;
    private String deviceId;
	private Integer idex;
	private Integer module;
	private Integer type;
	private String name;
	private Integer valueLen;
	private String valueInDevice;
	private String valueInDb;
	private Integer status;
	private Long minValue;
	private Long maxValue;
	private List<Integer> enumValue;
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getMinValue() {
		return minValue;
	}
	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}
	public Long getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}
	public List<Integer> getEnumValue() {
		return enumValue;
	}
	public void setEnumValue(List<Integer> enumValue) {
		this.enumValue = enumValue;
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
	public Integer getModule() {
		return module;
	}
	public void setModule(Integer module) {
		this.module = module;
	}
	public Integer getIdex() {
		return idex;
	}
	public void setIdex(Integer idex) {
		this.idex = idex;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getValueLen() {
		return valueLen;
	}
	public void setValueLen(Integer valueLen) {
		this.valueLen = valueLen;
	}
	public String getValueInDevice() {
		return valueInDevice;
	}
	public void setValueInDevice(String valueInDevice) {
		this.valueInDevice = valueInDevice;
	}
	public String getValueInDb() {
		return valueInDb;
	}
	public void setValueInDb(String valueInDb) {
		this.valueInDb = valueInDb;
	}  
}