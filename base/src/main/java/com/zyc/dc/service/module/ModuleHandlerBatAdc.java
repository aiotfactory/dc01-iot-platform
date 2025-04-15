package com.zyc.dc.service.module;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zyc.dc.dao.DataBatAdc;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceConfigElementModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerBatAdc extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerBatAdc.class);
	public ModuleHandlerBatAdc()
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
    		MiscUtil.resultPutRow(result, "name", "Bat Adc Pin", "value", "gpio_esp_1 adc1 channel0");
    		MiscUtil.resultPutRow(result, "name", "Bat Adc Power Onoff Pin", "value", "gpio_ext_oc6");

    		ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
    		if (moduleInfoModel != null) {
    		    if (moduleInfoModel.getUpload() != null) {
    		        DataBatAdc dataBatAdc = (DataBatAdc) moduleInfoModel.getUpload();
    		        MiscUtil.resultPutRow(result, "name", "Bat Adc Times", "value", dataBatAdc.getAdcTimes());
    		        MiscUtil.resultPutRow(result, "name", "Bat Adc Times Failed", "value", dataBatAdc.getAdcTimesFailed());
    		        MiscUtil.resultPutRow(result, "name", "Bat Adc Power Onoff", "value", dataBatAdc.getAdcPinOnoff());
    		        MiscUtil.resultPutRow(result, "name", "Bat Adc Value", "value", (Double.valueOf(dataBatAdc.getAdcValue()) / 10000) + "mV");
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
    	Map<String,Object> result=new HashMap<>();
    	utilParseVariable(msgReq.getData(),result);
    	DataBatAdc dataBatAdc=new DataBatAdc();
    	if(result.containsKey("bat_adc_times")) 
    	{
    		dataBatAdc.setAdcTimes((Long)result.get("bat_adc_times"));
    	}
    	if(result.containsKey("bat_adc_times_failed")) {
    		dataBatAdc.setAdcTimesFailed((Long)result.get("bat_adc_times_failed"));
    	}
    	if(result.containsKey("bat_adc_pin_onoff")) {
    		dataBatAdc.setAdcPinOnoff(((Long)result.get("bat_adc_pin_onoff")).intValue());
    	}
    	if(result.containsKey("bat_adc_value")) 
    	{
    		Map<Integer, DeviceConfigElementModel> config=deviceModel.getDeviceConfig();
    		if(config!=null)
    		{
    			DeviceConfigElementModel partialPressureElement=config.get(7);
    			if((partialPressureElement!=null)&&(partialPressureElement.getName().equals("adc_bat_partial_pressure")))
    			{
    				Integer partialPressureInt=MiscUtil.strParseInteger(partialPressureElement.getValueInDevice());
    				if(partialPressureInt!=null)
    				{
    					Long batAdcValueRaw=(Long)result.get("bat_adc_value");
    					Double partialPressureDouble=Double.valueOf(batAdcValueRaw)/(Double.valueOf(partialPressureInt)/10000);
    					dataBatAdc.setAdcValue((int)Math.round(partialPressureDouble*10000));	
    				}
    			}
    		}
    	}
    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
    	moduleInfoModel.setUpload(dataBatAdc);
    	commModel.setUpload(dataBatAdc);
		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
		if(msgReq.getOperate()==null) 
			commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
		else
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		return null;
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=12";
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
    	String jsonString = "[{ \"operator\": \"avg\", \"field\": \"adcValue\", \"legend\": \"avg adc value\", \"color\": \"#FF0087\" },"
                           + "{ \"operator\": \"max\", \"field\": \"adcValue\", \"legend\": \"max adc value\", \"color\": \"#FFBF00\" },"
                           + "{ \"operator\": \"min\", \"field\": \"adcValue\", \"legend\": \"min adc value\", \"color\": \"#37A2FF\" }"
                           + "]";

    	@SuppressWarnings("unchecked")
		List<Map<String, String>> seriesMeta=(List<Map<String, String>>)MiscUtil.jsonStr2Object(jsonString);
    	Map<String,Object> result=new HashMap<>();
    	result.put("seriesMeta", seriesMeta);
    	result.put("chartName", "Bat voltage chart");
    	result.put("chartNameInner", "5-min granularity");
    	result.put("chartType", displayType);
    	return super.getChartDataExe(deviceModel, startTime, endTime,null, result);
    }
}
