package com.zyc.dc.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.zyc.dc.dao.ApiKeyModel;
import com.zyc.dc.dao.DataUserLogModel;
import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.util.CacheUtil;

import jakarta.annotation.PostConstruct;

import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Sort;

@Service
public class MongoDBService {
	private static final Logger logger = LoggerFactory.getLogger(MongoDBService.class);
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ConfigProperties configProperties;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    
    public void log(String userId,String deviceId,String url, Map<String, Object> params,String comments,Object oldValue,Object newValue)
    {
    	if((userId==null)&&(CacheUtil.threadlocallogin.get()!=null))
    		userId=CacheUtil.threadlocallogin.get().getUserId();
    	String paramsStr=null;
    	if(params!=null)
    	{
    		paramsStr=params.entrySet()
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())  
            .collect(Collectors.joining(" ")); 
    	}
    	DataUserLogModel log=new DataUserLogModel();
    	log.setComments(comments);
    	log.setCreateTime(new Date());
    	log.setDeviceId(deviceId);
    	log.setUserId(userId);
    	log.setOldValue(oldValue);
    	log.setNewValue(newValue);
    	log.setUrl(url);
    	log.setParams(paramsStr);
    	mongoTemplate.save(log);
    }
    public <T> long count(Class<T> entityClass, Object... conditions) 
    {
    	Query query = new Query();
        Criteria criteria = new Criteria();
        for (int i=0;i<conditions.length;i+=2) {
            if (conditions[i+1] instanceof String)
            	criteria = criteria.and(conditions[i].toString()).is(conditions[i+1].toString());
            else if (conditions[i+1] instanceof Number)
            	criteria = criteria.and(conditions[i].toString()).is(conditions[i+1]);
        }
        query.addCriteria(criteria);
        return mongoTemplate.count(query,entityClass);
    }
    public long count(Criteria criteriaQuery, Class<?> entityClass) {
        Query query = new Query();
        if(criteriaQuery!=null)
        	query.addCriteria(criteriaQuery);
        return mongoTemplate.count(query, entityClass);
    }

    public <T> UpdateResult retainFields(T entity,List<String> retainFields) {
    	if(entity==null)
    		return null;
    	String id=MiscUtil.objectCall(entity, "getId").toString(); 
    	Field[] fields = entity.getClass().getDeclaredFields(); 	 
    	Query query = new Query(Criteria.where("_id").is(id));
    	Update update = new Update();
        for (Field field : fields) 
        {
            //field.setAccessible(true);
            String fieldName = field.getName();
            if (!retainFields.contains(fieldName)) 
            	update.unset(fieldName);     
        }
        return mongoTemplate.updateFirst(query, update, entity.getClass());
    }
    public String fileSave(Integer expireSeconds, String contentType, String filename, byte[] data, Object... params) {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {            
        	return fileSave(expireSeconds,contentType,filename,inputStream,params);
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
		}
        return null;
    }
    public String fileSave(Integer expireSeconds, String contentType, String filePath, Object... params) {
        File file = new File(filePath);
        if (!file.exists()) {
        	logger.error("file not exist "+filePath);
        	return null;
        }
        try (InputStream inputStream = new FileInputStream(file)) {
        	return fileSave(expireSeconds,contentType,file.getName(), inputStream,params);
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
		}
        return null;
    }

    private String fileSave(Integer expireSeconds, String contentType, String filename, InputStream data, Object... params) {
        try {
            Document metadata = new Document();
            if (expireSeconds == null) {
                metadata.append("isPermanent", true);
            } else {
                metadata.append("isPermanent", false);
                Date expireAt = new Date(System.currentTimeMillis() +expireSeconds * 1000L);
                metadata.append("expiredTime", expireAt);
            }
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i += 2) {
                    Object key = params[i];
                    Object value = params[i + 1];
                    metadata.append(key.toString(), value);
                }
            }
            return gridFsTemplate.store(data, filename, contentType, metadata).toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    public void fileDelById(String fileId) {
        ObjectId fileObjectId = new ObjectId(fileId);
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(fileObjectId));
        gridFsTemplate.delete(query);
    }
    public void fileDelExpired() {
    	Date currentDate = new Date();
        Criteria criteria = Criteria.where("metadata.expiredTime").lt(currentDate);
        gridFsTemplate.delete(Query.query(criteria));
    }
    public byte[] fileReadByte(String fileId) {
	   	 GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
	   	 if (gridFsFile != null)
	   	 {
	   		GridFsResource resource= gridFsTemplate.getResource(gridFsFile);
	        try {
				return StreamUtils.copyToByteArray(resource.getContent());
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			} 
	   	 }
         return null;
    }
    public GridFsResource fileReadStream(String fileId) {
	   	 GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
	   	 if (gridFsFile != null)
	   	 {
	   		return gridFsTemplate.getResource(gridFsFile);
	   	 }
        return null;
   }
    public GridFsResource  imageReadStream(String fileId) {
	   	 GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
	   	 if (gridFsFile != null)
	   	 {
	   		return  gridFsTemplate.getResource(gridFsFile);
	   	 }
        return null;
   }

    public <T> Collection<T> insertAll(List<T> entityList) {
        return mongoTemplate.insertAll(entityList);
    }
    public <T> List<T> find(Map<String,Object> conditionMap, Sort sort, Class<T> entityClass, int startRecord, int endRecord, List<String> fieldsToExclude) 
    {
        Query query = new Query();
        
        Object startTime = conditionMap.get("startTime");
        Object endTime = conditionMap.get("endTime");

        // 遍历条件映射
        for (String field : conditionMap.keySet()) {
            if (!field.equals("startTime") && !field.equals("endTime")) {
                Object value = conditionMap.get(field);
                query.addCriteria(Criteria.where(field).is(value));
            }
        }

        // 添加时间条件
        if (startTime != null && endTime != null) {
            query.addCriteria(Criteria.where("uploadTime").gte(startTime).lte(endTime));
        } else if (startTime != null) {
            query.addCriteria(Criteria.where("uploadTime").gte(startTime));
        } else if (endTime != null) {
            query.addCriteria(Criteria.where("uploadTime").lte(endTime));
        }

        // 添加排序
        query.with(sort);

        if (fieldsToExclude != null && !fieldsToExclude.isEmpty()) {
            for (String field : fieldsToExclude) {
                query.fields().exclude(field);
            }
        }
        
        // 计算要获取的记录数
        int limit = endRecord - startRecord;

        // 添加分页条件：skip 和 limit
        query.skip(startRecord).limit(limit);

        return mongoTemplate.find(query, entityClass);
    }
    public <T> List<T> findByFieldIn(String fieldName,List<String> fieldValues, Class<T> entityClass) {
        Query query = new Query(Criteria.where(fieldName).in(fieldValues));
        return mongoTemplate.find(query, entityClass);
    }
    public <T> List<T> findAll(Class<T> entityClass) {
        Query query = new Query();
        return mongoTemplate.find(query, entityClass);
    }
    public <T> List<T> findByField(Query query, String sortField,List<String> excludeFields, Boolean ascending, Integer limit,Class<T> entityClass) 
    {
    	if((sortField!=null)&&(ascending!=null)) 
    	{
    		Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
        	query.with(sort);
    	}

        if(excludeFields!=null)
        {
        	for(String field:excludeFields)
        		query.fields().exclude(field);
        }
        if(limit!=null)
        	query.limit(limit);
        return mongoTemplate.find(query, entityClass);
    }
    public <T> List<T> findByField(String fieldName, Object value,Class<T> entityClass) 
    {
    	Query query = new Query();
    	if(fieldName!=null)
    		query.addCriteria(Criteria.where(fieldName).is(value));
    	return findByField(query, null,null,null, null,entityClass); 
    }
    public <T> List<T> findByFields(Criteria criteriaQuery, String sortField, Boolean ascending, Integer from, Integer to, List<String> excludeFields, Class<T> entityClass)
    {
        Query query = new Query();
        if (sortField != null && ascending != null) {
            Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
            query.with(Sort.by(direction, sortField));
        }
        query.addCriteria(criteriaQuery);
        if (excludeFields != null) {
            for (String field : excludeFields) {
                query.fields().exclude(field);
            }
        }
        if (from != null && to != null) {
            int limit = to - from;
            if (limit > 0) {
                query.skip(from); 
                query.limit(limit); 
            }
        }
        return mongoTemplate.find(query, entityClass);
    }
    public <T> List<T> findLatest(Query query,String sortField, int limit, Class<T> entityClass) 
    {
        query.with(Sort.by(Sort.Order.desc(sortField))); // Sort by uploadTime in descending order
        query.limit(limit); // Limit the results to the specified number
        return mongoTemplate.find(query, entityClass);
    }
    public <T> T findOneByField(String fieldName, Object value, Class<T> entityClass) {
        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).is(value));
        T ret=mongoTemplate.findOne(query, entityClass);
        if((ret!=null)&&(MiscUtil.objectHasMethod(ret, "getCopyObject")))
        {
        	JsonNode node=(JsonNode)MiscUtil.jsonObject2Node(ret);
        	if(node!=null)
        		MiscUtil.objectCall(ret, "setCopyObject", (JsonNode)node);
        }
        return ret;
    }
    public <T> T findUpdateOneByFields(Criteria criteriaQeury, String sortField, Boolean ascending,Map<String, Object> updateFields, Class<T> entityClass) 
    {
        Query query = new Query();
        if (sortField != null && ascending != null) {
            Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
            query.with(sort);
        }
        
        query.addCriteria(criteriaQeury);
    	
        Update update = new Update();
        updateFields.forEach((fieldName, value) -> update.set(fieldName, value));

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true); // 返回修改后的文档
        return mongoTemplate.findAndModify(query,update,options,entityClass);
    }
    public <T> T findOneByFields(Criteria criteriaQeury, String sortField, Boolean ascending, Class<T> entityClass) {
        Query query = new Query();
        query.addCriteria(criteriaQeury);
        if (sortField != null && ascending != null) {
            Sort.Direction direction = ascending ? Sort.Direction.ASC : Sort.Direction.DESC;
            query.with(Sort.by(direction, sortField));
        }
        T ret=mongoTemplate.findOne(query, entityClass);
        if((ret!=null)&&(MiscUtil.objectHasMethod(ret, "getCopyObject")))
        {
        	JsonNode node=(JsonNode)MiscUtil.jsonObject2Node(ret);
        	if(node!=null)
        		MiscUtil.objectCall(ret, "setCopyObject", (JsonNode)node);
        }
        return ret;
    }
    @SuppressWarnings("unused")
	public <T> T save(String caller,T entity) {
    	if(false) 
    	{
	    	if(caller!=null)
	    	{
	    		String idTemp=(String)MiscUtil.objectCall(entity, "getId");
	    		logger.info("save "+caller+" "+entity.getClass().getName()+" id="+idTemp);
	    	}
    	}
    	return mongoTemplate.save(entity);
    }
    private <T> String entityGetId(T entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (String)idField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e.getMessage(),e);
        }
        return null;
    }
    public <T> void updateBatch(List<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            return; 
        }
        @SuppressWarnings("unchecked")
		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, (Class<T>) entityList.get(0).getClass());
        for (T entity : entityList) 
        {
        	String id=entityGetId(entity);
        	if(id!=null)
        	{
	            Update update = new Update();
	            Field[] fields = entity.getClass().getDeclaredFields();
	            for (Field field : fields) {
	                field.setAccessible(true); 
	                if (field.isAnnotationPresent(Transient.class)) 
	                    continue; 
	                try {
	                    Object value = field.get(entity);
	                    update.set(field.getName(), value);
	                } catch (IllegalAccessException e) {
	                	logger.error(e.getMessage(),e);
	                }
	            }
	            bulkOps.updateOne(new Query(Criteria.where("id").is(id)), update);
        	}else
        	{
        		bulkOps.insert(entity);
        	}
        }
        bulkOps.execute();
    }
    public <T> void updateById(Class<T> entityClass,String id, Object... fields) 
    {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update();
        for(int i=0;i<fields.length;i+=2)
        	update.set(fields[i].toString(), fields[i+1]);
        //return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), entityClass);
        mongoTemplate.updateFirst(query, update, entityClass);
    }
    public <T> void increaseById(Class<T> entityClass,String id, String filedName,Long fieldValue) 
    {
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc(filedName, fieldValue); 
        mongoTemplate.updateFirst(query, update, entityClass);
    }
    public <T> long updateTimeSeriesById(Class<T> entityClass,String id, String fieldName, Object newValue) {
    	Query query = new Query(Criteria.where("_id").is(id));
    	Update update = new Update().set(fieldName, newValue);
        UpdateResult result = mongoTemplate.updateMulti(query, update, entityClass);
        return result.getModifiedCount();
    }
    public <T> void updateByField(Class<T> entityClass,String fieldName, Object fieldValue,String newfieldName, Object newfieldValue) 
    {
        Query query = new Query(Criteria.where(fieldName).is(fieldValue));
        Update update = new Update().set(newfieldName, newfieldValue);
        //return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true), entityClass);
        mongoTemplate.updateMulti(query, update, entityClass);
    }
    public <T> DeleteResult delete(String fieldName, Object fieldValue, Class<T> entityClass) {
        Query query = new Query();
        query.addCriteria(Criteria.where(fieldName).is(fieldValue));
        return mongoTemplate.remove(query, entityClass);
    } 
    public <T> DeleteResult delete(Query query, Class<T> entityClass) {
        return mongoTemplate.remove(query, entityClass);
    } 
    public <T> DeleteResult deleteOld(String timeField,Integer keepSeconds, Class<T> entityClass) {
        Date timeAgo = new Date(System.currentTimeMillis() - keepSeconds * 1000);
        Query query = new Query(Criteria.where(timeField).lt(timeAgo));
        DeleteResult result = mongoTemplate.remove(query, entityClass);
        logger.info("Deleted {} documents from collection {}", result.getDeletedCount(), mongoTemplate.getCollectionName(entityClass));
        return result;
    } 

    public <T> List<T> aggregatedDataQuery(Criteria criteriaQeury,Sort sort,GroupOperation group,ProjectionOperation projectOperation,Class<T> entityClass) 
    {
        List<AggregationOperation> operations = new ArrayList<>();
        if (criteriaQeury != null) {
            operations.add(Aggregation.match(criteriaQeury));
        }
        if (sort != null) {
            operations.add(Aggregation.sort(sort));
        }
        if (group != null) {
            operations.add(group);
        }
        if (projectOperation != null) {
            operations.add(projectOperation);
        }
        Aggregation aggregation = Aggregation.newAggregation(operations);
        AggregationResults<T> results = mongoTemplate.aggregate(aggregation, entityClass, entityClass);
        return results.getMappedResults();
    }
    public List<Document> aggregatedModuleDataQuery(DeviceModel deviceModel,Integer moduleTypeId,Date startTime,Date endTime,Bson redact,Map<String,Object> chartMeta,Integer groupMinutes) 
    {
    	MongoCollection<Document> collection=mongoTemplate.getCollection("dataComm");
        String groupField="uploadTime";
        @SuppressWarnings("unchecked")
		List<Map<String, String>>seriesMeta=(List<Map<String, String>>)chartMeta.get("seriesMeta");
        BsonField[] bsonFields=new BsonField[seriesMeta.size()+1];
        List<String> fetchFields=new ArrayList<>();
        List<String> returnFields=new ArrayList<>();
        int j=0;
        
        for (Map<String, String> serieMap : seriesMeta) {
            String field1 = serieMap.get("field").toString();
            String field2 = MiscUtil.capitalizeFirstLetter(field1);
            String operator = serieMap.get("operator").toString();
            String field3 = operator + field2;

            String nestedField = "upload." + field1;
            if (!fetchFields.contains(nestedField)) {
                fetchFields.add(nestedField);
            }
            
            if ("max".equals(operator)) {
                bsonFields[j++] = Accumulators.max(field3, "$" + nestedField);
            } else if ("min".equals(operator)) {
                bsonFields[j++] = Accumulators.min(field3, "$" + nestedField);
            } else if ("avg".equals(operator)) {
                bsonFields[j++] = Accumulators.avg(field3, "$" + nestedField);
            } else if ("last".equals(operator)) {
                bsonFields[j++] = Accumulators.last(field3, "$" + nestedField);
            }
            returnFields.add(field3);
        }

        //match
        Bson match=Aggregates.match(Filters.and(
                Filters.eq("deviceId", deviceModel.getId()),
                Filters.eq("moduleTypeId", moduleTypeId),
                Filters.gte(groupField, startTime), 
                Filters.lt(groupField, endTime)    
            ));
        
        //project
        Bson project=Aggregates.project(Projections.fields(
                Projections.excludeId(),
                Projections.include(fetchFields),
                Projections.include(groupField),
                Projections.computed(
                    "timeBucket",
                    new Document("$subtract", Arrays.asList(
                        new Document("$toLong", "$"+groupField),  
                        new Document("$mod", Arrays.asList(
                            new Document("$toLong", "$"+groupField), groupMinutes*60*1000
                        ))
                    ))
                )
            ));
        //group
        bsonFields[j++]=Accumulators.sum("count", 1);
        Bson group=Aggregates.group("$timeBucket", bsonFields);
        
        //result
        List<Bson> pipeline = new ArrayList<>();
	    pipeline.add(match);
	    if (redact != null)
	        pipeline.add(redact);
	    pipeline.add(project);
	    pipeline.add(group);
	    pipeline.add(Aggregates.sort(new Document("timeBucket", 1)));
	    Iterable<Document> result = collection.aggregate(pipeline);

	    return aggregatedModuleDataFill(result,returnFields, seriesMeta,groupMinutes*60*1000,  startTime,endTime);
    }
    
    private List<Document> aggregatedModuleDataFill(Iterable<Document> documents,List<String> returnFields, List<Map<String, String>> seriesMeta,long bucketSizeMillis, Date startTime,Date endTime) {
        List<Document> allDocuments = new ArrayList<>();
        long currentBucket = startTime.getTime();
        currentBucket-=currentBucket%bucketSizeMillis;
        long endBucket=endTime.getTime();
        DecimalFormat df = new DecimalFormat("#.00");
        Map<Long,Document> documentsMap=new HashMap<>();
        for(Document doc:documents)
        	documentsMap.put(doc.getLong("_id"), doc);
        Document emptyDoc=aggregatedModuleDataEmpty(seriesMeta);
        while (currentBucket < endBucket) {
        	Document tempDco=documentsMap.get(currentBucket);
            if (tempDco!=null) {
                 returnFields.stream()
                     .filter(tempDco::containsKey) // Check if the field exists in the document
                     .forEach(field -> {
                         Object value = tempDco.get(field);
                         if (value instanceof Number) { // Ensure the value is a number
                             Number number = (Number) value;
                             // Format the number to 2 decimal places
                             String formattedValue = df.format(number.doubleValue());
                             // Update the document with the formatted value
                             tempDco.put(field, formattedValue);
                         }
                     });
            	allDocuments.add(tempDco);
            }
            else {
            	Document newDoc=new Document(emptyDoc);
            	newDoc.put("_id", currentBucket);
            	allDocuments.add(newDoc);
            }
            currentBucket += bucketSizeMillis;
        }
        return allDocuments;
    }
    private Document aggregatedModuleDataEmpty(List<Map<String, String>> seriesMeta) {
        // 创建空桶，避免每次手动 append
    	Document document=new Document();
        //{ operator: 'max', field: 'pin1Value', legend: 'max pin1',color: '#80FFA5'},
        //{ operator: 'max', field: 'pin2Value', legend: 'max pin2',color: '#00DDFF'}
        for(Map<String, String> serieMap:seriesMeta)
        {
        	String field1=serieMap.get("field").toString();
        	String field2=MiscUtil.capitalizeFirstLetter(field1);
        	String operator=serieMap.get("operator").toString();
        	String field3=operator+field2;
        	document=document.append(field3, 0);
        }
        return document;
    }
    @PostConstruct
    private void createOrEnsureTimeSeriesCollections() {
    	
    	createCollection(ApiKeyModel.class);
    	
    	ensureMultiIndex("aiRule",new Object[][] { {"userId", Sort.Direction.ASC} });
    	ensureMultiIndex("aiRule",new Object[][] { {"userId", Sort.Direction.ASC},{"status", Sort.Direction.ASC},{"deviceNos", Sort.Direction.ASC} });
    	
    	ensureTimeSeriesCollection("aiCall","createTime","ruleId","minutes",List.of("deviceId")); 
    	ensureMultiIndex("aiCall",new Object[][] { {"ruleId", Sort.Direction.ASC} });
    	
    	ensureTTLIndex("captcha","createTime",Sort.Direction.ASC,60*2L,null);
    	ensureTTLIndex("userSession","expiredTime",Sort.Direction.ASC,10L,null);

    	ensureMultiIndex("apiKey",new Object[][] { {"key", Sort.Direction.ASC} },new Object[][] { {"userId", Sort.Direction.ASC} });
    	ensureTTLIndex("dataCache","expiredTime",Sort.Direction.ASC,60L,null);
    	ensureMultiIndex("dataCache",new Object[][] { {"key", Sort.Direction.ASC} });
    	
    	ensureTimeSeriesCollection("dataComm","uploadTime","deviceId","minutes",List.of("moduleTypeId")); 

    	ensureTTLIndex("dataUserLog","createTime",Sort.Direction.ASC,configProperties.USER_LOG_KEEP_DAYS()*3600*24L,null);
    	
    	ensureMultiIndex("deviceConfigElement",new Object[][] { {"deviceId", Sort.Direction.ASC} });
    	
    	ensureMultiIndex("device",new Object[][] { {"userId", Sort.Direction.ASC} },new Object[][] { {"deviceNo", Sort.Direction.ASC} });
    	
    	ensureMultiIndex("moduleRuleLog",new Object[][] { {"deviceId", Sort.Direction.ASC} },new Object[][] { {"moduleTypeId", Sort.Direction.ASC} },new Object[][] { {"ruleId", Sort.Direction.ASC} },new Object[][] { {"status", Sort.Direction.ASC} });
    	ensureTTLIndex("moduleRuleLog","uploadTime",Sort.Direction.ASC,configProperties.MODULE_LOG_KEEP_DAYS()*3600*24L,null);
    	
    	ensureMultiIndex("otaDownload",new Object[][] { {"token", Sort.Direction.ASC} });
    	ensureTTLIndex("otaDownload","expiredTime",Sort.Direction.ASC,60L,null);
    	
    	ensureMultiIndex("otaRecord",new Object[][] { {"userId", Sort.Direction.ASC} });
    	
    	ensureMultiIndex("postLog",new Object[][] { {"deviceId", Sort.Direction.ASC},{"moduleTypeId", Sort.Direction.ASC} });
    	ensureMultiIndex("postLog",new Object[][] { {"deviceId", Sort.Direction.ASC} });
    	ensureTTLIndex("postLog","createTime",Sort.Direction.ASC,configProperties.MODULE_LOG_KEEP_DAYS()*3600*24L,null);
    	
    	ensureMultiIndex("rebootInfo",new Object[][] { {"deviceId", Sort.Direction.ASC} });
    	
    	ensureMultiIndex("systemConfig",new Object[][] { {"name", Sort.Direction.ASC} });
    	
    	ensureMultiIndex("user",new Object[][] { {"login", Sort.Direction.ASC} });

    	ensureMultiIndex("moduleInfo",new Object[][] { {"deviceId", Sort.Direction.ASC} });
    	
    	ensureMultiIndex("projectFile",new Object[][] { {"userId", Sort.Direction.ASC},{"projectId", Sort.Direction.ASC},{"path", Sort.Direction.ASC} });
    }
    
    private <T> void createCollection(Class<T> entityClass) 
    {
        if (!mongoTemplate.collectionExists(entityClass)) {
            mongoTemplate.createCollection(entityClass);
        }
    }
    private void ensureMultiIndex(String collectionName, Object[][]... paramsAll) {
        IndexOperations indexOps = mongoTemplate.indexOps(collectionName);
        for (Object[][] params : paramsAll) 
        {
	        Index index = new Index();
	        for (int i = 0; i < params.length; i++) {
	            Object[] oneParam = params[i];
	            index = index.on(oneParam[0].toString(), (Sort.Direction) oneParam[1]);
	        }
	        indexOps.ensureIndex(index);
        }
        //logger.info("collection " + collectionName + " composite index ensured");
    }
    private void ensureTTLIndex(String collectionName, String field, Sort.Direction direction, Long expireSeconds, String partialFilterExpressionStr) {
        Document indexKeys = new Document(field, direction == Sort.Direction.ASC ? 1 : -1);
        IndexOptions indexOptions = new IndexOptions();
        if (expireSeconds != null) {
            indexOptions.expireAfter(expireSeconds, java.util.concurrent.TimeUnit.SECONDS);
        }
        if (partialFilterExpressionStr != null && !partialFilterExpressionStr.isEmpty()) {
            Document partialFilterExpression = Document.parse(partialFilterExpressionStr);
            indexOptions.partialFilterExpression(partialFilterExpression);
        }
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        List<Document> existingIndexes = collection.listIndexes().into(new ArrayList<>());
        boolean indexExists = existingIndexes.stream()
                .anyMatch(index -> {
                    Document key = index.get("key", Document.class);
                    return key.equals(indexKeys) &&
                           (!index.containsKey("expireAfterSeconds") || 
                            index.getInteger("expireAfterSeconds").equals(expireSeconds.intValue())) &&
                           (!index.containsKey("partialFilterExpression") || 
                            index.get("partialFilterExpression").equals(Document.parse(partialFilterExpressionStr)));
                });

        if (!indexExists) {
            collection.createIndex(indexKeys, indexOptions);
            //logger.info("Index ensured for collection: " + collectionName + ", field: " + field);
        } else {
        	//logger.info("Index already exists for collection: " + collectionName + ", field: " + field);
        }
    }
    private void ensureTimeSeriesCollection(String collectionName, String timeField, String metaField, String granularity, List<String> additionalFields) {
        MongoDatabase database = mongoTemplate.getDb();
        boolean collectionExists = database.listCollectionNames().into(new ArrayList<>()).contains(collectionName);
        if (!collectionExists) {
            Document createCommand = new Document("create", collectionName)
                .append("timeseries", new Document("timeField", timeField)
                                               .append("metaField", metaField)
                                               .append("granularity", granularity));
            database.runCommand(createCommand);
            //logger.info("collection " + collectionName + " created as a time series collection.");
        } else {
            Document collStats = new Document("collStats", collectionName);
            Document collectionInfo = database.runCommand(collStats);
            boolean isTimeSeries = collectionInfo.containsKey("timeseries");
            if (!isTimeSeries) {
                Document updateCommand = new Document("collMod", collectionName)
                    .append("timeseries", new Document("timeField", timeField)
                                                   .append("metaField", metaField)
                                                   .append("granularity", granularity));
                database.runCommand(updateCommand);
                //logger.info("collection " + collectionName + " updated to time series collection.");
            } else {
                //logger.info("collection " + collectionName + " is already a time series collection.");
            }
        }
        int paramsLen=(additionalFields==null?2:2+additionalFields.size());       
        Object[][] params = new Object[paramsLen][2];
        int i=0;
        params[i][0]=metaField;
        params[i][1]=Sort.Direction.ASC;
        i++;
        params[i][0]=timeField;
        params[i][1]=Sort.Direction.ASC;
        i++;
        if(additionalFields!=null)
        {
        	for(String field:additionalFields)
        	{
                params[i][0]=field;
                params[i][1]=Sort.Direction.ASC;
                i++;
        	}
            //usually no need to do it, since mongodb will do it automatically
            ensureMultiIndex(collectionName,params);
        }
    }
}
