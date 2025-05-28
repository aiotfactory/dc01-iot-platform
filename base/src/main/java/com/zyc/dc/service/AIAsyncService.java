package com.zyc.dc.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationTokensDetails;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationUsage;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.zyc.dc.dao.AiCallModel;
import com.zyc.dc.dao.AiRuleModel;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.service.module.ModuleDefine;

@Service
public class AIAsyncService {
	@Autowired
	private SenderService senderService;
	@Autowired
	private MongoDBService mongoDBService;

	@Value("${llm.ali.api-key}")
    private String LLM_ALI_API_KEY;
	@Value("${llm.ali.temp-path}")
    private String LLM_ALI_TEMP_PATH;
	
	private static final Pattern PATTERN_VARIABLE1 = Pattern.compile("\\{(.*?)\\}");
	private static final Pattern PATTERN_VARIABLE2 = Pattern.compile("([\\w.]+)\\s*(?:\\((\\s*\\d+-?\\d+?\\s*)\\))?");
	
    private final Logger logger = LoggerFactory.getLogger(AIAsyncService.class);
    @Async
    public void ruleProcess(DeviceModel deviceModel,DataCommModel commModel)
    {
    	if(commModel==null)
    	{
    		logger.info("commmodel empty");
    		return;	
    	}
    	if(deviceModel==null)
    	{
    		logger.info("devicemodel empty");
    		return;	
    	}
    	UserModel userModel=mongoDBService.findOneByField("id", deviceModel.getUserId(), UserModel.class);
    	if(userModel==null) {
    		logger.info("usermodelempty");
    		return;	
    	}
    	if(userModel.getAccountBalance()==null || userModel.getAccountBalance()<=0)
    	{
    		logger.info("account balance not enough");
    		return;
    	}
    	List<AiRuleModel> ruleList=ruleFilter(deviceModel.getDeviceNo(), userModel.getId());
    	if(ruleList==null) {
    		//logger.info("no matched rules");
    		return;
    	}
    	for(AiRuleModel rule:ruleList)
    	{
	    	if(MiscUtil.isTimeNotCurrentDay(rule.getTriggerTime()))
	    		rule.setTriggerTimesPerDay(0);
	    	if(rule.getTriggerTimesPerDay()>=rule.getLimitTimesPerDay()) {
	    		//logger.info("exceed rule quotation");
	    		continue;
	    	}
    		AiCallModel callModel=new AiCallModel();
    		callModel.setCreateTime(new Date());
    		callModel.setRuleId(rule.getId());
    		callModel.setDeviceId(deviceModel.getId());
    		callModel.setUserId(userModel.getId());
    		callModel.setTemplateSystem(rule.getSysMsg());
    		callModel.setTemplateUser(rule.getUserMsg());
    		callModel.setActualSystem(rule.getSysMsg());
    		callModel.setMoneyUsed(0L);
    		if(ruleProcessTemplate(callModel, deviceModel.getId(), commModel)>=rule.getTriggerVariableCount())
    		{
        		rule.setTriggerTime(new Date());
        		rule.setTriggerTimesPerDay(rule.getTriggerTimesPerDay()+1);
        		mongoDBService.save("aiservice", rule);
    			llmCall(callModel);
    			userModel.setAccountBalance(userModel.getAccountBalance()-callModel.getMoneyUsed());
    			mongoDBService.increaseById(UserModel.class, userModel.getId(), "accountBalance", callModel.getMoneyUsed()*-1);
        		mongoDBService.save("aiservice", callModel);
        		ruleResultForward(userModel,deviceModel,rule,callModel,commModel.getModuleTypeId());
    		}else {
    			logger.info("valid variables not enough");
    		}
    		if(userModel.getAccountBalance()<=0) {
    			logger.info("break not enough account balance");
    			break;
    		}
    	}
    }
    private void ruleResultForward(UserModel userModel,DeviceModel deviceModel,AiRuleModel ruleModel,AiCallModel callModel,Integer moduleTypeId)
    {  	
    	if(ruleModel.getExpectStatus()==false && (!MiscUtil.patternMatch(callModel.getActualOutput(), ruleModel.getExpectMsg()))) {
    		logger.info("not meet forward condition");
    		return;
    	}
    	Map<String,Object> callMap=new HashMap<>();
    	callMap.put("id", callModel.getId());
    	callMap.put("deviceNo", deviceModel.getDeviceNo());
    	callMap.put("deviceId", deviceModel.getId());
    	callMap.put("ruleId", callModel.getRuleId());
    	callMap.put("ruleName", ruleModel.getName());
    	callMap.put("status", callModel.getStatus().name());
    	callMap.put("createTime",MiscUtil.dateFormat(callModel.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
    	callMap.put("templateSystem", callModel.getTemplateSystem());
    	callMap.put("templateUser", callModel.getTemplateUser());
    	callMap.put("actualSystem", callModel.getActualSystem());
    	callMap.put("actualUser", callModel.getActualUser());
    	callMap.put("actualOutput", callModel.getActualOutput());
    	callMap.put("moneyBalance",MiscUtil.long2Float(userModel.getAccountBalance(), MiscUtil.MONEY_DIVIDER, "%.8f") );
    	callMap.put("moneyUsed",MiscUtil.long2Float(callModel.getMoneyUsed(), MiscUtil.MONEY_DIVIDER, "%.8f") );
    	callMap.put("expectMsg", ruleModel.getExpectMsg());
    	callMap.put("expectStatus", ruleModel.getExpectStatus());
    	senderService.msgForwardSync(true, SenderService.ForwardType.LLM,deviceModel, callMap, ModuleDefine.ModuleType.fromId(moduleTypeId));
    }
    private int ruleProcessTemplate(AiCallModel callModel, String deviceId, DataCommModel commModel) {
        Matcher matcher = PATTERN_VARIABLE1.matcher(callModel.getTemplateUser());
        StringBuffer result = new StringBuffer();
        int validVariables=0;
        while (matcher.find()) 
        {
            String matchedVariableWithParams = matcher.group(1);
            String content = matchedVariableWithParams;
            Integer windowSecondsStart = 0,windowSecondsEnd=365*24*3600;

            // 检查是否有括号内的秒数
            Matcher secondsMatcher = PATTERN_VARIABLE2.matcher(matchedVariableWithParams);
            if (secondsMatcher.matches()) {
            	content = secondsMatcher.group(1); // 获取 module_name.content 部分
            	String part2=secondsMatcher.group(2);
            	if(part2!=null)
            	{
            		String[] part2Array=part2.split("-");
            		if(part2Array.length==2)
            		{
            			windowSecondsStart=MiscUtil.strParseInteger(part2Array[0]);
            			windowSecondsEnd=MiscUtil.strParseInteger(part2Array[1]);
            		}else
            			windowSecondsEnd=MiscUtil.strParseInteger(part2);
            	}
            }
            
            // 默认替换值
            String replacement=" ";// = miscUtil.utilLocal("ai_rule_constant_no_data");
            if (content != null && !content.isEmpty()) {
                // 提取模块名称并确定其类型
            	String[] contentArray=content.split("\\.");
            	if(contentArray.length==2) {
	                String moduleName = contentArray[0];
	                String moduleProperty = contentArray[1];
	                ModuleDefine.ModuleType moduleType = ModuleDefine.ModuleType.fromName(moduleName.toUpperCase());
	
	                // 根据模块类型、设备ID和窗口秒数获取实际数据
	                Object[] moduleResult = moduleType.getHandler().getLLMContent(deviceId, windowSecondsStart, windowSecondsEnd,moduleProperty,commModel);
	                if (moduleResult != null && "string".equals(moduleResult[0].toString())) {
	                    replacement = moduleResult[1].toString(); // 使用获取的数据作为替换值
	                    validVariables++;
	                }else if (moduleResult != null && "image".equals(moduleResult[0].toString())) {
	                	replacement="";
	                	callModel.getActualImages().add((byte[])moduleResult[1]);
	                	callModel.getActualImagesId().add((String)moduleResult[2]);
	                	validVariables++;
	                }
            	}
            }
            
            // 替换匹配到的部分为实际值或默认值
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result); // 追加输入字符串的剩余部分
        callModel.setActualUser(result.toString());
        logger.info(callModel.getActualUser());
        return validVariables;
    }
	private List<AiRuleModel> ruleFilter(String deviceNo, String userId) {
		Query query = new Query();
    	query.addCriteria(Criteria.where("userId").is(userId));
    	query.addCriteria(Criteria.where("status").is(AiRuleModel.AiRuleModelStatus.ENABLED));
    	Criteria deviceNosCriteria = new Criteria().orOperator(
    		    Criteria.where("deviceNos").size(0), 
    		    Criteria.where("deviceNos").in(List.of(deviceNo))
    		);
    	query.addCriteria(deviceNosCriteria);
    	
    	//rules match this device
    	List<AiRuleModel> ruleList=mongoDBService.findByField(query, "createTime", null,false, null,AiRuleModel.class);
    	if((ruleList==null)||(ruleList.size()==0))
    		return null;
    	//match time period
    	String currentTimeStr = MiscUtil.dateFormat(new Date(), AIService.TIME_FORMAT);
    	ruleList=ruleList.stream()
        .filter(rule -> {
            if (rule.getExeTime() == null || rule.getExeTime().isEmpty()) {
                return true;
            }
            for (Date[] exeTimeRange : rule.getExeTime()) {
                if (exeTimeRange.length == 2) {
                	String startTimeStr = MiscUtil.dateFormat(exeTimeRange[0], AIService.TIME_FORMAT);
                	String endTimeStr = MiscUtil.dateFormat(exeTimeRange[1], AIService.TIME_FORMAT);
                    if (currentTimeStr.compareTo(startTimeStr) >= 0 && currentTimeStr.compareTo(endTimeStr) <= 0) {
                        return true;
                    }
                }
            }
            return false;
        })
        .collect(Collectors.toList());
    	if((ruleList==null)||(ruleList.size()==0))
    		return null;
    	return ruleList;
	}

	private void llmCall(AiCallModel callModel)
	{
		if(callModel.getActualImages().size()==0) 
			llmAliWithText(callModel);
		else 
			llmAliWithImage(callModel);		
	}
    private GenerationResult llmAliWithText(AiCallModel callModel) {		
    	String userText=callModel.getActualUser();
    	String sysText=callModel.getActualSystem();
    	if (userText == null || userText.length() == 0) {
        	callModel.setStatus(AiCallModel.AiCallStatus.WRONG_INPUT);
        	return null;
        }
        try {
	        Generation gen = new Generation();
	        List<Message> msgList = new ArrayList<>();
	        if(sysText != null && sysText.length() > 0) {
		        Message systemMsg = Message.builder()
		                .role(Role.SYSTEM.getValue())
		                .content(sysText)
		                .build();
		        msgList.add(systemMsg);
	        }
	        Message userMsg = Message.builder()
	                .role(Role.USER.getValue())
	                .content(userText)
	                .build();
	        msgList.add(userMsg);
	        GenerationParam param = GenerationParam.builder()
	                .apiKey(LLM_ALI_API_KEY)
	                 //模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
	                .model("qwen-plus")
	                .messages(msgList)
	                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
	                .build();
	        GenerationResult result= gen.call(param);
			callModel.setTokensInput(result.getUsage().getInputTokens());
			callModel.setTokensInputText(result.getUsage().getInputTokens());
			callModel.setTokensOutput(result.getUsage().getOutputTokens());
			callModel.setTokensOutputText(result.getUsage().getOutputTokens());
			String content=result.getOutput().getChoices().get(0).getMessage().getContent();
			callModel.setActualOutput(content);
			callModel.setStatus(AiCallModel.AiCallStatus.SUCCESS);
			utilFee(callModel,0.0008f,0.002f);
            logger.info(MiscUtil.jsonObject2String(result));     
            return result;
        } catch (Exception e) {
            logger.error("Error occurred during API call: " + e.getMessage(), e);
            callModel.setStatus(AiCallModel.AiCallStatus.EXCEPTION);
            callModel.setErrorMsg(e.getMessage());
            return null; 
        }
    }
    private Long utilFee(AiCallModel callModel,float inRate,float outRate)
    {
    	Double value=Double.valueOf(((inRate*MiscUtil.MONEY_DIVIDER)/1000)*callModel.getTokensInput()+((outRate*MiscUtil.MONEY_DIVIDER)/1000)*callModel.getTokensOutput());
    	value=value*1.5;
    	callModel.setMoneyUsed(value.longValue());
    	return callModel.getMoneyUsed();
    }
    private MultiModalConversationResult llmAliWithImage(AiCallModel callModel) {
    	
    	String userText=callModel.getActualUser();
    	String sysText=callModel.getActualSystem();
    	List<byte[]> images=callModel.getActualImages();
    	//validation
        if (userText == null || userText.length() == 0 || images == null || images.size() == 0) {
            callModel.setStatus(AiCallModel.AiCallStatus.WRONG_INPUT);
        	return null;
        }        
        //store temp files
        List<File> tempFileList = new ArrayList<>();
        try {

            MultiModalConversation conv = new MultiModalConversation();
            List<MultiModalMessage> msgList = new ArrayList<>();
            //add systext
            if(sysText != null && sysText.length() > 0) {
                MultiModalMessage systemMessage = MultiModalMessage.builder()
                        .role(Role.SYSTEM.getValue())
                        .content(Collections.singletonList(Collections.singletonMap("text", sysText)))
                        .build();
                msgList.add(systemMessage);
            }
            //add usertext and images
            List<Map<String,Object>> userMsgList=new ArrayList<>();
            userMsgList.add(Collections.singletonMap("text", userText));
            for(byte[] image:images)
            {
            	File tempFile = File.createTempFile("ai_temp_img_", ".jpg", new File(LLM_ALI_TEMP_PATH));
            	tempFileList.add(tempFile);
                String filePath = tempFile.getAbsolutePath();
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(image);
                }
                userMsgList.add(Collections.singletonMap("image", filePath));
            }
            
            
            MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
                    .content(userMsgList).build();
            msgList.add(userMessage);

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(LLM_ALI_API_KEY)
                     //模型列表：https://help.aliyun.com/zh/model-studio/getting-started/models
                    .model("qwen-vl-plus")
                    //.model("qwen-turbo")
                    .messages(msgList)
                    .build();

            MultiModalConversationResult result = conv.call(param); 

            if(result.getUsage()!=null && result.getUsage()!=null) 
    		{
            	MultiModalConversationUsage usage=result.getUsage();
				callModel.setTokensInput(usage.getInputTokens());
				MultiModalConversationTokensDetails inputDetails=usage.getInputTokensDetails();
				if(inputDetails!=null) {
					callModel.setTokensInputText(inputDetails.getTextTokens());
					callModel.setTokensInputImage(inputDetails.getImageTokens());
					callModel.setTokensInputAudio(inputDetails.getAudioTokens());
					callModel.setTokensInputVideo(inputDetails.getVideoTokens());		
				}
				callModel.setTokensOutput(usage.getOutputTokens());
				MultiModalConversationTokensDetails outputDetails=usage.getOutputTokensDetails();
				if(outputDetails!=null) {
					callModel.setTokensOutputText(outputDetails.getTextTokens());
					callModel.setTokensOutputAudio(outputDetails.getAudioTokens());
					callModel.setTokensOutputImage(outputDetails.getImageTokens());
					callModel.setTokensOutputVideo(outputDetails.getVideoTokens());		
				}
				Object content=result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
				if(content!=null)
					callModel.setActualOutput(content.toString());
    		}
			callModel.setStatus(AiCallModel.AiCallStatus.SUCCESS);
			//模型调用-输入：¥0.0015/千Token
			//模型调用-输出：¥0.0045/千Token
			utilFee(callModel,0.0015f,0.0045f);
            logger.info(MiscUtil.jsonObject2String(result));            
            return result;
        } catch (Exception e) {
            logger.error("Error occurred during file operations or API call: " + e.getMessage(), e);
            callModel.setStatus(AiCallModel.AiCallStatus.EXCEPTION);
            callModel.setErrorMsg(e.getMessage());
            return null;
        } finally {
        	for(File file:tempFileList){
                try {
                    Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
                } catch (IOException e) {
                    logger.error("Failed to delete the temporary file: " + e.getMessage(), e);
                }
        	}
        }
    }
}


























