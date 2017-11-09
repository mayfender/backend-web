package com.may.ple.backend.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.may.ple.backend.entity.ColumnFormat;

public class MergeColumnUtil {
	private Map<String, List<ColumnFormat>> sameColumnAlias = new HashMap<>();
	
	public boolean groupCol(ColumnFormat colForm) {
		String columnDummyAlias = colForm.getColumnNameAlias();
		String dataType = colForm.getDataType();
		List<ColumnFormat> columLst;
		boolean isSameGroup = false;
		
		if(!StringUtils.isBlank(columnDummyAlias) && dataType.equals("str")) {
			columLst = sameColumnAlias.get(columnDummyAlias);
			
			if(columLst == null) {
				columLst = new ArrayList<>();
				columLst.add(colForm);
				sameColumnAlias.put(columnDummyAlias, columLst);
			} else {
				isSameGroup = true;
				columLst.add(colForm);
				sameColumnAlias.put(columnDummyAlias, columLst);											
			}
		}
		return isSameGroup;
	}
	
	public void matchVal(Map val) {
		List<ColumnFormat> value;
		String result, result2;
		Object obj;
		String objStr;
		
		for(Entry<String, List<ColumnFormat>> entry : sameColumnAlias.entrySet()) {
			value = entry.getValue();
			
			if(value.size() < 2) continue;
			
			result = "";
			result2 = "";
			
			for (ColumnFormat col : value) {
				obj = val.get(col.getColumnName());
				if(obj == null) obj = "";
				if(!(obj instanceof String)) break;
				
				objStr = (String)obj;
				
				if(!StringUtils.isBlank(objStr)) {
					result += " " + objStr;
//					result2 += "\n" + objStr;
					result2 += " " + objStr;
				}
				val.remove(col.getColumnName());				
			}
			val.put(value.get(0).getColumnName(), result.trim());
			val.put(value.get(0).getColumnName() + "_hide", result2.trim());
		}
	}

}
