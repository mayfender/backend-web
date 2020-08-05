package com.may.ple.backend.service;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_PAYMENT_DETAIL;
import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_DATE_TIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_PROBATION_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_UPDATED_DATE_TIME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import com.may.ple.backend.bussiness.ExcelReport;
import com.may.ple.backend.bussiness.ImportExcel;
import com.may.ple.backend.criteria.PaymentFindCriteriaReq;
import com.may.ple.backend.criteria.PaymentFindCriteriaResp;
import com.may.ple.backend.criteria.PaymentUpdateCriteriaReq;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.Forecast;
import com.may.ple.backend.entity.PaymentFile;
import com.may.ple.backend.entity.Product;
import com.may.ple.backend.entity.ProductSetting;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.GeneralModel1;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.FileUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.POIExcelUtil;
import com.may.ple.backend.utils.PaymentRulesUtil;
import com.may.ple.backend.utils.StringUtil;

@Service
public class PaymentUploadService {
	private static final Logger LOG = Logger.getLogger(PaymentUploadService.class.getName());
	private DbFactory dbFactory;
	private MongoTemplate templateCenter;
	private UserAction userAct;
	private ExcelReport excelUtil;
	@Value("${file.path.payment}")
	private String filePathPayment;

	@Autowired
	public PaymentUploadService(DbFactory dbFactory, MongoTemplate templateCenter, UserAction userAct, ExcelReport excelUtil) {
		this.dbFactory = dbFactory;
		this.templateCenter = templateCenter;
		this.userAct = userAct;
		this.excelUtil = excelUtil;
	}

	public PaymentFindCriteriaResp find(PaymentFindCriteriaReq req) throws Exception {
		try {
			PaymentFindCriteriaResp resp = new PaymentFindCriteriaResp();

			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			Criteria criteria = new Criteria();

			if(req.getEnabled() != null) {
				criteria.and("enabled").is(req.getEnabled());
			}

			Query query = Query.query(criteria);

			long totalItems = template.count(query, PaymentFile.class);

			query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage())).with(new Sort(Direction.DESC, "createdDateTime"));

			query.fields()
			.include("fileName")
			.include("createdDateTime")
			.include("enabled")
			.include("rowNum");

			List<PaymentFile> files = template.find(query, PaymentFile.class);

			resp.setTotalItems(totalItems);
			resp.setFiles(files);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map<String, Object> save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, String currentProduct,
					 				Boolean isConfirmImport, List<YearType> yearT) throws Exception {
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

			LOG.debug("Get product");
			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(currentProduct)), Product.class);
			List<ColumnFormat> columnFormatsPayment = product.getColumnFormatsPayment();

			if(columnFormatsPayment == null) {
				columnFormatsPayment = new ArrayList<>();
			}

			Sheet sheet = workbook.getSheetAt(0);
			POIExcelUtil.removeSheetExcept0(workbook);
			boolean isFirstTime = false;
			Map<String, Integer> headerIndex;

			if(columnFormatsPayment.size() == 0) {
				LOG.debug("Get Header of excel file");
				headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormatsPayment);
				isFirstTime = true;
			} else {
				LOG.debug("Get Header of excel file");
				headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet);
			}

			if(headerIndex.size() == 0) {
				throw new Exception("headerIndex's size is 0");
			}

			if(!isFirstTime && (isConfirmImport == null || !isConfirmImport)) {
				List<ColumnFormat> colDateTypes = ImportExcel.getColDateType(headerIndex, columnFormatsPayment);
				List<String> colNotFounds = ImportExcel.getColNotFound(headerIndex, columnFormatsPayment);
				Map<String, Object> colData = new HashMap<>();
				colData.put("colDateTypes", colDateTypes);
				colData.put("colNotFounds", colNotFounds);

				if(colDateTypes.size() > 0 || colNotFounds.size() > 0) return colData;
			}

			Users user = ContextDetailUtil.getCurrentUser(templateCenter);
			MongoTemplate template = dbFactory.getTemplates().get(currentProduct);

			String path = filePathPayment + "/" + FileUtil.getPath(currentProduct);
			LOG.debug(path);

			LOG.debug("Save new file");
			PaymentFile paymentFile = new PaymentFile(fd.fileName, date);
			paymentFile.setCreatedBy(user.getId());
			paymentFile.setUpdateedDateTime(date);
			paymentFile.setEnabled(true);
			paymentFile.setFilePath(path);
			template.insert(paymentFile);

			GeneralModel1 saveResult;

			if(isFirstTime) {
				LOG.debug("Save Details");
				saveResult = saveDetailFirstTime(sheet, template, headerIndex, paymentFile.getId(), date, product.getProductSetting());
			} else {
				String paidAmountCol = null;
				for (ColumnFormat cp : columnFormatsPayment) {
					if(cp.getIsSum() != null && cp.getIsSum()) {
						paidAmountCol = cp.getColumnName();
						break;
					}
				}
				LOG.info("paidAmountCol: " + paidAmountCol);
				saveResult = saveDetail(sheet, template, headerIndex, paymentFile.getId(), date, product.getProductSetting(), columnFormatsPayment, yearT, paidAmountCol, currentProduct);
			}

			if(saveResult.rowNum == -1) {
				LOG.debug("Remove taskFile because Saving TaskDetail Error.");
				template.remove(paymentFile);
				throw new CustomerException(4001, "Cann't save taskdetail.");
			}

			//--: Set datatype
			if(saveResult.dataTypes != null) {
				Map<String, String> dataTypes = saveResult.dataTypes;
				Set<String> dataTypeKey = dataTypes.keySet();
				for (String key : dataTypeKey) {
					for (ColumnFormat c : columnFormatsPayment) {
						if(key.equals(c.getColumnName())) {
							if(c.getDataType() == null) {
								c.setDataType(dataTypes.get(key));
							}
							break;
						}
					}
				}
			}

			//--: update rowNum to TaskFile.
			paymentFile.setRowNum(saveResult.rowNum);
			template.save(paymentFile);

			product.setColumnFormatsPayment(columnFormatsPayment);
			templateCenter.save(product);

			File file = new File(path);
			if(!file.exists()) {
				boolean result = file.mkdirs();
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}

			LOG.debug("Write to file");
			fileOut = new FileOutputStream(path + "/" + fd.fileName);
			workbook.write(fileOut);
			LOG.debug("End");

			return null;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(fileOut != null) fileOut.close();
		}
	}

	public void updateEnabled(PaymentUpdateCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			PaymentFile paymentFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), PaymentFile.class);

			if(paymentFile.getEnabled()) {
				paymentFile.setEnabled(false);
			} else {
				paymentFile.setEnabled(true);
			}

			template.save(paymentFile);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Map<String, String> getFile(PaymentFindCriteriaReq req) {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());

			PaymentFile paymentFile = template.findOne(Query.query(Criteria.where("id").is(req.getId())), PaymentFile.class);

			String filePath = paymentFile.getFilePath() + "/" + paymentFile.getFileName();

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

			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			ProductSetting setting = product.getProductSetting();
			Integer autoUpdateBalance = setting.getAutoUpdateBalance();
			PaymentFile paymentFile = template.findOne(Query.query(Criteria.where("id").is(id)), PaymentFile.class);

			if(autoUpdateBalance != null && autoUpdateBalance == 1) {
				String paymentRules = setting.getPaymentRules();
				String balanceCol = setting.getBalanceColumnName();
				String contNoColName = setting.getContractNoColumnName();
				String contNoColNamePay = setting.getContractNoColumnNamePayment();
				Map<String, String> rules = PaymentRulesUtil.getPaymentRules(paymentRules);

				if(rules != null) {
					Query query = new Query();
					query.with(new Sort(Direction.DESC, "createdDateTime"));
					PaymentFile lastPaymentFile = template.findOne(query, PaymentFile.class);

					if(paymentFile.getId().equals(lastPaymentFile.getId())) {
						query = Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id));
						query.fields().include(contNoColNamePay).include(balanceCol);

						for (Map.Entry<String, String> map : rules.entrySet()) {
							query.fields().include(map.getKey());
						}

						List<Map> payments = template.find(query, Map.class, NEW_PAYMENT_DETAIL.getName());
						if(payments == null) return;

						List<String> payContrct = new ArrayList<>();
						Update update;
						for (Map pay : payments) {
							// Get only first payment to update to TaskDetail.
							if(payContrct.contains(pay.get(contNoColNamePay).toString())) continue;
							payContrct.add(pay.get(contNoColNamePay).toString());

							update = new Update();
							update.set(balanceCol, pay.get(balanceCol));

							for (Map.Entry<String, String> map : rules.entrySet()) {
								update.set(map.getKey(), pay.get(map.getKey()));
							}

							query = Query.query(Criteria.where(contNoColName).is(pay.get(contNoColNamePay)));
							template.updateFirst(query, update, NEW_TASK_DETAIL.getName());
						}
					}
				}
			}

			template.remove(paymentFile);
			template.remove(Query.query(Criteria.where(SYS_FILE_ID.getName()).is(id)), NEW_PAYMENT_DETAIL.getName());

			if(!new File(paymentFile.getFilePath() + "/" + paymentFile.getFileName()).delete()) {
				LOG.warn("Cann't delete file " + paymentFile.getFileName());
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private GeneralModel1 saveDetailFirstTime(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex,
										String fileId, Date date, ProductSetting setting) {

		GeneralModel1 result = new GeneralModel1();
		String contNoColName = setting.getContractNoColumnName();
		String contNoColNamePay = setting.getContractNoColumnNamePayment();

		try {
			LOG.debug("Start save taskDetail");
			Set<String> keySet = headerIndex.keySet();
			List<Map<String, Object>> datas = new ArrayList<>();
			Map<String, String> dataTypes = new HashMap<>();
			Map<String, Object> data;
			List<String> ownerIds;
			Map taskDetail;
			Query query;
			Row row;
			int r = 1; //--: Start with row 1 for skip header row.
			Cell cell;
			boolean isLastRow;
			String dtt;

			while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}

				data = new LinkedHashMap<>();
				isLastRow = true;

				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);

					if(cell != null) {
						switch(cell.getCellType()) {
						case Cell.CELL_TYPE_STRING: {
							data.put(key, StringUtil.removeWhitespace(cell.getStringCellValue()));
							dtt = "str";
							break;
						}
						case Cell.CELL_TYPE_BOOLEAN: {
							data.put(key, cell.getBooleanCellValue());
							dtt = "bool";
							break;
						}
						case Cell.CELL_TYPE_NUMERIC: {
								if(HSSFDateUtil.isCellDateFormatted(cell)) {
									data.put(key, cell.getDateCellValue());
									dtt = "date";
								} else {
									data.put(key, cell.getNumericCellValue());
									dtt = "num";
								}
								break;
							}
						default: throw new Exception("Error on column: " + key);
						}

						if(!dataTypes.containsKey(key)) dataTypes.put(key, dtt);
						isLastRow = false;
					} else {
						data.put(key, null);
					}

					if(key.equals(contNoColNamePay)) {
						query = Query.query(Criteria.where(contNoColName).is(data.get(contNoColNamePay)));
						query.fields().include(SYS_OWNER_ID.getName());
						taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());

						if(taskDetail != null) {
							ownerIds = (List)taskDetail.get(SYS_OWNER_ID.getName());
							if(ownerIds != null || ownerIds.size() > 0) {
								data.put(SYS_OWNER_ID.getName(), ownerIds.get(0));
							}
						}
					}
				}

				//--: Break
				if(isLastRow) {
					r--;
					break;
				}

				//--: Add row
				data.put(SYS_FILE_ID.getName(), fileId);
				data.put(SYS_OLD_ORDER.getName(), r);
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);
				datas.add(data);
				r++;
			}

			template.insert(datas, NEW_PAYMENT_DETAIL.getName());
			result.rowNum = r;
			result.dataTypes = dataTypes;

			return result;
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	private GeneralModel1 saveDetail(Sheet sheetAt, MongoTemplate template, Map<String, Integer> headerIndex,
			String fileId, Date date, ProductSetting setting, List<ColumnFormat> columnFormats, List<YearType> yearType,
			String paidAmountCol, String productId) {

		GeneralModel1 result = new GeneralModel1();
		String contNoColName = setting.getContractNoColumnName();
		String contNoColNamePay = setting.getContractNoColumnNamePayment();
		String paidDateCol = setting.getPaidDateColumnNamePayment();
		String balanceCol = setting.getBalanceColumnName();
		Integer autoUpdateBalance = setting.getAutoUpdateBalance();
		int r = 1; //--: Start with row 1 for skip header row.

		try {
			LOG.debug("Start save taskDetail");

			Product product = templateCenter.findOne(Query.query(Criteria.where("id").is(productId)), Product.class);
			List<ColumnFormat> headers = product.getColumnFormats();
			headers = getColumnFormatsActive(headers);
			List<Users> users = userAct.getUserByProductToAssign(productId).getUsers();
			Map<String, String> rules = PaymentRulesUtil.getPaymentRules(setting.getPaymentRules());
			List<Map<String, Object>> datas = new ArrayList<>();
			Map<String, Map> taskDetailId = new HashMap<>();
			Date paidDateStart, paidDateEnd;
			Map<String, Object> data;
			Map taskDetail = null;
			List<String> ownerIds;
			boolean isLastRow;
			Object balanceValObj;
			double balanceVal;
			Update update;
			Query query;
			Field field;
			Cell cell;
			Row row;

			row : while(true) {
				row = sheetAt.getRow(r);
				if(row == null) {
					r--;
					break;
				}

				data = new LinkedHashMap<>();
				isLastRow = true;

				for (ColumnFormat colForm : columnFormats) {
					if(!headerIndex.containsKey(colForm.getColumnName())) continue;

					cell = row.getCell(headerIndex.get(colForm.getColumnName()), MissingCellPolicy.RETURN_BLANK_AS_NULL);

					if(cell != null) {
						data.put(colForm.getColumnName(), excelUtil.getValue(cell, colForm.getDataType(), yearType, colForm.getColumnName()));
						isLastRow = false;
					} else {
						if(colForm.getColumnName().equals(paidAmountCol)) {
							break;
						}
						data.put(colForm.getColumnName(), null);
					}

					if(colForm.getColumnName().equals(contNoColNamePay)) {
						query = Query.query(Criteria.where(contNoColName).is(data.get(contNoColNamePay)));
						field = query.fields();
						field.include(SYS_OWNER_ID.getName());
						field.include(SYS_PROBATION_OWNER_ID.getName());

						for (ColumnFormat cf : headers) {
							field.include(cf.getColumnName());
						}

						if(rules != null) {
							for (Map.Entry<String, String> map : rules.entrySet()) {
								field.include(map.getKey());
							}
						}

						if(taskDetailId.containsKey(data.get(contNoColNamePay).toString())) {
							taskDetail = taskDetailId.get(data.get(contNoColNamePay).toString());
						} else {
							taskDetail = template.findOne(query, Map.class, NEW_TASK_DETAIL.getName());
						}

						if(taskDetail == null) {
							LOG.info("Not found Contract No. : " + data.get(contNoColNamePay));
							r++;
							continue row;
						}

						ownerIds = (List)taskDetail.get(SYS_OWNER_ID.getName());
						if(ownerIds != null && ownerIds.size() > 0) {
							List<Map<String, String>> userList = MappingUtil.matchUserId(users, ownerIds.get(0));
							Map u = (Map)userList.get(0);

							data.put(SYS_OWNER_ID.getName(), ownerIds.get(0));
							taskDetail.put(SYS_OWNER.getName(), u.get("showname"));
							data.put("taskDetail", taskDetail);
							taskDetailId.put(data.get(contNoColNamePay).toString(), taskDetail);
						}
					}
				}

				//--: Break
				if(isLastRow) {
					r--;
					break;
				}

				//--: Add row
				data.put(SYS_FILE_ID.getName(), fileId);
				data.put(SYS_OLD_ORDER.getName(), r);
				data.put(SYS_CREATED_DATE_TIME.getName(), date);
				data.put(SYS_UPDATED_DATE_TIME.getName(), date);

				datas.add(data);
				r++;
			}

			if(datas.size() > 0) {
				for (Map d : datas) {
					taskDetail = (Map)d.get("taskDetail");
					if(taskDetail == null) continue;

					//---: Update paid data to Forecast Document
					update = new Update();
					update.set("paidAmount", d.get(paidAmountCol));
					update.set("paidAmountUpdatedDateTime", date);

					paidDateStart = DateUtils.truncate((Date)d.get(paidDateCol), Calendar.DAY_OF_MONTH);
					paidDateEnd = DateUtils.addDays(paidDateStart, 1);
					template.updateFirst(Query.query(Criteria.where("contractNo").is(d.get(contNoColNamePay)).and("appointDate").gte(paidDateStart).lt(paidDateEnd)), update, Forecast.class);
					//---: Update paid data to Forecast Document

					//---: Update payment to main task
					if(autoUpdateBalance != null && autoUpdateBalance.equals(1)) {
						if((balanceValObj = taskDetail.get(balanceCol)) != null) {
							balanceVal = (double)balanceValObj - (double)(d.get(paidAmountCol));
							taskDetail.put(balanceCol, balanceVal);
							d.put(balanceCol, balanceValObj);

							if(rules != null) {
								update = new Update();
								update.set(balanceCol, balanceVal);

								for (Map.Entry<String, String> map : rules.entrySet()) {
									update.set(map.getKey(), d.get(map.getValue()));
									d.put(map.getKey(), taskDetail.get(map.getKey()));
								}

								template.updateFirst(Query.query(Criteria.where("_id").is(taskDetail.get("_id"))), update, NEW_TASK_DETAIL.getName());
							}
						}
					}
				}

				template.insert(datas, NEW_PAYMENT_DETAIL.getName());
			}
			result.rowNum = datas.size();

			return result;
		} catch (Exception e) {
			LOG.error("Row : " + r +" " + e.toString(), e);
			result.rowNum = -1;
			return result;
		}
	}

	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();

		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}

		return result;
	}

}
