package com.may.ple.backend.criteria;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_COUNT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_LAST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NOW_DATETIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.may.ple.backend.constant.FileTypeConstant;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.service.TraceWorkService;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.StringUtil;
import com.mongodb.BasicDBObject;

public class TraceResultReportCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(TraceResultReportCriteriaResp.class.getName());
	private String filePath;
	private boolean isFillTemplate;
	private boolean isLastOnly;
	private boolean isNoTrace;
	private TraceWorkService traceService;
	private TraceResultCriteriaReq traceReq;
	private FileTypeConstant fileType;
	private UserAction userAct;
	private Boolean isActiveOnly;

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

	private void traceName(List<Map<String, String>> userOwnerList, Map val, boolean isOwner) {
		if(userOwnerList != null && userOwnerList.size() > 0) {
			Map u = (Map)userOwnerList.get(0);
			String firstName = "", lastName = "";

			if(u.get("firstName") != null) {
				firstName = u.get("firstName").toString();
				val.put(isOwner ? SYS_OWNER_FIRST_NAME.getName() : SYS_CREATED_FIRST_NAME.getName(), firstName);
			}
			if(u.get("lastName") != null) {
				lastName = u.get("lastName").toString();
				val.put(isOwner ? SYS_OWNER_LAST_NAME.getName() : SYS_CREATED_LAST_NAME.getName(), lastName);
			}
			val.put(isOwner ? SYS_OWNER_FULL_NAME.getName() : SYS_CREATED_FULL_NAME.getName(), (StringUtils.trimToEmpty(firstName) + " " + StringUtils.trimToEmpty(lastName)).trim());
		}
	}

	private void excelProcess(HeaderHolderResp header, XSSFSheet sheet, List<Map> traceDatas, boolean isActiveOnly) {
		try {
			Set<String> keySet = header.header.keySet();
			int startRow = header.rowCopy.getRowNum();
			CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
			cellCopyPolicy.setCopyCellStyle(true);
			boolean isFirtRow = true;
			String[] headerSplit;
			List<String> ownerId;
			HeaderHolder holder;
			Object objVal;

			List<Users> users = userAct.getUserByProductToAssign(traceReq.getProductId()).getUsers();
			List<Map<String, String>> userOwnerList, userCreaedList;

			Date now = Calendar.getInstance().getTime();
			int count = 0;

			for (Map val : traceDatas) {
				reArrangeMapV3(val, "taskDetail");
				reArrangeMap(val, "taskDetailFull");

				if(isActiveOnly) {
					if(!val.containsKey("sys_isActive") || !(boolean)((Map)val.get("sys_isActive")).get("status")) {
						continue;
					}
				}

				count++;

				if(header.yearType != null && header.yearType.equals("BE")) {
					objVal = new SimpleDateFormat("dd/MM/yyyy", new Locale("th", "TH")).format(now);
				} else {
					objVal = new SimpleDateFormat("dd/MM/yyyy", new Locale("en", "US")).format(now);
				}
				val.put(SYS_NOW_DATETIME.getName(), objVal);
				val.put(SYS_COUNT.getName(), count);

				ownerId = (List)val.get(SYS_OWNER_ID.getName());
				if(ownerId != null && ownerId.size() > 0) {
					userOwnerList = MappingUtil.matchUserId(users, ownerId.get(0));
					userCreaedList = MappingUtil.matchUserId(users, val.get("createdBy").toString());

					if(userCreaedList == null || userCreaedList.size() == 0) {
						LOG.info("Find others users.");
						Users user = userAct.getUserById(val.get("createdBy").toString()).getUser();
						if(user != null) {
							users.add(user);
							userCreaedList = MappingUtil.matchUserId(users, val.get("createdBy").toString());
						}
					}

					traceName(userOwnerList, val, true);
					traceName(userCreaedList, val, false);
				}

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
						//--type is dateObj
						objVal = val.get("createdDateTime");
						if(holder.type != null) {
							if(holder.type.equals("str")) {
								if(header.yearType != null && header.yearType.equals("BE")) {
									objVal = new SimpleDateFormat(holder.format == null ? "dd/MM/yyyy" : holder.format, new Locale("th", "TH")).format(objVal);
								} else {
									objVal = new SimpleDateFormat(holder.format == null ? "dd/MM/yyyy" : holder.format, new Locale("en", "US")).format(objVal);
								}
							}
						}
					} else {
						objVal = val.get(key);
					}

					if(holder.type != null && holder.type.contains("date")) {
						if(objVal == null) {
							header.rowCopy.getCell(holder.index).setCellValue("");
						} else {
							if(holder.type.equals("date")) {
								if(header.yearType != null && header.yearType.equals("BE")) {
									objVal = new SimpleDateFormat(holder.format == null ? "dd/MM/yyyy" : holder.format, new Locale("th", "TH")).format(objVal);
								} else {
									objVal = new SimpleDateFormat(holder.format == null ? "dd/MM/yyyy" : holder.format, new Locale("en", "US")).format(objVal);
								}
								header.rowCopy.getCell(holder.index).setCellValue(objVal.toString());
							} else {
								// type is dateObj
								header.rowCopy.getCell(holder.index).setCellValue((Date)objVal);
							}
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

	private byte[] txtProcess(HeaderHolderResp header, List<Map> traceDatas) throws Exception {
		try {
			Set<String> keySet = header.header.keySet();
			StringBuilder resultTxt = new StringBuilder();
			List<String> resultLst;
			HeaderHolder holder;
			String[] headerSplit;
			Object objVal;

			for (Map val : traceDatas) {
				reArrangeMap(val, "taskDetail");
				reArrangeMap(val, "link_actionCode");
				reArrangeMap(val, "link_resultCode");
				resultLst = new ArrayList<>();

				if(resultTxt.length() > 0) {
					resultTxt.append("\r\n");
				}

				for (String key : keySet) {
					holder = header.header.get(key);

					headerSplit = key.split("\\.");
					if(headerSplit.length > 1) {
						key = headerSplit[1];
					}

					if(key.equals("createdDate") || key.equals("createdTime")) {
						objVal = val.get("createdDateTime");
						if(header.yearType.equals("BE")) {
							resultLst.add(new SimpleDateFormat(holder.format, new Locale("th", "TH")).format(objVal));
						} else {
							resultLst.add(new SimpleDateFormat(holder.format, Locale.ENGLISH).format(objVal));
						}
					} else {
						if(!val.containsKey(key)) continue;

						objVal = val.get(key);

						if(objVal instanceof Date) {
							if(header.yearType.equals("BE")) {
								resultLst.add(new SimpleDateFormat(holder.format, new Locale("th", "TH")).format(objVal));
							} else {
								resultLst.add(new SimpleDateFormat(holder.format, Locale.ENGLISH).format(objVal));
							}
						} else if(objVal instanceof Number) {
							resultLst.add(String.format("%" + (holder.format == null ? ",.2" : holder.format) + "f", objVal));
						} else {
							if(objVal == null) {
								objVal = holder.emptySign;
							} else {
								if(objVal instanceof String) {
									objVal = StringUtils.defaultIfBlank(String.valueOf(objVal), holder.emptySign);
								}
							}
							resultLst.add(objVal.toString());
						}
					}
				}

				resultTxt.append(StringUtils.join(resultLst, header.delimiter));
			}

			byte[] data = String.valueOf(resultTxt).getBytes();

			return data;
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
				workbook = new XSSFWorkbook(fis);
				fis.close();

				XSSFSheet sheet = workbook.getSheetAt(0);
				List<HeaderHolderResp> headers = getHeader(sheet);
				TraceResultCriteriaResp traceResult;
				HeaderHolderResp headerHolderResp;
				List<Map> traceDatas = new ArrayList<>();

				if(fileType == FileTypeConstant.TXT) {
					headerHolderResp = headers.get(1);
					traceResult = traceService.traceResult(traceReq, headerHolderResp.fields, true);
					traceDatas = traceResult.getTraceDatas();

					if(traceDatas == null) return;

					if(isLastOnly) {
						LOG.info("Get only last");
						traceDatas = getLastTrace(traceDatas);
					}

					byte[] data = txtProcess(headerHolderResp, traceDatas);
					in = new ByteArrayInputStream(data);
					int bytes;

					while ((bytes = in.read()) != -1) {
						out.write(bytes);
					}
				} else {
					headerHolderResp = headers.get(0);

					traceReq.setItemsPerPage(1000);
					int currentPage = 1;

					LOG.info("Start get traceResult");
					while(true) {
						traceReq.setCurrentPage(currentPage++);
						traceResult = traceService.traceResult(traceReq, headerHolderResp.fields, true);
						traceDatas.addAll(traceResult.getTraceDatas());

						if(traceReq.getItemsPerPage() > traceResult.getTraceDatas().size()) {
							break;
						}
					}
					LOG.info("End get traceResult");

					if(isNoTrace) {
						if(traceDatas != null) {
							LOG.info("Get only last");
							traceDatas = getLastTrace(traceDatas);
						}

						LOG.debug("call noTraceResult");
						traceDatas = traceService.noTraceResult(traceDatas, headerHolderResp.fields, traceReq.getProductId());
					} else {
						if(traceDatas == null) return;

						if(isLastOnly) {
							LOG.info("Get only last");
							traceDatas = getLastTrace(traceDatas);
						}
					}

					LOG.info("Call traceResult");
					excelProcess(headerHolderResp, sheet, traceDatas, isActiveOnly);

					//--[* Have to placed before write out]
					XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);

					workbook.write(out);
				}
			} else {
				LOG.debug("Get byte");
				java.nio.file.Path path = Paths.get(filePath);
				byte[] data = Files.readAllBytes(path);
				in = new ByteArrayInputStream(data);
				int bytes;

				while ((bytes = in.read()) != -1) {
					out.write(bytes);
				}
			}

			LOG.info("End");
		} catch (Exception e) {
			LOG.error(e.toString(), e);
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
			Map map;

			if(objVal != null) {
				lstMap = (List)objVal;

				if(lstMap == null || lstMap.size() == 0) return;

				map = lstMap.get(0);
				map.remove("_id");
				val.putAll(map);
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

				if(map == null) return;

				map.remove("_id");
				val.putAll(map);
				val.remove(key);
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	private List<Map> getLastTrace(List<Map> traceDatas) {
		try {
			Date createdDateTime, createdDateTimeDummy;
			String contractNo, contractNoDummy;

			List<Map> traceDatasLastOnly = new ArrayList<>();
			boolean isFoundInLast;

			for (Map outerMap : traceDatas) {
				createdDateTime = (Date)outerMap.get("createdDateTime");
				contractNo = outerMap.get("contractNo").toString();
				isFoundInLast = false;

				for (Map innerMap : traceDatasLastOnly) {
					contractNoDummy = innerMap.get("contractNo").toString();

					if(contractNo.equals(contractNoDummy)) {
						createdDateTimeDummy = (Date)innerMap.get("createdDateTime");
						isFoundInLast = true;
						if(createdDateTime.after(createdDateTimeDummy)) {
							innerMap.clear();
							innerMap.putAll(outerMap);
							break;
						}
					}
				}

				if(!isFoundInLast) {
					traceDatasLastOnly.add(outerMap);
				}
			}

			return traceDatasLastOnly;
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

	public void setFileType(FileTypeConstant fileType) {
		this.fileType = fileType;
	}

	public FileTypeConstant getFileType() {
		return fileType;
	}

	public void setLastOnly(boolean isLastOnly) {
		this.isLastOnly = isLastOnly;
	}

	public boolean isNoTrace() {
		return isNoTrace;
	}

	public void setNoTrace(boolean isNoTrace) {
		this.isNoTrace = isNoTrace;
	}

	public UserAction getUserAct() {
		return userAct;
	}

	public void setUserAct(UserAction userAct) {
		this.userAct = userAct;
	}

	public void setIsActiveOnly(Boolean isActiveOnly) {
		this.isActiveOnly = isActiveOnly;
	}

}
