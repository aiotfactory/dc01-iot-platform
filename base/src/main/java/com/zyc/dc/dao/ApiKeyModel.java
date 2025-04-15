package com.zyc.dc.dao;

import java.time.Instant;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "apiKey")
public class ApiKeyModel {
    @Id
    private String id;
    private String key;
    private String userId;
    private UserModel.UserStatus userStatus;
    private String name;
    private Integer timesPerMinute;
    private Integer timesPerDay;
    private Integer limitPerMinute;
    private Integer limitPerDay;
    private String forwardUrl;
    private Boolean allowForward;
    private Map<String,LimitModel> apiLimitMap;
    private Map<String,CallModel> apiCallMap;
	private Long callTimes;
	private Instant createTime;
	private Instant refreshTime;
	private Instant latestCallTime;
	private String forwardEmail;
	private String forwardEmailRule;
	private String forwardEmailVerificationCode;
	private Integer forwardEmailStatus;//0:no email, 1:pending verification, 2: normal work, 3:pause
	private Integer forwardEmailLimitPerDay;
	private Integer forwardEmailTimesPerDay;
	private Instant forwardEmailVerificationTime;
	private Instant forwardEmailTime;
	
    public Instant getForwardEmailVerificationTime() {
		return forwardEmailVerificationTime;
	}
	public void setForwardEmailVerificationTime(Instant forwardEmailVerificationTime) {
		this.forwardEmailVerificationTime = forwardEmailVerificationTime;
	}
	public String getForwardEmailVerificationCode() {
		return forwardEmailVerificationCode;
	}
	public void setForwardEmailVerificationCode(String forwardEmailVerificationCode) {
		this.forwardEmailVerificationCode = forwardEmailVerificationCode;
	}
	public String getForwardEmail() {
		return forwardEmail;
	}
	public void setForwardEmail(String forwardEmail) {
		this.forwardEmail = forwardEmail;
	}
	public String getForwardEmailRule() {
		return forwardEmailRule;
	}
	public void setForwardEmailRule(String forwardEmailRule) {
		this.forwardEmailRule = forwardEmailRule;
	}
	public Integer getForwardEmailStatus() {
		return forwardEmailStatus;
	}
	public void setForwardEmailStatus(Integer forwardEmailStatus) {
		this.forwardEmailStatus = forwardEmailStatus;
	}
	public Integer getForwardEmailLimitPerDay() {
		return forwardEmailLimitPerDay;
	}
	public void setForwardEmailLimitPerDay(Integer forwardEmailLimitPerDay) {
		this.forwardEmailLimitPerDay = forwardEmailLimitPerDay;
	}
	public Integer getForwardEmailTimesPerDay() {
		return forwardEmailTimesPerDay;
	}
	public void setForwardEmailTimesPerDay(Integer forwardEmailTimesPerDay) {
		this.forwardEmailTimesPerDay = forwardEmailTimesPerDay;
	}
	public Instant getForwardEmailTime() {
		return forwardEmailTime;
	}
	public void setForwardEmailTime(Instant forwardEmailTime) {
		this.forwardEmailTime = forwardEmailTime;
	}
	public Boolean getAllowForward() {
		return allowForward;
	}
	public void setAllowForward(Boolean allowForward) {
		this.allowForward = allowForward;
	}
	public String getForwardUrl() {
		return forwardUrl;
	}
	public void setForwardUrl(String forwardUrl) {
		this.forwardUrl = forwardUrl;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public UserModel.UserStatus getUserStatus() {
		return userStatus;
	}
	public void setUserStatus(UserModel.UserStatus userStatus) {
		this.userStatus = userStatus;
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
	public Map<String, CallModel> getApiCallMap() {
		return apiCallMap;
	}

	public void setApiCallMap(Map<String, CallModel> apiCallMap) {
		this.apiCallMap = apiCallMap;
	}

	public Integer getTimesPerDay() {
		return timesPerDay;
	}

	public void setTimesPerDay(Integer timesPerDay) {
		this.timesPerDay = timesPerDay;
	}

	public Integer getLimitPerDay() {
		return limitPerDay;
	}

	public void setLimitPerDay(Integer limitPerDay) {
		this.limitPerDay = limitPerDay;
	}
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Integer getTimesPerMinute() {
		return timesPerMinute;
	}

	public void setTimesPerMinute(Integer timesPerMinute) {
		this.timesPerMinute = timesPerMinute;
	}


	public Integer getLimitPerMinute() {
		return limitPerMinute;
	}

	public void setLimitPerMinute(Integer limitPerMinute) {
		this.limitPerMinute = limitPerMinute;
	}



	public Map<String, LimitModel> getApiLimitMap() {
		return apiLimitMap;
	}

	public void setApiLimitMap(Map<String, LimitModel> apiLimitMap) {
		this.apiLimitMap = apiLimitMap;
	}

	public Long getCallTimes() {
		return callTimes;
	}

	public void setCallTimes(Long callTimes) {
		this.callTimes = callTimes;
	}

	public Instant getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Instant createTime) {
		this.createTime = createTime;
	}

	public Instant getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(Instant refreshTime) {
		this.refreshTime = refreshTime;
	}

	public Instant getLatestCallTime() {
		return latestCallTime;
	}

	public void setLatestCallTime(Instant latestCallTime) {
		this.latestCallTime = latestCallTime;
	}
	public static class CallModel { 
		private String apiName;
	    private Integer callTimes;
	    private Integer timesPerMinute;
	    private Integer timesPerDay;
		private Instant latestCallTime;
		public Integer getTimesPerMinute() {
			return timesPerMinute;
		}
		public void setTimesPerMinute(Integer timesPerMinute) {
			this.timesPerMinute = timesPerMinute;
		}
		public Integer getTimesPerDay() {
			return timesPerDay;
		}
		public void setTimesPerDay(Integer timesPerDay) {
			this.timesPerDay = timesPerDay;
		}
		public String getApiName() {
			return apiName;
		}
		public void setApiName(String apiName) {
			this.apiName = apiName;
		}
		public Integer getCallTimes() {
			return callTimes;
		}
		public void setCallTimes(Integer callTimes) {
			this.callTimes = callTimes;
		}
		public Instant getLatestCallTime() {
			return latestCallTime;
		}
		public void setLatestCallTime(Instant latestCallTime) {
			this.latestCallTime = latestCallTime;
		}

		
	}
	
	public static class LimitModel { 
		private String apiName;
	    private Integer limitPerMinute;
	    private Integer limitPerDay;
		public String getApiName() {
			return apiName;
		}
		public void setApiName(String apiName) {
			this.apiName = apiName;
		}
		public Integer getLimitPerMinute() {
			return limitPerMinute;
		}
		public void setLimitPerMinute(Integer limitPerMinute) {
			this.limitPerMinute = limitPerMinute;
		}
		public Integer getLimitPerDay() {
			return limitPerDay;
		}
		public void setLimitPerDay(Integer limitPerDay) {
			this.limitPerDay = limitPerDay;
		}
		
    }
}
