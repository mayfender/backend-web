/*package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.TableLandPersistCriteriaReq;
import com.may.ple.backend.criteria.TableLandSearchCriteriaReq;
import com.may.ple.backend.entity.TableLand;
import com.may.ple.backend.repository.TableLandRepository;

@Service
public class TableLandService {
	private static final Logger LOG = Logger.getLogger(TableLandService.class.getName());
	private TableLandRepository tableRepository;
	private DataSource dataSource;
	
	@Autowired
	public TableLandService(TableLandRepository tableRepository, DataSource dataSource) {
		this.tableRepository = tableRepository;
		this.dataSource = dataSource;
	}
	
	public List<TableLand> searchTableLand(TableLandSearchCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		List<TableLand> tables = new ArrayList<TableLand>();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, name, status");
			sql.append(" from table_land where 1=1 ");
			
			if(req != null) {
				if(!StringUtils.isBlank(req.getName())) {
					sql.append(" and name like '%" + req.getName() + "%' ");
				}
				if(req.getStatus() != null) {
					sql.append(" and status = " + req.getStatus());
				}
			}
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			tables = new ArrayList<TableLand>();
			TableLand tableLand;
			
			while(rst.next()) {
				tableLand = new TableLand(rst.getString("name"), rst.getInt("status"));
				tableLand.setId(rst.getLong("id"));
				
				tables.add(tableLand);
			}
			
			return tables;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public Long persistTable(TableLandPersistCriteriaReq req) {
		try {
			TableLand tableLand;
			
			if(req.getId() != null) {
				tableLand = tableRepository.findOne(req.getId());
				tableLand.setName(req.getName());
			} else {
				tableLand = new TableLand(req.getName(), 0);
			}
			
			tableRepository.save(tableLand);
			return tableLand.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteMenuType(Long id) throws Exception {
		try {
			tableRepository.delete(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
*/