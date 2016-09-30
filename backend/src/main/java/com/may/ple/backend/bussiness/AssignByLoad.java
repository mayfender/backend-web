package com.may.ple.backend.bussiness;

import static com.may.ple.backend.constant.CollectNameConstant.NEW_TASK_DETAIL;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OWNER_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.may.ple.backend.entity.Users;

public class AssignByLoad {
	
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
			
			userVal = cellUser.getStringCellValue().trim();
			
			if(cellContractNo.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				contractNoVal = String.format("%.0f", cellContractNo.getNumericCellValue());
			} else {
				contractNoVal = cellContractNo.getStringCellValue().trim();									
			}
			
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
				value = cell.getStringCellValue().trim().toUpperCase();
				
				if(value.equals(contractNoCol.toUpperCase()) || value.equals(user.toUpperCase())) {
					headerIndex.put(value, cellIndex - 1);					
				}
			}
		}
		
		return headerIndex;
	}

}
