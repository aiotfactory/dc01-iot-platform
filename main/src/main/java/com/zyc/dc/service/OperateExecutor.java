package com.zyc.dc.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zyc.dc.constant.ResultType;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.pojo.MessageRsp;
import com.zyc.dc.service.module.ModuleDefine;
import com.zyc.dc.service.module.OperateContent;
import com.zyc.dc.service.module.OperateRequest;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class OperateExecutor {
	@Autowired
	private CommService commService;
	private static final Logger logger = LoggerFactory.getLogger(OperateExecutor.class);
    private Map<String, CombinedOperate> combinedOperateMap = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        combinedOperateMap.put("sht30", new CombinedOperateSht30());
        combinedOperateMap.put("tm7705", new CombinedOperateTm7705());
    }
	public ResultType exeOperate(OperateRequest request, DeviceModel deviceModel) {
		ResultType result=new ResultType(-1,"error occured");
		String operateStr=request.getRawString();
		if(operateStr.length()>0)
		{
			//process combined commands
			CombinedOperate combinedOperate=combinedOperateMap.get(operateStr);
			if(combinedOperate!=null)
				return combinedOperate.read(deviceModel, this,request);
			
			OperateContent operateContent=OperateContent.parseOperateLine(operateStr);
			if(operateContent.getErrorType()==0)
			{
				byte[] commandData=operateContent.bytesValue();
		        MessageRsp msgRsp=commService.commandSend(ModuleDefine.COMMAND_ACROSS_REQ, ModuleDefine.COMMAND_FLAG_FROM_SERVER|ModuleDefine.COMMAND_FLAG_INIT_FROM_SERVER|ModuleDefine.COMMAND_FLAG_RAW_DATA, deviceModel, commandData,request);
				//String test1="07007370695f627573000100012200302e6e6f6e616d653a696e697465643d302c74783d302c72783d302c6661696c3d30000100002200312e6e6f6e616d653a696e697465643d302c74783d302c72783d302c6661696c3d30000100002200322e6e6f6e616d653a696e697465643d312c74783d302c72783d302c6661696c3d30000100012200332e6e6f6e616d653a696e697465643d302c74783d302c72783d302c6661696c3d30000100002200342e6e6f6e616d653a696e697465643d302c74783d302c72783d302c6661696c3d3000010000";
				//String test1="77726f6e6720636f6d6d616e642036";
				//MessageRsp msgRsp=new MessageRsp("24587cd06b90",4,0,3,MiscUtil.hexToBytes(test1),null,0);
				int commandStatus=msgRsp.getErrorType().intValue();
				String dataStr=msgRsp.getStringData();
				result.setResponseStr(dataStr);
				result.setResponseBytes(msgRsp.getData());
				//boolean successUpload=false;
		        switch(commandStatus)
		        {
		        	case 0:
		        		result.setErrorType(0);
		        		result.setErrorString("exe ok");
		    			DataCommModel dataCommModel=MiscUtil.jsonStr2ClassType(dataStr, DataCommModel.class);
		    			result.setDataComm(dataCommModel);
		        		break;
			        case -1:
		        		result.setErrorType(-1);
		        		result.setErrorString("device not exist");
			        	break;
			        case -2:
			        case -4:
		        		result.setErrorType(-2);
		        		result.setErrorString("device not online");
			        	break;
			        case -3:
		        		result.setErrorType(-3);
		        		result.setErrorString("device dirty data");
			        	break;
			        case -10:
		        		result.setErrorType(-10);
		        		result.setErrorString("unknown exception with TCP");
			        	break;
			        case -11:
		        		result.setErrorType(-11);
		        		result.setErrorString("wrong md5 from TCP");
			        	break;
			        case -12:
		        		result.setErrorType(-12);
		        		result.setErrorString("wrong magic from TCP");
			        	break;
			        case -13:
		        		result.setErrorType(-13);
		        		result.setErrorString("close exception with TCP");
			        	break;
			        case -21:
		        		result.setErrorType(-21);
		        		result.setErrorString("time out with device");
			        	break;
			        case -22:
		        		result.setErrorType(-22);
		        		result.setErrorString("unknown exception with device");
			        	break;
			        case -23:
		        		result.setErrorType(-23);
		        		result.setErrorString("no device connection ctx");
			        	break;
			        case -31:
		        		result.setErrorType(-24);
		        		result.setErrorString("connect TCP timeout");
			        	break;
			        case -32:
		        		result.setErrorType(-25);
		        		result.setErrorString("connect TCP unknown exception");
			        	break;
		        }
			}else
				result=new ResultType(-26,operateContent.getErrorString());
			result.setOperateSpec(operateContent.getOperateSpec());
		}else {
			result=new ResultType(-27,"operate empty");
		}
		return result;
	}
    public ResultType refreshInfo(String operate,DeviceModel deviceModel,HttpServletRequest request) {

    	ResultType result=null;
    	if(operate!=null) 
    	{
    		OperateRequest operateRequest=new OperateRequest();
    		operateRequest.setRawString(operate);
    		operateRequest.setIp(request.getRemoteHost());
    		operateRequest.setSourceType(DataCommModel.DataCommSource.CLOUD_WEB);
    		result=SpringUtil.getBean(OperateExecutor.class).exeOperate(operateRequest, deviceModel);
    	}
    	else {
    		result=new ResultType(-25,"no refresh feature");
    	}
    	if(result.getErrorType()<0)
    		logger.info("refresh "+operate+" error with "+result.getErrorString());
		return result;
    }
}
