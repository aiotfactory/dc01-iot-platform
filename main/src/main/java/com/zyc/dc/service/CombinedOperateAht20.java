package com.zyc.dc.service;

import com.zyc.dc.constant.ResultType;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.service.module.OperateRequest;

public class CombinedOperateAht20  implements CombinedOperate{
	private OperateExecutor operateExecutor;
	//private static final Logger logger = LoggerFactory.getLogger(CombinedOperateAht20.class);
	
	private String initStr="operate=11 clock_speed_hz=100000 addr=0x38 ";
	
	private ResultType aht20Write(DeviceModel deviceModel,String value,OperateRequest request)
	{
		value=value.toLowerCase().trim();
		if(!value.startsWith("0x"))
			value="0x"+value;
		String tempStr=initStr+"flag=1 tx_data="+value;
		request.setRawString(tempStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		return result;
	}
	private ResultType aht20Read(DeviceModel deviceModel,int valueLen,OperateRequest request)
	{
		String tempStr=initStr+"flag=0 rx_data_len="+valueLen;
		request.setRawString(tempStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		return result;
	}

	public ResultType read(DeviceModel deviceModel,OperateExecutor operateExecutor,OperateRequest request)
	{
		this.operateExecutor=operateExecutor;
		Integer aht20Lock=deviceModel.getLockInfoModel().getAht20Status();
		if((aht20Lock==null)||(aht20Lock==0))
		{			
			if(aht20Write(deviceModel,"ac3300",request).getErrorType()<0) //reset
				return new ResultType(-3,"aht20 failed reset");
			//logger.info("OperateExecutor aht20 device "+deviceModel.getDeviceNo()+" ready");
			deviceModel.getLockInfoModel().setAht20Status(1);//inited success
			deviceModel.saveLockInfoModel();
		}
		ResultType result=aht20Read(deviceModel,7,request);//read
		if(result.getErrorType()<0)
		{
			deviceModel.getLockInfoModel().setAht20Status(0);
			deviceModel.saveLockInfoModel();
			return new ResultType(-7,"aht20 failed read");
		}
		
		byte[] data=MiscUtil.hexToBytes(result.getHexContent());
		if(((data[0] & 0x98) == 0x18) && (MiscUtil.crcCalc(data, 6, 0x31)==data[6]))//crc
		{
			long s32x = (data[1] & 0xFF);
			s32x = (s32x << 8);
	        s32x += (data[2] & 0xFF);
	        s32x = (s32x << 8);
	        s32x += (data[3] & 0xFF);
	        s32x = (s32x >> 4);
	        long humidity = (s32x * 10000) / (1024 * 1024); 
	        
	        s32x = (data[3] & 0x0F);
	        s32x = (s32x << 8);
	        s32x += (data[4] & 0xFF);
	        s32x = (s32x << 8);
	        s32x += (data[5] & 0xFF);
	        long temperature = (s32x * 20000) / (1024 * 1024) - 5000;
	        
	        float humidityFloat=humidity;
	        float temperatureFloat=temperature;
	        // 错误校验
	        if (temperature < -4000 || temperature > 9900 ||
	            humidity < 0 || humidity > 10000) {
				deviceModel.getLockInfoModel().setAht20Status(0);
				deviceModel.saveLockInfoModel();
				return new ResultType(-8,"aht20 failed wrong value "+(temperatureFloat/100)+" humidity="+(humidityFloat/100)+"%");
	        }
	    	return new ResultType(0,"exe ok","aht20 temperature="+(temperatureFloat/100)+" humidity="+(humidityFloat/100)+"%");
		}
		deviceModel.getLockInfoModel().setAht20Status(0);
		deviceModel.saveLockInfoModel();
		return new ResultType(-6,"aht20 failed wrong flag");
	}
}
