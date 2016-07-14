package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import com.may.ple.backend.criteria.NoticeFindCriteriaReq;
import com.may.ple.backend.criteria.NoticeFindCriteriaResp;
import com.may.ple.backend.criteria.NoticeUpdateCriteriaReq;
import com.may.ple.backend.entity.NoticeFile;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;

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
	
	public NoticeFindCriteriaResp find(NoticeFindCriteriaReq req) throws Exception {
		try {
			NoticeFindCriteriaResp resp = new NoticeFindCriteriaResp();
			
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			long totalItems = template.count(new Query(), NoticeFile.class);
			
			Query query = new Query()
						  .with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			 			  .with(new Sort(Direction.DESC, "templateName"));
			
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
			
			Files.copy(uploadedInputStream, Paths.get(filePathNotice + "/" + fd.fileName));
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
	
	public void deleteFileTask(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			NoticeFile noticeFile = template.findOne(Query.query(Criteria.where("id").is(id)), NoticeFile.class);
			template.remove(noticeFile);
			
			if(!new File(filePathNotice + "/" + noticeFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + noticeFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
