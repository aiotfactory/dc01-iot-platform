package com.zyc.dc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.zyc.dc.service.ConfigProperties;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@Autowired
	private ConfigProperties configProperties;
	
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleAllExceptions(Exception ex) {
    	//if(configProperties.ENV_PRODUCTION()==0)//dev
    	logger.error("Uncatched exception1 on "+configProperties.ENV_PRODUCTION()+":", ex);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFoundException(Exception ex) {
    	logger.error("Uncatched exception2 on "+configProperties.ENV_PRODUCTION()+":", ex);
        return "notfound";
    }
}
