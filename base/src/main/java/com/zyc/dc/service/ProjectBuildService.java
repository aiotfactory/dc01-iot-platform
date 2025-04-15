package com.zyc.dc.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.zyc.dc.dao.DeviceModel;
import com.zyc.dc.dao.OTARecordModel;
import com.zyc.dc.dao.ProjectBuildModel;
import com.zyc.dc.dao.ProjectFileModel;
import com.zyc.dc.dao.ProjectInfoModel;

@Service
public class ProjectBuildService {
	@Autowired
	private MongoDBService mongoDBService;
	@Autowired
	private ConfigProperties configProperties;
	
	private AtomicBoolean isBuilding = new AtomicBoolean(false);
	
	private static final Logger logger = LoggerFactory.getLogger(ProjectBuildService.class);
	
    public OTARecordModel OTACheck(DeviceModel device) 
    {
    	
    	List<OTARecordModel> otaRecords=mongoDBService.findByField(new Query().addCriteria(Criteria.where("userId").is(device.getUserId())), "firmwareTime",null, false, null,OTARecordModel.class);
        if (otaRecords == null || otaRecords.isEmpty()) {
            return null;
        }
        
        for (OTARecordModel record : otaRecords) {
            if (!record.getDeviceType().equals(device.getDeviceType())) {
                continue;
            }
            if ((record.getDeviceFirmWareTargetVersion() != null) &&(record.getDeviceFirmWareTargetVersion().length()>0)) {
                Set<Long> targetVersions = Arrays.stream(record.getDeviceFirmWareTargetVersion().split(","))
                                                 .map(String::trim)
                                                 .map(Long::parseLong)
                                                 .collect(Collectors.toSet());
                if (!targetVersions.contains(device.getDeviceFirmWareVersion())) {
                    continue;
                }
            }

            // Step 2: Apply upgrade strategy
            boolean sameVersion = device.getDeviceFirmWareVersion().equals(record.getDeviceFirmWareVersion());
            if (record.getUpgradeStrategy() == OTARecordModel.OTAUpgradeStrategy.IGNORE_SAME && sameVersion) {
                continue;
            }

            // Step 3: Apply upgrade scope
            switch (record.getUpgradeScope()) {
                case NONE:
                    return null;
                case ALL:
                    return record;
                case EXCEPT_EXCLUDE:
                    if (record.getDeviceNoListExclude() != null 
                            && record.getDeviceNoListExclude().contains(device.getDeviceNo())) {
                        return null;
                    }
                    return record;
                case INCLUDE_ONLY:
                    if (record.getDeviceNoListInclude() != null 
                            && record.getDeviceNoListInclude().contains(device.getDeviceNo())) {
                        return record;
                    }
                    return null;
                default:
                    return null;
            }
        }

        return null;
    }
	@Async
    public void build()
    {
	    if (!isBuilding.compareAndSet(false, true)) {
	        logger.info("Build task is already in progress. Ignoring new request.");
	        return;
	    }
	    logger.info("Build task start");
    	Map<String, Object> queryFields=new HashMap<>();
    	queryFields.put("resultType", ProjectBuildModel.ProjectBuildResultType.PENDING);
    	
    	Criteria criteriaQeury = Criteria.where("resultType").in(ProjectBuildModel.ProjectBuildResultType.PENDING, ProjectBuildModel.ProjectBuildResultType.BUILDING);
    	
    	Map<String, Object> updateFields=new HashMap<>();
    	updateFields.put("resultType", ProjectBuildModel.ProjectBuildResultType.BUILDING);
    	
    	try {
	    	while(true) 
	    	{
	    		updateFields.put("buildStartTime", Instant.now());
		    	ProjectBuildModel projectBuildModel=mongoDBService.findUpdateOneByFields(criteriaQeury, "createTime", false,updateFields, ProjectBuildModel.class);
		    	
		    	if(projectBuildModel!=null)
		    	{
		    		ProjectInfoModel projectInfoModel=mongoDBService.findOneByField("id", projectBuildModel.getProjectId(), ProjectInfoModel.class);
		    		String prefix="user "+projectBuildModel.getUserId()+" build "+projectBuildModel.getId();
		    		logger.info(prefix+" start");
		    		List<ProjectBuildModel.BuildLogModel> logs=new LinkedList<>();
	            	clearBuildFiles(projectBuildModel);
	            	logger.info(prefix+" files cleared");
		    		projectBuildModel.setLogs(logs);
		    		String src=projectInfoModel.getPath();
		    		String srcBak=src;
		    		srcBak=MiscUtil.stringTrim(src, null, File.separator)+"-building"+File.separator;
		    		String command = String.format("/usr/bin/rsync -avh --delete --exclude='build/' %s %s", src,srcBak);
		    		//String command = String.format("/usr/bin/rsync -avh --delete %s %s", srcBak, src);
		    		shellLocal(command,projectBuildModel,null,false);//restore 
		    		logger.info(prefix+" src files are restored");
		    		copyProject(srcBak,projectBuildModel);
		    		logger.info(prefix+" user files are pushed");		    		
		    		command = String.format("/opt/esp/projects/build.sh %s",srcBak);
		    		logger.info(prefix+" esp build start");
		    		int[] shellResult=shellLocal(command,projectBuildModel,"python -m esptool --chip",true);//build
		    		logger.info(prefix+" esp build end with "+shellResult[1]);
		            Instant end=Instant.now();
		            projectBuildModel.setUpdateTime(end);
		            projectBuildModel.setBuildEndTime(end);
		            if(shellResult[1]>0) {
		            	String filePath=srcBak+"build/"+projectInfoModel.getBinName();
		            	EspAppDesc espDesc=EspAppDesc.readFromFile(filePath+".bin");
		            	if((espDesc!=null)&&(espDesc.getVersion()!=null)) {
			            	projectBuildModel.setBinFile(mongoDBService.fileSave(filePath+".bin"));
			            	projectBuildModel.setElfFile(mongoDBService.fileSave(filePath+".elf"));
			            	projectBuildModel.setMapFile(mongoDBService.fileSave(filePath+".map"));
			            	projectBuildModel.setBinVersion(espDesc.getVersion());
			            	logger.info(prefix+" esp firmware saved success");
			            	projectBuildModel.setResultType(ProjectBuildModel.ProjectBuildResultType.SUCCESS);
		            	}else
		            	{
		            		logger.info(prefix+" esp firmware failed head wrong");
		            		projectBuildModel.setResultType(ProjectBuildModel.ProjectBuildResultType.ERROR);
		            	}
		            }
		            else {
		            	projectBuildModel.setResultType(ProjectBuildModel.ProjectBuildResultType.ERROR);
		            	logger.info(prefix+" esp firmware build failed");
		            }
		    		mongoDBService.save("buildLocal", projectBuildModel);		    		
		    	}else
		    		break;
	    	}
    	}catch(Exception e)
    	{
    		logger.error(e.getMessage(),e);
    	}finally {
    		isBuilding.set(false); 
    	}
    }
	public void clearBuildFiles(ProjectBuildModel projectBuildModel)
	{
		if(projectBuildModel!=null)
		{
			if(projectBuildModel.getBinFile()!=null)
				mongoDBService.fileDelById(projectBuildModel.getBinFile());
			if(projectBuildModel.getMapFile()!=null)
				mongoDBService.fileDelById(projectBuildModel.getMapFile());
			if(projectBuildModel.getElfFile()!=null)
				mongoDBService.fileDelById(projectBuildModel.getElfFile());
		}
	}
    public boolean copyProject(String pathFolder,ProjectBuildModel projectBuildModel) throws IOException
    {
    	Criteria queryCriteria = Criteria.where("userId").is(projectBuildModel.getUserId()).and("projectId").is(projectBuildModel.getProjectId());
    	List<ProjectFileModel> files=mongoDBService.findByFields(queryCriteria, null, null, null, null,null,ProjectFileModel.class);
    	if((files!=null)&&(files.size()>0))
    	{
    		for(ProjectFileModel file:files)
    		{
    			String filePath=pathFolder+file.getPath();
    			Path path = Paths.get(filePath);
    			Files.write(path, file.getContent().getBytes());
    			//logger.info("copy file "+filePath);
    		}
    	}
    	return true;
    }
    public int[] shellLocal(String command,ProjectBuildModel projectBuildModel,String match,boolean recordLog) 
    {
    	int[] result= {0,0};
    	int exitCode = 0;
    	int matchResult=0;
    	logger.info("run start "+command);
    	if(configProperties.ENV_PRODUCTION()==0) {
    		return result;
    	}
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
        processBuilder.redirectErrorStream(true); 
        Process process=null;
        try {
            process = processBuilder.start();
            if(recordLog) 
            {
	            List<ProjectBuildModel.BuildLogModel> logs=projectBuildModel.getLogs();
	            int lines=0;
	            // 读取命令输出
	            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	                String line;
	                while ((line = reader.readLine()) != null) 
	                {
	                	//logger.info(line); 
	                	ProjectBuildModel.BuildLogModel log=new ProjectBuildModel.BuildLogModel();
	                	log.setCreateTime(Instant.now());
	                	log.setContent(line);
	                	logs.add(log);
	                	if(lines>2000)
	                		logs.remove(0);
	                    if (match != null && matchResult == 0 && line.contains(match)) 
	                        matchResult = 1;
	                	lines++;
	                	if(lines%10==8) {
	                		projectBuildModel.setUpdateTime(Instant.now());
	                		mongoDBService.save("buildLocal", projectBuildModel);
	                	}
	                }
	            }
            }
            exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("run success "+command);
            } else {
            	logger.info("run fail with error "+exitCode+" "+command);
            }    		
        } catch (Exception e) {
        	logger.error(e.getMessage(),e);
        } finally {
            if (process != null) {
                try {
                    process.getInputStream().close();
                    process.getOutputStream().close();
                    process.getErrorStream().close();
                } catch (IOException e) {
                	logger.error(e.getMessage(),e);
                }
                process.destroy();
            }
        }
        result[0]=exitCode;
        result[1]=matchResult;
        return result;
    }

    public static class EspAppDesc {
        private long magicWord;
        private long secureVersion;
        private long[] reserv1;
        private Long version;
        private String projectName;
        private String time;
        private String date;
        private String idfVer;
        private byte[] appElfSha256;
        private long[] reserv2;

        public long[] getReserv1() {
			return reserv1;
		}

		public void setReserv1(long[] reserv1) {
			this.reserv1 = reserv1;
		}

		public long getMagicWord() {
            return magicWord;
        }

        public void setMagicWord(long magicWord) {
            this.magicWord = magicWord;
        }

        public long getSecureVersion() {
            return secureVersion;
        }

        public void setSecureVersion(long secureVersion) {
            this.secureVersion = secureVersion;
        }

        public Long getVersion() {
            return version;
        }

        public void setVersion(Long version) {
            this.version = version;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getIdfVer() {
            return idfVer;
        }

        public void setIdfVer(String idfVer) {
            this.idfVer = idfVer;
        }

        public byte[] getAppElfSha256() {
            return appElfSha256;
        }

        public void setAppElfSha256(byte[] appElfSha256) {
            this.appElfSha256 = appElfSha256;
        }

        public long[] getReserv2() {
            return reserv2;
        }

        public void setReserv2(long[] reserv2) {
            this.reserv2 = reserv2;
        }

        public static EspAppDesc readFromFile(String filePath){
            File file = new File(filePath);
            if (!file.exists()) {
            	logger.error(filePath+" not exist");
            	return null;
            }

            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[288];
                fis.read(buffer);
                return parseAppDesc(buffer);
            } catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
            return null;
        }

        public static EspAppDesc parseAppDesc(byte[] data) {
        	if(data.length<288)
        		return null;
            ByteBuffer buffer = ByteBuffer.wrap(data);
            buffer.position(buffer.position() + 32);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            EspAppDesc appDesc = new EspAppDesc();
            appDesc.setMagicWord(buffer.getInt() & 0xFFFFFFFFL);
            appDesc.setSecureVersion(buffer.getInt() & 0xFFFFFFFFL);

            long[] reserv1 = new long[2];
            for (int i = 0; i < 2; i++) {
                reserv1[i] = buffer.getInt() & 0xFFFFFFFFL;
            }
            appDesc.setReserv1(reserv1);
            
            byte[] versionBytes = new byte[32];
            buffer.get(versionBytes);
            appDesc.setVersion( MiscUtil.strParseLong(new String(versionBytes)));

            byte[] projectNameBytes = new byte[32];
            buffer.get(projectNameBytes);
            appDesc.setProjectName(new String(projectNameBytes).trim());

            byte[] timeBytes = new byte[16];
            buffer.get(timeBytes);
            appDesc.setTime(new String(timeBytes).trim());

            byte[] dateBytes = new byte[16];
            buffer.get(dateBytes);
            appDesc.setDate(new String(dateBytes).trim());

            byte[] idfVerBytes = new byte[32];
            buffer.get(idfVerBytes);
            appDesc.setIdfVer(new String(idfVerBytes).trim());

            byte[] appElfSha256Bytes = new byte[32];
            buffer.get(appElfSha256Bytes);
            appDesc.setAppElfSha256(appElfSha256Bytes);

            long[] reserv2 = new long[20];
            for (int i = 0; i < 20; i++) {
                reserv2[i] = buffer.getInt() & 0xFFFFFFFFL;
            }
            appDesc.setReserv2(reserv2);

            return appDesc;
        }
        @Override
        public String toString() {
            return "EspAppDesc{" +
                    "magicWord=" + magicWord +
                    ", secureVersion=" + secureVersion +
                    ", version='" + version + '\'' +
                    ", projectName='" + projectName + '\'' +
                    ", time='" + time + '\'' +
                    ", date='" + date + '\'' +
                    ", idfVer='" + idfVer + '\'' +
                    ", sha256='" + MiscUtil.bytesToHex(appElfSha256) + '\'' +
                    '}';
        }
    }
}
