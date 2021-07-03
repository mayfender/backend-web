package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_AMOUNT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_APPOINT_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NEXT_TIME_DATE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_RESULT_TEXT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TEL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_TRACE_DATE;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaReq;
import com.may.ple.backend.criteria.TraceResultImportFindCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.DymList;
import com.may.ple.backend.entity.DymListDet;
import com.may.ple.backend.entity.PaymentFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.TraceResultImportFile;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.TraceWorkAPIUpload;
import com.may.ple.backend.entity.TraceWorkOld;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.StringUtil;
import com.may.ple.backend.utils.YearUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class TraceResultImportService {
	private static final Logger LOG = Logger.getLogger(TraceResultImportService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	@Value("${file.path.payment}")
	private String filePathPayment;
	private UserAction userAct;

	@Autowired
	public TraceResultImportService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
	}

	public TraceResultImportFindCriteriaResp find(TraceResultImportFindCriteriaReq req) throws Exception {
		try {
			TraceResultImportFindCriteriaResp resp = new TraceResultImportFindCriteriaResp();

			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Criteria criteria = new Criteria();

			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}

			Query query = Query.query(criteria);

			long totalItems = template.count(query, TraceResultImportFile.class);

			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort(Direction.DESC, "createdDateTime"));

			List<TraceResultImportFile> files = template.find(query, TraceResultImportFile.class);
			long totalNum, proceededNum;

			for (TraceResultImportFile traceFile : files) {
				if(traceFile.getIsAPIUpload() == null || !traceFile.getIsAPIUpload()) continue;

				LOG.info("Count trace is proceeded.");
				totalNum = traceFile.getRowNum().longValue();
				proceededNum = template.count(Query.query(Criteria.where("fileId").is(traceFile.getId())), "traceWorkAPIUpload");
				traceFile.setProceededCound(totalNum - proceededNum);
			}

			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct, Boolean isAPIUpload) throws Exception {
		Workbook workbook = null;
		FileOutputStream fileOut = null;

		try {
			LOG.debug("Start Save");
			Date date = Calendar.getInstance().getTime();

			LOG.debug("Get Filename");
			FileDetail fd = FileUtil.getFileName(fileDetail, date);

			LOG.debug("File ext: " + fd.fileExt);
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}

			Sheet sheet = workbook.getSheetAt(0);

			LOG.debug("Get Header of excel file");
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeaderIndex(sheet);

			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);

			LOG.debug("Save new file");
			TraceResultImportFile file = new TraceResultImportFile(fd.fileName, date);
			file.setCreatedBy(user.getId());
			file.setUpdateedDateTime(date);
			file.setIsAPIUpload(isAPIUpload);
			file.setRowNum(0);
			template.insert(file);

			LOG.debug("Save Details");
			GeneralModel1 saveResult = saveDetail(sheet, template, headerIndex, file.getId(), currentProduct, isAPIUpload);

			if(saveResult.rowNum == -1) {
				LOG.debug("Remove taskFile because Saving TaskDetail Error.");
				template.remove(file);
				throw new CustomerException(4001, "Cann't save taskdetail.");
			}

			//--: update rowNum to TaskFile.
			file.setRowNum(saveResult.rowNum);
			file.setIsOldTrace(saveResult.isOldTrace);
			template.save(file);

			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(fileOut != null) fileOut.close();
		}
	}

	public Map<String, String> getFile(PaymentFindCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			PaymentFile paymentFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), PaymentFile.class);

			String filePath = filePathPayment + "/" + paymentFile.getFileName();

			Map<String, String> map = new HashMap<>();
			map.put("filePath", filePath);
			map.put("fileName", paymentFile.getFileName());

			return  map;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public void deleteFileTask(String productId, String id) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(productId);

			TraceResultImportFile file = template.findOne(Query.query(Criteria.where("id").is(id)), TraceResultImportFile.class);
			template.remove(file);

			if(file.getIsOldTrace() != null && file.getIsOldTrace()) {
				template.remove(Query.query(Criteria.where("fileId").is(id)), TraceWorkOld.class);
			} else {
				template.remove(Query.query(Criteria.where("fileId").is(id)), TraceWork.class);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public GeneralModel1 saveDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex, String fileId, String productId, Boolean isAPIUpload) {
		GeneralModel1 result = new GeneralModel1();
		int r = 1; //--: Start with row 1 for skip header row.
		int firstFoundRow = 0;

		try {
			LOG.debug("Start save taskDetail");
			Set<String> keySet = headerIndex.keySet();

			if(!keySet.contains("resultText")) return result;

			List<Map> traceWorks = new ArrayList<>();
			Date dummyDate = new Date(Long.MAX_VALUE);
			Map traceWork;

			Integer type;
			Map<String, Object> dymLstDetMap;
			Map<String, Map<String, Object>> dymList = getDymList(template);

			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			String contractNoColumnName = product.getProductSetting().getContractNoColumnName();
			List<ColumnFormat> columnFormats = product.getColumnFormats();
			columnFormats = getColumnFormatsActive(columnFormats);

			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();

			List<Map<String, String>> userList;
			List<DymListDet> dymLstDets;
			boolean isOldTrace = false;
			List<String> ownerId;
			boolean isLastRow;
			Date cellDateVal;
			Map taskDetail;
			String cellVal;
			Date date, now;
			Field fields;
			Map userMap;
			Map dateMap;
			Query query;
			Cell cell;
			Row row;

			row: while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}

				traceWork = new HashMap<>();
				traceWork.put("isImported", true);

				dateMap = new HashMap<>();
				isLastRow = true;
				taskDetail = null;

				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);

					if(cell != null) {
						if(key.equals("contractNo") || key.equals("resultText") || key.equals("tel")) {
							cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell));

							if(StringUtils.isBlank(cellVal) && key.equals("contractNo")) {
								LOG.info("contractNo is empty on row : " + r);
								break row;
							}

							traceWork.put(key, cellVal);

							if(key.equals("contractNo")) {
								query = Query.query(Criteria.where(contractNoColumnName).is(cellVal));
								fields = query.fields()
								.include(SYS_OWNER_ID.getName())
								//---: Below date using for updating the taskdetail
								.include(SYS_TRACE_DATE.getName())
								.include(SYS_APPOINT_DATE.getName())
								.include(SYS_NEXT_TIME_DATE.getName());

								for (ColumnFormat colForm : columnFormats) {
									fields.include(colForm.getColumnName());
								}

								taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
								if(taskDetail != null) {
									ownerId = (List)taskDetail.get(SYS_OWNER_ID.getName());

									if(ownerId == null) {
										r++;
										continue row;
									}

									userList = MappingUtil.matchUserId(users, ownerId.get(0));

									if(userList != null && userList.size() > 0) {
										userMap = (Map)userList.get(0);
										taskDetail.put(SYS_OWNER.getName(), userMap.get("showname"));
										traceWork.put("taskDetail", taskDetail);
										traceWork.put("createdBy", userMap.get("id"));
										traceWork.put("createdByName", userMap.get("showname"));

										if(firstFoundRow == 0) firstFoundRow = r;
									} else {
										r++;
										continue row;
									}
								} else {
									r++;
									continue row;
								}
							}
						} else if(key.equals("createdDateTime") || key.equals("nextTimeDate") || key.equals("appointDate")) {
							cellDateVal = cell.getDateCellValue();

							if(cellDateVal == null) continue;

							if(key.equals("createdDateTime")) {
								cellDateVal = YearUtil.buddToGre(cellDateVal);
							}

							traceWork.put(key, cellDateVal);

							if(taskDetail != null) {
								if(key.equals("createdDateTime")) {
									date = (Date)taskDetail.get(SYS_TRACE_DATE.getName());
									if(date != null && (dummyDate.equals(date) || cellDateVal.after(date))) {
										dateMap.put(SYS_TRACE_DATE.getName(), cellDateVal);
									}
								} else if(key.equals("appointDate")) {
									if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
										dateMap.put(SYS_APPOINT_DATE.getName(), cellDateVal);
									}
								} else {
									if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
										dateMap.put(SYS_NEXT_TIME_DATE.getName(), cellDateVal);
									}
								}
							}
						} else if(key.equals("appointAmount")) {
							traceWork.put(key, cell.getNumericCellValue());
							if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
								dateMap.put(SYS_APPOINT_AMOUNT.getName(), cell.getNumericCellValue());
							}
						} else if(key.endsWith("_sys")) {
							key = key.substring(0, key.indexOf("_sys"));

							if(dymList.containsKey(key)) {
								dymLstDetMap = dymList.get(key);
								type = (Integer)dymLstDetMap.get("type");

								if(type != null && type.intValue() > 1) {
									if(type.intValue() == 2) {
										cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell));
										traceWork.put(key, cellVal);
										dateMap.put(key, cellVal);
									} else if(type.intValue() == 3) {
										traceWork.put(key, cell.getNumericCellValue());
										dateMap.put(key, cell.getNumericCellValue());
									} else if(type.intValue() == 4) {
										cellDateVal = cell.getDateCellValue();
										cellDateVal = YearUtil.buddToGre(cellDateVal);
										traceWork.put(key, cellDateVal);
										dateMap.put(key, cellDateVal);
									}
								} else {
									cellVal = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell));
									dymLstDets = (List<DymListDet>)dymLstDetMap.get("dymLstDets");
									for (DymListDet det : dymLstDets) {
										if((!StringUtils.isBlank(det.getCode()) && det.getCode().equals(cellVal)) ||
												(!StringUtils.isBlank(det.getMeaning()) && det.getMeaning().equals(cellVal))) {

											traceWork.put(key, new ObjectId(det.getId()));
											if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
												dateMap.put(key, new ObjectId(det.getId()));
											}
											break;
										}
									}
								}
							}
						} else if(key.equals("isOldTrace")) {
							if(r == firstFoundRow) isOldTrace = true;

							traceWork.put(key, cell.getBooleanCellValue());
						} else if(key.equals("isReadOnly")) {
							traceWork.put(key, cell.getBooleanCellValue());
						}

						isLastRow = false;
					} else {
						if(key.equals("contractNo")) {
							LOG.info("contractNo is empty on row : " + r);
							break row;
						} else if(key.equals("resultText")) {
							r++;
							continue row;
						} else if(key.equals("createdDateTime")) {
							date = (Date)taskDetail.get(SYS_TRACE_DATE.getName());
							now = Calendar.getInstance().getTime();
							traceWork.put(key, now);

							if(date != null && (dummyDate.equals(date) || now.after(date))) {
								dateMap.put(SYS_TRACE_DATE.getName(), now);
							}
						} else if(key.equals("appointDate")) {
							if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
								dateMap.put(SYS_APPOINT_DATE.getName(), dummyDate);
							}
						} else if(key.equals("nextTimeDate")) {
							if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
								dateMap.put(SYS_NEXT_TIME_DATE.getName(), dummyDate);
							}
						} else if(key.equals("appointAmount")) {
							if(dateMap.containsKey(SYS_TRACE_DATE.getName())) {
								dateMap.put(SYS_APPOINT_AMOUNT.getName(), null);
							}
						}
					}
				}

				//--: Break
				if(isLastRow) {
					r--;
					break;
				}

				//---:
				if(!isOldTrace) {
					if(isAPIUpload != null) {
						String uploadStatus = "DMS_IGN", uploadDesc = "ไม่ส่งไปที่ API";
						if(isAPIUpload) {
							uploadStatus = "DMS_100";
							uploadDesc = "รอกระบวนการส่งไปที่ API";
							traceWork.put("createdDateTime", Calendar.getInstance().getTime());
						}
						traceWork.put("uploadStatusCode", uploadStatus);
						traceWork.put("uploadStatusDesc", uploadDesc);
					}
				}

				//---:
				if(!traceWork.containsKey("uploadStatusCode") || traceWork.get("uploadStatusCode").equals("DMS_IGN")) {
					if(!(traceWork.containsKey("isOldTrace") && traceWork.get("isOldTrace") != null && (boolean)traceWork.get("isOldTrace"))) {
						Set<String> dateSet = dateMap.keySet();
						Update update = new Update();

						for (String dateKey : dateSet) {
							update.set(dateKey, dateMap.get(dateKey));
						}
						if(dateMap.size() > 0) {
							update.set(SYS_RESULT_TEXT.getName(), traceWork.get("resultText"));
							update.set(SYS_TEL.getName(), traceWork.get("tel"));
							template.updateFirst(Query.query(Criteria.where("_id").is(taskDetail.get("_id"))), update, NEW_TASK_DETAIL.getName());
						}
					}
				}

				//--: Save
				traceWork.put("fileId", fileId);
				traceWork.put("isHold", false);

				traceWorks.add(traceWork);
				r++;
			}

			if(isOldTrace) {
				template.insert(traceWorks, TraceWorkOld.class);
				result.isOldTrace = true;

				boolean isExis = template.collectionExists(TraceWorkOld.class);
				if(isExis) {
					DBCollection collection = template.getCollection("traceWorkOld");
					collection.createIndex(new BasicDBObject("createdDateTime", 1));
					collection.createIndex(new BasicDBObject("contractNo", 1));
				}
			} else if(isAPIUpload != null && isAPIUpload) {
				template.insert(traceWorks, TraceWorkAPIUpload.class);

				boolean isExis = template.collectionExists(TraceWorkAPIUpload.class);
				if(isExis) {
					DBCollection collection = template.getCollection("traceWorkAPIUpload");
					collection.createIndex(new BasicDBObject("createdDateTime", 1));
					collection.createIndex(new BasicDBObject("fileId", 1));
				}
			} else {
				template.insert(traceWorks, TraceWork.class);
			}
			result.rowNum = traceWorks.size();

			return result;
		} catch (Exception e) {
			LOG.error("Error on row: " + (r-1));
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}

	private Map<String, Map<String, Object>> getDymList(MongoTemplate template) {
		List<DymList> find = template.find(new Query(), DymList.class);
		Map<String, Map<String, Object>> dymLst = new HashMap<>();
		List<DymListDet> dymLstDets;
		Map<String, Object> data;

		for (DymList dymList : find) {
			dymLstDets = template.find(Query.query(Criteria.where("listId").is(new ObjectId(dymList.getId()))), DymListDet.class);

			data = new HashMap<>();
			data.put("dymLstDets", dymLstDets);
			data.put("type", dymList.getType());

			dymLst.put(dymList.getFieldName(), data);
		}

		return dymLst;
	}

	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		if(columnFormats == null) return null;

		List<ColumnFormat> result = new ArrayList<>();

		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}

		return result;
	}

}
