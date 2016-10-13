package com.may.ple.backend.utils;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

public class POIExcelUtil {
	private static final Logger LOG = Logger.getLogger(POIExcelUtil.class.getName());
	
	public static void removeSheetExcept0(Workbook workbook) {
		int sheetIndex = 1;
		
		try {
			while(workbook.getSheetAt(sheetIndex) != null) {
				workbook.removeSheetAt(sheetIndex);
			}
		} catch (Exception e) {
			LOG.info(e.toString());
		}
	}

}
