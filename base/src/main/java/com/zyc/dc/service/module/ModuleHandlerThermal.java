package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataThermal;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

import jakarta.servlet.http.HttpServletRequest;

public class ModuleHandlerThermal extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerThermal.class);
	public ModuleHandlerThermal()
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
    	DataThermal data = (DataThermal)commModel.getUpload();
		Object[] result=new Object[2];
		result[0]="string";
		switch(property)
		{
			case "avgcurrent":
				result[1]=MiscUtil.int2Float(data.getAvgCurrent(), 10, "%.1f");
				break;
			case "maxcurrent":
				result[1]=MiscUtil.int2Float(data.getMaxCurrent(), 10, "%.1f");
				break;
			case "mincurrent":
				result[1]=MiscUtil.int2Float(data.getMinCurrent(), 10, "%.1f");
				break;		
			case "maxavghistory":
				result[1]=MiscUtil.int2Float(data.getAvgMaxHistory(), 10, "%.1f");
				break;	
			case "minavghistory":
				result[1]=MiscUtil.int2Float(data.getAvgMinHistory(), 10, "%.1f");
				break;		
			case "maxhistory":
				result[1]=MiscUtil.int2Float(data.getMaxHistory(), 10, "%.1f");
				break;	
			case "minhistory":
				result[1]=MiscUtil.int2Float(data.getMinHistory(), 10, "%.1f");
				break;	
			case "pixels":
				result[1]=formatThermalData(data.getPixels(),24,32);
				if(result[1]==null)
					return null;
				break;
			default:
				return null;
		}
		return result;
	}
    private String formatThermalData(String input, int rows, int cols) {
    	if(input==null || input.length()==0)
    		return null;
        String[] tokens = input.split(",");
        if(tokens.length!=24*32)
        	return null;
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(" ");

        for (int i = 0; i < rows; i++) {
            sb.append(" (");
            for (int j = 0; j < cols; j++) {
                int index = i * cols + j;
                if (j > 0) sb.append(", ");
                sb.append(Integer.parseInt(tokens[index]) / 10.0f);
            }
            sb.append(")");
            if (i < rows - 1) sb.append(",");
            sb.append(" ");
        }

        sb.append("]");
        return sb.toString();
    }
	@Override
	public List<Map<String,Object>> custPageList(DeviceModel deviceModel)
	{
		Map<String,Object> link1=new HashMap<>();
		link1.put("pageLink", "modulecust?deviceId="+deviceModel.getId()+"&moduleTypeId="+getModuleTypeId());
		link1.put("pageName", getMiscUtil().utilLocal("thermal_cust_name1"));
		link1.put("pageDescription", getMiscUtil().utilLocal("thermal_cust_desc1"));
		return Arrays.asList(link1);
	}
	@Override
    public void custPageRefresh(DeviceModel deviceModel,Map<String, Object> paramsBody,HttpServletRequest request,Map<String,Object> result)
    {
		DataThermal data=null;
		Date endDate=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsBody, "endDate", String.class, null),null);	
		if(endDate!=null) {
	    	Map<String, Object> conditionMap = Map.of(
	    		    "deviceId", deviceModel.getId(),
	    		    "moduleTypeId",getModuleTypeId(),
	    		    "endTime", endDate
	    		);
	        Sort sort = Sort.by(
	                Sort.Order.desc("uploadTime")
	        );
	        List<DataCommModel> dataCommModelList=getMongoDBService().find(conditionMap, sort, DataCommModel.class,0,1,null);
	        if(dataCommModelList!=null && dataCommModelList.size()==1)
	        	data=(DataThermal)dataCommModelList.get(0).getUpload();
		}else {		
			ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
			data=(DataThermal)moduleInfoModel.getUpload();
		}
		if(data!=null && data.getPixels()!=null && data.getPixels().length()>0)
			result.put("pixels", data.getPixels());
    }
    @Override
    public String custPage(Map<String, Object> paramsReq,Model model)
    {
    	
    	return "modulethermal";
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=23";
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
		
    	DataThermal data=new DataThermal();
    	
    	StringBuilder pixels=new StringBuilder();
    	int maxCurrent=-30000,maxI=-1,maxJ=-1,minI=-1,minJ=-1,minCurrent=30000;
    	double avgd=0;
    	for(int i=0;i<768;i++)
    	{
    		int pixel = (dataBytes[i*2] & 0xFF) | ((dataBytes[i*2+1] & 0xFF) << 8);
    		pixels.append(pixel+",");
    		avgd+=pixel;
    		if(pixel>maxCurrent) {
    			maxCurrent=pixel;
    			maxI=i/32;
    			maxJ=i%24;
    		}
    		if(pixel<minCurrent) { 
    			minCurrent=pixel;
    			minI=i/32;
    			minJ=i%24;
    		}
    	}
    	avgd/=768;
    	data.setAvgCurrent((int)Math.round(avgd));
    	data.setMaxCurrent(maxCurrent);
    	data.setMaxI(maxI);
    	data.setMaxJ(maxJ);
    	data.setMinCurrent(minCurrent);
    	data.setMinI(minI);
    	data.setMinJ(minJ);
    	data.setPixels(pixels.toString());
    	//logger.info("x1 " +pixels.size());
    
		Map<String,Object> result=new HashMap<>();
		dataBytes=Arrays.copyOfRange(dataBytes, 768*2, dataBytes.length);
    	utilParseVariable(dataBytes,result);
    	
    	if(result.containsKey("times_total")) 
    		data.setTimes(((Long)result.get("times_total")).intValue());
    	if(result.containsKey("max")) 
    		data.setMaxHistory(((Long)result.get("max")).intValue());
    	if(result.containsKey("min")) 
    		data.setMinHistory(((Long)result.get("min")).intValue());
    	if(result.containsKey("avg_max")) 
    		data.setAvgMaxHistory(((Long)result.get("avg_max")).intValue());
    	if(result.containsKey("avg_min")) 
    		data.setAvgMinHistory(((Long)result.get("avg_min")).intValue());
    	
    	commModel.setUpload(data);
    	
		if(data.getAvgCurrent()== data.getMaxCurrent() && data.getAvgCurrent()==data.getMinCurrent())
			commModel.setErrorType(DataCommModel.DataCommErrorType.MODULE_DATA_ERROR);
		else {
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			deviceModel.incModuleRunInfo(getModuleTypeId());
		}
		if(msgReq.getOperate()==null)
			commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
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
        	DataThermal data=(DataThermal)moduleInfoModel.getUpload();
        	MiscUtil.resultPutRow(result, "name", "Times from Reboot","value", data.getTimes());
        	MiscUtil.resultPutRow(result, "name", "Max from Reboot","value", MiscUtil.int2Float(data.getMaxHistory(), 10, "%.1f"));
        	MiscUtil.resultPutRow(result, "name", "Min from Reboot","value", MiscUtil.int2Float(data.getMinHistory(), 10, "%.1f"));
        	MiscUtil.resultPutRow(result, "name", "Max Avg from Reboot","value", MiscUtil.int2Float(data.getAvgMaxHistory(), 10, "%.1f"));
        	MiscUtil.resultPutRow(result, "name", "Min Avg from Reboot","value", MiscUtil.int2Float(data.getAvgMinHistory(), 10, "%.1f"));
        	MiscUtil.resultPutRow(result, "name", "Max Latest","value", MiscUtil.int2Float(data.getMaxCurrent(), 10, "%.1f")+"("+data.getMaxI()+","+data.getMaxJ()+")");
        	MiscUtil.resultPutRow(result, "name", "Min Latest","value", MiscUtil.int2Float(data.getMinCurrent(), 10, "%.1f")+"("+data.getMinI()+","+data.getMinJ()+")");
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
    			           + "{ \"operator\": \"max\", \"field\": \"maxCurrent\", \"legend\": \"Max(°C)\", \"color\": \"#80DDFF\" },"
                           + "{ \"operator\": \"min\", \"field\": \"minCurrent\", \"legend\": \"Min(°C)\", \"color\": \"#9FEF5A\" },"
                           + "{ \"operator\": \"avg\", \"field\": \"avgCurrent\", \"legend\": \"Avg(°C)\", \"color\": \"#FFB74D\" }"
                           + "]";

    	@SuppressWarnings("unchecked")
		List<Map<String, String>> seriesMeta=(List<Map<String, String>>)MiscUtil.jsonStr2Object(jsonString);
    	Map<String,Object> result=new HashMap<>();
    	result.put("seriesMeta", seriesMeta);
    	result.put("chartName", "Thermal chart");
    	result.put("chartNameInner", "5-min granularity");
    	result.put("chartType", displayType);	
    	result=super.getChartDataExe(deviceModel, startTime, endTime,null, result);
    	
    	@SuppressWarnings("unchecked")
		Map<String,List<String>> yAxisData=(Map<String,List<String>>)result.get("yAxisData");
    	if(yAxisData!=null)
    	{
    		List<String> tempList=yAxisData.get("maxMaxCurrent");
    		if(tempList!=null) 
    	        yAxisData.put("maxMaxCurrent",tempList.stream().map(s -> Float.parseFloat(s) / 10).map(f -> String.format("%.1f", f)).collect(Collectors.toList()));
    		tempList=yAxisData.get("minMinCurrent");
    		if(tempList!=null) 
    	        yAxisData.put("minMinCurrent",tempList.stream().map(s -> Float.parseFloat(s) / 10).map(f -> String.format("%.1f", f)).collect(Collectors.toList()));
    		tempList=yAxisData.get("avgAvgCurrent");
    		if(tempList!=null) 
    	        yAxisData.put("avgAvgCurrent",tempList.stream().map(s -> Float.parseFloat(s) / 10).map(f -> String.format("%.1f", f)).collect(Collectors.toList()));
    	}
    	
    	return result;
    }

}