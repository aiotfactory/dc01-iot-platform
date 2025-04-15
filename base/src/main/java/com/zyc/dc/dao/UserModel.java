package com.zyc.dc.dao;
import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user")
public class UserModel {
    @Id
    private String id;
    @Indexed
    private String login;
    private String name;
    private String password;
    private String cellPhone;
    private String email;
    private String latestIp;
    private String parentId;
    private String createById;
	private Integer otaLimit;
	private Integer buildProjectLimitPerDay;
	private Integer buildProjectTimesPerDay;
    private Instant buildProjectTime;
    private String  emailActivationToken;
    private Instant emailActivationReqTime;
    private Integer emailActivationReqTimes;
    private Instant emailActivationRspTime;
    private boolean emailActivationRspResult;
    private String  verificationCode;
    private Instant verificationCodeReqTime;
    private Integer verificationCodeReqTimes;
    private Instant verificationCodeRspTime;
    private boolean verificationCodeRspResult;
    private Integer loginFailedTimes;
    private Instant loginFailedTime;
    private Integer loginTimes;
    private Instant loginTime;
    private Instant registerTime;
    private Instant latestVisitTime;
    private Instant passWordChangeTime;
    private Instant loginChangeTime;
    private UserStatus userStatus;
    private UserType userType;
    private PasswordType passwordType;
    private UserCloneModel clonedUser;
    private String locale;
    private UserSmtpModel smtp;
    private ProjectAccessModel.ProjectAccessType projectAccessType;
    
	public ProjectAccessModel.ProjectAccessType getProjectAccessType() {
		return projectAccessType;
	}
	public void setProjectAccessType(ProjectAccessModel.ProjectAccessType projectAccessType) {
		this.projectAccessType = projectAccessType;
	}
	public Integer getOtaLimit() {
		return otaLimit;
	}
	public void setOtaLimit(Integer otaLimit) {
		this.otaLimit = otaLimit;
	}
	public UserSmtpModel getSmtp() {
		return smtp;
	}
	public void setSmtp(UserSmtpModel smtp) {
		this.smtp = smtp;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public Instant getBuildProjectTime() {
		return buildProjectTime;
	}
	public void setBuildProjectTime(Instant buildProjectTime) {
		this.buildProjectTime = buildProjectTime;
	}
	public Integer getBuildProjectLimitPerDay() {
		return buildProjectLimitPerDay;
	}
	public void setBuildProjectLimitPerDay(Integer buildProjectLimitPerDay) {
		this.buildProjectLimitPerDay = buildProjectLimitPerDay;
	}
	public Integer getBuildProjectTimesPerDay() {
		return buildProjectTimesPerDay;
	}
	public void setBuildProjectTimesPerDay(Integer buildProjectTimesPerDay) {
		this.buildProjectTimesPerDay = buildProjectTimesPerDay;
	}
	public UserCloneModel getClonedUser() {
		return clonedUser;
	}
	public void setClonedUser(UserCloneModel clonedUser) {
		this.clonedUser = clonedUser;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getCreateById() {
		return createById;
	}
	public void setCreateById(String createById) {
		this.createById = createById;
	}
	public String getVerificationCode() {
		return verificationCode;
	}
	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
	public Instant getVerificationCodeReqTime() {
		return verificationCodeReqTime;
	}
	public void setVerificationCodeReqTime(Instant verificationCodeReqTime) {
		this.verificationCodeReqTime = verificationCodeReqTime;
	}
	public Integer getVerificationCodeReqTimes() {
		return verificationCodeReqTimes;
	}
	public void setVerificationCodeReqTimes(Integer verificationCodeReqTimes) {
		this.verificationCodeReqTimes = verificationCodeReqTimes;
	}
	public Instant getVerificationCodeRspTime() {
		return verificationCodeRspTime;
	}
	public void setVerificationCodeRspTime(Instant verificationCodeRspTime) {
		this.verificationCodeRspTime = verificationCodeRspTime;
	}
	public boolean isVerificationCodeRspResult() {
		return verificationCodeRspResult;
	}
	public void setVerificationCodeRspResult(boolean verificationCodeRspResult) {
		this.verificationCodeRspResult = verificationCodeRspResult;
	}
	public String getEmailActivationToken() {
		return emailActivationToken;
	}
	public void setEmailActivationToken(String emailActivationToken) {
		this.emailActivationToken = emailActivationToken;
	}
	public Instant getEmailActivationReqTime() {
		return emailActivationReqTime;
	}
	public void setEmailActivationReqTime(Instant emailActivationReqTime) {
		this.emailActivationReqTime = emailActivationReqTime;
	}
	public Integer getEmailActivationReqTimes() {
		return emailActivationReqTimes;
	}
	public void setEmailActivationReqTimes(Integer emailActivationReqTimes) {
		this.emailActivationReqTimes = emailActivationReqTimes;
	}
	public Instant getEmailActivationRspTime() {
		return emailActivationRspTime;
	}
	public void setEmailActivationRspTime(Instant emailActivationRspTime) {
		this.emailActivationRspTime = emailActivationRspTime;
	}
	public boolean isEmailActivationRspResult() {
		return emailActivationRspResult;
	}
	public void setEmailActivationRspResult(boolean emailActivationRspResult) {
		this.emailActivationRspResult = emailActivationRspResult;
	}
	public Instant getPassWordChangeTime() {
		return passWordChangeTime;
	}
	public void setPassWordChangeTime(Instant passWordChangeTime) {
		this.passWordChangeTime = passWordChangeTime;
	}
	public Instant getLoginChangeTime() {
		return loginChangeTime;
	}
	public void setLoginChangeTime(Instant loginChangeTime) {
		this.loginChangeTime = loginChangeTime;
	}
	public Instant getLatestVisitTime() {
		return latestVisitTime;
	}
	public void setLatestVisitTime(Instant latestVisitTime) {
		this.latestVisitTime = latestVisitTime;
	}
	public Instant getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Instant loginTime) {
		this.loginTime = loginTime;
	}
	public Integer getLoginFailedTimes() {
		return loginFailedTimes;
	}
	public void setLoginFailedTimes(Integer loginFailedTimes) {
		this.loginFailedTimes = loginFailedTimes;
	}
	public Instant getLoginFailedTime() {
		return loginFailedTime;
	}
	public void setLoginFailedTime(Instant loginFailedTime) {
		this.loginFailedTime = loginFailedTime;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCellPhone() {
		return cellPhone;
	}
	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getLatestIp() {
		return latestIp;
	}
	public void setLatestIp(String latestIp) {
		this.latestIp = latestIp;
	}
	public Integer getLoginTimes() {
		return loginTimes;
	}
	public void setLoginTimes(Integer loginTimes) {
		this.loginTimes = loginTimes;
	}
	public Instant getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(Instant registerTime) {
		this.registerTime = registerTime;
	}
	public UserStatus getUserStatus() {
		return userStatus;
	}
	public void setUserStatus(UserStatus userStatus) {
		this.userStatus = userStatus;
	}
	public UserType getUserType() {
		return userType;
	}
	public void setUserType(UserType userType) {
		this.userType = userType;
	}
	public PasswordType getPasswordType() {
		return passwordType;
	}
	public void setPasswordType(PasswordType passwordType) {
		this.passwordType = passwordType;
	}
	public enum UserType {
	    PERSONAL,
	    PROFESSIONAL,
	    ENTERPRISE,
	    ADMIN
	}
    public enum UserStatus{
    	ENABLED,
    	DISABLED,
    	FREEZED,
    	WAIT_ACTIVATION
    }
    public enum PasswordType{
    	INIT,
    	CHANGED
    }
    public static class UserCloneModel {
    	private List<String> loginList;
    	private List<String> userIdList;
		public List<String> getLoginList() {
			return loginList;
		}
		public void setLoginList(List<String> loginList) {
			this.loginList = loginList;
		}
		public List<String> getUserIdList() {
			return userIdList;
		}
		public void setUserIdList(List<String> userIdList) {
			this.userIdList = userIdList;
		}
    }
    public static class UserSmtpModel {
    	private String host;
    	private Integer port;
    	private String userName;
    	private String password;
    	private Integer smtpAuth;
    	private Integer smtpStartTlsEnable;
    	private Integer smtpSslEnable;
    	
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public Integer getPort() {
			return port;
		}
		public void setPort(Integer port) {
			this.port = port;
		}
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public Integer getSmtpAuth() {
			return smtpAuth;
		}
		public void setSmtpAuth(Integer smtpAuth) {
			this.smtpAuth = smtpAuth;
		}
		public Integer getSmtpStartTlsEnable() {
			return smtpStartTlsEnable;
		}
		public void setSmtpStartTlsEnable(Integer smtpStartTlsEnable) {
			this.smtpStartTlsEnable = smtpStartTlsEnable;
		}
		public Integer getSmtpSslEnable() {
			return smtpSslEnable;
		}
		public void setSmtpSslEnable(Integer smtpSslEnable) {
			this.smtpSslEnable = smtpSslEnable;
		}
    }
}
