package com.zyc.dc.service.module;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleRuleLogModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;
import com.zyc.dc.service.MongoDBService;
import com.zyc.dc.service.SpringUtil;
import com.zyc.dc.service.WebClientService;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceConfigElementModel;
import com.zyc.dc.dao.ModuleInfoModel;

/*Map<String,Object> dataResult=new HashMap<>();
Map<String,Object> returnResult=new HashMap<>();
			
returnResult.put("type", "property");
returnResult.put("type", "info");
returnResult.put("type", "error");
returnResult.put("type", "data");

dataResult.put("msg","wrong SPI operate "+operate+" resposne");
dataResult.put("msg", new String(Arrays.copyOfRange(data, 1, data.length)));
dataResult.put("hexContent", MiscUtil.bytesToHex(Arrays.copyOfRange(data, 1, data.length)));

returnResult.put("deviceContent", dataResult);
return MiscUtil.json2String(returnResult);*/

public class ModuleHandler {
	private List<ModuleRule> moduleRuleList=new ArrayList<>();
	private Map<String,Object> sectionMap=new HashMap<>();
	private Integer moduleTypeId;
	private MongoDBService mongoDBService;
	private WebClientService webClientService;
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandler.class);
	public ModuleHandler()
	{
	}
	public Object getRecentData(DeviceModel deviceModel,int timeSeconds)
	{
		return null;
	}
	public WebClientService getWebClientService()
	{
	    if (webClientService == null) {
	        synchronized (this) {
	            if (webClientService == null) {
	            	webClientService = SpringUtil.getBean(WebClientService.class);
	            }
	        }
	    }
	    return webClientService;
	}
    protected Map<String,Object> utilFastErrorResult(String type,Map<String,Object> returnResult,Map<String,Object> dataResult,String msg)
    {
    	if(dataResult==null)
    		dataResult=new HashMap<>();
    	if(returnResult==null)
    		returnResult=new HashMap<>();
		returnResult.put("type", type);
		dataResult.put("msg",msg);
		returnResult.put("deviceContent", dataResult);
		return returnResult;
    }
	public MongoDBService getMongoDBService()
	{
	    if (mongoDBService == null) {
	        synchronized (this) {
	            if (mongoDBService == null) {
	                mongoDBService = SpringUtil.getBean(MongoDBService.class);
	            }
	        }
	    }
	    return mongoDBService;
	}
	public Integer getModuleTypeId() {
		return moduleTypeId;
	}
	public void setModuleTypeId(Integer moduleTypeId) {
		this.moduleTypeId = moduleTypeId;
	}
	public Collection<ModuleRuleLogModel> evaluateRule(Object... args)
	{
		return null;
	}
	public Map<String,Object> getSectionMap()
	{
		return sectionMap;
	}
	
	public List<ModuleRule> getModuleRuleList() {
		return moduleRuleList;
	}

	public void ruleAdd(ModuleRule rule)
	{
		moduleRuleList.add(rule);
	}
	public String getRefreshOperate()
	{
		return null;
	}
	protected Map<String,Object> getChartDataExe(DeviceModel deviceModel,Instant startTime,Instant endTime,Bson redact,Map<String,Object> chartMeta)
	{
    	int startTimeMinutes=(int)(startTime.getEpochSecond()/60);
    	int endTimeMinutes=(int)(endTime.getEpochSecond()/60);
    	int diffMinutes=endTimeMinutes-startTimeMinutes;
    	int groupMinutes=diffMinutes/30;
    	//int groupMinutes=diffMinutes/3;
    	if(groupMinutes>24*60)
    		groupMinutes=24*60;
    	else if(groupMinutes>60)
    		groupMinutes-=(groupMinutes%60);
    	else if(groupMinutes>5)
    		groupMinutes-=(groupMinutes%5);
    	else
    		groupMinutes=1;

        // 时间格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.systemDefault());

        // 格式化时间
        String startTimeStr = formatter.format(startTime);
        String endTimeStr = formatter.format(endTime);
        String currentTimeStr = formatter.format(Instant.now());
        String fileName=String.format("report_%s_group_%dmin_%s_to_%s_%s.xlsx", deviceModel.getDeviceNo(), groupMinutes,startTimeStr, endTimeStr, currentTimeStr);
        chartMeta.put("excelName", fileName);
    	
    	String timeFormat="HH:mm";
    	chartMeta.put("chartNameInner", groupMinutes+"-min granularity");
    	if(groupMinutes>=24*60) {
    		timeFormat="MM-dd";
    		chartMeta.put("chartNameInner",groupMinutes/(24*60)+"-d granularity");
    	}else if(groupMinutes>=60)
    	{
    		timeFormat="MM-dd HH";
    		chartMeta.put("chartNameInner",groupMinutes/60+"-h granularity");
    	}
    	startTimeMinutes-=startTimeMinutes%groupMinutes;
    	diffMinutes=endTimeMinutes-startTimeMinutes;
    	List<Document> result=getMongoDBService().aggregatedModuleDataQuery(deviceModel,getModuleTypeId(), startTime,endTime,redact,chartMeta,groupMinutes);
    
    	if((result==null)||(result.size()==0))
    		return null;
    	

    	String[] xAxisData=new String[result.size()];
    	Map<String,List<String>> yAxisDataMap=new HashMap<>();
    	@SuppressWarnings("unchecked")
		List<Map<String, String>>seriesMeta=(List<Map<String, String>>)chartMeta.get("seriesMeta");
        for(Map<String, String> serieMap:seriesMeta)
        {
        	String field1=serieMap.get("field").toString();
        	String field2=MiscUtil.capitalizeFirstLetter(field1);
        	String operator=serieMap.get("operator").toString();
        	String field3=operator+field2;
        	yAxisDataMap.put(field3, new ArrayList<String>());
        }

    	for(int i=0;i<result.size();i++)
    	{
    		Document doc=result.get(i);
    		xAxisData[i]=MiscUtil.dateFormat(Instant.ofEpochMilli(doc.getLong("_id")),timeFormat);
    		for(String returnField:yAxisDataMap.keySet())
    		{
    			yAxisDataMap.get(returnField).add(MiscUtil.strDisplay(doc.get(returnField)).toString());
    		}
    	}
    	Map<String,Object> resultReturn=new HashMap<>();
    	resultReturn.put("xAxisData", xAxisData);
    	resultReturn.put("yAxisData", yAxisDataMap);
    	resultReturn.put("chartMeta", chartMeta);
    	return resultReturn;

	}
    public List<Map<String,Object>> getAlert(DeviceModel deviceModel)
    {
    	Map<String, Object> conditionMap = Map.of(
    		    "deviceId", deviceModel.getId(),
    		    "moduleTypeId", moduleTypeId
    		);
        Sort sort = Sort.by(
                Sort.Order.desc("uploadTime")
        );
        List<ModuleRuleLogModel> logList=getMongoDBService().find(conditionMap, sort, ModuleRuleLogModel.class,0,10);
        if((logList!=null)&&(logList.size()>0))
        {
        	List<Map<String,Object>> result=new ArrayList<>();
        	for(ModuleRuleLogModel log:logList)
        		MiscUtil.resultPutRow(result, "id",log.getId(),"ruleId",log.getRuleId()+"","ruleName",log.getRuleName(),"ruleOperator",log.getRuleOperator(),"status",log.getStatus()+"","reasonId",log.getReasonId()+"","reasonContent",log.getReason(),"uploadTime",MiscUtil.dateFormat(log.getUploadTime(),"MM-dd HH:mm"));
        	return result;
        }
        return null;
    }
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel)
    {
    	if((deviceModel.getDeviceConfig()==null)||(deviceModel.getDeviceConfig().size()==0)||(moduleTypeId==null))
    		return null;
    	List<DeviceConfigElementModel> elementList=deviceModel.getDeviceConfig().values().stream().filter(device -> device.getModule()==moduleTypeId).collect(Collectors.toList());
    	if((elementList==null)||(elementList.size()==0))
    		return null;
    	DeviceConfigElementModel versionElementModel=deviceModel.getDeviceConfig().get(0);    	
    	if((moduleTypeId.intValue()>0)&&(versionElementModel!=null))
    		elementList.add(0, versionElementModel);
    	return utilConfigDisplay(elementList);
    }
	public static List<Map<String, Object>> utilConfigDisplay(List<DeviceConfigElementModel> elementList) {
		List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
    	for(DeviceConfigElementModel element:elementList)
    	{
    		Map<String,Object> row=new HashMap<String,Object>();
    		row.put("id",element.getIdex());
    		row.put("module",ModuleDefine.ModuleType.fromId(element.getModule()).getName()); 
    		row.put("name",element.getName());
    		
    		String typeStr="Number";
    		if(element.getType()==1)
    			typeStr="Enum";
    		else if(element.getType()==51)
    			typeStr="String";
    		else if(element.getType()==52)
    			typeStr="IP_V4";
    		else if(element.getType()==53)
    			typeStr="IP_V6";
    		else if(element.getType()==54)
    			typeStr="IP_URL";
    		
    		row.put("type",typeStr);
    		row.put("valueInDevice",MiscUtil.strDisplay(element.getValueInDevice()));
    		row.put("valueInDb",MiscUtil.strDisplay(element.getValueInDb()));
    		row.put("valueLen",element.getValueLen());
    		String help="";
    		if(element.getType()==0)
    			help="Number between "+element.getMinValue()+" and "+element.getMaxValue();
    		else if((element.getType()==1)&&(element.getEnumValue()!=null))
    		{
    			help="Number should be";
    			for(Integer option:element.getEnumValue())
    				help=help+" "+option;
    		}else if(element.getType()==51)
    			help="Length of string between "+element.getMinValue()+" and "+element.getMaxValue();
    		else if(element.getType()==52)
    			help="IP v4 length between "+element.getMinValue()+" and "+element.getMaxValue();
    		else if(element.getType()==53)
    			help="IP v6 length between "+element.getMinValue()+" and "+element.getMaxValue();
    		else if(element.getType()==54)
    			help="IP or URL length between "+element.getMinValue()+" and "+element.getMaxValue();
    		row.put("help",help);
    		row.put("status",element.getStatus());
    		
    		result.add(row);
    	}
    	return result;
	}
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(moduleTypeId); 
    	MiscUtil.resultPutRow(result, "name","Last Request Time","value",MiscUtil.dateFormat(moduleInfoModel.getRequestTime(),"MM-dd HH:mm:ss"));
    	MiscUtil.resultPutRow(result, "name","Request Times","value",MiscUtil.strDisplay(moduleInfoModel.getRequestTimes()));
    	MiscUtil.resultPutRow(result, "name","Last Upload Time","value",MiscUtil.dateFormat(moduleInfoModel.getUploadTime(),"MM-dd HH:mm:ss"));
    	MiscUtil.resultPutRow(result, "name","Upload Times","value",MiscUtil.strDisplay(moduleInfoModel.getUploadTimes()));
		return result;
    }

    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	return null;
    }
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
		int displayTypeInt=5;
		
    	Map<String, Object> conditionMap = Map.of(
    		    "deviceId", deviceModel.getId(),
    		    "moduleTypeId",getModuleTypeId(),
    		    "startTime", startTime,
    		    "endTime", endTime
    		);
        Sort sort = Sort.by(
                Sort.Order.desc("uploadTime")
        );
        List<DataCommModel> dataCommModelList=null;
        if((displayType!=null)&&(displayType.equals("exportall")))
        	dataCommModelList=getMongoDBService().find(conditionMap, sort, DataCommModel.class,0,50000);
        else
        	dataCommModelList=getMongoDBService().find(conditionMap, sort, DataCommModel.class,displayTypeInt*page,displayTypeInt*(page+1));
        if((dataCommModelList!=null)&&(dataCommModelList.size()>0))
        {
        	List<Map<String, Object>> dataList = dataCommModelList.stream()
		    .map(dataModel -> {
		        Map<String, Object> map = new HashMap<>();
	        	map.put("uploadTime", MiscUtil.dateFormat(dataModel.getUploadTime(),"MM-dd HH:mm:ss"));    		 
	        	map.put("upload", MiscUtil.jsonObject2String(dataModel.getUpload()));
	        	map.put("requestTime",  MiscUtil.dateFormat(dataModel.getRequestTime(),"MM-dd HH:mm:ss"));
	        	map.put("request", MiscUtil.jsonObject2String(dataModel.getRequest()));
	        	map.put("info", MiscUtil.strDisplay(dataModel.getInfo()));
		        map.put("error", MiscUtil.strDisplay(dataModel.getErrorType()));
		        map.put("commType", MiscUtil.strDisplay(dataModel.getDataCommType()));
		        map.put("commSource", MiscUtil.strDisplay(dataModel.getDataCommSource()));       	
		        return map;
		    })
		    .collect(Collectors.toList());
        	Map<String,Object> result=new HashMap<>();
        	result.put("dataList", dataList);
        	result.put("page", page);
        	return result;
        }
        return null;
    }
    public Map<String,Object> getChartData(DeviceModel deviceModel,Instant startTime,Instant endTime,String displayType,Integer page)
    {
    	return null;
    }
	
    public static String utilParseVariable(byte[] data,Map<String,Object> result) {
		int idx=0;
		String propertyStr="";
		//String t="04696d65690100000569636369640100000e616c5f72756e5f7365636f6e6473000400d00500000f616c5f74696d65735f7265636f6e6e0004000b0000000d616c5f6d6574615f74696d6573000400700000001172745f726573746172745f726561736f6e000400000000001772745f726573746172745f726561736f6e5f74696d6573000400000000000f72745f72657365745f726561736f6e000400010000001072745f726573746172745f74696d6573000400010000001972745f726573746172745f726561736f6e5f7365636f6e6473000400000000000872745f34675f6970000400000000000a72745f34675f6d61736b000400000000000872745f34675f6777000400000000000a72745f34675f646e7331000400000000000a72745f34675f646e7332000400000000000a72745f34675f69707636001000000000000000000000000000000000000f72745f34675f697076365f7a6f6e65000400000000000e72745f776966695f7374615f6970000400c0a812661072745f776966695f7374615f6d61736b000400ffffff000e72745f776966695f7374615f6777000400c0a812010e72745f776966695f61705f646e73000400000000001272745f776966695f61705f7374615f6e756d00040001000000";
		//data=MiscUtil.hexToBytes(t);
		//logger.info(MiscUtil.bytesToHex(data));
		try {
			while(idx<data.length)
			{
				String oneProperty="";
				int propertyNameLen=data[idx++];//name len
				if(propertyNameLen>0)
				{
					String propertyName=new String(Arrays.copyOfRange(data, idx, idx+propertyNameLen));//name
					oneProperty=oneProperty+propertyName;
					//logger.info(propertyName);
				}
				idx+=propertyNameLen;
				int propertyFlag=(int)data[idx++];//flag
				int propertyValueLen=MiscUtil.bytesToUint16LittleEndian(Arrays.copyOfRange(data, idx, idx+2));//value len
				idx+=2;
				String propertyValueStr="";
				if(propertyValueLen>0)//value
				{
					if(propertyFlag==0) 
					{
						List<Long> tempValueList=new ArrayList<>();
						for(int numI=0;numI<propertyValueLen;numI+=4)
						{
							int tempValueLen=(numI+4)>propertyValueLen?propertyValueLen:(numI+4);
							Long tempValue=MiscUtil.bytesToUint32LittleEndianFlex(Arrays.copyOfRange(data, idx+numI, idx+tempValueLen));
							if(tempValueLen<propertyValueLen)
								propertyValueStr=propertyValueStr+tempValue+" ";
							else
								propertyValueStr=propertyValueStr+tempValue+"";
							tempValueList.add(tempValue);
						}
						if(result!=null) {
							if(tempValueList.size()==1)
								result.put(oneProperty,tempValueList.get(0));
							else if(tempValueList.size()>1)
								result.put(oneProperty,tempValueList);
						}
					}else {
						propertyValueStr=new String(Arrays.copyOfRange(data, idx, idx+propertyValueLen));
						if(result!=null)
							result.put(oneProperty, propertyValueStr);
					}
					oneProperty=oneProperty+"="+propertyValueStr;
					idx+=propertyValueLen;
				}
				propertyStr=propertyStr+oneProperty+";";
			}
		}catch(Exception e)
		{
			logger.info(MiscUtil.bytesToHex(data));
			logger.error(e.getMessage(),e);			
		}
		return propertyStr;
	}
    public void postPreprocess(DataCommModel commModel)
    {
    	
    }
}
