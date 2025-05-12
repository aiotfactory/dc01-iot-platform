package com.zyc.dc.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import com.zyc.dc.dao.AiCallModel;
import com.zyc.dc.dao.AiRuleModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.Login;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.service.module.ModuleDefine;
import com.zyc.dc.util.CacheUtil;

@Service
public class AIService {
	@Autowired
	private MongoDBService mongoDBService;
	@Autowired
	private MiscUtil miscUtil;

	//private static final Logger logger = LoggerFactory.getLogger(AIService.class);
	public static final String SEPARATOR="[,\\s]+";
	public static final String TIME_FORMAT = "HH:mm:ss";

	
	public String webRuleLog(Map<String, Object> paramsReq,Model model)
	{
    	String ruleId = MiscUtil.webParamsGet(paramsReq, "ruleId", String.class, "");
    	if(ruleId.length()==0)
    		return "accessdeny";
    	AiRuleModel rule=mongoDBService.findOneByField("id", ruleId, AiRuleModel.class);
    	if(rule==null)
    		return "accessdeny";
    	Login login=CacheUtil.threadlocallogin.get();
    	UserModel userModel=login.getUserModel();
    	if(!rule.getUserId().equals(userModel.getId()))
    		return "accessdeny";
        List<AiCallModel> callList = mongoDBService.findByField(
                new Query().addCriteria(Criteria.where("ruleId").is(ruleId)),
                "createTime", null, false, 200, AiCallModel.class);        
    	if(callList!=null && callList.size()>0)
    	{
    		List<Map<String,Object>> callMapList=new ArrayList<>();
    		for(AiCallModel call:callList)
    		{
    			Map<String,Object> callMap=new HashMap<>();
    			callMap.put("id", call.getId());
    			callMap.put("templateSystem", call.getTemplateSystem());
    			callMap.put("templateUser", call.getTemplateUser());
    			callMap.put("actualSystem", call.getActualSystem());
    			callMap.put("actualUser", call.getActualUser());
    			callMap.put("actualOutput", call.getActualOutput());
    			String imagesStr="";
    			if(call.getActualImagesId()!=null && !call.getActualImagesId().isEmpty()) {
    				List<String> imagesList=call.getActualImagesId();
    				imagesStr=imagesList.stream().collect(Collectors.joining(","));
    			}
    			callMap.put("images", imagesStr);
    			if(call.getMoneyUsed()!=null)
    				callMap.put("moneyUsed",MiscUtil.long2Float(call.getMoneyUsed(), MiscUtil.MONEY_DIVIDER, "%.8f") );
    			callMap.put("createTime",  MiscUtil.dateFormat(call.getCreateTime(),"MM-dd HH:mm:ss"));
    			callMap.put("status", call.getStatus().name());
    			callMapList.add(callMap);
    		}
    		model.addAttribute("logs", callMapList);
    	}
        return "llmrulelog"; 
	}
	public Map<String,Object> webProcess(Map<String, Object> paramsBody)
	{
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	Login login=CacheUtil.threadlocallogin.get();
    	UserModel userModel=login.getUserModel();
    	
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
    	switch(action)
    	{
	    	case "ruleAdd":
	    	{
	    		if(!webRuleAdd(userModel,paramsBody, result))
	    			return result;
	    		break;	
	    	}
	    	case "ruleDetail":
	    	{
	    		Map<String, Object> ruleMap=webRuleDetail(userModel,paramsBody);
	    		result.put("ruleDetail", ruleMap);
	    		return result;
	    	}
	    	case "ruleDel":
	    	{
	    		webRuleDel(userModel,paramsBody);
	    		break;	
	    	}
    	}
    	List<Map<String,Object>> allModules=webGetModules();
    	List<Map<String,Object>> allRules=webRuleList(userModel.getId());
    	result.put("allModules", allModules);
    	result.put("allRules", allRules);
    	long balance=userModel.getAccountBalance()==null?0:userModel.getAccountBalance();
    	String balanceStr=MiscUtil.long2Float(balance, MiscUtil.MONEY_DIVIDER, "%.8f");
    	if(balance<=0)
    		result.put("balanceMsg", miscUtil.utilLocal("ai_web_balance_no")+" "+balanceStr);
    	else
    		result.put("balanceMsg", miscUtil.utilLocal("ai_web_balance_yes")+" "+balanceStr);
    	//test();
    	return result;
	}
	/*
	private byte[] utilImageToByte(String imagePath)
	{
	    File file = new File(imagePath);
	    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
	        BufferedImage bufferedImage = ImageIO.read(file);
	        if (bufferedImage == null) {
	            logger.info("can not read file " + imagePath);
	            return null;
	        }
	        ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream);
	        return byteArrayOutputStream.toByteArray();
	    } catch (IOException e) {
	    	logger.info("read error " + imagePath, e);
	        return null;
	    }
	}
	*/
	private List<Map<String,Object>> webRuleList(String userId) {
        List<AiRuleModel> ruleModelList = mongoDBService.findByField(
                new Query().addCriteria(Criteria.where("userId").is(userId)),
                "createTime", null, false, 200, AiRuleModel.class);
        List<Map<String,Object>> resultList = new ArrayList<>();
        for (AiRuleModel ruleModel : ruleModelList) 
        {
            String createTimeStr = MiscUtil.dateFormat(ruleModel.getCreateTime(), "MM-dd HH:mm");
            Map<String, Object> ruleMap = new HashMap<>();
            ruleMap.put("id", ruleModel.getId()); 
            ruleMap.put("name", ruleModel.getName()); 
            ruleMap.put("createTime", createTimeStr); 
            ruleMap.put("status", ruleModel.getStatus().toString()); 
            resultList.add(ruleMap);
        }
        
        return resultList;
	}
	private void webRuleDel(UserModel userModel,Map<String, Object> paramsBody) {
		String ruleId = MiscUtil.webParamsGet(paramsBody, "ruleId", String.class, "").trim();
		if(ruleId.length()>0)
		{
			AiRuleModel ruleModel = mongoDBService.findOneByField("id", ruleId, AiRuleModel.class);
			if(ruleModel!=null && ruleModel.getUserId().equals(userModel.getId()))
			{
				mongoDBService.delete("id", ruleId, AiRuleModel.class);
			}
		}
	}
	private Map<String, Object> webRuleDetail(UserModel userModel,Map<String, Object> paramsBody) {
		String ruleId = MiscUtil.webParamsGet(paramsBody, "ruleId", String.class, "").trim();
		if(ruleId.length()>0)
		{
			AiRuleModel ruleModel = mongoDBService.findOneByField("id", ruleId, AiRuleModel.class);
			if(ruleModel!=null && ruleModel.getUserId().equals(userModel.getId()))
			{
		        Map<String, Object> ruleMap = new HashMap<>();
		        ruleMap.put("id", ruleModel.getId());
		        if (ruleModel.getDeviceNos() != null && !ruleModel.getDeviceNos().isEmpty()) {
		            String deviceNosStr = String.join(",", ruleModel.getDeviceNos());
		            ruleMap.put("deviceNos", deviceNosStr);
		        } else {
		            ruleMap.put("deviceNos", "");
		        }
		        if (ruleModel.getExeTime() != null) {
		            List<String> timePeriods = new ArrayList<>();
		            for (Date[] timeArray : ruleModel.getExeTime()) {
		                if (timeArray.length == 2) { // 确保每个数组有两个元素：开始时间和结束时间
		                    Date startTime = timeArray[0];
		                    Date endTime = timeArray[1];
		                    String startStr =  MiscUtil.dateFormat(startTime, "HH:mm:ss");
		                    String endStr = MiscUtil.dateFormat(endTime, "HH:mm:ss");
		                    timePeriods.add(startStr + "-" + endStr);
		                }
		            }
		            ruleMap.put("exeTime", String.join(",", timePeriods));
		        } else {
		            ruleMap.put("exeTime", null);
		        }
		        ruleMap.put("name", ruleModel.getName());
		        ruleMap.put("triggerVariableCount", ruleModel.getTriggerVariableCount());
		        ruleMap.put("limitTimesPerDay", ruleModel.getLimitTimesPerDay());
		        ruleMap.put("userMsg", ruleModel.getUserMsg());
		        ruleMap.put("sysMsg", ruleModel.getSysMsg());
		        ruleMap.put("expectMsg", ruleModel.getExpectMsg());
		        ruleMap.put("expectStatus", ruleModel.getExpectStatus());
		        return ruleMap;
			}
		}
		return null;
	}
	
	private boolean webRuleAdd(UserModel userModel,Map<String, Object> paramsBody, Map<String, Object> result) {
		Date now=new Date();
		String ruleId = MiscUtil.webParamsGet(paramsBody, "ruleId", String.class, "").trim();
		String ruleName = MiscUtil.webParamsGet(paramsBody, "ruleName", String.class, "").trim();
		String sysMsg = MiscUtil.webParamsGet(paramsBody, "sysMsg", String.class, "").trim();
		String userMsg = MiscUtil.webParamsGet(paramsBody, "userMsg", String.class, "").trim();
		String deviceNos = MiscUtil.webParamsGet(paramsBody, "deviceNos", String.class, "").trim().toLowerCase();
		String exeTime = MiscUtil.webParamsGet(paramsBody, "exeTime", String.class, "").trim();
		Integer triggerVariableCount = MiscUtil.webParamsGet(paramsBody, "triggerVariableCount", Integer.class, null);
		Integer limitTimesPerDay = MiscUtil.webParamsGet(paramsBody, "limitTimesPerDay", Integer.class, null);
		String expectMsg = MiscUtil.webParamsGet(paramsBody, "expectMsg", String.class, "").trim();
		Boolean expectStatus = MiscUtil.webParamsGet(paramsBody, "expectStatus", Boolean.class, false);
		
		if(triggerVariableCount==null || triggerVariableCount<0)
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_mintrigger"));
			return false;
		}	
		if(limitTimesPerDay==null || limitTimesPerDay<0 )
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_limittimes"));
			return false;
		}	
		if(ruleName.length()==0||ruleName.length()>512) 
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_rulename"));
			return false;
		}
		if(expectMsg.length()>512) 
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_expectmsg"));
			return false;
		}
		if(sysMsg.length()>1024) 
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_sysmsg"));
			return false;
		}
		if(userMsg.length()==0||userMsg.length()>1024) 
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_usermsg"));
			return false;
		}
		List<Date[]> exeTimeList=webValidTime(exeTime);
		if(exeTimeList==null)
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_exetime"));
			return false;
		}
		List<DeviceModel> deviceModelList=webValidDeviceNo(userModel,deviceNos);
		if(deviceModelList==null) 
		{
			result.put("status", miscUtil.utilLocal("ai_web_error_device"));
			return false;
		}
		List<String> deviceNoList = deviceModelList.stream()
		        .map(DeviceModel::getDeviceNo) 
		        .collect(Collectors.toList()); 
		AiRuleModel aiRule=null;
		if(ruleId.length()>0)
			aiRule=mongoDBService.findOneByField("id", ruleId, AiRuleModel.class);
		if(aiRule==null) {
			aiRule=new AiRuleModel();
			aiRule.setCreateTime(now);
		}		
		aiRule.setDeviceNos(deviceNoList);
		aiRule.setExeTime(exeTimeList);
		aiRule.setLimitTimesPerDay(limitTimesPerDay);
		aiRule.setUserMsg(userMsg);
		aiRule.setSysMsg(sysMsg);
		aiRule.setTriggerVariableCount(triggerVariableCount);
		aiRule.setModifyTime(now);
		aiRule.setName(ruleName);
		aiRule.setStatus(AiRuleModel.AiRuleModelStatus.ENABLED);
		aiRule.setUserId(userModel.getId());
		aiRule.setExpectMsg(expectMsg);
		aiRule.setExpectStatus(expectStatus);
		mongoDBService.save("aiservice", aiRule);
		return true;
	}
	public List<Date[]> webValidTime(String input) {
		List<Date[]> ranges = new ArrayList<>();
		if(input.length()>0) 
		{
	        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
	        sdf.setLenient(false); 
	        String[] timeRanges = input.split(SEPARATOR);
	        
	        
	        for (String range : timeRanges) {
	            String[] times = range.split("-");
	            if (times.length != 2) {
	                return null;
	            } 
	            try {
	                Date startTime = webParseTime(sdf, times[0]);
	                Date endTime = webParseTime(sdf, times[1]);
	
	                if (startTime.after(endTime)) {
	                	 return null;
	                }
	                
	                for (Date[] existingRange : ranges) {
	                    if (startTime.before(existingRange[1]) && endTime.after(existingRange[0])) {
	                        return null;
	                    }
	                }
	                ranges.add(new Date[]{startTime, endTime});          
	            } catch (ParseException e) {
	                return null;
	            }
	        }
		}
        return ranges;
    }
	private Date webParseTime(SimpleDateFormat sdf, String time) throws ParseException {
        String[] parts = time.split(":");
        if (parts.length == 3) {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = Integer.parseInt(parts[2]);
            String normalizedTime = String.format("%02d:%02d:%02d", hour, minute, second);
            return sdf.parse(normalizedTime);
        } else {
            throw new ParseException("wrong time format " + time, 0);
        }
    }
	private List<DeviceModel> webValidDeviceNo(UserModel user,String deviceNos) {
		if(deviceNos.length()==0)
			return new ArrayList<>();
		List<String> deviceList = Arrays.stream(deviceNos.split(SEPARATOR))
		        .filter(s -> !s.trim().isEmpty()) 
		        .map(String::trim) 
		        .collect(Collectors.toList()); 
		if((deviceList!=null)&&(deviceList.size()>0))
		{
	        List<DeviceModel> deviceModelListTemp = mongoDBService.findByFieldIn("deviceNo", deviceList, DeviceModel.class);
	        deviceModelListTemp = deviceModelListTemp == null ? new ArrayList<>() : deviceModelListTemp;
	        List<DeviceModel> userDeviceModels = deviceModelListTemp.stream()
	                .filter(deviceModel -> deviceModel.getUserId().equals(user.getId()))
	                .collect(Collectors.toList());
	        if (userDeviceModels!=null && userDeviceModels.size()>0 && userDeviceModels.size() == deviceList.size()) {
	        	return userDeviceModels;
	        }
		}
		return null;
	}

    private List<Map<String, Object>> webGetModules() {
        List<Map<String, Object>> modules = new ArrayList<>();
        webAddModuleInfo(modules, ModuleDefine.MODULE_CAMERA.getId(), "ai_module_camera", "ai_module_camera_desc");
        webAddModuleInfo(modules, ModuleDefine.MODULE_SPL06.getId(), "ai_module_spl06", "ai_module_spl06_desc");
        webAddModuleInfo(modules, ModuleDefine.MODULE_AHT20.getId(), "ai_module_aht20", "ai_module_aht20_desc");
        webAddModuleInfo(modules, ModuleDefine.MODULE_PIR.getId(), "ai_module_pir", "ai_module_pir_desc");
        webAddModuleInfo(modules, ModuleDefine.MODULE_THERMAL.getId(), "ai_module_thermal", "ai_module_thermal_desc");
        webAddModuleInfo(modules, ModuleDefine.MODULE_ULTRASONIC.getId(), "ai_module_ultrasonic", "ai_module_ultrasonic_desc");
        return modules;
    }

    private void webAddModuleInfo(List<Map<String, Object>> modules, int moduleId, String nameKey, String descKey) {
        Map<String, Object> moduleMap = new HashMap<>();
        moduleMap.put("id", moduleId);
        moduleMap.put("name", miscUtil.utilLocal(nameKey));
        moduleMap.put("description", miscUtil.utilLocal(descKey));
        modules.add(moduleMap);
    }
}



















