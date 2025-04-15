package com.zyc.dc.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.service.MiscUtil;
import com.zyc.dc.dao.DeviceConfigElementModel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;


public class DeviceConfigElement {
	public int idx;
	public int module;
	public int type;
	public String name;
	public byte[] value;
	public int valueLen;
	public int status;
	public long minValue,maxValue;
	private List<Integer> enumValue;
	
	private static final Logger logger = LoggerFactory.getLogger(DeviceConfigElement.class);
	
	public DeviceConfigElement(int idx,int module,int type,String name,int valueLen,byte[] value,long minValue,long maxValue,List<Integer> enumValue,int status)
	{
		this.idx=idx;
		this.module=module;
		this.type=type;
		this.name=name;
		this.valueLen=valueLen;
		this.value=value;
		this.minValue=minValue;
		this.maxValue=maxValue;
		this.enumValue=enumValue;
		this.status=status;
	}
	public String toString()
	{
		String tempstr="config "+idx+" type "+type+" name "+name;
		if(type==0)
			tempstr=tempstr+" value="+MiscUtil.bytesToUint32LittleEndianFlex(value);
		else
			tempstr=tempstr+" value="+new String(MiscUtil.bytesRightTrim(value,0));
		if(enumValue==null)
			tempstr=tempstr+" min="+minValue+" max="+maxValue;
		else {
			tempstr=tempstr+" options ";
			for(int i=0;i<enumValue.size();i++)
				tempstr=tempstr+enumValue.get(i)+" ";
		}
			//tempstr=tempstr+" value="+DatatypeConverter.printHexBinary(value).toLowerCase()+"|"+new String(value);
		return tempstr;
	}
	/*
	public static List<DeviceConfigElement> json2Element(String configstr)
	{
		if((configstr==null)||(configstr.length()==0))
			return null;
		JSONObject configjsonobject=JSONObject.fromObject(configstr);
		if((configjsonobject==null)||(configjsonobject.size()==0))
			return null;
		List<DeviceConfigElement> elementlist=new ArrayList<DeviceConfigElement>();
		for(Object key:configjsonobject.keySet())
		{
			JSONObject elementjson=configjsonobject.getJSONObject(key.toString());
			Integer index=MiscUtil.jsonGetInt(elementjson, "index", null);
			Integer type=MiscUtil.jsonGetInt(elementjson, "type", null);
			String name=MiscUtil.jsonGetString(elementjson, "name", null);
			Integer valuelen=MiscUtil.jsonGetInt(elementjson, "valuelen", null);
			String valueindevice=MiscUtil.jsonGetString(elementjson, "value_in_device", null);
			String valueindb=MiscUtil.jsonGetString(elementjson, "value_in_db", null);
			DeviceConfigElement element=new DeviceConfigElement();
		}
	}
	*/
	public static void printConfig(String prefix,DeviceModel deviceModel)
	{
		 logger.info(prefix);
		 Map<Integer, DeviceConfigElementModel> map=deviceModel.getDeviceConfig();
		 if(map!=null)
		 {
			 for(Integer index:map.keySet())
			 {
				 DeviceConfigElementModel elementModel=map.get(index);
				 logger.info(elementModel.getModule()+" "+elementModel.getIdex()+" "+elementModel.getName()+" "+elementModel.getValueInDevice()+" "+elementModel.getValueInDb());
			 }
		 }
	}
	public static Map<Integer,DeviceConfigElementModel> uploadConfig(String deviceId,List<DeviceConfigElement> elementList,Map<Integer,DeviceConfigElementModel> configDb)
	{
		Map<Integer,DeviceConfigElementModel> configMerged=new HashMap<Integer,DeviceConfigElementModel>();
		for(int i=0;i<elementList.size();i++)
		{
			DeviceConfigElement element=elementList.get(i);
			DeviceConfigElementModel newElement=null;
			if(configDb!=null)
				newElement=configDb.get(element.idx);
			if(newElement==null)
				newElement=new DeviceConfigElementModel();
			newElement.setIdex(element.idx);
			newElement.setModule(element.module);
			newElement.setName(element.name);
			newElement.setType(element.type);
			newElement.setValueLen(element.valueLen);
			newElement.setMinValue(element.minValue);
			newElement.setMaxValue(element.maxValue);
			newElement.setEnumValue(element.enumValue);
			newElement.setStatus(element.status);
			
			String valuestr="";
			if(element.type<=1)
				valuestr=MiscUtil.bytesToUint32LittleEndianFlex(element.value)+"";
			else
				valuestr=new String(MiscUtil.bytesRightTrim(element.value,0));
			newElement.setValueInDevice(valuestr);
			newElement.setDeviceId(deviceId);
			configMerged.put(newElement.getIdex(), newElement);
		}
		if(configMerged.size()>0)
		{
			return configMerged;
		}
		return null;
	}
	public static byte[] rspConfig(Map<Integer,DeviceConfigElementModel> configDb)
	{
		if((configDb==null)||(configDb.size()==0))
			return null;
		DeviceConfigElementModel elementVersion=configDb.get(0);
		//no rsp if version is not changed
		//if device hasn't uploaded, it will not rsp config
		if((elementVersion==null)||(elementVersion.getValueInDb()==null)||(elementVersion.getValueInDevice()==null)||(elementVersion.getValueInDevice()==elementVersion.getValueInDb()))
			return null;
		ByteBuf bufall = Unpooled.buffer(); 
        for (Integer key:configDb.keySet()) 
        {
        	DeviceConfigElementModel element=configDb.get(key);
            if((element.getIdex()!=null)&&(element.getValueInDb()!=null))
            {
            	if((element.getValueInDevice()!=null)&&(element.getValueInDevice().equals(element.getValueInDb())))
            		continue;
            	ByteBuf bufelement = Unpooled.buffer(); 
            	int index=element.getIdex();
            	bufelement.writeBytes(MiscUtil.uint322BytesLittleEndian(index));
            	String valuestr=element.getValueInDb();
            	int valuelen=valuestr.length();
            	bufelement.writeBytes(MiscUtil.uint322BytesLittleEndian(valuelen));
        		if(valuestr.length()>256)
        		{
        			logger.info("rspConfig config error index "+index+" too long "+valuestr.length());
        			continue;
        		}
        		bufelement.writeBytes(valuestr.getBytes());
            	byte[] elementbytes=new byte[bufelement.readableBytes()];
            	bufelement.readBytes(elementbytes);
            	bufall.writeBytes(elementbytes);
            }
        }
        byte[] byteret=new byte[bufall.readableBytes()];
        bufall.readBytes(byteret);
        if(byteret.length>0)
        	return byteret;
        return null;
	}
}
