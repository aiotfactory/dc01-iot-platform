package com.zyc.dc.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AIService {
    @Autowired
    private WebClient.Builder webClientBuilder;
    
	private static final Logger logger = LoggerFactory.getLogger(AIService.class);
	
	public void translate()
	{
	   	//String target="将下面html中的中文内容翻译为英文html结构不修改，不要留下任何中文内容，包括注释内的中文也要翻译，只输出完整html，不要输出其他:";
        //String folder = "D:\\project\\datacollection\\cloud\\dc01\\main\\src\\main\\resources\\templates\\doc\\dc01\\command";
    	//aiService.translateOneFile(folder,"spl06_zh.html","spl06_en.html",target);
    	//aiService.translateOneFile(folder,"tm7705inout_zh.html","tm7705inout_en.html",target);
    	//aiService.translateOneFile(folder,"tm7705status_zh.html","tm7705status_en.html",target);
    	//aiService.translateOneFile(folder,"uartinout_zh.html","uartinout_en.html",target);
    	//String folder = "D:\\project\\datacollection\\cloud\\dc01\\main\\src\\main\\resources\\templates\\doc\\dc01\\module";
    	//aiService.translateOneFile(folder,"4g_zh.html","4g_en.html",target);
    	//aiService.translateOneFile(folder,"batadc_zh.html","batadc_en.html",target);
    	//aiService.translateOneFile(folder,"camera_zh.html","camera_en.html",target);
    	//aiService.translateOneFile(folder,"cloudlog_zh.html","cloudlog_en.html",target);
    	//aiService.translateOneFile(folder,"config_zh.html","config_en.html",target);
    	//aiService.translateOneFile(folder,"forward_zh.html","forward_en.html",target);
    	//aiService.translateOneFile(folder,"general_zh.html","general_en.html",target);
    	//aiService.translateOneFile(folder,"gpio_zh.html","gpio_en.html",target);
    	//aiService.translateOneFile(folder,"i2c_zh.html","i2c_en.html",target);
    	//aiService.translateOneFile(folder,"lora_zh.html","lora_en.html",target);
    	//aiService.translateOneFile(folder,"meta_zh.html","meta_en.html",target);
    	//aiService.translateOneFile(folder,"rs485_zh.html","rs485_en.html",target);
    	//aiService.translateOneFile(folder,"spi_zh.html","spi_en.html",target);
    	//aiService.translateOneFile(folder,"spi_zh.html","spi_en.html",target);
    	//aiService.translateOneFile(folder,"spl06_zh.html","spl06_en.html",target);
    	//aiService.translateOneFile(folder,"tm7705_zh.html","tm7705_en.html",target);
    	//aiService.translateOneFile(folder,"uart_zh.html","uart_en.html",target);
    	
    	//String folder = "D:\\project\\datacollection\\cloud\\dc01\\main\\src\\main\\resources\\templates\\doc";
    	//aiService.translateOneFile(folder,"onlinecoding_zh.html","onlinecoding_en.html",target);
    	//aiService.translateOneFile(folder,"ota_zh.html","ota_en.html",target);
    	//aiService.translateOneFile(folder,"user_zh.html","user_en.html",target);

    	//String folder = "D:\\project\\datacollection\\cloud\\dc01\\main\\src\\main\\resources\\templates\\doc\\dc01";
    	//aiService.translateOneFile(folder,"api_zh.html","api_en.html",target);
    	//aiService.translateOneFile(folder,"dataflow_zh.html","dataflow_en.html",target);
	}
    public void translateOneFile(String folder,String nameFrom,String nameTo,String target) 
    {
        // API URL
        String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

        // API Key (请替换为你的实际API Key)
        String apiKey = "sk-12a8958dccad4fd4924bdbb4b353c5f2";
        
        // 读取本地HTML文件内容

        String filePath = folder+"\\"+nameFrom;
        String content = readFileContent(filePath);

        if (content == null || content.isEmpty()) {
        	logger.info("Failed to read the file.");
            return;
        }
        
        // JSON 请求体
        String jsonBody = "{"
                + "\"model\": \"qwen-omni-turbo\","
                + "\"messages\": ["
                + "    {\"role\": \"user\", \"content\": \""+target+ escapeJson(content) + "\"}"
                + "],"
                + "\"stream\": true,"
                + "\"stream_options\": {"
                + "    \"include_usage\": true"
                + "},"
                + "\"modalities\": [\"text\"]"
                + "}";

        // 创建 WebClient 实例
        WebClient webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();

        // 发起 POST 请求并处理流式响应
        StringBuilder fullResponse = new StringBuilder();
        webClient.post()
                .bodyValue(jsonBody) // 设置请求体
                .retrieve()          // 获取响应
                .bodyToFlux(String.class) // 流式处理响应数据
                .doOnNext(chunk -> {
                	//System.out.println(chunk);
                    if (chunk.startsWith("{\"choices")) {                       
                        try {
                            String deltaContent = parseDeltaContent(chunk);
                            if ((deltaContent != null)&&(!deltaContent.startsWith("```"))) {
                            	System.out.print(".");
                                fullResponse.append(deltaContent); // 合并内容
                            }
                        } catch (Exception e) {
                        	logger.info("Failed to parse chunk: " + chunk);
                        }
                    }
                })
                .doOnError(error -> logger.info("Error: " + error.getMessage()))
                .blockLast(); // 等待所有数据接收完成

        logger.info("\r\n");
        // 如果有响应数据，解析并保存到文件
        if (fullResponse.length() > 0) {
            try {
                Path filePath2 = Paths.get(folder, nameTo);
                removeFirstAndLastLine(fullResponse);
                Files.writeString(filePath2, fullResponse.toString());
                logger.info(fullResponse.toString());
                logger.info("Response saved to file: " + filePath2.toString());
            } catch (IOException e) {
                logger.info("Failed to write response to file: " + e.getMessage());
            }
        } else {
            logger.info("No response received from the API.");
        }
    }
    /**
     * 读取文件内容
     */
    private String readFileContent(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
        	logger.info("Failed to read file: " + e.getMessage());
            return null;
        }
        return contentBuilder.toString();
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    /**
     * 从 JSON 分块中提取 delta.content
     */
    private static String parseDeltaContent(String jsonData) throws Exception {
        // 使用 Jackson 解析 JSON
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(jsonData);

        // 提取 delta.content 字段
        JsonNode choicesNode = jsonNode.at("/choices/0/delta/content");
        if (choicesNode != null && !choicesNode.isNull()) {
            return choicesNode.asText();
        }
        return null;
    }
    public void removeFirstAndLastLine(StringBuilder fullResponse) {
        // 查找第一个换行符的位置，这将是第一行的末尾（如果存在的话）
        int firstNewLineIndex = fullResponse.indexOf("\n");
        if (firstNewLineIndex == -1) {
            // 如果没有找到换行符，说明只有一行或者根本没有内容
            fullResponse.setLength(0); // 清空整个 StringBuilder
            return;
        }

        // 删除第一行以及其后的换行符
        fullResponse.delete(0, firstNewLineIndex + 1);

        // 查找最后一个换行符的位置，这将是最后一行的开始之前
        int lastNewLineIndex = fullResponse.lastIndexOf("\n");
        if (lastNewLineIndex != -1) {
            // 删除最后一个换行符及其后的内容（即最后一行）
            fullResponse.delete(lastNewLineIndex, fullResponse.length());
        } else {
            // 如果找不到最后一个换行符，说明只剩一行了，清空 StringBuilder
            fullResponse.setLength(0);
        }
    }
}
