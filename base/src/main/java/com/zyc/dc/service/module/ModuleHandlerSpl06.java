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
import com.zyc.dc.dao.DataSpl06;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerSpl06 extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerSpl06.class);
	public ModuleHandlerSpl06()
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
    		MiscUtil.resultPutRow(result, "name", "SPL06 I2C Address", "value", "0x77");
    		MiscUtil.resultPutRow(result, "name", "SPL06 Spec", "value", "Pressure sensor precision: ± 0.006 hPa (or ±5 cm) (high precision mode), accuracy: ± 0.06 hPa (or ±50 cm) (non-linearity), ±1 hPa (or ±8 m) (absolute), temperature sensitivity: < 0.5Pa/K. Temperature Resolution 0.1°C, Accuracy ±0.5°C,  Range -40 to 85°C.");
    		ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
    		if (moduleInfoModel != null) {
    		    if (moduleInfoModel.getUpload() != null) {
    		    	DataSpl06 dataSPL06 = (DataSpl06) moduleInfoModel.getUpload();
    		    	MiscUtil.resultPutRow(result, "name", "Properties Since Reboot", "value", "");
    		        MiscUtil.resultPutRow(result, "name", "Read Times", "value", dataSPL06.getTimesTotal());
    		        MiscUtil.resultPutRow(result, "name", "Read Success Times", "value", dataSPL06.getTimesSuccess());
    		        MiscUtil.resultPutRow(result, "name", "Latest Pressure(Pa)|Temperature(°C)", "value",  MiscUtil.int2Float(dataSPL06.getPressure(), 100, "%.2f")+"|"+MiscUtil.int2Float(dataSPL06.getTemperature(), 100, "%.2f"));
    		        MiscUtil.resultPutRow(result, "name", "Pressure Min|Max(Pa)", "value", MiscUtil.int2Float(dataSPL06.getPressureMin(), 100, "%.2f")+"|"+MiscUtil.int2Float(dataSPL06.getPressureMax(), 100, "%.2f"));
    		        MiscUtil.resultPutRow(result, "name", "Temperature Min|Max(°C)", "value",MiscUtil.int2Float(dataSPL06.getTemperatureMin(), 100, "%.2f")+"|"+MiscUtil.int2Float(dataSPL06.getTemperatureMax(), 100, "%.2f"));
    		        
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
    	DataSpl06 dataSPL06=new DataSpl06();
    	commModel.setUpload(dataSPL06);
    	int idex=0;
    	dataSPL06.setFlag(data[idex++]&0xff);
    	dataSPL06.setTemperature(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setPressure(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setTimesTotal(MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setTimesSuccess(MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setTemperatureMax(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setTemperatureMin(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setPressureMax(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		dataSPL06.setPressureMin(MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, idex,idex+4)));
		idex+=4;
		commModel.setErrorType(dataSPL06.getFlag()==0?DataCommModel.DataCommErrorType.OK:DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);

    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	moduleInfoModel.setUpload(dataSPL06);
    	return null;
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=20";
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
    	String jsonString = "[{ \"operator\": \"avg\", \"field\": \"pressure\", \"legend\": \"Pressure(Pa)\", \"color\": \"#FF0087\" },"
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
    		List<String> pressureList=yAxisData.get("avgPressure");
    		if(pressureList!=null) 
    	        yAxisData.put("avgPressure",pressureList.stream().map(s -> Float.parseFloat(s) / 100).map(f -> String.format("%.2f", f)).collect(Collectors.toList()));
    	}
    	
    	return result;
    }
}
