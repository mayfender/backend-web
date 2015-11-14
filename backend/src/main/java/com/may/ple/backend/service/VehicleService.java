package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.VehicleSaveCriteriaReq;
import com.may.ple.backend.criteria.VehicleSearchCriteriaReq;
import com.may.ple.backend.criteria.VehicleSearchCriteriaResp;
import com.may.ple.backend.entity.VehicleParking;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.util.DatabaseUtil;
import com.may.ple.backend.util.DateTimeUtil;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

@Service
public class VehicleService {
	private static final Logger LOG = Logger.getLogger(VehicleService.class.getName());
	private DataSource dataSource;
	
	@Autowired
	public VehicleService(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public VehicleSearchCriteriaResp searchVehicleParking(VehicleSearchCriteriaReq req) throws Exception {
		VehicleSearchCriteriaResp resp = new VehicleSearchCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {
			conn = dataSource.getConnection();
			
			List<String> tables = DatabaseUtil.getVehicleParkingTable(conn);
			if(tables == null || tables.isEmpty()) {
				LOG.debug("Not found vehicle_parking_xxx table");
				return resp;
			}
			
			int size = tables.size();
			StringBuilder sql = new StringBuilder();
			
			for (int i = 0; i < size; i++) {			
				sql.append(" select id, in_date_time, out_date_time, price, status, license_no");
				sql.append(" from " + tables.get(i) + " where 1=1 ");
				
				if(req != null) {
					if(req.getLicenseNo() != null) {
						sql.append(" and license_no like '%" + req.getLicenseNo() + "%' ");
					}
					if(!StringUtils.isBlank(req.getStartDate())) {
						sql.append(" and created_date_time >= STR_TO_DATE('" + req.getStartDate() + " 00:00:00','%d-%m-%Y %H:%i:%s') ");
					}
					if(!StringUtils.isBlank(req.getEndDate())) {
						sql.append(" and created_date_time <= STR_TO_DATE('" + req.getEndDate() + " 23:59:59','%d-%m-%Y %H:%i:%s') ");
					}
					if(req.getStatus() != null) {
						sql.append(" and status = " + req.getStatus());
					}
				}
				
				if(i < (size - 1)) {
					sql.append(" union all ");
				}
			}
			
			//-----------------: Get size
			try {				
				StringBuilder sqlCount = new StringBuilder();
				sqlCount.append("select count(id) as size from ( " + sql.toString()+ " ) sub");
				pstmt = conn.prepareStatement(sqlCount.toString());
				rst = pstmt.executeQuery();
				if(rst.next()) {
					resp.setTotalItems(rst.getLong("size"));
				}
			} catch (Exception e) {
				LOG.error(e.toString());
				throw e;
			} finally {
				try { if(rst != null) rst.close(); } catch (Exception e2) {}
				try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			}
			
			
			//-----------------: Get data 
			sql.append(" order by in_date_time desc ");
			sql.append(" limit " + (req.getCurrentPage() - 1) * req.getItemsPerPage() + ", " + req.getItemsPerPage());
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<VehicleParking> vehicleParkings = new ArrayList<VehicleParking>();
			VehicleParking vehicleParking;
			Integer price;
			
			while(rst.next()) {
				price = rst.getInt("price");
				price = rst.wasNull() ? null : price;
				
				vehicleParking = new VehicleParking(rst.getTimestamp("in_date_time"), 
													rst.getTimestamp("out_date_time"), 
													price, 
													rst.getInt("status"), 
													rst.getString("license_no"), null, null);
				vehicleParking.setId(rst.getLong("id"));
				
				if(vehicleParking.getInDateTime() != null && vehicleParking.getOutDateTime() != null) {					
					vehicleParking.setDateTimeDiffMap(DateTimeUtil.dateTimeDiff(vehicleParking.getInDateTime(), vehicleParking.getOutDateTime()));					
				}
				
				vehicleParkings.add(vehicleParking);
			}
			resp.setVehicleParkings(vehicleParkings);
			
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
	
	public void saveVehicleParking(VehicleSaveCriteriaReq req, Timestamp timestamp) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" insert into vehicle_parking (in_date_time, license_no, checkin_device_id, gate_in_name) ");
			sql.append(" value(?, ?, ?, ?)");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setTimestamp(1, timestamp);
			pstmt.setString(2, req.getLicenseNo());
			pstmt.setString(3, req.getDeviceId());
			pstmt.setString(4, req.getGateName());
			int executeUpdate = pstmt.executeUpdate();
			
			if(executeUpdate == 0) 
				throw new CustomerException(4001, "Cann't save");
			
		} catch (MySQLSyntaxErrorException e) {
			LOG.error(e.toString());
			throw e;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
}
