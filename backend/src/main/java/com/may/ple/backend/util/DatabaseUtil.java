package com.may.ple.backend.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class DatabaseUtil {
	private static final Logger LOG = Logger.getLogger(DatabaseUtil.class.getName());
	
	public static List<String> getVehicleParkingTable(Connection conn) throws Exception {
		ResultSet rs = null;
		List<String> tables = new ArrayList<String>();
		
		try {
			DatabaseMetaData md = conn.getMetaData();
			rs = md.getTables(null, null, "vehicle_parking%", new String[]{"TABLE"});
			while (rs.next()) {
				/*
				 (1)TABLE_CAT, (2)TABLE_SCHEM, (3)TABLE_NAME, (4)TABLE_TYPE, (5)REMARKS 
				 */
			  tables.add(rs.getString(3));
			}
			
			return tables;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(rs != null) rs.close();
		}
	}
	
	public static void createVehicleParkingTable(Connection conn, int year) throws Exception {
		Statement stmt = null;
		
		try {	
			LOG.debug("Create table");
			StringBuilder sql = new StringBuilder();
			sql.append(" CREATE TABLE vehicle_parking_" + year + " ");
			sql.append(" ( ");
			sql.append(" id int NOT NULL AUTO_INCREMENT, ");
			sql.append(" in_date_time datetime, ");
			sql.append(" out_date_time datetime, ");
			sql.append(" price int, ");
			sql.append(" status tinyint DEFAULT '0' NOT NULL, ");
			sql.append(" license_no VARCHAR(5) NOT NULL, ");
			sql.append(" device_id VARCHAR(100), ");
			sql.append(" gate_name VARCHAR(100), ");
			sql.append(" PRIMARY KEY (id)");
			sql.append(" ) ");
			sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8; ");
			
			stmt = conn.createStatement();
			stmt.executeUpdate(sql.toString());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(stmt != null) stmt.close();
		}
	}

}
