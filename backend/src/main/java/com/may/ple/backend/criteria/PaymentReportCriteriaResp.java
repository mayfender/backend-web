package com.may.ple.backend.criteria;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_COUNT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NOW_DATETIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.service.PaymentDetailService;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.PdfUtil;
import com.may.ple.backend.utils.StringUtil;
import com.may.ple.backend.utils.ZipUtil;
import com.mongodb.BasicDBObject;

public class PaymentReportCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(PaymentReportCriteriaResp.class.getName());
	private PaymentDetailService paymentService;
	private PaymentDetailCriteriaReq req;
	private boolean isFillTemplate;
	private UserAction userAct;
	private String filePath;
	private String filePathTemp;
	private Integer pocModule;
	private String wkhtmltopdfPath;
	
	private List<HeaderHolderResp> getHeader(XSSFSheet sheet) {
		try {
			int startRow = 1;
			int cellIndex = 0;
			int countNull = 0;
			XSSFRow row = null;
			XSSFRow rowCopy = null;
			XSSFCell cell = null;
			BasicDBObject fields = new BasicDBObject();
			List<HeaderHolderResp> result = new ArrayList<>();
			Map<String, HeaderHolder> header;
			HeaderHolder headerHolder;
			String[] headers, delimiters, yearTypes;
			String colName, delimiter = null, yearType = null;
			
			while((row = sheet.getRow(startRow++)) != null) {
				header = new LinkedHashMap<>();
				
				while(true) {
					cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);				
					
					if(countNull == 10) break;
				
					if(cell == null) {
						countNull++;
						continue;
					} else {
						countNull = 0;
					}
					
					colName = StringUtil.removeWhitespace(new DataFormatter().formatCellValue(cell)); 
					
					if(colName.startsWith("${")) {
						if(rowCopy == null) rowCopy = row;
						
						colName = colName.replace("${", "").replace("}", "");
						headers = colName.split("&");
						
						headerHolder = new HeaderHolder();
						colName = headers[0];
						
						if(colName.contains("#")) {
							delimiters = colName.split("#");
							colName = delimiters[0];				
							yearTypes = delimiters[1].split("\\^");
							delimiter = yearTypes[0];
							yearType = yearTypes[1];
						}
						
						if(headers.length > 1) {
							headerHolder.type = headers[1];
							
							if(headers.length > 2) {
								headerHolder.format = headers[2];
								
								if(headers.length > 3) {
									headerHolder.emptySign = headers[3];
								}
							}
						}
						
						fields.append(colName.equals("createdDate") || colName.equals("createdTime") ? "createdDateTime" : colName, 1);
						headerHolder.index = cellIndex - 1;
						
						if(header.containsKey(colName)) {							
							header.put(colName + "_" + headerHolder.index, headerHolder);
						} else {							
							header.put(colName, headerHolder);
						}
					}							
				}
				
				if(header.size() > 0) {				
					result.add(new HeaderHolderResp(header, fields, rowCopy, delimiter, yearType));
				}
				
				countNull = 0;
				cellIndex = 0;
			}
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void excelProcess(HeaderHolderResp header, XSSFSheet sheet, List<Map> traceDatas) {
		try {		
			Set<String> keySet = header.header.keySet();
			int startRow = header.rowCopy.getRowNum();
			CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
			cellCopyPolicy.setCopyCellStyle(true);
			boolean isFirtRow = true;
			String[] headerSplit;
			Object ownerIdObj;
			String ownerId;
			HeaderHolder holder;
			Object objVal;
			
			List<Users> users = userAct.getUserByProductToAssign(req.getProductId()).getUsers();
			List<Map<String, String>> userList;
			Map u;
			
			Date now = Calendar.getInstance().getTime();
			String firstName = "", lastName = "";
			int count = 0;
			
			for (Map val : traceDatas) {
				count++;
				reArrangeMapV3(val, "taskDetail");
				
				if(header.yearType != null && header.yearType.equals("BE")) {								
					objVal = new SimpleDateFormat("dd/MM/yyyy", new Locale("th", "TH")).format(now);
				} else {								
					objVal = new SimpleDateFormat("dd/MM/yyyy", new Locale("en", "US")).format(now);
				}
				val.put(SYS_NOW_DATETIME.getName(), objVal);
				val.put(SYS_COUNT.getName(), count);
				
				ownerIdObj = val.get(SYS_OWNER_ID.getName());
				if(ownerIdObj != null) {
					ownerId = ownerIdObj.toString();
					userList = MappingUtil.matchUserId(users, ownerId);
					if(userList != null && userList.size() > 0) {
						u = (Map)userList.get(0);				
						firstName = "";
						lastName = "";
						
						if(u.get("firstName") != null) {							
							firstName = u.get("firstName").toString();
							val.put(SYS_OWNER_FIRST_NAME.getName(), firstName);
						}
						if(u.get("lastName") != null) {		
							lastName = u.get("lastName").toString();
							val.put(SYS_OWNER_LAST_NAME.getName(), lastName);
						}
						val.put(SYS_OWNER_FULL_NAME.getName(), (StringUtils.trimToEmpty(firstName) + " " + StringUtils.trimToEmpty(lastName)).trim());
					}
				}
				
				reArrangeMap(val, "taskDetailFull");
				Set<String> fields = header.fields.keySet();
				
				for (String field : keySet) {
					if(field.startsWith("link_")) {
						reArrangeMapV2(val, field);						
					}
				}
				
				if(!isFirtRow) {			
					sheet.copyRows(startRow, startRow, ++startRow, cellCopyPolicy);	
					header.rowCopy = sheet.getRow(startRow);
				}
				for (String key : keySet) {
					holder = header.header.get(key);
					
					if(!key.startsWith("link_")) {						
						headerSplit = key.split("\\.");
						if(headerSplit.length > 1) {
							key = headerSplit[1];
						}
					}
					
					if(key.endsWith("_" + holder.index)) {
						key = key.replace("_" + holder.index, "");
					}
					
					if(key.equals("createdDate") || key.equals("createdTime")) {							
						objVal = val.get("createdDateTime");
						if(holder.type != null && holder.type.equals("str")) {
							if(header.yearType != null && header.yearType.equals("BE")) {								
								objVal = new SimpleDateFormat(holder.format, new Locale("th", "TH")).format(objVal);
							} else {								
								objVal = new SimpleDateFormat(holder.format, new Locale("en", "US")).format(objVal);
							}
						}
					} else {
						objVal = val.get(key);							
					}
					
					if(holder.type != null && holder.type.equals("date")) {	
						if(objVal == null) {							
							header.rowCopy.getCell(holder.index).setCellValue("");
						} else {
							if(header.yearType != null && header.yearType.equals("BE")) {								
								objVal = new SimpleDateFormat(holder.format == null ? "dd/MM/yyyy" : holder.format, new Locale("th", "TH")).format(objVal);
							} else {								
								objVal = new SimpleDateFormat(holder.format == null ? "dd/MM/yyyy" : holder.format, new Locale("en", "US")).format(objVal);
							}
							header.rowCopy.getCell(holder.index).setCellValue(objVal.toString());
						}
					} else if(holder.type != null && holder.type.equals("num")) {							
						header.rowCopy.getCell(holder.index).setCellValue(objVal == null ? 0 : Double.valueOf(objVal.toString()));							
					} else {
						header.rowCopy.getCell(holder.index).setCellValue(objVal == null ? null : objVal.toString());							
					}
				}
				isFirtRow = false;
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		XSSFWorkbook workbook = null;
		
		try {
			out = new BufferedOutputStream(os);
			
			if(isFillTemplate) {
				LOG.debug("Fill template values");
				fis = new FileInputStream(new File(filePath));
				
				workbook = new XSSFWorkbook(new FileInputStream(filePath));
				XSSFSheet sheet = workbook.getSheetAt(0);
				List<HeaderHolderResp> headers = getHeader(sheet);
				HeaderHolderResp headerHolderResp = headers.get(0);
				PaymentDetailCriteriaResp paymentResult;
				List<Map> paymentDatas;
				
				LOG.debug("call traceResult");
				List<String> includeFields = new ArrayList<>();
				includeFields.add("html");
				paymentResult = paymentService.find(req, true, includeFields);
				paymentDatas = paymentResult.getPaymentDetails();
								
				if(paymentDatas == null) return;		
				
				excelProcess(headerHolderResp, sheet, paymentDatas);
				
				//--[* Have to placed before write out]
				XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
				
				if(pocModule != null && pocModule.equals(1)) {
					String uuidDateTime = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", Calendar.getInstance());
					String subDir = filePathTemp + "/" + uuidDateTime;
					new File(subDir).mkdirs();
					
					createPdf(paymentDatas, subDir);
					String fileName = uuidDateTime + ".xlsx";
					FileOutputStream fileOut = new FileOutputStream(new File(subDir + "/" + fileName));
					workbook.write(fileOut);
					fileOut.close();
					
					String zipFile = filePathTemp + "/" + uuidDateTime + ".zip";
					ZipUtil.createZip(subDir, zipFile);
					FileUtils.deleteQuietly(new File(subDir));
					
					writeOut(out, zipFile);
					FileUtils.deleteQuietly(new File(zipFile));
				} else {					
					workbook.write(out);
				}
			} else {
				writeOut(out, filePath);
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		} finally {
			try {if(workbook != null) workbook.close();} catch (Exception e2) {}
			try {if(fis != null) fis.close();} catch (Exception e2) {}
			try {if(in != null) in.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
		}	
	}
	
	private void writeOut(OutputStream out, String file) throws Exception {
		FileInputStream in = null;
		
		try {
			in = new FileInputStream(new File(file));
			IOUtils.copy(in,out);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try {if(in != null) in.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
		}
	}
	
	private void createPdf(List<Map> paymentDatas, String dir) throws Exception {
		try {
			LOG.info("Start cratePdf");
			String pdfFile, suffix;
			for (Map payment : paymentDatas) {
				if(payment.get("html") == null) {
					LOG.error(payment.get("ID_CARD") + " html not found");
					continue;
				}
				
				suffix = String.format("%1$tH%1$tM%1$tS%1$tL", Calendar.getInstance());
				pdfFile = dir + "/" + payment.get("ลำดับ").toString() + "_" + payment.get("ID_CARD") + "_" + suffix + ".pdf";
				PdfUtil.html2pdf(wkhtmltopdfPath,  payment.get("html").toString(), pdfFile);
			}
			LOG.info("End cratePdf");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void reArrangeMap(Map val, String key) {
		try {
			Object objVal = val.get(key);
			List<Map> lstMap;
			
			if(objVal != null) {
				lstMap = (List)objVal;
				
				if(lstMap == null || lstMap.size() == 0) return;
				
				val.putAll(lstMap.get(0));
				val.remove(key);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void reArrangeMapV2(Map val, String key) {
		try {
			String[] keys = null;
			if(key.contains(".")) {
				keys = key.split("\\.");
			}
			
			if(keys == null && keys.length < 2) return;
			
			Object objVal = val.get(keys[0]);
			List<Map> lstMap;
			
			if(objVal != null) {
				lstMap = (List)objVal;
				
				if(lstMap == null || lstMap.size() == 0) return;
				
				Map map = lstMap.get(0);
				map.put(keys[0] + "." + keys[1], map.get(keys[1]));
				
				val.putAll(map);
				val.remove(keys[1]);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void reArrangeMapV3(Map val, String key) {
		try {
			Object objVal = val.get(key);
			Map map;
			
			if(objVal != null) {
				map = (Map)objVal;
								
				val.putAll(map);
				val.remove(key);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	class HeaderHolder {
		public String type;
		public String format;
		public String emptySign;
		public int index;
	}
	
	class HeaderHolderResp {
		public Map<String, HeaderHolder> header;
		public BasicDBObject fields;
		public XSSFRow rowCopy;
		public String delimiter;
		public String yearType;
		
		public HeaderHolderResp(Map<String, HeaderHolder> header, BasicDBObject fields, XSSFRow rowCopy, String delimiter, String yearType) {
			this.header = header;
			this.fields = fields;
			this.rowCopy = rowCopy;
			this.delimiter = delimiter;
			this.yearType = yearType;
		}
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isFillTemplate() {
		return isFillTemplate;
	}

	public void setFillTemplate(boolean isFillTemplate) {
		this.isFillTemplate = isFillTemplate;
	}

	public PaymentDetailService getTraceService() {
		return paymentService;
	}

	public void setTraceService(PaymentDetailService traceService) {
		this.paymentService = traceService;
	}

	public PaymentDetailCriteriaReq getTraceReq() {
		return req;
	}

	public void setTraceReq(PaymentDetailCriteriaReq traceReq) {
		this.req = traceReq;
	}

	public UserAction getUserAct() {
		return userAct;
	}

	public void setUserAct(UserAction userAct) {
		this.userAct = userAct;
	}
	
	public String getFilePathTemp() {
		return filePathTemp;
	}

	public void setFilePathTemp(String filePathTemp) {
		this.filePathTemp = filePathTemp;
	}

	public Integer getPocModule() {
		return pocModule;
	}

	public void setPocModule(Integer pocModule) {
		this.pocModule = pocModule;
	}

	public String getWkhtmltopdfPath() {
		return wkhtmltopdfPath;
	}

	public void setWkhtmltopdfPath(String wkhtmltopdfPath) {
		this.wkhtmltopdfPath = wkhtmltopdfPath;
	}

}
