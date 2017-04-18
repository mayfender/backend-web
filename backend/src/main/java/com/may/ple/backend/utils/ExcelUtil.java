package com.may.ple.backend.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.NumberToTextConverter;

import com.may.ple.backend.constant.YearTypeConstant;
import com.may.ple.backend.model.YearType;

public class ExcelUtil {
	private static final Logger LOG = Logger.getLogger(ExcelUtil.class.getName());
	
	public static Object getValue(Cell cell, String dataType, List<YearType> yearType, String colName) throws Exception {
		try {
			String ddMMYYYYFormat;
			Calendar calendar;
			String cellValue;
			Object val = null;
			
			if(dataType == null || dataType.equals("str")) {
				LOG.debug("Type str");
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {	
					if(HSSFDateUtil.isCellDateFormatted(cell)) {
						LOG.debug("Cell type is date");
						val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
					} else {						
						LOG.debug("Cell type is number");
						val = NumberToTextConverter.toText(cell.getNumericCellValue());
					}
				} else {						
					LOG.debug("To text");
					val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)); 
				}
			} else if(dataType.equals("num")) {
				LOG.debug("Type num");
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					LOG.debug("Cell type is number");
					val = cell.getNumericCellValue(); 
				} else {
					LOG.debug("Cell type is string");
					val = Double.parseDouble(StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)) .replace(",", ""));
				}
			} else if(dataType.equals("bool")) {
				LOG.debug("Type bool");
				if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {		
					LOG.debug("Cell type is boolean");
					val = cell.getBooleanCellValue();
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
						cellValue = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
						
						if(YearTypeConstant.valueOf(yt.getYearType()) == YearTypeConstant.BE) {
							LOG.debug("Year type BE");
							ddMMYYYYFormat = DateUtil.ddMMYYYYFormat(cellValue, true);										
						} else {								
							LOG.debug("Year type AD");
							ddMMYYYYFormat = DateUtil.ddMMYYYYFormat(cellValue, false);
						}
						val = new SimpleDateFormat("dd/MM/yyyy").parse(ddMMYYYYFormat);
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