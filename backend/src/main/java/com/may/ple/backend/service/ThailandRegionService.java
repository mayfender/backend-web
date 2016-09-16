package com.may.ple.backend.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Amphures;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.utils.FileUtil;

@Service
public class ThailandRegionService {
	private static final Logger LOG = Logger.getLogger(ThailandRegionService.class.getName());
	private MongoTemplate templateCenter;
	
	@Autowired	
	public ThailandRegionService(MongoTemplate templateCenter) {
		this.templateCenter = templateCenter;
	}
	
	public void upload(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		BufferedReader reader = null;
		
		try {
			LOG.debug("Start");
			
			Date date = Calendar.getInstance().getTime();
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			if(!fd.fileExt.equals(".sql")) {
				throw new Exception("File type is wrong.");
			}
				
			reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
			
//			templateCenter.insert(batchToSave, Amphures.class);
			
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(reader != null) reader.close();
		}
	}
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		try {
			deserialze();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void serialze() throws Exception {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rst = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost/spt_trader?user=root&password=1234");
			
			String sql = "select * from amphures";
			
			stmt = conn.createStatement();
			rst = stmt.executeQuery(sql);
			List<Amphures> aphures = new ArrayList<>();
			Amphures aphure;
			
			while(rst.next()) {
				aphure = new Amphures();
				aphure.setId(rst.getLong("AMPHUR_ID"));
				aphure.setAmphurCode(rst.getString("AMPHUR_CODE"));
				aphure.setAmphurName(rst.getString("AMPHUR_NAME"));
				aphure.setAmphurNameEng(rst.getString("AMPHUR_NAME_ENG"));
				aphure.setGeoId(rst.getLong("GEO_ID"));
				aphure.setProvinceId(rst.getLong("PROVINCE_ID"));
				
				aphures.add(aphure);			
			}
			
			FileOutputStream fout = new FileOutputStream("C:\\Users\\sarawuti\\Desktop\\file serializer\\mayfender.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(aphures);
			oos.close();
			
			System.out.println("finished");			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(stmt != null) stmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public static void deserialze() throws Exception {
		List<Amphures> aphures;
		ObjectInputStream ois = null;
		
		try {

			FileInputStream fin = new FileInputStream("C:\\Users\\sarawuti\\Desktop\\file serializer\\mayfender.dat");
			ois = new ObjectInputStream(fin);
			aphures = (List<Amphures>) ois.readObject();
			
			for (Amphures amphures : aphures) {
				System.out.println(amphures);
			}

			System.out.println("finished");
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(ois != null) ois.close(); } catch (Exception e) {}
		}
	}
			
}
