package com.zyc.dc.service;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zyc.dc.dao.Login;
import com.zyc.dc.dao.UserModel;
import com.zyc.dc.dao.UserSessionModel;
import com.zyc.dc.util.CacheUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Service
public class MiscUtil {
	@Autowired
	MongoDBService mongoDBService;
    @Autowired
	private ConfigProperties configProperties;
    private static final Logger logger = LoggerFactory.getLogger(MiscUtil.class);
	private final static BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
	
    private static final String EMAIL_REGEX = "^[\\w-\\.]+@[\\w-]+\\.[a-zA-Z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\W).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final String IPV4_REGEX = "^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\." +
            "(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private static final String DOMAIN_REGEX = "^(?!-)[A-Za-z0-9-]{1,63}(?<!-)\\." +
            "([A-Za-z]{2,})$";
    private static final Pattern DOMAIN_PATTERN=Pattern.compile(DOMAIN_REGEX);


    
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final String ALL_CHARACTERS = LOWER + UPPER + DIGITS + SPECIAL;
    private static final String CHARACTERS = "0123456789"; // 只包含数字
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    

    public static byte[] base64Decode(String data)
    {
    	if(data==null)
    		return null;
    	return Base64.getDecoder().decode(data);
    }
    public static String base64Encode(byte[] data)
    {
    	if(data==null)
    		return null;
    	return Base64.getEncoder().encodeToString(data);
    }
    public static String camelCase(String snakeCase) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true; 
            } else {
                if (nextUpperCase) {
                    camelCase.append(Character.toUpperCase(c)); 
                    nextUpperCase = false;  
                } else {
                    camelCase.append(Character.toLowerCase(c));  
                }
            }
        }
        
        return camelCase.toString();
    }
    public static Map<String,Object> camelCaseMap(Map<String,Object> map)
    {
    	if(map==null)
    		return null;
    	Map<String,Object> temp=new HashMap<>();
    	for(String key:map.keySet())
    		temp.put(camelCase(key), map.get(key));
    	return temp;
    }
    public static String URLEncode(String input) {
    	if(input==null)
    		return null;
        try {
            String encoded = URLEncoder.encode(input, "UTF-8");
            return encoded;
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static boolean isTimeNotCurrentDay(Instant time)
    {
    	if((time==null)||((time.getEpochSecond()/(3600*24))!=(Instant.now().getEpochSecond()/(3600*24))))
    		return true;
    	return false;
    }
    public static boolean isTimeNotCurrentMinute(Instant time)
    {
    	if((time==null)||((time.getEpochSecond()/60)!=(Instant.now().getEpochSecond()/60)))
    		return true;
    	return false;
    }
    public static String URLDecode(String input) {
    	if(input==null)
    		return null;
        try {
            String encoded = URLDecoder.decode(input, "UTF-8");
            return encoded;
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static boolean URLValid(String url,boolean hasHost) 
    {
    	try {
            URI uri = new URI(url); 
            URL parsedUrl = uri.toURL();  
            return (!hasHost)||(parsedUrl.getHost() != null && !parsedUrl.getHost().isEmpty());
        } catch (Exception e) {
            return false;  
        }
    }
    public static String passwordNew() {
    	int length=8;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Ensure at least one character from each category is included
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill the remaining length with random characters from all categories
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARACTERS.charAt(random.nextInt(ALL_CHARACTERS.length())));
        }
        return password.toString();
    }

    public static String randomNumber(int length) {
        StringBuilder randomNumber = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(CHARACTERS.length());
            randomNumber.append(CHARACTERS.charAt(index));
        }
        return randomNumber.toString();
    }
    
    public static String tokenCreate()
    {
    	return MiscUtil.MD5(UUID.randomUUID().toString().toLowerCase());
    }
    public static boolean passwordValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    public static boolean emailValid(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    public static String clientGetIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            String[] addresses = xForwardedFor.split(",");
            for (String addr : addresses) {
                String trimmedIp = addr.trim();
                if (!trimmedIp.isEmpty()) {
                    return trimmedIp;
                }
            }
        }
        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr.startsWith("::1")||remoteAddr.startsWith("0:0")) {
            return "127.0.0.1"; 
        }
        return remoteAddr;
    }
	public static String  bytesToString(byte[] data) {
        String decodedString = new String(data, StandardCharsets.UTF_8);
        byte[] reEncodedBytes = decodedString.getBytes(StandardCharsets.UTF_8);
        boolean isSame = Arrays.equals(data, reEncodedBytes);
        if(isSame)
        	return decodedString;
        
        return "";      
    }
	public static boolean objectHasMethod(Object object,String methodName) 
	{
        try {
            Method method = object.getClass().getMethod(methodName);
            if(method!=null)
            	return true;
        } catch (Exception e) {
            //logger.error(e.getMessage(),e);
        }
        return false;
	}
	public static Object objectCall(Object object,String methodName) 
	{
        try {
            Method method = object.getClass().getMethod(methodName);
            Object result = method.invoke(object);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
	}
	public static Object objectCall(Object object, String methodName, Object... params) {
	    try {
	        Class<?>[] paramTypes = new Class[params.length];
	        for (int i = 0; i < params.length; i++) {
	            paramTypes[i] = params[i].getClass();
	        }
	        Method method = object.getClass().getMethod(methodName, paramTypes);
	        Object result = method.invoke(object, params);
	        return result;
	    } catch (Exception e) {
	        logger.error(e.getMessage(), e);
	    }
	    return null;
	}   
	public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
    public static JsonNode jsonObject2Node(Object object) {
        try {
        	JsonNode modifiedJson =objectMapper.valueToTree(object);
            return modifiedJson;
        } catch (Exception e) {
        	//logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static <T> T jsonNode2Object(TreeNode node,Class<T> targetType) {
        try {
        	T object =objectMapper.treeToValue(node, targetType);
            return object;
        } catch (Exception e) {
        	//logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static String jsonObject2String(Object object) {
    	if(object==null)
    		return "";
        try {
            String jsonString = objectMapper.writeValueAsString(object);
            return jsonString;
        } catch (JsonProcessingException e) {
        	//logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static <T> T jsonStr2ClassType(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
        	//logger.error(e.getMessage(),e);
        }
        return null; 
    }
    public static <T> T jsonStr2ClassType(String jsonString, TypeReference<T> typeRef) {
        try {
            return objectMapper.readValue(jsonString, typeRef);
        } catch (Exception e) {
        	//logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static Object jsonStr2Object(String jsonString) {
        try {
        	Object list = objectMapper.readValue(jsonString,Object.class);
            return list;
        } catch (JsonProcessingException e) {
        	//logger.error(e.getMessage(),e);
        }
        return null;
   }
   public static JsonNode jsonStr2JsonNode(String jsonString) {
        try {
        	JsonNode rootNode = objectMapper.readTree(jsonString);
            return rootNode;
        } catch (JsonProcessingException e) {
        	//logger.error(e.getMessage(),e);
        }
        return null;
   }
   public static List<Map<String,Object>> resultPutRow(List<Map<String,Object>> result,Object... values)
   {
	   if(result==null)
		   result=new ArrayList<Map<String,Object>>();
   		Map<String,Object> row=new HashMap<>();
   		for(int i=0;(i+1)<values.length;i+=2)
   		{
   			if(values[i+1]==null)
   				values[i+1]="";
   			else if(values[i+1] instanceof Integer)
   				values[i+1]=values[i+1].toString();
   			row.put(values[i].toString(), values[i+1].toString());
   		}
   		result.add(row);
   		return result;
   }
   
   public static byte[] hexToBytes(String hexString) {
	   byte[] data=null;
	   try {
        int len = hexString.length();
        data= new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
	   }catch(Exception e)
	   {
		   logger.error("hexToBytes error with "+hexString,e);
	   }
        return data;
    }
    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b); 
            if (hex.length() == 1) {
                hexString.append('0'); 
            }
            hexString.append(hex);
        }
        return hexString.toString().toLowerCase();
    }
    public static String stringTrim(String str,String trimStart,String trimEnd)
    {
    	if((trimStart!=null)&&(str.startsWith(trimStart)))
    		str=str.substring(trimStart.length(),str.length());
    	if((trimEnd!=null)&&(str.endsWith(trimEnd)))
    		str=str.substring(0,str.length()-trimEnd.length());
    	return str;
    }

	public static byte[] bytesRightTrim(byte[] data,int value)
	{
		int i=data.length;
		while(i>0)
		{
			if(data[i-1]!=value)
				break;
			i--;
		}
		return Arrays.copyOfRange(data, 0, i);
	}
	
	public static int byteToUint8(byte value)
	{
		int uint8 = value & 0xFF;
		return uint8;
	}
	/*小端，低字节在后*/
    public static int bytesToUint16LittleEndian(byte[] bytes) {
        // byte数组中序号小的在右边
        return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8);
    }
    
	/*小端，低字节在后*/
    public static int bytesToIntLittleEndian(byte[] bytes) {
        // byte数组中序号小的在右边        
        Long ret=((long) (bytes[0] & 0xFF)) | 
        ((long) (bytes[1] & 0xFF) << 8) | 
        ((long) (bytes[2] & 0xFF) << 16) | 
        ((long) (bytes[3] & 0xFF) << 24);
        return ret.intValue();
    }
	/*小端，低字节在后*/
    public static long bytesToUint32LittleEndian(byte[] bytes) {
        // byte数组中序号小的在右边
        return ((long) (bytes[0] & 0xFF)) | 
                ((long) (bytes[1] & 0xFF) << 8) | 
                ((long) (bytes[2] & 0xFF) << 16) | 
                ((long) (bytes[3] & 0xFF) << 24);
    }
	/*小端，低字节在后*/
    public static long bytesToUint32LittleEndianFlex(byte[] bytes) {
        // byte数组中序号小的在右边
    	long ret=0;
    	for(int i=0;i<bytes.length;i++)
    		ret=ret | ((long) (bytes[i] & 0xFF) <<(8*i));
    	return ret;
    }
    public static byte[] uint322BytesLittleEndian(long n) {
	  	  byte[] b = new byte[4];  
	  	  b[0] = (byte) (n & 0xff);  
	  	  b[1] = (byte) (n >> 8 & 0xff);  
	  	  b[2] = (byte) (n >> 16 & 0xff);  
	  	  b[3] = (byte) (n >> 24 & 0xff);  
	  	  return b;  
    }
    public static byte[] uint642BytesLittleEndian(long n) {
	  	  byte[] b = new byte[8];  
	  	  b[0] = (byte) (n & 0xff);  
	  	  b[1] = (byte) (n >> 8 & 0xff);  
	  	  b[2] = (byte) (n >> 16 & 0xff);  
	  	  b[3] = (byte) (n >> 24 & 0xff);  
	  	  b[4] = (byte) (n >> 32 & 0xff);  
	  	  b[5] = (byte) (n >> 40 & 0xff);  
	  	  b[6] = (byte) (n >> 48 & 0xff);  
	  	  b[7] = (byte) (n >> 56 & 0xff);  
	  	  return b;  
  }
    public static byte[] uint162BytesLittleEndian(int n) {
	  	  byte[] b = new byte[2];  
	  	  b[0] = (byte) (n & 0xff);  
	  	  b[1] = (byte) (n >> 8 & 0xff);  
	  	  return b;  
    }
    public static byte[] uint82BytesLittleEndian(int n) {
	  	  byte[] b = new byte[1];  
	  	  b[0] = (byte) (n & 0xff);  
	  	  return b;  
    }

	@SuppressWarnings("unchecked")
	public static <T> T webParamsGet(Map<String, Object> params, String key, Class<T> returnType, T defaultValue) {
        if (params == null || params.isEmpty()) {
            return defaultValue;
        }
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (returnType.isInstance(value)) {
            return (T) value;
        }
        if (returnType == Integer.class) {
            if (value instanceof String) {
                try {
                    return (T) Integer.valueOf((String) value);
                } catch (NumberFormatException e) {
                    // Handle conversion failure
                    return defaultValue;
                }
            }
            if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
        }if (returnType == Long.class) {
            if (value instanceof String) {
                try {
                    return (T) Long.valueOf((String) value);
                } catch (NumberFormatException e) {
                    // Handle conversion failure
                    return defaultValue;
                }
            }
            if (value instanceof Number) {
            	return (T) Long.valueOf(((Number) value).longValue());
            }
        }else if (returnType == String.class) {
            if (value instanceof Integer) {
                return (T) ((String) value).toString();
            }
            if (value instanceof Number) {
                return (T) (((Number) value).intValue()+"");
            }
        }
        return defaultValue;
    }
    public static String MD5(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }
    public static String MD5(String input,String key) {
    	input=input+key;
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }
	public static String MD5(byte[] btInput,String key) 
	{
        char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};       
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            if((key!=null)&&(key.length()>0))
            	mdInst.update(key.getBytes());
            // 获得密文
            byte[] md = mdInst.digest();
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
            return null;
        }
    }
	public static byte[] MD5Bytes(byte[] btInput,String key) 
	{      
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            mdInst.update(btInput);
            mdInst.update(key.getBytes());
            // 获得密文
            byte[] md = mdInst.digest();
            return md;
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
            return null;
        }
    }
	public static byte[] MD5(Long[] dataU32,byte[] btInput,String key) 
	{
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要
            for(int i=0;i<dataU32.length;i++)
            	mdInst.update(MiscUtil.uint322BytesLittleEndian(dataU32[i]));
            mdInst.update(btInput);
            mdInst.update(key.getBytes());
            // 获得密文
            byte[] md = mdInst.digest();
            return md;
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
            return null;
        }
    }
    //"MM-dd HH:mm:ss"
    public static String dateFormat(Instant cur,String format) {
    	if(cur==null)
    		return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
        return formatter.format(cur);
    }
    public static Instant dateSelectNew(Instant date1, Instant date2) {
        if (date1 == null && date2 == null) {
            return null;  // 如果两个日期都是 null，返回 null
        }
        if (date1 == null) {
            return date2;  // 如果 date1 为 null，返回 date2
        }
        if (date2 == null) {
            return date1;  // 如果 date2 为 null，返回 date1
        }
        return date1.isAfter(date2) ? date1 : date2;  // 比较两个日期，返回较晚的一个
    }
    //"yyyy-MM-dd'T'HH:mm:ss"
    public static Instant dateParse(String date,String format) {
    	if((date==null)||(date.length()==0))
    		return null;
    	if(format==null)
    		return Instant.parse(date);
    	Instant instant=null;
    	try {
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);       
    		LocalDateTime dateTime = LocalDateTime.parse(date, formatter);
        	instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
    	}catch(Exception e)
    	{
    		//logger.error(e.getMessage(),e);
    	}
        return instant;
    }
    public static Instant dateToday(int hour,int minute,int second) {
        Instant originalInstant = Instant.now();
        ZonedDateTime zonedDateTime = originalInstant.atZone(ZoneId.systemDefault());
        ZonedDateTime endOfDay = zonedDateTime.toLocalDate().atTime(hour, minute, second).atZone(zonedDateTime.getZone());
        return endOfDay.toInstant();
    }
    public static Instant dateTrim(Instant date,int hour,int minute,int second) {
        ZonedDateTime zonedDateTime = date.atZone(ZoneId.systemDefault());
        ZonedDateTime endOfDay = zonedDateTime.toLocalDate().atTime(hour, minute, second).atZone(zonedDateTime.getZone());
        return endOfDay.toInstant();
    }
    public static Long dateDiff(Instant start,Instant end)
    {
        Duration duration = Duration.between(start, end);
        Long seconds= duration.toSeconds();
        return seconds;
    }
    public static Long strParseLong(String value)
    {
 	   if((value==null)||(value.length()==0))
 		   return null;
 	   int digit=10;
 	   value=value.trim().toLowerCase();
 	   if(value.startsWith("0x"))
 	   {
 		   digit=16;
 		   value=value.substring(2);
 	   }
 	   try {
 		   return Long.parseLong(value,digit);
 	   }catch(Exception e)
 	   {
 		  //logger.error(e.getMessage(),e);
 	   }
 	   return null;
    }
    public static Integer strParseInteger(String value)
    {
 	   if((value==null)||(value.length()==0))
 		   return null;
 	   int digit=10;
 	   value=value.trim().toLowerCase();
 	   if(value.startsWith("0x"))
 	   {
 		   digit=16;
 		   value=value.substring(2);
 	   }
 	   try {
 		   return Integer.parseInt(value,digit);
 	   }catch(Exception e)
 	   {
 		   logger.error(e.getMessage(),e);
 	   }
 	   return null;
    }
    public static boolean strIsNull(String data)
	{
		if((data==null)||(data.length()==0)||(data.equalsIgnoreCase("null"))||(data.equalsIgnoreCase("undefined")))
			return true;
		return false;
	}
	public static boolean strNotNull(String data)
	{
		return !strIsNull(data);
	}
	public static String strLimit(String data,String suffix,int limit)
	{
		if(data==null)
			return "";
		if(data.length()<=limit)
			return data;
		data=data.substring(0,limit);
		if(suffix!=null)
			data=data+suffix;
		return data;
	}
	public static String strDisplay(Object data)
	{
		if(data==null)
			return "";
		if(data instanceof Integer)
			return data.toString();
		if(data instanceof Long)
			return data.toString();
		if(data instanceof String) {
			if(((String)data).length()==0)
				data="";
			return data.toString().trim();
		}
		return data.toString();
	}
	public static boolean listIsNull(List<Object> data)
	{
		if((data==null)||(data.size()==0))
			return true;
		return false;
	}
	public static boolean listNotNull(List<Object> data)
	{
		return (!listIsNull(data));
	}
	public static boolean mapIsNull(Map<Object,Object> data)
	{
		if((data==null)||(data.size()==0))
			return true;
		return false;
	}
	public static boolean mapNotNull(Map<Object,Object> data)
	{
		return (!mapIsNull(data));
	}
	public static String userPasswordEncode(String password)
	{
		return bCryptPasswordEncoder.encode(password);
	}
	public static boolean userPasswordMatch(String passwordcrypt,String password)
	{
		if((passwordcrypt==null)||(passwordcrypt.length()==0)||(password==null)||(password.length()==0))
			return false;
		return bCryptPasswordEncoder.matches(password, passwordcrypt);
	}
	public void loginCookieSet(UserSessionModel userSession,HttpServletResponse response,Instant now)
	{
		int cookieSeconds=MiscUtil.dateDiff(now,userSession.getExpiredTime()).intValue();
		if(!userSession.isRemember())
			cookieSeconds=-1;
		Cookie cookie1 = new Cookie("usersecret", userSession.getId());
		cookie1.setPath("/");				
		cookie1.setMaxAge(cookieSeconds);
		cookie1.setHttpOnly(true);
		response.addCookie(cookie1);
		cookie1 = new Cookie("username", userSession.getLogin());
		cookie1.setPath("/");				
		cookie1.setMaxAge(cookieSeconds);
		cookie1.setHttpOnly(true);
		response.addCookie(cookie1);
	}
	public void logout(HttpServletResponse response)
	{
		Cookie cookie1 = new Cookie("usersecret", null);
		cookie1.setPath("/");				
		cookie1.setMaxAge(0);
		cookie1.setHttpOnly(true);
		response.addCookie(cookie1);
		cookie1 = new Cookie("username", null);
		cookie1.setPath("/");				
		cookie1.setMaxAge(0);
		cookie1.setHttpOnly(true);
		response.addCookie(cookie1);
		Login login=CacheUtil.threadlocallogin.get();
		if(login!=null)
			mongoDBService.delete("id", login.getUserSecret(), UserSessionModel.class);
	}
	//[pass,userModel]
	public Object[] loginMatch(HttpServletRequest request,HttpServletResponse response,String login,String password,String remember,String secret,Instant now)
	{
		//logger.info(request.getRequestURI());
		Object[] ret=new Object[2];
		ret[0]=false;
		if(((login==null)||(login.length()==0)||(password==null)||(password.length()==0))&&((secret==null)||(secret.length()==0)))
			return ret;
		
		if((secret!=null)&&(login!=null)) //check session
		{
			UserSessionModel userSession=mongoDBService.findOneByField("id", secret, UserSessionModel.class);
			if((userSession!=null)&&(userSession.getLogin()!=null)&&(userSession.getLogin().equals(login)))
			{
				if(now.getEpochSecond()<userSession.getExpiredTime().getEpochSecond())
				{
					UserModel userModel=mongoDBService.findOneByField("id", userSession.getUserId(), UserModel.class);
					ret[1]=userModel;
					if((userModel!=null)&&(userModel.getUserStatus()!=UserModel.UserStatus.WAIT_ACTIVATION)&&(userModel.getUserStatus()!=UserModel.UserStatus.FREEZED)&&(userModel.getUserStatus()!=UserModel.UserStatus.DISABLED)&&(userModel.getLogin().equals(userSession.getLogin()))&&(userModel.getPassword().equals(userSession.getPassword())))
					{
						userSession.setLastReadTime(now);
						userSession.setLastWriteTime(now);
						userSession.setLogin(login);
		    			if(userSession.isRemember())
		    				userSession.setExpiredTime(now.plusSeconds(configProperties.COOKIE_DEFAULT_TIME()*24*60));
		    			else
		    				userSession.setExpiredTime(now.plusSeconds(configProperties.SESSION_NON_REMEMBER_TIME()*24*60));
						mongoDBService.save("loginmatch1",userSession);
						loginCookieSet(userSession,response, now);
						userModel.setLatestVisitTime(now);
						userModel.setLatestIp(MiscUtil.clientGetIp(request));
						userModel.setLocale(LocaleContextHolder.getLocale().getLanguage() + "-" + LocaleContextHolder.getLocale().getCountry());
						
						mongoDBService.save("loginmatch2",userModel);
						ret[0]=true;
						return ret;
					}
				}
			}
		}
		if((login!=null)&&(password!=null))//check password
		{
			boolean result=false;
	    	UserModel userModel=mongoDBService.findOneByField("login", login, UserModel.class);
			ret[1]=userModel;
	    	if((userModel!=null)&&(userModel.getUserStatus()!=UserModel.UserStatus.WAIT_ACTIVATION)&&(userModel.getUserStatus()!=UserModel.UserStatus.FREEZED)&&(userModel.getUserStatus()!=UserModel.UserStatus.DISABLED)&&(userModel!=null)&&(userModel.getPassword()!=null)&&(userModel.getPassword().length()>0)) 
	    	{
	    		if(MiscUtil.userPasswordMatch(userModel.getPassword(), password))//match
	    		{
	    			userModel.setLoginTime(now);
	    			userModel.setLoginTimes(userModel.getLoginTimes()==null?1:userModel.getLoginTimes()+1);
	    			userModel.setLatestVisitTime(now);
					userModel.setLatestIp(MiscUtil.clientGetIp(request));
	    			result=true;
	    		}else//not match
	    		{
	    			userModel.setLoginFailedTime(now);
	    			userModel.setLoginFailedTimes(userModel.getLoginFailedTimes()==null?1:userModel.getLoginFailedTimes()+1);
	    		}
				userModel.setLocale(LocaleContextHolder.getLocale().getLanguage() + "-" + LocaleContextHolder.getLocale().getCountry());
	    		mongoDBService.save("loginmatch3",userModel);
	    		if(result)
	    		{
	    			secret=UUID.randomUUID().toString().toLowerCase();
	    			UserSessionModel userSession=new UserSessionModel();
	    			if(remember.equals("on"))
	    				userSession.setRemember(true);
	    			userSession.setUserId(userModel.getId());
	    			userSession.setId(secret);
	    			userSession.setCreateTime(now);
	    			userSession.setLastReadTime(now);
	    			userSession.setLastWriteTime(now);
	    			userSession.setPassword(userModel.getPassword());
	    			userSession.setLogin(login);
	    			if(userSession.isRemember())
	    				userSession.setExpiredTime(now.plusSeconds(configProperties.COOKIE_DEFAULT_TIME()*24*60));
	    			else
	    				userSession.setExpiredTime(now.plusSeconds(configProperties.SESSION_NON_REMEMBER_TIME()*24*60));
	    			mongoDBService.save("loginmatch4",userSession);
	    			loginCookieSet(userSession,response, now);
	    			ret[0]=true;
	    			return ret;
	    		}
	    	}
		}
		return ret;
	}
    public static boolean ipV4OrDomainValid(String ip) {
    	if(ip==null)
    		return false;
    	return (IPV4_PATTERN.matcher(ip).matches()||DOMAIN_PATTERN.matcher(ip).matches());
    }
    public static String ipNumber2String(long ip) {
        // 将 long 转换为 4 个字节
        return String.format("%d.%d.%d.%d",
        		
        		(ip & 0xFF),
        		(ip >> 8) & 0xFF,
        		(ip >> 16) & 0xFF,
                (ip >> 24) & 0xFF);

    }
    public static boolean ipV4Valid(String ip) {
    	if(ip==null)
    		return false;
    	return IPV4_PATTERN.matcher(ip).matches();
    }
    public static String ipV6Number2String(List<Long> addrLongs, int zone) {
        // 转换为 IPv6 的 16 字节（128 位）二进制形式
        byte[] ipv6Bytes = new byte[16];
        for (int i = 0; i < 4; i++) {
            long value = addrLongs.get(i);
            ipv6Bytes[i * 4] = (byte) ((value >> 24) & 0xFF);
            ipv6Bytes[i * 4 + 1] = (byte) ((value >> 16) & 0xFF);
            ipv6Bytes[i * 4 + 2] = (byte) ((value >> 8) & 0xFF);
            ipv6Bytes[i * 4 + 3] = (byte) (value & 0xFF);
        }

        // 将二进制形式转换为 IPv6 字符串表示
        try {
        	String ipv6Address = java.net.Inet6Address.getByAddress(ipv6Bytes).getHostAddress();
            String zoneString = (zone != 0) ? "%" + zone : ""; // 如果 zone 非零，添加 zone
            return ipv6Address+zoneString;
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
        }
        return null;
    }
    public static String cookieGet(Cookie[] cookies,String name)
    {
    	if((cookies!=null)&&(cookies.length>0))
    	{
    		for(Cookie cookie:cookies)
    		{
    			if(cookie.getName().equals(name))
    				return cookie.getValue();
    		}
    	}
    	return null;
    }
    public static String int2Float(Integer data, Integer factor, String format) {
    	if(data==null)
    		return "";
        float result = (float) data / factor;
        return String.format(format, result);
    }
}
