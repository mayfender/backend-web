package com.may.ple.backend.service;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.DocumentFindCriteriaReq;
import com.may.ple.backend.criteria.DocumentFindCriteriaResp;
import com.may.ple.backend.entity.Document;
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
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(id)));
			template.remove(query, Document.class);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
