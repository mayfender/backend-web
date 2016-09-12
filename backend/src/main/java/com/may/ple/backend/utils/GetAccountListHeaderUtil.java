package com.may.ple.backend.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;

import com.may.ple.backend.entity.ColumnFormat;

public class GetAccountListHeaderUtil {
	private static final Logger LOG = Logger.getLogger(GetAccountListHeaderUtil.class.getName());
	private static final int INIT_GROUP_ID = 1;
	
	public static Map<String, Integer> getFileHeader(Sheet sheet, List<ColumnFormat> columnFormats) {
		try {
			Map<String, Integer> headerIndex = new LinkedHashMap<>();
			int maxOrder = columnFormats.size();
			Row row = sheet.getRow(0);
			ColumnFormat colForm;
			int cellIndex = 0;
			int countNull = 0;
			boolean isContain;
			String value, valueDummy;
			Cell cell;
			int countSameColName;
			
			while(true) {
				cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);				
				
				if(countNull == 10) break;
			
				if(cell == null) {
					countNull++;
					continue;
				} else {
					countNull = 0;
					value = cell.getStringCellValue().trim().replaceAll("\\.", "");
					countSameColName = 2;
					valueDummy = value;
					
					while(headerIndex.containsKey(valueDummy)) {
						valueDummy = value + "_" + countSameColName++;
					}
					
					headerIndex.put(valueDummy, cellIndex - 1);
				}
				
				isContain = false;
				
				for (ColumnFormat c : columnFormats) {
					if(valueDummy.equals(c.getColumnName())) {						
						isContain = true;
						break;
					}
				}
				
				if(!isContain) {
					colForm = new ColumnFormat(valueDummy, false);
					colForm.setDetGroupId(INIT_GROUP_ID);
					colForm.setDetIsActive(true);
					colForm.setIsNotice(false);
					colForm.setDetOrder(++maxOrder);
					columnFormats.add(colForm);
				}
			}
			
			return headerIndex;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
