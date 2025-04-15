package com.zyc.dc.service.module;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyc.dc.dao.DataAht20;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerAht20 extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerAht20.class);
	public ModuleHandlerAht20()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("cmdContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
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
    		MiscUtil.resultPutRow(result, "name", "AHT20 I2C Address", "value", "0x38");
    		MiscUtil.resultPutRow(result, "name", "AHT20 Spec", "value", "Humidity Resolution 0.024, Accuracy ±2%, Repeatability ±0.1%. Temperature Resolution 0.01°C, Accuracy ±0.3°C, Repeatability ±0.1°C, Range -40 to 85°C.");
    		ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
    		if (moduleInfoModel != null) {
    		    if (moduleInfoModel.getUpload() != null) {
    		    	DataAht20 dataAht20 = (DataAht20) moduleInfoModel.getUpload();
    		    	MiscUtil.resultPutRow(result, "name", "Properties Since Reboot", "value", "");
    		        MiscUtil.resultPutRow(result, "name", "Read Times", "value", dataAht20.getTimesTotal());
    		        MiscUtil.resultPutRow(result, "name", "Read Success Times", "value", dataAht20.getTimesSuccess());
    		        MiscUtil.resultPutRow(result, "name", "Latest Humidity(%)|Temperature(°C)", "value",  MiscUtil.int2Float(dataAht20.getHumidity(), 100, "%.2f")+"|"+MiscUtil.int2Float(dataAht20.getTemperature(), 100, "%.2f"));
    		        MiscUtil.resultPutRow(result, "name", "Humidity Min|Max(%)", "value", MiscUtil.int2Float(dataAht20.getHumidityMin(), 100, "%.2f")+"|"+MiscUtil.int2Float(dataAht20.getHumidityMax(), 100, "%.2f"));
    		        MiscUtil.resultPutRow(result, "name", "Temperature Min|Max(°C)", "value",MiscUtil.int2Float(dataAht20.getTemperatureMin(), 100, "%.2f")+"|"+MiscUtil.int2Float(dataAht20.getTemperatureMax(), 100, "%.2f"));
    		        
    		    }
    		}
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }

    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	DataCommModel.DataCommType commType=msgReq.getOperate()==null?DataCommModel.DataCommType.PERIOD_UPLOAD:DataCommModel.DataCommType.REQUEST_UPLOAD;
    	byte[] data=msgReq.getData();
    	commModel.setDataCommType(commType);
    	DataAht20 dataAht20=new DataAht20();
    	commModel.setUpload(dataAht20);
    	int idex=0;
    	dataAht20.setFlag(data[idex++]&0xff);
    	dataAht20.setStatus(data[idex++]&0xff);
    	dataAht20.setHumidity(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setTemperature(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setTimesTotal(MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setTimesSuccess(MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setTemperatureMax(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setTemperatureMin(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setHumidityMax(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataAht20.setHumidityMin(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		commModel.setErrorType(dataAht20.getFlag()==0?DataCommModel.DataCommErrorType.OK:DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);

    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	moduleInfoModel.setUpload(dataAht20);
    	return null;
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=19";
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
    		displayType="line";
    	else 
    		displayType=displayType.substring(0,displayType.length()-5);
    	String jsonString = "[{ \"operator\": \"avg\", \"field\": \"humidity\", \"legend\": \"Humidity(%)\", \"color\": \"#FF0087\" },"
                           + "{ \"operator\": \"avg\", \"field\": \"temperature\", \"legend\": \"Temperature(°C)\", \"color\": \"#FFBF00\" }"
                           + "]";

    	@SuppressWarnings("unchecked")
		List<Map<String, String>> seriesMeta=(List<Map<String, String>>)MiscUtil.jsonStr2Object(jsonString);
    	Map<String,Object> result=new HashMap<>();
    	result.put("seriesMeta", seriesMeta);
    	result.put("chartName", "Humidity and Temperature chart");
    	result.put("chartNameInner", "5-min granularity");
    	result.put("chartType", displayType);
    	result=super.getChartDataExe(deviceModel, startTime, endTime,null, result);
    	@SuppressWarnings("unchecked")
		Map<String,List<String>> yAxisData=(Map<String,List<String>>)result.get("yAxisData");
    	if(yAxisData!=null)
    	{
    		List<String> temperatureList=yAxisData.get("avgTemperature");
    		if(temperatureList!=null) 
    	        yAxisData.put("avgTemperature",temperatureList.stream().map(s -> Float.parseFloat(s) / 100).map(f -> String.format("%.2f", f)).collect(Collectors.toList()));
    		List<String> humidityList=yAxisData.get("avgHumidity");
    		if(temperatureList!=null) 
    	        yAxisData.put("avgHumidity",humidityList.stream().map(s -> Float.parseFloat(s) / 100).map(f -> String.format("%.2f", f)).collect(Collectors.toList()));
    	}
    	return result;
    }
}
