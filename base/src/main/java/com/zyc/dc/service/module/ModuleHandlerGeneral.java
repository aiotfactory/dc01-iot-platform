package com.zyc.dc.service.module;

import java.util.List;
import java.util.Map;

import com.zyc.dc.dao.DeviceModel;

public class ModuleHandlerGeneral extends ModuleHandler {
	
	public ModuleHandlerGeneral()
	{
		super();
		getSectionMap().put("configContainer", 1);
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }
}
