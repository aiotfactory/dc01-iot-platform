package com.zyc.dc.service.module;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DataForward;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.MiscUtil;

public class ModuleHandlerForward extends ModuleHandler {
	private static final Logger logger = LoggerFactory.getLogger(ModuleHandlerForward.class);
	public ModuleHandlerForward()
	{
		super();
		getSectionMap().put("configContainer", 1);
		getSectionMap().put("forwardContainer", 1);
		getSectionMap().put("runtimeContainer", 1);
		getSectionMap().put("cmdContainer", 1);
		getSectionMap().put("historyDataContainer", 1);
	}
    @Override
    public List<Map<String,Object>> getConfigDisplay(DeviceModel deviceModel){
        return super.getConfigDisplay(deviceModel);
    }
    @Override
	public String getRefreshOperate()
	{
    	logger.info("getRefreshOperate");
		return "operate=17 hex_input=0x303132";
	}
    @Override
    public byte[] recvUpload(DeviceModel deviceModel,MessageReq msgReq,DataCommModel commModel)
    {
    	DataForward dataForward=new DataForward();
    	dataForward.setBase64Data(MiscUtil.base64Encode(msgReq.getData()));
    	commModel.setUpload(dataForward);
		commModel.setErrorType(DataCommModel.DataCommErrorType.OK);
		if(msgReq.getOperate()==null) 
			commModel.setDataCommType(DataCommModel.DataCommType.PERIOD_UPLOAD);
		else
			commModel.setDataCommType(DataCommModel.DataCommType.REQUEST_UPLOAD);
		return null;
    }
}