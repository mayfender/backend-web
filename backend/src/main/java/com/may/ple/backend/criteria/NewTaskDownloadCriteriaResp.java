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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.bussiness.ExcelReport;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.model.HeaderHolderResp;
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
	private ExcelReport excelUtil;
	private String contractNoColumnName;

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

				if(FilenameUtils.getBaseName(filePath).contains("_newversion")) {
					LOG.info("Use new template");
					//--[.]
					HeaderHolderResp holderResp = excelUtil.getHeader(sheet).get(0);
					Set<String> keySet = holderResp.fields.keySet();
					List<String> fields = new ArrayList<>(keySet);
					List<String> codeFields = new ArrayList<>();

					if(!fields.contains("user")) {
						fields.add("user");
					}

					String[] keys = null;
					for (int i = 0; i < fields.size(); i++) {
						if(fields.get(i).startsWith("link_")) {
							codeFields.add(fields.get(i));
							keys = fields.get(i).replace("link_", "").split("\\.");
							fields.set(i, keys[0]);
						}
					}

					List<Map> taskDetails = service.find(req, fields).getTaskDetails();
					extraData(taskDetails, keySet, fields, codeFields);

					excelUtil.fillBody(
							holderResp,
							sheet,
							taskDetails,
							req.getProductId(),
							false,
							req.getContractNoList(),
							contractNoColumnName
							);
				} else {
					LOG.info("Use old template");
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
							List<Map> dynListFull = dymService.findFullList(reqDym, true);
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
					int rowIndex = 1;
					Row row;
					Cell cell;
					Object val;

					for (Map task : taskDetails) {

						row = sheet.getRow(rowIndex++);

						if(rowIndex != 1) {
							sheet.copyRows(1, 1, rowIndex, cellCopyPolicy);
						}

						/*if(row == null) {
						row = sheet.createRow(rowIndex - 1);
					}*/

						for (String key : keySet) {
							if(dynResult.containsKey(key)) {
								val = task.get(key.replace("_sys", ""));

								if(val != null) {
									result = dynResult.get(key);

									for (Map<String, String> map : result) {
										if(val.equals(map.get("_id"))) {
											if(map.containsKey("meaning") && !StringUtils.isBlank(StringUtils.stripToEmpty(map.get("meaning")))) {
												val = StringUtils.stripToEmpty(map.get("meaning"));
											} else {
												val = "";
											}
											if(map.containsKey("code") && !StringUtils.isBlank(StringUtils.stripToEmpty(map.get("code")))) {
												val += "[" + StringUtils.stripToEmpty(map.get("code")) + "]";
											}
											break;
										}
									}
								}
							} else {
								val = task.get(key);
							}

							cell = row.getCell(headerIndex.get(key));

							if(val == null) {
								if(cell != null) {
									cell.setCellType(Cell.CELL_TYPE_BLANK);
								}
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
								} else {
									cell.setCellValue("");
								}
							} else if(val instanceof Number) {
								cell.setCellValue(Double.parseDouble(String.valueOf(val)));
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
				}

				//--[* Have to placed before write out]
				LOG.info("evaluateAllFormulaCells");
				XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

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

	private void extraData(List<Map> taskDetails, Set<String> keySet, List<String> fields, List<String> codeFields) throws Exception {
		try {
			Map<String, List<Map<String, String>>> dynResult = new HashMap<>();
			List<Map<String, String>> result;
			Object val;

			if(codeFields.size() > 0) {
				DymListFindCriteriaReq reqDym = new DymListFindCriteriaReq();
				List<Integer> statuses = new ArrayList<>();
				statuses.add(0);
				statuses.add(1);
				reqDym.setStatuses(statuses);
				reqDym.setProductId(req.getProductId());
				List<Map> dynListFull = dymService.findFullList(reqDym, true);
				for (String field : fields) {
					for (Map parent : dynListFull) {
						if(parent.get("fieldName").equals(field)) {
							result = (List<Map<String, String>>)parent.get("dymListDet");
							dynResult.put("link_" + field, result);
							break;
						}
					}
				}
			}

			String keys[] = null;
			for (Map task : taskDetails) {
				for (String key : keySet) {
					if(!key.startsWith("link_")) continue;

					keys = key.split("\\.");

					if(!dynResult.containsKey(keys[0])) continue;

					val = task.get(keys[0].replace("link_", ""));

					if(val == null) continue;

					result = dynResult.get(keys[0]);

					for (Map<String, String> map : result) {
						if(val.equals(map.get("_id"))) {
							if(keys[1].equals("code") && !StringUtils.isBlank(StringUtils.stripToEmpty(map.get("code")))) {
								val = StringUtils.stripToEmpty(map.get("code"));
							} else if(keys[1].equals("meaning") && !StringUtils.isBlank(StringUtils.stripToEmpty(map.get("meaning")))) {
								val = StringUtils.stripToEmpty(map.get("meaning"));
							} else {
								val = "";
							}
							break;
						}
					}
					task.put(key, val);
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
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

	public ExcelReport getExcelUtil() {
		return excelUtil;
	}

	public void setExcelUtil(ExcelReport excelUtil) {
		this.excelUtil = excelUtil;
	}

	public void setContractNoColumnName(String contractNoColumnName) {
		this.contractNoColumnName = contractNoColumnName;
	}

}
