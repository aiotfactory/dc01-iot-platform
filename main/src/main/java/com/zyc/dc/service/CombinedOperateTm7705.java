package com.zyc.dc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zyc.dc.constant.GpioSpec;
import com.zyc.dc.constant.ResultType;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.service.module.OperateRequest;


public class CombinedOperateTm7705 implements CombinedOperate{
	private OperateExecutor operateExecutor;
	private static final Logger logger = LoggerFactory.getLogger(CombinedOperateTm7705.class);
	
	private String initStr="command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3";
	
//	private ResultType tm7705SpiInit(DeviceModel deviceModel)
//	{
//		String spiStr="operate=1 spi_no=1 command_bits=0 address_bits=8 dummy_bits=0 clock_speed_hz=200000 duty_cycle_pos=128 mode=0 cs_ena_posttrans=3 queue_size=3";
//		ResultType result=operateExecutor.exeOperate(spiStr, deviceModel);
//		return result;
//	}
	
	private ResultType tm7705SpiWriteReg(DeviceModel deviceModel,int reg,String value,OperateRequest request)//if not inited, init first
	{
		value=value.toLowerCase().trim();
		if(!value.startsWith("0x"))
			value="0x"+value;
		String spiStr="operate=3 spi_no=1 spi_address="+reg+" spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send="+value+" data_recv_len=0";
		request.setRawString(spiStr+" "+initStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		return result;
	}
	private ResultType tm7705SpiReadReg(DeviceModel deviceModel,int reg,int valueLen,OperateRequest request)//if not inited, init first
	{
		String spiStr="operate=3 spi_no=1 spi_address="+reg+" spi_address_valid=1 spi_command=0 spi_command_valid=0 data_send= data_recv_len="+valueLen;
		request.setRawString(spiStr+" "+initStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		return result;
	}
	private boolean tm7705TestPin(DeviceModel deviceModel,int pin,OperateRequest request)
	{
		if(tm7705SpiWriteReg(deviceModel,0x20+(pin-1),"0x04",request).getErrorType()<0) return false;
		ResultType result=tm7705SpiReadReg(deviceModel,0x28+(pin-1),1,request);
		if((result.getErrorType()<0)||(MiscUtil.strParseInteger("0x"+result.getHexContent())!=0x04))
		{
			logger.info("OperateExecutor tm7705TestPin device "+deviceModel.getDeviceNo()+" pin "+pin+" error "+result.getErrorType()+" error msg "+result.getErrorString()+" response "+result.getHexContent());
			return false;
		}
		logger.info("OperateExecutor tm7705TestPin device "+deviceModel.getDeviceNo()+" pin "+pin+" success");
		return true;
	}
	private boolean tm7705Busy(DeviceModel deviceModel,int pin,OperateRequest request)
	{
		ResultType result=tm7705SpiReadReg(deviceModel,0x08+(pin-1),1,request);
		if((result.getErrorType()<0)||(MiscUtil.strParseInteger(result.getHexContent())==null))
		{
			logger.info("OperateExecutor tm7705TestPin device "+deviceModel.getDeviceNo()+" pin "+pin+" error "+result.getErrorType()+" error msg "+result.getErrorString()+" response "+result.getHexContent());
			return false;
		}
		int regValue=MiscUtil.strParseInteger("0x"+result.getHexContent());
		if(((regValue >> 7) & 0x01)==0x01)
			return true;
		return false;
	}
	private int tm7705ReadPin(DeviceModel deviceModel,int pin,OperateRequest request)
	{
		double allv1=2.50000f;
		double allv2=65535.00000f;
		double gain=16.0000f;
		
		double ddata2=0.0000f;
		boolean isValid=false;
		for(int i=0;i<2;i++)
		{
			int m=0;
			while(tm7705Busy(deviceModel,pin,request))
			{
				m++;
				if(m>6)
					break;
			}
			if(m>6) {
				logger.info("OperateExecutor tm7705ReadPin device "+deviceModel.getDeviceNo()+" pin "+pin+" failed "+i);
				continue;
			}
			ResultType result=tm7705SpiReadReg(deviceModel,0x38+(pin-1),2,request);
			if(result.getErrorType()<0)
			{
				logger.info("OperateExecutor tm7705ReadPin device "+deviceModel.getDeviceNo()+" pin "+pin+" error "+result.getErrorType()+" error msg "+result.getErrorString()+" response "+result.getHexContent()+" "+i);
				continue;
			}
			int regValue=MiscUtil.strParseInteger("0x"+result.getHexContent());
			byte[] regValueBytes=MiscUtil.uint162BytesLittleEndian(regValue);
			int regValue2 = regValueBytes[0] * 256 + regValueBytes[1];
			if(regValue2==65535){
				logger.info("OperateExecutor tm7705ReadPin device "+deviceModel.getDeviceNo()+" pin "+pin+" error "+result.getErrorType()+" error msg "+result.getErrorString()+" response "+result.getHexContent()+" "+i+" value=65535");
				continue;
			}
			ddata2 = ((allv1* regValue2)/allv2)/gain;
			isValid=true;
			if(isValid)
				break;
		}
		if(isValid){
			return (int)(ddata2*10000000);
		}
		else
			return -100;//0
	}
	private ResultType tm7705ReadOnce(DeviceModel deviceModel,OperateRequest request)
	{
		int pin1Value=-100,pin2Value=-100;
		Integer tm7705Lock=deviceModel.getLockInfoModel().getTm7705Status();
		if((tm7705Lock==null)||(tm7705Lock==0))
		{
			setGpioExt(deviceModel,GpioSpec.CH423_OC_IO0,1,request);//pull up the resut pin 
			//if(tm7705SpiInit(deviceModel).getErrorType()<0) return new ResultType(-1,"init spi failed");//init spi
			if((tm7705TestPin(deviceModel,1,request)==false)||(tm7705TestPin(deviceModel,1,request)==false))//test pin1 pin2
				return new ResultType(-2,"test pin failed");
			logger.info("OperateExecutor tm7705ReadPin device "+deviceModel.getDeviceNo()+" both pin1 and pin2 ready");
			if(tm7705SpiWriteReg(deviceModel,0x10,"0x64",request).getErrorType()<0) return new ResultType(-3,"config pin1 failed");//config pin1
			if(tm7705SpiWriteReg(deviceModel,0x11,"0x64",request).getErrorType()<0) return new ResultType(-4,"config pin2 failed");//config pin2		
			deviceModel.getLockInfoModel().setTm7705Status(1);//inited success
			deviceModel.saveLockInfoModel();
		}
		pin1Value=tm7705ReadPin(deviceModel,1,request);
		logger.info("OperateExecutor tm7705ReadPin device "+deviceModel.getDeviceNo()+" pin1 "+pin1Value);
		pin2Value=tm7705ReadPin(deviceModel,1,request);
		logger.info("OperateExecutor tm7705ReadPin device "+deviceModel.getDeviceNo()+" pin2 "+pin2Value);
		if((pin1Value==-100)||(pin2Value==-100))
		{
			deviceModel.getLockInfoModel().setTm7705Status(0);
			deviceModel.saveLockInfoModel();
			return new ResultType(-6,"read pin failed");
		}
		return new ResultType(0,"exe ok","pin1="+pin1Value+",pin2="+pin2Value);
	}
	public ResultType read(DeviceModel deviceModel,OperateExecutor operateExecutor,OperateRequest request)
	{
		this.operateExecutor=operateExecutor;
		ResultType result=null;
		for(int i=0;i<1;i++)
		{
			result=tm7705ReadOnce(deviceModel,request);
			if(result.getErrorType()<0)
				setGpioExt(deviceModel,GpioSpec.CH423_OC_IO0,0,request);
		}
		return result;
	}
	private ResultType setGpioExt(DeviceModel deviceModel,int no,int status,OperateRequest request)
	{
		String spiStr="operate=2 gpio_ext_no="+no+" status="+status;
		request.setRawString(spiStr);
		ResultType result=operateExecutor.exeOperate(request, deviceModel);
		if(result.getErrorType()<0);
			logger.info("OperateExecutor gpioExt device "+deviceModel.getDeviceNo()+" gpio ext "+no+" to "+status+" "+result.getErrorString());
		return result;
	}
}
//operate=2 gpio_ext_no=8 status=1