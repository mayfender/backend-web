package com.may.ple.backend.utils;

import java.text.MessageFormat;
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
		String objStr, space;
		int msgIndex, provinceIndex;
		List<String> msgVal;
		String[] msgArr;
		
		for(Entry<String, List<ColumnFormat>> entry : sameColumnAlias.entrySet()) {
			value = entry.getValue();
			
			if(value.size() < 2) continue;
			
			result = "";
			result2 = "";
			msgIndex = 0;
			msgVal = new ArrayList<>();
			provinceIndex = 0;
			
			for (ColumnFormat col : value) {
				obj = val.get(col.getColumnName());
				if(obj == null) obj = "";
				if(!(obj instanceof String)) break;
				
				objStr = (String)obj;
				space = " ";
				
				if(!StringUtils.isBlank(objStr)) {
					if(StringUtils.isNoneBlank(col.getPrefix())) {
						space += "{" + (msgIndex++) + "}";
						msgVal.add(col.getPrefix());
					}
					if(col.getIsProvince() != null && col.getIsProvince()) {
						if(objStr.contains("กรุงเทพ")) {
							provinceIndex = msgVal.size() - 1;
						}
					}
					
					result += space + objStr;
//					result2 += "\n" + objStr;
					result2 += space + objStr;
				}
				val.remove(col.getColumnName());				
			}
			if(msgVal.size() > 0) {
				msgArr = msgVal.toArray(new String[0]);
				if(provinceIndex != 0) {
					msgArr[provinceIndex] = "";
					msgArr[provinceIndex - 1] = "เขต";
					msgArr[provinceIndex - 2] = "แขวง";
				}
				
				result = MessageFormat.format(result, msgArr);
				result2 = MessageFormat.format(result2, msgArr);
			}
			val.put(value.get(0).getColumnName(), result.trim());
			val.put(value.get(0).getColumnName() + "_hide", result2.trim());
		}
	}

}
