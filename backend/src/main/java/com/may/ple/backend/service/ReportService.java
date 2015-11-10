package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.util.DatabaseUtil;

@Service
public class ReportService {
	private static final Logger LOG = Logger.getLogger(ReportService.class.getName());
	private DataSource dataSource;
	
	@Autowired
	public ReportService(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Map<String, List<String>> reportVehicleYear() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				result.put("years", null);
				result.put("values", null);
				return result;
			}
			
			int size = tables.size();
			StringBuilder sqlSub = new StringBuilder();
			
			for (int i = 0; i < size; i++) {	
				sqlSub.append(" select id, in_date_time ");
				sqlSub.append(" from " + tables.get(i) + " ");
				
				if(i < (size - 1)) {
					sqlSub.append(" union all ");
				}
			}
			StringBuilder sql = new StringBuilder();
			sql.append(" select count(id) as num, year(in_date_time) as year ");
			sql.append(" from ( " + sqlSub + " ) sub ");
			sql.append(" group by year(in_date_time) ");
			sql.append(" order by in_date_time asc ");
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<String> years = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			
			while(rst.next()) {
				years.add(rst.getString("year"));
				values.add(rst.getString("num"));
			}
			result.put("years", years);
			result.put("values", values);
			
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
	
	
	public Map<String, List<String>> reportMoneyYear() throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				result.put("years", null);
				result.put("values", null);
				return result;
			}
			
			int size = tables.size();
			StringBuilder sqlSub = new StringBuilder();
			
			for (int i = 0; i < size; i++) {					
				sqlSub.append(" select price, in_date_time ");
				sqlSub.append(" from " + tables.get(i) + " ");
				
				if(i < (size - 1)) {
					sqlSub.append(" union all ");
				}
			}
			StringBuilder sql = new StringBuilder();
			sql.append(" select sum(price) as price, year(in_date_time) as year ");
			sql.append(" from ( " + sqlSub + " ) sub ");
			sql.append(" group by year(in_date_time) ");
			sql.append(" order by in_date_time asc ");
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<String> years = new ArrayList<String>();
			List<String> values = new ArrayList<String>();
			
			while(rst.next()) {
				years.add(rst.getString("year"));
				values.add(rst.getString("price"));
			}
			result.put("years", years);
			result.put("values", values);
			
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
	
	
	public Map<String, List<String>> reportVehicleMonth(String year) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				result.put("values", null);
				return result;
			}
			
			int size = tables.size();
			StringBuilder sqlSub = new StringBuilder();
			
			for (int i = 0; i < size; i++) {					
				sqlSub.append(" select in_date_time ");
				sqlSub.append(" from " + tables.get(i) + " ");
				
				if(i < (size - 1)) {
					sqlSub.append(" union all ");
				}
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append(" select sum(month(in_date_time) = 01) as jan, ");
			sql.append(" sum(month(in_date_time) = 02) as feb, ");
			sql.append(" sum(month(in_date_time) = 03) as mar, ");
			sql.append(" sum(month(in_date_time) = 04) as apr, ");
			sql.append(" sum(month(in_date_time) = 05) as may, ");
			sql.append(" sum(month(in_date_time) = 06) as june, ");
			sql.append(" sum(month(in_date_time) = 07) as july, ");
			sql.append(" sum(month(in_date_time) = 08) as aug, ");
			sql.append(" sum(month(in_date_time) = 09) as sept, ");
			sql.append(" sum(month(in_date_time) = 10) as oct, ");
			sql.append(" sum(month(in_date_time) = 11) as nov, ");
			sql.append(" sum(month(in_date_time) = 12) as decem ");
			sql.append(" from ( " + sqlSub + " ) sub");
			sql.append(" where year(in_date_time) = ? ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, year);
			rst = pstmt.executeQuery();
			List<String> values = new ArrayList<String>();
			
			if(rst.next()) {
				values.add(rst.getString(1));
				values.add(rst.getString(2));
				values.add(rst.getString(3));
				values.add(rst.getString(4));
				values.add(rst.getString(5));
				values.add(rst.getString(6));
				values.add(rst.getString(7));
				values.add(rst.getString(8));
				values.add(rst.getString(9));
				values.add(rst.getString(10));
				values.add(rst.getString(11));
				values.add(rst.getString(12));
			}
			result.put("values", values);
			
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
	
	public Map<String, List<String>> reportMoneyMonth(String year) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				result.put("values", null);
				return result;
			}
			
			int size = tables.size();
			StringBuilder sqlSub = new StringBuilder();
			
			for (int i = 0; i < size; i++) {					
				sqlSub.append(" select price, in_date_time ");
				sqlSub.append(" from " + tables.get(i) + " ");
				
				if(i < (size - 1)) {
					sqlSub.append(" union all ");
				}
			}
			
			StringBuilder sql = new StringBuilder();			
			sql.append(" select sum(case when month(in_date_time) = 01 THEN price ELSE 0 END) as jan, ");
			sql.append(" sum(case when month(in_date_time) = 02 THEN price ELSE 0 END) as feb, ");
			sql.append(" sum(case when month(in_date_time) = 03 THEN price ELSE 0 END) as mar, ");
			sql.append(" sum(case when month(in_date_time) = 04 THEN price ELSE 0 END) as apr, ");
			sql.append(" sum(case when month(in_date_time) = 05 THEN price ELSE 0 END) as may, ");
			sql.append(" sum(case when month(in_date_time) = 06 THEN price ELSE 0 END) as june, ");
			sql.append(" sum(case when month(in_date_time) = 07 THEN price ELSE 0 END) as july, ");
			sql.append(" sum(case when month(in_date_time) = 08 THEN price ELSE 0 END) as aug, ");
			sql.append(" sum(case when month(in_date_time) = 09 THEN price ELSE 0 END) as sept, ");
			sql.append(" sum(case when month(in_date_time) = 10 THEN price ELSE 0 END) as oct, ");
			sql.append(" sum(case when month(in_date_time) = 11 THEN price ELSE 0 END) as nov, ");
			sql.append(" sum(case when month(in_date_time) = 12 THEN price ELSE 0 END) as decem ");
			sql.append(" from ( " + sqlSub + ") sub ");
			sql.append(" where year(in_date_time) = ? ");
						
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, year);
			rst = pstmt.executeQuery();
			List<String> values = new ArrayList<String>();
			
			if(rst.next()) {
				for (int i = 1; i <= 12; i++) {
					values.add(rst.getString(i));					
				}
			}
			result.put("values", values);
			
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
	
	public Map<String, List<String>> reportVehicleDay(String year, String month) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				result.put("values", null);
				return result;
			}
			
			int size = tables.size();
			StringBuilder sqlSub = new StringBuilder();
			
			for (int i = 0; i < size; i++) {					
				sqlSub.append(" select in_date_time ");
				sqlSub.append(" from " + tables.get(i) + " ");
				
				if(i < (size - 1)) {
					sqlSub.append(" union all ");
				}
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append(" select sum(day(in_date_time) = 01) as i, ");
			sql.append(" sum(day(in_date_time) = 02) as ii, ");
			sql.append(" sum(day(in_date_time) = 03) as iii, ");
			sql.append(" sum(day(in_date_time) = 04) as iv, ");
			sql.append(" sum(day(in_date_time) = 05) as v, ");
			sql.append(" sum(day(in_date_time) = 06) as vi, ");
			sql.append(" sum(day(in_date_time) = 07) as vii, ");
			sql.append(" sum(day(in_date_time) = 08) as viii, ");
			sql.append(" sum(day(in_date_time) = 09) as ix, ");
			sql.append(" sum(day(in_date_time) = 10) as x, ");
			sql.append(" sum(day(in_date_time) = 11) as xi, ");
			sql.append(" sum(day(in_date_time) = 12) as xii, ");
			sql.append(" sum(day(in_date_time) = 13) as xiii, ");
			sql.append(" sum(day(in_date_time) = 14) as xiv, ");
			sql.append(" sum(day(in_date_time) = 15) as xv, ");
			sql.append(" sum(day(in_date_time) = 16) as xvi, ");
			sql.append(" sum(day(in_date_time) = 17) as xvii, ");
			sql.append(" sum(day(in_date_time) = 18) as xviii, ");
			sql.append(" sum(day(in_date_time) = 19) as xix, ");
			sql.append(" sum(day(in_date_time) = 20) as xx, ");
			sql.append(" sum(day(in_date_time) = 21) as xxi, ");
			sql.append(" sum(day(in_date_time) = 22) as xxii, ");
			sql.append(" sum(day(in_date_time) = 23) as xxiii, ");
			sql.append(" sum(day(in_date_time) = 24) as xxiv, ");
			sql.append(" sum(day(in_date_time) = 25) as xxv, ");
			sql.append(" sum(day(in_date_time) = 26) as xxvi, ");
			sql.append(" sum(day(in_date_time) = 27) as xxvii, ");
			sql.append(" sum(day(in_date_time) = 28) as xxviii, ");
			sql.append(" sum(day(in_date_time) = 29) as xxix, ");
			sql.append(" sum(day(in_date_time) = 30) as xxx, ");
			sql.append(" sum(day(in_date_time) = 31) as xxxi ");
			sql.append(" from (" + sqlSub + ") sub ");
			sql.append(" where year(in_date_time) = ? and month(in_date_time) = ? ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, year);
			pstmt.setString(2, month);
			rst = pstmt.executeQuery();
			List<String> values = new ArrayList<String>();
			
			if(rst.next()) {
				int daysMonth = getDaysMonth(Integer.parseInt(year), Integer.parseInt(month));
				LOG.debug("year: " + year +", month: " + month + ", daysMonth: " + daysMonth);
				
				for (int i = 1; i <= daysMonth; i++) {					
					values.add(rst.getString(i));
				}
			}
			result.put("values", values);
			
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
	
	public Map<String, List<String>> reportMoneyDay(String year, String month) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				result.put("values", null);
				return result;
			}
			
			int size = tables.size();
			StringBuilder sqlSub = new StringBuilder();
			
			for (int i = 0; i < size; i++) {					
				sqlSub.append(" select price, in_date_time ");
				sqlSub.append(" from " + tables.get(i) + " ");
				
				if(i < (size - 1)) {
					sqlSub.append(" union all ");
				}
			}
			
			StringBuilder sql = new StringBuilder();			
			sql.append(" select sum(case when day(in_date_time) = 01 THEN price ELSE 0 END) as i, ");
			sql.append(" sum(case when day(in_date_time) = 02 THEN price ELSE 0 END) as ii, ");
			sql.append(" sum(case when day(in_date_time) = 03 THEN price ELSE 0 END) as iii, ");
			sql.append(" sum(case when day(in_date_time) = 04 THEN price ELSE 0 END) as iv, ");
			sql.append(" sum(case when day(in_date_time) = 05 THEN price ELSE 0 END) as v, ");
			sql.append(" sum(case when day(in_date_time) = 06 THEN price ELSE 0 END) as vi, ");
			sql.append(" sum(case when day(in_date_time) = 07 THEN price ELSE 0 END) as vii, ");
			sql.append(" sum(case when day(in_date_time) = 08 THEN price ELSE 0 END) as viii, ");
			sql.append(" sum(case when day(in_date_time) = 09 THEN price ELSE 0 END) as ix, ");
			sql.append(" sum(case when day(in_date_time) = 10 THEN price ELSE 0 END) as x, ");
			sql.append(" sum(case when day(in_date_time) = 11 THEN price ELSE 0 END) as xi, ");
			sql.append(" sum(case when day(in_date_time) = 12 THEN price ELSE 0 END) as xii, ");
			sql.append(" sum(case when day(in_date_time) = 13 THEN price ELSE 0 END) as xiii, ");
			sql.append(" sum(case when day(in_date_time) = 14 THEN price ELSE 0 END) as xiv, ");
			sql.append(" sum(case when day(in_date_time) = 15 THEN price ELSE 0 END) as xv, ");
			sql.append(" sum(case when day(in_date_time) = 16 THEN price ELSE 0 END) as xvi, ");
			sql.append(" sum(case when day(in_date_time) = 17 THEN price ELSE 0 END) as xvii, ");
			sql.append(" sum(case when day(in_date_time) = 18 THEN price ELSE 0 END) as xviii, ");
			sql.append(" sum(case when day(in_date_time) = 19 THEN price ELSE 0 END) as xix, ");
			sql.append(" sum(case when day(in_date_time) = 20 THEN price ELSE 0 END) as xx, ");
			sql.append(" sum(case when day(in_date_time) = 21 THEN price ELSE 0 END) as xxi, ");
			sql.append(" sum(case when day(in_date_time) = 22 THEN price ELSE 0 END) as xxii, ");
			sql.append(" sum(case when day(in_date_time) = 23 THEN price ELSE 0 END) as xxiii, ");
			sql.append(" sum(case when day(in_date_time) = 24 THEN price ELSE 0 END) as xxiv, ");
			sql.append(" sum(case when day(in_date_time) = 25 THEN price ELSE 0 END) as xxv, ");
			sql.append(" sum(case when day(in_date_time) = 26 THEN price ELSE 0 END) as xxvi, ");
			sql.append(" sum(case when day(in_date_time) = 27 THEN price ELSE 0 END) as xxvii, ");
			sql.append(" sum(case when day(in_date_time) = 28 THEN price ELSE 0 END) as xxviii, ");
			sql.append(" sum(case when day(in_date_time) = 29 THEN price ELSE 0 END) as xxix, ");
			sql.append(" sum(case when day(in_date_time) = 30 THEN price ELSE 0 END) as xxx, ");
			sql.append(" sum(case when day(in_date_time) = 31 THEN price ELSE 0 END) as xxxi ");
			sql.append(" from ( " + sqlSub + " ) sub ");
			sql.append(" where year(in_date_time) = ? and month(in_date_time) = ? ");
			
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, year);
			pstmt.setString(2, month);
			rst = pstmt.executeQuery();
			List<String> values = new ArrayList<String>();
			
			if(rst.next()) {
				int daysMonth = getDaysMonth(Integer.parseInt(year), Integer.parseInt(month));
				LOG.debug("year: " + year +", month: " + month + ", daysMonth: " + daysMonth);
				
				for (int i = 1; i <= daysMonth; i++) {					
					values.add(rst.getString(i));
				}
			}
			result.put("values", values);
			
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
	
	private int getDaysMonth(int year, int month) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		return calendar.getActualMaximum(Calendar.DATE);
	}
	
}