package com.zyc.dc.service;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zyc.dc.constant.ResultType;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.service.module.OperateRequest;

public class CombinedOperateSht30  implements CombinedOperate{
	private OperateExecutor operateExecutor;
	private static final Logger logger = LoggerFactory.getLogger(CombinedOperateSht30.class);
	
	private String initStr="operate=11 clock_speed_hz=100000 addr=0x44 ";
	
	private ResultType sht30Write(DeviceModel deviceModel,String value,OperateRequest request)
	{
		value=value.toLowerCase().trim();
		if(!value.startsWith("0x"))
			value="0x"+value;
		String tempStr=initStr+"flag=1 tx_data="+value;
		request.setRawString(tempStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		return result;
	}
	private ResultType sht30Read(DeviceModel deviceModel,int valueLen,OperateRequest request)
	{
		String tempStr=initStr+"flag=0 rx_data_len="+valueLen;
		request.setRawString(tempStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		return result;
	}
	public ResultType read(DeviceModel deviceModel,OperateExecutor operateExecutor,OperateRequest request)
	{
		this.operateExecutor=operateExecutor;
		Integer sht30Lock=deviceModel.getLockInfoModel().getSht30Status();
		if((sht30Lock==null)||(sht30Lock==0))
		{			
			if(sht30Write(deviceModel,"30a2",request).getErrorType()<0) //reset
				return new ResultType(-3,"sht30 failed reset");
			logger.info("OperateExecutor sht30 device "+deviceModel.getDeviceNo()+" ready");
			deviceModel.getLockInfoModel().setSht30Status(1);//inited success
			deviceModel.saveLockInfoModel();
		}
		if(sht30Write(deviceModel,"2c0d",request).getErrorType()<0) //config
		{
			deviceModel.getLockInfoModel().setSht30Status(0);
			deviceModel.saveLockInfoModel();
			return new ResultType(-6,"sht30 failed config");
		}
		ResultType result=sht30Read(deviceModel,6,request);//read
		if(result.getErrorType()<0)
		{
			deviceModel.getLockInfoModel().setSht30Status(0);
			deviceModel.saveLockInfoModel();
			return new ResultType(-7,"sht30 failed read");
		}
		byte[] data=result.getResponseBytes();
		if(((int)data[0])==1)//data
		{
			data=Arrays.copyOfRange(data, 1, data.length);
		    double tem = ((data[0]& 0xFF) << 8) | (data[1]& 0xFF);
		    double temperature = (175.0 * tem / 65535.0 - 45.0) ;  /*!< T = -45 + 175 * tem / (2^16-1), this temperature conversion formula is for Celsius ��C */
		    double hum = ((data[3]& 0xFF) << 8) | (data[4]& 0xFF);
		    double humidity = (100.0 * (float)hum / 65535.0);            /*!< RH = hum*100 / (2^16-1) */
		    if ((temperature >= -40) && (temperature <= 125) && (humidity >= 0) && (humidity <= 100)) {
		    	temperature = temperature*100;
		    	humidity = humidity*100;
		    	return new ResultType(0,"sht30 temperature="+(temperature/100)+" humidity="+(humidity/100)+"%");
		    }else {
				deviceModel.getLockInfoModel().setSht30Status(0);
				deviceModel.saveLockInfoModel();
				return new ResultType(-8,"sht30 failed wrong value "+(temperature/100)+" humidity="+(humidity/100)+"%");
		    }
		    	
		}
		deviceModel.getLockInfoModel().setSht30Status(0);
		deviceModel.saveLockInfoModel();
		return new ResultType(-6,"sht30 failed wrong flag");
	}
}
