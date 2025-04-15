package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerSpi extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerSpi.class);
	public ModuleHandlerSpi()
	{
		super();
		getSectionMap().put("cmdContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);
    	if(result != null) {
    	    MiscUtil.resultPutRow(result, "name", "Spi Host", "value", "host=2,miso=gpio_esp_39,mosi=gpio_esp_38,sclk=gpio_esp_40");
    	    MiscUtil.resultPutRow(result, "name", "Spi Lora", "value", "device=0,cs=gpio_ext_io7,lora_busy=gpio_esp_48,lora_dio1=gpio_esp_45,lora_reset=gpio_ext_oc5,power=gpio_ext_oc1");
    	    MiscUtil.resultPutRow(result, "name", "Spi Tm7705", "value", "device=1,cs=gpio_ext_io5,reset=gpio_ext_oc0,power=gpio_ext_oc15(shared with i2c power)");
    	    MiscUtil.resultPutRow(result, "name", "Spi W5500", "value", "device=2,cs=gpio_ext_oc4,int=gpio_esp_347,power=gpio_ext_oc2");
    	    MiscUtil.resultPutRow(result, "name", "Spi Ext1", "value", "device=3,cs=gpio_ext_oc9,power=gpio_ext_io6(shared between ext1 and ext2)");
    	    MiscUtil.resultPutRow(result, "name", "Spi Ext2", "value", "device=4,cs=gpio_ext_oc12,power=gpio_ext_io6(shared between ext1 and ext2)"); 
    	    
    	    ModuleInfoModel spiInfo = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
    	    if(spiInfo != null) {
    	        MiscUtil.resultPutRow(result, "name", "Spi Status", "value", spiInfo.getUpload());    
    	    }
    	    
    	    if((result != null) && (result.size() > 0)) {
    	        return result;
    	    }
    	}
    	return null;
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh");
		return "operate=4";
	}
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	if(msgReq.getOperate()!=null) 
    	{
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
    		Long operate=msgReq.getOperate();
    		byte[] data=msgReq.getData();
	    	Map<String,Object> dataResult=new HashMap<>();
	    	if(operate==4)//SPI_PROPERTY
	    	{
	    		utilParseVariable(data,dataResult);
	    		dataResult=MiscUtil.camelCaseMap(dataResult);	    		
	    		commModel.setUpload(dataResult);
	    		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);		
	    		
				ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
				moduleInfoModel.setUpload(dataResult);
	    	}else if(operate==3)//SPI_INOUT
	    	{
	        	int type=MiscUtil.byteToUint8(data[0]);
	        	if(type==0)//warning info
	        	{
	    			commModel.setInfo(new String(Arrays.copyOfRange(data, 1, data.length)));
	    			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);			
	        	}else if(type==1){//data
	        		dataResult.put("hexContent", MiscUtil.bytesToHex(Arrays.copyOfRange(data, 1, data.length)));
		    		commModel.setUpload(dataResult);
		    		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);		
	        	}
	    	}else{
	    		commModel.setInfo("wrong SPI operate "+operate+" resposne");
    			commModel.setErrorType(DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);			
	    	}
    	}
    	return null;
    }
}