package com.zyc.dc.dao;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.zyc.dc.service.MongoDBService;
import com.zyc.dc.service.SpringUtil;

@Document(collection = "device")
public class DeviceModel {
    @Id
    private String id;
    private String userId;
    private String deviceNo;
    private String deviceToken;
    private String deviceName;
    private DeviceType deviceType;
    private Long deviceFirmWareVersion;
    private String imei;
    private String mac;
    private String clientIp;
    private String iccid;
    private Integer csqCurrent;
    private Integer csqLow;
    private Integer csqMax;
    private Long aliveRunSeconds;
    private Long alivePackSeqDevice;
    private Long alivePackSeqTCP;
    private Long aliveTimesReconn;
    private Long aliveTimesMeta;
    private Long timesRestart;
    private Long timesUpload;
    private Long timesReconn;
    private Long runSeconds;
    private Long bat;
    private String deviceServerIp;
    private String module4gIp;
    private String module4gMask;
    private String module4gGw;
    private String module4gDns1;
    private String module4gDns2;
    private String module4gIpv6;
    private String moduleWifiStaIp;
    private String moduleWifiStaMask;
    private String moduleWifiStaGw;
    private String moduleWifiApDns;
    private Long moduleWifiApStaNum;
    private String moduleW5500Ip;
    private String moduleW5500Mask;
    private String moduleW5500Gw;
    private String moduleW5500Dns1;
    private String moduleW5500Dns2;
    private String moduleW5500Ipv6;
    private Boolean forwardStatus;
    private Instant createTime;
    private Instant modifyTime;
    private Instant restartTime;
    private Instant uploadTime;
    private Instant acrossCommandTime;
    private Long acrossCommandTimes;
    private DeviceStatus deviceStatus;
    private DeviceTradeStatus deviceTradeStatus;
    private DeviceTestStatus deviceTestStatus;
    @Transient
    private Map<Integer,DeviceConfigElementModel> deviceConfig;
    @Transient
    private Map<Integer,ModuleInfoModel> moduleInfoModelMap;
    @Transient
    private List<RebootInfoModel> rebootInfoModelList;
    @Transient
    private LockInfoModel lockInfoModel;
    @Transient
    private MongoDBService mongoDBService;

    private transient DeviceTCPHostModel deviceTCPHost;

	public String getDeviceServerIp() {
		return deviceServerIp;
	}
	public void setDeviceServerIp(String deviceServerIp) {
		this.deviceServerIp = deviceServerIp;
	}
	public DeviceTCPHostModel getDeviceTCPHost() 
	{
		if(deviceTCPHost==null)
		{
			DeviceTCPHostModel temp=getMongoDBService().findOneByField("id", id, DeviceTCPHostModel.class);
			if(temp==null)
			{ 
				temp=new DeviceTCPHostModel();
				temp.setId(id);
				temp.setFailedTimes(0);
				temp.setTime(Instant.now());
			}
			deviceTCPHost=temp;
		}
		return deviceTCPHost;
	}
	public void saveDeviceTCPHost(String caller) {
		getMongoDBService().save(caller, deviceTCPHost);
	}
	public Boolean getForwardStatus() {
		return forwardStatus;
	}
	public void setForwardStatus(Boolean forwardStatus) {
		this.forwardStatus = forwardStatus;
	}
	public LockInfoModel getLockInfoModel() 
	{
		if(lockInfoModel==null)
		{
			LockInfoModel temp=getMongoDBService().findOneByField("id", id, LockInfoModel.class);
			if(temp==null)
			{ 
				temp=new LockInfoModel();
				temp.setId(id);
				temp.setUpdateTime(Instant.now());
				temp.setSht30Status(0);
				temp.setTm7705Status(0);
			}
			lockInfoModel=temp;
		}
		return lockInfoModel;
	}

	public void saveLockInfoModel() {
		getMongoDBService().save("from devicemodel", lockInfoModel);
	}

	public List<RebootInfoModel> getRebootInfoModelList() {
		if(rebootInfoModelList==null)
		{
			List<RebootInfoModel> list=getMongoDBService().findByField(new Query().addCriteria(Criteria.where("deviceId").is(id)), "uploadTime", null,false, 1,RebootInfoModel.class);
			if(list!=null)
				rebootInfoModelList=list;
			else
				rebootInfoModelList=new ArrayList<>();
		}
		return rebootInfoModelList;
	}

	public Map<Integer, ModuleInfoModel> getModuleInfoModelMap() {
		if(moduleInfoModelMap==null)
		{
			if((id!=null)&&(id.length()>0)) 
			{
				List<ModuleInfoModel> list=getMongoDBService().findByField("deviceId",id,ModuleInfoModel.class);
				if(list!=null)
					moduleInfoModelMap=list.stream().collect(Collectors.toMap(ModuleInfoModel::getModuleTypeId, element -> element));
			}
		}
		if(moduleInfoModelMap==null)
			moduleInfoModelMap=new HashMap<>();
		return moduleInfoModelMap;
	}
	
	public void setModuleInfoModelMap(Map<Integer, ModuleInfoModel> moduleInfoModelMap) {
		this.moduleInfoModelMap = moduleInfoModelMap;
	}
	public Map<Integer, DeviceConfigElementModel> getDeviceConfig() {
		if(deviceConfig==null)
		{
			List<DeviceConfigElementModel> list=getMongoDBService().findByField("deviceId",id, DeviceConfigElementModel.class);
			if(list!=null)
				deviceConfig=list.stream().collect(Collectors.toMap(DeviceConfigElementModel::getIdex, element -> element));
			else
				deviceConfig=new HashMap<>();
		}
		return deviceConfig;
	}
	public void setDeviceConfig(Map<Integer, DeviceConfigElementModel> deviceConfig) {
		this.deviceConfig = deviceConfig;
	}

	public MongoDBService getMongoDBService() {
		if(mongoDBService==null)
			mongoDBService=SpringUtil.getBean(MongoDBService.class);
		return mongoDBService;
	}
	public String getDeviceToken() {
		return deviceToken;
	}
	public void setDeviceToken(String deviceToken) {
		this.deviceToken = deviceToken;
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	public String getModuleW5500Ipv6() {
		return moduleW5500Ipv6;
	}
	public void setModuleW5500Ipv6(String moduleW5500Ipv6) {
		this.moduleW5500Ipv6 = moduleW5500Ipv6;
	}
	public String getModuleW5500Ip() {
		return moduleW5500Ip;
	}
	public void setModuleW5500Ip(String moduleW5500Ip) {
		this.moduleW5500Ip = moduleW5500Ip;
	}
	public String getModuleW5500Mask() {
		return moduleW5500Mask;
	}
	public void setModuleW5500Mask(String moduleW5500Mask) {
		this.moduleW5500Mask = moduleW5500Mask;
	}
	public String getModuleW5500Gw() {
		return moduleW5500Gw;
	}
	public void setModuleW5500Gw(String moduleW5500Gw) {
		this.moduleW5500Gw = moduleW5500Gw;
	}
	public String getModuleW5500Dns1() {
		return moduleW5500Dns1;
	}
	public void setModuleW5500Dns1(String moduleW5500Dns1) {
		this.moduleW5500Dns1 = moduleW5500Dns1;
	}
	public String getModuleW5500Dns2() {
		return moduleW5500Dns2;
	}
	public void setModuleW5500Dns2(String moduleW5500Dns2) {
		this.moduleW5500Dns2 = moduleW5500Dns2;
	}
	public String getModuleWifiStaIp() {
		return moduleWifiStaIp;
	}
	public void setModuleWifiStaIp(String moduleWifiStaIp) {
		this.moduleWifiStaIp = moduleWifiStaIp;
	}
	public String getModuleWifiStaMask() {
		return moduleWifiStaMask;
	}
	public void setModuleWifiStaMask(String moduleWifiStaMask) {
		this.moduleWifiStaMask = moduleWifiStaMask;
	}
	public String getModuleWifiStaGw() {
		return moduleWifiStaGw;
	}
	public void setModuleWifiStaGw(String moduleWifiStaGw) {
		this.moduleWifiStaGw = moduleWifiStaGw;
	}
	public String getModuleWifiApDns() {
		return moduleWifiApDns;
	}
	public void setModuleWifiApDns(String moduleWifiApDns) {
		this.moduleWifiApDns = moduleWifiApDns;
	}
	public Long getModuleWifiApStaNum() {
		return moduleWifiApStaNum;
	}
	public void setModuleWifiApStaNum(Long moduleWifiApStaNum) {
		this.moduleWifiApStaNum = moduleWifiApStaNum;
	}
	public String getModule4gIp() {
		return module4gIp;
	}
	public void setModule4gIp(String module4gIp) {
		this.module4gIp = module4gIp;
	}
	public String getModule4gMask() {
		return module4gMask;
	}
	public void setModule4gMask(String module4gMask) {
		this.module4gMask = module4gMask;
	}
	public String getModule4gGw() {
		return module4gGw;
	}
	public void setModule4gGw(String module4gGw) {
		this.module4gGw = module4gGw;
	}
	public String getModule4gDns1() {
		return module4gDns1;
	}
	public void setModule4gDns1(String module4gDns1) {
		this.module4gDns1 = module4gDns1;
	}
	public String getModule4gDns2() {
		return module4gDns2;
	}
	public void setModule4gDns2(String module4gDns2) {
		this.module4gDns2 = module4gDns2;
	}
	public String getModule4gIpv6() {
		return module4gIpv6;
	}
	public void setModule4gIpv6(String module4gIpv6) {
		this.module4gIpv6 = module4gIpv6;
	}
	public Long getAliveTimesMeta() {
		return aliveTimesMeta;
	}
	public void setAliveTimesMeta(Long aliveTimesMeta) {
		this.aliveTimesMeta = aliveTimesMeta;
	}
	public Long getRunSeconds() {
		return runSeconds;
	}
	public void setRunSeconds(Long runSeconds) {
		this.runSeconds = runSeconds;
	}
	public Long getAliveRunSeconds() {
		return aliveRunSeconds;
	}
	public void setAliveRunSeconds(Long aliveRunSeconds) {
		this.aliveRunSeconds = aliveRunSeconds;
	}
	public Long getAlivePackSeqDevice() {
		return alivePackSeqDevice;
	}
	public void setAlivePackSeqDevice(Long alivePackSeqDevice) {
		this.alivePackSeqDevice = alivePackSeqDevice;
	}
	public Long getAlivePackSeqTCP() {
		return alivePackSeqTCP;
	}
	public void setAlivePackSeqTCP(Long alivePackSeqTCP) {
		this.alivePackSeqTCP = alivePackSeqTCP;
	}
	public Long getAliveTimesReconn() {
		return aliveTimesReconn;
	}
	public void setAliveTimesReconn(Long aliveTimesReconn) {
		this.aliveTimesReconn = aliveTimesReconn;
	}
	public Long getTimesRestart() {
		return timesRestart;
	}
	public void setTimesRestart(Long timesRestart) {
		this.timesRestart = timesRestart;
	}
	public Long getTimesUpload() {
		return timesUpload;
	}
	public void setTimesUpload(Long timesUpload) {
		this.timesUpload = timesUpload;
	}
	public Long getTimesReconn() {
		return timesReconn;
	}
	public void setTimesReconn(Long timesReconn) {
		this.timesReconn = timesReconn;
	}

	public Integer getCsqCurrent() {
		return csqCurrent;
	}
	public void setCsqCurrent(Integer csqCurrent) {
		this.csqCurrent = csqCurrent;
	}
	public Integer getCsqLow() {
		return csqLow;
	}
	public void setCsqLow(Integer csqLow) {
		this.csqLow = csqLow;
	}
	public Integer getCsqMax() {
		return csqMax;
	}
	public void setCsqMax(Integer csqMax) {
		this.csqMax = csqMax;
	}
	public Instant getAcrossCommandTime() {
		return acrossCommandTime;
	}
	public void setAcrossCommandTime(Instant acrossCommandTime) {
		this.acrossCommandTime = acrossCommandTime;
	}
	public Long getAcrossCommandTimes() {
		return acrossCommandTimes;
	}
	public void setAcrossCommandTimes(Long acrossCommandTimes) {
		this.acrossCommandTimes = acrossCommandTimes;
	}

	public String getClientIp() {
		return clientIp;
	}
	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDeviceNo() {
		return deviceNo;
	}
	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public Long getDeviceFirmWareVersion() {
		return deviceFirmWareVersion;
	}
	public void setDeviceFirmWareVersion(Long deviceFirmWareVersion) {
		this.deviceFirmWareVersion = deviceFirmWareVersion;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getIccid() {
		return iccid;
	}
	public void setIccid(String iccid) {
		this.iccid = iccid;
	}
	public Long getBat() {
		return bat;
	}
	public void setBat(Long bat) {
		this.bat = bat;
	}
	public Instant getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Instant createTime) {
		this.createTime = createTime;
	}
	public Instant getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Instant modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Instant getRestartTime() {
		return restartTime;
	}
	public void setRestartTime(Instant restartTime) {
		this.restartTime = restartTime;
	}
	public Instant getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Instant uploadTime) {
		this.uploadTime = uploadTime;
	}
	public DeviceStatus getDeviceStatus() {
		return deviceStatus;
	}
	public void setDeviceStatus(DeviceStatus deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
	public DeviceTradeStatus getDeviceTradeStatus() {
		return deviceTradeStatus;
	}
	public void setDeviceTradeStatus(DeviceTradeStatus deviceTradeStatus) {
		this.deviceTradeStatus = deviceTradeStatus;
	}
	public DeviceTestStatus getDeviceTestStatus() {
		return deviceTestStatus;
	}
	public void setDeviceTestStatus(DeviceTestStatus deviceTestStatus) {
		this.deviceTestStatus = deviceTestStatus;
	}
    public enum DeviceStatus{
    	ENABLED,
    	DISABLED,
    	FREEZED,
    	NOLICENSE
    }
    public enum DeviceTradeStatus{
    	FACOTRY,
    	DISTRIBUTOR,
    	CUSTOMER
    }
    public enum DeviceTestStatus{
    	INIT,
    	START,
    	SUCCESS,
    	FAILED
    }
    public enum DeviceType {
        DC01(1, "DC01"),
        DC02(2, "DC02");

        private final int id;
        private final String name;
        private static final Map<Integer, DeviceType> ID_TO_ENUM = new HashMap<>();

        static {
            for (DeviceType status : DeviceType.values()) {
                ID_TO_ENUM.put(status.id, status);
            }
        }

        DeviceType(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static DeviceType getById(int id) {
        	DeviceType status = ID_TO_ENUM.get(id);
            return status;
        }
    }
}
