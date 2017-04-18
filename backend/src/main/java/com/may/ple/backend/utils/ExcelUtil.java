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
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC && !HSSFDateUtil.isCellDateFormatted(cell)) {								
					val = NumberToTextConverter.toText(cell.getNumericCellValue());
				} else {								
					val = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)); 
				}
			} else if(dataType.equals("num")) {
				if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					val = cell.getNumericCellValue(); 
				} else {
					val = Double.parseDouble(StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)) .replace(",", ""));
				}
			} else if(dataType.equals("bool")) {
				if(cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {								
					val = cell.getBooleanCellValue();
				} else {
					val = Boolean.parseBoolean(StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell)));
				}
			} else if(dataType.equals("date")) {
				for (YearType yt : yearType) {
					if(!yt.getColumnName().equals(colName)) continue;
					
					if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						if(YearTypeConstant.valueOf(yt.getYearType()) == YearTypeConstant.BE) {
							calendar = Calendar.getInstance();
							calendar.setTime(cell.getDateCellValue());
							calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 543);
							val = calendar.getTime();
						} else {
							val = cell.getDateCellValue();
						}
					} else {
						cellValue = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cell));
						
						if(YearTypeConstant.valueOf(yt.getYearType()) == YearTypeConstant.BE) {
							ddMMYYYYFormat = DateUtil.ddMMYYYYFormat(cellValue, true);										
						} else {										
							ddMMYYYYFormat = DateUtil.ddMMYYYYFormat(cellValue, false);
						}
						val = new SimpleDateFormat("dd/MM/yyyy").parse(ddMMYYYYFormat);
					}
					break;
				}
			}
			
			return val;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}