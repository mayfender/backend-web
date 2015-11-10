package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.VehicleCheckOutCriteriaReq;
import com.may.ple.backend.criteria.VehicleSaveCriteriaReq;
import com.may.ple.backend.criteria.VehicleSearchCriteriaReq;
import com.may.ple.backend.criteria.VehicleSearchCriteriaResp;
import com.may.ple.backend.custom.PropertiesCustom;
import com.may.ple.backend.entity.VehicleParking;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.util.DatabaseUtil;
import com.may.ple.backend.util.DateTimeUtil;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

@Service
public class VehicleService {
	private static final Logger LOG = Logger.getLogger(VehicleService.class.getName());
	private DataSource dataSource;
	private PropertiesCustom dbProp;
	
	@Autowired
	public VehicleService(DataSource dataSource, PropertiesCustom dbProp) {
		this.dataSource = dataSource;
		this.dbProp = dbProp;
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
						sql.append(" and in_date_time >= STR_TO_DATE('" + req.getStartDate() + " 00:00:00','%d-%m-%Y %H:%i:%s') ");
					}
					if(!StringUtils.isBlank(req.getEndDate())) {
						sql.append(" and in_date_time <= STR_TO_DATE('" + req.getEndDate() + " 23:59:59','%d-%m-%Y %H:%i:%s') ");
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
			/*try {
				if(e.getSQLState() != null || e.getSQLState().equals("42S02")) {
					LOG.debug("Don't have the TABLE of year: "+year);
					DatabaseUtil.createVehicleParkingTable(conn, year);
					LOG.debug("Re save again");
					saveVehicleParking(req);
				}
			} catch (Exception e2) {
				LOG.error(e.toString());
				throw e2;
			}*/
			
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
	
	public VehicleParking checkOutVehicle(VehicleCheckOutCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		VehicleParking vehicleParking = null;
		
		try {
			conn = dataSource.getConnection();
			
			// 1. Find VehicleParking
			if(req.getId() == null) {
				LOG.debug("Call findVehicleByLicenseNo");
				vehicleParking = findVehicleByLicenseNo(conn, req.getLicenseNo());
				req.setId(vehicleParking != null? vehicleParking.getId() : null);
			} else {
				LOG.debug("Call findVehicleById");
				vehicleParking = findVehicleById(conn, req.getId());
			}
			if(vehicleParking == null) throw new CustomerException(3000, "Not found vehicleParking");
			
			vehicleParking.setOutDateTime(DateTimeUtil.getTimstampNoMillisecond());
			
			Map<String, Long> dateTimeDiff = DateTimeUtil.dateTimeDiff(vehicleParking.getInDateTime(), vehicleParking.getOutDateTime());
			vehicleParking.setDateTimeDiffMap(dateTimeDiff);
			
			// 2. Calculate price
			LOG.debug("Call calculatePrice");
			int price = calculatePrice(dateTimeDiff);
			vehicleParking.setPrice(price);
			
			// 3. Update out-time
			LOG.debug("Call updateVehicleOut");
			updateVehicleOut(conn, req, price, (Timestamp)vehicleParking.getOutDateTime());
			
			return vehicleParking;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	private int calculatePrice(Map<String, Long> dateTimeDiff) {
		try {
			Boolean unlimtedTime = dbProp.getBoolean("unlimted.time");
			int price;
			
			if(unlimtedTime) {
				LOG.debug("Unlimited-time mode");
				price = dbProp.getInt("price.per.time");
			} else {
				LOG.debug("Depend on time mode");
				long hours = dateTimeDiff.get("hours");
				long minutes = dateTimeDiff.get("minutes");
				long minuteToHour = dbProp.getLong("minute.to.hour");
				
				// If minute since (minute.to.hour) assume to be a hour.
				if(minutes >= minuteToHour) {
					hours++;
				}
				
				long beforeHour = dbProp.getLong("before.hour");
				if(hours <= beforeHour) {
					price = new Long(dbProp.getLong("before.hour.price.rate") * hours).intValue();
				} else {
					price = new Long(dbProp.getLong("before.hour.price.rate") * beforeHour).intValue();
					long secondSec = hours - beforeHour;
					price += dbProp.getInt("after.price.rate") * secondSec;
				}
			}
			
			return price;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private void updateVehicleOut(Connection conn, VehicleCheckOutCriteriaReq req, int price, Timestamp timestamp) throws Exception {
		PreparedStatement pstmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" update vehicle_parking ");
			sql.append(" set out_date_time = ?, ");
			sql.append(" price = ?, ");
			sql.append(" status = 1, ");
			sql.append(" checkout_device_id = ?, ");
			sql.append(" gate_out_name = ?, ");
			sql.append(" reason_no_scan = ? ");
			sql.append(" where id = ? ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setTimestamp(1, timestamp); // out_date_time
			pstmt.setInt(2, price); // price
			pstmt.setString(3, req.getDeviceId()); //device_id
			pstmt.setString(4, req.getGateName()); // gate_name
			
			if(req.getReasonNoScan() != null) 
				pstmt.setInt(5, req.getReasonNoScan()); // reason_no_scan				
			else 
				pstmt.setNull(5, Types.INTEGER); // reason_no_scan				
			
			pstmt.setLong(6, req.getId()); // id
			
			int executeUpdate = pstmt.executeUpdate();
			if(executeUpdate == 0) 
				throw new CustomerException(4000, "Cann't update");
			
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
		}
	}
	
	private VehicleParking findVehicleByLicenseNo(Connection conn, String licenseNo) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, in_date_time, out_date_time, price ");
			sql.append(" from vehicle_parking ");
			sql.append(" where status = 0 ");
			sql.append(" and license_no = ? ");
			sql.append(" order by in_date_time desc ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setString(1, licenseNo);
			rst = pstmt.executeQuery();
			VehicleParking vehicleParking = null;
			
			if(rst.next()) {
				vehicleParking = new VehicleParking(
					rst.getTimestamp("in_date_time"), 
					rst.getTimestamp("out_date_time"), 
					rst.getInt("price"), 
					null, null, null, null
				);
				vehicleParking.setId(rst.getLong("id"));
			}
			
			return vehicleParking;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
		}
	}
	
	private VehicleParking findVehicleById(Connection conn, Long id) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		VehicleParking vehicleParking = null;
		
		try {
			
			// 4. Query to return data
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, in_date_time, out_date_time, price ");
			sql.append(" from vehicle_parking ");
			sql.append(" where and id = ? ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setLong(1, id);
			rst = pstmt.executeQuery();
			
			if(rst.next()) {
				vehicleParking = new VehicleParking(
					rst.getTimestamp("in_date_time"), rst.getTimestamp("out_date_time"), 
					rst.getInt("price"), null, null, null, null
				);
				vehicleParking.setId(rst.getLong("id"));
			}
			
			return vehicleParking;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
		}
	}
	
}
