package com.zyc.dc.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.zyc.dc.dao.ApiKeyModel;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.dao.PostLogModel;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.service.module.ModuleDefine;

import reactor.core.publisher.Mono;

@Service
public class SenderService {
	@Autowired
	private MongoDBService mongoDBService;
	@Autowired
	private ConfigProperties configProperties;
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private MessageSource messageSource;
	@Autowired
	private WebClient.Builder webClientBuilder;
    
	private static final Logger logger = LoggerFactory.getLogger(SenderService.class);
	
    private String utilLocal(String code,Locale local)
    {
    	 return messageSource.getMessage(code, null, local);
    }
    @Async
    public void msgForwardAsync(boolean noCheck,ForwardType type,DeviceModel deviceModel, Object data, ModuleDefine.ModuleType moduleType) 
    {
		if((data!=null)&&(deviceModel.getForwardStatus()!=null)&&(deviceModel.getForwardStatus()))//device allow 
		{
			ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(moduleType.getId()); 
		    if(noCheck || ((moduleInfoModel.getAllowForward()!=null)&&(moduleInfoModel.getAllowForward())))//module allow forward
		    {
				ApiKeyModel apiKeyModel=mongoDBService.findOneByField("userId", deviceModel.getUserId(), ApiKeyModel.class);//key created
				if(apiKeyModel!=null) 
				{
					if(!noCheck)
						moduleType.getHandler().postPreprocess((DataCommModel)data);
					logger.info(MiscUtil.jsonObject2String(data));
					if((apiKeyModel.getAllowForward()!=null)&&(apiKeyModel.getAllowForward()==true))//url forward true
					{
						httpPostMap(deviceModel.getId(), deviceModel.getDeviceNo(),apiKeyModel,moduleType, false, "type",type.name(),"content",data);
					}
				    if((apiKeyModel.getForwardEmailStatus()!=null)&&(apiKeyModel.getForwardEmailStatus()==2))//email forward true
				    {
				    	emailRuleTrigger(noCheck,deviceModel.getId(), deviceModel.getDeviceNo(),apiKeyModel,moduleType, "type",type.name(),"content",data);
				    }
				}
		    }
		}
	}
    public void msgForwardSync(boolean noCheck,ForwardType type,DeviceModel deviceModel, Object data, ModuleDefine.ModuleType moduleType) 
    {
    	logger.info("LLM will forward");
    	this.msgForwardAsync(noCheck,type, deviceModel, data, moduleType);
    }
    @Async
    public void emailSend(UserModel.UserSmtpModel smtp, String to, String subject, String text) {
        if (!MiscUtil.emailValid(to)) {
            logger.info("{} is not a valid email address, ignoring sending.", to);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        JavaMailSender sender = emailSender; 

        if (smtp != null) {
            JavaMailSenderImpl userMailSender = new JavaMailSenderImpl();
            userMailSender.setHost(smtp.getHost());
            userMailSender.setPort(smtp.getPort());
            userMailSender.setUsername(smtp.getUserName());
            userMailSender.setPassword(smtp.getPassword());

            Properties props = userMailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", smtp.getSmtpAuth() == null || smtp.getSmtpAuth() == 0 ? "false" : "true");
            props.put("mail.smtp.starttls.enable", smtp.getSmtpStartTlsEnable() == null || smtp.getSmtpStartTlsEnable() == 0 ? "false" : "true");
            props.put("mail.smtp.ssl.enable", smtp.getSmtpSslEnable() == null || smtp.getSmtpSslEnable() == 0 ? "false" : "true");

            message.setFrom(smtp.getUserName());
            sender = userMailSender;
        } else {
            message.setFrom(configProperties.SITE_EMAIL());
        }

        try {
            sender.send(message);
            logger.info("Email sent successfully to {} from {}", to, message.getFrom());
        } catch (Exception e) {
            logger.error("Failed to send email to {} from {}. Error: {}", to, message.getFrom(), e.getMessage(), e);
        }
    }
    public void emailPasswordReset(UserModel userModel,String password)
    {
    	if ((userModel.getEmail() == null) || (userModel.getEmail().length() == 0))
    	    return;
    	Locale local=Locale.forLanguageTag(userModel.getLocale()==null?"en-US":userModel.getLocale());
    	String subject = utilLocal("password_been_changed",local)+" " + utilLocal("iot_factory",local);
    	String messageText = utilLocal("dear",local)+" " + userModel.getLogin() + ",\n\n"
    	        + utilLocal("password_been_success_changed_to",local)+" "+password+"\n"
    	        + utilLocal("not_request_this_change",local)+".\n\n"
    	        + utilLocal("using_the_following_link",local)+"\n"
    	        + configProperties.SITE_URL() + "\n\n"
    	        + utilLocal("contains_confidential",local)+".\n\n"
    	        + utilLocal("best_regards",local)+"\n"
    	        + utilLocal("iot_factory",local);

    	emailSend(null,userModel.getLogin(), subject, messageText);

    }
    public void emailUserCreate(UserModel userModel,String password)
    {
    	if((userModel.getLogin()==null)||(userModel.getLogin().length()==0))
    		return;
    	Locale local=Locale.forLanguageTag(userModel.getLocale()==null?"en-US":userModel.getLocale());
    	String subject = utilLocal("your_account_credentials",local)+" - "+utilLocal("iot_factory",local);
        String messageText = utilLocal("dear",local)+" "+userModel.getLogin()+",\n\n"
                +  utilLocal("your_account_created_success",local)+"\n"
                +  utilLocal("username",local)+": " + userModel.getLogin() + "\n"
                +  utilLocal("password",local)+": " + password + "\n\n"
                +  utilLocal("log_in_use_follow_link",local)+":\n"
                +  configProperties.SITE_URL() + "\n\n"
                +  utilLocal("not_request_ignore_it",local)+"\n\n"
                +  utilLocal("contains_confidential",local)+"\n\n"
                +  utilLocal("best_regards",local)+"\n"
                +  utilLocal("iot_factory",local);
        emailSend(null,userModel.getLogin(), subject, messageText);
    }
    public void emailActivation(String to, String token,UserModel userModel,String password) {
    	Locale local=Locale.forLanguageTag(userModel.getLocale()==null?"en-US":userModel.getLocale());
        String subject = "Account Activation - "+utilLocal("iot_factory",local);
        String activationLink = configProperties.SITE_URL() + "/pub/useractivate?userId="+userModel.getId()+"&token=" + token; // Activation link
        String passwordStr="";
        if(password!=null)
        	passwordStr=utilLocal("password_set_is",local)+" "+password+"\n\n";
        String messageText = utilLocal("dear",local)+" "+to+",\n\n"
                + utilLocal("click_below_activate_account",local)+"\n"
                + activationLink + "\n\n"+passwordStr                
                + utilLocal("not_request_ignore_it",local)+"\n\n"
                + utilLocal("contains_confidential",local)+"\n\n"
                + utilLocal("best_regards",local)+"\n"
                + utilLocal("iot_factory",local);
        emailSend(null,to, subject, messageText);
    }
    public void emailVerificationCode(String to, UserModel userModel,String code) {
    	Locale local=Locale.forLanguageTag(userModel.getLocale()==null?"en-US":userModel.getLocale());
        String subject = utilLocal("your_verification_code",local)+" - "+utilLocal("iot_factory",local);
        String messageText = utilLocal("dear",local)+" "+to+",\n\n"
                + utilLocal("your_verification_code_is",local)+": " + code + "\n\n"
                + utilLocal("enter_code_complete_verification_process",local)+"\n\n"
                + utilLocal("not_request_ignore_it",local)+"\n\n"
                + utilLocal("contains_confidential",local)+"\n\n"
                + utilLocal("best_regards",local)+"\n"
                + utilLocal("iot_factory",local);
        emailSend(null,to, subject, messageText);
    }
 
    @SuppressWarnings("unchecked")
	public void emailRuleTrigger(boolean noCheck,String deviceId,String deviceNo,ApiKeyModel apiKeyModel,ModuleDefine.ModuleType moduleType,Object... params)
	{
		String key= MiscUtil.MD5(apiKeyModel.getKey()).toLowerCase();
		Map<Object,Object> body=new HashMap<>();
		Date now=new Date();
		body.put("key",key);
		if((params!=null)&&(params.length>0))
		{
			for(int i=0;i<params.length;)
			{
				Object param=params[i];
				if(param instanceof Map) {
					body.putAll((Map<Object,Object>)param);
					i++;
				}
				else
				{
					body.put(param, params[i+1]);
					i+=2;
				}
			}
		}
		String json=MiscUtil.jsonObject2String(body);
		String pack=MiscUtil.tokenCreate();

		if(noCheck||((apiKeyModel.getForwardEmailRule()!=null)&&(apiKeyModel.getForwardEmailRule().length()>0)))
		{
			boolean email=noCheck;
			
			if(!email)
				email=MiscUtil.patternMatch(json, apiKeyModel.getForwardEmailRule());
			
			if(email)
			{
				PostLogModel urlLog=new PostLogModel();
				urlLog.setModuleName(moduleType.getName());
				urlLog.setModuleTypeId(moduleType.getId());
				urlLog.setCreateTime(now);
				urlLog.setDeviceId(deviceId);
				urlLog.setDeviceNo(deviceNo);
				urlLog.setPack(pack);
				urlLog.setTx(json);
				urlLog.setType(PostLogModel.PostLogType.EMAIL);
				urlLog.setError(-1);
				urlLog.setUrl(apiKeyModel.getForwardEmail());
		    	if((apiKeyModel.getForwardEmailTime()==null)||((apiKeyModel.getForwardEmailTime().getTime()/1000/(3600*24))!=(now.getTime()/1000/(3600*24))))
		    		apiKeyModel.setForwardEmailTimesPerDay(0);
		    	if(apiKeyModel.getForwardEmailTimesPerDay()<apiKeyModel.getForwardEmailLimitPerDay())//limit check
		    	{			    	
		    		urlLog.setError(0);
		    		UserModel userModel=mongoDBService.findOneByField("id", apiKeyModel.getUserId(), UserModel.class);
		    		emailSend(userModel.getSmtp(),apiKeyModel.getForwardEmail(), "Email inform "+deviceNo+" "+moduleType.getName(), json);		 
		    		apiKeyModel.setLatestCallTime(now);
		    		apiKeyModel.setForwardEmailTime(now);
		    		apiKeyModel.setForwardEmailTimesPerDay(apiKeyModel.getForwardEmailTimesPerDay()+1);
		    		mongoDBService.save("httppost", apiKeyModel);
		    	}else {
		    		logger.info("exceed limit for "+apiKeyModel.getUserId()+" "+apiKeyModel.getForwardEmail());
		    	}
	    		mongoDBService.save("httppost", urlLog);
			}
		}
	}
	public PostLogModel httpPost(String url, String body, boolean block, PostLogModel log) {
	    WebClient webClient = webClientBuilder.build();
	
	    try {
	        Mono<String> responseMono = webClient.post()
	                .uri(url)
	                .header("Content-Type", "application/json")
	                .bodyValue(body)
	                .retrieve()
	                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
	                        clientResponse -> Mono.error(new RuntimeException("4xx or 5xx error")))
	                .bodyToMono(String.class)
	                .defaultIfEmpty(log.getPack()) // Handle empty responses
	                .doOnTerminate(() -> logger.info("forward send pack {} sent to {} with json len {}", log.getPack(), url, body.length()));
	        //.doOnTerminate(() -> logger.info("pack {} sent to {} with json {}", log.getPack(), url, body));
	
	        if (block) {
	            String response = responseMono.block();
	            httpHandleResponse(log, response);
	        } else {
	            // Asynchronous mode
	            responseMono.subscribe(
	                    response -> {
	                        httpHandleResponse(log, response);
	                    },
	                    error -> {
	                        logger.error("error occurred: {}", error.getMessage());
	                        log.setError(-2);
	                        httpHandleResponse(log, error.getMessage());
	                    }
	            );
	        }
	    } catch (RuntimeException e) {
	        logger.error("runtime exception: {}", e.getMessage(), e);
	        log.setError(-3);
	        httpHandleResponse(log, e.getMessage());
	    } catch (Exception e) {
	        logger.error("exception: {}", e.getMessage(), e);
	        log.setError(-4);
	        httpHandleResponse(log, e.getMessage());
	    }
	
	    return log;
	}
	private void httpHandleResponse(PostLogModel log,String response) {
		if((response!=null)&&(response.equals(log.getPack())))
		{
			log.setError(-5);
			response="no response";
		}
		logger.info("forward recv pack "+log.getPack()+" recv len "+response.length());
		if((response!=null)&&(response.length()>100))
			response=response.substring(0,100);
		log.setRx(MiscUtil.URLEncode(response));
		if(log.getError()<0)
			log.setType(PostLogModel.PostLogType.LOG);
		mongoDBService.save("httppost", log);
	}
	@SuppressWarnings("unchecked")
	public PostLogModel httpPostMap(String deviceId,String deviceNo,ApiKeyModel apiKeyModel,ModuleDefine.ModuleType moduleType,boolean block,Object... params)
	{
		String key= MiscUtil.MD5(apiKeyModel.getKey()).toLowerCase();
		Map<Object,Object> body=new HashMap<>();
		body.put("key",key);
		if((params!=null)&&(params.length>0))
		{
			for(int i=0;i<params.length;)
			{
				Object param=params[i];
				if(param instanceof Map) {
					body.putAll((Map<Object,Object>)param);
					i++;
				}
				else
				{
					body.put(param, params[i+1]);
					i+=2;
				}
			}
		}
		String json=MiscUtil.jsonObject2String(body);
		String pack=MiscUtil.tokenCreate();
	
		PostLogModel urlLog=new PostLogModel();
		urlLog.setModuleName(moduleType.getName());
		urlLog.setModuleTypeId(moduleType.getId());
		urlLog.setCreateTime(new Date());
		urlLog.setDeviceId(deviceId);
		urlLog.setDeviceNo(deviceNo);
		urlLog.setPack(pack);
		urlLog.setTx(json);
		if((apiKeyModel.getForwardUrl()==null)||(apiKeyModel.getForwardUrl().length()==0))//record in db only
		{
			urlLog.setType(PostLogModel.PostLogType.LOG);
			urlLog.setError(-1);
	    	mongoDBService.save("savelog", urlLog);
		}else if((apiKeyModel.getForwardUrl()!=null)&&(apiKeyModel.getForwardUrl().length()>0))//post to url
		{
			urlLog.setType(PostLogModel.PostLogType.URL);
			urlLog.setError(0);
			urlLog.setUrl(apiKeyModel.getForwardUrl());
			httpPost(apiKeyModel.getForwardUrl(),json,block,urlLog); 
		}
		return urlLog;
	}
	public enum ForwardType{
    	FORWARD,
    	LLM
    }
}
