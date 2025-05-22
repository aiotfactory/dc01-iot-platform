package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataPir;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerPir extends ModuleHandler {
	//private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerPir.class);
	public ModuleHandlerPir()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
		getSectionMap().put("aggChartContainer", 1);
	}
    @Override
	protected Object[] getLLMContentExe(DataCommModel commModel,String property)
	{
    	DataPir data = (DataPir)commModel.getUpload();
		Object[] result=new Object[2];
		result[0]="string";
		switch(property)
		{
			case "status":
				result[1]=data.getPirStatus()+"";
				break;
			case "type":				
				result[1]=data.getType()+"";
				break;
			case "keepseconds":				
				result[1]=data.getStateKeepSeconds()+"";
				break;
			case "triggertimes":				
				result[1]=data.getTriggerTimesFromPreviousUpload()+"";
				break;
			case "time":				
				result[1]= MiscUtil.dateFormat(commModel.getUploadTime(), "yyyy-MM-dd HH:mm:ss");
				break;
			default:
				return null;
		}
		return result;
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }

	@Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
		Map<String,Object> result=new HashMap<>();
    	utilParseVariable(msgReq.getData(),result);
    	
    	DataPir dataPir=new DataPir();

    	if(result.containsKey("type")) 
    		dataPir.setType(((Long)result.get("type")).intValue());
    	if(result.containsKey("trigger_times_total")) 
    		dataPir.setTriggerTimesTotal((Long)result.get("trigger_times_total"));
    	if(result.containsKey("trigger_times_from_previous_upload")) 
    		dataPir.setTriggerTimesFromPreviousUpload((Long)result.get("trigger_times_from_previous_upload"));
    	if(result.containsKey("state_keep_seconds")) 
    		dataPir.setStateKeepSeconds((Long)result.get("state_keep_seconds"));
    	if(result.containsKey("pir_status")) 
    		dataPir.setPirStatus(((Long)result.get("pir_status")).intValue());
    	
    	commModel.setUpload(dataPir);
		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
		
		if(dataPir.getType()==0)
			commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
		else {
			commModel.setDataCommType(DataCommModel.DataCommType.INTERRUPT_UPLOAD);
	    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
			moduleInfoModel.incValidUploadTimes();
		}
		
		ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	moduleInfoModel.setUpload(dataPir);
    	return null;
    }
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);         
    	result.removeFirst();result.removeFirst();
        ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        if(moduleInfoModel.getUpload()!=null)
        {
        	String rawString = MiscUtil.jsonObject2String(moduleInfoModel.getUpload());
        	MiscUtil.resultPutRow(result, "name", "Latest Data","value", rawString);
        }
        if ((result != null) && (result.size() > 0)) {
            return result;
    	}
    	return null;
    }
    @Override
    public Map<String,Object> getChartData(DeviceModel deviceModel,Date startTime,Date endTime,String displayType,Integer page)
    {
		if(startTime==null)
			startTime=MiscUtil.dateToday(0, 0, 0);
		if(endTime==null)
			endTime=new Date();
		startTime=MiscUtil.dateTrim(startTime, 0, 0, 0);
		endTime=MiscUtil.dateTrim(endTime, 0, 0, 0);
		endTime=MiscUtil.dateAddDays(endTime, 1);
    	if(displayType.length()==0)
    		displayType="scatter";
    	else 
    		displayType=displayType.substring(0,displayType.length()-5);
    	String jsonString = "["
    			           + "{ \"operator\": \"max\", \"field\": \"pirStatus\", \"legend\": \"Active\", \"color\": \"#80DDFF\" }"
                           + "]";

    	@SuppressWarnings("unchecked")
		List<Map<String, String>> seriesMeta=(List<Map<String, String>>)MiscUtil.jsonStr2Object(jsonString);
    	Map<String,Object> result=new HashMap<>();
    	result.put("seriesMeta", seriesMeta);
    	result.put("chartName", "PIR chart");
    	result.put("chartNameInner", "5-min granularity");
    	result.put("chartType", displayType);	
        Bson redact = new Document("$redact", 
                new Document("$cond", Arrays.asList(
                    new Document("$or", Arrays.asList(
                        new Document("$ne", Arrays.asList(
                            new Document("$ifNull", Arrays.asList("$upload.pirStatus", null)),
                            null
                        ))
                    )),
                    "$$KEEP",
                    "$$PRUNE"
                ))
            );
    	return super.getChartDataExe(deviceModel, startTime, endTime,redact, result);
    }
}