package com.zyc.dc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class SimDWService {
	private static final Logger logger = LoggerFactory.getLogger(SimDWService.class);
	@Value("${sim.dw.api-url}")
    private String API_URL;
	@Value("${sim.dw.api-id}")
    private String API_ID;
	@Value("${sim.dw.api-secret}")
    private String API_SECRET;
	

    public String monthFlowByIccid(String iccid,String month) 
    {
        Map<String, Object> params = new HashMap<>();
        params.put("iccid", iccid);
        params.put("month", MiscUtil.dateFormat(Instant.now(), month));
        JsonNode rootNode=query(params,"get_flow_month");
        if(rootNode!=null) {
            JsonNode resultNode = rootNode.path("result");
            if (resultNode != null) {
                Double used=resultNode.path("used").asDouble();
                if(used!=null)
                {
                	if(used>1024)
                		return (used/1024)+"M";
                	return used+"K";
                }
            }
        }
        return null;
    }
    public String monthLastFlowByIccid(String iccid,int numOfMonth) 
    {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        LocalDate currentDate = LocalDate.now();
        String ret="";
        for (int i = 0; i < numOfMonth; i++) {
            String month = currentDate.format(formatter);
            String temp=monthFlowByIccid(iccid,month);
            if(temp!=null)
            	ret=ret+month+":"+temp+",";
            currentDate = currentDate.minusMonths(1);
        }
        return ret;
    }
    public String infoByIccid(String iccid) 
    {
        Map<String, Object> params = new HashMap<>();
        params.put("iccids", iccid);
        JsonNode rootNode=query(params,"get_sim_card_detail");
        if(rootNode!=null) {
	        JsonNode resultNode = rootNode.path("result").get(0); // 获取第一个结果对象
	        if(resultNode!=null) {
	        	String serviceEndTime = resultNode.path("service_end_time").asText();
	        	if((serviceEndTime!=null)&&(serviceEndTime.length()>0))
	        		return serviceEndTime;
	        }
        }
        return null;
    }
	private JsonNode query(Map<String, Object> params,String api) {
		params.put("api_id", API_ID);
		params.put("timestamp", System.currentTimeMillis() / 1000);
		String signBase = params.keySet().stream()
                .sorted()
                .map(key -> key + "=" + params.get(key))
                .reduce((a, b) -> a + "&" + b)
                .orElse("") + API_SECRET;
        String signStr = SHA256(encode(signBase.getBytes(StandardCharsets.UTF_8)));
        params.put("sign", signStr);
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        params.forEach((key, value) -> formData.add(key, value.toString()));
        WebClient webClient = WebClient.builder()
                .baseUrl(API_URL+"/sim_cards/"+api)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        Mono<String> responseMono = webClient.post()
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(String.class);
        String response = responseMono.block();
        if((response!=null)&&(response.length()>0))
        {
        	JsonNode rootNode= MiscUtil.jsonStr2JsonNode(response);
        	if((rootNode!=null)&&("0".equals(rootNode.path("code").asText())))
        		return rootNode;
        }
        logger.info(response);
        return null;
	}

    private String encode(final byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    private String SHA(final String strText, final String strType) {
        String strResult = null;
        if (strText != null && strText.length() > 0) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                messageDigest.update(strText.getBytes());
                byte byteBuffer[] = messageDigest.digest();
                StringBuilder strHexString = new StringBuilder();
                for (byte b : byteBuffer) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }
}