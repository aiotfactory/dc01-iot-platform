package com.zyc.dc.service.module;

import java.time.Instant;
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
import org.springframework.data.mongodb.core.query.Criteria;

import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataTm7705;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.dao.ModuleRuleLogModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;
import com.zyc.dc.service.module.ModuleRule.ModuleRuleResult;

public class ModuleHandlerTm7705 extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerTm7705.class);
	public ModuleHandlerTm7705()
	{
		super();
		ruleAdd(ModuleRule.MAX);
		ruleAdd(ModuleRule.AVG);
		ruleAdd(ModuleRule.MIN);
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("cmdContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
		getSectionMap().put("alertInfoContainer", 1);
		getSectionMap().put("aggChartContainer", 1);
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
        	MiscUtil.resultPutRow(result, "name","Spi Host","value","host=2,miso=gpio_esp_39,mosi=gpio_esp_38,sclk=gpio_esp_40");
        	MiscUtil.resultPutRow(result, "name","Spi TM7705","value","device=1,cs=gpio_ext_io5,reset=gpio_ext_oc0,power=gpio_ext_oc15(shared with i2c power)");
        	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        	if(moduleInfoModel!=null)
        		MiscUtil.resultPutRow(result, "name","TM7705 Status","value",MiscUtil.jsonObject2String(moduleInfoModel.getUpload()));	
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }


    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	byte[] data= msgReq.getData();
		if(msgReq.getOperate()==null) //period upload
		{
			int pin=MiscUtil.byteToUint8(data[0]);
	    	int flag=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, 1, 5));
	    	int ml01=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, 5, 9));
	    	int ml02=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, 9, 13));
			if(flag<0)
	    	{
				commModel.setInfo("tm7705 is error");
				commModel.setErrorType(DataCommModel.DataCommErrorType.MODULE_UNAVAILABLE);
				commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
	    	}else
	    	{
	        	DataTm7705 dataTm7705=new DataTm7705();
	        	if((pin&0x01)>0)
	        		dataTm7705.setPin1Value(ml01);
	        	if((pin&0x02)>0)
	        		dataTm7705.setPin2Value(ml02);
	        	commModel.setUpload(dataTm7705);
				commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
				commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
				evaluateRule(commModel);
	    	}
		}else //request
		{
			int whichRequest=MiscUtil.byteToUint8(data[0]);
			if(whichRequest==0)//tm7705_read
			{
				int pin=MiscUtil.byteToUint8(data[1]);
		    	int flag=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, 2, 6));
		    	int ml01=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, 6, 10));
		    	int ml02=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, 10, 14));
				if(flag<0)
		    	{
					commModel.setInfo("tm7705 is error");
					commModel.setErrorType(DataCommModel.DataCommErrorType.MODULE_UNAVAILABLE);
					commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
		    	}else
		    	{
		        	DataTm7705 dataTm7705=new DataTm7705();
		        	if((pin&0x01)>0)
		        		dataTm7705.setPin1Value(ml01);
		        	if((pin&0x02)>0)
		        		dataTm7705.setPin2Value(ml02);
		        	commModel.setUpload(dataTm7705);
					commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
					commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
					evaluateRule(commModel);
		    	}
					
			}else {//tm7705_property
			   	Map<String,Object> dataResult=new HashMap<>();
		    	utilParseVariable(Arrays.copyOfRange(data, 1, data.length),dataResult);
	        	commModel.setUpload(dataResult);
				commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
				commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
				ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
				moduleInfoModel.setUpload(dataResult);
			}
		}
		return null;
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=5";
	}
    @Override
    public Map<String,Object> getChartData(DeviceModel deviceModel,Instant startTime,Instant endTime,String displayType,Integer page)
    {
		if(startTime==null)
			startTime=MiscUtil.dateToday(0, 0, 0);
		if(endTime==null)
			endTime=Instant.now();
		startTime=MiscUtil.dateTrim(startTime, 0, 0, 0);
		endTime=MiscUtil.dateTrim(endTime, 0, 0, 0).plus(1, ChronoUnit.DAYS);
		
    	if(displayType.length()==0)
    		displayType="scatter";
    	else 
    		displayType=displayType.substring(0,displayType.length()-5);
    	String jsonString = "[{ \"operator\": \"max\", \"field\": \"pin1Value\", \"legend\": \"max pin1\", \"color\": \"#80DDFF\" },"
                           + "{ \"operator\": \"max\", \"field\": \"pin2Value\", \"legend\": \"max pin2\", \"color\": \"#9FEF5A\" },"
                           + "{ \"operator\": \"min\", \"field\": \"pin1Value\", \"legend\": \"min pin1\", \"color\": \"#FFB74D\" },"
                           + "{ \"operator\": \"min\", \"field\": \"pin2Value\", \"legend\": \"min pin2\", \"color\": \"#FF8A80\" },"
                           + "{ \"operator\": \"avg\", \"field\": \"pin1Value\", \"legend\": \"avg pin1\", \"color\": \"#B388FF\" },"
                           + "{ \"operator\": \"avg\", \"field\": \"pin2Value\", \"legend\": \"avg pin2\", \"color\": \"#FFAB91\" }"
                           + "]";

    	@SuppressWarnings("unchecked")
		List<Map<String, String>> seriesMeta=(List<Map<String, String>>)MiscUtil.jsonStr2Object(jsonString);
    	Map<String,Object> result=new HashMap<>();
    	result.put("seriesMeta", seriesMeta);
    	result.put("chartName", "TM7705 chart");
    	result.put("chartNameInner", "5-min granularity");
    	result.put("chartType", displayType);	
        Bson redact = new Document("$redact", 
                new Document("$cond", Arrays.asList(
                    new Document("$or", Arrays.asList(
                        new Document("$ne", Arrays.asList(
                            new Document("$ifNull", Arrays.asList("$upload.pin1Value", null)),
                            null
                        )),
                        new Document("$ne", Arrays.asList(
                            new Document("$ifNull", Arrays.asList("$upload.pin2Value", null)),
                            null
                        ))
                    )),
                    "$$KEEP",
                    "$$PRUNE"
                ))
            );
    	return super.getChartDataExe(deviceModel, startTime, endTime,redact, result);
    }
    @Override
	public Collection<ModuleRuleLogModel> evaluateRule(Object... args)
	{
    	DataCommModel commModel=(DataCommModel)args[0];
    	DataTm7705 dataTm7705=(DataTm7705)commModel.getUpload();
    	Criteria criteria = Criteria.where("deviceId").is(commModel.getDeviceId())
                            .and("moduleTypeId").is(commModel.getModuleTypeId());
    	List<DataCommModel> dataCommList=getMongoDBService().findLatest(criteria,30, DataCommModel.class);
    	if((dataCommList==null)||(dataCommList.size()<10))
    		return null;
    	
        boolean pin1NotNull = dataTm7705.getPin1Value() != null;
        boolean pin2NotNull = dataTm7705.getPin2Value() != null;
        List<DataTm7705> dataTm7705List=dataCommList.stream()
        .map(model -> model.getUpload())
        .filter(upload -> upload instanceof DataTm7705)
        .map(upload -> (DataTm7705) upload)
        .filter(temp -> {
            if (pin1NotNull && (temp.getPin1Value() == null)) {
                return false;
            }
            if (pin2NotNull && (temp.getPin2Value() == null)) {
                return false;
            }
            return true;
        })
        .collect(Collectors.toList());

        
    	if((dataTm7705List==null)||(dataTm7705List.size()<10))//at least
    		return null;
    	List<ModuleRuleResult> tempRuleResultList=new ArrayList<>();
    	///List<data>, field,min percent,max percent,current,deviceId,moduleType,reason
    	List<ModuleRule> moduleRuleList=getModuleRuleList();
    	if(dataTm7705.getPin1Value()!=null) {
			for(ModuleRule rule:moduleRuleList)
			{
				ModuleRuleResult tempResult=rule.evaluate(dataTm7705List,"getPin1Value",0.50d,1.50d,dataTm7705.getPin1Value(),commModel.getDeviceId(),ModuleDefine.MODULE_TM7705,"adc1");
				if(tempResult.getResult())
					tempRuleResultList.add(tempResult);
			}
    	}
    	if(dataTm7705.getPin2Value()!=null) {
			for(ModuleRule rule:moduleRuleList)
			{
				ModuleRuleResult tempResult=rule.evaluate(dataTm7705List,"getPin2Value",0.50d,1.50d,dataTm7705.getPin2Value(),commModel.getDeviceId(),ModuleDefine.MODULE_TM7705,"adc2");
				if(tempResult.getResult())
					tempRuleResultList.add(tempResult);
			}
    	}
		return ModuleRule.saveModuleRuleResult(tempRuleResultList);
	}
}