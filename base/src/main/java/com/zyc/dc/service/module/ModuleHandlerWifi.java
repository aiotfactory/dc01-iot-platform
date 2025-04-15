package com.zyc.dc.service.module;

import java.util.List;
import java.util.Map;

import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerWifi extends ModuleHandler {
	public ModuleHandlerWifi()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);
    	if(result!=null)
    	{
        	MiscUtil.resultPutRow(result, "name","Sta Ip","value", deviceModel.getModuleWifiStaIp());
        	MiscUtil.resultPutRow(result, "name","Sta Netmask","value", deviceModel.getModuleWifiStaMask());
        	MiscUtil.resultPutRow(result, "name","Sta Gateway","value", deviceModel.getModuleWifiStaGw());
        	MiscUtil.resultPutRow(result, "name","Ap Dns","value", deviceModel.getModuleWifiApDns());
        	MiscUtil.resultPutRow(result, "name","Ap Sta Number","value", deviceModel.getModuleWifiApStaNum());

    		ModuleInfoModel moduleMetaModel=deviceModel.getModuleInfoModelMap().get(ModuleDefine.MODULE_META.getId()); 
        	MiscUtil.resultPutRow(result, "name","Upload Time","value", MiscUtil.dateFormat(moduleMetaModel.getUploadTime(),"yyyy-MM-dd HH:mm:ss"));
     
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
}