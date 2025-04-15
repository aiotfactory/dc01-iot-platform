package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerI2c extends ModuleHandler {
	//private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerI2c.class);
	public ModuleHandlerI2c()
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
    	if(result!=null)
    	{
        	MiscUtil.resultPutRow(result, "name","I2C Port","value","0");
        	MiscUtil.resultPutRow(result, "name","I2C Sda Pin","value","gpio_esp_42");
        	MiscUtil.resultPutRow(result, "name","I2C Scl Pin","value","gpio_esp_41");
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	byte[] data=msgReq.getData();
    	int type=MiscUtil.byteToUint8(data[0]);
    	if(type==0) {
			String tempStr=new String(Arrays.copyOfRange(data, 1, data.length));
			commModel.setInfo(tempStr);
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);			
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
    	}else if(type==1) {
    		Map<String,String> hexMap=new HashMap<>();
    		hexMap.put("hexContent", MiscUtil.bytesToHex(Arrays.copyOfRange(data, 1, data.length)));
    		commModel.setUpload(hexMap);
    	}else{
			commModel.setInfo("wrong I2C operate resposne");
			commModel.setErrorType(DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);			
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
    	}
    	return null;
    }
}