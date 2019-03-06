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
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DocumentFindCriteriaReq;
import com.may.ple.backend.criteria.DocumentFindCriteriaResp;
import com.may.ple.backend.criteria.SeizureDataCriteriaReq;
import com.may.ple.backend.entity.Document;
import com.may.ple.backend.entity.Seizure;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;

@Service
public class DocumentService {
	private static final Logger LOG = Logger.getLogger(DocumentService.class.getName());
	private MongoTemplate templateCenter;
	private DbFactory dbFactory;
	@Value("${file.path.exportTemplate}")
	private String filePathExportTemplate;
	
	@Autowired
	public DocumentService(DbFactory dbFactory, MongoTemplate templateCenter) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
	}
	
	public void uploadDoc(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String productId, String contractNo, Integer type, String comment) throws Exception {
		try {
			Date now = Calendar.getInstance().getTime();
			FileDetail fd = FileUtil.getFileName(fileDetail, now);
			
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			
			Document document = new Document();
			document.setCreatedDateTime(now);
			document.setCreatedBy(user.getShowname());
			document.setComment(comment);
			document.setFileName(fd.fileName);
			document.setType(type);
			document.setContractNo(contractNo);
			
			template.save(document);
			
			String path = filePathExportTemplate + "/doc_" + productId;
			File file = new File(path);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			Files.copy(uploadedInputStream, Paths.get(path + "/" + fd.fileName));
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public DocumentFindCriteriaResp findUploadDoc(DocumentFindCriteriaReq req) throws Exception {
		try {
			DocumentFindCriteriaResp resp = new DocumentFindCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Query query = Query.query(Criteria.where("contractNo").is(req.getContractNo()).and("type").is(1));
			long totalItems = template.count(query, Document.class);
			
			if(totalItems == 0) {
				resp.setTotalItems(0l);
				return resp;
			}
			
			query
			.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()))
			.with(new Sort(Direction.DESC, "createdDateTime"));
			
			query.fields()
			.include("fileName")
			.include("comment")
			.include("createdDateTime")
			.include("createdBy");
			
			List<Document> documents = template.find(query, Document.class);			
			
			resp.setTotalItems(totalItems);
			resp.setDocuments(documents);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteDoc(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			
			Document document = template.findOne(Query.query(Criteria.where("id").is(id)), Document.class);
			template.remove(document);
			
			String path = filePathExportTemplate + "/doc_" + productId;
			
			if(!new File(path + "/" + document.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + document.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String getFilePath(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);
			Document document = template.findOne(Query.query(Criteria.where("id").is(id)), Document.class);
			
			return filePathExportTemplate + "/doc_" + productId + "/" + document.getFileName();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateSeizure(SeizureDataCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProdId());
			
			Seizure seizure = template.findOne(Query.query(Criteria.where("contractNo").is(req.getContractNo())), Seizure.class);
			
			if(seizure == null) {
				seizure = new Seizure();
				seizure.setKey(req.getKey());
				seizure.setValue(req.getValue());
				seizure.setContractNo(req.getContractNo());
				template.save(seizure);
			} else {
				Update update = new Update();
				update.set(req.getKey(), req.getValue());
				template.updateFirst(Query.query(Criteria.where("contractNo").is(req.getContractNo())), update, Seizure.class);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
