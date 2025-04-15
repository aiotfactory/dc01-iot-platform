package com.zyc.dc.service;


import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    public static <T> T getBean(Class<T> beanClass) {
        if (applicationContext == null) {
            throw new IllegalStateException("Application context has not been set");
        }
        return applicationContext.getBean(beanClass);
    }

    @SuppressWarnings("unchecked")
	public static <T> T getBean(String beanName) {
        if (applicationContext == null) {
            throw new IllegalStateException("Application context has not been set");
        }
        return (T) applicationContext.getBean(beanName);
    }
}

