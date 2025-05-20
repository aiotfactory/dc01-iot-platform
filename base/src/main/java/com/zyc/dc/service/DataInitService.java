package com.zyc.dc.service;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.dao.ProjectAccessModel;
import com.zyc.dc.dao.ProjectInfoModel;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.service.module.ModuleDefine;

@Service
public class DataInitService {
	@Autowired
	private ConfigProperties configProperties;
	@Autowired
	private MongoDBService mongoDBService;
	
	private static final Logger logger = LoggerFactory.getLogger(DataInitService.class);
	private void initDbAddModule(String deviceId,ModuleDefine.ModuleType moduleType,Map<Integer,ModuleInfoModel> moduleInfoModelMap)
	{
		if(!moduleInfoModelMap.containsKey(moduleType.getId()))
		{
			ModuleInfoModel moduleInfo1=new ModuleInfoModel();
			moduleInfo1.setModuleTypeId(moduleType.getId());
			moduleInfo1.setModifyTime(new Date());
			moduleInfo1.setDeviceId(deviceId);
			moduleInfo1.setAllowForward(false);
			moduleInfoModelMap.put(moduleType.getId(), moduleInfo1);
		}
	}
	public void setModuleToDevice(DeviceModel deviceModel)
	{
		String deviceId=deviceModel.getId();
		Map<Integer,ModuleInfoModel> moduleInfoModelMapInfo=deviceModel.getModuleInfoModelMap();
		if(moduleInfoModelMapInfo.size()==0)
		{
			Map<Integer,ModuleInfoModel> moduleInfoModelMap=new HashMap<>();
			switch(deviceModel.getDeviceType()) 
			{
				case DeviceModel.DeviceType.DC01:
					initDbAddModule(deviceId,ModuleDefine.MODULE_GENERAL,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_4G,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_SPI,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_TM7705,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_CAMERA,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_META,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_CONFIG,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_WIFI,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_W5500,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_GPIO,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_UART,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_LORA,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_I2C,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_BATADC,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_FORWARD,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_CLOUDLOG,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_AHT20,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_SPL06,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_RS485,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_PIR,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_THERMAL,moduleInfoModelMap);
					initDbAddModule(deviceId,ModuleDefine.MODULE_ULTRASONIC,moduleInfoModelMap);
					break;
				default:
					break;
			}
			if(moduleInfoModelMap.size()>0) {
				mongoDBService.updateBatch(new ArrayList<>(moduleInfoModelMap.values()));
				List<ModuleInfoModel> list=mongoDBService.findByField("deviceId",deviceId,ModuleInfoModel.class);
				if(list!=null) {
					moduleInfoModelMap=list.stream().collect(Collectors.toMap(ModuleInfoModel::getModuleTypeId, element -> element));
					deviceModel.setModuleInfoModelMap(moduleInfoModelMap);
				}
			}
		}
	}
	public DeviceModel deviceCreate(String deviceId,String ip,Integer csq,Long firmVersion,DeviceModel.DeviceType deviceType,String deviceNo)
	{
		Date cur=new Date();
		DeviceModel deviceModel=new DeviceModel();
		if(deviceId!=null)
			deviceModel.setId(deviceId);
		deviceModel.setCreateTime(cur);
		deviceModel.setClientIp(ip);
		deviceModel.setForwardStatus(false);
		//deviceModel.setUserId(configProperties.DEFAULT_DEVICE_USER());
		deviceModel.setCsqCurrent(csq);
		deviceModel.setCsqLow(csq);
		deviceModel.setCsqMax(csq);
		deviceModel.setUploadTime(cur);
		deviceModel.setDeviceFirmWareVersion(firmVersion);
		deviceModel.setAlivePackSeqDevice(0L);
		deviceModel.setAlivePackSeqTCP(0L);
		deviceModel.setAliveTimesReconn(0L);
		deviceModel.setAliveRunSeconds(0L);
		deviceModel.setTimesReconn(0L);
		deviceModel.setTimesRestart(0L);
		deviceModel.setTimesUpload(0L);
		deviceModel.setRunSeconds(0L);
		deviceModel.setAliveTimesMeta(0L);
		deviceModel.setDeviceType(deviceType);
		deviceModel.setDeviceNo(deviceNo);
		deviceModel.setDeviceStatus(DeviceModel.DeviceStatus.ENABLED);
		deviceModel.setUserId(ADMIN_USER_MODEL().getId());
		deviceModel.setModuleRunInfo(new HashMap<>());
		mongoDBService.save("utilDeviceCreate", deviceModel);
		setModuleToDevice(deviceModel);
		return deviceModel;
	}
	public void init()
	{
		ADMIN_USER_MODEL();//create admin user
		initProject("dc01-v01");
		if(configProperties.TEST_DATA())
			initTestData(ADMIN_USER_MODEL());
		logger.info("data init done");
	}
	private ProjectAccessModel initProjectAccess(String name,String projectId,ProjectAccessModel.ProjectAccessType accessType,Set<String> fileFilters)
	{
		Criteria queryCriteria = Criteria.where("projectId").is(projectId).and("accessType").is(accessType).and("name").is(name);
		ProjectAccessModel accessModel=mongoDBService.findOneByFields(queryCriteria, null,null,ProjectAccessModel.class);
		if(accessModel==null) 
		{
			accessModel=new ProjectAccessModel();
			accessModel.setProjectId(projectId);
			accessModel.setAccessType(accessType);
			accessModel.setCreateTime(new Date());
			accessModel.setName(name);
			accessModel.setFilters(fileFilters);
			mongoDBService.save("init data", accessModel);
		}
		return accessModel;
	}
	private void initProject(String projectName)
	{
		ProjectInfoModel projectInfoModel=mongoDBService.findOneByField("name", projectName, ProjectInfoModel.class);
		if(projectInfoModel==null)
		{
			projectInfoModel=new ProjectInfoModel();
			projectInfoModel.setCreateTime(new Date());
			projectInfoModel.setName(projectName);
			projectInfoModel.setBinName("dc01");
			projectInfoModel.setStatus(ProjectInfoModel.ProjectStatus.NORMAL);
		}
		if(configProperties.ENV_PRODUCTION()==0)//dev
			projectInfoModel.setPath("D:\\temp1\\"+projectName+"\\");
		else
			projectInfoModel.setPath("/opt/esp/projects/"+projectName+"/");
		mongoDBService.save("init data", projectInfoModel);		
		
		//ProjectAccessModel accessAdmin=initProjectAccess("Admin",ProjectAccessModel.ProjectAccessType.ADMIN,null);
		Set<String> fileFilters=new HashSet<>();		
		fileFilters.add("/bak");
		fileFilters.add("/build");
		fileFilters.add("/common_components");
		fileFilters.add("/private_components");
		fileFilters.add("/managed_components");
		fileFilters.add("/.clangd");
		fileFilters.add("/.cproject");
		fileFilters.add("/build.sh");
		fileFilters.add("/.project");
		fileFilters.add("/.clang-format");
		//fileFilters.add("/CMakeLists.txt");
		fileFilters.add("/dependencies.lock");
		fileFilters.add("/partitions.bak.csv");
		//fileFilters.add("/partitions.csv");
		fileFilters.add("/README.md");
		fileFilters.add("/README_cn.md");
		//fileFilters.add("/sdkconfig");
		fileFilters.add("/sdkconfig.ci.noweb");
		fileFilters.add("/sdkconfig.ci.web");
		fileFilters.add("/sdkconfig.defaults");
		fileFilters.add("/sdkconfig.defaults.esp32s3");
		fileFilters.add("/sdkconfig.old");
		//fileFilters.add("/main/CMakeLists.txt");
		//fileFilters.add("/main/idf_component.yml");
		//fileFilters.add("/main/Kconfig.projbuild");
		fileFilters.add("/main/build");
		fileFilters.add("/main/prebuilt");
		
		initProjectAccess("ReadAll",projectInfoModel.getId(), ProjectAccessModel.ProjectAccessType.ALL_READ,null);
		initProjectAccess("WriteAll",projectInfoModel.getId(), ProjectAccessModel.ProjectAccessType.ALL_WRITE,null);
		initProjectAccess("ReadLimit",projectInfoModel.getId(), ProjectAccessModel.ProjectAccessType.LIMIT_READ,fileFilters);
		initProjectAccess("WriteLimit",projectInfoModel.getId(), ProjectAccessModel.ProjectAccessType.LIMIT_WRITE,fileFilters);
	}
	private void initTestData(UserModel user) {
		//add a device
		String testDeviceNo="24587cd6ef0c";
		DeviceModel deviceModel=mongoDBService.findOneByField("id", "67340ecab2707d5b339cb2cd", DeviceModel.class);
		if(deviceModel==null)
			deviceCreate("67340ecab2707d5b339cb2cd",null,null,null,DeviceModel.DeviceType.DC01,testDeviceNo);
		mongoDBService.save("initmodule",deviceModel);
		setModuleToDevice(deviceModel);

		//add device test data
		for(int i=1;i<3;i++)
		{
			String deviceNo=testDeviceNo.substring(0,testDeviceNo.length()-1)+i;
			deviceModel=mongoDBService.findOneByField("deviceNo", deviceNo, DeviceModel.class);
			if(deviceModel==null)
			{
				deviceModel=deviceCreate(null,null,null,null,DeviceModel.DeviceType.DC01,deviceNo);
				deviceModel.setDeviceToken("123456");
				mongoDBService.save("init test", deviceModel);
			}
		}		
		//setProjectToUser(user, ProjectAccessModel.ProjectAccessType.ALL_WRITE);
		
		UserModel userModel=mongoDBService.findOneByField("login", configProperties.ADMIN_USER(), UserModel.class);
		if(userModel!=null && (userModel.getAccountBalance()==null || userModel.getAccountBalance()==0))
		{
			userModel.setAccountBalance(100*MiscUtil.MONEY_DIVIDER);
			mongoDBService.save("init test", userModel);
		}
			
	}
	private UserModel userModel;	
    public UserModel ADMIN_USER_MODEL() 
    {
    	if(userModel==null)
    	{
    		synchronized (this) 
    		{
    			if(userModel==null) {
		    		userModel=mongoDBService.findOneByField("login", configProperties.ADMIN_USER(), UserModel.class);
					//add user
					if(userModel==null)
					{
						userModel=new UserModel();
						userModel.setName("admin");
						userModel.setLogin(configProperties.ADMIN_USER());
						userModel.setPasswordType(UserModel.PasswordType.CHANGED);
						userModel.setRegisterTime(new Date());
						userModel.setUserStatus(UserModel.UserStatus.ENABLED);
						userModel.setUserType(UserModel.UserType.ADMIN);
						userModel.setPassword(MiscUtil.userPasswordEncode(configProperties.ADMIN_USER_PASSWORD()));
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

    private Long deviceSleepCalc(List<Date[]> exeTimeList) {
        Date now = new Date();
        String currentTimeStr = MiscUtil.dateFormat(now, AIService.TIME_FORMAT);
        
        // This will store the earliest start time that is in the future
        Date closestStartTime = null;
        for (Date[] range : exeTimeList) {
            if (range.length != 2 || range[0] == null || range[1] == null)
                continue;

            String startStr = MiscUtil.dateFormat(range[0], AIService.TIME_FORMAT);
            String endStr = MiscUtil.dateFormat(range[1], AIService.TIME_FORMAT);

            int cmpStart = currentTimeStr.compareTo(startStr);
            int cmpEnd = currentTimeStr.compareTo(endStr);

            // If current time is within a working time range → no need to sleep
            if (cmpStart >= 0 && cmpEnd <= 0) {
                return null;
            }
            // If current time is before the start of this time window → track the earliest start
            if (cmpStart < 0) {
                if (closestStartTime == null || range[0].before(closestStartTime)) {
                    closestStartTime = range[0];
                }
            }
        }
        // If there is a future time window → sleep until it starts
        if (closestStartTime != null) {
            return MiscUtil.dateDiffIgnoreDate(now,closestStartTime);
        }

        // No valid time window found → no action
        return null;
    }
    public byte[] deviceSleep(DeviceModel deviceModel)
    {
    	if(deviceModel.getExeTime()==null || deviceModel.getExeTime().isEmpty())
    		return null;
    	Long diff= deviceSleepCalc(deviceModel.getExeTime());
    	if(diff!=null && diff>60)
    		return MiscUtil.uint322BytesLittleEndian(diff);
    	return null;
    }
}
