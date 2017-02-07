package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeFindCriteriaResp;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.entity.NoticeFile;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.ZipUtil;

@Service
public class NoticeUploadService {
	private static final Logger LOG = Logger.getLogger(NoticeUploadService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.notice}")
	private String filePathNotice;
	
	@Autowired
	public NoticeUploadService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public NoticeFindCriteriaResp find(NoticeFindCriteriaReq req, String ...fields) throws Exception {
		try {
			NoticeFindCriteriaResp resp = new NoticeFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			Criteria criteria = new Criteria();
			
			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}
			
			Query query = Query.query(criteria);
			for (String field : fields) {
				query.fields().include(field);					
			}
			
			long totalItems = template.count(query, NoticeFile.class);
			
			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort("templateName"));
			
			List<NoticeFile> files = template.find(query, NoticeFile.class);			
			
			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct, String templateName) throws Exception {		
		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();
			
			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);
			
			LOG.debug("Save new TaskFile");
			NoticeFile noticeFile = new NoticeFile(fd.fileName, templateName, date);
			noticeFile.setCreatedBy(user.getId());
			noticeFile.setUpdateedDateTime(date);
			noticeFile.setEnabled(true);
			template.insert(noticeFile);
			
			File file = new File(filePathNotice);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			String filePathStr = filePathNotice + "/" + fd.fileName;
			
			Files.copy(uploadedInputStream, Paths.get(filePathStr));
			LOG.debug("Save finished");
			
			ZipUtil.unZipToCurrentFolder(new File(filePathStr));
			LOG.debug("Unzip finished");
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateTemplateName(NoticeUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeFile.class);
			noticeFile.setTemplateName(req.getTemplateName());
			
			template.save(noticeFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateEnabled(NoticeUpdateCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeFile.class);
			
			if(noticeFile.getEnabled()) {
				noticeFile.setEnabled(false);
			} else {
				noticeFile.setEnabled(true);
			}
			
			template.save(noticeFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map<String, String> getNoticeFile(NoticeFindCriteriaReq req) {
		try {			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			NoticeFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), NoticeFile.class);
			
			if(noticeFile == null) return null;
			
			String filePath = filePathNotice + "/" + noticeFile.getFileName();
			
			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", noticeFile.getFileName());
			
			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteNoticeFile(String productId, String id) throws Exception {
		try {
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			NoticeFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(id)), NoticeFile.class);
			template.remove(noticeFile);
			
			if(!new File(filePathNotice + "/" + noticeFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + noticeFile.getFileName());
			}
			
			String folder = FilenameUtils.removeExtension(noticeFile.getFileName());
			FileUtils.deleteDirectory(new File(filePathNotice + "/" + folder));
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
