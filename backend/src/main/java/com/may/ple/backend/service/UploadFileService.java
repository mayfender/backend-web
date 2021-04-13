package com.may.ple.backend.service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.constant.RolesConstant;
import com.may.ple.backend.criteria.UploadFileCriteriaReq;
import com.may.ple.backend.criteria.UploadFileCriteriaResp;
import com.may.ple.backend.model.DbFactory;
import com.mongodb.BasicDBObject;

@Service
public class UploadFileService {
	private static final Logger LOG = Logger.getLogger(UploadFileService.class.getName());
	private MongoTemplate template;
	private OrderService ordService;
	private DbFactory dbFactory;
	@Value("${file.path.base}")
	private String basePath;

	@Autowired
	public UploadFileService(MongoTemplate template, DbFactory dbFactory, OrderService ordService) {
		this.template = template;
		this.ordService = ordService;
		this.dbFactory = dbFactory;
	}

	public UploadFileCriteriaResp getFiles(UploadFileCriteriaReq req) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			List<Integer> staus = new ArrayList<>();
			if(req.getStatus() != null) {
				staus.add(req.getStatus());
			} else {
				staus.add(0);
				staus.add(1);
				staus.add(2);
			}

			Criteria criteria = Criteria.where("periodId").is(new ObjectId(req.getPeriodId())).and("status").in(staus);
			if(StringUtils.isNoneBlank(req.getCustomerName())) {
				criteria.and("customerName").is(req.getCustomerName());
			}

			long totalItems = dealerTemp.count(Query.query(criteria), "orderFile");

			UploadFileCriteriaResp resp = new UploadFileCriteriaResp();
			resp.setTotalItems(totalItems);

			if(totalItems > 0) {
				Query query = Query.query(criteria).with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
				query.with(new Sort(Sort.Direction.DESC, "createdDateTime"));
				resp.setOrderFiles(dealerTemp.find(query, Map.class, "orderFile"));
				return resp;
			}

			return null;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public int removeFile(UploadFileCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getId())));

			Map orderFile = dealerTemp.findOne(query, Map.class, "orderFile");
			int status = (int)orderFile.get("status");

			Update update = new Update();
			update.set("status", 9);

			dealerTemp.updateFirst(query, update, "orderFile");
			return status;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void saveFile(InputStream uploadedInputStream,
						FormDataContentDisposition fileDetail,
						String periodId, String dealerId, String custName) throws Exception {
		try {
			//---[1]
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);
			ordService.saveCustomerName(dealerTemp, RolesConstant.ROLE_ADMIN.getId(), null, custName);

			//---[2] Save file to disk.
			String orderFilePath = basePath + "/imageFiles/" + periodId + "/" + dealerId;
			File file = new File(orderFilePath);
			if(!file.exists()) {
				boolean result = file.mkdirs();
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}

			String fileBaseName = FilenameUtils.getBaseName(fileDetail.getFileName());
			fileBaseName = new String(fileBaseName.getBytes("iso-8859-1"), "UTF-8");
			String fileExtension = FilenameUtils.getExtension(fileDetail.getFileName());
			Date now = Calendar.getInstance().getTime();
			String fileName = fileBaseName + "_" + String.format("%1$tH%1$tM%1$tS%1$tL", now) + "." + fileExtension;

			String filePathStr = orderFilePath + "/" + fileName;
			long fileSize = Files.copy(uploadedInputStream, Paths.get(filePathStr));

			//---[3]
			long count = dealerTemp.count(Query.query(Criteria.where("periodId").is(new ObjectId(periodId)).and("customerName").is(custName)), "orderFile");

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			BasicDBObject orderFile = new BasicDBObject();
			orderFile.put("createdBy", authentication.getName());
			orderFile.put("createdDateTime", now);
			orderFile.put("customerName", custName);
			orderFile.put("status", 0);
			orderFile.put("code", String.format("%04d", count + 1));
			orderFile.put("fileName", fileName);
			orderFile.put("size", fileSize);
			orderFile.put("periodId", new ObjectId(periodId));

			dealerTemp.insert(orderFile, "orderFile");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map<String, Object> getLastPeriod() {
		try {
			Query query = new Query();
			query.with(new Sort(Sort.Direction.DESC, "periodDateTime"));
			return template.findOne(query, Map.class, "period");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public List<String> getCustomerNameByPeriod(String periodId, String dealerId) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			BasicDBObject dbObject = new BasicDBObject();
			dbObject.append("periodId", new ObjectId(periodId));

			List<String> names = dealerTemp.getCollection("orderFile").distinct("customerName", dbObject);
			Collections.sort(names);

			return names;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
