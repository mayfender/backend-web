package com.may.ple.backend.bussiness;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_COUNT;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_CREATED_LAST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_NOW_DATETIME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FIRST_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_FULL_NAME;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_LAST_NAME;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.constant.YearTypeConstant;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.HeaderHolder;
import com.may.ple.backend.model.HeaderHolderResp;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.utils.DateUtil;
import com.may.ple.backend.utils.MappingUtil;
import com.may.ple.backend.utils.NameUtil;
import com.may.ple.backend.utils.StringUtil;
import com.may.ple.backend.utils.YearUtil;
import com.mongodb.BasicDBObject;

@Service
public class ExcelReport {
	private static final Logger LOG = Logger.getLogger(ExcelReport.class.getName());
	private UserAction userAct;

	@Autowired
	public ExcelReport(UserAction userAct) {
		this.userAct = userAct;
	}

	public List<HeaderHolderResp> getHeader(XSSFSheet sheet) {
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

	public void fillBody(HeaderHolderResp header, XSSFSheet sheet, List<Map> datas,
			String prodId, boolean isActiveOnly, List<String> contractNoList, String contractNoColumnName) {
		try {
			Set<String> keySet = header.header.keySet();
			int startRow = header.rowCopy.getRowNum();
			CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
			Date maxDate = new Date(Long.MAX_VALUE);
			cellCopyPolicy.setCopyCellStyle(true);
			boolean isFirtRow = true;
			String[] headerSplit;
			List<String> ownerId;
			HeaderHolder holder;
			Object objVal;

			List<Users> users = userAct.getUserByProductToAssign(prodId).getUsers();
			List<Map<String, String>> userOwnerList, userCreaedList;

			Date now = Calendar.getInstance().getTime();
			int count = 0;

			if(contractNoList == null) {
				contractNoList = new ArrayList<>();
				contractNoList.add("none");
			}

			boolean chkExit;
			for (String contractNo : contractNoList) {
				chkExit = false;

				for (Map val : datas) {
					if(!contractNo.equals("none") && !contractNo.equals(val.get(contractNoColumnName))) {
						continue;
					}
					chkExit = true;

					reArrangeMapV3(val, "taskDetail", "taskDetailFull");

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

						if(val.get("createdBy") != null) {
							userCreaedList = MappingUtil.matchUserId(users, val.get("createdBy").toString());

							if(userCreaedList == null || userCreaedList.size() == 0) {
								LOG.info("Find others users.");
								Users user = userAct.getUserById(val.get("createdBy").toString()).getUser();
								if(user != null) {
									users.add(user);
									userCreaedList = MappingUtil.matchUserId(users, val.get("createdBy").toString());
								}
							}
							NameUtil.traceName(userCreaedList, val, false);
						}
						NameUtil.traceName(userOwnerList, val, true);
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
									if(((Date) objVal).compareTo(maxDate) != 0) {
										header.rowCopy.getCell(holder.index).setCellValue((Date)objVal);
									} else {
										header.rowCopy.getCell(holder.index).setCellValue("");
									}
								}
							}
						} else if(holder.type != null && holder.type.equals("num")) {
							header.rowCopy.getCell(holder.index).setCellValue(objVal == null ? 0 : Double.valueOf(objVal.toString()));
						} else {
							if(objVal != null) {
								if(objVal instanceof Date) {
									if(((Date) objVal).compareTo(maxDate) != 0) {
										header.rowCopy.getCell(holder.index).setCellValue((Date)objVal);
									} else {
										header.rowCopy.getCell(holder.index).setCellValue("");
									}
								} else {
									header.rowCopy.getCell(holder.index).setCellValue(objVal.toString());
								}
							} else {
								header.rowCopy.getCell(holder.index).setCellValue("");
							}
						}
					}
					isFirtRow = false;

					if(!contractNo.equals("none")) {
						break;
					}
				}
				if(!contractNo.equals("none")) {
					if(!chkExit) {
						sheet.copyRows(startRow, startRow, ++startRow, cellCopyPolicy);
						header.rowCopy = sheet.getRow(startRow);

						for (String key : keySet) {
							holder = header.header.get(key);

							if(key.equals(contractNoColumnName)) {
								header.rowCopy.getCell(holder.index).setCellValue(contractNo);
							} else {
								header.rowCopy.getCell(holder.index).setCellValue("-");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

	public Object getValue(Cell cell, String dataType, List<YearType> yearType, String colName) throws Exception {
		try {
			FormulaEvaluator evaluator;
			String ddMMYYYYFormat;
			Calendar calendar;
			String cellValue;
			Object val = null;

			if(cell == null) return null;

			if(dataType == null || dataType.equals("str")) {
				LOG.debug("Type str");
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					if(HSSFDateUtil.isCellDateFormatted(cell)) {
						LOG.debug("Cell type is date");

						if(cell.getCellStyle().getDataFormat() == 14) {
							if(cell.getDateCellValue() == null) return null;

							val = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(cell.getDateCellValue());
						} else {
							val = new DataFormatter(Locale.ENGLISH).formatCellValue(cell);
						}
					} else {
						LOG.debug("Cell type is number");
//						val = NumberToTextConverter.toText(cell.getNumericCellValue());
						val = new DataFormatter(Locale.ENGLISH).formatCellValue(cell);
					}
				} else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					LOG.debug("Formula To text");
					evaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
					val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, evaluator));
				} else {
					LOG.debug("To text");
					val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
				}
			} else if(dataType.equals("num")) {
				LOG.debug("Type num");
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					LOG.debug("Cell type is number");
					val = cell.getNumericCellValue();
				} else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					LOG.debug("Formular to num");
					evaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
					val = Double.parseDouble(StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, evaluator)) .replace(",", "").replace("-", ""));
				} else {
					LOG.debug("Cell type is string");

					String strVal = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)) .replace(",", "").replace("-", "");
					if(StringUtils.isBlank(strVal)) return null;

					val = Double.parseDouble(strVal);
				}
			} else if(dataType.equals("bool")) {
				LOG.debug("Type bool");
				if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
					LOG.debug("Cell type is boolean");
					val = cell.getBooleanCellValue();
				} else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					evaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
					val = Boolean.parseBoolean(StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, evaluator)));
				} else {
					LOG.debug("To text");
					val = Boolean.parseBoolean(StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)));
				}
			} else if(dataType.equals("date")) {
				LOG.debug("Type date");

				if(yearType == null) {
					if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
						cellValue = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
						val = YearUtil.buddToGre(new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(cellValue));
					} else {
						val = YearUtil.buddToGre(cell.getDateCellValue());
					}
				} else {
					for (YearType yt : yearType) {
						if(!yt.getColumnName().equals(colName)) continue;

						if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							LOG.debug("Cell type number");
							if(YearTypeConstant.valueOf(yt.getYearType()) == YearTypeConstant.BE) {
								LOG.debug("Year type BE");
								calendar = Calendar.getInstance();
								calendar.setTime(cell.getDateCellValue());
								calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 543);
								val = calendar.getTime();
							} else {
								LOG.debug("Year type AD");
								val = cell.getDateCellValue();
							}
						} else {
							if(cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
								LOG.debug("Formula to date");
								evaluator = cell.getRow().getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
								cellValue = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell, evaluator));
							} else {
								cellValue = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
							}

							LOG.info("cellValue: " + cellValue);
							if(StringUtils.isBlank(cellValue)) return null;

							if(YearTypeConstant.valueOf(yt.getYearType()) == YearTypeConstant.BE) {
								LOG.debug("Year type BE");
								ddMMYYYYFormat = DateUtil.ddMMYYYYFormat(cellValue, true);
							} else {
								LOG.debug("Year type AD");
								ddMMYYYYFormat = DateUtil.ddMMYYYYFormat(cellValue, false);
							}
							val = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(ddMMYYYYFormat);
						}
						break;
					}
				}
			}

			LOG.debug("Val: " + val);

			return val;
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

	private void reArrangeMapV3(Map val, String... key) {
		try {
			for (int i = 0; i < key.length; i++) {
				Object objVal = val.get(key[i]);
				Map map;

				if(objVal != null) {
					if(objVal instanceof List) {
						reArrangeMap(val, key[i]);
						return;
					}

					map = (Map)objVal;

					if(map == null) return;

					val.putAll(map);
					val.remove(key[i]);
				}
			}
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

}