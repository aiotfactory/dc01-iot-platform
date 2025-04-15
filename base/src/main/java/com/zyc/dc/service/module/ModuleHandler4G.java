package com.zyc.dc.service.module;

import java.util.List;
import java.util.Map;

import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandler4G extends ModuleHandler {
	public ModuleHandler4G()
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
    		result.clear();
        	MiscUtil.resultPutRow(result, "name","Iccid","value", deviceModel.getIccid());
        	MiscUtil.resultPutRow(result, "name","Imei","value", deviceModel.getImei());
        	MiscUtil.resultPutRow(result, "name","Csq","value", deviceModel.getCsqCurrent()+"");
        	MiscUtil.resultPutRow(result, "name","Csq Min","value", deviceModel.getCsqLow()+"");
        	MiscUtil.resultPutRow(result, "name","Csq Max","value", deviceModel.getCsqMax()+"");      	
        	MiscUtil.resultPutRow(result, "name","Ip","value", deviceModel.getModule4gIp());
        	MiscUtil.resultPutRow(result, "name","Mask","value", deviceModel.getModule4gMask());
        	MiscUtil.resultPutRow(result, "name","Gateway","value", deviceModel.getModule4gGw());
        	MiscUtil.resultPutRow(result, "name","Dns1","value", deviceModel.getModule4gDns1());
        	MiscUtil.resultPutRow(result, "name","Dns2","value", deviceModel.getModule4gDns2());
        	MiscUtil.resultPutRow(result, "name","Ipv6","value", deviceModel.getModule4gIpv6());
        	
    		ModuleInfoModel moduleMetaModel=deviceModel.getModuleInfoModelMap().get(ModuleDefine.MODULE_META.getId()); 
        	MiscUtil.resultPutRow(result, "name","Upload Time","value", MiscUtil.dateFormat(moduleMetaModel.getUploadTime(),"yyyy-MM-dd HH:mm:ss"));
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
}