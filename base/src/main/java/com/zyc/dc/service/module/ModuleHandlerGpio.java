package com.zyc.dc.service.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataGpio;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerGpio extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerGpio.class);
	public ModuleHandlerGpio()
	{
		super();
		getSectionMap().put("configContainer", 1);
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
        if((result!=null)&&(result.size()>0))
        	return result;
    	return null;
    }
    
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	parseUtil(deviceModel, msgReq.getData(),commModel);
    	return null;
    }
	private void parseUtil(DeviceModel deviceModel, byte[] data,DataCommModel commModel) {
    	int type=MiscUtil.byteToUint8(data[0]);
    	DataGpio dataGpio=new DataGpio();
    	commModel.setUpload(dataGpio);
		if(type==3)//string content
		{ 
			String tempStr=new String(Arrays.copyOfRange(data, 1, data.length));
			commModel.setInfo(tempStr);
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);			
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		}if(type==2) 
		{   //nodata
			String tempStr="gpio is off or not allowable.";
			commModel.setInfo(tempStr);
			commModel.setErrorType(DataCommModel.DataCommErrorType.MODULE_UNAVAILABLE);
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		}else if(type==1)//interrupt upload
    	{
    		long pinRaw=MiscUtil.bytesToUint32LittleEndian(Arrays.copyOfRange(data, 1, 5));
    		int pin = (int) (pinRaw & 0xFF);
    		int pinMode = (int) ((pinRaw >> 8) & 0xFF);
    		//int pinConfig = (int) ((pinRaw >> 16) & 0xFF);       		
    		int pinLevel = (int) ((pinRaw >> 31) & 0x01);
    		List<DataGpio.GpioPinValue> pinValues=new ArrayList<>();
    		DataGpio.GpioPinValue pinValue2=new DataGpio.GpioPinValue();
    		pinValue2.setPin(pin);
    		pinValue2.setPinMode(pinMode);
    		pinValue2.setPinType(DataGpio.GpioPinType.ESP);
    		pinValue2.setPinValue(pinLevel);
    		pinValue2.setValueType(DataGpio.GpioValueType.DIGITAL);
    		pinValues.add(pinValue2);
    		dataGpio.setValues(pinValues);
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			commModel.setDataCommType(DataCommModel.DataCommType.INTERRUPT_UPLOAD);
    	}else if((type==4)||(type==5)||(type==6))//normal upload=4, read request=5
    	{
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			commModel.setDataCommType(type==4?DataCommModel.DataCommType.PERIOD_UPLOAD:DataCommModel.DataCommType.REQUEST_UPLOAD);
    		int dataIdx=1;
    		List<DataGpio.GpioPinValue> pinValues=new ArrayList<>();
    		while(dataIdx<data.length)
    		{
    			int isEsp=MiscUtil.byteToUint8(data[dataIdx++]);
    			int pin=MiscUtil.byteToUint8(data[dataIdx++]);
    			int pinMode=MiscUtil.byteToUint8(data[dataIdx++]);
    			int pinValue=MiscUtil.bytesToIntLittleEndian(Arrays.copyOfRange(data, dataIdx, dataIdx+4));
    			dataIdx+=4;
    			int isLogic=MiscUtil.byteToUint8(data[dataIdx++]);
        		DataGpio.GpioPinValue pinValue2=new DataGpio.GpioPinValue();
        		pinValue2.setPin(pin);
        		pinValue2.setPinMode(pinMode);
        		pinValue2.setPinType(isEsp>0?DataGpio.GpioPinType.ESP:DataGpio.GpioPinType.EXT);
        		pinValue2.setPinValue(pinValue);
        		pinValue2.setValueType(isLogic>0?DataGpio.GpioValueType.DIGITAL:DataGpio.GpioValueType.ANALOG);
        		pinValues.add(pinValue2);
    		}
    		dataGpio.setValues(pinValues);
    	}
	}
	
    @Override
	public String getRefreshOperate()
	{
    	logger.info("refresh clicked");
		return "operate=7";
	}
}