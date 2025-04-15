package com.zyc.dc.service.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataUart;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerUart extends ModuleHandler {
	//private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerUart.class);
	public ModuleHandlerUart()
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
    	if(result!=null)
    	{
            ModuleInfoModel moduleInfoModel = deviceModel.getModuleInfoModelMap().get(getModuleTypeId());
        	if((moduleInfoModel!=null)&&(moduleInfoModel.getUpload()!=null))
        	{
        		DataUart dataUartModel = (DataUart) moduleInfoModel.getUpload();
        		MiscUtil.resultPutRow(result, "name", "Rx Length", "value", dataUartModel.getRxLength() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times", "value", dataUartModel.getRxTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Failed", "value", dataUartModel.getRxTimesFailed() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Upload Failed", "value", dataUartModel.getRxUploadFailed() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Fifo Over", "value", dataUartModel.getRxFifoOverTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Buf Full", "value", dataUartModel.getRxBufFullTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Break", "value", dataUartModel.getRxBreakTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Parity Err", "value", dataUartModel.getRxParityErrTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Rx Times Frame Err", "value", dataUartModel.getRxFrameErrTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Tx Length", "value", dataUartModel.getTxLength() + "");
        		MiscUtil.resultPutRow(result, "name", "Tx Times", "value", dataUartModel.getTxTimes() + "");
        		MiscUtil.resultPutRow(result, "name", "Tx Times Failed", "value", dataUartModel.getTxTimesFailed() + "");
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
		commModel.setDataCommType(commType);
		byte[] data=msgReq.getData();
    	DataUart dataUart=new DataUart();
    	dataUart.setProtocolType(DataUart.ProtocolType.UART);
    	if(msgReq.getOperate()==null) 
    	{
    		int type=MiscUtil.byteToUint8(data[0]);
	    	if(type==1)//dtu
	    	{
	    		commModel.setUpload(dataUart);
	        	dataUart.setDataType(DataUart.DataType.DTU);
	    		dataUart.setRxData(MiscUtil.bytesToHex(Arrays.copyOfRange(data, 1, data.length)));
	    		dataUart.setRxLength((data.length-1)*2);
				commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
				commModel.setDataCommType(DataCommModel.DataCommType.INTERRUPT_UPLOAD);
	    	}else if(type==0)//meta
	    	{
	    		dataUart.setDataType(DataUart.DataType.PROPERTY);
	    		Map<String,Object> result=new HashMap<>();
	        	utilParseVariable(Arrays.copyOfRange(data, 1, data.length),result);
	        	if(result.containsKey("rx_times")) 
	        		dataUart.setRxTimes((Long)result.get("rx_times"));
	        	if(result.containsKey("rx_length")) 
	        		dataUart.setRxLength((Long)result.get("rx_length"));
	        	if(result.containsKey("rx_times_failed")) 
	        		dataUart.setRxTimesFailed((Long)result.get("rx_times_failed"));
	        	if(result.containsKey("rx_upload_failed_times")) 
	        		dataUart.setRxUploadFailed((Long)result.get("rx_upload_failed_times"));
	        	if(result.containsKey("rx_fifo_over_times")) 
	        		dataUart.setRxFifoOverTimes((Long)result.get("rx_fifo_over_times"));
	        	if(result.containsKey("rx_buf_full_times")) 
	        		dataUart.setRxBufFullTimes((Long)result.get("rx_buf_full_times"));
	        	if(result.containsKey("rx_break_times")) 
	        		dataUart.setRxBreakTimes((Long)result.get("rx_break_times"));
	        	if(result.containsKey("rx_parity_err_times")) 
	        		dataUart.setRxParityErrTimes((Long)result.get("rx_parity_err_times"));
	        	if(result.containsKey("rx_frame_err_times")) 
	        		dataUart.setRxFrameErrTimes((Long)result.get("rx_frame_err_times"));
	        	if(result.containsKey("tx_length")) 
	        		dataUart.setTxLength((Long)result.get("tx_length"));
	        	if(result.containsKey("tx_times")) 
	        		dataUart.setTxTimes((Long)result.get("tx_times"));
	        	if(result.containsKey("tx_times_failed")) 
	        		dataUart.setTxTimesFailed((Long)result.get("tx_times_failed"));        	
	    		commModel.setUpload(dataUart);
				commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
				ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(getModuleTypeId()); 
				moduleInfoModel.setUpload(dataUart);
	    	}else {
				commModel.setInfo("uart receive wrong data");
				commModel.setErrorType(DataCommModel.DataCommErrorType.DEVICE_EXCEPTION);
	    	}
    	}else
    	{
			commModel.setInfo("tx done");
			commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
    	}
    	return null;
    }
}
