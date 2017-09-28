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

import com.may.ple.backend.criteria.ProgramFileFindCriteriaReq;
import com.may.ple.backend.criteria.ProgramFileFindCriteriaResp;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.NewTaskFile;
import com.may.ple.backend.entity.ProgramFile;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.FileUtil;

@Service
public class ProgramService {
	private static final Logger LOG = Logger.getLogger(ProgramService.class.getName());
	private MongoTemplate coreTemplate;
	@Value("${file.path.programFile}")
	private String filePathProgram;
	
	@Autowired	
	public ProgramService(MongoTemplate coreTemplate) {
		this.coreTemplate = coreTemplate;
	}
	
	public void deploy(String id) throws Exception {
		try {
		    Socket socket = new Socket("localhost", 8005); 
		    if (socket.isConnected()) {
		    	
		    	Query query = new Query();
				query.fields().include("productKey");
				
		    	ApplicationSetting setting = coreTemplate.findOne(query, ApplicationSetting.class);
		    	
		    	ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), ProgramFile.class);
				String warfilePath = filePathProgram + "/" + file.getFileName();
				LOG.info("War File path: " + warfilePath);
		    	
		    	LOG.info("Socket connected");
		    	String separator = File.separator;
		    	final String tomcatHome = System.getProperty( "catalina.base" );
		    	final String webapps = "webapps";
		    	final String webappsPath = tomcatHome + separator + webapps;
				LOG.info("deployerPath: " + webappsPath);
		    	
				//---: [param1: tomcat_home, param2: warfilePath]
				LOG.info("Start to execute deployer");
		    	String[] cmd = { "javaw", "-jar", "deployer.jar", tomcatHome, warfilePath, setting.getProductKey()};
		    	ProcessBuilder pb = new ProcessBuilder(cmd);
		    	pb.directory(new File(webappsPath));
		    	pb.environment().put("_RUNJAVA", "");
		    	pb.environment().put("_RUNJDB", "");
		    	pb.environment().put("JSSE_OPTS", "");
		    	pb.start();
				
		    	LOG.info("Start to shutdown tomcat");
		        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
		        pw.println("SHUTDOWN");
		        pw.close(); 
		        socket.close(); 
		    }
			
			LOG.info("Finished");
			return;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deployDeploy(String id) throws Exception {
		try {
			String tomcatHome = System.getProperty( "catalina.base" );
			String separator = File.separator;
			final String webapps = "webapps";
			final String webappsPath = tomcatHome + separator + webapps;
			LOG.info("deployerPath: " + webappsPath);
			
			LOG.info("Delete old file");
			FileDeleteStrategy.FORCE.delete(new File(webappsPath + separator + "deployer.jar"));
			
			ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), ProgramFile.class);
			String jarfilePath = filePathProgram + "/" + file.getFileName();
			LOG.info("Jar File path: " + jarfilePath);
			
			LOG.info("Copy new file");
			FileUtils.copyFile(new File(jarfilePath), new File(webappsPath + separator + "deployer.jar"));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deployTunnel(String id) throws Exception {
		try {
			String tomcatHome = System.getProperty( "catalina.base" );
			String separator = File.separator;
			final String webapps = "webapps";
			final String webappsPath = tomcatHome + separator + webapps;
			LOG.info("deployerPath: " + webappsPath);
			
			try {
				LOG.info("Stop process before restart");
				Socket socket = new Socket("localhost", 8015); 
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
					Thread.sleep(10000);					
				} catch (Exception e2) {}
			}
			
			LOG.info("Delete old file");
			FileDeleteStrategy.FORCE.delete(new File(webappsPath + separator + "tunnel.jar"));
			
			ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), ProgramFile.class);
			String jarfilePath = filePathProgram + "/" + file.getFileName();
			LOG.info("Jar File path: " + jarfilePath);
			
			LOG.info("Copy new file");
			FileUtils.copyFile(new File(jarfilePath), new File(webappsPath + separator + "tunnel.jar"));
			
			LOG.info("Start to execute tunnel");
			ArrayList<String> args = new ArrayList<>();
			args.add("javaw");
			args.add("-jar");
			args.add("tunnel.jar");
			args.addAll(Arrays.asList(file.getCommand().split(" ")));
			
	    	ProcessBuilder pb = new ProcessBuilder(args);
	    	pb.directory(new File(webappsPath));
	    	pb.start();
	    	
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			FileDetail fd = saveFile(uploadedInputStream, fileDetail, date);
			
			LOG.debug("Save new TaskFile");
			ProgramFile programFile = new ProgramFile(fd.fileName, date);
			programFile.setFileSize(fd.fileSize);
			coreTemplate.insert(programFile);
			
			LOG.debug("Save finished");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveDeployer(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			FileDetail fd = saveFile(uploadedInputStream, fileDetail, date);
			
			LOG.debug("Save new TaskFile");
			ProgramFile programFile = new ProgramFile(fd.fileName, date);
			programFile.setIsDeployer(true);
			programFile.setFileSize(fd.fileSize);
			coreTemplate.insert(programFile);
			
			LOG.debug("Save finished");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void saveTunnel(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			ProgramFile file = getLastTunnel();
			
			FileDetail fd = saveFile(uploadedInputStream, fileDetail, date);
			
			LOG.debug("Save new TaskFile");
			ProgramFile programFile = new ProgramFile(fd.fileName, date);
			programFile.setIsTunnel(true);
			programFile.setFileSize(fd.fileSize);
			programFile.setCommand(file == null ? null : file.getCommand());
			coreTemplate.insert(programFile);
			
			LOG.debug("Save finished");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ProgramFile getLastTunnel() {
		Query query = Query.query(Criteria.where("isTunnel").is(true));
		query.with(new Sort(Direction.DESC, "createdDateTime"));
		return coreTemplate.findOne(query, ProgramFile.class);
	}

	public ProgramFileFindCriteriaResp findAll(ProgramFileFindCriteriaReq req) throws Exception {
		try {
			ProgramFileFindCriteriaResp resp = new ProgramFileFindCriteriaResp();
			
			long totalItems = coreTemplate.count(new Query(), NewTaskFile.class);
			
			Query query = new Query(Criteria.where("isDeployer").ne(true).and("isTunnel").ne(true))
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<ProgramFile> files = coreTemplate.find(query, ProgramFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ProgramFileFindCriteriaResp findAllDeployer(ProgramFileFindCriteriaReq req) throws Exception {
		try {
			ProgramFileFindCriteriaResp resp = new ProgramFileFindCriteriaResp();
			
			long totalItems = coreTemplate.count(new Query(), NewTaskFile.class);
			
			Query query = new Query(Criteria.where("isDeployer").is(true))
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<ProgramFile> files = coreTemplate.find(query, ProgramFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ProgramFileFindCriteriaResp findAllTunnel(ProgramFileFindCriteriaReq req) throws Exception {
		try {
			ProgramFileFindCriteriaResp resp = new ProgramFileFindCriteriaResp();
			
			long totalItems = coreTemplate.count(new Query(), NewTaskFile.class);
			
			Query query = new Query(Criteria.where("isTunnel").is(true))
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "createdDateTime"));
			
			List<ProgramFile> files = coreTemplate.find(query, ProgramFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void delete(String id) throws Exception {
		try {			
			LOG.debug("Remove ProgramFile");
			ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), ProgramFile.class);
			coreTemplate.remove(file);
			
			if(!new File(filePathProgram + "/" + file.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + file.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateCommand(ProgramFileFindCriteriaReq req) throws Exception {
		try {
			ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(req.getId())), ProgramFile.class);
			file.setCommand(req.getCommand());
			coreTemplate.save(file);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getFile(ProgramFileFindCriteriaReq req) {
		try {			
			ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(req.getId())), ProgramFile.class);
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
			
}
