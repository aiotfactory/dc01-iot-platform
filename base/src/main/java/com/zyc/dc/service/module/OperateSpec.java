package com.zyc.dc.service.module;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OperateSpec {
	//private static final Logger logger = LoggerFactory.getLogger(OperateSpec.class);
	private List<OperateSpecBatch> batchList=new ArrayList<>();
	private ModuleDefine.ModuleType moduleType;
    private Integer id;
    private String name;
	private Integer minBatchNum=0;
	private Map<String,OperateSpecValue> parameterOrder=new LinkedHashMap<>();
	private static Map<Integer,OperateSpec> specMap=new ConcurrentHashMap<>();
    public OperateSpec(Integer operate, String name,ModuleDefine.ModuleType moduleType,int minBatchNum) {
    	this.id=operate;
    	this.name=name;
        this.moduleType=moduleType;
        this.minBatchNum=minBatchNum;
        specMap.put(operate, this);
    }
    public static OperateSpec fromId(Integer operate)
    {
    	return specMap.get(operate);
    }
    public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	public List<OperateSpecBatch> getBatchList() {
		return batchList;
	}
    public boolean containBatch(OperateSpecBatch batch) {
		return batchList.contains(batch);
	}
	public Integer getMinBatchNum()
    {
    	return minBatchNum;
    }
    public Map<String,OperateSpecValue> getAllSpec()
    {
    	return parameterOrder;
    }
    public ModuleDefine.ModuleType getModuleType()
    {
    	return moduleType;
    }
    public OperateSpecBatch newBatch(boolean isOptional)
    {
    	OperateSpecBatch batch=new OperateSpecBatch(this,isOptional);
    	batchList.add(batch);
    	return batch;
    }
    public static class OperateSpecBatch { 
    	private boolean isOptional;
    	private OperateSpec spec;
    	private Map<String,OperateSpecValue> parameterOrder=new LinkedHashMap<>();
		public OperateSpecBatch(OperateSpec spec,boolean isOptional) {
			this.isOptional = isOptional;
			this.spec=spec;
		}

		public boolean isOptional() {
			return isOptional;
		}
	    public OperateSpecBatch addParameter(String paramName, int size) {
	    	OperateSpecValue specValue=new OperateSpecValue(size);
	    	specValue.setBatch(this);
	        parameterOrder.put(paramName, specValue);
	        spec.getAllSpec().put(paramName, specValue);
	        return this;
	    }
	    public OperateSpecBatch addParameter(String paramName, int size,boolean isOptional) {
	    	OperateSpecValue specValue=new OperateSpecValue(size,isOptional);
	    	specValue.setBatch(this);
	        parameterOrder.put(paramName,specValue);
	        spec.getAllSpec().put(paramName, specValue);
	        return this;
	    }

		public Map<String, OperateSpecValue> getParameter() {
			return parameterOrder;
		}
    }
    public static class OperateSpecValue { 
    	private int size;
    	private boolean isOptional;
    	private OperateSpecBatch batch;
		public OperateSpecValue(int size, boolean isOptional) {
			this.size = size;
			this.isOptional = isOptional;
		}
		public OperateSpecValue(int size) {
			this.size = size;
			this.isOptional = false;
		}
		public int getSize() {
			return size;
		}
		public boolean isOptional() {
			return isOptional;
		}
		public void setBatch(OperateSpecBatch batch) {
			this.batch = batch;
		}
		public OperateSpecBatch getBatch() {
			return batch;
		}
		
    }
}
