package com.zyc.dc;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.zyc.dc.dao.Login;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.service.ConfigProperties;
import com.zyc.dc.service.MiscUtil;
import com.zyc.dc.util.CacheUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {
	@Autowired
	private MiscUtil miscUtil;
	@Autowired
	private ConfigProperties configProperties;
	
	//private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
	
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	String command=request.getServletPath().substring(request.getServletPath().lastIndexOf('/')+1);
    	request.getServletPath().lastIndexOf('/');
    	Cookie[] cookies = request.getCookies();
    	Instant now=Instant.now();
    	String userSecret=null,userName=null;
    	if((cookies!=null)&&(cookies.length>0))
    	{
    		for(Cookie cookie:cookies)
    		{
    			if(cookie.getName().equals("usersecret"))
    			{
    				userSecret = cookie.getValue();
    			}else if(cookie.getName().equals("username"))
    			{
    				userName = cookie.getValue();
    			}
    		}
    	}
    	//printRequest(command,request,userName,userSecret);
		if((userSecret!=null)&&(userSecret.length()>0)&&(userName!=null)&&(userName.length()>0))
		{
			Object[] matchResult=miscUtil.loginMatch(request,response, userName, null, null,userSecret, now);
			if((matchResult!=null)&&(((Boolean)matchResult[0])==true))
			{
				UserModel userModel=(UserModel)matchResult[1];
				Login login=new Login();
				login.setUserId(userModel.getId());
				login.setUserlogin(userModel.getLogin());
				login.setUserModel(userModel);
				login.setCommand(command);
				login.setUserName(login.getUserName());
				login.setUserSecret(userSecret);
				CacheUtil.threadlocallogin.set(login);
				return true;
			}
		}
		response.sendRedirect(configProperties.SITE_URL());
        return false;
    }
//	private void printRequest(String command,HttpServletRequest request,String userName,String userSecret) 
//	{
//		Map<String,String[]> reqmap=request.getParameterMap();
//		String reqstr="";
//		for(String key:reqmap.keySet())
//		{
//			String[] values=reqmap.get(key);
//			reqstr=reqstr+key+":";
//			if(values!=null)
//			{
//				for(String value:values)
//					reqstr=reqstr+value+".";
//			}
//			reqstr=reqstr+",";
//		}
//		logger.info("interceptor1 "+command+" name "+userName+" secret "+userSecret);
//		logger.info("interceptor2 "+command+" "+reqstr);
//	} 
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    	CacheUtil.threadlocallogin.remove();
    }
}
