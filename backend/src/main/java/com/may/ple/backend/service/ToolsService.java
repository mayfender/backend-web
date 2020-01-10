package com.may.ple.backend.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.olap4j.impl.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.fasterxml.uuid.Generators;
import com.ibm.icu.text.SimpleDateFormat;
import com.may.ple.backend.bussiness.ExcelReport;
import com.may.ple.backend.bussiness.WebExtractData;
import com.may.ple.backend.bussiness.WebReport1Impl;
import com.may.ple.backend.bussiness.WebReport2Impl;
import com.may.ple.backend.bussiness.WebReport3Impl;
import com.may.ple.backend.constant.ConvertTypeConstant;
import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.constant.SplitterConstant;
import com.may.ple.backend.criteria.Img2TxtCriteriaReq;
import com.may.ple.backend.criteria.ManageDataCriteriaReq;
import com.may.ple.backend.entity.ApplicationSetting;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.TraceWork;
import com.may.ple.backend.entity.TraceWorkOld;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.CaptchaUtil;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;
import com.may.ple.backend.utils.JodConverterUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class ToolsService {
	private static final Logger LOG = Logger.getLogger(ToolsService.class.getName());
	private enum IMG_TO_TXT {ANTI_CAPTCHA, LOCAL};
	private static IMG_TO_TXT CONVERT_SERVICE = IMG_TO_TXT.LOCAL;
	private SettingService settingServ;
	private ExcelReport excelUtil;
	@Value("${file.path.temp}")
	private String filePathTemp;
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	
	@Autowired
	public ToolsService(SettingService settingServ, ExcelReport excelUtil, MongoTemplate templateCore, DbFactory dbFactory) {
		this.settingServ = settingServ;
		this.excelUtil = excelUtil;
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
	}
	
	public void excel2txt(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, ConvertTypeConstant type, String encoding, SplitterConstant splitterConst) throws Exception {
		ByteArrayOutputStream outputArray = null;
		OutputStreamWriter writer = null;
		Workbook workbook = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);						
			
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
			List<ColumnFormat> columnFormats = new ArrayList<>();
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
			if(headerIndex.size() == 0) {		
				LOG.error("Not found Headers");
				return;
			}
			
			String splitter = "|";
			if(splitterConst == SplitterConstant.NONE) {
				splitter = "";
			} else if(splitterConst == SplitterConstant.PIPE) {
				splitter = "|";
			} else if(splitterConst == SplitterConstant.SPACE) {
				splitter = " ";
			}
			
			outputArray = new ByteArrayOutputStream();
			writer = new OutputStreamWriter(outputArray, encoding);
			StringBuilder txtRaw = new StringBuilder();
			Set<String> keySet = headerIndex.keySet();
			Cell cell;
			int r = 1;
			Row row;
			
			while(true) {
				if(r > 1) {
					if(txtRaw.length() != 0) {
						txtRaw.deleteCharAt(txtRaw.length() - 1);
						txtRaw.append(System.getProperty("line.separator"));
					}
				}
				
				row = sheet.getRow(r++);
				if(row == null) {
					r--;
					break;
				}
				
				for (String key : keySet) {
					cell = row.getCell(headerIndex.get(key), MissingCellPolicy.RETURN_BLANK_AS_NULL);
					
					if(cell == null) {
						txtRaw.append(splitter);
					} else {						
						if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {	
							if(HSSFDateUtil.isCellDateFormatted(cell)) {
								LOG.debug("Date format: " + cell.getCellStyle().getDataFormatString() + ", " + cell.getCellStyle().getDataFormat());
								
								if(cell.getCellStyle().getDataFormat() == 14) {
									txtRaw.append(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cell.getDateCellValue()) + splitter);																
								} else {									
									txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell) + splitter);							
								}
							} else {
								txtRaw.append(NumberToTextConverter.toText(cell.getNumericCellValue()) + splitter);							
							}
						} else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
							txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, formulaEvaluator) + splitter);	
						} else {
							txtRaw.append(new DataFormatter(Locale.ENGLISH).formatCellValue(cell) + splitter);							
						}
					}
				}			
			}
			
			writer.write(txtRaw.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(writer != null) writer.close();
		}
		
		LOG.debug("Call saveToFile");
		saveToFile(filePathTemp, fd.fileName, type.getExt(), outputArray);
	}
	
	public void toImg(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, ConvertTypeConstant type) throws Exception {
		ByteArrayOutputStream outputArray = null;
		OutputStreamWriter writer = null;
		PDDocument document = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);
			
			if(!fd.fileExt.equals(".pdf")) {
				byte[] convert = JodConverterUtil.convert(uploadedInputStream, fd.fileExt.replace(".", ""), FileTypeConstant.PDF.getName(), null, null);
				document = PDDocument.load(convert);
			} else {
				document = PDDocument.load(uploadedInputStream);
			}
			
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			BufferedImage bim;
			int widthImg = 0;
			int heightImg = 0;
			float dpi = 128;
			
			for (int page = 0; page < document.getNumberOfPages(); ++page) { 				
			    bim = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
			    
			    widthImg = Math.max(widthImg, bim.getWidth());
			    heightImg += bim.getHeight();			    
			}
			
			BufferedImage combined = new BufferedImage(
					widthImg, // Final image will have width and height as
					heightImg, // addition of widths and heights of the images we already have
					BufferedImage.TYPE_INT_RGB);
			
			Graphics2D gbi = combined.createGraphics();
			heightImg = 0;
			
			for (int page = 0; page < document.getNumberOfPages(); ++page) { 				
			    bim = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);
			    gbi.drawImage(bim, 0, heightImg, null);
			    heightImg += bim.getHeight();
			}
			
			outputArray = new ByteArrayOutputStream();
			ImageIO.write(combined, "jpg", outputArray);			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(document != null) document.close();
			if(writer != null) writer.close();
		}
		
		LOG.debug("Call saveToFile");
		saveToFile(filePathTemp, fd.fileName, type.getExt(), outputArray);
	}
	
	public void web2report(InputStream uploadedInputStream, FormDataContentDisposition fileDetail, FileDetail fd, ConvertTypeConstant type, Integer site) throws Exception {
		Workbook workbook = null;
		FileOutputStream fileOut = null;
		
		try {
			LOG.debug("File ext: " + fd.fileExt);
			if(fd.fileExt.equals(".xlsx")) {
				workbook = new XSSFWorkbook(uploadedInputStream);
			} else if(fd.fileExt.equals(".xls")) {
				workbook = new HSSFWorkbook(uploadedInputStream);
			} else {
				throw new CustomerException(5000, "Filetype not match");
			}
			
			Sheet sheet = workbook.getSheetAt(0);
			Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet);
			List<Map<String, String>> requestData = getRequestData(sheet, headerIndex, site);
			
			WebExtractData extractData = new WebExtractData();
			List<Map<String, String>> result = null;
			fileOut = new FileOutputStream(filePathTemp + "/" + fd.fileName + "." + type.getExt());		    
			ApplicationSetting setting = settingServ.getData();
			
			if(site == 1) {
				result = extractData.getWebData1(setting.getSiteSpshUsername(), setting.getSiteSpshPassword(), requestData);
				new WebReport1Impl("ข้อมูล สปสช.").createReport(result, fileOut);
			} else if(site == 2) {
				result = extractData.getWebData2(setting.getSiteComptrollerUsername(), setting.getSiteComptrollerPassword(), requestData);
				new WebReport2Impl("ข้อมูล กรมบัญชีกลาง.").createReport(result, fileOut);
			} else if(site == 3) {
				result = extractData.getWebData3(setting.getSiteTrueTVUsername(), setting.getSiteTrueTVPassword(), requestData);
				new WebReport3Impl("ข้อมูล Truevision").createReport(result, fileOut);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(fileOut != null) fileOut.close();
			if(workbook != null) workbook.close();
		}
	}
	
	public byte[] getFile(String fileName, boolean isDelete) throws Exception {
		try {
			String filePath = filePathTemp + "/" + fileName;
			Path path = Paths.get(filePath);
			byte[] data = Files.readAllBytes(path);
			
			if(isDelete && !new File(filePath).delete()) {
				LOG.warn("Cann't delete file " + filePath);
			}
			
			return data;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String img2txt(Img2TxtCriteriaReq req) throws Exception {
		try {
			//--: Write input img to file in temp[filePathTemp] folder
			String txt = "";
			UUID uuid = Generators.timeBasedGenerator().generate();
			String imgPath = filePathTemp + "/" + uuid + ".jpg";
			FileUtils.writeByteArrayToFile(new File(imgPath), Base64.decode(req.getImgBase64()));
			
			if(CONVERT_SERVICE == IMG_TO_TXT.LOCAL) {
				String webappsPath = System.getProperty( "catalina.base" ) + File.separator + "webapps";
				ApplicationSetting setting = settingServ.getData();
				txt = CaptchaUtil.tesseract(imgPath, webappsPath, setting.getTesseractPath(), setting.getPythonPath());
			} else if(CONVERT_SERVICE == IMG_TO_TXT.ANTI_CAPTCHA) {
				txt = CaptchaUtil.antiCaptcha(imgPath);				
			}
			return txt;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Map getAllTrace(ManageDataCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			Map<String, Long> result = new HashMap<>();
			Long count;
			
			LOG.info("Start inTraceWorkAll");
			count = template.count(Query.query(new Criteria()), "traceWork");
			result.put("inTraceWorkAll", count);
			LOG.info("End inTraceWorkAll");
			
			LOG.info("Start inTraceWorkSystem");
			count = template.count(Query.query(Criteria.where("isOldTrace").is(null)), "traceWork");
			result.put("inTraceWorkSystem", count);
			LOG.info("End inTraceWorkSystem");
			
			LOG.info("Start inTraceWorkOld");
			count = template.count(Query.query(Criteria.where("isOldTrace").is(true)), "traceWork");
			result.put("inTraceWorkOld", count);
			LOG.info("End inTraceWorkOld");
			
			LOG.info("Start inTraceWorkOldAll");
			count = template.count(Query.query(Criteria.where("isOldTrace").is(true)), "traceWorkOld");
			result.put("inTraceWorkOldAll", count);
			LOG.info("End inTraceWorkOldAll");
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void moveTraceData(ManageDataCriteriaReq req) throws Exception {
		try {
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			List<Map> traceWorkOlds;
			int itemPerPage = 2500;
			Query query;
			
			while(true) {				
				query = Query.query(Criteria.where("isOldTrace").is(true));
				query.limit(itemPerPage);
				
				LOG.info("Start get traceWork");
				traceWorkOlds = template.find(query, Map.class, "traceWork");
				LOG.info("End get traceWork : " + traceWorkOlds.size());
				
				if(traceWorkOlds.size() == 0) break;
				
				LOG.info("Start Insert to new table TraceWorkOld");
				template.insert(traceWorkOlds, TraceWorkOld.class);
				LOG.info("End Insert to new table TraceWorkOld");
				
				LOG.info("Start remove");
				template.findAllAndRemove(query, TraceWork.class, "traceWork");				
				LOG.info("End remove");
				
				LOG.info("Start create index");
				boolean isExis = template.collectionExists(TraceWorkOld.class);
				if(isExis) {
					DBCollection collection = template.getCollection("traceWorkOld");
					collection.createIndex(new BasicDBObject("createdDateTime", 1));
					collection.createIndex(new BasicDBObject("contractNo", 1));
				}
				LOG.info("End create index");
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	
	private void saveToFile(String path, String fileNameFull, String ext, ByteArrayOutputStream outputArray) throws Exception {
		FileOutputStream fileOut = null;
		
		try {
			LOG.debug("Start save file");
			
			File file = new File(path);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			fileOut = new FileOutputStream(path + "/" + fileNameFull + "." + ext);
			fileOut.write(outputArray.toByteArray());
		
			LOG.debug("Finished save file");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(fileOut != null) fileOut.close();
		}
	}
	
	private List<Map<String, String>> getRequestData(Sheet sheet, Map<String, Integer> headerIndex, Integer site) throws Exception {
		try {
			int r = 1;
			Row row;
			Cell cell;
			Object value;
			List<Map<String, String>> results = new ArrayList<>(); 
			Map<String, String> mapResult;
			
			while(true) {
				row = sheet.getRow(r++);
				if(row == null) {
					break;
				}
				
				cell = row.getCell(headerIndex.get("ID Number"), MissingCellPolicy.RETURN_BLANK_AS_NULL);
				if(cell == null) continue;
				
				value = excelUtil.getValue(cell, "str", null, null);
				if(value == null) continue;
				
				mapResult = new HashMap<>();
				mapResult.put("ID Number", value.toString());
				results.add(mapResult);
			}
			
			return results;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
