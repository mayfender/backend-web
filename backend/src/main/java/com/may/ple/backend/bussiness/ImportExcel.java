package com.may.ple.backend.bussiness;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.may.ple.backend.entity.ColumnFormat;

public class ImportExcel {
	private static final Logger LOG = Logger.getLogger(ImportExcel.class.getName());
	
	public static List<ColumnFormat> getColDateType(Map<String, Integer> headerIndex, List<ColumnFormat> columnFormats) {
		try {
			Set<String> keySet = headerIndex.keySet();
			List<ColumnFormat> colDateType = new ArrayList<>();
					
			for (ColumnFormat columnFormat : columnFormats) {
				if(columnFormat.getDataType() == null || !columnFormat.getDataType().equals("date")) continue;
				
				for (String key : keySet) {
					if(!key.equals(columnFormat.getColumnName())) continue;
					
					colDateType.add(columnFormat);						
					break;
				}
			}
			
			return colDateType;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static List<String> getColNotFound(Map<String, Integer> headerIndex, List<ColumnFormat> columnFormats) {
		try {
			Set<String> keySet = headerIndex.keySet();
			List<String> colNotFounds = new ArrayList<>();
			boolean isFound;
					
			for (String key : keySet) {
				isFound = false;
				
				for (ColumnFormat columnFormat : columnFormats) {
					if(key.equals(columnFormat.getColumnName())) {	
						isFound = true;
						break;
					}
				}
				if(!isFound) {
					colNotFounds.add(key);					
				}
			}
			
			return colNotFounds;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
