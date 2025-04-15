package com.zyc.dc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import com.zyc.dc.service.CommService;
import com.zyc.dc.service.DataInitService;
import com.zyc.dc.service.module.ModuleDefine;


@Component
public class AutoStarter implements ApplicationRunner {
	@Autowired
	private DataInitService dataInitService;
	@Autowired
	private CommService commService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
    	ModuleDefine.init();
    	commService.start();
    	dataInitService.init();
    }
}
