package com.zyc.dc;

import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;
import com.zyc.dc.service.CommService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.Locale;
import java.util.Properties;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;
	/*
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8000") // 前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") 
                .excludePathPatterns("/","/pub/**", "/api/call","/test","/css/**", "/js/**","/thirdparty/**", "/help/**","/favicon.ico"); 
    }
    @Bean
    public WebClient.Builder webClientBuilder() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(50)  
                .maxIdleTime(Duration.ofMinutes(2)) 
                .maxLifeTime(Duration.ofMinutes(5)) 
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 20*1000)  // 20 seconds
                .responseTimeout(Duration.ofSeconds(20)) 
                .doOnConnected(conn -> conn
                        .addHandlerLast(new io.netty.handler.timeout.ReadTimeoutHandler(20)) 
                        .addHandlerLast(new io.netty.handler.timeout.WriteTimeoutHandler(20))  
                );
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", "IOT Factory")
                .defaultHeader("Accept", "application/json");
    }
    @Bean
    public Producer defaultKaptcha() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width", "100"); // 图像宽度
        properties.setProperty("kaptcha.image.height", "40"); // 图像高度
        properties.setProperty("kaptcha.textproducer.font.size", "30"); // 字体大小
        //properties.setProperty("kaptcha.textproducer.char.string", "0123456789abcdefghijklmnopqrstuvwxyz"); // 字符集
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789"); // 字符集
        properties.setProperty("kaptcha.textproducer.char.length", "4"); // 字符长度
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.ShadowGimpy"); // 扰乱实现
        //properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple"); // 扰乱实现
        //properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.DefaultKaptcha"); // 扰乱实现
        Config config = new Config(properties);
        return config.getProducerImpl();
    }
    
    // 配置 CookieLocaleResolver
    @SuppressWarnings("deprecation")
	@Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setCookieName("lang");  // 设置 Cookie 名称
        localeResolver.setDefaultLocale(Locale.US);  // 设置默认语言为美国英语
        localeResolver.setCookieMaxAge(-1);  // 设置 Cookie 过期时间，30 天
        return localeResolver;
    }
    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory); // 设置连接工厂
        return container;
    }
    @Bean
    public CommService commService() {
        return new CommService();
    }
}
