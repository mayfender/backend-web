package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.service.DymListService;
import com.may.ple.backend.service.TaskDetailService;
import com.may.ple.backend.utils.GetAccountListHeaderUtil;

public class NewTaskDownloadCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(NewTaskDownloadCriteriaResp.class.getName());
	private String filePath;
	private Boolean isCheckData;
	private Boolean isByCriteria = false;
	private TaskDetailService service;
	private TaskDetailCriteriaReq req;
	private DymListService dymService;
	
	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		XSSFWorkbook workbook = null;
		Date maxDate = new Date(Long.MAX_VALUE);
		
		try {
			byte[] data;
			out = new BufferedOutputStream(os);
			
			if(isCheckData) {
				if(filePath.endsWith(".xlsx")) {
					workbook = new XSSFWorkbook(new FileInputStream(filePath));
				} else {
					throw new CustomerException(5000, "Filetype not match");
				}
				
				if(!isByCriteria) workbook.setSheetName(0, workbook.getSheetName(0) + "_Validation");
				
				XSSFSheet sheet = workbook.getSheetAt(0);				
				List<ColumnFormat> columnFormats = new ArrayList<>();
				Map<String, Integer> headerIndex = GetAccountListHeaderUtil.getFileHeader(sheet, columnFormats);
				Set<String> keySet = headerIndex.keySet();

				List<String> fields = new ArrayList<>(keySet);
				List<String> codeFields = new ArrayList<>();
				Map<String, List<Map<String, String>>> dynResult = new HashMap<>();
				List<Map<String, String>> result;
				
				if(fields != null) {					
					for (int i = 0; i < fields.size(); i++) {
						if(fields.get(i).endsWith("_sys")) {
							codeFields.add(fields.get(i));
							fields.set(i, fields.get(i).replace("_sys", ""));
						}
					}
					if(codeFields.size() > 0) {
						DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
						List<Integer> statuses = new ArrayList<>();
						statuses.add(0);
						statuses.add(1);
						reqDym.setStatuses(statuses);
						reqDym.setProductId(req.getProductId());
						List<Map> dynListFull = dymService.findFullList(reqDym);
						for (String field : fields) {
							for (Map parent : dynListFull) {
								if(parent.get("fieldName").equals(field)) {
									result = (List<Map<String, String>>)parent.get("dymListDet");
									dynResult.put(field + "_sys", result);
									break;
								}
							}
						}
					}
				}
				
				List<Map> taskDetails = service.find(req, fields).getTaskDetails();
				
				CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
				cellCopyPolicy.setCopyCellStyle(true);
				Map<Integer, CellStyle> cellStyleMap = new HashMap<>();
				int rowIndex = 1;
				Row row;
				Cell cell;
				Object val;
				
				for (Map task : taskDetails) {			
					
					row = sheet.getRow(rowIndex++);
					
					if(rowIndex != 1) {
						sheet.copyRows(1, 1, rowIndex, cellCopyPolicy);	
					}
					
					if(row == null) {
						row = sheet.createRow(rowIndex - 1);
					}
					
					for (String key : keySet) {
						if(dynResult.containsKey(key)) {
							val = task.get(key.replace("_sys", ""));
							if(val == null) {
								continue;
							}
							
							result = dynResult.get(key);
							for (Map<String, String> map : result) {
								if(val.equals(map.get("_id"))) {
									if(map.containsKey("meaning")) {
										val = StringUtils.stripToEmpty(map.get("meaning"));
									}
									if(map.containsKey("code")) {
										val += "[" +StringUtils.stripToEmpty(map.get("code")) + "]";
									}
									break;
								}
							}
						} else {
							val = task.get(key);
						}
						
						cell = row.getCell(headerIndex.get(key));
						
						if(val == null) {
							/*if(cell != null) {
								row.removeCell(cell);								
							}*/
							continue;
						}
						
						/*if(cell == null) {
							cell = row.createCell(headerIndex.get(key));
							cell.setCellStyle(cellStyleMap.get(headerIndex.get(key)));
						} else {
							if(cellStyleMap.get(headerIndex.get(key)) == null) {
								cellStyleMap.put(headerIndex.get(key), cell.getCellStyle());								
							}
						}*/
						
						if(val instanceof Date) {
							if(((Date) val).compareTo(maxDate) != 0) {
								cell.setCellValue((Date)val);									
							}
						} else if(val instanceof Number) {
							cell.setCellValue((Double)val);
						} else if(val instanceof Boolean){
							cell.setCellValue((Boolean)val);
						} else {
							cell.setCellValue(StringUtils.defaultString(String.valueOf(val), ""));
						}
					}
				}
				
				if(isByCriteria) {
					int countNull = 0;
					
					while(true) {
						if(countNull == 10) break;
						
						row  = sheet.getRow(rowIndex++);
						
						if(row != null) {
							sheet.removeRow(row);
						} else {
							countNull++;
						}
					}
				}
				
				workbook.write(out);
			} else {
				LOG.debug("Get byte");
				java.nio.file.Path path = Paths.get(filePath);
				data = Files.readAllBytes(path);					
			
				in = new ByteArrayInputStream(data);
				int bytes;
				
				while ((bytes = in.read()) != -1) {
					out.write(bytes);
				}
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try { if(workbook != null) workbook.close(); } catch (Exception e2) {}
			try { if(fis != null) fis.close(); } catch (Exception e2) {}
			try { if(in != null) in.close(); } catch (Exception e2) {}			
			try { if(out != null) out.close(); } catch (Exception e2) {}			
		}	
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setIsCheckData(Boolean isCheckData) {
		this.isCheckData = isCheckData;
	}

	public Boolean getIsByCriteria() {
		return isByCriteria;
	}

	public void setIsByCriteria(Boolean isByCriteria) {
		this.isByCriteria = isByCriteria;
	}

	public TaskDetailService getService() {
		return service;
	}

	public void setService(TaskDetailService service) {
		this.service = service;
	}

	public TaskDetailCriteriaReq getReq() {
		return req;
	}

	public void setReq(TaskDetailCriteriaReq req) {
		this.req = req;
	}

	public void setDymService(DymListService dymService) {
		this.dymService = dymService;
	}

}
