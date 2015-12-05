package com.may.ple.backend.service;

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

import com.may.ple.backend.criteria.CustomerSearchCriteriaReq;
import com.may.ple.backend.entity.Customer;

@Service
public class CustomerService {
	private static final Logger LOG = Logger.getLogger(CustomerService.class.getName());
	private DataSource dataSource;
	
	@Autowired
	public CustomerService(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<Customer> searchCus(CustomerSearchCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		List<Customer> tables = new ArrayList<Customer>();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, ref, table_detail");
			sql.append(" from customer where 1=1 and status = 0 ");
			
			if(req != null) {
				if(!StringUtils.isBlank(req.getRef())) {
					sql.append(" and ref like '%" + req.getRef() + "%' or table_detail like '%" + req.getRef() + "%' ");
				}
			}
			sql.append(" order by created_date_time ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			tables = new ArrayList<Customer>();
			Customer customer;
			
			while(rst.next()) {
				customer = new Customer(rst.getString("ref"), rst.getString("table_detail"), null, null, null);
				customer.setId(rst.getLong("id"));
				
				tables.add(customer);
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
	
}
