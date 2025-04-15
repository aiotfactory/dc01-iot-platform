package com.zyc.dc.service.module;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zyc.dc.dao.ModuleRuleLogModel;
import com.zyc.dc.service.MongoDBService;
import com.zyc.dc.service.SpringUtil;

public class ModuleRule {
	private static final Logger logger = LoggerFactory.getLogger(ModuleRule.class);
	private int ruleId;
	private int ruleScope;
	private String ruleName;
	private String operator;
	private static MongoDBService mongoDBService=SpringUtil.getBean(MongoDBService.class);;
	
	public static ModuleRule AVG=new ModuleRuleOperator("moving_check",1,"avg");
	public static ModuleRule MAX=new ModuleRuleOperator("moving_check",2,"max");
	public static ModuleRule MIN=new ModuleRuleOperator("moving_check",3,"min");
	public static ModuleRule EVENT=new ModuleRuleOperator("event_check",4,"event");
	
	public ModuleRule(String ruleName,int ruleId,String operator)
	{
		this.ruleId=ruleId;
		this.operator=operator;
		this.ruleName=ruleName;
	}
	
	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public int getRuleId() {
		return ruleId;
	}

	public void setRuleId(int ruleId) {
		this.ruleId = ruleId;
	}

	public int getRuleScope() {
		return ruleScope;
	}

	public void setRuleScope(int ruleScope) {
		this.ruleScope = ruleScope;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public static Collection<ModuleRuleLogModel> saveModuleRuleResult(List<ModuleRuleResult> resultList)
	{
		Instant now=Instant.now();
		if((resultList!=null)&&(resultList.size()>0))
		{
			List<ModuleRuleLogModel> logList=new ArrayList<>();
			for(ModuleRuleResult tempResult:resultList)
			{
				if(tempResult.getResult())
				{
					ModuleRuleLogModel ruleLog=new ModuleRuleLogModel();
					ruleLog.setDeviceId(tempResult.getDeviceId());
					ruleLog.setModuleTypeId(tempResult.getModuleTypeId());
					ruleLog.setReason(tempResult.getReason());
					ruleLog.setReasonId(tempResult.getReasonId());
					ruleLog.setRuleId(tempResult.getModuleRule().getRuleId());
					ruleLog.setUploadTime(now);
					ruleLog.setStatus(ModuleRuleLogModel.ModuleRuleLogModelStatusEnum.INIT);
					ruleLog.setRuleName(tempResult.getModuleRule().getRuleName());
					ruleLog.setRuleOperator(tempResult.getModuleRule().getOperator());
					logList.add(ruleLog);
				}
			}
			return mongoDBService.insertAll(logList);
		}
		return null;
	}
	public ModuleRuleResult evaluate(Object... args)
	{
		return null;
	}
	public static class ModuleRuleResult {
		private Boolean result;
		private Integer reasonId;
		private String reason;
		private ModuleRule moduleRule;
		private String deviceId;
		private Integer moduleTypeId;

	
		public String getDeviceId() {
			return deviceId;
		}

		public void setDeviceId(String deviceId) {
			this.deviceId = deviceId;
		}
		public Integer getModuleTypeId() {
			return moduleTypeId;
		}

		public void setModuleTypeId(Integer moduleTypeId) {
			this.moduleTypeId = moduleTypeId;
		}

		public boolean getResult() {
			return result;
		}

		public String getReason() {
			return reason;
		}
		public ModuleRule getModuleRule() {
			return moduleRule;
		}
		public int getReasonId() {
			return reasonId;
		}

		public ModuleRuleResult(String deviceId,Integer moduleTypeId,ModuleRule rule,boolean result,int reasonId,String reason)
		{
			this.reason=reason;
			this.reasonId=reasonId;
			this.result=result;
			this.deviceId=deviceId;
			this.moduleTypeId=moduleTypeId;
			this.moduleRule=rule;
		}
		public ModuleRuleResult()
		{
			this.reason=null;
			this.reasonId=null;
			this.result=false;
			this.moduleRule=null;
		}
	}
	public static class ModuleRuleOperator extends ModuleRule {
		public ModuleRuleOperator(String ruleName,int ruleId,String operator)
		{
			super(ruleName,ruleId,operator);
		}
        @Override
        //List<data>, field,min percent,max percent,current,deviceId,moduleType
        public ModuleRuleResult evaluate(Object... args)
		{
        	String operator=getOperator();
        	@SuppressWarnings("unchecked")
			List<Object> listData=(List<Object>)args[0];
        	String methodName=(String)args[1];
        	Double minPercent=(Double)args[2];
        	Double maxPercent=(Double)args[3];
        	Integer current=(Integer)args[4];
        	String deviceId=(String)args[5];
        	ModuleDefine.ModuleType moduleType=(ModuleDefine.ModuleType)args[6];
        	String reason=(String)args[7];
        	
        	String tmpStr=operator+"(";
        	Double valueDouble=null;
        	String reasonPrefix=reason+" current value "+current+" %s recent "+listData.size()+" records "+operator+" %.2f*%.2f=%.2f";
            try {
	            for(int i=0;i<listData.size();i++)
	            {
	            	Object data=listData.get(i);
	            	Method method = data.getClass().getMethod(methodName);
	            	Integer x=(Integer) method.invoke(data);
	            	if(valueDouble==null)
	            		valueDouble=(double)x;
	            	else if(operator.equals("avg"))
	            		valueDouble+=x;
	            	else if(operator.equals("max")&&(x>valueDouble))
	            		valueDouble=(double)x;
	            	else if(operator.equals("min")&&(x<valueDouble))
	            		valueDouble=(double)x;
	            	tmpStr=tmpStr+x+",";
	            }
	            if(operator.equals("avg"))
	            	valueDouble/=listData.size();
	            tmpStr=tmpStr.substring(0, tmpStr.length()-1)+")="+valueDouble+"|"+(valueDouble*minPercent)+"<"+current+"<"+valueDouble*maxPercent;
	            //logger.info("raw "+tmpStr);
            	if(current<(valueDouble*minPercent))
            	{
            		//logger.info(tmpStr);
            		return new ModuleRuleResult(deviceId,moduleType.getId(),this,true,1,String.format(reasonPrefix, "<",valueDouble,minPercent,(valueDouble*minPercent)));
            	}else if(current>(valueDouble*maxPercent))
            	{
            		//logger.info(tmpStr);
            		return new ModuleRuleResult(deviceId,moduleType.getId(),this,true,2,String.format(reasonPrefix, ">",valueDouble,maxPercent,(valueDouble*maxPercent)));
            	}

            }catch(Exception e)
            {
            	logger.error(e.getMessage(),e);
            }
            String reasonStr=String.format(reasonPrefix, ">",valueDouble,minPercent,(valueDouble*minPercent))+" "+String.format(reasonPrefix, "<",valueDouble,maxPercent,(valueDouble*maxPercent));
            return new ModuleRuleResult(deviceId,moduleType.getId(),this,false,3,reasonStr);
		}
	}
}
