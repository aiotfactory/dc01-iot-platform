package com.zyc.dc.service.module;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;

import com.zyc.dc.dao.DataCamera;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.dao.ProjectBuildModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerCamera extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerCamera.class);
	public ModuleHandlerCamera()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("cmdContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
		getSectionMap().put("imageGalleryContainer", 1);
	}
    @Override
	public Object getRecentData(DeviceModel deviceModel,int timeSeconds)
	{
    	Instant timeBefore = Instant.now().minusSeconds(timeSeconds);
    	Criteria criteria = Criteria.where("deviceId").is(deviceModel.getId())
    	                            .and("moduleTypeId").is(getModuleTypeId())
    	                            .and("picId").ne(null)
    	                            .and("uploadTime").gte(timeBefore); 
    	DataCommModel dataCommMode=getMongoDBService().findOneByFields(criteria, "uploadTime", false,DataCommModel.class);
    	if(dataCommMode!=null)
    	{
    		DataCamera dataCamera=(DataCamera)dataCommMode.getUpload();
    		if(dataCamera!=null) 
    			return getMongoDBService().fileReadByte(dataCamera.getPicId());
    	}
		return null;
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }
    @Override
	public String getRefreshOperate()
	{
		return "operate=6";
	}
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);
    	if(result!=null)
    	{
        	MiscUtil.resultPutRow(result, "name","Power Control","value","gpio_ext_oc10");
        	MiscUtil.resultPutRow(result, "name","VCC","value","3.3v");
        	MiscUtil.resultPutRow(result, "name","Pins","value","siod=gpio_esp_4,sioc=gpio_esp_5,vsync=gpio_esp_6,href=gpio_esp_7,d9=gpio_esp_15,xclk=gpio_esp_16,d8=gpio_esp_17,d7=gpio_esp_18,pclk=gpio_esp_8,d2=gpio_esp_3,d6=gpio_esp_46,d5=gpio_esp_9,d3=gpio_esp_10,d4=gpio_esp_11");
           	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        	if(moduleInfoModel!=null)
        		MiscUtil.resultPutRow(result, "name","Camera Status","value",MiscUtil.jsonObject2String(moduleInfoModel.getUpload()));	
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
    @Override
    public void postPreprocess(DataCommModel commModel)
    {
    	Object upload=commModel.getUpload();
    	if((upload!=null)&&(upload instanceof DataCamera))
    	{
    		DataCamera dataCamera=(DataCamera)upload;
    		if(dataCamera.getPicContentBase64()==null) 
    		{
	    		byte[] picData=getMongoDBService().fileReadByte(dataCamera.getPicId());
	    		if(picData!=null)
	    		{
	    			String picBase64 = Base64.getEncoder().encodeToString(picData);
	    			dataCamera.setPicContentBase64(picBase64);
	    		}
    		}
    	}
    }
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {  
    	DataCommModel.DataCommType commType=msgReq.getOperate()==null?DataCommModel.DataCommType.PERIOD_UPLOAD:DataCommModel.DataCommType.REQUEST_UPLOAD;
        byte[] data=msgReq.getData();
    	if((data==null)||(data.length==0))
    	{
    		logger.info("recv image length 0");
			commModel.setInfo("camera is off or not workable");
			commModel.setErrorType(DataCommModel.DataCommErrorType.MODULE_UNAVAILABLE);
			commModel.setDataCommType(commType);
    	}else if((msgReq.getOperate()==null)||(msgReq.getOperate()==15))
    	{
    		
        	String picName=deviceModel.getDeviceNo()+"-"+MiscUtil.dateFormat(Instant.now(),"yyyyMMddHHmmss")+".jpeg";
        	String picId=getMongoDBService().fileSave("image/jpeg",picName, data);        	
        	DataCamera dataCamera=new DataCamera();
        	dataCamera.setPicName(picName);
        	dataCamera.setPicSize(data.length);
        	dataCamera.setPicId(picId);
        	commModel.setUpload(dataCamera);
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			commModel.setDataCommType(commType);
    	}else if(msgReq.getOperate()==6)
    	{
        	Map<String,Object> dataResult=new HashMap<>();
    		utilParseVariable(data, dataResult);
        	commModel.setUpload(dataResult);
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
			ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
			moduleInfoModel.setUpload(dataResult);
    	}
    	return null;
    }

    @Override
    public Map<String,Object> getHistoryData(DeviceModel deviceModel,Instant startTime,Instant endTime,String displayType,Integer page)
    {        	
		if(startTime==null)
			startTime=MiscUtil.dateToday(0, 0, 0);
		if(endTime==null)
			endTime=Instant.now();
		startTime=MiscUtil.dateTrim(startTime, 0, 0, 0);
		endTime=MiscUtil.dateTrim(endTime, 0, 0, 0).plus(1, ChronoUnit.DAYS);
		if((page==null)||(page<0))
			page=0;
    	if(displayType.length()==0)
    		displayType="4";
    	else 
    		displayType=displayType.substring(2,3);
    	
    	int displayTypeInt=MiscUtil.strParseInteger(displayType)*4;
    	//displayTypeInt=2;
		
    	Map<String, Object> conditionMap = Map.of(
    		    "deviceId", deviceModel.getId(),
    		    "moduleTypeId",getModuleTypeId(),
    		    "startTime", startTime,
    		    "endTime", endTime
    		);
        Sort sort = Sort.by(
                Sort.Order.desc("uploadTime")
        );
        List<DataCommModel> dataCommModelList=getMongoDBService().find(conditionMap, sort, DataCommModel.class,displayTypeInt*page,displayTypeInt*(page+1));
        if((dataCommModelList!=null)&&(dataCommModelList.size()>0))
        {
        	List<Map<String, Object>> cameraList = dataCommModelList.stream()
		    .map(dataCommModel -> {
	    		Object temp=dataCommModel.getUpload();
	    		if(temp==null)
	    			return null;
	    		if(!(temp instanceof DataCamera))
	    			return null;
		    	DataCamera dataCamera=(DataCamera)temp;
		        Map<String, Object> map = new HashMap<>();
		        map.put("uploadTime", MiscUtil.dateFormat(dataCommModel.getUploadTime(),"MM-dd HH:mm:ss"));
		        map.put("picName", dataCamera.getPicName());
		        map.put("picSize", dataCamera.getPicSize());
		        map.put("picId", dataCamera.getPicId());
		        return map;
		    })
		    .filter(Objects::nonNull)
		    .collect(Collectors.toList());
        	Map<String,Object> result=new HashMap<>();
        	result.put("cameraDataList", cameraList);
        	result.put("displayType", displayType);
        	result.put("page", page);        	
        	return result;
        }
        return null;
    }
}