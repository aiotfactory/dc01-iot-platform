package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataUltrasonic;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerUltrasonic extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerUltrasonic.class);
	public ModuleHandlerUltrasonic()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
		getSectionMap().put("aggChartContainer", 1);
		getSectionMap().put("custContainer", 1);
	}
    @Override
	protected Object[] getLLMContentExe(DataCommModel commModel,String property)
	{
    	DataUltrasonic data = (DataUltrasonic)commModel.getUpload();
		Object[] result=new Object[2];
		result[0]="string";
		switch(property)
		{
			case "avgcurrent":
				result[1]=data.getAvg();
				break;
			case "maxcurrent":
				result[1]=data.getMax();
				break;
			case "mincurrent":
				result[1]=data.getMin();
				break;		
			case "maxhistory":
				result[1]=data.getHistoryMax();
				break;	
			case "minhistory":
				result[1]=data.getHistoryMin();
				break;	
			case "distances":
				result[1]=data.getValues();
				break;
			default:
				return null;
		}
		return result;
	}
    
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=24";
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }
    protected List<String> getHistoryExcludedFields()
    {
    	return Arrays.asList("upload.pixels");
    }
	@Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
		byte[] dataBytes=msgReq.getData();
		if(dataBytes==null || dataBytes.length==0 )
			return null;
		
		DataUltrasonic data=new DataUltrasonic();
    	int dataIdx=0,datalen=0;
    	datalen=1;
    	int type=MiscUtil.byteToUint8(dataBytes[dataIdx]);
    	dataIdx+=datalen;
    	
    	datalen=4;
    	long totalTimes=MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(dataBytes, dataIdx, dataIdx+datalen));
    	dataIdx+=datalen;
    	
    	datalen=2;
    	int historyMax=MiscUtil.bytesToUint16LittleEndian(Arrays.copyOfRange(dataBytes, dataIdx, dataIdx+datalen));
    	dataIdx+=datalen;
    	
    	datalen=2;
    	int historyMin=MiscUtil.bytesToUint16LittleEndian(Arrays.copyOfRange(dataBytes, dataIdx, dataIdx+datalen));
    	dataIdx+=datalen;
    	
    	datalen=4;
    	long cacheIdx=MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(dataBytes, dataIdx, dataIdx+datalen));
    	dataIdx+=datalen;
    	
    	int cacheSize=(dataBytes.length-dataIdx)/2;
    	long startI=0,actualLen=cacheIdx;//not full
    	
    	
    	if(cacheIdx>cacheSize)//full
    	{
    		startI=cacheIdx%cacheSize;
    		actualLen=cacheSize;
    	}
    	long min=-1,max=-1,avg=-1,latest=-1;
    	StringBuilder sb=new StringBuilder();
    	for(int i=0;i<actualLen;i++)
    	{
    		int actualIdx=Long.valueOf(dataIdx+(startI%cacheSize)*2).intValue();
    		long cacheElement=MiscUtil.bytesToUint16LittleEndian(Arrays.copyOfRange(dataBytes, actualIdx, actualIdx+2));
    		startI++;
    		if(min==-1)
    		{
    			min=cacheElement;
    			max=cacheElement;
    			avg=cacheElement;
    		}else {
    			if(cacheElement<min)
    				min=cacheElement;
    			if(cacheElement>max)
    				max=cacheElement;
    			avg+=cacheElement;
    		}
    		sb.append(cacheElement+",");
    		latest=cacheElement;
    	}
    	data.setHistoryMax(historyMax);
    	data.setHistoryMin(historyMin);
    	data.setMax(Long.valueOf(max).intValue());
    	data.setMin(Long.valueOf(min).intValue());
    	data.setAvg(Long.valueOf(avg/actualLen).intValue());
    	data.setMeasureTimes(Long.valueOf(actualLen).intValue());
    	data.setLatest(Long.valueOf(latest).intValue());
    	data.setValues(sb.toString());
    	data.setTotalMeasureTimes(totalTimes);
    	
    	DataUltrasonic.DataUltrasonicType typeObject=DataUltrasonic.DataUltrasonicType.UPLOAD;
    	if(type==1) typeObject=DataUltrasonic.DataUltrasonicType.REQUEST;
    	else if (type==2) typeObject=DataUltrasonic.DataUltrasonicType.TRIGGER;
    	data.setType(typeObject);
    	
    	commModel.setUpload(data);
		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
		
		if(msgReq.getOperate()==null) {
			if(type==0)
				commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
			else
				commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		}
		else
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		
		
		ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	moduleInfoModel.setUpload(data);
    	return null;
    }
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);         
        ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        if(moduleInfoModel.getUpload()!=null)
        {
        	DataUltrasonic data=(DataUltrasonic)moduleInfoModel.getUpload();
        	MiscUtil.resultPutRow(result, "name", "Measure Times From Reboot","value", data.getTotalMeasureTimes());
        	MiscUtil.resultPutRow(result, "name", "Max From Reboot","value", data.getHistoryMax()+"mm");
        	MiscUtil.resultPutRow(result, "name", "Min From Reboot","value", data.getHistoryMin()+"mm");
        	MiscUtil.resultPutRow(result, "name", "Latest Current Upload","value", data.getLatest()+"mm");
        	MiscUtil.resultPutRow(result, "name", "Avg Current Upload","value", data.getAvg()+"mm");
        	MiscUtil.resultPutRow(result, "name", "Max Current Upload","value", data.getMax()+"mm");
        	MiscUtil.resultPutRow(result, "name", "Min Current Upload","value", data.getMin()+"mm");
        	MiscUtil.resultPutRow(result, "name", "Measure Times Current Upload", "value",data.getMeasureTimes());
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
    					   + "{ \"operator\": \"last\", \"field\": \"latest\", \"legend\": \"Latest(mm)\", \"color\": \"#FFAB91\" },"
    			           + "{ \"operator\": \"max\", \"field\": \"max\", \"legend\": \"Max(mm)\", \"color\": \"#80DDFF\" },"
                           + "{ \"operator\": \"min\", \"field\": \"min\", \"legend\": \"Min(mm)\", \"color\": \"#9FEF5A\" },"
                           + "{ \"operator\": \"avg\", \"field\": \"avg\", \"legend\": \"Avg(mm)\", \"color\": \"#FFB74D\" }"
                           + "]";

    	@SuppressWarnings("unchecked")
		List<Map<String, String>> seriesMeta=(List<Map<String, String>>)MiscUtil.jsonStr2Object(jsonString);
    	Map<String,Object> result=new HashMap<>();
    	result.put("seriesMeta", seriesMeta);
    	result.put("chartName", "Ultrasonic chart");
    	result.put("chartNameInner", "5-min granularity");
    	result.put("chartType", displayType);	
    	result=super.getChartDataExe(deviceModel, startTime, endTime,null, result);
    	return result;
    }
}