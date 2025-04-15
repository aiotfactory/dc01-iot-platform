package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataLora;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerLora extends ModuleHandler {
	//private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerLora.class);
	public ModuleHandlerLora()
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
	public String getRefreshOperate()
	{
		return "operate=21";
	}
    @Override
    public List<Map<String,Object>> getRuntimeInfoDisplay(DeviceModel deviceModel)
    {
    	List<Map<String,Object>> result=super.getRuntimeInfoDisplay(deviceModel);
    	if(result!=null)
    	{
    		DataLora dataLoraModel=(DataLora)deviceModel.getModuleInfoModelMap().get(getModuleTypeId()).getUpload(); 
        	if(dataLoraModel!=null)
        	{                	
        		MiscUtil.resultPutRow(result, "name","Tx Times","value",dataLoraModel.getTxTimes()+"");	
        		MiscUtil.resultPutRow(result, "name","Tx Times Timeout","value",dataLoraModel.getTxTimesTimeout()+"");	
        		MiscUtil.resultPutRow(result, "name","Rx Times","value",dataLoraModel.getRxTimes()+"");	
        		MiscUtil.resultPutRow(result, "name","Rx Times Timeout","value",dataLoraModel.getRxTimesTimeout()+"");	
        		MiscUtil.resultPutRow(result, "name","Rx Times Error","value",dataLoraModel.getRxTimesError()+"");	
        	}
        	if((result!=null)&&(result.size()>0))
        		return result;
    	}
    	return null;
    }

    //0 meta, 1 dtu, 2 info, 
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	DataCommModel.DataCommType commType=msgReq.getOperate()==null?DataCommModel.DataCommType.PERIOD_UPLOAD:DataCommModel.DataCommType.REQUEST_UPLOAD;
		commModel.setDataCommType(commType);
    	byte[] data=msgReq.getData();
    	//logger.info(MiscUtil.bytesToHex(data));
    	if((data==null)||(data.length==0)) {
			commModel.setInfo("device return wrong");
			commModel.setErrorType(DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);
			return null;
    	}
    	int type=MiscUtil.byteToUint8(data[0]);
    	if((msgReq.getOperate()!=null)&&((type==2)||(type==3)))//info or errror
    	{
			commModel.setInfo(new String(Arrays.copyOfRange(data, 1, data.length)));
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			return null;
    	}
    	DataLora dataLora=new DataLora();
    	if(type==1)//dtu
    	{
    		dataLora.setUploadDatatype("dtu");
    		dataLora.setRxData(Arrays.copyOfRange(data, 1, data.length));
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
			commModel.setDataCommType(DataCommModel.DataCommType.INTERRUPT_UPLOAD);
	    	commModel.setUpload(dataLora);
    		return null;
    	}else if(type==0)//meta
    	{
        	commModel.setUpload(dataLora);
    		dataLora.setUploadDatatype("property");
    		Map<String,Object> result=new HashMap<>();
        	utilParseVariable(Arrays.copyOfRange(data, 1, data.length),result);
        	if(result.containsKey("lora_tx_times")) 
        		dataLora.setTxTimes((Long)result.get("lora_tx_times"));
        	if(result.containsKey("lora_tx_times_timeout")) 
        		dataLora.setTxTimesTimeout((Long)result.get("lora_tx_times_timeout"));
        	if(result.containsKey("lora_rx_times")) 
        		dataLora.setRxTimes((Long)result.get("lora_rx_times"));
        	if(result.containsKey("lora_rx_times_timeout")) 
        		dataLora.setRxTimesTimeout((Long)result.get("lora_rx_times_timeout"));
        	if(result.containsKey("lora_rx_times_error")) 
        		dataLora.setRxTimesError((Long)result.get("lora_rx_times_error"));

	    	ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
	    	moduleInfoModel.setUpload(dataLora);
	    	commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
    		return null;
    	}else {
			commModel.setInfo("lora receive wrong data");
			commModel.setErrorType(DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);
			return null;
    	}
    }
}    
