package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	/*public static void main(String[] args) {
		try {
			LOG.info("Start to execute deployer");
	    	String[] cmd = { "javaw", "-jar", "deployer.jar"};
//			String[] cmd = { "cmd", "/c", "start", "test.bat" };
	    	ProcessBuilder pb = new ProcessBuilder(cmd);
	    	pb.directory(new File("D:\\Server_Container\\tomcat\\apache-tomcat-8.5.12\\webapps"));
	    	pb.environment().put("_RUNJAVA", "");
	    	pb.environment().put("_RUNJDB", "");
	    	pb.environment().put("JSSE_OPTS", "");
	    	pb.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public void deploy(String id) throws Exception {
		try {
			if(true) {
				
				try { 
				    Socket socket = new Socket("localhost", 8005); 
				    if (socket.isConnected()) {
				    	LOG.info("Socket connected");
				    	String separator = File.separator;
				    	final String deployerPath = System.getProperty( "catalina.base" ) + separator + "webapps";
						LOG.info("deployerPath: " + deployerPath);
				    	
						LOG.info("Start to execute deployer");
				    	String[] cmd = { "javaw", "-jar", "deployer.jar"};
				    	ProcessBuilder pb = new ProcessBuilder(cmd);
				    	pb.directory(new File(deployerPath));
				    	pb.environment().put("_RUNJAVA", "");
				    	pb.environment().put("_RUNJDB", "");
				    	pb.environment().put("JSSE_OPTS", "");
				    	pb.start();
						
						/*LOG.info("Start to execute deployer");
						DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
						CommandLine cmdLine = CommandLine.parse("javaw -jar " + deployerPath);
						Executor executor = new DefaultExecutor();
						executor.execute(cmdLine, resultHandler);*/
						
				    	LOG.info("Start to shutdown tomcat");
				        PrintWriter pw = new PrintWriter(socket.getOutputStream(), true); 
				        pw.println("SHUTDOWN"); //send shut down command 
				        pw.close(); 
				        socket.close(); 
				    }
				} catch (Exception e) { 
					LOG.error(e.toString());
					throw e;
				}
				
				LOG.info("Finished");
				return;
			}
			
			
			/*ProgramFile file = coreTemplate.findOne(Query.query(Criteria.where("id").is(id)), ProgramFile.class);
			String filePath = filePathProgram + "/" + file.getFileName();
			LOG.info("File path: " + filePath);
			
			String deployerPath = context.getRealPath("/WEB-INF/lib/deployer.jar");
			LOG.info("deployerPath: " + deployerPath);
			
			// Run a java app in a separate system process
			Process proc = Runtime.getRuntime().exec("java -jar " + deployerPath);
			BufferedReader reader = null;
			String line = null;
			
			try {
				reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
	            
	            while ((line = reader.readLine()) != null) {
	            	LOG.info(line);
	            }
			} catch (Exception e) {
				LOG.error(e.toString());
			} finally {
				if(reader != null) reader.close();				
			}
            
			try {
	            reader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
	            line = null;
	            
	            while ((line = reader.readLine()) != null) {
	            	LOG.info(line);
	            }		
			} catch (Exception e) {
				LOG.error(e.toString());
			} finally {
				if(reader != null) reader.close();
			}*/
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			LOG.debug("Save new TaskFile");
			ProgramFile programFile = new ProgramFile(fd.fileName, date);
			coreTemplate.insert(programFile);
			
			File file = new File(filePathProgram);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			String filePathStr = filePathProgram + "/" + fd.fileName;
			
			Files.copy(uploadedInputStream, Paths.get(filePathStr));
			LOG.debug("Save finished");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ProgramFileFindCriteriaResp findAll(ProgramFileFindCriteriaReq req) throws Exception {
		try {
			ProgramFileFindCriteriaResp resp = new ProgramFileFindCriteriaResp();
			
			long totalItems = coreTemplate.count(new Query(), NewTaskFile.class);
			
			Query query = new Query()
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
			
}
