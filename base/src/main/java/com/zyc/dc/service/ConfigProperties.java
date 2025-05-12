package com.zyc.dc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfigProperties {
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
	@Value("${constant.data.sensor.keep-days}")
	private Integer SENSOR_DATA_KEEP_DAYS;	    	
	@Value("${constant.data.user-log.keep-days}")
	private Integer USER_LOG_KEEP_DAYS;	
	@Value("${constant.data.module-log.keep-days}")
	private Integer MODULE_LOG_KEEP_DAYS;
	@Value("${constant.clean.mongo-seconds}")
	private Integer CLEAN_MONGO_INTERVAL;
	
		    
	public Integer CLEAN_MONGO_INTERVAL() {
    	return CLEAN_MONGO_INTERVAL*1000;
    }	    
    public Integer USER_LOG_KEEP_DAYS() {
    	return USER_LOG_KEEP_DAYS;
    }
    public Integer MODULE_LOG_KEEP_DAYS() {
    	return MODULE_LOG_KEEP_DAYS;
    }
    public Integer SENSOR_DATA_KEEP_DAYS() {
    	return SENSOR_DATA_KEEP_DAYS;
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
