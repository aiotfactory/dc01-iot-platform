package com.zyc.dc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
	private static final Logger logger = LoggerFactory.getLogger(RedisService.class);
    // 频道名
    public final static String CHANNEL_BUILD_SUBMIT = "channel_build_submit";

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisMessageListenerContainer redisMessageListenerContainer;

    // 构造方法注入 StringRedisTemplate 和 RedisMessageListenerContainer
    public RedisService(StringRedisTemplate stringRedisTemplate, 
                        RedisMessageListenerContainer redisMessageListenerContainer) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    // 发布消息的方法
    public void put(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    // 获取消息的方法
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    // 发布消息到指定频道
    public void publishMessage(String channel,String message) {
        stringRedisTemplate.convertAndSend(channel, message);
    }

    // 启动订阅功能，订阅指定的频道
    //@PostConstruct
    public void startListening() {
        // 创建消息监听器，处理接收到的消息
        MessageListener messageListener = (message, pattern) -> {
            // 这里可以处理收到的消息
        	logger.info("received message from channel " + CHANNEL_BUILD_SUBMIT + ": " + new String(message.getBody()));
        };

        // 将监听器添加到监听容器，并订阅指定频道
        redisMessageListenerContainer.addMessageListener(messageListener, new PatternTopic(CHANNEL_BUILD_SUBMIT));

        // 启动容器来接收消息
        redisMessageListenerContainer.start();
        logger.info("started listening on channel: "+ CHANNEL_BUILD_SUBMIT);
    }
    //@PreDestroy
    public void stopListening() {
        // 停止容器，停止订阅
        redisMessageListenerContainer.stop();
        logger.info("stopped listening on channel: " + CHANNEL_BUILD_SUBMIT);
    }
}
