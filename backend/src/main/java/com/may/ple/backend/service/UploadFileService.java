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
						String periodId, String dealerId,
						String custName, String sendRoundId) throws Exception {
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
			orderFile.put("sendRoundId", new ObjectId(sendRoundId));

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

	public Map getNextImage(UploadFileCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getOrderFileId())));
			Map orderFile = dealerTemp.findOne(query, Map.class, "orderFile");

			String periodId = ((ObjectId)orderFile.get("periodId")).toString();
			String fileName = (String)orderFile.get("fileName");
			String fileAbsolutePath = periodId + "/" + req.getDealerId() + "/" + fileName;
			orderFile.put("imgPath", fileAbsolutePath);
			LOG.info(fileAbsolutePath);

			Update update = new Update();
			update.set("checker", ((int)orderFile.get("checker")) + 1);
			dealerTemp.updateFirst(query, update, "orderFile");

			return orderFile;
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

	public void rotateImg(UploadFileCriteriaReq req) throws Exception {
		try {
			MongoTemplate dealerTemp = dbFactory.getTemplates().get(req.getDealerId());
			Query query = Query.query(Criteria.where("_id").is(new ObjectId(req.getId())));
			Map orderFile = dealerTemp.findOne(query, Map.class, "orderFile");
			String fileName = (String)orderFile.get("fileName");
			String fileExtension = FilenameUtils.getExtension(fileName);
			String fileAbsolutePath = basePath + "/imageFiles/" + req.getPeriodId() + "/" + req.getDealerId() + "/" + fileName;

			BufferedImage img = rotate(ImageIO.read(new File(fileAbsolutePath)));
			ImageIO.write(img, fileExtension, new File(fileAbsolutePath));

			Update update = new Update();
			update.set("fileWidth", img.getWidth());
			update.set("fileHeight", img.getHeight());

			dealerTemp.updateFirst(query, update, "orderFile");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	/*public static void main(String[] args) throws IOException {
		String imgPath = "C:\\Users\\LENOVO\\Desktop\\gambler\\imageFiles\\6080ca9956665b28542a56cd\\5f37e3f3c91cef2718b99c1d\\EA374EC7-40A6-4AEF-A5CF-937314A8A338-L0-001_073212123.jpg";
		BufferedImage orignalImg = ImageIO.read(new File(imgPath));

		BufferedImage SubImg = rotate(orignalImg);

		File outputfile = new File("C:\\Users\\LENOVO\\Desktop\\gambler\\imageFiles\\6080ca9956665b28542a56cd\\5f37e3f3c91cef2718b99c1d\\test.jpg");

		ImageIO.write(SubImg, "jpg", outputfile);
	}*/

	public static BufferedImage rotate(BufferedImage img) {
		// Getting Dimensions of image
		int width = img.getWidth();
		int height = img.getHeight();

		// Creating a new buffered image
		// BufferedImage newImage = new BufferedImage(img.getWidth(), img.getHeight(),
		// img.getType());
		BufferedImage newImage = new BufferedImage(height, width, img.getType());

		// creating Graphics in buffered image
		Graphics2D g2 = newImage.createGraphics();

		g2.translate((height - width) / 2, (height - width) / 2);
		g2.rotate(Math.PI / 2, height / 2, width / 2);
		g2.drawImage(img, null, 0, 0);

		return newImage;
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
