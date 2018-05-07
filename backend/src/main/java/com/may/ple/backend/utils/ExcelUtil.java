package com.may.ple.backend.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;

import com.may.ple.backend.constant.YearTypeConstant;
import com.may.ple.backend.model.YearType;

public class ExcelUtil {
	private static final Logger LOG = Logger.getLogger(ExcelUtil.class.getName());
	
	public static Object getValue(Cell cell, String dataType, List<YearType> yearType, String colName) throws Exception {
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
			
			LOG.debug("Val: " + val);
			
			return val;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}