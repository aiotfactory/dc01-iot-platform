package com.zyc.dc.service.module;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zyc.dc.dao.DataCloudLog;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerCloudLog extends ModuleHandler {
	public ModuleHandlerCloudLog()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("forwardContainer", 1);
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
    		ModuleInfoModel moduleInfo=deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
    		result.clear();
        	MiscUtil.resultPutRow(result, "name","Last Upload Time","value",MiscUtil.dateFormat(moduleInfo.getUploadTime(),"MM-dd HH:mm:ss"));
        	MiscUtil.resultPutRow(result, "name","Upload Times","value",MiscUtil.strDisplay(moduleInfo.getUploadTimes()));       	
        	if((moduleInfo!=null)&&(moduleInfo.getUpload()!=null))
        	{
        		MiscUtil.resultPutRow(result, "name","Log Status","value",MiscUtil.jsonObject2String(moduleInfo.getUpload()));
        	}
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	Instant now=Instant.now();
    	DataCloudLog dataCloudLog=new DataCloudLog();
    	dataCloudLog.setContent(new String(msgReq.getData()));
    	commModel.setUpload(dataCloudLog);
		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
		commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
		
    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	@SuppressWarnings("unchecked")
		Map<String,Object> moduleInfo=(Map<String,Object>)moduleInfoModel.getUpload();
    	if(moduleInfo==null)
    		moduleInfo=new HashMap<>();
    	Integer times=(Integer)moduleInfo.get("times");
    	if(times==null)
    		times=0;
    	times++;
    	moduleInfo.put("times", times);
    	moduleInfo.put("uploadTime", now);
    	moduleInfoModel.setUpload(moduleInfo);
    	return null;
    }
}