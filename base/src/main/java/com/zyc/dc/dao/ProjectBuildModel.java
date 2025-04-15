package com.zyc.dc.dao;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projectBuild")
public class ProjectBuildModel {
    @Id
    private String id;
    private String userId;
    private String projectId;
    private Instant createTime;
    private Instant updateTime;
    private Instant buildStartTime;
    private Instant buildEndTime;
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

	public Instant getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Instant updateTime) {
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

	public Instant getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Instant createTime) {
		this.createTime = createTime;
	}

	public Instant getBuildStartTime() {
		return buildStartTime;
	}

	public void setBuildStartTime(Instant buildStartTime) {
		this.buildStartTime = buildStartTime;
	}

	public Instant getBuildEndTime() {
		return buildEndTime;
	}

	public void setBuildEndTime(Instant buildEndTime) {
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
		private Instant createTime;
		public String getContent() {
			return content;
		}
		public void setContent(String content) {
			this.content = content;
		}
		public Instant getCreateTime() {
			return createTime;
		}
		public void setCreateTime(Instant createTime) {
			this.createTime = createTime;
		}
		
    }
}
