package com.zyc.dc.service.module;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyc.dc.service.MiscUtil;


public class OperateContent {
	private static final Logger logger = LoggerFactory.getLogger(OperateContent.class);
	private Map<String, Object> operateContentMap;
	private String rawContent;
	private OperateSpec operateSpec;
	private int errorType;
	private String errorString;
    private OperateContent() {
    	operateContentMap = new LinkedHashMap<>();
    }
    
    public OperateSpec getOperateSpec() {
		return operateSpec;
	}

	public void setOperateSpec(OperateSpec operateSpec) {
		this.operateSpec = operateSpec;
	}

	public String getRawContent() {
		return rawContent;
	}

	public void setRawContent(String rawContent) {
		this.rawContent = rawContent;
	}

	private void addValue(String name,Object value)
    {
    	operateContentMap.put(name, value);
    }
    public int getErrorType()
    {
    	return errorType;
    }
    public String getErrorString()
    {
    	return errorString;
    }
    public byte[] bytesValue() {
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
        	OperateSpec spec=operateSpec;
	        baos.write(MiscUtil.uint322BytesLittleEndian(spec.getModuleType().getId()));//module id
	        baos.write(MiscUtil.uint322BytesLittleEndian(spec.getId()));//operate
	        for(String key:spec.getAllSpec().keySet())
	        {
	        	if(operateContentMap.containsKey(key))
	        	{
	        		int valueLen=spec.getAllSpec().get(key).getSize();
	        		Object valueObject= operateContentMap.get(key);
	        		if((valueLen==-1)||(valueLen==0))
	        		{
	        			baos.write(MiscUtil.uint322BytesLittleEndian(((byte[])valueObject).length));
	        			baos.write((byte[])valueObject);
	        		}else if(valueLen==1)
	        			baos.write((byte) (int) valueObject);
	        		else if(valueLen==2)
	        			baos.write(MiscUtil.uint162BytesLittleEndian((int) valueObject));
	        		else if(valueLen==4)
	        			baos.write(MiscUtil.uint322BytesLittleEndian((int) valueObject));
	        		else if(valueLen==8)
	        			baos.write(MiscUtil.uint642BytesLittleEndian((Long) valueObject));
	        	}
	        }
        }catch(Exception e)
        {
        	logger.error(e.getMessage(),e);
        }
        byte[] byteArray = baos.toByteArray();
        if(byteArray==null)
        	return new byte[0];
        return byteArray;
    }
    public static OperateContent parseOperateLine(String operateLine) {
    	logger.info("parseOperateLine "+operateLine);
    	OperateContent operateContent=new OperateContent();
        String[] parts = operateLine.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        //String[] parts = operateLine.split(" ");//check if there is space splited string
        if (parts.length < 1) {
        	operateContent.errorType=-1;
        	operateContent.errorString="parameters too few";
            return operateContent;
        }
        Map<String,String> rawMap=new HashMap<String,String>();//split the string and put it in key/value pair format
        for (int i = 0; i < parts.length; i++) {
            String[] paramParts = parts[i].trim().split("=",-1);
            if (paramParts.length != 2) {
            	operateContent.errorType=-2;
            	operateContent.errorString="wrong format "+parts[i];
                return operateContent;
            }
            String paramName = paramParts[0].trim();
            String paramValue = paramParts[1];
            paramValue=MiscUtil.stringTrim(paramValue, "\"", "\"");
            rawMap.put(paramName, paramValue);
        }
        Integer operate=MiscUtil.strParseInteger(rawMap.get("operate"));//check if the operate exists
        if(operate==null)
        {
        	operateContent.errorType=-4;
        	operateContent.errorString="no operate parameter";
            return operateContent;
        }
        OperateSpec operateSpecTemp= OperateSpec.fromId(operate);
        if(operateSpecTemp==null)
        {
        	operateContent.errorType=-5;
        	operateContent.errorString="no operate "+operate;
            return operateContent;
        }
        operateContent.setRawContent(operateLine);
        operateContent.operateSpec=operateSpecTemp;
        OperateSpec spec=operateSpecTemp;
        Map<OperateSpec.OperateSpecBatch,Integer> batchMap=new HashMap<>();
        for (String key:rawMap.keySet()) 
        {
        	if(key.equals("operate"))
        		continue;
        	OperateSpec.OperateSpecValue operateSpecValue=spec.getAllSpec().get(key);
        	if(operateSpecValue!=null)	
        	{
        		batchMap.put(operateSpecValue.getBatch(),1);
        		Integer valueLen=operateSpecValue.getSize();
        		String valueStr=rawMap.get(key);
        		Object valueObject=null;
        		if(valueLen>0)//number
        		{
	        		if(valueLen>4)
	        			valueObject=MiscUtil.strParseLong(valueStr);
	        		else
	        			valueObject=MiscUtil.strParseInteger(valueStr);
        		}else if(valueLen==0) {//hex
        			valueStr=valueStr.trim().toLowerCase();
        			if(valueStr.startsWith("0x"))
        				valueStr=valueStr.substring(2);
        			valueObject=MiscUtil.hexToBytes(valueStr);
        		}else if(valueLen==-1) {//string
        			valueObject=valueStr.getBytes();
        		}
        		if(valueObject==null)
        		{
                	operateContent.errorType=-3;
                	operateContent.errorString="wrong format "+key;
                    return operateContent;
        		}
        		operateContent.addValue(key, valueObject);
        	} else {
            	operateContent.errorType=-6;
            	operateContent.errorString="unknown parameter "+key;
                return operateContent;
            }
        }
        //required batch
        int batchNum=0;
        for(OperateSpec.OperateSpecBatch batch:spec.getBatchList())
        {
    		int batchValuesNum=0;
        	for(String key:batch.getParameter().keySet())
        	{
        		OperateSpec.OperateSpecValue operateSpecValue=batch.getParameter().get(key);
        		if(rawMap.containsKey(key))
        			batchValuesNum++;
        		else 
        		{
        			if((operateSpecValue.isOptional()==false)&&((batch.isOptional()==false)||(batchMap.containsKey(batch))))
        			{
                    	operateContent.errorType=-7;
                    	operateContent.errorString="short of parameters "+key;
                        return operateContent;
                	}
        		}
        	}
        	if((batch.isOptional()==false)&&(batchValuesNum==0))
        	{
            	operateContent.errorType=-8;
            	operateContent.errorString="short of batch";
                return operateContent;
        	}
        	if(batchValuesNum>0)
        		batchNum++;
        }
        
        if(spec.getMinBatchNum()>batchNum)
        {
        	operateContent.errorType=-9;
        	operateContent.errorString="parameters too few";
            return operateContent;
        }
        return operateContent;
    }
}
