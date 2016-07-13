package com.may.ple.backend.service;

import static com.may.ple.backend.constant.SysFieldConstant.SYS_FILE_ID;
import static com.may.ple.backend.constant.SysFieldConstant.SYS_OLD_ORDER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ImportOthersFindDetailCriteriaReq;
import com.may.ple.backend.criteria.ImportOthersFindDetailCriteriaResp;
import com.may.ple.backend.entity.ColumnFormat;
import com.may.ple.backend.entity.ImportMenu;
import com.may.ple.backend.model.DbFactory;

@Service
public class ImportOthersDetailService {
	private static final Logger LOG = Logger.getLogger(ImportOthersDetailService.class.getName());
	private DbFactory dbFactory;
	
	@Autowired
	public ImportOthersDetailService(DbFactory dbFactory) {
		this.dbFactory = dbFactory;
	}
	
	public ImportOthersFindDetailCriteriaResp find(ImportOthersFindDetailCriteriaReq req) throws Exception {
		try {
			ImportOthersFindDetailCriteriaResp resp = new ImportOthersFindDetailCriteriaResp();
			MongoTemplate template = dbFactory.getTemplates().get(req.getProductId());
			
			ImportMenu importMenu = template.findOne(Query.query(Criteria.where("id").is(req.getMenuId())), ImportMenu.class);
			List<ColumnFormat> columnFormats = importMenu.getColumnFormats();
			
			if(columnFormats == null) return resp;
			LOG.debug("Before size: " + columnFormats.size());
			columnFormats = getColumnFormatsActive(columnFormats);
			LOG.debug("After size: " + columnFormats.size());
			
			Criteria criteria = Criteria.where(SYS_FILE_ID.getName()).is(req.getFileId());
			Query query = Query.query(criteria);
			Field fields = query.fields();
			List<Criteria> multiOr = new ArrayList<>();
			Map<String, List<ColumnFormat>> sameColumnAlias = new HashMap<>();
			List<ColumnFormat> columRemovable = new ArrayList<>();
			String columnDummyAlias = "";
			List<ColumnFormat> columLst;
			
			for (ColumnFormat columnFormat : columnFormats) {
				//--: Concat fields
				columnDummyAlias = columnFormat.getColumnNameAlias();
				
				if(!StringUtils.isBlank(columnDummyAlias) && columnFormat.getDataType().equals("str")) {
					columLst = sameColumnAlias.get(columnDummyAlias);
					
					if(columLst == null) {
						columLst = new ArrayList<>();
						columLst.add(columnFormat);
						sameColumnAlias.put(columnFormat.getColumnNameAlias(), columLst);
					} else {
						columRemovable.add(columnFormat);
						columLst.add(columnFormat);
						sameColumnAlias.put(columnFormat.getColumnNameAlias(), columLst);											
					}
				}
				//--: End Concat fields
				
				fields.include(columnFormat.getColumnName());
				
				if(columnFormat.getDataType() != null) {
					if(columnFormat.getDataType().equals("str")) {
						if(!StringUtils.isBlank(req.getKeyword())) {								
							multiOr.add(Criteria.where(columnFormat.getColumnName()).regex(Pattern.compile(req.getKeyword(), Pattern.CASE_INSENSITIVE)));							
						}
					} else if(columnFormat.getDataType().equals("num")) {
						//--: Ignore right now.
					}
				} else {
					LOG.debug(columnFormat.getColumnName() + "' dataType is null");
				}
			}
			
			//--: Remove Column Header
			if(columRemovable.size() > 0) {
				LOG.debug("Remove Column Header");
				columnFormats.removeAll(columRemovable);
			}
			
			Criteria[] multiOrArr = multiOr.toArray(new Criteria[multiOr.size()]);
			if(multiOrArr.length > 0) {
				criteria.orOperator(multiOrArr);				
			}
			
			LOG.debug("Start Count newTaskDetail record");
			long totalItems = template.count(query, req.getMenuId());
			LOG.debug("End Count newTaskDetail record");
			
			query = query.with(new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage()));
			
			if(req.getColumnName() == null) {
				query.with(new Sort(SYS_OLD_ORDER.getName()));
			} else {				
				query.with(new Sort(Direction.fromString(req.getOrder()), req.getColumnName()));
			}
			
			LOG.debug("Start find newTaskDetail");
			List<Map> dataLst = template.find(query, Map.class, req.getMenuId());			
			LOG.debug("End find newTaskDetail");
			
			LOG.debug("Change id from ObjectId to normal ID");
			Object obj;
			String result = "";
			
			for (Map map : dataLst) {
				//--: Concat fields
				for(Entry<String, List<ColumnFormat>> entry : sameColumnAlias.entrySet()) {
					List<ColumnFormat> value = entry.getValue();
					if(value.size() < 2) continue;
					
					result = "";
					for (ColumnFormat col : value) {
						obj = map.get(col.getColumnName());
						if(!(obj instanceof String)) break;
						result += obj;
						map.remove(col.getColumnName());
					}
					map.put(value.get(0).getColumnName(), result);
				}
				//--: End Concat fields
				
				map.put("id", map.get("_id").toString()); 
				map.remove("_id");
			}
			
			resp.setHeaders(columnFormats);
			resp.setTotalItems(totalItems);
			resp.setDataLst(dataLst);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private List<ColumnFormat> getColumnFormatsActive(List<ColumnFormat> columnFormats) {
		List<ColumnFormat> result = new ArrayList<>();
		
		for (ColumnFormat colFormat : columnFormats) {
			if(colFormat.getIsActive()) {
				result.add(colFormat);
			}
		}
		
		return result;
	}
	
}
