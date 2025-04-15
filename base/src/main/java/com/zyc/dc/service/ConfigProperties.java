package com.zyc.dc.service;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zyc.dc.dao.ProjectAccessModel;
import com.zyc.dc.dao.UserModel;

@Component
public class ConfigProperties {
	@Autowired
	private MongoDBService mongoDBService;
	@Value("${md5.keys.password}")
    private String MD5_PASSWORD;
	@Value("${md5.keys.api}")
    private String MD5_API;
	@Value("${constant.cookie.default-max-time}")
    private Integer COOKIE_DEFAULT_TIME;
	@Value("${constant.system.admin-user}")
    private String ADMIN_USER;
	@Value("${constant.system.admin-user-password}")
    private String ADMIN_USER_PASSWORD;
	@Value("${constant.session.non-remember-time}")	
    private Integer SESSION_NON_REMEMBER_TIME;
	@Value("${constant.password.valid}")	
    private Integer PASSWORD_VALID;
	@Value("${constant.login.valid}")	
    private Integer LOGIN_VALID;	
	@Value("${tcp.pic-path}")
    private String PIC_PATH;
	@Value("${tcp.port}")
    private Integer	TCP_PORT;
	@Value("${env.production}")
    private Integer	ENV_PRODUCTION;
	@Value("${spring.mail.username}")
	private String SITE_EMAIL;
	@Value("${constant.site-url}")
	private String SITE_URL;
	@Value("${constant.ota.server}")
	private String OTA_SERVER;
	@Value("${constant.ota.port}")
	private Integer OTA_PORT;
	@Value("${constant.ota.uri}")
	private String OTA_URI;
	@Value("${env.test-data}")
	private Boolean TEST_DATA;
		    	
		    	
	private UserModel userModel;
    public UserModel ADMIN_USER_MODEL() 
    {
    	if(userModel==null)
    	{
    		synchronized (this) 
    		{
    			if(userModel==null) {
		    		userModel=mongoDBService.findOneByField("login", ADMIN_USER(), UserModel.class);
					//add user
					if(userModel==null)
					{
						userModel=new UserModel();
						userModel.setName("admin");
						userModel.setLogin(ADMIN_USER());
						userModel.setPasswordType(UserModel.PasswordType.CHANGED);
						userModel.setRegisterTime(Instant.now());
						userModel.setUserStatus(UserModel.UserStatus.ENABLED);
						userModel.setUserType(UserModel.UserType.ADMIN);
						userModel.setPassword(MiscUtil.userPasswordEncode(ADMIN_USER_PASSWORD()));
						userModel.setBuildProjectLimitPerDay(2000000000);
						userModel.setOtaLimit(2000000000);
						userModel.setProjectAccessType(ProjectAccessModel.ProjectAccessType.LIMIT_WRITE);
						mongoDBService.save("init admin user",userModel);
					}
    			}
    		}
    	}
    	return userModel;
    }
    public String OTA_SERVER() {
        return OTA_SERVER;
    }
    public Boolean TEST_DATA() {
        return TEST_DATA;
    }
    public String OTA_URI() {
        return OTA_URI;
    }
    public Integer OTA_PORT() {
        return OTA_PORT;
    }
    public String SITE_EMAIL() {
        return SITE_EMAIL;
    }   
    public String SITE_URL() {
        return SITE_URL;
    }    
    public Integer LOGIN_VALID() {
        return LOGIN_VALID;
    }
    public Integer PASSWORD_VALID() {
        return PASSWORD_VALID;
    }
    public Integer SESSION_NON_REMEMBER_TIME() {
        return SESSION_NON_REMEMBER_TIME;
    }
    public Integer ENV_PRODUCTION() {
        return ENV_PRODUCTION;
    }
    public String PIC_PATH() {
        return PIC_PATH;
    }
    public Integer TCP_PORT() {
        return TCP_PORT;
    }
    public String MD5_PASSWORD() {
        return MD5_PASSWORD;
    }
    public Integer COOKIE_DEFAULT_TIME() {
        return COOKIE_DEFAULT_TIME;
    }
    public String ADMIN_USER() {
        return ADMIN_USER;
    }
    public String ADMIN_USER_PASSWORD() {
        return ADMIN_USER_PASSWORD;
    }
    public String MD5_API() {
        return MD5_API;
    }
}
