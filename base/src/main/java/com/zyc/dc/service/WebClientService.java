package com.zyc.dc.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.zyc.dc.dao.ApiKeyModel;
import com.zyc.dc.dao.PostLogModel;
import com.zyc.dc.service.module.ModuleDefine;

import reactor.core.publisher.Mono;

@Service
public class WebClientService {
	@Autowired
	private MongoDBService mongoDBService;
    @Autowired
    private WebClient.Builder webClientBuilder;
	private static final Logger logger = LoggerFactory.getLogger(WebClientService.class);
	
	@SuppressWarnings("unchecked")
	public PostLogModel postMap(String deviceId,String deviceNo,ApiKeyModel apiKeyModel,ModuleDefine.ModuleType moduleType,boolean block,Object... params)
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
		urlLog.setCreateTime(Instant.now());
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
			post(apiKeyModel.getForwardUrl(),json,block,urlLog); 
		}
		return urlLog;
	}
	public PostLogModel post(String url, String body, boolean block, PostLogModel log) {
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
                handleResponse(log, response);
            } else {
                // Asynchronous mode
                responseMono.subscribe(
                        response -> {
                            handleResponse(log, response);
                        },
                        error -> {
                            logger.error("error occurred: {}", error.getMessage());
                            log.setError(-2);
                            handleResponse(log, error.getMessage());
                        }
                );
            }
        } catch (RuntimeException e) {
            logger.error("runtime exception: {}", e.getMessage(), e);
            log.setError(-3);
            handleResponse(log, e.getMessage());
        } catch (Exception e) {
            logger.error("exception: {}", e.getMessage(), e);
            log.setError(-4);
            handleResponse(log, e.getMessage());
        }

        return log;
    }
    private void handleResponse(PostLogModel log,String response) {
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
}
