package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.service.TraceWorkService;
import com.mongodb.BasicDBObject;

public class TraceResultReportCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(TraceResultReportCriteriaResp.class.getName());
	private String filePath;
	private boolean isFillTemplate;
	private TraceWorkService traceService;
	private TraceResultCriteriaReq traceReq;
	
	private HeaderHolderResp getHeader(XSSFSheet sheet) {
		int startRow = 1;
		int cellIndex = 0;
		int countNull = 0;
		XSSFRow row = null;
		XSSFRow rowCopy = null;
		XSSFCell cell = null;
		BasicDBObject fields = new BasicDBObject();
		Map<String, HeaderHolder> header = new HashMap<>();		
		String colName;
		String[] headers;
		HeaderHolder headerHolder;
		
		while((row = sheet.getRow(startRow++)) != null) {
			while(true) {
				cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);				
				
				if(countNull == 10) break;
			
				if(cell == null) {
					countNull++;
					continue;
				} else {
					countNull = 0;
				}
				
				if(cell.getStringCellValue().startsWith("${")) {
					if(rowCopy == null) rowCopy = row;
					
					colName = cell.getStringCellValue().replace("${", "").replace("}", "");
					headers = colName.split("&");
					
					headerHolder = new HeaderHolder();
					colName = headers[0];
					
					if(headers.length > 1) {
						headerHolder.type = headers[1];
						
						if(headers.length > 2) {
							headerHolder.format = headers[2];
						}
					}
					
					fields.append(colName.equals("createdDate") || colName.equals("createdTime") ? "createdDateTime" : colName, 1);
					headerHolder.index = cellIndex - 1;
					header.put(colName, headerHolder);
				}							
			}
			
			countNull = 0;
			cellIndex = 0;
		}
		
		return new HeaderHolderResp(header, fields, rowCopy);
	}

	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		XSSFWorkbook workbook = null;
		
		try {
			if(isFillTemplate) {
				LOG.debug("Fill template values");
				out = new BufferedOutputStream(os);
				fis = new FileInputStream(new File(filePath));
				
				workbook = new XSSFWorkbook(new FileInputStream(filePath));
				XSSFSheet sheet = workbook.getSheetAt(0);
				
				HeaderHolderResp header = getHeader(sheet);
				
				TraceResultCriteriaResp traceResult = traceService.traceResult(traceReq, header.fields, false);
				List<Map> traceDatas = traceResult.getTraceDatas();
				
				if(traceDatas == null) return;
				
				Set<String> keySet = header.header.keySet();
				int startRow = header.rowCopy.getRowNum();
				CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
				cellCopyPolicy.setCopyCellStyle(true);
				boolean isFirtRow = true;
				String[] headerSplit;
				HeaderHolder holder;
				Object objVal;
				
				for (Map val : traceDatas) {
					reArrangeMap(val, "taskDetail");
					reArrangeMap(val, "link_actionCode");
					reArrangeMap(val, "link_resultCode");
					
					if(!isFirtRow) {			
						sheet.copyRows(startRow, startRow, ++startRow, cellCopyPolicy);	
						header.rowCopy = sheet.getRow(startRow);
					}
					for (String key : keySet) {
						holder = header.header.get(key);
						
						headerSplit = key.split("\\.");
						if(headerSplit.length > 1) {
							key = headerSplit[1];
						}
						
						if(key.equals("createdDate") || key.equals("createdTime")) {							
							objVal = val.get("createdDateTime");
							if(holder.type != null && holder.type.equals("str")) {
								objVal = new SimpleDateFormat(holder.format).format(objVal);
							}
						} else {
							objVal = val.get(key);							
						}
						
						if(holder.type != null && holder.type.equals("date")) {	
							header.rowCopy.getCell(holder.index).setCellValue(objVal == null ? null : (Date)objVal);
						} else if(holder.type != null && holder.type.equals("num")) {							
							header.rowCopy.getCell(holder.index).setCellValue(objVal == null ? 0 : Double.valueOf(objVal.toString()));							
						} else {
							header.rowCopy.getCell(holder.index).setCellValue(objVal == null ? null : objVal.toString());							
						}
					}
					isFirtRow = false;
				}
				
				workbook.write(out);
			} else {
				LOG.debug("Get byte");
				java.nio.file.Path path = Paths.get(filePath);
				byte[] data = Files.readAllBytes(path);					
				
				in = new ByteArrayInputStream(data);
				out = new BufferedOutputStream(os);
				int bytes;
				
				while ((bytes = in.read()) != -1) {
					out.write(bytes);
				}
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try {if(workbook != null) workbook.close();} catch (Exception e2) {}
			try {if(fis != null) fis.close();} catch (Exception e2) {}
			try {if(in != null) in.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
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
	
	class HeaderHolder {
		public String type;
		public String format;
		public int index;
	}
	
	class HeaderHolderResp {
		public Map<String, HeaderHolder> header;
		public BasicDBObject fields;
		public XSSFRow rowCopy;
		
		public HeaderHolderResp(Map<String, HeaderHolder> header, BasicDBObject fields, XSSFRow rowCopy) {
			this.header = header;
			this.fields = fields;
			this.rowCopy = rowCopy;
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

	public TraceWorkService getTraceService() {
		return traceService;
	}

	public void setTraceService(TraceWorkService traceService) {
		this.traceService = traceService;
	}

	public TraceResultCriteriaReq getTraceReq() {
		return traceReq;
	}

	public void setTraceReq(TraceResultCriteriaReq traceReq) {
		this.traceReq = traceReq;
	}

}
