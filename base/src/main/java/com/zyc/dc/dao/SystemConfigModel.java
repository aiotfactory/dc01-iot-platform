package com.zyc.dc.dao;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "systemConfig")
public class SystemConfigModel {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String value;
    private Instant createTime;
    private Instant modifyTime;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
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
}
