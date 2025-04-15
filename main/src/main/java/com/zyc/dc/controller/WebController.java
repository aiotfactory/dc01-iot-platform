package com.zyc.dc.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartFile;
import com.google.code.kaptcha.Producer;
import com.zyc.dc.constant.ResultType;
import com.zyc.dc.dao.ApiKeyModel;
import com.zyc.dc.dao.ApiKeyModel.CallModel;
import com.zyc.dc.dao.ApiKeyModel.LimitModel;
import com.zyc.dc.dao.CaptchaModel;
import com.zyc.dc.dao.DataCacheModel;
import com.zyc.dc.dao.DataCloudLog;
import com.zyc.dc.dao.DeviceConfigElementModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.Login;
import com.zyc.dc.dao.ModuleInfoModel;
import com.zyc.dc.dao.ModuleRuleLogModel;
import com.zyc.dc.dao.OTADownloadModel;
import com.zyc.dc.dao.OTARecordModel;
import com.zyc.dc.dao.PostLogModel;
import com.zyc.dc.dao.ProjectAccessModel;
import com.zyc.dc.dao.ProjectAccessModel.ProjectAccessType;
import com.zyc.dc.dao.ProjectBuildModel;
import com.zyc.dc.dao.ProjectFileModel;
import com.zyc.dc.dao.ProjectInfoModel;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.dao.DataCommModel;
import com.zyc.dc.pojo.MessageReq;
import com.zyc.dc.service.CommService;
import com.zyc.dc.service.SimDWService;
import com.zyc.dc.service.ConfigProperties;
import com.zyc.dc.service.EmailService;
import com.zyc.dc.service.MiscUtil;
import com.zyc.dc.service.MongoDBService;
import com.zyc.dc.service.OperateExecutor;
import com.zyc.dc.service.ProjectBuildService;
import com.zyc.dc.service.WebClientService;
import com.zyc.dc.service.module.ModuleDefine;
import com.zyc.dc.service.module.ModuleDefine.ModuleType;
import com.zyc.dc.service.module.ModuleHandler;
import com.zyc.dc.service.module.OperateRequest;
import com.zyc.dc.util.AnsiToHtmlConverter;
import com.zyc.dc.util.CacheUtil;
import com.zyc.dc.util.CodeEditorUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


@Controller
@RequestMapping("/")
public class WebController {
	private static final Logger logger = LoggerFactory.getLogger(WebController.class);
	@Autowired
	private MongoDBService mongoDBService;
	@Autowired
	private MiscUtil miscUtil;
	@Autowired
	private OperateExecutor operateExecutor;
	@Autowired
	private ConfigProperties configProperties;
	@Autowired
	private EmailService emailService;
    @Autowired
    private Producer kaptchaProducer;
    @Autowired
    private WebClientService webClientService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private ProjectBuildService projectBuildService;
	@Autowired
	private CommService commService;
	@Autowired
	SimDWService simDWService;
	//@Autowired
	//private AIService aiService;
	
    private final ErrorAttributes errorAttributes;
    public WebController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }
    private String utilLocal(String code)
    {
    	 return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
    //boolean,view/write,device
	private Object[] utilHasPriv(String deviceId)
	{
		Object[] ret=new Object[3];
		ret[0]=false;
		if(deviceId.length()==0)
			return ret;
		Login login=CacheUtil.threadlocallogin.get();
		DeviceModel device=mongoDBService.findOneByField("id", deviceId, DeviceModel.class);
		if(device==null)
			return ret;
		ret[2]=device;
		ret[0]=true;
		if(login.getUserId().equals(device.getUserId()))//belong to current user
		{
			ret[1]="write";
			return ret;
		}
		//clone
		if((login.getUserModel().getClonedUser()!=null)&&(login.getUserModel().getClonedUser().getUserIdList().contains(device.getUserId())))
		{
			ret[1]="read";
			return ret;
		}
		//child
		UserModel deviceUserModel=mongoDBService.findOneByField("id", device.getUserId(), UserModel.class);
		if((deviceUserModel!=null)&&(deviceUserModel.getParentId().equals(login.getUserId())))
		{
			ret[1]="write";
			return ret;
		}
		ret[0]=false;
		return ret;
	}
    private void utilBreadcrumb(Model model,String[][] links)
    {
        String[][] breadcrumbs = {
                {utilLocal("breadcrumb_home"), "dashboard"}
            };
        String[][] combined = new String[breadcrumbs.length + links.length][2];
        System.arraycopy(breadcrumbs, 0, combined, 0, breadcrumbs.length);
        System.arraycopy(links, 0, combined, breadcrumbs.length, links.length);
        model.addAttribute("breadcrumbs", combined);
        Login login=CacheUtil.threadlocallogin.get();
        if(login!=null) {
        	model.addAttribute("login", login.getUserlogin());
        	model.addAttribute("userId", login.getUserId());
        	if(login.getUserModel().getParentId()!=null)
            	model.addAttribute("parentUserId",login.getUserModel().getParentId());
        }
        model.addAttribute("siteCompany",utilLocal("iot_factory"));
        model.addAttribute("siteUrl",configProperties.SITE_URL());
        
    }
    //0:ok, -1:not match, -2:not input
    private int pubCaptchaCheck(HttpServletRequest request,String content,Map<String,Object> result)
    {
    	String id=MiscUtil.cookieGet(request.getCookies(), "captcha");
    	if((id!=null)&&(content!=null)&&(content.length()>0))
    	{
    		 CaptchaModel captchaModel=mongoDBService.findOneByField("id", id, CaptchaModel.class);
    		 if(captchaModel!=null) {
    			 if(captchaModel.getContent().equalsIgnoreCase(content))
    				 return 0;
    			 else {
 					result.put("status", utilLocal("invalid_captcha") );
 					result.put("captchaShow", 1);
    				return -1;
    			 }
    		 }
    	}
		result.put("status",  utilLocal("input_captcha_confirm") );
		result.put("captchaShow", 1);
    	return -2;
    }
    @GetMapping("/pub/error")
    public String pubError(WebRequest webRequest, Model model) {
        var errorDetails = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE));
        model.addAllAttributes(errorDetails);
        return "error";
    }
    

    @RequestMapping(value = "/pub/safeserver", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    //safe_server_start|01|config_version|server_host|server_port|add|safe_server_end|kkxxx
    //safe_server_start|02|ota_server|ota_port|uri|ota_size|ota_add|add|safe_server_end|kkxxx
    public String pubSafeServer(@RequestParam Map<String, Object> paramsReq,@RequestBody byte[] configData)
    {
    	//deviceType=%lu&firmwareVersion=%lu&deviceNo
    	Instant now=Instant.now();
    	String deviceNo = MiscUtil.webParamsGet(paramsReq, "deviceNo", String.class, "");
    	Long deviceType = MiscUtil.webParamsGet(paramsReq, "deviceType", Long.class, null);
    	Long firmwareVersion = MiscUtil.webParamsGet(paramsReq, "firmwareVersion", Long.class, null);
    	Long server = MiscUtil.webParamsGet(paramsReq, "server", Long.class, 0L);
    	String add = MiscUtil.webParamsGet(paramsReq, "add", String.class, "");
    	String ret="kkxxx";
    	if(deviceNo.length()==0) 
    	{
    		logger.info("pubSafeServer wrong1 "+deviceNo);
    		return "no device-"+ret;
    	}
    	if((deviceType==null)||(firmwareVersion==null))
    	{
    		logger.info("pubSafeServer wrong1 deviceType "+deviceType+" firmwareVersion "+firmwareVersion);
    		return "no type of version-"+ret;
    	}
    	if((configData==null)||(configData.length==0)) 
    	{
    		logger.info("pubSafeServer wrong config data ");
    		return "no config-"+ret;
    	}
    	if((add.length()!=32)||(!add.equals(commService.getStr(configData))))
    	{
    		logger.info("pubSafeServer wrong add "+add);
    		return "wrong add-"+ret;
    		
    	}
    	DeviceModel deviceModel=mongoDBService.findOneByField("deviceNo", deviceNo, DeviceModel.class);
    	if(deviceModel==null) 
    	{
    		logger.info("pubSafeServer not exist "+deviceNo);
    		return "not exist device-"+ret;
    	}
		deviceModel.setDeviceType(DeviceModel.DeviceType.getById(Long.valueOf(deviceType).intValue()));
		deviceModel.setDeviceFirmWareVersion(firmwareVersion);
		deviceModel.setUploadTime(now);
		if(server>0)
			deviceModel.setDeviceServerIp(MiscUtil.ipNumber2String(server));
    	logger.info("pubSafeServer recv "+deviceNo+" deviceType "+deviceType+" firmwareVersion "+firmwareVersion+" add "+add+" config len "+configData.length);
    	mongoDBService.save("pubSafeServer", deviceModel);
    	//check issue
    	int issueId=0;
    	if(deviceModel.getDeviceStatus()==DeviceModel.DeviceStatus.NOLICENSE)
    		issueId=4;
    	else if(deviceModel.getDeviceStatus()==DeviceModel.DeviceStatus.FREEZED)
        	issueId=5;
    	if(issueId>0)
    	{
			String temp="safe_server_start|03|"+issueId+"|";
			String temp2=commService.getStr(temp);
			temp=temp+temp2+"|safe_server_end";
			ret=temp+"|"+ret;
	    	logger.info("pubSafeServer rsp "+ret);
	    	return ret;
    	}
    	//check ota
    	String otaRsp=webOTARsp(deviceModel,ret);
    	if(otaRsp!=null)
    	{
    		logger.info("pubSafeServer rsp "+otaRsp);
	    	return otaRsp;
    	}
    	
    	MessageReq msgReq=new MessageReq(configData);
    	ModuleDefine.MODULE_CONFIG.getHandler().recvUpload(deviceModel, msgReq,null);
    	Map<Integer, DeviceConfigElementModel> configMap=deviceModel.getDeviceConfig();

    	//check config
    	if(configMap!=null)
    	{
    		//logger.info("pubSafeServer json config "+MiscUtil.json2String(configMap));
    		DeviceConfigElementModel elementVersion=configMap.get(0);
    		if((elementVersion!=null)&&(((elementVersion.getValueInDb()!=null)&&(elementVersion.getValueInDevice()!=null)&&(!elementVersion.getValueInDevice().equals(elementVersion.getValueInDb())))))
    		{
	    		DeviceConfigElementModel elementHost=configMap.get(1);
	    		DeviceConfigElementModel elementPort=configMap.get(2);
	    		if((elementHost!=null)&&(elementPort!=null))
	    		{
	    			if(((elementPort.getValueInDb()!=null)&&(elementPort.getValueInDevice()!=null)&&(!elementPort.getValueInDevice().equals(elementPort.getValueInDb())))||((elementHost.getValueInDb()!=null)&&(elementHost.getValueInDevice()!=null)&&(!elementHost.getValueInDevice().equals(elementHost.getValueInDb()))))
	    			{
	    				//safe_server_start|config_version|server_host|server|add|safe_server_end
	    				String configVersion=elementVersion.getValueInDevice();
	    				if((elementVersion.getValueInDb()!=null)&&(elementVersion.getValueInDb().length()>0))
	    					configVersion=elementVersion.getValueInDb();
	    				String host=elementHost.getValueInDevice();
	    				if((elementHost.getValueInDb()!=null)&&(elementHost.getValueInDb().length()>0))
	    					host=elementHost.getValueInDb();
	    				String port=elementPort.getValueInDevice();
	    				if((elementPort.getValueInDb()!=null)&&(elementPort.getValueInDb().length()>0))
	    					port=elementPort.getValueInDb();
	    				String temp="safe_server_start|01|"+configVersion+"|"+host+"|"+port+"|";
	    				String temp2=commService.getStr(temp);
	    				temp=temp+temp2+"|safe_server_end";
	    				ret=temp+"|"+ret;
	    		    	logger.info("pubSafeServer rsp "+ret);
	    		    	return ret;
	    			}
	    		}
    		}
    	}

    	logger.info("pubSafeServer rsp "+ret);
    	return ret;
    }
    @GetMapping("/pub/captcha")
    public void pubCaptcha(HttpServletRequest request, HttpServletResponse response)
    {
    	Instant now=Instant.now();
    	String id=MiscUtil.cookieGet(request.getCookies(), "captcha");
    	if(id==null)
    		id=UUID.randomUUID().toString().toLowerCase();
        String captchaText = kaptchaProducer.createText(); // 创建验证码文本
        CaptchaModel captchaModel=new CaptchaModel();
        captchaModel.setContent(captchaText);
        captchaModel.setId(id);
        captchaModel.setCreateTime(now);
        mongoDBService.save("captcah",captchaModel);
        BufferedImage captchaImage = kaptchaProducer.createImage(captchaText); // 创建验证码图像
        response.setHeader("Cache-Control", "no-store, no-cache");
        response.setContentType("image/jpeg");
       
		Cookie cookie1 = new Cookie("captcha", id);
		cookie1.setPath("/");				
		cookie1.setMaxAge(-1);
		cookie1.setHttpOnly(true);
		response.addCookie(cookie1);

        try {
			ImageIO.write(captchaImage, "jpg", response.getOutputStream());
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
    }
    @PostMapping("/pub/recvforward")
    public ResponseEntity<Map<String,Object>> pubRecvForward(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	result.put("info", "received");
    	result.put("time", Instant.now());
		return ResponseEntity.ok(result);
    }
    @GetMapping("/pub/userforget")
    public String pubUserForget(@RequestParam Map<String, Object> paramsReq,HttpServletRequest request,Model model) {
    	
    	model.addAttribute("siteCompany",utilLocal("iot_factory"));
    	return "userforget"; 
    }
    @PostMapping("/pub/userforgetrefresh")
    public ResponseEntity<Map<String,Object>> pubUserForgetRefresh(@RequestBody Map<String, Object> params,HttpServletRequest request, HttpServletResponse response) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	Instant cur=Instant.now();
    	String email = MiscUtil.webParamsGet(params, "email", String.class, "");
    	String code = MiscUtil.webParamsGet(params, "code", String.class, "");
    	String password1 = MiscUtil.webParamsGet(params, "password1", String.class, "");
    	String password2 = MiscUtil.webParamsGet(params, "password2", String.class, "");
    	Integer step = MiscUtil.webParamsGet(params, "step", Integer.class, 1);
    	miscUtil.logout(response);
    	if(step==null)
    		return null;
		if(!MiscUtil.emailValid(email))
		{
    		result.put("status", utilLocal("wrong_email"));
            return ResponseEntity.ok(result);
		}
		if(pubCaptchaCheck(request, MiscUtil.webParamsGet(params, "captcha", String.class, ""),result)!=0)
			return ResponseEntity.ok(result);
		UserModel userModel=mongoDBService.findOneByField("login", email, UserModel.class);
		if(userModel==null)
		{
    		result.put("status",  utilLocal("user_not_exist"));
            return ResponseEntity.ok(result);
		}else if(userModel.getUserStatus()!=UserModel.UserStatus.ENABLED)//can not get back password if the email is not verified yet
		{
			
    		result.put("status", utilLocal("user")+" "+userModel.getUserStatus());
            return ResponseEntity.ok(result);
		}
		mongoDBService.log(null, null, request.getRequestURI(), params, "pubUserForgetRefresh", null,null);
    	switch(step)
    	{
    		case 1://input email
    			result.put("step", 2);
    			if((userModel.getVerificationCodeReqTime()==null)||(MiscUtil.dateDiff(userModel.getVerificationCodeReqTime(), cur)>10*60))
    			{
	    			userModel.setVerificationCode(MiscUtil.randomNumber(6));
	    			userModel.setVerificationCodeReqTime(cur);
	    			userModel.setVerificationCodeReqTimes((userModel.getVerificationCodeReqTimes()==null?0:userModel.getVerificationCodeReqTimes())+1);
	    			userModel.setVerificationCodeRspResult(false);
	    			userModel.setVerificationCodeRspTime(null);
	    			mongoDBService.save("forgetpassword1",userModel);
	    			emailService.sendVerificationCode(email, userModel,userModel.getVerificationCode());
    			}
    			result.put("info", utilLocal("check_email_and_fill"));
    			result.put("status", "ok");
    			break;
    		case 2://input verification
    			if(code.length()<6)
    			{
    	    		result.put("status", utilLocal("verification_code_wrong"));
    	            return ResponseEntity.ok(result);
    			}
    			if(userModel.isVerificationCodeRspResult()==true)
    			{
    	    		result.put("status", utilLocal("repeat_verification"));
    	            return ResponseEntity.ok(result);
    			}
    			if(MiscUtil.dateDiff(userModel.getVerificationCodeReqTime(), cur)>30*60)
    			{
       	    		result.put("status", utilLocal("verification_code_expired"));
    	            return ResponseEntity.ok(result);
    			}
    			if(!code.equals(userModel.getVerificationCode()))
    			{
    	    		result.put("status", utilLocal("verification_code_incorrect"));
    	            return ResponseEntity.ok(result);
    			}
    			userModel.setVerificationCodeRspResult(true);
    			userModel.setVerificationCodeRspTime(cur);
    			mongoDBService.save("forgetpassword2",userModel);
    			result.put("step", 3);
    			result.put("info", utilLocal("set_new_password") );
    			result.put("status", "ok");
    			break;
    		case 3://input password
    			if((password1.length()==0)||(password2.length()==0)||(!password1.equals(password2))) //check password
    			{
    	    		result.put("status", utilLocal("invalid_password"));
    	            return ResponseEntity.ok(result);
    			}
    			if((!utilUserValid(null,password1,result))||(!utilUserValid(null,password2,result))) //check password
    	            return ResponseEntity.ok(result);
    			if(!(userModel.isVerificationCodeRspResult()&&(code.equals(userModel.getVerificationCode())))) //check code again
    			{
    	    		result.put("status", utilLocal("invalid_info") );
    	            return ResponseEntity.ok(result);
    			}
    			if(MiscUtil.dateDiff(userModel.getVerificationCodeRspTime(), cur)>30*60)//check time
    			{
       	    		result.put("status", utilLocal("time_expired") );
    	            return ResponseEntity.ok(result);
    			}
    			if(MiscUtil.userPasswordMatch(userModel.getPassword(), password1))
    			{
       	    		result.put("status", utilLocal("password_same") );
    	            return ResponseEntity.ok(result);
    			}
    			userModel.setVerificationCodeRspResult(false);
    			userModel.setVerificationCodeRspTime(null);
    			userModel.setVerificationCode(null);
    			userModel.setVerificationCodeReqTime(null);
    			userModel.setPassword(MiscUtil.userPasswordEncode(password1));
    			userModel.setPasswordType(UserModel.PasswordType.CHANGED);
    			userModel.setPassWordChangeTime(cur);
    			mongoDBService.save("forgetpassword3",userModel);
    	    	miscUtil.logout(response);
    			result.put("step", 4);
    			result.put("info", utilLocal("password_reset_success"));
    			result.put("status", "ok");
    			break;
    	}
        return ResponseEntity.ok(result);
    }
    @GetMapping("/pub/useractivate")
    public String pubUserActivate(@RequestParam Map<String, Object> paramsReq,HttpServletRequest request,Model model) {
    	String userId = MiscUtil.webParamsGet(paramsReq, "userId", String.class, "");
    	String token = MiscUtil.webParamsGet(paramsReq, "token", String.class, "");
    	String resultStr=utilLocal("wrong_link");
    	Instant now=Instant.now();
		mongoDBService.log(null, null, request.getRequestURI(), paramsReq, "pubUserForgetRefresh", null,null);
    	if((userId.length()>0)&&(token.length()>0))
    	{
	    	UserModel userModel=mongoDBService.findOneByField("id", userId, UserModel.class);
	        if((userModel!=null)&&(userModel.getEmailActivationToken()!=null)&&(userModel.getEmailActivationToken().equals(token)))
	        {        		
	        	userModel.setLatestIp(MiscUtil.clientGetIp(request));
	        	if(userModel.isEmailActivationRspResult()||(userModel.getUserStatus()!=UserModel.UserStatus.WAIT_ACTIVATION))
	        		resultStr=utilLocal("no_need_activate_again") ;
	        	else {
	        		resultStr=utilLocal("user_activated");
	        		userModel.setEmailActivationRspResult(true);
	        		userModel.setEmailActivationToken(null);
	        		userModel.setEmailActivationRspTime(now);
	        		userModel.setUserStatus(UserModel.UserStatus.ENABLED);
	        	}
	        	mongoDBService.save("useactivate",userModel);
	        }else
	        	resultStr=utilLocal("wrong_activation") ;
    	}
    	model.addAttribute("siteCompany",utilLocal("iot_factory"));
    	model.addAttribute("forwardMessage",resultStr);
        return "login"; 
    }
    @GetMapping("/pub/test")
    public String pubTest(Model model) {
    	//model.addAttribute("siteCompany",configProperties.SITE_COMPANY());
    	//model.addAttribute("captchaShow",1);
        return "test"; 
    }
    @GetMapping({"/", "/pub/login"})
    public String pubLogin(Model model) {
    	model.addAttribute("year",MiscUtil.dateFormat(Instant.now(), "yyyy"));
    	//model.addAttribute("captchaShow",1);
        return "login"; 
    }
    @PostMapping("/pub/loginrefresh")
    public ResponseEntity<Map<String,Object>> pubLoginRefresh(@RequestBody Map<String, Object> params,HttpServletRequest request, HttpServletResponse response) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	Instant cur=Instant.now();
    	String login = MiscUtil.webParamsGet(params, "login", String.class, "");
    	String password = MiscUtil.webParamsGet(params, "password", String.class, "");
    	String remember = MiscUtil.webParamsGet(params, "remember", String.class, "off");
    	if(!utilUserValid(login,password,result))//check the input of password and user	    			
    	{
    		result.put("captchaShow", 1);
	        return ResponseEntity.ok(result);       	
    	}
    	Object[] matchResult=miscUtil.loginMatch(request,response,login,password,remember,null,cur);//match according to db
    	
    	//check captcha
    	if(matchResult[1]!=null)
    	{
    		UserModel userModel=(UserModel)matchResult[1];
    		//there must be valid captcha as long as it failed within a specific time
    		if((userModel.getLoginFailedTime()!=null)&&((MiscUtil.dateDiff(userModel.getLoginFailedTime(), cur))<2*60))
			{
				if(pubCaptchaCheck(request, MiscUtil.webParamsGet(params, "captcha", String.class, ""),result)!=0)
					return ResponseEntity.ok(result);
			}
    	}
    	//start to check if need to send email activation link
    	if((matchResult[1]==null)||(((UserModel)matchResult[1]).getUserStatus()==UserModel.UserStatus.WAIT_ACTIVATION))
    	{
    		//before create user or active user, need captcha to confirm
			if(pubCaptchaCheck(request, MiscUtil.webParamsGet(params, "captcha", String.class, ""),result)!=0)
				return ResponseEntity.ok(result);
    		UserModel userModel=null;
	    	//user not exist or wait_activation
			if(matchResult[1]==null)//user not exist, then create new one
				userModel=utilUserCreate(null,null,UserModel.UserStatus.WAIT_ACTIVATION,login,password,cur,null,MiscUtil.clientGetIp(request));
			else if(((UserModel)matchResult[1]).getUserStatus()==UserModel.UserStatus.WAIT_ACTIVATION)// use wait_activiation status
				userModel=(UserModel)matchResult[1];
			
			if(userModel!=null)//before email is activated, user can change password and send email to activate again
			{
				userModel.setLatestIp(MiscUtil.clientGetIp(request));
				if((userModel.getEmailActivationReqTime()==null)||(MiscUtil.dateDiff(userModel.getEmailActivationReqTime(), cur))>120)
				{
					userModel.setPassword(MiscUtil.userPasswordEncode(password));
					userModel.setEmailActivationToken(MiscUtil.tokenCreate());
					userModel.setEmailActivationReqTime(cur);
					userModel.setEmailActivationReqTimes((userModel.getEmailActivationReqTimes()==null?0:userModel.getEmailActivationReqTimes())+1);
					emailService.sendActivation(login,userModel.getEmailActivationToken(),userModel,password);
					result.put("status", utilLocal("activation_link_sent") );
				}else
					result.put("status",utilLocal("submit_too_frequently") );			
				mongoDBService.save("loginrefresh",userModel);			
				return ResponseEntity.ok(result);
			}
        }

    	//login check
		if((matchResult!=null)&&(((Boolean)matchResult[0])==true))
		{
			UserModel userModel=(UserModel)matchResult[1];
			if(userModel.getUserStatus()==UserModel.UserStatus.DISABLED)
			{
	    		result.put("captchaShow", 1);
				result.put("status", utilLocal("user_disabled") );
			}
			else if(userModel.getUserStatus()==UserModel.UserStatus.FREEZED)
			{
	    		result.put("captchaShow", 1);
				result.put("status", utilLocal("user_freezed") );
			}
			else
				result.put("status", "ok");
    	}else {
    		result.put("captchaShow", 1);
    		result.put("status", utilLocal("user_password_wrong") );
    	}
        return ResponseEntity.ok(result);
    }
	@GetMapping("/pub/doc")
    public String pubDoc(Model model,@RequestParam Map<String, Object> params) {
    	String page = MiscUtil.webParamsGet(params, "page", String.class, "");
    	String deviceType = MiscUtil.webParamsGet(params, "deviceType", String.class, "");
    	model.addAttribute("siteCompany",utilLocal("iot_factory"));
    	model.addAttribute("siteUrl",configProperties.SITE_URL());
    	String uri="";
    	Locale locale=LocaleContextHolder.getLocale();
    	String language=locale.getLanguage();
    	String[] elements=page.split("_");
    	for(String element:elements)
    		uri=uri+"/"+element;
    	if(deviceType.length()>0)
    		deviceType="/"+deviceType;
    	uri="doc"+deviceType+uri+"_"+language;
    	//System.out.println(uri);
        return uri; 
    }
	 @GetMapping("/pub/doc/configlist")
    public ResponseEntity<Map<String,Object>> pubDocConfigList()
    {
    	List<Map<String,Object>> configResult=new ArrayList<Map<String,Object>>();       	
    	Map<Integer,ModuleType> moduleMap=ModuleDefine.ModuleType.getModuleMap();
    	if(moduleMap.size()>1)
    	{
	      	for(int i=0;i<(moduleMap.size()-1);i++)
	    	{
	      		ModuleType moduleType=moduleMap.get(i);
	      		if((moduleType==ModuleDefine.MODULE_BLE)||(moduleType==ModuleDefine.MODULE_SPI)||(moduleType==ModuleDefine.MODULE_I2C))
	      			continue;
	    		Map<String,Object> row=new HashMap<String,Object>();
	    		row.put("id",moduleType.getId());
	    		row.put("moduleName",moduleType.getName());
	    		row.put("order",moduleType.getId());
	    		configResult.add(row);
	    	}
    	}
    	if(configResult.size()==0)
    		return null;
    	Map<String,Object> result=new HashMap<>();
    	result.put("status", "ok");
    	result.put("configList", configResult);
        return ResponseEntity.ok(result);    
    }  
    @GetMapping("/web/logout")
    public String webLogout(HttpServletResponse response) {
    	miscUtil.logout(response);
        return "login"; 
    }
    private UserModel utilUserCreate(String parentId,String createById,UserModel.UserStatus userStatus,String email,String password,Instant cur,String actToken,String ip)
    {
    	UserModel userModel=new UserModel();
    	userModel.setParentId(parentId);
    	userModel.setCreateById(createById);
    	userModel.setEmail(email);
    	userModel.setLogin(email);
    	userModel.setPassword(MiscUtil.userPasswordEncode(password));
    	userModel.setBuildProjectLimitPerDay(20);
    	if(actToken!=null)
    	{
    		userModel.setEmailActivationReqTime(cur);
    		userModel.setEmailActivationReqTimes(1);
    		userModel.setEmailActivationToken(email);
    		userModel.setEmailActivationToken(actToken);
    	}
    	userModel.setLatestIp(ip);
    	userModel.setLatestVisitTime(cur);
    	userModel.setName(email);
    	userModel.setPasswordType(UserModel.PasswordType.INIT);
    	userModel.setRegisterTime(cur);
    	userModel.setUserStatus(userStatus);
    	userModel.setUserType(UserModel.UserType.PERSONAL);
    	userModel.setBuildProjectTimesPerDay(0);
    	
    	String keyStr=MiscUtil.MD5(UUID.randomUUID().toString().toLowerCase(),configProperties.MD5_API());
    	ApiKeyModel apiKeyModel=new ApiKeyModel();
		apiKeyModel.setCallTimes(0L);
		apiKeyModel.setCreateTime(cur);
		apiKeyModel.setLimitPerDay(10000);
		apiKeyModel.setLimitPerMinute(100);
		apiKeyModel.setAllowForward(true);
		apiKeyModel.setTimesPerDay(0);
		apiKeyModel.setTimesPerMinute(0);
		apiKeyModel.setName("sys");
		apiKeyModel.setRefreshTime(cur);
		apiKeyModel.setKey(keyStr);
		utilUserLevel(apiKeyModel, userModel);
		mongoDBService.save("usercreate",userModel);
		apiKeyModel.setUserId(userModel.getId());
		apiKeyModel.setUserStatus(userModel.getUserStatus());
		mongoDBService.save("utilUserCreate",apiKeyModel);
		
    	return userModel;
    }
    private boolean utilUserValid(String login,String password,Map<String,Object> result)
    {    	
    	if(login!=null)
    	{
    		if(login.length()==0)
    		{
	    		if(result!=null)
	    			result.put("status", utilLocal("user_name_empty"));
	    		return false;
    		}
    		if(login.contains(","))
    		{
	    		if(result!=null)
	    			result.put("status", utilLocal("user_name_contain_comma") );
	    		return false;
    		}
    	}
    	if((password!=null)&&(password.length()==0))
    	{
    		if(result!=null)
    			result.put("status", utilLocal("password_empty") );
    		return false;
    	}
    	if((login!=null)&&(configProperties.LOGIN_VALID()==1)&&(!MiscUtil.emailValid(login)))
    	{
    		if(result!=null)
    			result.put("status", utilLocal("not_valid_email") );
    		return false;
    	}
    	if((password!=null)&&(configProperties.PASSWORD_VALID()==1)&&(!MiscUtil.passwordValid(password)))
    	{
    		if(result!=null)
    			result.put("status", utilLocal("password_rule") );
    		return false;
    	}
    	return true;
    }
    private ApiKeyModel apiKeyValid(String key,String api,Map<String,Object> result)
    {
    	Instant now=Instant.now();
    	if(api.length()<3)
    	{
    		result.put("status", utilLocal("api_wrong") );
    		return null;
    	}
    	if((key==null)||(key.length()!=32))
    	{
    		result.put("status", utilLocal("key_is_wrong"));
    		return null;
    	}
    	ApiKeyModel apiKeyModel=mongoDBService.findOneByField("key", key, ApiKeyModel.class);
    	if(apiKeyModel==null)
    	{
    		result.put("status", utilLocal("key_not_exist"));
    		return null;
    	}
    	if((apiKeyModel.getUserStatus()==null)||(apiKeyModel.getUserStatus()!=UserModel.UserStatus.ENABLED))
    	{
    		result.put("status", utilLocal("user_not_enabled") );
    		return null;
    	}
    	//new minute
    	boolean minuteChange=false,dayChange=false;
    	if((apiKeyModel.getLatestCallTime()==null)||((apiKeyModel.getLatestCallTime().getEpochSecond()/60)!=(now.getEpochSecond()/60)))
    		minuteChange=true;
    	if((apiKeyModel.getLatestCallTime()==null)||((apiKeyModel.getLatestCallTime().getEpochSecond()/(3600*24))!=(now.getEpochSecond()/(3600*24))))
    		dayChange=true;
    		
		Map<String, CallModel> callModelMap=apiKeyModel.getApiCallMap();
		if(callModelMap==null)
		{
			callModelMap=new HashMap<>();
			apiKeyModel.setApiCallMap(callModelMap);
		}
		CallModel callModel=callModelMap.get(api);
		if(callModel==null)
		{
			callModel=new CallModel();
			callModel.setApiName(api);
			callModel.setCallTimes(0);
			callModel.setTimesPerDay(0);
			callModel.setTimesPerMinute(0);
			callModelMap.put(api, callModel);
		}
    	if(minuteChange) {
    		apiKeyModel.setTimesPerMinute(0);
    		callModel.setTimesPerMinute(0);
    	}
    	if(dayChange) {
    		apiKeyModel.setTimesPerDay(0);
    		callModel.setTimesPerDay(0);
    	}
    	//check limit
    	if((apiKeyModel.getLimitPerDay()!=null)&&(apiKeyModel.getTimesPerDay()>=apiKeyModel.getLimitPerDay()))
    	{
    		result.put("status", utilLocal("exceed_limit_per_day") );
    		return null;
    	}
    	if((apiKeyModel.getLimitPerMinute()!=null)&&(apiKeyModel.getTimesPerMinute()>=apiKeyModel.getLimitPerMinute()))
    	{
    		result.put("status", utilLocal("exceed_limit_per_minute"));
    		return null;
    	}
    	if((apiKeyModel.getApiLimitMap()!=null)&&(callModel!=null))
    	{
    		Map<String, LimitModel> apiLimitMap=apiKeyModel.getApiLimitMap();
    		LimitModel limitModel=apiLimitMap.get(api);
    		if(limitModel!=null)
    		{
    			if((limitModel.getLimitPerMinute()!=null)&&(callModel.getTimesPerMinute()!=null)&&(callModel.getTimesPerMinute()>=limitModel.getLimitPerMinute()))
    			{
    	    		result.put("status",  utilLocal("exceed_limit_per_minute_for") + " "+api);
    	    		return null;
    			}
    			if((limitModel.getLimitPerDay()!=null)&&(callModel.getTimesPerDay()!=null)&&(callModel.getTimesPerDay()>=limitModel.getLimitPerDay()))
    			{
    	    		result.put("status", utilLocal("exceed_limit_per_day_for") + " "+api);
    	    		return null;
    			}
    		}
    	}

    	//set new info
		callModel.setCallTimes(callModel.getCallTimes()+1);
		callModel.setLatestCallTime(now);
		callModel.setTimesPerDay(callModel.getTimesPerDay()+1);
		callModel.setTimesPerMinute(callModel.getTimesPerMinute()+1);
		apiKeyModel.setTimesPerMinute(apiKeyModel.getTimesPerMinute()+1);
		apiKeyModel.setTimesPerDay(apiKeyModel.getTimesPerDay()+1);
    	apiKeyModel.setCallTimes(apiKeyModel.getCallTimes()+1);
    	apiKeyModel.setLatestCallTime(now);
    	mongoDBService.save("apikeyvalid",apiKeyModel);
    	return apiKeyModel;
    }
    //@PostMapping("/api/call")
    @RequestMapping(value = "/api/call", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<Map<String,Object>> apiCall(@RequestParam Map<String, Object> paramsReq,@RequestBody(required = false) Map<String, Object> paramsBody,HttpServletRequest request) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	String api = MiscUtil.webParamsGet(paramsReq, "api", String.class, "");
    	String key = MiscUtil.webParamsGet(paramsReq, "key", String.class, "");
    	if((paramsBody!=null)&&(api.length()==0))
    		api = MiscUtil.webParamsGet(paramsBody, "api", String.class, "");
    	if((paramsBody!=null)&&(key.length()==0))
    		key = MiscUtil.webParamsGet(paramsBody, "key", String.class, "");
    	
    	ApiKeyModel apiKeyModel=apiKeyValid(key,api,result);
    	if(apiKeyModel==null)
    		return ResponseEntity.ok(result);
    	UserModel userModel=mongoDBService.findOneByField("id", apiKeyModel.getUserId(), UserModel.class);
    	if(userModel==null)
    	{
    		result.put("status", utilLocal("user_not_exist") );
    		return ResponseEntity.ok(result);
    	}
    	if(userModel.getParentId()!=null) {
    		result.put("status", utilLocal("child_user_not_call_api") );
    		return ResponseEntity.ok(result);
    	}
    	Login login=new Login();
		login.setUserId(userModel.getId());
		login.setUserlogin(userModel.getLogin());
		login.setUserModel(userModel);
		login.setCommand(api);
		login.setUserName(userModel.getName());
		ResponseEntity<Map<String,Object>> resultApi=null;
		Map<String, Object> paramsBody2 = new HashMap<>();
		if(paramsBody!=null)
			paramsBody2.putAll(paramsBody);
    	if(paramsReq!=null)
    	{
	        paramsReq.forEach((key1, value) -> {
	            if (!"api".equals(key1) && !"key".equals(key1)) {
	            	paramsBody2.put(key1, value);
	            }
	        });
    	}
		CacheUtil.threadlocallogin.set(login);
		mongoDBService.log(null, null, null, paramsBody2, "apiCall", null,null);
		try 
		{
			resultApi = apiCallExe(api, paramsBody2,request);
		}catch(Exception e)
		{
			logger.error(e.getMessage(),e);
		}
		CacheUtil.threadlocallogin.remove();
		if(resultApi==null) {
			result.put("status", utilLocal("unknown_error") );
			return ResponseEntity.ok(result);
		}
    	return resultApi;
    }
	private ResponseEntity<Map<String, Object>> apiCallExe(String api,Map<String, Object> paramsBody,HttpServletRequest request) 
	{
		ResponseEntity<Map<String,Object>> resultApi=null;
		Map<String,Object> result=new HashMap<String,Object>();
		result.put("status", "ok");
		switch(api)
		{
			case "device":
				resultApi=webDeviceListRefresh(paramsBody);
				break;
			case "forward":
			case "alert":
			case "module":
				if((paramsBody!=null)&&(paramsBody.containsKey("deviceId"))&&(!paramsBody.containsKey("moduleTypeId")))
					resultApi=webDeviceViewRefresh(paramsBody);
				else
					resultApi=webDeviceModuleRefresh(paramsBody,request);
				if(resultApi!=null) {
					Map<String,Object> bodyMap=resultApi.getBody();
					bodyMap.remove("sectionMap");
					@SuppressWarnings("unchecked")
					Map<String,Object> aggMap=(Map<String,Object>)bodyMap.get("aggMap");
					if(aggMap!=null)
						aggMap.remove("chartMeta");
				}
				break;
			case "user":
				resultApi=webUserListRefresh(paramsBody);
				if(resultApi!=null)
					resultApi.getBody().remove("userListMeta");
				break;
			case "command":
				resultApi=apiCmdUtil(paramsBody, result,request);
		    	break;
		    default:
				result.put("status",utilLocal("api_not_exist")  );
				resultApi=ResponseEntity.ok(result);
		    	break;
		}
		return resultApi;
	}
	private ResponseEntity<Map<String, Object>> apiCmdUtil(Map<String, Object> paramsBody, Map<String, Object> result,HttpServletRequest request) {
		//operateExecutor
		//operate=2 gpio_ext_no=8 status=1 
		String cmd = MiscUtil.webParamsGet(paramsBody, "cmd", String.class, "");
		cmd=MiscUtil.stringTrim(cmd, "\"", "\"");
		String deviceId = MiscUtil.webParamsGet(paramsBody, "deviceId", String.class, "");
		logger.info("device "+deviceId+" cmd "+cmd);
		if(cmd.length()==0)
		{
			result.put("status", utilLocal("param_cmd_wrong")  );
			return ResponseEntity.ok(result);
		}
		Object[] devicePriv=utilHasPriv(deviceId);
		if(((Boolean)devicePriv[0]==false)||(((String)devicePriv[1]).equals("read")))
		{
			result.put("status", utilLocal("no_priv"));
			return ResponseEntity.ok(result);
		}
		DeviceModel deviceModel=(DeviceModel)devicePriv[2];
		OperateRequest operateRequest=new OperateRequest();
		operateRequest.setRawString(cmd);
		operateRequest.setIp(request.getRemoteHost());
		operateRequest.setSourceType(DataCommModel.DataCommSource.CLIENT_API);
		ResultType opeResult=operateExecutor.exeOperate(operateRequest, deviceModel);
		mongoDBService.save("cmdutil",deviceModel); 
		if(opeResult.getOperateSpec()!=null)
			result.put("module", opeResult.getOperateSpec().getModuleType().getName());
		result.put("cmdCode", opeResult.getErrorType());
		result.put("cmdStatus",opeResult.getErrorString());
		String tempStr=MiscUtil.strDisplay(opeResult.getHexContent());
	    result.put("cmdResponse",tempStr);
		return ResponseEntity.ok(result);
	}
	@GetMapping("/web/doc")
    public String webDoc(Model model,@RequestParam Map<String, Object> params) {
    	String page = MiscUtil.webParamsGet(params, "page", String.class, "");
    	String deviceType = MiscUtil.webParamsGet(params, "deviceType", String.class, "");
    	model.addAttribute("siteCompany",utilLocal("iot_factory"));
    	model.addAttribute("siteUrl",configProperties.SITE_URL());
    	String uri="";
    	Locale locale=LocaleContextHolder.getLocale();
    	String language=locale.getLanguage();
    	String[] elements=page.split("_");
    	for(String element:elements)
    		uri=uri+"/"+element;
    	if(deviceType.length()>0)
    		deviceType="/"+deviceType;
    	uri="doc"+deviceType+uri+"_"+language;
    	switch(elements[0])
    	{
	    	case "api":
	    		model.addAttribute("docName",utilLocal("api_list"));
	        	Login login=CacheUtil.threadlocallogin.get();
	        	ApiKeyModel apiKeyModel=mongoDBService.findOneByField("userId", login.getUserId(), ApiKeyModel.class);
	        	if(apiKeyModel==null) 
	        		apiKeyModel=webApiKeyCreateModify(login.getUserModel(), apiKeyModel,null);
	        	model.addAttribute("apiKey",apiKeyModel.getKey());
	        	String apiUri = MiscUtil.webParamsGet(params, "apiUri", String.class, "");
	        	if(uri.length()>0) 
	        	{
		        	Map<String,Object> apiRunning=webApiStat(apiKeyModel,apiUri);
		        	if(apiRunning!=null)
		        		model.addAttribute("apiRunning",MiscUtil.jsonObject2String(apiRunning));
	        	}	        	
	        	utilBreadcrumb(model,new String[][]{
	                {"Commands", "/web/doc?page=api_index"}
	            });
	    		break;
    	}
        return uri; 
    }
    @GetMapping("/web/llmsetting")
    public String webLLMSetting(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_llm"), "llmsetting"}
        });
        return "llmsetting"; 
    }
    @PostMapping("/web/llmsettingrefresh")
    public ResponseEntity<Map<String,Object>> webLLMSettingRefresh(@RequestBody Map<String, Object> paramsBody) 
    {
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	return ResponseEntity.ok(result);
    }
    @GetMapping("/web/apisetting")
    public String webApiSetting(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_api"), "apisetting"}
        });
        return "apisetting"; 
    }
    @PostMapping("/web/apisettingrefresh")
    public ResponseEntity<Map<String,Object>> webApiSettingRefresh(@RequestBody Map<String, Object> paramsBody) 
    {
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
		String forwardEmail = MiscUtil.webParamsGet(paramsBody, "forwardEmail", String.class, "").trim();
		String forwardEmailVerificationCode = MiscUtil.webParamsGet(paramsBody, "forwardEmailVerificationCode", String.class, "").trim();
		Integer forwardEmailStatus = MiscUtil.webParamsGet(paramsBody, "forwardEmailStatus", Integer.class, 0);
		String forwardEmailRule = MiscUtil.webParamsGet(paramsBody, "forwardEmailRule", String.class, null);
		
    	Instant now=Instant.now();
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	Login login=CacheUtil.threadlocallogin.get();
    	UserModel userModel=login.getUserModel();
    	if(userModel.getParentId()!=null)
    		return ResponseEntity.ok(result);
    	ApiKeyModel apiKeyModel=mongoDBService.findOneByField("userId", login.getUserId(), ApiKeyModel.class);
    	String url =null;
    	if(action.equals("updateUrl")||action.equals("testUrl")||action.equals("updateEmail") ||action.equals("updateForward")||action.equals("updateSmtp")) 
    	{
			if(apiKeyModel==null)
    		{
    			result.put("status", utilLocal("create_key_first") );
    			return ResponseEntity.ok(result);
    		}
    		url = MiscUtil.webParamsGet(paramsBody, "url", String.class, "");
    	}
		mongoDBService.log(null, null, null, paramsBody, "webApiSettingRefresh", null,null);
    	switch(action)
    	{
    		case "updateSmtp":
    			String smtpServer = MiscUtil.webParamsGet(paramsBody, "smtpServer", String.class, null);
    			Integer smtpPort = MiscUtil.webParamsGet(paramsBody, "smtpPort", Integer.class, null);
    			String smtpUserName = MiscUtil.webParamsGet(paramsBody, "smtpUserName", String.class, null);
    			String smtpPassword = MiscUtil.webParamsGet(paramsBody, "smtpPassword", String.class, null);
    			Integer smtpAuth = MiscUtil.webParamsGet(paramsBody, "smtpAuth", Integer.class, null);
    			Integer smtpTls = MiscUtil.webParamsGet(paramsBody, "smtpTls", Integer.class, null);
    			Integer smtpSsl = MiscUtil.webParamsGet(paramsBody, "smtpSsl", Integer.class, null);
    	        if (smtpServer == null || smtpPort == null || smtpUserName == null || smtpPassword == null ||
    	            smtpAuth == null || smtpTls == null || smtpSsl == null ) {
        			result.put("status", utilLocal("error_smtp_enter_all"));
        			return ResponseEntity.ok(result);
    	        }
    	        if (!(smtpAuth == 0 || smtpAuth == 1)) {
        			result.put("status", utilLocal("error_smtp_auth"));
        			return ResponseEntity.ok(result);
    	        }
    	        if (!(smtpTls == 0 || smtpTls == 1)) {
        			result.put("status", utilLocal("error_smtp_tls"));
        			return ResponseEntity.ok(result);
    	        }
    	        if (!(smtpSsl == 0 || smtpSsl == 1)) {
        			result.put("status", utilLocal("error_smtp_ssl"));
        			return ResponseEntity.ok(result);
    	        }
    	        UserModel.UserSmtpModel userSmtpModel=userModel.getSmtp();
    			if(userSmtpModel==null)
    			{
    				userSmtpModel=new UserModel.UserSmtpModel();
    				userModel.setSmtp(userSmtpModel);
    			}
    			userSmtpModel.setHost(smtpServer);
    			userSmtpModel.setPassword(smtpPassword);
    			userSmtpModel.setPort(smtpPort);
    			userSmtpModel.setSmtpAuth(smtpAuth);
    			userSmtpModel.setSmtpSslEnable(smtpSsl);
    			userSmtpModel.setSmtpStartTlsEnable(smtpTls);
    			userSmtpModel.setUserName(smtpUserName);
    			mongoDBService.save("webApiSettingRefresh",userModel);
    			result.put("info", utilLocal("update_ok"));
    			return ResponseEntity.ok(result);
    		case "keyAdd":
        		String name = MiscUtil.webParamsGet(paramsBody, "name", String.class, "");
			    apiKeyModel =  webApiKeyCreateModify(userModel, apiKeyModel,name);
    			break;
    		case "updateForwardEmail":
    			if((forwardEmailStatus>=2)&&(apiKeyModel.getForwardEmailStatus()!=null)&&((apiKeyModel.getForwardEmailStatus()==2)||(apiKeyModel.getForwardEmailStatus()==3)))
    			{
    				apiKeyModel.setForwardEmailStatus(forwardEmailStatus);
    				mongoDBService.save("updateEmail", apiKeyModel);
    			}
    			break;
    		case "updateEmail":

    			boolean ruleChanged=false;
    			//set rule
    			if((forwardEmailStatus>=2)&&(!Objects.equals(apiKeyModel.getForwardEmailRule(),forwardEmailRule)))
    			{
    				apiKeyModel.setForwardEmailRule(forwardEmailRule);
    				ruleChanged=true;
    			}
    			if(forwardEmail.length()==0)//clear email address
    			{
    				if((apiKeyModel.getForwardEmail()==null)||(apiKeyModel.getForwardEmail().length()==0))
    				{
            			result.put("status", utilLocal("email_already_empty") );
            			return ResponseEntity.ok(result);
    				}
            		apiKeyModel.setForwardEmail(null);
            		apiKeyModel.setForwardEmailStatus(0);
            		apiKeyModel.setForwardEmailVerificationTime(null);
        			apiKeyModel.setForwardEmailVerificationCode(null);
        			
        			mongoDBService.save("updateEmail", apiKeyModel);
        			result.put("alarmInfo", utilLocal("email_is_removed") );
    			}else if((forwardEmail.length()>0)&&(!MiscUtil.emailValid(forwardEmail)))//not valid email
        		{
        			result.put("status", utilLocal("not_valid_email")  );
        			return ResponseEntity.ok(result);
        		}else if((((apiKeyModel.getForwardEmailStatus()<=1)||(apiKeyModel.getForwardEmail()==null)||(!apiKeyModel.getForwardEmail().equals(forwardEmail))))&&(forwardEmailVerificationCode.length()==0))
        		{
        			//sent out verification, 0,1状态或者email地址变化; 且2分钟内没发过;且没输入verification code
        			if((apiKeyModel.getForwardEmailVerificationTime()==null)||(MiscUtil.dateDiff(apiKeyModel.getForwardEmailVerificationTime(), now)>120))
        			{
	            		apiKeyModel.setForwardEmail(forwardEmail);
	            		apiKeyModel.setForwardEmailStatus(1);
	            		apiKeyModel.setForwardEmailVerificationTime(now);
	        			apiKeyModel.setForwardEmailVerificationCode(MiscUtil.randomNumber(6));
	        			mongoDBService.save("updateEmail", apiKeyModel);
	        			emailService.sendVerificationCode(forwardEmail,login.getUserModel(), apiKeyModel.getForwardEmailVerificationCode());
	        			result.put("alarmInfo", utilLocal("verification_sent_out") );
        			}else {
        				result.put("alarmInfo", utilLocal("verification_request_too_frequent") );
        			}
        		}else if(forwardEmailVerificationCode.length()>0)//verify email
        		{
        			if((apiKeyModel.getForwardEmailVerificationCode()==null)||(apiKeyModel.getForwardEmailVerificationTime()==null)||(MiscUtil.dateDiff(apiKeyModel.getForwardEmailVerificationTime(), now)>300))
        			{
            			result.put("alarmInfo", utilLocal("verification_code_expired") );
            			return ResponseEntity.ok(result);
        			}
        			if(!forwardEmailVerificationCode.equals(apiKeyModel.getForwardEmailVerificationCode()))
        			{
            			result.put("alarmInfo", utilLocal("verification_code_incorrect") );
            			return ResponseEntity.ok(result);
        			}
        			if(!forwardEmail.equals(apiKeyModel.getForwardEmail()))
        			{
            			result.put("alarmInfo", utilLocal("email_incorrect_with_before") );
            			return ResponseEntity.ok(result);
        			}
            		apiKeyModel.setForwardEmailStatus(2);
            		apiKeyModel.setForwardEmailVerificationTime(now);
        			apiKeyModel.setForwardEmailVerificationCode(null);
        			mongoDBService.save("updateEmail", apiKeyModel);
        			result.put("alarmInfo", utilLocal("verification_successful") );
        		}else if((!forwardEmailStatus.equals(apiKeyModel.getForwardEmailStatus()))&&(forwardEmailStatus>1)&&(forwardEmailStatus<4)&&(apiKeyModel.getForwardEmailStatus()>1)) {
        			//continue or pause
        		    //forwardEmailStatus;//0:no email, 1:pending verification, 2: normal work, 3:pause
        			apiKeyModel.setForwardEmailStatus(forwardEmailStatus);
        			mongoDBService.save("updateEmail", apiKeyModel);
        		}else if(ruleChanged)
        		{
        			mongoDBService.save("updateEmail", apiKeyModel);
        			result.put("alarmInfo", utilLocal("rule_modified") );
        		}else
        			result.put("alarmInfo", utilLocal("nothing_changed") );
        		break;
    		case "updateForward":
    			Integer forward = MiscUtil.webParamsGet(paramsBody, "forward", Integer.class, null);
    			if((forward==null)||(forward>1)||(forward<0))
    				return null;
    			apiKeyModel.setAllowForward((forward==0?false:true));
    			mongoDBService.save("apiset2", apiKeyModel);
    			result.put("status", "ok");
    			return ResponseEntity.ok(result);
    		case "updateUrl":
        		if((url.trim().length()>0)&&(!MiscUtil.URLValid(url, true)))
        		{
        			result.put("status", utilLocal("not_valid_url") );
        			return ResponseEntity.ok(result);
        		}
    			apiKeyModel.setForwardUrl(url);
    			mongoDBService.save("apiset3", apiKeyModel);
    			result.put("info", utilLocal("update_ok"));
    			return ResponseEntity.ok(result);
    		case "testUrl":
        		url = MiscUtil.webParamsGet(paramsBody, "url", String.class, "");
        		if(!MiscUtil.URLValid(url, true))
        		{
        			result.put("status", utilLocal("not_valid_url"));
        			return ResponseEntity.ok(result);
        		}
    			apiKeyModel.setForwardUrl(url);
    			//DeviceModel deviceModel=mongoDBService.findOneByField("deviceNo", "24587cd06b90", DeviceModel.class);
        		PostLogModel postLogModel=webClientService.postMap("xxxx","xxxx",apiKeyModel,ModuleDefine.MODULE_GENERAL,true);

        		String checkStr=postLogModel.getRx();
        		checkStr=MiscUtil.URLDecode(checkStr);
        		if(checkStr!=null)
        			checkStr=checkStr.length() <= 100 ? checkStr : checkStr.substring(0, 100);
        		if(postLogModel.getError()<0)
        		{
        			result.put("status", checkStr);
        			return ResponseEntity.ok(result);
        		}
        		result.put("info", utilLocal("test_success") + " "+checkStr);

    			return ResponseEntity.ok(result);
    	}
    	
    	if(apiKeyModel!=null) 
    	{
    		Map<String,Object> apiSetting=webApiStat(apiKeyModel,null);
        	if((apiSetting!=null)&&(apiSetting.size()>0)) {
        		List<Map<String,Object>> tempList=new ArrayList<>();
        		tempList.add(apiSetting);
        		result.put("apiSetting", tempList);
        	}
    		result.put("allowForward", apiKeyModel.getAllowForward());
	    	if((apiKeyModel.getForwardUrl()!=null)&&(apiKeyModel.getForwardUrl().length()>0)) {
	    		result.put("forwardUrl", apiKeyModel.getForwardUrl());
	    	}
	    	result.put("forwardEmailStatus", apiKeyModel.getForwardEmailStatus());
	    	result.put("forwardEmailRule", apiKeyModel.getForwardEmailRule());
	    	if((apiKeyModel.getForwardEmail()!=null)&&(apiKeyModel.getForwardEmail().length()>0)) {
	    		result.put("forwardEmail", apiKeyModel.getForwardEmail());
	    	}
	    	UserModel.UserSmtpModel userSmtpModel=userModel.getSmtp();
			if(userSmtpModel!=null)
			{
				result.put("smtpServer", userSmtpModel.getHost());
				result.put("smtpPassword", userSmtpModel.getPassword());
				result.put("smtpPort", userSmtpModel.getPort());
				result.put("smtpAuth", userSmtpModel.getSmtpAuth());
				result.put("smtpSsl", userSmtpModel.getSmtpSslEnable());
				result.put("smtpTls", userSmtpModel.getSmtpStartTlsEnable());
				result.put("smtpUserName", userSmtpModel.getUserName());
			}
    	}
    	return ResponseEntity.ok(result);
    }
	private ApiKeyModel webApiKeyCreateModify(UserModel userModel, ApiKeyModel apiKeyModel,String name) {
		Instant now=Instant.now();
		if(name==null) name="auto";
		String keyStr=MiscUtil.MD5(UUID.randomUUID().toString().toLowerCase(),configProperties.MD5_API());
		if(apiKeyModel==null)
		{  			
			apiKeyModel=new ApiKeyModel();
			apiKeyModel.setCallTimes(0L);
			apiKeyModel.setCreateTime(now);
			apiKeyModel.setAllowForward(true);
			apiKeyModel.setTimesPerDay(0);
			apiKeyModel.setTimesPerMinute(0);
			apiKeyModel.setUserId(userModel.getId());
			apiKeyModel.setForwardEmailStatus(0);
			apiKeyModel.setForwardEmailTimesPerDay(0);
			utilUserLevel(apiKeyModel,userModel);
		}
		apiKeyModel.setUserStatus(userModel.getUserStatus());
		apiKeyModel.setName(name);
		apiKeyModel.setRefreshTime(now);
		apiKeyModel.setKey(keyStr);
		mongoDBService.save("apiset1",apiKeyModel);
		return apiKeyModel;
	}
    private Map<String,Object> webApiStat(ApiKeyModel apiKeyModel,String apiName)
    {
		if(apiKeyModel!=null)
    	{
			//clear the times
	    	boolean minuteChange=false,dayChange=false;
	    	if(MiscUtil.isTimeNotCurrentMinute(apiKeyModel.getLatestCallTime()))
	    		minuteChange=true;
	    	if(MiscUtil.isTimeNotCurrentDay(apiKeyModel.getLatestCallTime()))
	    		dayChange=true;
	    	if(minuteChange) 
	    		apiKeyModel.setTimesPerMinute(0);
	    	if(dayChange) {
	    		apiKeyModel.setTimesPerDay(0);
	    		apiKeyModel.setForwardEmailTimesPerDay(0);
	    	}
	    	if(minuteChange||dayChange)
	    		mongoDBService.save("clearlimit",apiKeyModel);
	    	//display
			Map<String,Object> row=new HashMap<String,Object>();
	    	if((apiName!=null)&&(apiName.length()>0)) 
	    	{
				ApiKeyModel.CallModel call=null;
				ApiKeyModel.LimitModel limit=null;	
				if(apiKeyModel.getApiCallMap()!=null) 
					call=apiKeyModel.getApiCallMap().get(apiName);
				if(apiKeyModel.getApiLimitMap()!=null) 
					limit=apiKeyModel.getApiLimitMap().get(apiName);
				if((minuteChange)&&(call!=null))
					call.setTimesPerMinute(0);
				if((dayChange)&&(call!=null))
				    call.setTimesPerDay(0);
				Map<String,Object> apiStat=new HashMap<>();
				apiStat.put("name", apiName);
				if(call!=null)
				{
					apiStat.put("callTimes", call.getCallTimes());
					apiStat.put("latestCallTime", MiscUtil.dateFormat(call.getLatestCallTime(),"MM-dd HH:mm:ss"));
					apiStat.put("timesPerMinute", call.getTimesPerMinute());
					apiStat.put("timesPerDay", call.getTimesPerDay());
				}
				if(limit!=null)
				{
					apiStat.put("limitPerMinute", limit.getLimitPerMinute());
					apiStat.put("limitPerDay", limit.getLimitPerDay());
				}
				row.put("apiStat", apiStat);
	    	}
			row.put("name",apiKeyModel.getName());
			row.put("key",apiKeyModel.getKey());
			row.put("timesPerMinute",apiKeyModel.getTimesPerMinute());
			row.put("limitPerMinute",apiKeyModel.getLimitPerMinute());
			row.put("timesPerDay",apiKeyModel.getTimesPerDay());
			row.put("limitPerDay",apiKeyModel.getLimitPerDay());
			row.put("emailTimesPerDay",MiscUtil.strDisplay(apiKeyModel.getForwardEmailTimesPerDay()));
			row.put("emailLimitPerDay",apiKeyModel.getForwardEmailLimitPerDay());
			row.put("calls",apiKeyModel.getCallTimes());
			row.put("createTime",MiscUtil.dateFormat(apiKeyModel.getCreateTime(),"MM-dd HH:mm:ss"));
			row.put("refreshTime",MiscUtil.dateFormat(apiKeyModel.getRefreshTime(),"MM-dd HH:mm:ss"));
			row.put("latestCallTime",MiscUtil.dateFormat(apiKeyModel.getLatestCallTime(),"MM-dd HH:mm:ss"));
			row.put("fowardUrl", MiscUtil.strDisplay(apiKeyModel.getForwardUrl()));
			return row;
		}
		return null;
	}
   
   
    @GetMapping("/web/userlist")
    public String webUserList(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_users"), "userlist"}
        });
    	int isAdmin=CacheUtil.threadlocallogin.get().getUserModel().getUserType()==UserModel.UserType.ADMIN?1:0;
    	model.addAttribute("isAdmin", isAdmin);
        return "userlist"; 
    }
    //fieldMeta=Object[2], 0=fieldName,1=Options (String,List<String>,List<Integer>,List<Integer[]>)
    private Object paramsUpdateValid(Map<String, Object[]> fieldsMeta, Map<String, Object> paramsBody,Map<String,Object> result) 
    {
    	String fieldType = MiscUtil.webParamsGet(paramsBody, "fieldType", String.class, "");
    	String fieldName = MiscUtil.webParamsGet(paramsBody, "fieldName", String.class, "");
    	Object[] fieldOption=(Object[])fieldsMeta.get(fieldName);
    	if((fieldName.length()==0)||(fieldOption==null))
    	{
    		result.put("status", utilLocal("field_name_wrong") );
    		return null;
    	}
    	String optionString="";
    	if(fieldType.equals("string"))
    	{
    		String fieldValue = MiscUtil.webParamsGet(paramsBody, "fieldValue", String.class, "");
	        if(fieldOption[0].toString().equals("list-string"))
	        {
	    		if(fieldValue.length()==0)
	    		{
	        		result.put("status", utilLocal("field_value_empty"));
	        		return null;
	    		}
	        	@SuppressWarnings("unchecked")
				List<String> fieldOptionList=(List<String>)fieldOption[1];
	        	if(fieldOptionList.size()>0)
	        	{
		        	for(String option:fieldOptionList)
		        	{
		        		optionString=optionString+option+" ";
		        		if(option.equalsIgnoreCase(fieldValue))
		        			return fieldValue;
		        	}
	        	}
	        	result.put("status", utilLocal("select_from")+" "+optionString.trim());
	        }else if(fieldOption[0].toString().equals("pattern-string")){
	        	if((fieldValue.length()==0)||(fieldValue.matches((String)fieldOption[1])))
	        		return fieldValue;
	        	result.put("status", utilLocal("wrong_content") );
    		}else if(fieldOption[0].toString().equals("free-string")){
    			int min=((Integer[])fieldOption[1])[0];
    			int max=((Integer[])fieldOption[1])[1];
	        	if((fieldValue.length()>=min)&&(fieldValue.length()<=max))
	        		return fieldValue;
	        	result.put("status", utilLocal("length_between")+ " "+min+" "+utilLocal("and")+" "+max);
    		}
    		return null;
    	}
//    	else if(fieldType.equals("number"))
//    	{
//    		Integer fieldValue = MiscUtil.webParamsGet(paramsBody, "fieldValue", Integer.class, null);
//    		if(fieldValue==null)
//    		{
//        		result.put("status", "Field value empty");
//        		return null;
//    		}
//    		if(fieldOption instanceof List)
//    		{
//    		    if(((List)fieldOption).get(0) instanceof Integer)
//    			{
//    		    	List<Integer> fieldOptionList=(List<Integer>)fieldOption;
//    		    	for(Integer option:fieldOptionList)
//    		    	{
//    	        		optionString=optionString+option+" ";
//    	        		if(option.intValue()==fieldValue.intValue())
//    	        			return fieldValue;
//    		    	}
//    			}else if(((List)fieldOption).get(0) instanceof Integer[])
//    			{
//    		    	List<Integer[]> fieldOptionList=(List<Integer[]>)fieldOption;
//    		    	for(Integer[] option:fieldOptionList)
//    		    	{
//    	        		optionString=optionString+"["+option[0]+","+option[1]+"] ";
//    	        		if((option[0].intValue()<=fieldValue.intValue())&&(option[1].intValue()>=fieldValue.intValue()))
//    	        			return fieldValue;
//    		    	}
//    			}
//        		result.put("status", "Select from "+optionString.trim());
//        		return null;
//    		}
//    	}
    	result.put("status", utilLocal("unknown_reason") );
        return null; 
    }
    private static final Map<String, Object[]> userUpdatefieldsMeta = Map.of(
    		"userType",   new Object[]{"list-string",Arrays.stream(UserModel.UserType.values()).map(UserModel.UserType::name).collect(Collectors.toList())},
            "userStatus",   new Object[]{"list-string",Arrays.stream(UserModel.UserStatus.values()).map(UserModel.UserStatus::name).collect(Collectors.toList())},
            "clonedUser",   new Object[]{"pattern-string","^(?!,)([^,]+(,[^,]+)*)?$"}//comma separated
        );  
    private UserModel.UserCloneModel webUserListCloneCheck(UserModel userModelModified,String users,Map<String,Object> result)
    {
    	Login login=CacheUtil.threadlocallogin.get();
        Map<String,String> userIdMap = new HashMap<>();
        List<String> listUserLogin=new ArrayList<>();
        List<String> listUserId=new ArrayList<>();
        
        if(users.trim().length()>0)
        {
	        List<UserModel> userModelList=mongoDBService.findByField("parentId", login.getUserId(), UserModel.class);//all child
	        if(userModelList!=null) {
		        for (UserModel userModel : userModelList) {
		        	if(!userModelModified.getLogin().equals(userModel.getLogin()))
		        		userIdMap.put(userModel.getLogin(), userModel.getId());
		        }
	        }
	        userIdMap.put(login.getUserlogin(),login.getUserId());
	        
	        Set<String> checkedIds = new HashSet<>(Arrays.asList(users.split(",")));
	        for (String id : checkedIds) {
	        	String userId=userIdMap.get(id.trim());
	            if((userId==null)||(userId.length()==0)) {
	            	result.put("status", id+" "+utilLocal("is_wrong"));
	                return null; 
	            }
	            listUserLogin.add(id);
	            listUserId.add(userId);
	        }
        }
        UserModel.UserCloneModel userCloneModel=new UserModel.UserCloneModel();
        userCloneModel.setLoginList(listUserLogin);
        userCloneModel.setUserIdList(listUserId);
        return userCloneModel;
      
    }
    private void utilUserLevel(ApiKeyModel apiKeyModel,UserModel userModel)
    {
    	userModel.setProjectAccessType(ProjectAccessType.LIMIT_WRITE);
    	if(apiKeyModel!=null)
    	{
    		int limitPerDay=0, limitPerMinute=0,emailLimitPerDay=0,buildLimitPerDay=0,otaLimit=0;
    		switch(userModel.getUserType())
    		{
    			case UserModel.UserType.PERSONAL:
    				limitPerMinute=2;
    				limitPerDay=100;
    				emailLimitPerDay=3;
    				buildLimitPerDay=3;
    				otaLimit=1;
    				break;
    			case UserModel.UserType.PROFESSIONAL:
    				limitPerMinute=10;
    				limitPerDay=10000;
    				emailLimitPerDay=1000;
    				buildLimitPerDay=50;
    				otaLimit=10;
    				break;
    			case UserModel.UserType.ENTERPRISE:
    				limitPerMinute=100;
    				limitPerDay=100000;
    				emailLimitPerDay=10000;
    				buildLimitPerDay=1000;
    				otaLimit=50;
    				break;
    			case UserModel.UserType.ADMIN:
    				limitPerMinute=999999999;
    				limitPerDay=999999999;
    				emailLimitPerDay=999999999;
    				buildLimitPerDay=999999999;
    				otaLimit=999999999;
    				break;
    		}
    		userModel.setBuildProjectLimitPerDay(buildLimitPerDay);
    		userModel.setOtaLimit(otaLimit);
    		apiKeyModel.setLimitPerMinute(limitPerMinute);
    		apiKeyModel.setLimitPerDay(limitPerDay);
    		apiKeyModel.setForwardEmailLimitPerDay(emailLimitPerDay);
    	}
    }
    @PostMapping("/web/userlistrefresh")
    public ResponseEntity<Map<String,Object>> webUserListRefresh(@RequestBody Map<String, Object> paramsBody) {
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
    	String userId = MiscUtil.webParamsGet(paramsBody, "userId", String.class, "");
    	String fieldName = MiscUtil.webParamsGet(paramsBody, "fieldName", String.class, "");
    	Instant now=Instant.now();
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	Login login=CacheUtil.threadlocallogin.get();
    	UserModel userModelLogin=login.getUserModel();
    	if(login.getUserModel().getParentId()!=null)
    		return ResponseEntity.ok(result);
    	UserModel userModelModified=null;
    	boolean isAdmin=userModelLogin.getUserType()==UserModel.UserType.ADMIN?true:false;
    	if((action.equals("userUpdate")||action.equals("userDel")||action.equals("userPasswordReset")))
    	{
	    	userModelModified=mongoDBService.findOneByField("id", userId, UserModel.class);
	    	if((!isAdmin)&&((userModelModified.getParentId()==null)||(!userModelModified.getParentId().equals(login.getUserId()))))
	    	{
				result.put("status", utilLocal("no_priv"));
				return ResponseEntity.ok(result);
	    	}
    	}
		mongoDBService.log(null, null, null, paramsBody, "webUserListRefresh", null,null);
    	switch(action)
    	{
	    	case "userPasswordReset":
	    		String password=MiscUtil.passwordNew();
	    		userModelModified.setPassword(MiscUtil.userPasswordEncode(password));
	    		userModelModified.setPassWordChangeTime(now);
	    		userModelModified.setPasswordType(UserModel.PasswordType.INIT);
	    		mongoDBService.save("passwordreset",userModelModified);
	    		if(MiscUtil.emailValid(userModelModified.getLogin()))
	    		{
	    			emailService.sendPasswordReset(userModelModified, password);
	    			result.put("alarmInfo",utilLocal("email_sent_inform_password_change"));
	    		}else {
	    			result.put("alarmInfo", utilLocal("password_changed_to") +" "+password);
	    		}
	    		break;
	    	case "userUpdate":
	    		Object fieldValue=paramsUpdateValid(userUpdatefieldsMeta,  paramsBody,result);
	    		if(fieldValue==null)
	    			return ResponseEntity.ok(result);        
	    		if((!isAdmin)&&fieldName.equals("userType"))
	    		{
	    			result.put("alarmInfo",utilLocal("admin_only_operate"));
	    			return ResponseEntity.ok(result);
	    		}
	    		if(fieldName.equals("clonedUser"))
	    		{
	    			UserModel.UserCloneModel clonedUserList=webUserListCloneCheck(userModelModified,fieldValue.toString(),result);
	    			if(clonedUserList==null)
	    				return ResponseEntity.ok(result);    
	    			fieldValue=clonedUserList;
	    		}
	    		mongoDBService.updateById(UserModel.class,userId, fieldName, fieldValue);
	    		if(fieldName.equals("userStatus")) //status change
	    			mongoDBService.updateByField(ApiKeyModel.class,"userId", userId, "userStatus",fieldValue);
	    		if(fieldName.equals("userType")) //type change
	    		{
	    			UserModel userModel=mongoDBService.findOneByField("id", userId, UserModel.class);
	    			ApiKeyModel apiKeyModel=mongoDBService.findOneByField("userId", userId, ApiKeyModel.class);
	    			utilUserLevel(apiKeyModel,userModel);
	    			if(apiKeyModel!=null)
	    				mongoDBService.save("userUpdate", apiKeyModel);
	    			mongoDBService.save("userUpdate", userModel);
	    		}
	    		break;
	    	case "userDel":
	    		if(!webUserListDel(login.getUserId(),userModelModified.getId(),result))
	    			return ResponseEntity.ok(result);
	    		break;
	    	case "userAdd":
	    		if(!webUserListAdd(paramsBody,result))
	    			return ResponseEntity.ok(result);
	    		break;
    	}
    	List<UserModel> userModelList=null;
    	if(isAdmin) {
    		Query query = new Query().addCriteria(Criteria.where("id").ne(login.getUserId()));
    		userModelList=mongoDBService.findByField(query, null, null, null, null,UserModel.class);
    	}
    	else
    		userModelList=mongoDBService.findByField("parentId", login.getUserId(), UserModel.class);
        if((userModelList!=null)&&(userModelList.size()>0))
        {
        	result.put("userList",  webUserListDisplay(userModelList));
        	result.put("userListMeta",userUpdatefieldsMeta);
        }
        return ResponseEntity.ok(result);          
    }
    private boolean webUserListDel(String userId,String userIdDel,Map<String,Object> result)
    {
    	mongoDBService.updateByField(DeviceModel.class,"userId", userIdDel,"userId", userId);//move devices to parent user
    	mongoDBService.delete("id", userIdDel, UserModel.class);
    	mongoDBService.delete("userId", userIdDel, ApiKeyModel.class);
    	return true;
    }
    private boolean webUserListAdd(Map<String, Object> paramsBody,Map<String,Object> result)
    {
    	Instant now=Instant.now();
    	Login login=CacheUtil.threadlocallogin.get();
    	if(login==null)
    	{
    		result.put("status", utilLocal("not_login") );
    		return false;
    	}
    	String userId=login.getUserId();
    	String userName = MiscUtil.webParamsGet(paramsBody, "userName", String.class, "");
    	String password = MiscUtil.webParamsGet(paramsBody, "password", String.class, "");
    	if((userName.length()==0)||(password.length()==0))
    	{
    		result.put("status", utilLocal("username_password_empty") );
    		return false;
    	}
    	if((!utilUserValid(userName, null, result))||(!utilUserValid(null, password, result)))
    		return false;
    	UserModel userModel=mongoDBService.findOneByField("login", userName, UserModel.class);
    	if(userModel!=null)
    	{
    		result.put("status", utilLocal("user_name_exist") );
    		return false;
    	}
    	String parentUserId=userId;
    	if(login.getUserModel().getUserType()==UserModel.UserType.ADMIN)
    		parentUserId=null;
    	userModel=utilUserCreate(parentUserId,userId,UserModel.UserStatus.ENABLED,userName,password,now,null,null);
    	emailService.sendUserCreate(userModel, password);
    	return true;
    }
    private List<Map<String,Object>>  webUserListDisplay(List<UserModel> userModelList)
    {
    	List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
    	for(UserModel userModel:userModelList)
    	{
    		Map<String,Object> row=new HashMap<String,Object>();
    		row.put("id",userModel.getId());
    		row.put("name",userModel.getLogin());
    		row.put("userStatus",userModel.getUserStatus());
    		row.put("userType",userModel.getUserType());
    		row.put("createBy",userModel.getCreateById()==null?"":CacheUtil.threadlocallogin.get().getUserlogin());
    		row.put("latestVisitTime",MiscUtil.dateFormat(userModel.getLatestVisitTime(),"MM-dd HH:mm:ss"));
    		row.put("registerTime",MiscUtil.dateFormat(userModel.getRegisterTime(),"MM-dd HH:mm:ss"));
    		if((userModel.getClonedUser()!=null)&&(userModel.getClonedUser().getLoginList()!=null)&&(userModel.getClonedUser().getLoginList().size()>0))
    		{
    			String clonedUser = String.join(", ", userModel.getClonedUser().getLoginList());
    			row.put("clonedUser",MiscUtil.strDisplay(clonedUser));
    		}
    		result.add(row);
    	}
    	if(result.size()==0)
    		return null;
    	return result;
    }
    @GetMapping("/web/usersetting")
    public String webUserSetting(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_settings"), "usersetting"}
        });
        return "usersetting"; 
    }
    @PostMapping("/web/usersettingrefresh")
    public ResponseEntity<Map<String,Object>> webUserSettingRefresh(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	Login login=CacheUtil.threadlocallogin.get();
        if(login==null)
        {
        	result.put("status", utilLocal("no_access") );
        	return ResponseEntity.ok(result);        	
        }
        UserModel userModel=mongoDBService.findOneByField("id", login.getUserId(), UserModel.class);
        if(userModel==null)
        {
        	result.put("status", utilLocal("no_user") );
        	return ResponseEntity.ok(result);      
        }
        if(userModel.getUserStatus()==UserModel.UserStatus.DISABLED)
        {
        	result.put("status", utilLocal("user_disabled") ); 
        	return ResponseEntity.ok(result);      
        }
        if(userModel.getUserStatus()==UserModel.UserStatus.FREEZED)
        {
        	result.put("status", utilLocal("user_freezed"));
        	return ResponseEntity.ok(result);      
        }
    	Instant now=Instant.now();
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
		mongoDBService.log(null, null, null, paramsBody, "webUserSettingRefresh", null,null);
    	switch(action)
    	{
	    	case "userUpdate":
	    		//String fieldType = MiscUtil.webParamsGet(paramsBody, "fieldType", String.class, "");
	    		String fieldName = MiscUtil.webParamsGet(paramsBody, "fieldName", String.class, "");
	    		String fieldValue = MiscUtil.webParamsGet(paramsBody, "fieldValue", String.class, "");
	    		if(fieldName.equals("Password"))
	    		{
	    			fieldValue=fieldValue.trim();
	    			if(!utilUserValid(null,fieldValue,result))	    			
	    	        	return ResponseEntity.ok(result);   
	    			userModel.setPassword(MiscUtil.userPasswordEncode(fieldValue));
	    			userModel.setPasswordType(UserModel.PasswordType.CHANGED);
	    			userModel.setPassWordChangeTime(now);
	    			mongoDBService.save("usersetting2",userModel);
	    	    	result.put("alarmInfo", utilLocal("password_changed") );
	    		}else if(fieldName.equals("Login"))
	    		{
	    			fieldValue=fieldValue.trim();
	     			if(!utilUserValid(fieldValue,null,result))	    			
	    	        	return ResponseEntity.ok(result);   
	    			if(fieldValue.equals(userModel.getLogin()))
	    			{
	    	        	result.put("status", utilLocal("same_as_current") );
	    	        	return ResponseEntity.ok(result);   
	    			}
	    			UserModel userModel2=mongoDBService.findOneByField("login", fieldValue, UserModel.class);
	    			if(userModel2!=null)
	    			{
	    	        	result.put("status", utilLocal("same_as_others")  );
	    	        	return ResponseEntity.ok(result);   
	    			}
	    			userModel.setLogin(fieldValue);
	    			userModel.setLoginChangeTime(now);
	    			mongoDBService.save("userupdate",userModel);
	    	    	result.put("alarmInfo", utilLocal("login_changed") );
	    		}else if(fieldName.equals("License"))
	    		{
	    			if((fieldValue!=null)&&(fieldValue.length()>10))
	    			{
	    				String importRet=commService.i(fieldValue.trim());
	    				if(importRet==null)
	    				{
	        	        	result.put("status", utilLocal("license_import_error") );
		    	        	return ResponseEntity.ok(result);   
	    				}//else
	    				//	result.put("alarmInfo", utilLocal("license_import_ok") );
	    			}
	    		}
	    		break;
    	}
    	List<Map<String,Object>> userInfo = new ArrayList<Map<String,Object>>();
    	MiscUtil.resultPutRow(userInfo, "name",utilLocal("user_level2"),"value",userModel.getUserType());	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("login2"),"value",userModel.getLogin());	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("login_change_time2"),"value",MiscUtil.dateFormat(userModel.getLoginChangeTime(),"MM-dd HH:mm:ss"));	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("password2"),"value","********");	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("password_change_time2"),"value",MiscUtil.dateFormat(userModel.getPassWordChangeTime(),"MM-dd HH:mm:ss"));	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("password_changed2"),"value",userModel.getPasswordType());	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("last_login_ip2"),"value",userModel.getLatestIp());	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("login_times2"),"value",userModel.getLoginTimes());	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("last_login_time2"),"value",MiscUtil.dateFormat(userModel.getLoginTime(),"MM-dd HH:mm:ss"));	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("login_failed_times2"),"value",userModel.getLoginFailedTimes());	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("last_login_failed_time2"),"value",MiscUtil.dateFormat(userModel.getLoginFailedTime(),"MM-dd HH:mm:ss"));	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("latest_visit_time2"),"value",MiscUtil.dateFormat(userModel.getLatestVisitTime(),"MM-dd HH:mm:ss"));	
		MiscUtil.resultPutRow(userInfo, "name",utilLocal("register_time2"),"value",MiscUtil.dateFormat(userModel.getRegisterTime(),"MM-dd HH:mm:ss"));	
		if(userModel.getUserType()==UserModel.UserType.ADMIN) {
			MiscUtil.resultPutRow(userInfo, "name",utilLocal("license_info"),"value",MiscUtil.strDisplay(commService.l()));	
			MiscUtil.resultPutRow(userInfo, "name",utilLocal("license_info2"),"value",utilLocal("license_info2_guide"));	 
		}
		result.put("userInfo", userInfo);
        return ResponseEntity.ok(result);
    }
    private static final Map<String, Object[]> otaUpdatefieldsMeta = Map.of(	
            "deviceType",   new Object[]{"list-string",Arrays.stream(DeviceModel.DeviceType.values()).map(DeviceModel.DeviceType::name).collect(Collectors.toList())},
            "upgradeStrategy",   new Object[]{"list-string",Arrays.stream(OTARecordModel.OTAUpgradeStrategy.values()).map(OTARecordModel.OTAUpgradeStrategy::name).collect(Collectors.toList())},
            "upgradeScope",   new Object[]{"list-string",Arrays.stream(OTARecordModel.OTAUpgradeScope.values()).map(OTARecordModel.OTAUpgradeScope::name).collect(Collectors.toList())},
            "deviceFirmWareTargetVersion",   new Object[]{"pattern-string","^(\\d+(,\\d+)*)?$"}//comma separated
        ); 
    private String webOTARsp(DeviceModel device,String ret) 
    {
    	OTARecordModel otaRecord=projectBuildService.OTACheck(device);
    	if(otaRecord!=null)
    	{
    		Instant now=Instant.now();
			OTADownloadModel download=new OTADownloadModel();
			download.setCreateTime(now);
			download.setDeviceId(device.getId());
			download.setExpiredTime(now.plusSeconds(10*60));
			download.setOtaRecordId(otaRecord.getId());
			download.setUserId(device.getUserId());
			download.setToken(MiscUtil.tokenCreate());
			download.setMd5(otaRecord.getFirmWareMd5());
    		//safe_server_start|02|ota_server|ota_port|uri|ota_size|ota_md5_32|add|safe_server_end|kkxxx
			String temp="safe_server_start|02|"+configProperties.OTA_SERVER()+"|"+configProperties.OTA_PORT()+"|"+configProperties.OTA_URI()+"?token="+download.getToken()+"|"+otaRecord.getFirmWareSize()+"|"+otaRecord.getFirmWareMd5()+"|";
			String temp2=commService.getStr(temp);
			temp=temp+temp2+"|safe_server_end";
			ret=temp+"|"+ret;
			download.setRsp(ret);
	    	logger.info("webOTARsp rsp "+ret);
	    	mongoDBService.save("webOTARsp", download);
	    	
	    	return ret;
    	}
    	return null;
    }
 
    @GetMapping("/web/otarecord")
    public String webOTARecord(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String id = MiscUtil.webParamsGet(paramsReq, "id", String.class, "");
    	if(id.length()==0)
    		return "notfound";
        UserModel login = CacheUtil.threadlocallogin.get().getUserModel();
        OTARecordModel dataModel = mongoDBService.findOneByField("id", id, OTARecordModel.class);
        if (dataModel == null || !dataModel.getUserId().equals(login.getId())) {
        	return "accessdeny";
        }
        

    	model.addAttribute("id", dataModel.getId());
    	model.addAttribute("name", dataModel.getName());
    	model.addAttribute("deviceType", dataModel.getDeviceType());
    	model.addAttribute("deviceFirmWareTargetVersion", dataModel.getDeviceFirmWareTargetVersion());
    	model.addAttribute("deviceFirmWareVersion", dataModel.getDeviceFirmWareVersion());
    	model.addAttribute("firmWareName", dataModel.getFirmWareName());
    	model.addAttribute("firmWareOriginalName", dataModel.getFirmWareOriginalName());
    	model.addAttribute("firmWareId", dataModel.getFirmWareId());
    	model.addAttribute("firmWareMd5", dataModel.getFirmWareMd5());
    	model.addAttribute("firmWareSize", dataModel.getFirmWareSize());
    	model.addAttribute("downloadTimes", dataModel.getDownloadTimes());
    	model.addAttribute("upgradeStrategy", dataModel.getUpgradeStrategy());
    	model.addAttribute("upgradeScope", dataModel.getUpgradeScope());
    	model.addAttribute("deviceNoListInclude", dataModel.getDeviceNoListInclude()==null?"":String.join(" ", dataModel.getDeviceNoListInclude()));
    	model.addAttribute("deviceNoListExclude", dataModel.getDeviceNoListExclude()==null?"":String.join(" ", dataModel.getDeviceNoListExclude()));
    	model.addAttribute("comments", dataModel.getComments());
    	model.addAttribute("firmwareTime", MiscUtil.dateFormat(dataModel.getFirmwareTime(),"MM-dd HH:mm:ss"));
    	model.addAttribute("createTime", MiscUtil.dateFormat(dataModel.getCreateTime(),"MM-dd HH:mm:ss"));
    	model.addAttribute("modifyTime", MiscUtil.dateFormat(dataModel.getModifyTime(),"MM-dd HH:mm:ss"));
    	
        return "otarecord"; 
    }

    @RequestMapping(value = "/pub/otadownload", method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<byte[]> webOTADownload(@RequestParam Map<String, Object> paramsReq) {
        String token = MiscUtil.webParamsGet(paramsReq, "token", String.class, null);
        if ((token == null) || (token.length() == 0)) {
            return ResponseEntity.badRequest().body(null);
        }
        
        OTADownloadModel otaDownload = mongoDBService.findOneByField("token", token, OTADownloadModel.class);
        if (otaDownload == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
        OTARecordModel otaRecord = mongoDBService.findOneByField("id", otaDownload.getOtaRecordId(), OTARecordModel.class);
        if ((otaRecord == null) || (!otaRecord.getFirmWareMd5().equals(otaDownload.getMd5()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
        byte[] data = mongoDBService.fileReadByte(otaRecord.getFirmWareId());
        if (data == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        
        try {
            String fileName = otaRecord.getFirmWareName();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
            headers.setContentLength(data.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
   
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @PostMapping("/web/otaupload")
    public ResponseEntity<Map<String, Object>> webOTAUpload(@RequestParam MultipartFile file, @RequestParam Map<String, Object> paramsReq) {
        Map<String, Object> result = new HashMap<>();
        String id = MiscUtil.webParamsGet(paramsReq, "id", String.class, null);
        if (id == null) {
            result.put("status", utilLocal("wrong_id") );
            return ResponseEntity.ok(result);
        }
        if (file.isEmpty()) {
            result.put("status", utilLocal("file_is_empty") );
            return ResponseEntity.ok(result);
        }

        Instant now = Instant.now();
        UserModel login = CacheUtil.threadlocallogin.get().getUserModel();
    	if(login.getParentId()!=null)
    		return ResponseEntity.ok(result);
        OTARecordModel model = mongoDBService.findOneByField("id", id, OTARecordModel.class);
        if (model == null || !model.getUserId().equals(login.getId())) {
            result.put("status", utilLocal("no_priv") );
            return ResponseEntity.ok(result);
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileData = inputStream.readAllBytes();  
            ProjectBuildService.EspAppDesc espDesc=ProjectBuildService.EspAppDesc.parseAppDesc(fileData);
            if((espDesc==null)||(espDesc.getVersion()==null))
            {
                result.put("status", utilLocal("wrong_firmware") );
                return ResponseEntity.ok(result);
            }
            String fileMd5 = MiscUtil.MD5(fileData, null);
            String firmWareName = login.getId() + "-" + MiscUtil.dateFormat(Instant.now(), "yyyyMMddHHmmss") + ".bin";
            String firmWareId = mongoDBService.fileSave("application/octet-stream", firmWareName, fileData);
            if (model.getFirmWareId() != null && model.getFirmWareId().length() > 0) {
                mongoDBService.fileDelById(model.getFirmWareId());
                model.setFirmWareMd5(null);
                model.setFirmWareSize(null);
                model.setFirmwareTime(null);
                model.setFirmWareName(null);
                model.setFirmWareOriginalName(null);
                model.setDeviceFirmWareVersion(null);
            }
            if((espDesc!=null)&&(espDesc.getVersion()!=null))
            	model.setDeviceFirmWareVersion(espDesc.getVersion());
            model.setFirmWareName(firmWareName);
            model.setFirmWareOriginalName(file.getOriginalFilename());
            model.setFirmWareId(firmWareId);
            model.setFirmWareMd5(fileMd5);
            model.setFirmWareSize(fileData.length);
            model.setFirmwareTime(now);
            mongoDBService.save("webOTAUpload", model);

            result.put("status", utilLocal("upload_successfully")  );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("status", utilLocal("upload_failed") );
            return ResponseEntity.ok(result);
        }
    }


    @GetMapping("/web/otalist")
    public String webOTAList(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_ota"), "otalist"}
        });
        return "otalist"; 
    }

    @PostMapping("/web/otalistrefresh")
    public ResponseEntity<Map<String,Object>> webOTAListRefresh(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	Instant now=Instant.now();
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
    	UserModel login=CacheUtil.threadlocallogin.get().getUserModel();
    	result.put("status", "ok");
    	if(login.getParentId()!=null)
    		return ResponseEntity.ok(result);
    	OTARecordModel recordModel=null;
		if(action.equals("otaDeviceList")||action.equals("otaDel")||action.equals("otaUpdate"))
		{
			String id = MiscUtil.webParamsGet(paramsBody, "id", String.class, "");
			if(id.length()<5)
			{
				result.put("alarmInfo", utilLocal("id_empty")  );
				return ResponseEntity.ok(result);
			}
			recordModel=mongoDBService.findOneByField("id", id, OTARecordModel.class);
			if((recordModel==null)||(!recordModel.getUserId().equals(login.getId())))
			{
				result.put("alarmInfo", utilLocal("no_priv") );
				return ResponseEntity.ok(result);
			}
		}
    	switch(action)
    	{
	    	case "otaDeviceList":
	    		if(!webOTAAddDeviceList(paramsBody,result,recordModel))
	    			return ResponseEntity.ok(result);
	    		break;
	    	case "otaDel":
	    		if(!webOTAListDel(paramsBody,result,recordModel))
	    			return ResponseEntity.ok(result);
	    		break;
	    	case "otaUpdate":
	    		if(!webOTAListUpdate(paramsBody,result,now,recordModel))
	    			return ResponseEntity.ok(result);
	    		break;
	    	case "otaAdd":
	    		String name = MiscUtil.webParamsGet(paramsBody, "name", String.class, "");
	    		webOTAListAdd(name,"Manual upload",result);
	    		break;
    	}
    	List<Map<String,Object>>  otaList=webOTAListAll(login);
    	if((otaList!=null)&&(otaList.size()>0)) {
    		result.put("otaList", otaList);
    		result.put("otaListMeta", otaUpdatefieldsMeta);
    	}
        return ResponseEntity.ok(result);
    }
    private boolean webOTAAddDeviceList(Map<String, Object> paramsBody,Map<String,Object> result,OTARecordModel recordModel)
    {
		String deviceNoList = MiscUtil.webParamsGet(paramsBody, "deviceNoList", String.class, "").trim();
		String listType = MiscUtil.webParamsGet(paramsBody, "listType", String.class, "");
		String[] devices = deviceNoList.split("\\s+");
		List<String> newList=new ArrayList<>();
		for(String device:devices)
		{
			if(device.length()>0)
				newList.add(device);
		}
		List<String> tempList=listType.equals("include")?recordModel.getDeviceNoListInclude():recordModel.getDeviceNoListExclude();
		if(newList.size()==0)
		{
			if((tempList!=null)&&(tempList.size()>0))
				result.put("alarmInfo", utilLocal("no_valid_input_old_data_cleared") );
			else {
				result.put("alarmInfo", utilLocal("no_valid_input") );
				return false;
			}
		}else {
			if((tempList!=null)&&(tempList.size()>0))
				result.put("alarmInfo", utilLocal("replace_old_list_successfully") );
			else
				result.put("alarmInfo", utilLocal("add_device_list_successfully") );
		}
		if(listType.equals("include"))
			recordModel.setDeviceNoListInclude(newList);
		else
			recordModel.setDeviceNoListExclude(newList);
		mongoDBService.save("webOTAAddInclude", recordModel);
		return true;
    }
    private boolean webOTAListDel(Map<String, Object> paramsBody,Map<String,Object> result,OTARecordModel model)
    {
        if (model.getFirmWareId() != null && model.getFirmWareId().length() > 0) 
            mongoDBService.fileDelById(model.getFirmWareId());
		mongoDBService.delete("id",model.getId(), OTARecordModel.class);
		return true;
    }
    private boolean webOTAListUpdate(Map<String, Object> paramsBody,Map<String,Object> result,Instant now,OTARecordModel model)
    {
    	String fieldName = MiscUtil.webParamsGet(paramsBody, "fieldName", String.class, "");
    	Object fieldValue=paramsUpdateValid(otaUpdatefieldsMeta, paramsBody,result);
		if(fieldValue==null)
			return false;     
		mongoDBService.updateById(OTARecordModel.class,model.getId(), fieldName, fieldValue,"modifyTime",now);
		return true;
    }
    private OTARecordModel webOTAListAdd(String otaName,String comments,Map<String,Object> result)
    {
		String name = otaName;
		if(name.length()<2)
		{
			result.put("alarmInfo", utilLocal("at_least_characters_for_name") );
			return null;
		}
		UserModel userModel=CacheUtil.threadlocallogin.get().getUserModel();
		String userId=userModel.getId();
		Long otaNum=mongoDBService.count(OTARecordModel.class,"userId",userId);
		if((otaNum!=null)&&(otaNum>=userModel.getOtaLimit()))
		{
			result.put("alarmInfo", utilLocal("too_many_ota_records")  );
			return null;
		}
		Instant now=Instant.now();
		OTARecordModel recordModel=new OTARecordModel();
		recordModel.setCreateTime(now);
		recordModel.setModifyTime(now);
		recordModel.setName(name);
		recordModel.setDeviceFirmWareVersion(null);
		recordModel.setDownloadTimes(0);
		recordModel.setComments(comments);
		recordModel.setDeviceType(DeviceModel.DeviceType.getById(1));
		recordModel.setUpgradeScope(OTARecordModel.OTAUpgradeScope.NONE);
		recordModel.setUpgradeStrategy(OTARecordModel.OTAUpgradeStrategy.IGNORE_SAME);
		recordModel.setUserId(userId);
		mongoDBService.save("otaAdd", recordModel);
		return recordModel;
    }
    private List<Map<String,Object>> webOTAListAll(UserModel userModel)
    {
    	
    	List<OTARecordModel> tempModelList=mongoDBService.findByField(new Query().addCriteria(Criteria.where("userId").is(userModel.getId())), "createTime",null, true,10,OTARecordModel.class); 
    	if((tempModelList!=null)&&(tempModelList.size()>0))
    	{
    		List<Map<String,Object>> result=new ArrayList<>();
        	for(OTARecordModel tempModel:tempModelList)
        	{
        		Map<String,Object> row=new HashMap<String,Object>();
        		row.put("id",tempModel.getId());
        		row.put("name",tempModel.getName());
        		row.put("deviceType",tempModel.getDeviceType());
        		row.put("deviceFirmWareTargetVersion",tempModel.getDeviceFirmWareTargetVersion());
        		row.put("deviceFirmWareVersion",tempModel.getDeviceFirmWareVersion());
        		row.put("firmWareMd5",tempModel.getFirmWareMd5());
        		row.put("firmWareSize",tempModel.getFirmWareSize());
        		row.put("downloadTimes",tempModel.getDownloadTimes());
        		row.put("upgradeStrategy",tempModel.getUpgradeStrategy());
        		row.put("includeNum",tempModel.getDeviceNoListInclude()==null?0:tempModel.getDeviceNoListInclude().size());
        		row.put("excludeNum",tempModel.getDeviceNoListExclude()==null?0:tempModel.getDeviceNoListExclude().size());
        		row.put("upgradeScope",tempModel.getUpgradeScope());
        		row.put("createTime",MiscUtil.dateFormat(tempModel.getCreateTime(),"MM-dd HH:mm:ss"));
        		row.put("modifyTime",MiscUtil.dateFormat(tempModel.getModifyTime(),"MM-dd HH:mm:ss"));
        		result.add(row);
        	}
        	if(result.size()>0)
        		return result;
    	}
    	return null;
    }
    private boolean webCodeProjectFilePrivi(ProjectAccessModel projectAccessModel, String filePath, boolean readOnly) 
    {
        if (projectAccessModel == null || filePath == null || filePath.isEmpty()) {
            return false;
        }
        ProjectAccessModel.ProjectAccessType accessType = projectAccessModel.getAccessType();
        if (accessType == ProjectAccessModel.ProjectAccessType.LIMIT_READ || accessType == ProjectAccessModel.ProjectAccessType.LIMIT_WRITE) {
            Set<String> allowedPaths = projectAccessModel.getFilters();
            if (allowedPaths != null && allowedPaths.contains("/"+filePath)) {
                return false;
            }
            if (accessType == ProjectAccessModel.ProjectAccessType.LIMIT_READ && !readOnly) {
                return false; 
            }
            return true;
        }
        if (readOnly) {
            if (accessType == ProjectAccessModel.ProjectAccessType.ALL_READ || 
                accessType == ProjectAccessModel.ProjectAccessType.ALL_WRITE) {
                return true;
            }
        }
        if (!readOnly) {
            if (accessType == ProjectAccessModel.ProjectAccessType.ALL_WRITE) {
                return true;
            }
        }
        return false;
    }
    private DataCacheModel dataCacheSave(String key,Object value,int expiredSeconds)
    {
    	Instant now=Instant.now();
    	DataCacheModel dataCacheModel=new DataCacheModel();
		dataCacheModel.setCreateTime(now);
		dataCacheModel.setExpiredTime(now.plusSeconds(expiredSeconds));
		dataCacheModel.setKey(key);
		dataCacheModel.setValue(value);
		mongoDBService.save("dataCacheSave", dataCacheModel);
		return dataCacheModel;
    }
    private DataCacheModel dataCacheRead(String key)
    {
    	return mongoDBService.findOneByField("key", key, DataCacheModel.class);
    }
    @GetMapping("/web/codelist")
    public String webCodeList(Model model) {
      	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_project"), "codelist"}
        });
        return "webcodelist"; 
    }
    @PostMapping("/web/codelistrefresh")
    public ResponseEntity<Map<String,Object>> webCodeListRefresh(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
    	UserModel userModel=CacheUtil.threadlocallogin.get().getUserModel();
    	
    	//must have a device
		List<DeviceModel> deviceModelList=mongoDBService.findByField("userId",userModel.getId(), DeviceModel.class);
		if((deviceModelList==null)||(deviceModelList.size()==0))
		{
			result.put("alarmInfo",utilLocal("no_bind_device")); 
			return ResponseEntity.ok(result);
		}

    	switch(action)
    	{
	    	case "test":
	    		break;		
    	}
    	//read all projects
    	List<ProjectInfoModel> projectInfoModelList=mongoDBService.findByField("status", ProjectInfoModel.ProjectStatus.NORMAL, ProjectInfoModel.class);
     	//read lastest build for each project
     	Criteria queryCriteria = Criteria.where("userId").is(userModel.getId());
    	Sort sort = Sort.by(Sort.Order.desc("updateTime"));
    	GroupOperation group = Aggregation.group("projectId").first("id").as("id").first("projectId").as("projectId").first("updateTime").as("updateTime").first("buildStartTime").as("buildStartTime").first("buildEndTime").as("buildEndTime").first("binVersion").as("binVersion").first("resultType").as("resultType");
    	ProjectionOperation projectOperation = Aggregation.project("id", "projectId","updateTime", "buildStartTime", "buildEndTime", "binVersion", "resultType");
    	List<ProjectBuildModel> buildList = mongoDBService.aggregatedDataQuery(queryCriteria, sort, group, projectOperation, ProjectBuildModel.class);
    	Map<String, ProjectBuildModel> buildMap= ((buildList!=null)&&(buildList.size()>0))?buildList.stream().collect(Collectors.toMap(ProjectBuildModel::getProjectId, project -> project)):new HashMap<>();
    	//read latest modified fiel for each project
    	queryCriteria = Criteria.where("userId").is(userModel.getId());
    	sort = Sort.by(Sort.Order.desc("modifyTime"));
    	group = Aggregation.group("projectId").first("id").as("id").first("projectId").as("projectId").first("modifyTime").as("modifyTime").first("path").as("path");
    	projectOperation = Aggregation.project("id", "projectId","modifyTime", "path");
    	List<ProjectFileModel> fileList = mongoDBService.aggregatedDataQuery(queryCriteria, sort, group, projectOperation, ProjectFileModel.class);
    	Map<String, ProjectFileModel> fileMap= ((fileList!=null)&&(fileList.size()>0))?fileList.stream().collect(Collectors.toMap(ProjectFileModel::getProjectId, project -> project)):new HashMap<>();

 		List<Map<String,Object>> projectResult=new ArrayList<Map<String,Object>>();
		for(ProjectInfoModel infoModel:projectInfoModelList)
		{
			Map<String,Object> row=new HashMap<String,Object>();
			row.put("id",infoModel.getId());
			ProjectBuildModel buildModel=buildMap.get(infoModel.getId());
			ProjectFileModel fileModel=fileMap.get(infoModel.getId());
			row.put("name",infoModel.getName());
			row.put("access",userModel.getProjectAccessType().name());
			if(buildModel!=null)
			{
				row.put("buildUpdateTime",MiscUtil.dateFormat(buildModel.getUpdateTime(),"MM-dd HH:mm:ss"));
				row.put("buildStartTime",MiscUtil.dateFormat(buildModel.getBuildStartTime(),"MM-dd HH:mm:ss"));
				row.put("buildEndTime",MiscUtil.dateFormat(buildModel.getBuildEndTime(),"MM-dd HH:mm:ss"));
				row.put("buildBinVersion",buildModel.getBinVersion());
				row.put("buildResultType",buildModel.getResultType());
			}
			if(fileModel!=null)
			{
				row.put("filePath",fileModel.getPath());
				row.put("fileModifyTime",MiscUtil.dateFormat(fileModel.getModifyTime(),"MM-dd HH:mm:ss"));
			}
			projectResult.add(row);
		}
		result.put("codeslist", projectResult);
        return ResponseEntity.ok(result);
    }
    @GetMapping("/web/codelog")
    public String webCodeLog(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String projectId = MiscUtil.webParamsGet(paramsReq, "projectId", String.class, null);   	
    	model.addAttribute("projectId", projectId);
        return "webcodelog"; 
    }
    @PostMapping("/web/codelogrefresh")
    public ResponseEntity<Map<String,Object>> webCodeLogRefresh(@RequestBody Map<String, Object> paramsBody) {
     	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	String projectId = MiscUtil.webParamsGet(paramsBody, "projectId", String.class, null);   	
        if(projectId==null)
        	return ResponseEntity.ok(result);
        ProjectInfoModel projectInfoModel=mongoDBService.findOneByField("id", projectId, ProjectInfoModel.class);
        if(projectInfoModel==null)
        	return ResponseEntity.ok(result);
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");   	
    	UserModel userModel=CacheUtil.threadlocallogin.get().getUserModel();
    	if(userModel.getParentId()!=null)
    		return ResponseEntity.ok(result);
		Criteria queryCriteria = Criteria.where("userId").is(userModel.getId()).and("projectId").is(projectId);
		List<ProjectBuildModel> buildList=mongoDBService.findByFields(queryCriteria, "createTime", false, 0,20, Arrays.asList("logs"),ProjectBuildModel.class); 
		Map<String, ProjectBuildModel> buildMap=buildList==null?new HashMap<>():buildList.stream().collect(Collectors.toMap(ProjectBuildModel::getId, build -> build));
	   	
		String buildId = MiscUtil.webParamsGet(paramsBody, "buildId", String.class, null);	   	
		if((buildId!=null)&&(!buildMap.containsKey(buildId)))
			return ResponseEntity.ok(result);
    	if(((buildId==null)||(buildId.length()==0))&&(buildList!=null)&&(buildList.size()>0))
    		buildId=buildList.get(0).getId();
		ProjectBuildModel projectBuildModel=buildMap.get(buildId);
		
    	switch(action)
    	{
    		case "saveBuild":
	    		if((buildId==null)||(buildId.length()==0))
	    			break;
    			if((projectBuildModel.getResultType()!=ProjectBuildModel.ProjectBuildResultType.SUCCESS)||(projectBuildModel.getBinFile()==null))
    			{
    				result.put("alarmInfo", utilLocal("not_successful_build") );
    				break;
    			}
	    		String otaName = MiscUtil.webParamsGet(paramsBody, "otaName", String.class, "");   	
	    		String comments="project "+projectInfoModel.getName()+" code version "+projectBuildModel.getCodeVersion()+ " bin version "+projectBuildModel.getBinVersion();
	    		OTARecordModel otaRecordModel=webOTAListAdd(otaName,comments,result);
	    		if(otaRecordModel==null)
	    			break;
	    		byte[] binContent=mongoDBService.fileReadByte(projectBuildModel.getBinFile());
	    		if(binContent==null)
	    		{
	        		result.put("alarmInfo",  utilLocal("ota_bin_removed") );
	        		break;
	    		}
	    		String binMd5 = MiscUtil.MD5(binContent, null);
	    		String newFileId=mongoDBService.fileSave("application/octet-stream", projectBuildModel.getBinFile(), binContent);
	    		otaRecordModel.setFirmWareId(newFileId);
	    		otaRecordModel.setFirmWareMd5(binMd5);
	    		otaRecordModel.setFirmWareName(projectInfoModel.getBinName()+".bin");
	    		otaRecordModel.setFirmWareOriginalName("online_build");
	    		otaRecordModel.setFirmWareSize(binContent.length);
	    		otaRecordModel.setFirmwareTime(projectBuildModel.getBuildEndTime());
	    		otaRecordModel.setDeviceFirmWareVersion(projectBuildModel.getBinVersion());
	    		mongoDBService.save("saveBuild", otaRecordModel);
	    		result.put("alarmInfo",  utilLocal("ota_saved") );
    			break;
	    	case "delBuild":
	    		if((buildId==null)||(buildId.length()==0))
	    			break;
	    		if(projectBuildModel.getResultType()==ProjectBuildModel.ProjectBuildResultType.BUILDING)
	    		{
		    		result.put("alarmInfo", utilLocal("in_building") );
		    		break;
	    		}
	    		mongoDBService.delete("id", buildId, ProjectBuildModel.class);
    			projectBuildService.clearBuildFiles(projectBuildModel);
	    		buildMap.remove(buildId);
	    		if(buildMap.size()>0) {
	    			buildList=new ArrayList<>(buildMap.values());
	    			buildList.sort(Comparator.comparing(ProjectBuildModel::getCreateTime).reversed());
	    			buildId=buildList.get(0).getId();
	    		}else {
	    			buildId=null;
	    			buildList=null;
	    		}
	    		break;
    	}
    	
    	result.put("projectId", projectId);
    	result.put("projectName", projectInfoModel.getName());
    	
    	webCodeLogDisplay(paramsBody, result, buildList, buildId);
    	
		return ResponseEntity.ok(result);
    }

	private void webCodeLogDisplay(Map<String, Object> paramsBody, Map<String, Object> result,List<ProjectBuildModel> buildList, String buildId) {
		Criteria queryCriteria;
		if((buildList!=null)&&(buildList.size()>0))
    	{
    		List<Map<String,String>> builds = new ArrayList<>();
    		Long pendingNum=null;
        	for(ProjectBuildModel build:buildList)
        	{
        		Map<String,String> row=new HashMap<>();
        		row.put("id",build.getId());
        		row.put("codeVersion",build.getCodeVersion()+"");
        		row.put("time",MiscUtil.dateFormat(build.getCreateTime(),"MM-dd HH:mm:ss"));
        		row.put("status",build.getResultType()+"");
        		if(build.getResultType().equals(ProjectBuildModel.ProjectBuildResultType.PENDING)&&(pendingNum==null))
        		{
        			queryCriteria = Criteria.where("resultType").is(ProjectBuildModel.ProjectBuildResultType.PENDING);
        			pendingNum=mongoDBService.count(queryCriteria, ProjectBuildModel.class);
        		}
        		if(build.getResultType().equals(ProjectBuildModel.ProjectBuildResultType.PENDING))
        			row.put("pendingNum",pendingNum+"");  			
        		builds.add(row);
        	}
        	result.put("builds", builds);
    	}
    	if((buildId!=null)&&(buildId.length()>0))
    	{
    		ProjectBuildModel projectBuildModel=mongoDBService.findOneByField("id", buildId, ProjectBuildModel.class);
    		if(projectBuildModel!=null)
    		{
    			result.put("buildId", projectBuildModel.getId());
    			List<Map<String,String>> formattedLogs = new ArrayList<>();
    	        if((projectBuildModel.getLogs()!=null)&&(projectBuildModel.getLogs().size()>0))
    	        {
    	        	List<ProjectBuildModel.BuildLogModel> logs=projectBuildModel.getLogs();
    	        	int numLogs=logs.size();
    	        	Integer topNum = MiscUtil.webParamsGet(paramsBody, "topNum", Integer.class, 100000);    	        	
    	        	topNum=topNum>=numLogs?0:(numLogs-topNum);
    	        	for(int i=numLogs;i>topNum;i--)
    	        	{
    	        		ProjectBuildModel.BuildLogModel log=logs.get(i-1);
    	        		Map<String,String> row=new HashMap<>();
    	        		row.put("time",MiscUtil.dateFormat(log.getCreateTime(),"yyyy-MM-dd HH:mm:ss.SSS"));
    	        		row.put("content",log.getContent());
    	        		formattedLogs.add(row);
    	        	}
    	        	result.put("logs", formattedLogs);
    	        }
    		}
    	}
	}
    @GetMapping("/web/code")
    public String webCode(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String projectId = MiscUtil.webParamsGet(paramsReq, "projectId", String.class, "");
        ProjectInfoModel projectInfoModel=mongoDBService.findOneByField("id", projectId, ProjectInfoModel.class);
        if(projectInfoModel==null)
        	return null;
    	String filePath = "main/app_main.c";
    	model.addAttribute("filePath", filePath);
    	model.addAttribute("projectId", projectId);
    	UserModel userModel=CacheUtil.threadlocallogin.get().getUserModel();
    	ProjectAccessModel.ProjectAccessType accessType= userModel.getProjectAccessType();
		Criteria queryCriteria = Criteria.where("projectId").is(projectId).and("accessType").is(accessType);
		ProjectAccessModel projectAccessModel=mongoDBService.findOneByFields(queryCriteria,null,null, ProjectAccessModel.class);
		if(projectAccessModel==null)
			return null;
    	try {
    		String key=projectInfoModel.getId()+"-"+projectAccessModel.getId();
    		DataCacheModel dataCacheModel=dataCacheRead(key);
    		if(dataCacheModel==null)
    		{
    			Map<String, Object> result=CodeEditorUtil.readDirectoryStructure(projectInfoModel,projectAccessModel);
    			String resultStr=MiscUtil.jsonObject2String(result);
    			dataCacheModel=dataCacheSave(key,resultStr,3600);
    		}
    		model.addAttribute("projectFolder",dataCacheModel.getValue());
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
        return "webcode"; 
    }
    @PostMapping("/web/coderefresh")
    public ResponseEntity<Map<String,Object>> webCodeRefresh(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	result.put("status", "ok");
    	UserModel userModel=CacheUtil.threadlocallogin.get().getUserModel();
		List<DeviceModel> deviceModelList=mongoDBService.findByField("userId",userModel.getId(), DeviceModel.class);
		if((deviceModelList==null)||(deviceModelList.size()==0))
		{
			result.put("alarmInfo",utilLocal("no_bind_device")); 
			return ResponseEntity.ok(result);
		}
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "viewFile");
    	String filePath = MiscUtil.webParamsGet(paramsBody, "filePath", String.class, "");
    	String fileContent = MiscUtil.webParamsGet(paramsBody, "fileContent", String.class, "");
    	String projectId = MiscUtil.webParamsGet(paramsBody, "projectId", String.class, "");
    	Instant now = Instant.now();
    	//if(userModel.getParentId()!=null)
    	//	return ResponseEntity.ok(result);
    	if(projectId.length()==0)
    		return null;
        ProjectInfoModel projectInfoModel=mongoDBService.findOneByField("id", projectId, ProjectInfoModel.class);
        if(projectInfoModel==null)
        	return null;
    	ProjectAccessModel.ProjectAccessType accessType= userModel.getProjectAccessType();
		Criteria queryCriteria = Criteria.where("projectId").is(projectId).and("accessType").is(accessType);
		ProjectAccessModel projectAccessModel=mongoDBService.findOneByFields(queryCriteria, null,null,ProjectAccessModel.class);
		if(projectAccessModel==null)
			return null;
    	switch(action)
    	{
	    	case "viewFile":
	    		fileContent=webCodeFileRead(userModel,projectInfoModel,projectAccessModel,filePath);
	    		result.put("fileContent", fileContent);
	    		break;
	    	case "saveFile":
	    		if(!webCodeFileSave(result,now,userModel,projectInfoModel,projectAccessModel,filePath,fileContent))
	    			return ResponseEntity.ok(result);
	    		break;
	    	case "buildSubmit":
	    		if(!webCodeBuildSubmit(result,now,userModel,projectInfoModel))
	    			return ResponseEntity.ok(result);
	    		break;		
    	}
    	result.put("status", "ok");
        return ResponseEntity.ok(result);
    }
    private boolean webCodeBuildSubmit(Map<String,Object> result,Instant now,UserModel userModel,ProjectInfoModel projectInfoModel)
    {
    	boolean ret=false;
    	if(MiscUtil.isTimeNotCurrentDay(userModel.getBuildProjectTime()))
    	{
    		userModel.setBuildProjectTimesPerDay(0);
    	}
    	if(userModel.getBuildProjectTimesPerDay()>=userModel.getBuildProjectLimitPerDay())
    	{
    		result.put("status", utilLocal("build_times_exceeds_quotation")+" "+userModel.getBuildProjectLimitPerDay());
    		return ret;
    	}
		Criteria queryCriteria = Criteria.where("userId").is(userModel.getId()).and("projectId").is(projectInfoModel.getId()).and("resultType").in(ProjectBuildModel.ProjectBuildResultType.PENDING, ProjectBuildModel.ProjectBuildResultType.BUILDING);
		ProjectBuildModel buildModel=mongoDBService.findOneByFields(queryCriteria, null,null,ProjectBuildModel.class);
    	if(buildModel==null)
    	{
    		Long version=webCodeFileVersionUpgrade(userModel.getId(),projectInfoModel);
			buildModel=new ProjectBuildModel();
			buildModel.setCodeVersion(version);
			buildModel.setCreateTime(now);
			buildModel.setUpdateTime(now);
			buildModel.setProjectId(projectInfoModel.getId());
			buildModel.setResultType(ProjectBuildModel.ProjectBuildResultType.PENDING);
			buildModel.setUserId(userModel.getId());
			mongoDBService.save("buildSubmit", buildModel);
			userModel.setBuildProjectTime(now);
			userModel.setBuildProjectTimesPerDay(userModel.getBuildProjectTimesPerDay()+1);
			mongoDBService.save("buildSubmit", userModel);
			ret=true;
    	}else {
    		result.put("status", utilLocal("pending_build_exist") );
    	}
    	projectBuildService.build();
    	return ret;
    }
    private String webCodeFileRead(UserModel userModel,ProjectInfoModel projectInfoModel,ProjectAccessModel projectAccessModel,String filePath)
    {
		if(!webCodeProjectFilePrivi(projectAccessModel,filePath,true))
	    	return null;
        try {
    		Criteria queryCriteria = Criteria.where("userId").is(userModel.getId()).and("projectId").is(projectInfoModel.getId()).and("path").is(filePath);    		
    		ProjectFileModel fileModel=mongoDBService.findOneByFields(queryCriteria, null,null,ProjectFileModel.class);
    		if(fileModel!=null) 
    			return fileModel.getContent();
			return new String(Files.readAllBytes(Paths.get(projectInfoModel.getPath()+filePath)));
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
        return null;
    }
    private boolean webCodeFileSave(Map<String,Object> result,Instant now,UserModel userModel,ProjectInfoModel projectInfoModel,ProjectAccessModel projectAccessModel,String filePath,String fileContent)
    {
		if(fileContent.length()>10*1024*1024)
		{
	    	result.put("status", utilLocal("file_too_big") );
	    	return false;
		}
		if(!webCodeProjectFilePrivi(projectAccessModel,filePath,false))
		{
	    	result.put("status", utilLocal("no_access_file")  );
	    	return false;
		}
		Criteria queryCriteria = Criteria.where("userId").is(userModel.getId()).and("projectId").is(projectInfoModel.getId()).and("path").is(filePath);
		ProjectFileModel fileModel=mongoDBService.findOneByFields(queryCriteria, null,null,ProjectFileModel.class);
		if(fileModel==null) 
		{
			fileModel=new ProjectFileModel();
			fileModel.setCreateTime(now);
			fileModel.setPath(filePath);
			fileModel.setProjectId(projectInfoModel.getId());
			fileModel.setUserId(userModel.getId());
		}
		fileModel.setContent(fileContent);
		fileModel.setSize(fileContent.length());
		fileModel.setModifyTime(now);
		mongoDBService.save("saveFile", fileModel);
		return true;
    }
    private Long webCodeFileVersionUpgrade(String userId,ProjectInfoModel projectInfoModel)
    {
    	String filePath="version.txt";
    	Instant now=Instant.now();
        try 
        {
        	String fileContent=null;
    		Criteria queryCriteria = Criteria.where("userId").is(userId).and("projectId").is(projectInfoModel.getId()).and("path").is(filePath);    		
    		ProjectFileModel fileModel=mongoDBService.findOneByFields(queryCriteria, null,null,ProjectFileModel.class);
    		if(fileModel!=null) 
    			fileContent=fileModel.getContent();
    		else
    			fileContent=new String(Files.readAllBytes(Paths.get(projectInfoModel.getPath()+filePath)));
        	Long version=MiscUtil.strParseLong(fileContent);
        	if(version!=null) 
        	{
        		version=version+1L;
        		fileContent=version+"";
        		if(fileModel==null) 
        		{
        			fileModel=new ProjectFileModel();
        			fileModel.setCreateTime(now);
        			fileModel.setPath(filePath);
        			fileModel.setProjectId(projectInfoModel.getId());
        			fileModel.setUserId(userId);
        		}
        		fileModel.setContent(fileContent);
        		fileModel.setSize(fileContent.length());
        		fileModel.setModifyTime(now);
        		mongoDBService.save("saveFile", fileModel);
        		return version;
        	}
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
        return 0L;
    }
    
    @GetMapping("/web/dashboard")
    public String webDashboard(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_devices"), "devicelist"}
        });
        return "devicelist"; 
    }
    private static final Map<String, Object[]> deviceUpdatefieldsMeta = Map.of(
            "deviceName",   new Object[]{"free-string",new Integer[] {1,20}}
        ); 
    @GetMapping("/web/devicelist")
    public String webDeviceList(Model model) {
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_devices"), "devicelist"}
        });
        return "devicelist"; 
    }
    private void webDeviceListDel(DeviceModel deviceModel)
    {
    	mongoDBService.retainFields(deviceModel, Arrays.asList("id", "deviceNo", "deviceToken"));
    }
    private boolean webDeviceListAdd(Map<String, Object> paramsBody,Map<String,Object> result)
    {
    	Login login=CacheUtil.threadlocallogin.get();
    	if(login==null)
    	{
    		result.put("status",  utilLocal("not_login"));
    		return false;
    	}
    	String userId=login.getUserId();
    	String deviceNo = MiscUtil.webParamsGet(paramsBody, "deviceNo", String.class, "");
    	String deviceToken = MiscUtil.webParamsGet(paramsBody, "deviceToken", String.class, "");
    	if((deviceNo.length()==0)||(deviceToken.length()==0))
    	{
    		result.put("status", utilLocal("lack_of_input") );
    		return false;
    	}
    	DeviceModel deviceModel=mongoDBService.findOneByField("deviceNo", deviceNo, DeviceModel.class);
    	if((deviceModel==null)||(deviceModel.getDeviceToken()==null)||(!deviceModel.getDeviceToken().equals(deviceToken)))
    	{
    		result.put("status", utilLocal("invalid_input") );
    		return false;
    	}
    	if((deviceModel.getDeviceTradeStatus()!=null)||(deviceModel.getDeviceTradeStatus()==DeviceModel.DeviceTradeStatus.CUSTOMER))
    	{
    		if(deviceModel.getUserId().equals(userId))
    			result.put("status", utilLocal("already_in_your_account") );
    		else
    			result.put("status", utilLocal("used_by_others") );
    		return false;
    	}
		deviceModel.setForwardStatus(false);
    	deviceModel.setDeviceStatus(DeviceModel.DeviceStatus.ENABLED);
    	deviceModel.setUserId(userId);
    	deviceModel.setModifyTime(Instant.now());
    	deviceModel.setDeviceTradeStatus(DeviceModel.DeviceTradeStatus.CUSTOMER);
    	mongoDBService.save("deviceadd",deviceModel);
    	return true;
    }

    @PostMapping("/web/devicelistrefresh")
    public ResponseEntity<Map<String,Object>> webDeviceListRefresh(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "list");
    	String deviceId = MiscUtil.webParamsGet(paramsBody, "deviceId", String.class, "");
    	Object[] devicePriv=utilHasPriv(deviceId);
		if((action.equals("deviceUpdate")||action.equals("deviceDel"))&&(((Boolean)devicePriv[0]==false)||(((String)devicePriv[1]).equals("read"))))
		{
			result.put("status", utilLocal("no_priv"));
			return ResponseEntity.ok(result);
		}
    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
		mongoDBService.log(null, deviceModel==null?null:deviceModel.getId(), null, paramsBody, "webDeviceListRefresh", null,null);
    	switch(action)
    	{
	    	case "deviceUpdate":
	        	String fieldName = MiscUtil.webParamsGet(paramsBody, "fieldName", String.class, "");
	        	Object fieldValue=paramsUpdateValid(deviceUpdatefieldsMeta,  paramsBody,result);
	    		if(fieldValue==null)
	    			return ResponseEntity.ok(result);     
	    		mongoDBService.updateById(DeviceModel.class,deviceId, fieldName, fieldValue);
	    		break;
	    	case "deviceDel":
	    		webDeviceListDel(deviceModel);
	    		break;
	    	case "deviceAdd":
	    		if(!webDeviceListAdd(paramsBody,result))
	    			return ResponseEntity.ok(result);
	    		break;
    	}
    	//List<DeviceModel> deviceList=mongoDBService.findByField("userId", CacheUtil.threadlocallogin.get().getUserId(), DeviceModel.class);
    	List<Object[]>  deviceList=webDeviceListAll(CacheUtil.threadlocallogin.get().getUserModel());
    	result.put("status", "ok");
    	if((deviceList!=null)&&(deviceList.size()>0))
    		result.put("deviceList",  webDeviceListDisplay(deviceList));
        return ResponseEntity.ok(result);
    }
    private List<Object[]> webDeviceListAll(UserModel userModel)
    {
    	List<UserModel> childUserList=mongoDBService.findByField("parentId", userModel.getId(), UserModel.class);//child users
    	Map<String,String> userMap=new HashMap<>();
    	if((childUserList!=null)&&(childUserList.size()>0))
    		childUserList.forEach(user -> userMap.put(user.getId(), "Child"));
    	if((userModel.getClonedUser()!=null)&&(userModel.getClonedUser().getUserIdList()!=null)&&(userModel.getClonedUser().getUserIdList().size()>0))//clone
    		userModel.getClonedUser().getUserIdList().forEach(userId -> userMap.put(userId, "Clone"));
    	userMap.put(userModel.getId(), "Owner");//itself
    	if(userMap.size()>0)
    	{
    		List<DeviceModel> deviceModelList=mongoDBService.findByFieldIn("userId",new ArrayList<String>(userMap.keySet()), DeviceModel.class);
    		if((deviceModelList!=null)&&(deviceModelList.size()>0))
    		{
    	        List<Object[]> objectList = deviceModelList.stream()
    	                .map(device -> {
    	                    String fixedString = userMap.get(device.getUserId());
    	                    return fixedString != null ? new Object[]{fixedString, device} : null;
    	                })
    	                .filter(obj -> obj != null) 
    	                .collect(Collectors.toList());
    	        if((objectList!=null)&&(objectList.size()>0))
    	        	return objectList;
    		}
    	}
    	return null;
    }
    private List<Map<String,Object>> webDeviceListDisplay(List<Object[]>  deviceList)
    {
    	List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
    	Instant now=Instant.now();
    	for(Object[] deviceContainer:deviceList)
    	{
    		Map<String,Object> row=new HashMap<String,Object>();
    		DeviceModel device=(DeviceModel)deviceContainer[1];
    		row.put("id",device.getId());
    		row.put("deviceNo",device.getDeviceNo());
    		row.put("firmwareVersion",device.getDeviceFirmWareVersion()==null?"":device.getDeviceFirmWareVersion()+"");
    		row.put("deviceName",MiscUtil.strDisplay(device.getDeviceName()));
    		row.put("imei",MiscUtil.strDisplay(device.getImei()));
    		row.put("iccid",MiscUtil.strDisplay(device.getIccid()));
    		row.put("uploadTime",MiscUtil.dateFormat(device.getUploadTime(),"MM-dd HH:mm:ss"));
    		row.put("deviceStatus",device.getDeviceStatus());    		
    		row.put("colorStatus",0);
    		row.put("ownerShip",deviceContainer[0].toString());
    		if(device.getUploadTime()!=null)
    		{
    			long diffSeconds=MiscUtil.dateDiff(device.getUploadTime(), now);
    			if(diffSeconds<30*60)
    				row.put("colorStatus",2);
    			else if(diffSeconds<24*3600)
    				row.put("colorStatus",1);
    		}
    		result.add(row);
    	}
    	if(result.size()==0)
    		return null;
    	return result;
    }
    @GetMapping("/web/camera/{id}")
    public ResponseEntity<InputStreamResource> webCamera(@PathVariable String id) {
    	GridFsResource fsResource=mongoDBService.imageReadStream(id);
    	if(fsResource==null)
    		return null;
    	//todo, add priviledge control feature, use meta of fs.
    	try {
	        return ResponseEntity.ok()
	                .contentType(MediaType.IMAGE_JPEG) 
	                .body(new InputStreamResource(fsResource.getInputStream()));
    	}
	    catch(Exception e)
	    {
	    	logger.error(e.getMessage(),e);
	    }
	    return null;
    }

    @GetMapping("/web/devicemodule")
    public String webDeviceModule(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String deviceId = MiscUtil.webParamsGet(paramsReq, "deviceId", String.class, "");
    	Integer moduleTypeId = MiscUtil.webParamsGet(paramsReq, "moduleTypeId", Integer.class, -1);
    	Object[] devicePriv=utilHasPriv(deviceId);
    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
    	if(deviceModel==null)
    		return "notfound";
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_devices"), "deviceview?deviceId="+deviceId},
            {utilLocal("breadcrumb_module"), "deviceId="+deviceId+"&moduleTypeId="+moduleTypeId}
        });
    	
    	model.addAttribute("deviceId", deviceId);
    	model.addAttribute("moduleTypeId", moduleTypeId);
    	model.addAttribute("deviceType", deviceModel.getDeviceType().getName().toLowerCase());
    	ModuleDefine.ModuleType moduleType=ModuleDefine.ModuleType.fromId(moduleTypeId);
    	model.addAttribute("moduleName", moduleType.getName());
    	model.addAttribute("moduleDoc", moduleType.getName().toLowerCase());
        return "devicemodule"; 
    }
    @PostMapping("/web/devicemodulerefresh")
    public ResponseEntity<Map<String,Object>> webDeviceModuleRefresh(@RequestBody Map<String, Object> paramsBody,HttpServletRequest request) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	//Instant cur=Instant.now();
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "");
    	String deviceId = MiscUtil.webParamsGet(paramsBody, "deviceId", String.class, "");
    	Integer moduleTypeId = MiscUtil.webParamsGet(paramsBody, "moduleTypeId", Integer.class, -1);
		Instant startDate=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsBody, "startDate", String.class, null),null);
		Instant endDate=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsBody, "endDate", String.class, null),null);
		String displayType = MiscUtil.webParamsGet(paramsBody, "displayType", String.class, "");
		Integer page = MiscUtil.webParamsGet(paramsBody, "page", Integer.class, null);
    	String fields = MiscUtil.webParamsGet(paramsBody, "fields", String.class, null);

    	Object[] devicePriv=utilHasPriv(deviceId);
     	if ((action.equals("configUpdate") || action.equals("configDel") || action.equals("alertDel")
				|| action.equals("alertProcess") || action.equals("alertOk") || action.equals("infoRefresh"))
				&& (((Boolean) devicePriv[0] == false) || (((String) devicePriv[1]).equals("read")))) {
			result.put("status", utilLocal("no_priv"));
			return ResponseEntity.ok(result);
		}		
    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
		ModuleDefine.ModuleType moduleType=ModuleDefine.ModuleType.fromId(moduleTypeId);
		if(moduleType==null)
		{
			result.put("status", utilLocal("no_module") +" "+moduleTypeId);
			return ResponseEntity.ok(result);
		}
		if((deviceModel.getModuleInfoModelMap()==null)||(!deviceModel.getModuleInfoModelMap().containsKey(moduleType.getId())))
		{
			result.put("status", utilLocal("no_module_for_device") +" "+deviceId);
			return ResponseEntity.ok(result);
		}
		//ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(moduleType.getId());
		ModuleHandler moduleHandler=moduleType.getHandler();
		ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(moduleType.getId());
		result.put("status", "ok");
		mongoDBService.log(null, deviceModel==null?null:deviceModel.getId(), moduleInfoModel.getModuleTypeId().toString(), paramsBody, "webDeviceModuleRefresh", null,null);
    	switch(action)
    	{
	    	case "configUpdate":
	    		ResponseEntity<Map<String,Object>>tempResult=webDeviceConfigUpdate(paramsBody, result, deviceModel);
	    		if(tempResult!=null)//error
	    			return tempResult;	    		
	    		moduleInfoModel.setModifyTime(Instant.now());
	    		mongoDBService.save("webDeviceModuleRefresh1", moduleInfoModel);
	    		break;
	    	case "configDel":
	    		if(!webDeviceConfigDel(paramsBody, deviceModel,result))
	    			return ResponseEntity.ok(result);
	    		moduleInfoModel.setModifyTime(Instant.now());
	    		mongoDBService.save("webDeviceModuleRefresh2", moduleInfoModel);
	    		break;
	    	case "forwardUpdate":
	    		webDeviceForwardUpdate(moduleInfoModel,paramsBody);
	    		break;
	    	case "alertDel":
	    		webDeviceAlertDel(paramsBody);
	    		break;
	    	case "alertProcess":
	    		webDeviceAlertProcess(paramsBody);
	    		break;
	    	case "alertOk":
	    		webDeviceAlertOk(paramsBody);
	    		break;	
	    	case "infoRefresh":
	    		operateExecutor.refreshInfo(moduleHandler.getRefreshOperate(), deviceModel, request);
	    		break;
	    	case "historyRefresh":
		        Map<String,Object> aggMap1=moduleHandler.getHistoryData(deviceModel, startDate, endDate,displayType,page);
		        if(aggMap1!=null)
		        	result.put("aggMap", aggMap1);
	        	return ResponseEntity.ok(result);
	    	case "chartRefresh":
		        Map<String,Object> aggMap2=moduleHandler.getChartData(deviceModel, startDate, endDate,displayType,page);
		        if(aggMap2!=null)
		        	result.put("aggMap", aggMap2);
	        	return ResponseEntity.ok(result);
    	}    	
    	if(((fields==null)||(fields.contains("config")))&&(moduleHandler.getSectionMap().containsKey("configContainer")))
    	{
	    	List<Map<String,Object>> configMapList=moduleHandler.getConfigDisplay(deviceModel);
	    	if(configMapList!=null)
	    		result.put("configList", configMapList);
    	}
    	if(((fields==null)||(fields.contains("info")))&&(moduleHandler.getSectionMap().containsKey("runtimeContainer")))
    	{
	    	List<Map<String,Object>> infoMapList=moduleHandler.getRuntimeInfoDisplay(deviceModel);
	    	if(infoMapList!=null)
	    		result.put("infoList", infoMapList);
    	}
    	if(((fields==null)||(fields.contains("history")))&&(moduleHandler.getSectionMap().containsKey("historyDataContainer")))
    	{
    		Map<String,Object> aggMap1=moduleHandler.getHistoryData(deviceModel, startDate, endDate,displayType,page);
	        if((aggMap1!=null)&&(aggMap1.containsKey("dataList")))
	        	result.put("historyList", aggMap1.get("dataList"));
    	}
    	if(((fields==null)||(fields.contains("alert")))&&(moduleHandler.getSectionMap().containsKey("alertInfoContainer")))
    	{
	    	List<Map<String,Object>> alertMapList=moduleHandler.getAlert(deviceModel);
	    	if(alertMapList!=null)
	    		result.put("alertList", alertMapList);
    	}
    	if(((fields==null)||(fields.contains("forward")))&&(moduleHandler.getSectionMap().containsKey("forwardContainer")))
    	{
			ModuleInfoModel moduleInfo=deviceModel.getModuleInfoModelMap().get(moduleType.getId());
			if((moduleInfo.getAllowForward()!=null)&&moduleInfo.getAllowForward())
				result.put("forward", true);
			Integer from = MiscUtil.webParamsGet(paramsBody, "from", Integer.class, null);
			Integer to = MiscUtil.webParamsGet(paramsBody, "to", Integer.class, null);
	    	List<Map<String,Object>> postLogMapList=webDeviceViewPostLogDisplay(deviceModel,moduleType.getId(),from,to,fields);
	    	if(postLogMapList!=null)
	    		result.put("forwardList", postLogMapList);
    	}
    	if((fields==null)||(fields.contains("section")))
    	{
    		result.put("sectionMap", moduleHandler.getSectionMap());
    	}
    	
    	return ResponseEntity.ok(result);
    }
    @GetMapping("/web/devicesim")
    public String webDeviceSim(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String deviceId = MiscUtil.webParamsGet(paramsReq, "deviceId", String.class, "");
    	Object[] devicePriv=utilHasPriv(deviceId);
    	if(((Boolean)devicePriv[0]==false)||(devicePriv[2]==null))
    		return "accessdeny";
    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
    	String iccid=deviceModel.getIccid();
    	if(iccid!=null)
    	{
	    	//iccid="898608411124d0301568";
	    	model.addAttribute("iccid", iccid);
    		if(iccid.startsWith("89860"))
    		{
		    	String expired=simDWService.infoByIccid(iccid);
		    	if(expired!=null) 
		    	{
		    		model.addAttribute("msg", utilLocal("sim_msg_normal"));
		    		model.addAttribute("expired", expired);
		    		model.addAttribute("vendor", "DW");
		    		String used=simDWService.monthLastFlowByIccid(iccid,3); 
		    		if(used!=null)
		    			model.addAttribute("used", used);
		    	}else
		    	{
		    		model.addAttribute("msg", utilLocal("sim_msg_not_provided_by_us"));
		    	}
    		}else
    			model.addAttribute("msg", utilLocal("sim_msg_not_provided_by_us"));
    	}else
    		model.addAttribute("msg", utilLocal("sim_msg_no_card"));
        return "devicesim"; 
    }
    @GetMapping("/web/devicepostlog")
    public String webDevicePostLog(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String logId = MiscUtil.webParamsGet(paramsReq, "logId", String.class, "");
    	if(logId.length()==0)
    		return "notfound";
    	PostLogModel postLogModel=mongoDBService.findOneByField("id", logId, PostLogModel.class);
    	if(postLogModel==null)
    		return "accessdeny";
    	Object[] devicePriv=utilHasPriv(postLogModel.getDeviceId());
    	if(((Boolean)devicePriv[0]==false)||(devicePriv[2]==null))
    		return "accessdeny";
    	model.addAttribute("id", postLogModel.getId());
    	model.addAttribute("deviceId", postLogModel.getDeviceId());
    	model.addAttribute("deviceNo", postLogModel.getDeviceNo());
    	model.addAttribute("moduleName", postLogModel.getModuleName());
    	model.addAttribute("pack", postLogModel.getPack());
    	model.addAttribute("rx", postLogModel.getRx());
    	model.addAttribute("tx", postLogModel.getTx());
    	model.addAttribute("url", postLogModel.getUrl());
    	model.addAttribute("error", postLogModel.getError());
    	model.addAttribute("type", postLogModel.getType());
    	model.addAttribute("createTime", MiscUtil.dateFormat(postLogModel.getCreateTime(),"MM-dd HH:mm:ss"));
        return "devicepostlog"; 
    }
    @GetMapping("/web/devicecloudlog")
    public String webDeviceCloudLog(@RequestParam Map<String, Object> paramsReq,Model model) {
    	Integer topNum = MiscUtil.webParamsGet(paramsReq, "topNum", Integer.class, null);
    	String deviceId = MiscUtil.webParamsGet(paramsReq, "deviceId", String.class, "");
    	String action = MiscUtil.webParamsGet(paramsReq, "action", String.class, "listLog");
    	Instant startTime=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsReq, "startDate", String.class, null),"yyyy-MM-dd HH:mm");
    	Instant endTime=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsReq, "endDate", String.class, null),"yyyy-MM-dd HH:mm");
    	if(deviceId.length()==0)
    		return "notfound";
    	Object[] devicePriv=utilHasPriv(deviceId);
    	if(((Boolean)devicePriv[0]==false)||(devicePriv[2]==null))
    		return "accessdeny";
    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
     	
    	switch(action)
    	{
    		case "listLog":
    	    	Map<String, Object> conditionMap = new HashMap<>();
    	    	conditionMap.put("deviceId", deviceId);
    	    	conditionMap.put("moduleTypeId", ModuleDefine.MODULE_CLOUDLOG.getId());
    			if(topNum==null) //filter 
    	    	{
    		    	if(startTime!=null) {
    		    		startTime=startTime.truncatedTo(ChronoUnit.MINUTES);
    		    		conditionMap.put("startTime", startTime);
    		    	}
    		    	if(endTime!=null) {
    		    		endTime=endTime.truncatedTo(ChronoUnit.MINUTES).plus(1, ChronoUnit.MINUTES);
    		    		conditionMap.put("endTime", endTime);
    		    	}
    		    	topNum=1000;
    	    	}
    	        Sort sort = Sort.by(
    	                Sort.Order.desc("uploadTime")
    	        );
    	        List<DataCommModel> dataCommModelList=mongoDBService.find(conditionMap, sort, DataCommModel.class,0,1000);
    	        List<Map<String,String>> formattedLogs = new ArrayList<>();
    	        if((dataCommModelList!=null)&&(dataCommModelList.size()>0))
    	        {
    	        	for(DataCommModel dataCommModel:dataCommModelList)
    	        	{
    	        		if(dataCommModel.getUpload()!=null)
    	        		{
    	        			DataCloudLog logModel=(DataCloudLog)dataCommModel.getUpload();
	    	        		String[] lines=AnsiToHtmlConverter.convertToHtml(logModel.getContent());
	    	        		String timeStr=MiscUtil.dateFormat(dataCommModel.getUploadTime(),"MM-dd HH:mm:ss");
	    	        		for(String line:lines) {
	    	        			if(line.length()<=1)
	    	        				continue;
		    	        		Map<String,String> row=new HashMap<>();
		    	        		row.put("time",timeStr);
		    	        		row.put("content",line);
		    	        		formattedLogs.add(row);
	    	        		}
    	        		}
    	        	}
    	        	model.addAttribute("logs", formattedLogs);
    	        	
    	        	model.addAttribute("endTime", MiscUtil.dateFormat(dataCommModelList.get(0).getUploadTime(),"yyyy-MM-dd HH:mm:ss"));
    	        	model.addAttribute("startTime", MiscUtil.dateFormat(dataCommModelList.get(dataCommModelList.size()-1).getUploadTime(),"yyyy-MM-dd HH:mm:ss"));
    	        }      

    			break;
    	}
    	  
        model.addAttribute("deviceNo", deviceModel.getDeviceNo());
        model.addAttribute("deviceId", deviceModel.getId());
        return "devicecloudlog";
    }
    @GetMapping("/web/deviceview")
    public String webDeviceView(@RequestParam Map<String, Object> paramsReq,Model model) {
    	String deviceId = MiscUtil.webParamsGet(paramsReq, "deviceId", String.class, "");
    	utilBreadcrumb(model,new String[][]{
            {utilLocal("menu_devices"), "deviceview?deviceId="+deviceId}
        });
    	model.addAttribute("deviceId", deviceId);
        return "deviceview"; 
    }
    @PostMapping("/web/deviceviewrefresh")
    public ResponseEntity<Map<String,Object>> webDeviceViewRefresh(@RequestBody Map<String, Object> paramsBody) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	String deviceId = MiscUtil.webParamsGet(paramsBody, "deviceId", String.class, "");
    	String fields = MiscUtil.webParamsGet(paramsBody, "fields", String.class, null);
    	String action = MiscUtil.webParamsGet(paramsBody, "action", String.class, "");
    	Object[] devicePriv=utilHasPriv(deviceId);
    	if(((Boolean)devicePriv[0]==false)||(devicePriv[2]==null))
    	{
			result.put("status", utilLocal("no_device") );
			return ResponseEntity.ok(result);
    	}
    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
    	//forward
    	Integer forward = MiscUtil.webParamsGet(paramsBody, "forward", Integer.class, null);
    	mongoDBService.log(null, deviceModel==null?null:deviceModel.getId(), null, paramsBody, "webDeviceViewRefresh", null,null);
    	switch(action)
    	{
   			case "moduleForwardUpdate":
   				Integer moduleTypeId = MiscUtil.webParamsGet(paramsBody, "moduleTypeId", Integer.class, null);
   				if(moduleTypeId!=null)
   				{
   					ModuleInfoModel moduleInfoModel=deviceModel.getModuleInfoModelMap().get(moduleTypeId);
   					webDeviceForwardUpdate(moduleInfoModel,paramsBody);
   				}
   				break;
    		case "forwardUpdate":
        		if((forward==null)||(forward>1)||(forward<0))
        			return null;
        		if(forward==0)
        			deviceModel.setForwardStatus(false);
        		else 
        			deviceModel.setForwardStatus(true);
        		mongoDBService.save("webDeviceViewRefresh1", deviceModel);
    			break;
    		case "forwardReset":
        		if((forward==null)||(forward>1)||(forward<0))
        			return null;
    			boolean tempValue=(forward==0?false:true);
    			mongoDBService.updateByField(ModuleInfoModel.class, "deviceId", deviceModel.getId(), "allowForward", tempValue);
    			deviceModel.setForwardStatus(tempValue);
        		mongoDBService.save("webDeviceViewRefresh2", deviceModel);
    			for(ModuleInfoModel moduleInfoModel:deviceModel.getModuleInfoModelMap().values())
    				moduleInfoModel.setAllowForward(tempValue);
    			break;
    	}
    	//forward status
    	boolean forwardStatus=false;
    	if((deviceModel.getForwardStatus()!=null)&&(deviceModel.getForwardStatus()))
    		forwardStatus=true;
    	result.put("forwardStatus", forwardStatus);
    	//module list
    	if((fields==null)||(fields.contains("module")))
    	{
	    	List<Map<String,Object>> moduleMapList=webDeviceViewModuleDisplay(deviceModel);
	    	if(moduleMapList!=null)
	    		result.put("moduleList", moduleMapList);
    	}
    	//post log
    	if((fields==null)||(fields.contains("forward")))
    	{
			Integer from = MiscUtil.webParamsGet(paramsBody, "from", Integer.class, null);
			Integer to = MiscUtil.webParamsGet(paramsBody, "to", Integer.class, null);
	    	List<Map<String,Object>> postLogMapList=webDeviceViewPostLogDisplay(deviceModel,null,from,to,fields);
	    	if(postLogMapList!=null)
	    		result.put("forwardList", postLogMapList);
    	}
    	result.put("status", "ok");
    	return ResponseEntity.ok(result);
    }
	private boolean webDeviceConfigDel(Map<String, Object> paramsBody,DeviceModel deviceModel,Map<String,Object> result) {
		Map<Integer, DeviceConfigElementModel> configmap=deviceModel.getDeviceConfig();
		Integer index = MiscUtil.webParamsGet(paramsBody, "index", Integer.class, -1);
		if(index.intValue()==0)
		{
			result.put("status",  utilLocal("can_not_del_version_config") );
			return false;
		}
		if((configmap!=null)&&(configmap.containsKey(index)))
		{
			configmap.remove(index);
			mongoDBService.delete("id", configmap.get(index).getId(), DeviceConfigElementModel.class);
			return true;
		}
		result.put("status", utilLocal("del_config_failed") );
		return false;
	}
	private void webDeviceForwardUpdate(ModuleInfoModel moduleInfoModel,Map<String, Object> paramsBody) {
		Integer forward = MiscUtil.webParamsGet(paramsBody, "forward", Integer.class, null);
		if((forward==null)||(forward>1)||(forward<0))
			return;
		if(forward==0)
			moduleInfoModel.setAllowForward(false);
		else 
			moduleInfoModel.setAllowForward(true);
		mongoDBService.save("updateforward", moduleInfoModel);
	}
	private void webDeviceAlertDel(Map<String, Object> paramsBody) {
		String alertId = MiscUtil.webParamsGet(paramsBody, "alertId", String.class, "");
		if(alertId.length()>0)
		{
			mongoDBService.updateById(ModuleRuleLogModel.class,alertId, "status", ModuleRuleLogModel.ModuleRuleLogModelStatusEnum.CANCEL);
		}
	}
	private void webDeviceAlertProcess(Map<String, Object> paramsBody) {
		String alertId = MiscUtil.webParamsGet(paramsBody, "alertId", String.class, "");
		if(alertId.length()>0)
		{
			mongoDBService.updateById(ModuleRuleLogModel.class,alertId, "status", ModuleRuleLogModel.ModuleRuleLogModelStatusEnum.PROCESS);
		}
	}
	private void webDeviceAlertOk(Map<String, Object> paramsBody) {
		String alertId = MiscUtil.webParamsGet(paramsBody, "alertId", String.class, "");
		if(alertId.length()>0)
		{
			mongoDBService.updateById(ModuleRuleLogModel.class,alertId, "status", ModuleRuleLogModel.ModuleRuleLogModelStatusEnum.DONE);
		}
	}
	private ResponseEntity<Map<String, Object>> webDeviceConfigUpdate(Map<String, Object> paramsBody, Map<String, Object> result, DeviceModel deviceModel) {
		String fieldValue = MiscUtil.webParamsGet(paramsBody, "fieldValue", String.class, "");
		Map<Integer, DeviceConfigElementModel>  configmap=deviceModel.getDeviceConfig();
		Integer index = MiscUtil.webParamsGet(paramsBody, "index", Integer.class, -1);
		fieldValue=fieldValue.trim();
		if((configmap!=null)&&(configmap.containsKey(index)))
		{
			DeviceConfigElementModel element=configmap.get(index);
			if(element.getType()<=1)//number
			{
				if(fieldValue.length()>0)
				{
					Long fieldValueLong=MiscUtil.strParseLong(fieldValue);
					if(fieldValueLong==null) {
						result.put("status", utilLocal("value_in_wrong_format") );
						return ResponseEntity.ok(result);
					}
					long maxValue=(long)element.getValueLen()*0xFFL;
					if(fieldValueLong>maxValue) {
						result.put("status", utilLocal("value_too_big") );
						return ResponseEntity.ok(result);
					}else if(fieldValueLong<0)
					{
						result.put("status", utilLocal("value_is_negative") );
						return ResponseEntity.ok(result);
					}
					if((element.getType()==0)&&((element.getMinValue()>fieldValueLong)||(element.getMaxValue()<fieldValueLong)))//normal
					{
						result.put("status", utilLocal("between")+" "+element.getMinValue()+" "+utilLocal("and")+" "+element.getMaxValue());
						return ResponseEntity.ok(result);
					}
					if((element.getType()==1)&&(!element.getEnumValue().contains(fieldValueLong.intValue())))//enum
					{
						String errMsg = element.getEnumValue().stream().map(String::valueOf).collect(Collectors.joining(" "));
						result.put("status", utilLocal("should_be")+" "+errMsg);
						return ResponseEntity.ok(result);
					}
				}
				if(fieldValue.trim().length()==0)
					fieldValue=null;
			}else if(element.getType()<=54)//string
			{
				if(fieldValue.length()>element.getValueLen()) {
					result.put("status", utilLocal("string_too_long") );
					return ResponseEntity.ok(result);
				}
				if((fieldValue.length()>element.getMaxValue())||(fieldValue.length()<element.getMinValue()))
				{
					result.put("status", utilLocal("string_length_between")+" "+element.getMinValue()+" "+utilLocal("and")+" "+element.getMaxValue());
					return ResponseEntity.ok(result);
				}
				if((element.getType()==52)&&(!MiscUtil.ipV4Valid(fieldValue)))//ip v4
				{
					result.put("status", utilLocal("not_valid_ip_address") );
					return ResponseEntity.ok(result);
				}
				if((element.getType()==54)&&(!MiscUtil.ipV4OrDomainValid(fieldValue)))//url
				{
					result.put("status", utilLocal("not_valid_url")  );
					return ResponseEntity.ok(result);
				}
			}
			if(!Objects.equals(configmap.get(index).getValueInDb(), fieldValue))
			{
				configmap.get(index).setValueInDb(fieldValue);
				mongoDBService.save("configupdate", element);
			}
		}else
		{
			result.put("status", utilLocal("no_this_index")  );
			return ResponseEntity.ok(result);
		}
		return null;
	}
//    private List<Map<String,Object>> webDeviceViewConfigDisplay(DeviceModel deviceModel)
//    {
//    	Map<Integer, DeviceConfigElementModel>  configmap=deviceModel.getDeviceConfig();
//    	if((configmap==null)||(configmap.size()==0))
//    		return null;
//    	List<Map<String,Object>> result=ModuleHandler.utilConfigDisplay(new ArrayList<DeviceConfigElementModel>(configmap.values()));
//    	if(result.size()==0)
//    		return null;
//    	return result;
//    }
    private List<Map<String,Object>> webDeviceViewModuleDisplay(DeviceModel deviceModel)
    {
    	Map<Integer, ModuleInfoModel> moduleInfoMap=deviceModel.getModuleInfoModelMap();
    	if((moduleInfoMap==null)||(moduleInfoMap.size()==0))
    		return null;
    	Instant now=Instant.now();
        List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
    	for(ModuleInfoModel moduleInfoModel:moduleInfoMap.values())
    	{
    		Map<String,Object> row=new HashMap<String,Object>();
    		row.put("id",moduleInfoModel.getModuleTypeId());
    		row.put("allowForward",((moduleInfoModel.getAllowForward()==null)||moduleInfoModel.getAllowForward()==false)?0:1);
    		row.put("name",ModuleType.fromId(moduleInfoModel.getModuleTypeId()).getName());
    		row.put("description",ModuleType.fromId(moduleInfoModel.getModuleTypeId()).getDescription());
    		row.put("modifyTime",MiscUtil.dateFormat(moduleInfoModel.getModifyTime(),"MM-dd HH:mm:ss"));
    		row.put("infoReqTime",MiscUtil.dateFormat(moduleInfoModel.getRequestTime(),"MM-dd HH:mm:ss"));
    		Instant uploadDate=MiscUtil.dateSelectNew(moduleInfoModel.getUploadTime(), moduleInfoModel.getUploadTime());
    		row.put("uploadTime",MiscUtil.dateFormat(uploadDate,"MM-dd HH:mm:ss"));
    		row.put("colorStatus",0);
    		if(uploadDate!=null)
    		{
    			long diffSeconds=MiscUtil.dateDiff(uploadDate, now);
    			if(diffSeconds<30*60)
    				row.put("colorStatus",2);
    			else if(diffSeconds<24*3600)
    				row.put("colorStatus",1);
    		}
    		result.add(row);
    	}
    	if(result.size()==0)
    		return null;
        Collections.sort(result, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> map1, Map<String, Object> map2) {
                Integer id1 = (Integer) map1.get("id");
                Integer id2 = (Integer) map2.get("id");
                return id1.compareTo(id2); 
            }
        });

    	return result;
    }
    private List<Map<String,Object>> webDeviceViewPostLogDisplay(DeviceModel deviceModel,Integer moduleTypeId,Integer from,Integer to,String fields)
    {
      	Criteria criteria = new Criteria();
        criteria.and("deviceId").is(deviceModel.getId());
    	if(moduleTypeId!=null)
    		criteria.and("moduleTypeId").is(moduleTypeId);
    	if((from==null)||(to==null)||(from>=to)||(to>(from+1000)))
    	{
    		from=0;
    		to=10;
    	}
    		
    	List<PostLogModel> postLogModelList=mongoDBService.findByFields(criteria, "createTime", false, from,to, null,PostLogModel.class);
        List<Map<String,Object>> result=new ArrayList<Map<String,Object>>();
        if(postLogModelList==null)
        	return null;
    	for(PostLogModel model:postLogModelList)
    	{
    		Map<String,Object> row=new HashMap<String,Object>();
    		String pack=model.getPack();
    		pack=pack.substring(pack.length()-6,pack.length());
    		row.put("id",model.getId());
    		row.put("pack",pack);
    		row.put("moduleTypeId", model.getModuleTypeId());
    		row.put("moduleName", model.getModuleName());
    		String tx=model.getTx();
    		String rx=model.getRx();
    		if(fields==null)//web page,limit length of data
    		{
    			tx=MiscUtil.strLimit(tx, " ...", 300);
    			rx=MiscUtil.strLimit(rx, " ...", 50);
    		}
    		row.put("tx",tx);
    		row.put("rx",rx);
    		row.put("url",model.getUrl());
    		row.put("type",model.getType());    	
    		row.put("error",model.getError()+"");    	
    		row.put("createTime",MiscUtil.dateFormat(model.getCreateTime(),"MM-dd HH:mm:ss"));
    		result.add(row);
    	}
    	if(result.size()==0)
    		return null;
    	return result;
    }
    @PostMapping("/web/devicecmdrefresh")
    public ResponseEntity<Map<String,Object>> webDeviceCmdRefresh(@RequestBody Map<String, Object> paramsBody,HttpServletRequest request) {
    	Map<String,Object> result=new HashMap<String,Object>();
    	String deviceId = MiscUtil.webParamsGet(paramsBody, "deviceId", String.class, "");
    	//String moduleTypeId = MiscUtil.webParamsGet(paramsBody, "moduleTypeId", String.class, "");
    	String command = MiscUtil.webParamsGet(paramsBody, "command", String.class, "").trim();
        result.put("status", "ok");
        logger.info("webDeviceCmdRefresh command "+command);
        String rspStr=utilLocal("exe_wrong") ;
        if(CacheUtil.threadlocallogin.get().getUserModel().getParentId()!=null)
        {
			result.put("status", utilLocal("no_priv"));
			return ResponseEntity.ok(result);
        }
    	Object[] devicePriv=utilHasPriv(deviceId);
		if(((Boolean)devicePriv[0]==false)||(((String)devicePriv[1]).equals("read")))
		{
			result.put("status", utilLocal("no_priv"));
			return ResponseEntity.ok(result);
		}

    	DeviceModel deviceModel=(DeviceModel)devicePriv[2];
		if(deviceModel!=null)
		{
			 mongoDBService.log(null, deviceModel==null?null:deviceModel.getId(), null, paramsBody, "webDeviceCmdRefresh", null,null);
			 OperateRequest operateRequest=new OperateRequest();
			 operateRequest.setRawString(command);
			 operateRequest.setIp(request.getRemoteHost());
			 operateRequest.setSourceType(DataCommModel.DataCommSource.CLOUD_COMMAND);
			 ResultType resultModule=operateExecutor.exeOperate(operateRequest, deviceModel);
		     rspStr=resultModule.getErrorString() +" "+resultModule.getResponseStr();
			 mongoDBService.save("cmdrefresh",deviceModel);
		}else
			rspStr="no priviledge";
        result.put("response", MiscUtil.strDisplay(rspStr));
		return ResponseEntity.ok(result);
    }
    @PostMapping("/web/export/excelupload")
    public ResponseEntity<byte[]> webExportExcelUpload(@RequestBody Map<String, Object> paramsBody)  {
    	String deviceId = MiscUtil.webParamsGet(paramsBody, "deviceId", String.class, "");
    	Integer moduleTypeId = MiscUtil.webParamsGet(paramsBody, "moduleTypeId", Integer.class, -1);
    	Object[] devicePriv=utilHasPriv(deviceId);
    	if((boolean) devicePriv[0] == false)
    		 return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		ModuleDefine.ModuleType moduleType=ModuleDefine.ModuleType.fromId(moduleTypeId);
		if(moduleType==null)
			 return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		ModuleHandler moduleHandler=moduleType.getHandler();
		DeviceModel deviceModel=(DeviceModel)devicePriv[2];
		Instant startDate=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsBody, "startDate", String.class, null),null);
		Instant endDate=MiscUtil.dateParse(MiscUtil.webParamsGet(paramsBody, "endDate", String.class, null),null);
        Map<String,Object> aggMap=moduleHandler.getHistoryData(deviceModel, startDate, endDate,"exportall",-1);
        if(aggMap==null)
        	 return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        @SuppressWarnings("unchecked")
		List<Map<String, Object>> dataList=(List<Map<String, Object>>)aggMap.get("dataList");
        if((dataList==null)||(dataList.size()==0))
       	 	return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        try (
                Workbook workbook = new XSSFWorkbook(); // Create workbook
                ByteArrayOutputStream out = new ByteArrayOutputStream() // Output stream for workbook data
            ) {
                Sheet sheet = workbook.createSheet("data");
             	// Create header row 
                int rowIdx = 0,colIdx=0;
	            Row headerRow = sheet.createRow(rowIdx++);
	            headerRow.createCell(colIdx++).setCellValue("deviceId");
	            headerRow.createCell(colIdx++).setCellValue("deviceNo");
	            headerRow.createCell(colIdx++).setCellValue("moduleTypeId");
	            headerRow.createCell(colIdx++).setCellValue("moduleType");
	            headerRow.createCell(colIdx++).setCellValue("request");
	            headerRow.createCell(colIdx++).setCellValue("requestTime");
	            headerRow.createCell(colIdx++).setCellValue("info");
	            headerRow.createCell(colIdx++).setCellValue("upload");
	            headerRow.createCell(colIdx++).setCellValue("uploadTime");
	            headerRow.createCell(colIdx++).setCellValue("error");
	            headerRow.createCell(colIdx++).setCellValue("commType");
	            headerRow.createCell(colIdx++).setCellValue("commSource");
	            
	            for (Map<String, Object> data : dataList) {
	                Row row = sheet.createRow(rowIdx++);
	                colIdx=0;
	                row.createCell(colIdx++).setCellValue(deviceId);
	                row.createCell(colIdx++).setCellValue(deviceModel.getDeviceNo());
	                row.createCell(colIdx++).setCellValue(moduleTypeId);
	                row.createCell(colIdx++).setCellValue(moduleType.getName());
	                for(int i=0;i<data.size();i++)
	                {
	                	String cellName=headerRow.getCell(colIdx+i).getStringCellValue();
	                	row.createCell(colIdx+i).setCellValue(data.get(cellName).toString());
	                }
	            }
	            workbook.write(out);

	            // Prepare HTTP response
	            HttpHeaders headers = new HttpHeaders();
	            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=upload_data.xlsx");
	            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return HTTP 500 if an error occurs
        }
    }	
    @PostMapping("/web/export/excelchart")
    public ResponseEntity<byte[]> webExportExcelChart(@RequestBody Map<String, Object> chartData)  {
        try (
                Workbook workbook = new XSSFWorkbook(); // Create workbook
                ByteArrayOutputStream out = new ByteArrayOutputStream() // Output stream for workbook data
            ) {
                Sheet sheet = workbook.createSheet("Chart Data");
                @SuppressWarnings("unchecked")
				List<String> xAxisData = (List<String>) chartData.get("xAxisData");
                @SuppressWarnings("unchecked")
				Map<String, List<Object>> yAxisData = (Map<String, List<Object>>) chartData.get("yAxisData");
            
                if ((xAxisData == null) || (yAxisData == null) || (xAxisData.size()==0) || (yAxisData.size()==0)) {
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Return HTTP 204 if no data
                }

            	// Create header row for X axis data
	            Row headerRow = sheet.createRow(0);
	            headerRow.createCell(0).setCellValue("Series Name");
	            int colIdx = 1;
	            for (String xLabel : xAxisData) {
	                headerRow.createCell(colIdx++).setCellValue(xLabel);
	            }
	
	            // Create rows for each series
	            int rowIdx = 1;
	            for (String seriesName : yAxisData.keySet()) {
	            	List<Object> serieData=yAxisData.get(seriesName);
	                Row row = sheet.createRow(rowIdx++);
	                row.createCell(0).setCellValue(seriesName);
	                for (int i = 0; i < serieData.size(); i++) {
	                    row.createCell(i + 1).setCellValue(serieData.get(i).toString());
	                }
	            }
	            workbook.write(out);

	            // Prepare HTTP response
	            HttpHeaders headers = new HttpHeaders();
	            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=chart_data.xlsx");
	            headers.add(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

	            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.error(e.getMessage(),e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Return HTTP 500 if an error occurs
        }
    }
    
    @GetMapping("/test")
    @ResponseBody
    public Map<String,Object> test() {
 
        return null;
    }
}
