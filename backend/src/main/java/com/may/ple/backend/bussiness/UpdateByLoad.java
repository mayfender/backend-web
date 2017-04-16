package com.may.ple.backend.bussiness;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_IS_ACTIVE;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.IsActive;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.YearType;
import com.may.ple.backend.utils.ExcelUtil;
import com.may.ple.backend.utils.StringUtil;

public class UpdateByLoad {
	
	public void assign(List<Users> users, Map<String, List<String>> assignVal, MongoTemplate template, String contractNoCol, String taskFileId) {
		Set<String> keySet = assignVal.keySet();
		List<String> contractNos;
		List<String> ownerId;
		Users user = null;
		Criteria criteria;
		
		for (String key : keySet) {
			contractNos = assignVal.get(key);
			user = null;
			
			for (Users u : users) {
				if(key.equals(u.getUsername())) {
					user = u;
					break;
				}
			}
			
			if(user == null) continue;
			
			ownerId = new ArrayList<>();
			ownerId.add(user.getId());
			
			criteria = Criteria.where(contractNoCol).in(contractNos);
			
			if(!StringUtils.isBlank(taskFileId)) {
				criteria.and(SYS_FILE_ID.getName()).is(taskFileId);
			}
			
			template.updateMulti(Query.query(criteria), Update.update(SYS_OWNER_ID.getName(), ownerId), NEW_TASK_DETAIL.getName());
		}
	}
	
	public void update(List<Map<String, Object>> updateVal, MongoTemplate template, String contractNoCol, String taskFileId) {
		Criteria criteria;
		Update update;
		Object contractNo;
		boolean haveChanged;
		
		for (Map<String, Object> val : updateVal) {
			
			contractNo = val.get(contractNoCol);
			
			if(contractNo == null) continue;
			
			criteria = Criteria.where(contractNoCol).is(contractNo.toString());
			
			if(!StringUtils.isBlank(taskFileId)) {
				criteria.and(SYS_FILE_ID.getName()).is(taskFileId);
			}
			
			Set<String> keySet = val.keySet();
			update = new Update();
			haveChanged = false;
			
			for (String key : keySet) {
				if(contractNoCol.equals(key)) continue;
				
				haveChanged = true;
				
				if(SYS_IS_ACTIVE.getName().equals(key)) {
					update.set(key, new IsActive(Boolean.valueOf(val.get(key).toString()), ""));
				} else {
					update.set(key, val.get(key));					
				}
			}
			
			if(!haveChanged) continue;
			
			template.updateFirst(Query.query(criteria), update, NEW_TASK_DETAIL.getName());
		}
	}
	
	public Map<String, List<String>> getBodyAssign(Sheet sheet, Map<String, Integer> headerIndex, String contractNoColKey, String userKey) throws Exception {
		int lastRowNum = sheet.getLastRowNum();
		int rowIndex = 1;
		Row row;
		Cell cellUser, cellContractNo;
		Map<String, List<String>> assignValMap = new HashMap<>();
		String userVal, contractNoVal;
		List<String> contractNoValLst;
		
		while(lastRowNum >= rowIndex) {
			row = sheet.getRow(rowIndex++);
			
			if(row == null) continue;
			
			cellUser = row.getCell(headerIndex.get(userKey.toUpperCase()), MissingCellPolicy.RETURN_BLANK_AS_NULL);
			cellContractNo = row.getCell(headerIndex.get(contractNoColKey.toUpperCase()), MissingCellPolicy.RETURN_BLANK_AS_NULL);
			
			if(cellUser == null || cellContractNo == null) {
				continue;
			}
			
			userVal = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cellUser));
			contractNoVal = StringUtil.removeWhitespace(new DataFormatter(Locale.ENGLISH).formatCellValue(cellContractNo));
			
			if(assignValMap.containsKey(userVal)) {
				contractNoValLst = assignValMap.get(userVal);
				contractNoValLst.add(contractNoVal);
			} else {					
				contractNoValLst = new ArrayList<>();
				contractNoValLst.add(contractNoVal);
				assignValMap.put(userVal, contractNoValLst);
			}
		}
		
		return assignValMap;
	}
	
	public Map<String, Integer> getHeaderAssign(Sheet sheet, String contractNoCol, String user) {
		Map<String, Integer> headerIndex = new LinkedHashMap<>();
		Cell cell;
		int cellIndex = 0;
		int countNull = 0;
		String value;
		Row row = sheet.getRow(0);
		
		while(true) {
			cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);
			if(countNull == 10) break;
			
			if(cell == null) {
				countNull++;
				continue;
			} else {
				countNull = 0;
				value = StringUtil.removeWhitespace(cell.getStringCellValue()).toUpperCase();
				
				if(value.equals(contractNoCol.toUpperCase()) || value.equals(user.toUpperCase())) {
					headerIndex.put(value, cellIndex - 1);					
				}
			}
		}
		
		return headerIndex;
	}
	
	public Map<String, Integer> getHeaderUpdate(Sheet sheet, String userCol) {
		Map<String, Integer> headerIndex = new LinkedHashMap<>();
		Cell cell;
		int cellIndex = 0;
		int countNull = 0;
		String value;
		Row row = sheet.getRow(0);
		
		while(true) {
			cell = row.getCell(cellIndex++, MissingCellPolicy.RETURN_BLANK_AS_NULL);
			if(countNull == 10) break;
			
			if(cell == null) {
				countNull++;
				continue;
			} else {
				countNull = 0;
				value = StringUtil.removeWhitespace(cell.getStringCellValue());
				
				if(value.equalsIgnoreCase(userCol)) continue;
					
				headerIndex.put(value, cellIndex - 1);
			}
		}
		
		return headerIndex;
	}
	
	public List<Map<String, Object>> getBodyUpdate(Sheet sheet, 
												   Map<String, Integer> headerIndex, 
												   List<ColumnFormat> columnFormats, 
												   List<YearType> yearType) throws Exception {
		
		List<Map<String, Object>> datas = new ArrayList<>();
		int lastRowNum = sheet.getLastRowNum();
		Map<String, Object> data;
		int rowIndex = 1;
		Cell cell;
		Row row;
		
		while(lastRowNum >= rowIndex) {
			row = sheet.getRow(rowIndex++);
			
			if(row == null) {
				continue;
			}
			
			data = new LinkedHashMap<>();
			
			for (ColumnFormat colForm : columnFormats) {	
				if(!headerIndex.containsKey(colForm.getColumnName())) continue;
				
				cell = row.getCell(headerIndex.get(colForm.getColumnName()), MissingCellPolicy.RETURN_BLANK_AS_NULL);
				if(cell != null) {
					data.put(colForm.getColumnName(), ExcelUtil.getValue(cell, colForm.getDataType(), yearType, colForm.getColumnName()));
				} else {
					data.put(colForm.getColumnName(), null);
				}
			}
			
			datas.add(data);
		}
		
		return datas;
	}

}
