package com.zyc.dc.service.module;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyc.dc.constant.ResetEspEnum;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.dao.RebootInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerMeta extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerMeta.class);
	public ModuleHandlerMeta()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("cmdContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }

    @SuppressWarnings("unchecked")
	@Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
		Map<String,Object> result=new HashMap<>();
		Instant now=Instant.now();
    	utilParseVariable(msgReq.getData(),result);
    	Map<String,Object> newResult=new HashMap<>();
    	if(result.containsKey("imei")) {
    		deviceModel.setImei(result.get("imei").toString());
    		newResult.put("imei", deviceModel.getImei());
    	}
    	if(result.containsKey("iccid")) {
    		deviceModel.setIccid(result.get("iccid").toString());
    		newResult.put("iccid", deviceModel.getIccid());
    	}
    	if(result.containsKey("al_run_seconds")) {
    		Long tempData=(Long)result.get("al_run_seconds");
    		deviceModel.setRunSeconds(deviceModel.getRunSeconds()+tempData-deviceModel.getAliveRunSeconds());
    		deviceModel.setAliveRunSeconds(tempData);
    		newResult.put("aliveRunSeconds", deviceModel.getAliveRunSeconds());
    		newResult.put("runSeconds", deviceModel.getRunSeconds());
    	}
    	if(result.containsKey("al_times_reconn")) {
    		Long tempData=(Long)result.get("al_times_reconn");
    		deviceModel.setTimesReconn(deviceModel.getTimesReconn()+(tempData-deviceModel.getAliveTimesReconn()));
    		deviceModel.setAliveTimesReconn(tempData);
    		newResult.put("timesReconn", deviceModel.getTimesReconn());
    		newResult.put("aliveTimesReconn", deviceModel.getAliveTimesReconn());
    	}
    	if(result.containsKey("al_meta_times")) {
    		Long tempData=(Long)result.get("al_meta_times");
    		deviceModel.setAliveTimesMeta(tempData);
    		newResult.put("aliveTimesMeta", deviceModel.getAliveTimesMeta());
    	}
    	
       	if(result.containsKey("rt_4g_ip")) {
       		Long rt4gIP=(Long)result.get("rt_4g_ip");
       		if(rt4gIP>0)
       		{
       			ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(ModuleDefine.MODULE_4G.getId()); 
       			if(moduleInfoModel!=null) {
	       	    	moduleInfoModel.setUploadTime(now);
	       	    	getMongoDBService().save("metaHandler", moduleInfoModel);
       			}
       		}
    		deviceModel.setModule4gIp(MiscUtil.ipNumber2String(rt4gIP));
       		newResult.put("module4gIp", deviceModel.getModule4gIp());
		}
       	if(result.containsKey("rt_4g_mask")) {
    		deviceModel.setModule4gMask(MiscUtil.ipNumber2String((Long)result.get("rt_4g_mask")));
   			newResult.put("module4gMask", deviceModel.getModule4gMask());
		}
       	if(result.containsKey("rt_4g_gw")) {
    		deviceModel.setModule4gGw(MiscUtil.ipNumber2String((Long)result.get("rt_4g_gw")));
   			newResult.put("module4gGw", deviceModel.getModule4gGw());
		}
       	if(result.containsKey("rt_4g_dns1")) {
    		deviceModel.setModule4gDns1(MiscUtil.ipNumber2String((Long)result.get("rt_4g_dns1")));
   			newResult.put("module4gDns1", deviceModel.getModule4gDns1());
		}
       	if(result.containsKey("rt_4g_dns2")) {
    		deviceModel.setModule4gDns2(MiscUtil.ipNumber2String((Long)result.get("rt_4g_dns2")));
   			newResult.put("module4gDns2", deviceModel.getModule4gDns2());
		}
    	if(result.containsKey("rt_4g_ipv6")&&result.containsKey("rt_4g_ipv6_zone")) {
    		deviceModel.setModule4gIpv6(MiscUtil.ipV6Number2String((List<Long>)result.get("rt_4g_ipv6"), ((Long)result.get("rt_4g_ipv6_zone")).intValue()));
    		newResult.put("module4gIpv6", deviceModel.getModule4gIpv6());
    	}
       	if(result.containsKey("rt_wifi_sta_ip")) {
       		Long rtWifiStaIp=(Long)result.get("rt_wifi_sta_ip");
       		if(rtWifiStaIp>0)
       		{
       			ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(ModuleDefine.MODULE_WIFI.getId()); 
       			if(moduleInfoModel!=null) {
	       	    	moduleInfoModel.setUploadTime(now);
       				getMongoDBService().save("metaHandler", moduleInfoModel);
   				}
       		}
    		deviceModel.setModuleWifiStaIp(MiscUtil.ipNumber2String(rtWifiStaIp));
    		newResult.put("moduleWifiStaIp", deviceModel.getModuleWifiStaIp());
       	}
       	if(result.containsKey("rt_wifi_sta_mask")) {
    		deviceModel.setModuleWifiStaMask(MiscUtil.ipNumber2String((Long)result.get("rt_wifi_sta_mask")));
    		newResult.put("moduleWifiStaMask", deviceModel.getModuleWifiStaMask());
       	}
       	if(result.containsKey("rt_wifi_sta_gw")) {
    		deviceModel.setModuleWifiStaGw(MiscUtil.ipNumber2String((Long)result.get("rt_wifi_sta_gw")));
    		newResult.put("moduleWifiStaGw", deviceModel.getModuleWifiStaGw());
       	}
       	if(result.containsKey("rt_wifi_ap_dns")) {
    		deviceModel.setModuleWifiApDns(MiscUtil.ipNumber2String((Long)result.get("rt_wifi_ap_dns")));
    		newResult.put("moduleWifiApDns", deviceModel.getModuleWifiApDns());
       	}
    	if(result.containsKey("rt_wifi_ap_sta_num")) {
    		Long tempData=(Long)result.get("rt_wifi_ap_sta_num");
    		deviceModel.setModuleWifiApStaNum(tempData);
    		newResult.put("moduleWifiApStaNum", deviceModel.getModuleWifiApStaNum());
    	}

    	//uint32_t rt_restart_times=0,rt_restart_reason=0,rt_restart_reason_times=0,rt_restart_reason_seconds=0;
    	if(result.containsKey("rt_restart_reason")) 
    	{
        	List<RebootInfoModel> rebootInfoModelList=deviceModel.getRebootInfoModelList();
        	if((rebootInfoModelList.size()==0)||(rebootInfoModelList.getLast().getRuntimeRestartTimes()!=((Long)result.get("rt_restart_times")).intValue()))
			{
        		RebootInfoModel temp=new RebootInfoModel();
        		temp.setDeviceId(deviceModel.getId());
        		temp.setRuntimeRestartReason((Long)result.get("rt_restart_reason"));
        		temp.setRuntimeRestartReasonSeconds((Long)result.get("rt_restart_reason_seconds"));
        		temp.setRuntimeRestartReasonTimes((Long)result.get("rt_restart_reason_times"));
        		temp.setRuntimeRestartTimes((Long)result.get("rt_restart_times"));
        		temp.setRuntimeResetReason(((Long)result.get("rt_reset_reason")).intValue());
        		temp.setUploadTime(now);
        		getMongoDBService().save("metahandler", temp);      		
        		newResult.put("runtimeRestartReason", temp.getRuntimeRestartReason());
        		newResult.put("runtimeRestartReasonSeconds", temp.getRuntimeRestartReasonSeconds());
        		newResult.put("runtimeRestartReasonTimes", temp.getRuntimeRestartReasonTimes());
        		newResult.put("runtimeRestartTimes", temp.getRuntimeRestartTimes());
        		newResult.put("runtimeResetReason", temp.getRuntimeResetReason());
			}
    	}
       	if(result.containsKey("rt_w5500_sta_ip")) {
       		Long rtW5500StaIp=(Long)result.get("rt_w5500_sta_ip");
       		if(rtW5500StaIp>0)
       		{
       			ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(ModuleDefine.MODULE_W5500.getId()); 
       			if(moduleInfoModel!=null) {
	       	    	moduleInfoModel.setUploadTime(now);
	       	    	getMongoDBService().save("metaHandler", moduleInfoModel);
       			}
       		}
    		deviceModel.setModuleW5500Ip(MiscUtil.ipNumber2String(rtW5500StaIp));
    		newResult.put("moduleW5500Ip", deviceModel.getModuleW5500Ip());
    	}
       	if(result.containsKey("rt_w5500_sta_mask")) {
    		deviceModel.setModuleW5500Mask(MiscUtil.ipNumber2String((Long)result.get("rt_w5500_sta_mask")));
    		newResult.put("moduleW5500Mask", deviceModel.getModuleW5500Mask());
    	}
       	if(result.containsKey("rt_w5500_sta_gw")) {
    		deviceModel.setModuleW5500Gw(MiscUtil.ipNumber2String((Long)result.get("rt_w5500_sta_gw")));
    		newResult.put("moduleW5500Gw", deviceModel.getModuleW5500Gw());
    	}
       	if(result.containsKey("rt_w5500_sta_dns1")) {
    		deviceModel.setModuleW5500Dns1(MiscUtil.ipNumber2String((Long)result.get("rt_w5500_sta_dns1")));
    		newResult.put("moduleW5500Dns1", deviceModel.getModuleW5500Dns1());
    	}
       	if(result.containsKey("rt_w5500_sta_dns2")) {
    		deviceModel.setModuleW5500Dns2(MiscUtil.ipNumber2String((Long)result.get("rt_w5500_sta_dns2")));
    		newResult.put("moduleW5500Dns2", deviceModel.getModuleW5500Dns2());
    	}
    	if(result.containsKey("rt_w5500_sta_ipv6")&&result.containsKey("rt_w5500_sta_ipv6_zone")) {
    		deviceModel.setModuleW5500Ipv6(MiscUtil.ipV6Number2String((List<Long>)result.get("rt_w5500_sta_ipv6"), ((Long)result.get("rt_w5500_sta_ipv6_zone")).intValue()));
    		newResult.put("moduleW5500Ipv6", deviceModel.getModuleW5500Ipv6());
    	}
    	commModel.setUpload(newResult);
		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
		if(msgReq.getOperate()==null) 
			commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
		else
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		
		ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	moduleInfoModel.setUpload(newResult);
    	return null;
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=13";
	}
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);
    	if(result!=null)
    	{
            MiscUtil.resultPutRow(result, "name", "Imei", "value",MiscUtil.strDisplay(deviceModel.getImei()));
            MiscUtil.resultPutRow(result, "name", "Iccid","value",MiscUtil.strDisplay(deviceModel.getIccid()));

            List<RebootInfoModel> rebootInfoModelList = deviceModel.getRebootInfoModelList();
            if ((rebootInfoModelList != null) && (rebootInfoModelList.size() > 0)) {
                RebootInfoModel rebootInfoModel = rebootInfoModelList.getLast();
                if ((rebootInfoModel.getRuntimeRestartTimes() != null) && (deviceModel.getTimesRestart() != null) 
                    && (deviceModel.getTimesRestart() == rebootInfoModel.getRuntimeRestartTimes())) {
                	
                	String[] tempReasons=ResetEspEnum.getNameById(rebootInfoModel.getRuntimeResetReason());
                    MiscUtil.resultPutRow(result, "name", "Reset Reason","value", rebootInfoModel.getRuntimeResetReason() 
                        + "|" + tempReasons[0]+"|"+ tempReasons[1]);
                    String manualCycleStr = "LATEST ";
                    if ((rebootInfoModel.getRuntimeRestartTimes() + 1) == rebootInfoModel.getRuntimeRestartTimes()) {
                        manualCycleStr = "";
                    }
                    if(rebootInfoModel.getRuntimeRestartReason()>0) {
	                    MiscUtil.resultPutRow(result, "name",  manualCycleStr + "Manual Restart Reason","value", rebootInfoModel.getRuntimeRestartReason());
	                    MiscUtil.resultPutRow(result, "name",  manualCycleStr + "Manual Restart Running Seconds","value", rebootInfoModel.getRuntimeRestartReasonSeconds());
	                    MiscUtil.resultPutRow(result, "name",  manualCycleStr + "Manual Restart Times","value", rebootInfoModel.getRuntimeRestartReasonTimes());
	                    MiscUtil.resultPutRow(result, "name",  manualCycleStr + "Manual Restart Time","value", MiscUtil.dateFormat(rebootInfoModel.getUploadTime(), "yyyy-MM-dd HH:mm"));
                    }
                }
            }
            
        	

         // 修改4G相关的条目
         MiscUtil.resultPutRow(result, "name", "4G IP","value", MiscUtil.strDisplay(deviceModel.getModule4gIp()));
         MiscUtil.resultPutRow(result, "name", "4G IPv6","value", MiscUtil.strDisplay(deviceModel.getModule4gIpv6()));
         MiscUtil.resultPutRow(result, "name", "4G Netmask","value", MiscUtil.strDisplay(deviceModel.getModule4gMask()));
         MiscUtil.resultPutRow(result, "name", "4G Gateway","value", MiscUtil.strDisplay(deviceModel.getModule4gGw()));
         MiscUtil.resultPutRow(result, "name", "4G DNS1","value", MiscUtil.strDisplay(deviceModel.getModule4gDns1()));
         MiscUtil.resultPutRow(result, "name", "4G DNS2","value", MiscUtil.strDisplay(deviceModel.getModule4gDns2()));

         // 修改W5500相关的条目
         MiscUtil.resultPutRow(result, "name", "W5500 IP","value", MiscUtil.strDisplay(deviceModel.getModuleW5500Ip()));
         MiscUtil.resultPutRow(result, "name", "W5500 Netmask","value", MiscUtil.strDisplay(deviceModel.getModuleW5500Mask()));
         MiscUtil.resultPutRow(result, "name", "W5500 Gateway","value", MiscUtil.strDisplay(deviceModel.getModuleW5500Gw()));
         MiscUtil.resultPutRow(result, "name", "W5500 DNS1","value", MiscUtil.strDisplay(deviceModel.getModuleW5500Dns1()));
         MiscUtil.resultPutRow(result, "name", "W5500 DNS2","value", MiscUtil.strDisplay(deviceModel.getModuleW5500Dns2()));

         // 修改Wi-Fi STA相关的条目
         MiscUtil.resultPutRow(result, "name", "Wifi Sta IP","value", MiscUtil.strDisplay(deviceModel.getModuleWifiStaIp()));
         MiscUtil.resultPutRow(result, "name", "Wifi Sta Netmask","value", MiscUtil.strDisplay(deviceModel.getModuleWifiStaMask()));
         MiscUtil.resultPutRow(result, "name", "Wifi Sta Gateway","value", MiscUtil.strDisplay(deviceModel.getModuleWifiStaGw()));
         MiscUtil.resultPutRow(result, "name", "Wifi Ap DNS","value", MiscUtil.strDisplay(deviceModel.getModuleWifiApDns()));
         MiscUtil.resultPutRow(result, "name", "Wifi Ap Station Number","value", MiscUtil.strDisplay(deviceModel.getModuleWifiApStaNum()));

         // 修改其他条目
         MiscUtil.resultPutRow(result, "name", "CSQ Current","value", MiscUtil.strDisplay(deviceModel.getCsqCurrent()));
         MiscUtil.resultPutRow(result, "name", "CSQ Low","value", MiscUtil.strDisplay(deviceModel.getCsqLow()));
         MiscUtil.resultPutRow(result, "name", "CSQ Max","value", MiscUtil.strDisplay(deviceModel.getCsqMax()));
         MiscUtil.resultPutRow(result, "name", "Run Seconds","value", MiscUtil.strDisplay(deviceModel.getAliveRunSeconds()));
         MiscUtil.resultPutRow(result, "name", "Total Run Seconds","value", MiscUtil.strDisplay(deviceModel.getRunSeconds()));
         MiscUtil.resultPutRow(result, "name", "Pack Nums Initiated From Device","value", MiscUtil.strDisplay(deviceModel.getAlivePackSeqDevice()));
         MiscUtil.resultPutRow(result, "name", "Pack Nums Requested By Server","value", MiscUtil.strDisplay(deviceModel.getAlivePackSeqTCP()));
         MiscUtil.resultPutRow(result, "name", "Downstream Operate Times","value", (deviceModel.getAcrossCommandTimes() == null ? "0" : MiscUtil.strDisplay(deviceModel.getAcrossCommandTimes())));
         MiscUtil.resultPutRow(result, "name", "Upload Times","value", MiscUtil.strDisplay(deviceModel.getTimesUpload()));
         MiscUtil.resultPutRow(result, "name", "Restart Times","value", MiscUtil.strDisplay(deviceModel.getTimesRestart()));
         MiscUtil.resultPutRow(result, "name", "Reconnect Times","value", MiscUtil.strDisplay(deviceModel.getAliveTimesReconn()));
         MiscUtil.resultPutRow(result, "name", "Total Reconnect Times","value", MiscUtil.strDisplay(deviceModel.getTimesReconn()));
         MiscUtil.resultPutRow(result, "name", "Meta Upload Times","value", MiscUtil.strDisplay(deviceModel.getAliveTimesMeta()));

         
         
        ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        if(moduleInfoModel.getUpload()!=null)
        {
        	 String rawString = MiscUtil.jsonObject2String(moduleInfoModel.getUpload());
        	 MiscUtil.resultPutRow(result, "name", "Status","value", rawString);
        }
        if ((result != null) && (result.size() > 0)) {
            return result;
        }
    	}
    	return null;
    }


}