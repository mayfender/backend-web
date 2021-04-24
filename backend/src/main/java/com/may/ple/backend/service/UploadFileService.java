package com.may.ple.backend.service;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
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

	public UploadFileCriteriaResp getFiles(UploadFileCriteriaReq req, List<Integer> status) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			Criteria criteria = Criteria.where("periodId").is(new ObjectId(req.getPeriodId()));
			if(StringUtils.isNoneBlank(req.getCustomerName())) {
				criteria.and("customerName").is(req.getCustomerName());
			}
			if(status != null) {
				criteria.and("status").in(status);
			}

			long totalItems = dealerTemp.count(Query.query(criteria), "orderFile");

			UploadFileCriteriaResp resp = new UploadFileCriteriaResp();
			resp.setTotalItems(totalItems);

			if(totalItems > 0) {
				Query query = Query.query(criteria);
				boolean isIncludeImg = false;
				Sort sort;
				if(req.getIsIncludeImg() != null && req.getIsIncludeImg()) {
					isIncludeImg = true;
					sort = new Sort(Sort.Direction.ASC, "createdDateTime");
//					query.limit(3);
				} else {
					query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
					sort = new Sort(Sort.Direction.DESC, "createdDateTime");
				}

				query.with(sort);
				List<Map> orderFiles = dealerTemp.find(query, Map.class, "orderFile");

				if(isIncludeImg) {
					LOG.debug("Add image data as well.");
					String fileName, periodId;
					for (Map orderFile : orderFiles) {
						fileName = (String)orderFile.get("fileName");
						periodId = ((ObjectId)orderFile.get("periodId")).toString();
						orderFile.put("imgPath", periodId + "/" + req.getDealerId() + "/" + fileName);
					}
				}

				resp.setOrderFiles(orderFiles);
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

	public String viewImage(UploadFileCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getId())));

			Map orderFile = dealerTemp.findOne(query, Map.class, "orderFile");
			String fileName = (String)orderFile.get("fileName");
			String periodId = ((ObjectId)orderFile.get("periodId")).toString();
			String orderFilePath = basePath + "/imageFiles/" + periodId + "/" + req.getDealerId() + "/" + fileName;
			LOG.debug(orderFilePath);

			byte[] fileContent = FileUtils.readFileToByteArray(new File(orderFilePath));
			String encodedString = Base64.getEncoder().encodeToString(fileContent);
			return encodedString;
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

			//---[3]
			long count = dealerTemp.count(Query.query(Criteria.where("periodId").is(new ObjectId(periodId)).and("customerName").is(custName)), "orderFile");
			String countStr = String.format("%04d", count + 1);

			//---[4]
			InputStream targetStream = new ByteArrayInputStream(addTextWatermark(custName + " (" + countStr + ")", fileExtension, uploadedInputStream));
			long fileSize = Files.copy(targetStream, Paths.get(filePathStr));
			BufferedImage bimg = ImageIO.read(new File(filePathStr));

			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			BasicDBObject orderFile = new BasicDBObject();
			orderFile.put("createdBy", authentication.getName());
			orderFile.put("createdDateTime", now);
			orderFile.put("customerName", custName);
			orderFile.put("status", 0);
			orderFile.put("code", countStr);
			orderFile.put("fileName", fileName);
			orderFile.put("fileWidth", bimg.getWidth());
			orderFile.put("fileHeight", bimg.getHeight());
			orderFile.put("size", fileSize);
			orderFile.put("periodId", new ObjectId(periodId));
			orderFile.put("checker", 1);

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

	public List<String> getCustomerNameByPeriod(String periodId, String dealerId, List<Integer> status) {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(dealerId);

			BasicDBObject dbObject = new BasicDBObject();
			dbObject.append("periodId", new ObjectId(periodId));

			if(status != null) {
				dbObject.append("status", new BasicDBObject("$in", status));
			}

			List<String> names = dealerTemp.getCollection("orderFile").distinct("customerName", dbObject);
			Collections.sort(names);

			return names;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public synchronized Map getNextImage(UploadFileCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());

			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Query query = Query.query(Criteria
					.where("periodId").is(new ObjectId(req.getPeriodId()))
					.and("status").is(1)
					.and("proceedBy").is(auth.getName())
					);

			Map orderFile = dealerTemp.findOne(query, Map.class, "orderFile");
			boolean isExit = false;

			if(orderFile != null) {
				LOG.info("Found orderFile on status 1");
				isExit = true;
			} else {
				LOG.info("Get orderFile that status is 0 on delaer: " + req.getDealerId());
				query = Query.query(Criteria.where("periodId").is(new ObjectId(req.getPeriodId())).and("status").is(0));
				query.with(new Sort(Direction.ASC, "createdDateTime"));
				orderFile = dealerTemp.findOne(query, Map.class, "orderFile");
				if(orderFile == null) return null;
			}
			orderFile.put("_id", ((ObjectId)orderFile.get("_id")).toString());

			String periodId = ((ObjectId)orderFile.get("periodId")).toString();
			String fileName = (String)orderFile.get("fileName");
			String fileAbsolutePath = periodId + "/" + req.getDealerId() + "/" + fileName;
			LOG.info(fileAbsolutePath);
			orderFile.put("imgPath", fileAbsolutePath);
			Update update;

			if(isExit) {
				LOG.info("Set flag to show Photoviewer is activing.");
				update = new Update();
				update.set("checker", orderFile.get("checker") == null ? 1 : ((int)orderFile.get("checker")) + 1);
				dealerTemp.updateFirst(Query.query(Criteria.where("_id").is(orderFile.get("_id"))), update, "orderFile");
			} else {
				LOG.info("Get orderFile(0) Update staus to 1");
				update = new Update();
				update.set("status", 1);
				update.set("proceedBy", auth.getName());
				dealerTemp.updateFirst(Query.query(Criteria.where("_id").is(orderFile.get("_id"))), update, "orderFile");
			}

			LOG.info("End getNextImage on dealer: " + req.getDealerId());
			return orderFile;








/*			//---: Update previous image status to release.
			if(StringUtils.isNotBlank(req.getPreviousImgId())) {
				LOG.info("Update previous image staus to release 0");
				update.set("status", 0);
				dealerTemp.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(req.getPreviousImgId()))), update, "orderFile");
			}

			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getId())));
			query.fields().include("status");

			Map ordFile = dealerTemp.findOne(query, Map.class, "orderFile");
			if((int)ordFile.get("status") != 0) {
				throw new CustomerException(500, "This item isn't free.");
			}

			//---: Update current image status to hold.
			update.set("status", 1);
			dealerTemp.updateFirst(query, update, "orderFile");
			LOG.info("Update current image staus to 1");*/
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map getCurrentImage(UploadFileCriteriaReq req) throws Exception {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Query query = Query.query(Criteria
					.where("periodId").is(new ObjectId(req.getPeriodId()))
					.and("status").is(1)
					.and("proceedBy").is(auth.getName())
					);

			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Map orderFile = dealerTemp.findOne(query, Map.class, "orderFile");

			if(orderFile == null) return null;

			orderFile.put("_id", ((ObjectId)orderFile.get("_id")).toString());
			String fileName = (String)orderFile.get("fileName");
			String fileAbsolutePath = req.getPeriodId() + "/" + req.getDealerId() + "/" + fileName;
			LOG.info(fileAbsolutePath);
			orderFile.put("imgPath", fileAbsolutePath);

			return orderFile;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private byte[] addTextWatermark(String text, String type, InputStream source) throws IOException {
		BufferedImage image = ImageIO.read(source);

		// determine image type and handle correct transparency
		int imageType = "png".equalsIgnoreCase(type) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage watermarked = new BufferedImage(image.getWidth(), image.getHeight(), imageType);

		// initializes necessary graphic properties
		Graphics2D w = (Graphics2D) watermarked.getGraphics();
		w.drawImage(image, 0, 0, null);
		AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
		w.setComposite(alphaChannel);
		w.setColor(Color.BLACK);
		w.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
		FontMetrics fontMetrics = w.getFontMetrics();
		Rectangle2D rect = fontMetrics.getStringBounds(text, w);

		// calculate center of the image
		/*int centerX = (image.getWidth() - (int) rect.getWidth()) / 2;
		int centerY = image.getHeight() / 2;*/
		int centerX = (image.getWidth() - (int) rect.getWidth()) - 10;
		int centerY = image.getHeight() - 10;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// add text overlay to the image
		w.drawString(text, centerX, centerY);
		ImageIO.write(watermarked, type, baos);
		w.dispose();

		return baos.toByteArray();
	}

}
