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

@Service
public class PluginService {
	private static final Logger LOG = Logger.getLogger(PluginService.class.getName());
	private MongoTemplate coreTemplate;
	@Value("${file.path.programFile}")
	private String filePathProgram;
	private final String webappsPath;
	{
		webappsPath = System.getProperty( "catalina.base" ) + File.separator + "webapps";
	}
	
	@Autowired	
	public PluginService(MongoTemplate coreTemplate) {
		this.coreTemplate = coreTemplate;
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
			case KYS:
				LOG.info("Module: " + module.name());
				stopJar(module.getPort());				
				copyFileToDeploy(id, file.getFileName());
				startJar(file.getFileName(), file.getCommand());
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
			case KYS:
				LOG.info("Call stopJar");
				stopJar(module.getPort());
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
			case KYS:
				LOG.info("Call stopJar");
				stopJar(module.getPort());
				
				LOG.info("Call startJar");
				startJar(file.getFileName(), file.getCommand());
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
	
	private void stopJar(int port) throws Exception {
		try {
			LOG.info("Stop Jar");
			Socket socket = new Socket("localhost", port); 
			if (socket.isConnected()) {
				LOG.info("Can stop");
				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
		        pw.println("SHUTDOWN");
		        pw.close(); 
		        socket.close(); 
			}
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try {
				Thread.sleep(5000);					
			} catch (Exception e2) {}
		}
	}
	
	private void startJar(String fullName, String command) throws Exception {
		try {
			LOG.info("Start Jar");
			
			command = StringUtils.trimToEmpty(command);
			LOG.info("Command : " + command);
			
			List<String> commands = Arrays.asList(command.split(" "));
			String programName = changeProgramName(fullName);
			
			ArrayList<String> args = new ArrayList<>();
			args.add("javaw");
			args.add("-jar");
			args.add(programName);
			args.addAll(commands);
			
	    	ProcessBuilder pb = new ProcessBuilder(args);
	    	pb.directory(new File(webappsPath));
	    	Process process = pb.start();
	    	
	    	Thread.sleep(10000);
	    	process.destroy();
	    	LOG.info("Destroy process");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void copyFileToDeploy(String id, String fullName) throws Exception {
		try {
			LOG.info("copyFileToDeploy");
			String slash = File.separator;
			String programName = changeProgramName(fullName);
			LOG.debug("programFileName: " + programName);
			
			LOG.debug("Delete old file");
			FileDeleteStrategy.FORCE.delete(new File(webappsPath + slash + programName));
			
			String originaleProgramFileName = filePathProgram + "/" + fullName;
			LOG.debug("File path: " + originaleProgramFileName);
			
			FileUtils.copyFile(new File(originaleProgramFileName), new File(webappsPath + slash + programName));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private String changeProgramName(String name) {
		try {
			int dashIndex = name.indexOf("-");
			String ext = "";
			
			if(dashIndex == -1) {
				dashIndex = name.length();
			} else {
				ext = "." + FilenameUtils.getExtension(name);
			}
			final String programFileName = name.substring(0, dashIndex);
			return programFileName + ext;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
			
}
