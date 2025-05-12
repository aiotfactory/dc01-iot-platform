package com.zyc.dc.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projectBuild")
public class ProjectBuildModel {
    @Id
    private String id;
    private String userId;
    private String projectId;
    private Date createTime;
    private Date updateTime;
    private Date buildStartTime;
    private Date buildEndTime;
    private String binFile;
    private String elfFile;
    private String mapFile;
    private Long binVersion;
    private List<BuildLogModel> logs;
    private ProjectBuildResultType resultType;
    private Long codeVersion;
    

	public Long getCodeVersion() {
		return codeVersion;
	}

	public void setCodeVersion(Long codeVersion) {
		this.codeVersion = codeVersion;
	}

	public Long getBinVersion() {
		return binVersion;
	}

	public void setBinVersion(Long binVersion) {
		this.binVersion = binVersion;
	}

	public String getBinFile() {
		return binFile;
	}

	public void setBinFile(String binFile) {
		this.binFile = binFile;
	}

	public String getElfFile() {
		return elfFile;
	}

	public void setElfFile(String elfFile) {
		this.elfFile = elfFile;
	}

	public String getMapFile() {
		return mapFile;
	}

	public void setMapFile(String mapFile) {
		this.mapFile = mapFile;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public List<BuildLogModel> getLogs() {
		return logs;
	}

	public void setLogs(List<BuildLogModel> logs) {
		this.logs = logs;
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

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getBuildStartTime() {
		return buildStartTime;
	}

	public void setBuildStartTime(Date buildStartTime) {
		this.buildStartTime = buildStartTime;
	}

	public Date getBuildEndTime() {
		return buildEndTime;
	}

	public void setBuildEndTime(Date buildEndTime) {
		this.buildEndTime = buildEndTime;
	}

	public ProjectBuildResultType getResultType() {
		return resultType;
	}

	public void setResultType(ProjectBuildResultType resultType) {
		this.resultType = resultType;
	}

	public enum ProjectBuildResultType{
    	PENDING,
    	BUILDING,
    	ERROR,
    	SUCCESS,
    	CANCEL
    }
	
    public static class BuildLogModel {
    	private String content;
		private Date createTime;
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public Date getCreateTime() {
			return createTime;
		}
		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}
		
    }
}
