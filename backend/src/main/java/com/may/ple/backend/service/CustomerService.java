package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.CustomerSearchCriteriaReq;
import com.may.ple.backend.criteria.OpenCashDrawerCriteriaReq;
import com.may.ple.backend.entity.Customer;
import com.may.ple.backend.repository.CustomerRepository;

@Service
public class CustomerService {
	private static final Logger LOG = Logger.getLogger(CustomerService.class.getName());
	private DataSource dataSource;
	private CustomerRepository customerRepository;
	
	@Autowired
	public CustomerService(DataSource dataSource, CustomerRepository customerRepository) {
		this.dataSource = dataSource;
		this.customerRepository = customerRepository;
	}

	public List<Customer> searchCus(CustomerSearchCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		List<Customer> tables = new ArrayList<Customer>();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, ref, table_detail, status, cash_receive_amount, change_cash, total_price ");
//			sql.append(" from customer where 1=1 and created_date_time >= DATE_SUB(NOW(), INTERVAL 15 HOUR) ");
			sql.append(" from customer where 1=1 and DATE(created_date_time) = CURDATE() ");
			
			if(req != null) {
				if(!StringUtils.isBlank(req.getRef())) {
					sql.append(" and (ref like '%" + req.getRef() + "%' or table_detail like '%" + req.getRef() + "%' ) ");
				}
				if(req.getStatus() != null) {
					sql.append(" and status = " + req.getStatus());					
				}
			}
			sql.append(" order by created_date_time ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			tables = new ArrayList<Customer>();
			Customer customer;
			
			while(rst.next()) {
				customer = new Customer(rst.getString("ref"), rst.getString("table_detail"), 
										rst.getInt("status"), null, null, 
										rst.getDouble("cash_receive_amount"),
										rst.getDouble("change_cash"),
										rst.getDouble("total_price"));
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
	
	public boolean findCusActive(String tableName, String ref) throws Exception {
		boolean result = false;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id ");
			sql.append(" from customer ");
			sql.append(" where status = 1 and table_detail = ? and ref = ? ");
//			sql.append(" and created_date_time >= DATE_SUB(NOW(), INTERVAL 15 HOUR) ");
			sql.append(" and DATE(created_date_time) = CURDATE() ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, tableName);
			pstmt.setString(2, ref);
			rst = pstmt.executeQuery();
			
			if(rst.next()) result = true;
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public void checkBill(OpenCashDrawerCriteriaReq req) {
		try {
			Customer customer = customerRepository.findOne(req.getId());
			customer.setStatus(0);
			customer.setCashReceiveAmount(req.getReceiveAmount());
			customer.setTotalPrice(req.getTotalPrice());
			customer.setChangeCash(req.getChange());
			customer.setUpdatedDateTime(new Date());
			
			customerRepository.save(customer);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
