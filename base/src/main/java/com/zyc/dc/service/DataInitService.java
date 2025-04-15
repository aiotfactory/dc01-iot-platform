package com.zyc.dc.service;

import java.time.Instant;
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
			moduleInfo1.setModifyTime(Instant.now());
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
	/*
	public void setProjectToUser(UserModel user, ProjectAccessModel.ProjectAccessType accessType)
	{
		ProjectInfoModel projectInfoModel=mongoDBService.findOneByField("name", "dc01", ProjectInfoModel.class);
		ProjectAccessModel accessAdmin=mongoDBService.findOneByField("accessType", accessType, ProjectAccessModel.class);
	    UserModel.UserProjectModel userProjectModel=new UserModel.UserProjectModel();
	    userProjectModel.setCreateTime(Instant.now());
	    userProjectModel.setProjectAccessId(accessAdmin.getId());
	    userProjectModel.setProjectInfoId(projectInfoModel.getId());
	    user.setProjects(new ArrayList<UserModel.UserProjectModel>());
	    user.getProjects().add(userProjectModel);
	    mongoDBService.save("init data", user);
	}
	*/
	public void init()
	{
		configProperties.ADMIN_USER_MODEL();//create admin user
		initProject("dc01-v01");
		if(configProperties.TEST_DATA())
			initTestData(configProperties.ADMIN_USER_MODEL());
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
			accessModel.setCreateTime(Instant.now());
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
			projectInfoModel.setCreateTime(Instant.now());
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
		{
			deviceModel=new DeviceModel();
			deviceModel.setId("67340ecab2707d5b339cb2cd");
			deviceModel.setDeviceNo(testDeviceNo);
			deviceModel.setCsqCurrent(0);
			deviceModel.setCsqLow(0);
			deviceModel.setCsqMax(0);
			deviceModel.setAlivePackSeqDevice(0L);
			deviceModel.setAlivePackSeqTCP(0L);
			deviceModel.setAliveTimesReconn(0L);
			deviceModel.setAliveRunSeconds(0L);
			deviceModel.setTimesReconn(0L);
			deviceModel.setTimesRestart(0L);
			deviceModel.setTimesUpload(0L);
			deviceModel.setRunSeconds(0L);
			deviceModel.setAliveTimesMeta(0L);
			deviceModel.setDeviceType(DeviceModel.DeviceType.DC01);
			deviceModel.setUserId(user.getId());
			deviceModel.setDeviceStatus(DeviceModel.DeviceStatus.ENABLED);
			mongoDBService.save("init test data", deviceModel);
		}
		mongoDBService.save("initmodule",deviceModel);
		setModuleToDevice(deviceModel);

		//add device test data
		for(int i=1;i<3;i++)
		{
			String deviceNo=testDeviceNo.substring(0,testDeviceNo.length()-1)+i;
			deviceModel=mongoDBService.findOneByField("deviceNo", deviceNo, DeviceModel.class);
			if(deviceModel==null)
			{
				deviceModel=new DeviceModel();
				deviceModel.setDeviceNo(deviceNo);
				deviceModel.setDeviceToken("123456");
				mongoDBService.save("initdevice",deviceModel);
			}
		}		
		//setProjectToUser(user, ProjectAccessModel.ProjectAccessType.ALL_WRITE);
	}
}
