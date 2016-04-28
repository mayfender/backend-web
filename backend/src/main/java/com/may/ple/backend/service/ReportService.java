package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ReportMenuCriteriaResp;
import com.may.ple.backend.criteria.ReportMoneyCriteriaReq;
import com.may.ple.backend.criteria.ReportMoneyCriteriaResp;
import com.may.ple.backend.dto.ReportMenuDto;
import com.may.ple.backend.dto.ReportMoneyDto;

@Service
public class ReportService {
	private static final Logger LOG = Logger.getLogger(ReportService.class.getName());
	private DataSource dataSource;
	
	@Autowired
	public ReportService(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public ReportMoneyCriteriaResp money(ReportMoneyCriteriaReq req) throws Exception {
		ReportMoneyCriteriaResp resp = new ReportMoneyCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {		
			StringBuilder sql = new StringBuilder();
			sql.append(" select DATE(created_date_time) as reportDate, sum(total_price) as totalPrice ");
			sql.append(" from customer ");
			sql.append(" where DATE_FORMAT(created_date_time, '%m-%Y') = ? ");
			sql.append(" group by DATE(created_date_time) ");
			sql.append(" order by created_date_time desc ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, String.format("%1$tm-%1$tY", req.getReportDate()));
			rst = pstmt.executeQuery();
			List<ReportMoneyDto> moneys = new ArrayList<ReportMoneyDto>();
			ReportMoneyDto dto;
			
			while(rst.next()) {
				dto = new ReportMoneyDto();
				dto.setReportDate(rst.getDate("reportDate"));
				dto.setValue(rst.getDouble("totalPrice"));
				moneys.add(dto);
			}
			
			resp.setMoneys(moneys);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public ReportMenuCriteriaResp menu(ReportMoneyCriteriaReq req) throws Exception {
		ReportMenuCriteriaResp resp = new ReportMenuCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {		
			StringBuilder sql = new StringBuilder();
			
			sql.append(" select m.name, sum(o.amount) as amount ");
			sql.append(" from order_menu o join menu m on o.menu_id = m.id ");
			sql.append(" where is_calcel = 0 and DATE(o.created_date_time) = ? ");
			sql.append(" group by o.menu_id ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, String.format("%1$tY-%1$tm-%1$td", req.getReportDate()));
			rst = pstmt.executeQuery();
			List<ReportMenuDto> menus = new ArrayList<ReportMenuDto>();
			ReportMenuDto dto;
			
			while(rst.next()) {
				dto = new ReportMenuDto();
				dto.setName(rst.getString("name"));
				dto.setAmount(rst.getInt("amount"));
				menus.add(dto);
			}
			
			resp.setMenus(menus);
			
			return resp;
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
