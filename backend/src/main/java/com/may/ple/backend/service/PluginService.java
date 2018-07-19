package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.PluginModuleConstant;
import com.may.ple.backend.criteria.PluginFindCriteriaReq;
import com.may.ple.backend.criteria.PluginFindCriteriaResp;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaReq;
import com.may.ple.backend.entity.PluginFile;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.ZipUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class PluginService {
	private static final Logger LOG = Logger.getLogger(PluginService.class.getName());
	private MongoTemplate coreTemplate;
	private JWebsocketService jwsService;
	@Value("${file.path.programFile}")
	private String filePathProgram;
	private final String webappsPath;
	{
		webappsPath = System.getProperty( "catalina.base" ) + File.separator + "webapps";
	}
	
	@Autowired	
	public PluginService(MongoTemplate coreTemplate, JWebsocketService jwsService) {
		this.coreTemplate = coreTemplate;
		this.jwsService = jwsService;
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String module) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			FileDetail fd = saveFile(uploadedInputStream, fileDetail, date);
			
			LOG.debug("Save new TaskFile");
			PluginFile PluginFile = new PluginFile(fd.fileName, date);
			PluginFile.setModule(module);
			PluginFile.setEnabled(true);
			PluginFile.setFileSize(fd.fileSize);
			coreTemplate.insert(PluginFile);
			LOG.debug("Save finished");
			
			LOG.debug("Check and create Index.");
			DBCollection collection = coreTemplate.getCollection("pluginFile");
			collection.createIndex(new BasicDBObject("module", 1), "module_1", true);
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public PluginFindCriteriaResp find(PluginFindCriteriaReq req) throws Exception {
		try {
			PluginFindCriteriaResp resp = new PluginFindCriteriaResp();
			
			long totalItems = coreTemplate.count(new Query(), PluginFile.class);
			
			Query query = new Query()
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<PluginFile> files = coreTemplate.find(query, PluginFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deploy(String id) throws Exception {
		try {
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), PluginFile.class);
			PluginModuleConstant module = PluginModuleConstant.valueOf(file.getModule());
			
			switch (module) {
			case DPY:
			case JWS:
				LOG.info("Module: " + module.name());
				stopJar(module);
				copyFileToDeploy(id, file.getFileName(), module);
				startJar(file);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void stop(PluginFile file) throws Exception {
		try {
			PluginModuleConstant module = PluginModuleConstant.valueOf(file.getModule());
			
			switch (module) {
			case JWS:
				LOG.info("Call stopJar");
				stopJar(module);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void stop(String id) throws Exception {
		try {
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), PluginFile.class);
			stop(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void start(PluginFile file) throws Exception {
		try {
			PluginModuleConstant module = PluginModuleConstant.valueOf(file.getModule());
			
			switch (module) {
			case JWS:
				LOG.info("Call stopJar");
				stopJar(module);
				
				LOG.info("Call startJar");
				startJar(file);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void start(String id) throws Exception {
		try {
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), PluginFile.class);
			start(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String id) throws Exception {
		try {			
			LOG.debug("Remove PluginFile");
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), PluginFile.class);
			coreTemplate.remove(file);
			
			if(!new File(filePathProgram + "/" + file.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + file.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateCommand(PluginFindCriteriaReq req) throws Exception {
		try {
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(req.getId())), PluginFile.class);
			file.setOption(req.getOption());
			file.setCommand(req.getCommand());
			coreTemplate.save(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getFile(ProgramFileFindCriteriaReq req) {
		try {			
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(req.getId())), PluginFile.class);
			String filePath = filePathProgram + "/" + file.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", file.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateEnabled(PluginFindCriteriaReq req) {
		try {			
			PluginFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(req.getId())), PluginFile.class);
			
			if(file.getEnabled()) {
				file.setEnabled(false);
			} else {
				file.setEnabled(true);
			}
			
			coreTemplate.save(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private FileDetail saveFile(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, Date date) throws Exception {
		try {
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			File file = new File(filePathProgram);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			String filePathStr = filePathProgram + "/" + fd.fileName;
			fd.fileSize = Files.copy(uploadedInputStream, Paths.get(filePathStr))/1024/1024;
			
			return fd;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void stopJar(PluginModuleConstant module) throws Exception {
		try {
			if(module == PluginModuleConstant.JWS) {
				LOG.info("Request JWS to shutdown");
				jwsService.shutdownJWS();
			} else {
				LOG.info("Stop Jar");
				Socket socket = new Socket("localhost", module.getPort()); 
				if (socket.isConnected()) {
					LOG.info("Can stop");
					PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
					pw.println("SHUTDOWN");
					pw.close(); 
					socket.close(); 
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try {
				Thread.sleep(5000);					
			} catch (Exception e2) {}
		}
	}
	
	private void startJar(PluginFile file) throws Exception {
		try {
			LOG.info("Start Jar");
			PluginModuleConstant module = PluginModuleConstant.valueOf(file.getModule());
			String moduleName = file.getModule();
			String slash = File.separator;
			String option = StringUtils.trimToEmpty(file.getOption());
			String command = StringUtils.trimToEmpty(file.getCommand());
			
			if(module == PluginModuleConstant.JWS) {
				command = "-home " + webappsPath + slash + module.name();
			}
			
			LOG.info("Option : " + option);
			LOG.info("Command : " + command);
			
			List<String> options = Arrays.asList(option.split(" "));
			List<String> commands = Arrays.asList(command.split(" "));
			String programName = moduleName + ".jar";
			
			ArrayList<String> args = new ArrayList<>();
			args.add("javaw");
			args.addAll(options);
			args.add("-jar");
			args.add(programName);
			args.addAll(commands);
			
	    	ProcessBuilder pb = new ProcessBuilder(args);
	    	
	    	if(module == PluginModuleConstant.JWS) {
	    		pb.directory(new File(webappsPath + slash + module.name()));	    		
	    	} else {
	    		pb.directory(new File(webappsPath));
	    	}
	    	pb.start();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void copyFileToDeploy(String id, String fullName, PluginModuleConstant module) throws Exception {
		try {
			LOG.info("copyFileToDeploy");
			String slash = File.separator;
			
			String programName = module.name() + "." + FilenameUtils.getExtension(fullName);
			LOG.debug("programFileName: " + programName);
			String moduleName = module == PluginModuleConstant.JWS ? module.name() : programName;
			
			LOG.debug("Delete old file or folder");
			FileDeleteStrategy.FORCE.delete(new File(webappsPath + slash + moduleName));
			
			File file = new File(webappsPath + slash + programName);
			String originaleProgramFileName = filePathProgram + "/" + fullName;
			LOG.debug("File path: " + originaleProgramFileName);
			
			FileUtils.copyFile(new File(originaleProgramFileName), file);
			LOG.info("Module: " + module.name());
			
			if(module == PluginModuleConstant.JWS) {
				LOG.debug("Unzip file");
				ZipUtil.unZipToCurrentFolder(file);
				
				LOG.debug("Delete zip file");
				FileDeleteStrategy.FORCE.delete(file);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
