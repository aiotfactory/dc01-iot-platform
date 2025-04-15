package com.zyc.dc.service.module;

import java.util.List;
import java.util.Map;

import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerW5500 extends ModuleHandler {
	public ModuleHandlerW5500()
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
        	MiscUtil.resultPutRow(result, "name","Ip","value", deviceModel.getModuleW5500Ip());
        	MiscUtil.resultPutRow(result, "name","Mask","value", deviceModel.getModuleW5500Mask());
        	MiscUtil.resultPutRow(result, "name","Gateway","value", deviceModel.getModuleW5500Gw());
        	MiscUtil.resultPutRow(result, "name","Dns1","value",deviceModel.getModuleW5500Dns1());
        	MiscUtil.resultPutRow(result, "name","Dns2","value", deviceModel.getModuleW5500Dns2());      	
    		ModuleInfoModel moduleMetaModel=deviceModel.getModuleInfoModelMap().get(ModuleDefine.MODULE_META.getId()); 
        	MiscUtil.resultPutRow(result, "name","Upload Time","value", MiscUtil.dateFormat(moduleMetaModel.getUploadTime(),"yyyy-MM-dd HH:mm:ss"));
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
}