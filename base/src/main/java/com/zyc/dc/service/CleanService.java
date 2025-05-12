package com.zyc.dc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.zyc.dc.dao.AiCallModel;
import com.zyc.dc.dao.CaptchaModel;
import com.zyc.dc.dao.DataCacheModel;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataUserLogModel;
import com.zyc.dc.dao.ModuleRuleLogModel;
import com.zyc.dc.dao.OTADownloadModel;
import com.zyc.dc.dao.PostLogModel;
import com.zyc.dc.dao.UserSessionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Conditional(CleanServiceCondition.class) 
@Component
public class CleanService {
	@Autowired
	private MongoDBService mongoDBService;
	@Autowired
	private ConfigProperties configProperties;
	private int times=0;
    private static final Logger logger = LoggerFactory.getLogger(CleanService.class);
    
    @Scheduled(fixedRateString = "#{@configProperties.CLEAN_MONGO_INTERVAL()}")
    public void cleanupExpiredFiles() {
    	long time=System.currentTimeMillis();
    	logger.info("Clean start times "+times);
    	
    	mongoDBService.fileDelExpired();
        mongoDBService.deleteOld("uploadTime",configProperties.SENSOR_DATA_KEEP_DAYS() * 24 * 3600, DataCommModel.class);        
        mongoDBService.deleteOld("createTime",200, CaptchaModel.class);        
        mongoDBService.deleteOld("expiredTime",20, UserSessionModel.class);     
        mongoDBService.deleteOld("createTime",configProperties.MODULE_LOG_KEEP_DAYS() * 24 * 3600, AiCallModel.class);   
        mongoDBService.deleteOld("expiredTime",100, DataCacheModel.class);   
        mongoDBService.deleteOld("createTime",configProperties.USER_LOG_KEEP_DAYS() * 24 * 3600, DataUserLogModel.class);   
        mongoDBService.deleteOld("uploadTime", configProperties.MODULE_LOG_KEEP_DAYS() * 24 * 3600, ModuleRuleLogModel.class);   
        mongoDBService.deleteOld("expiredTime", 100, OTADownloadModel.class);   
        mongoDBService.deleteOld("createTime", configProperties.MODULE_LOG_KEEP_DAYS() * 24 * 3600, PostLogModel.class);   
    	        
        time=(System.currentTimeMillis()-time)/1000;
        logger.info("Clean end times "+times+" used seconds "+time);
        times++;
    }
}