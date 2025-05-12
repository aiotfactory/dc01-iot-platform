package com.zyc.dc.dao;

import java.util.Date;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projectAccess")
public class ProjectAccessModel {
    @Id
    private String id;
    private String projectId;
    private String name;
    private Set<String> filters;
    private ProjectAccessType accessType;
    private Date createTime;
	
    public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public Set<String> getFilters() {
		return filters;
	}

	public void setFilters(Set<String> filters) {
		this.filters = filters;
	}

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

	public ProjectAccessType getAccessType() {
		return accessType;
	}

	public void setAccessType(ProjectAccessType accessType) {
		this.accessType = accessType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public enum ProjectAccessType{
    	ADMIN,
    	ALL_READ,
    	ALL_WRITE,
    	LIMIT_READ,
    	LIMIT_WRITE
    }
}
