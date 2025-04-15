package com.zyc.dc.service.module;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceConfigElementModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.DeviceConfigElement;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerConfig extends ModuleHandler {
	//private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerConfig.class);
	public ModuleHandlerConfig()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
		getSectionMap().put("cmdContainer", 1);
	}
    @Override
	public String getRefreshOperate()
	{
		return "operate=18";
	}
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);
    	if(result!=null)
    	{
    		ModuleInfoModel moduleInfo=deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        	if((moduleInfo!=null)&&(moduleInfo.getUpload()!=null))
        	{
        		MiscUtil.resultPutRow(result, "name","Status","value",MiscUtil.jsonObject2String(moduleInfo.getUpload()));
        	}
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
    	Map<Integer, DeviceConfigElementModel>  configmap=deviceModel.getDeviceConfig();
    	if((configmap==null)||(configmap.size()==0))
    		return null;
    	List<Map<String,Object>> result=ModuleHandler.utilConfigDisplay(new ArrayList<DeviceConfigElementModel>(configmap.values()));
    	if(result.size()==0)
    		return null;
    	return result;
    }

    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	List<DeviceConfigElement> configelementlist=new ArrayList<DeviceConfigElement>();
		for(int i=0;i<msgReq.getData().length;)
		{
			int tempidx=i;
			int idx=MiscUtil.bytesToUint16LittleEndian(Arrays.copyOfRange(msgReq.getData(), tempidx, tempidx+2));//index
			tempidx+=2;
			int module=(int)MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(msgReq.getData(), tempidx, tempidx+4));//module id
			tempidx+=4;
			int type=msgReq.getData()[tempidx++];//type
			int namelen=msgReq.getData()[tempidx++];//name len
			String name=new String(Arrays.copyOfRange(msgReq.getData(), tempidx, tempidx+namelen));//name
			tempidx+=namelen;
			int valuelen=(int)MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(msgReq.getData(), tempidx, tempidx+4));//value len
			tempidx+=4;
			byte[] value=Arrays.copyOfRange(msgReq.getData(), tempidx, tempidx+valuelen);//value
			tempidx+=valuelen;
			int rangeLen=8;
			long minValue=0,maxValue=0;
			List<Integer> enumValue=null;
			if(type==1)//enum
			{
				rangeLen=msgReq.getData()[tempidx]+1;
				enumValue=new ArrayList<>();
				for(int j=1;j<rangeLen;j++)
					enumValue.add((int)msgReq.getData()[tempidx+j]);
			}else {
				minValue=(long)MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(msgReq.getData(), tempidx, tempidx+4));
				maxValue=(long)MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(msgReq.getData(), tempidx+4, tempidx+8));
			}
			tempidx+=rangeLen;
			int status=msgReq.getData()[tempidx++];	
			i=tempidx;
			DeviceConfigElement configElement=new DeviceConfigElement(idx,module,type,name,valuelen,value,minValue,maxValue,enumValue,status);
			configelementlist.add(configElement);
			//logger.info("valuelen "+valuelen+" "+DatatypeConverter.printHexBinary(value).toLowerCase());
			//logger.info("messageProcessConfig "+configElement.toString());
		}
		Map<Integer,DeviceConfigElementModel> mergedConfig=DeviceConfigElement.uploadConfig(deviceModel.getId(),configelementlist, deviceModel.getDeviceConfig());
		deviceModel.setDeviceConfig(mergedConfig);
		List<DeviceConfigElementModel> configList=new ArrayList<>(mergedConfig.values());
		getMongoDBService().updateBatch(configList);
		
		if(commModel!=null) 
		{
			commModel.setUpload(configList);
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			if(msgReq.getOperate()==null) 
				commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
			else
				commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		}
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
    	moduleInfo.put("uploadTime", Instant.now());
    	moduleInfoModel.setUpload(moduleInfo);
    	return null;
    }
}