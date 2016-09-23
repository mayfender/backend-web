package com.may.ple.backend.service;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Amphures;
import com.may.ple.backend.entity.Districts;
import com.may.ple.backend.entity.Provinces;
import com.may.ple.backend.entity.Zipcodes;
import com.may.ple.backend.model.FileDetail;
import com.may.ple.backend.model.ThailandRegion;
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
		ObjectInputStream ois = null;
		
		try {
			LOG.debug("Start");
			
			Date date = Calendar.getInstance().getTime();
			FileDetail fd = FileUtil.getFileName(fileDetail, date);
			
			LOG.debug("File ext: " + fd.fileExt);
			if(!fd.fileExt.equals(".dat")) {
				throw new Exception("File type is wrong.");
			}
			
			ois = new ObjectInputStream(uploadedInputStream);
			
			ThailandRegion region = (ThailandRegion)ois.readObject();
			
			LOG.debug("Drop Amphures collection");
			templateCenter.dropCollection(Amphures.class);
			LOG.debug("Drop Districts collection");
			templateCenter.dropCollection(Districts.class);
			LOG.debug("Drop Provinces collection");
			templateCenter.dropCollection(Provinces.class);
			LOG.debug("Drop Zipcodes collection");
			templateCenter.dropCollection(Zipcodes.class);
			LOG.debug("Drop All collection are droped");
			
			LOG.debug("Insert Amphures collection");
			templateCenter.insert(region.getAmphures(), Amphures.class);
			LOG.debug("Insert Districts collection");
			templateCenter.insert(region.getDistricts(), Districts.class);
			LOG.debug("Insert Provinces collection");
			templateCenter.insert(region.getProvinces(), Provinces.class);
			LOG.debug("Insert Zipcodes collection");
			templateCenter.insert(region.getZipcodes(), Zipcodes.class);
			LOG.debug("All collection are saved");
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(ois != null) ois.close();
		}
	}
	
	public List<Provinces> findProvince(String provinceName) {
		try {
			
			Query query = Query.query(Criteria.where("provinceName").regex(Pattern.compile(provinceName, Pattern.CASE_INSENSITIVE)));
			query.fields().include("provinceName");
			
			List<Provinces> provinces = templateCenter.find(query, Provinces.class);
			
			return provinces;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Amphures> findAmphure(Long provinceId) {
		try {
			
			Query query = Query.query(Criteria.where("provinceId").is(provinceId));
			query.fields().include("amphurName");
			
			List<Amphures> amphures = templateCenter.find(query, Amphures.class);
			
			return amphures;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Districts> findDistrict(Long provinceId, Long amphureId) {
		try {
			
			Query query = Query.query(Criteria.where("provinceId").is(provinceId).and("amphurId").is(amphureId));
			query.fields().include("districtName").include("districtCode");
			
			List<Districts> districts = templateCenter.find(query, Districts.class);
			
			return districts;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Zipcodes findZipcode(String districtCode) {
		try {
			
			Query query = Query.query(Criteria.where("districtCode").is(districtCode));
			query.fields().include("zipcode");
			
			Zipcodes zipcodes = templateCenter.findOne(query, Zipcodes.class);
			
			return zipcodes;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*public static void main(String[] args) {
		try {
			serialze();
//			deserialze();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void serialze() throws Exception {
		Connection conn = null;
		
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://localhost/spt_trader?user=root&password=1234");
			
			
			ThailandRegion region = new ThailandRegion();
			
			List<Amphures> amphures = getAmphures(conn);
			List<Districts> districts = getDistricts(conn);
			List<Provinces> provinces = getProvinces(conn);
			List<Zipcodes> zipcodes = getZipcodes(conn);
			
			region.setAmphures(amphures);
			region.setDistricts(districts);
			region.setProvinces(provinces);
			region.setZipcodes(zipcodes);
			
			
			FileOutputStream fout = new FileOutputStream("C:\\Users\\sarawuti\\Desktop\\file serializer\\thailandRegion.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(region);
			oos.close();
			
			System.out.println("finished");			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	private static List<Amphures> getAmphures(Connection conn) throws Exception {
		Statement stmt = null;
		ResultSet rst = null;
		
		try {
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
			
			return aphures;
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(stmt != null) stmt.close(); } catch (Exception e2) {}
		}
	}
	
	private static List<Districts> getDistricts(Connection conn) throws Exception {
		Statement stmt = null;
		ResultSet rst = null;
		
		try {
			String sql = "select * from districts";
			
			stmt = conn.createStatement();
			rst = stmt.executeQuery(sql);
			
			List<Districts> districts = new ArrayList<>();
			Districts district;
			
			while(rst.next()) {
				district = new Districts();
				district.setId(rst.getLong("DISTRICT_ID"));
				district.setDistrictCode(rst.getString("DISTRICT_CODE"));
				district.setDistrictName(rst.getString("DISTRICT_NAME"));
				district.setDistrictNameEng(rst.getString("DISTRICT_NAME_ENG"));
				district.setAmphurId(rst.getLong("AMPHUR_ID"));
				district.setProvinceId(rst.getLong("PROVINCE_ID"));
				district.setGeoId(rst.getLong("GEO_ID"));
				
				districts.add(district);			
			}
			
			return districts;
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(stmt != null) stmt.close(); } catch (Exception e2) {}
		}
	}
	
	private static List<Provinces> getProvinces(Connection conn) throws Exception {
		Statement stmt = null;
		ResultSet rst = null;
		
		try {
			String sql = "select * from provinces";
			
			stmt = conn.createStatement();
			rst = stmt.executeQuery(sql);
			
			List<Provinces> provinces = new ArrayList<>();
			Provinces province;
			
			while(rst.next()) {
				province = new Provinces();
				province.setId(rst.getLong("PROVINCE_ID"));
				province.setProvinceCode(rst.getString("PROVINCE_CODE"));
				province.setProvinceName(rst.getString("PROVINCE_NAME"));
				province.setProvinceNameEng(rst.getString("PROVINCE_NAME_ENG"));
				province.setGeoId(rst.getLong("GEO_ID"));
				provinces.add(province);			
			}
			
			return provinces;
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(stmt != null) stmt.close(); } catch (Exception e2) {}
		}
	}
	
	private static List<Zipcodes> getZipcodes(Connection conn) throws Exception {
		Statement stmt = null;
		ResultSet rst = null;
		
		try {
			String sql = "select * from zipcodes";
			
			stmt = conn.createStatement();
			rst = stmt.executeQuery(sql);
			
			List<Zipcodes> zipcodes = new ArrayList<>();
			Zipcodes Zipcode;
			
			while(rst.next()) {
				Zipcode = new Zipcodes();
				Zipcode.setId(rst.getLong("id"));
				Zipcode.setDistrictCode(rst.getString("district_code"));
				Zipcode.setZipcode(rst.getString("zipcode"));
				zipcodes.add(Zipcode);			
			}
			
			return zipcodes;
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(stmt != null) stmt.close(); } catch (Exception e2) {}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	private static void deserialze() throws Exception {
		ObjectInputStream ois = null;
		
		try {

			FileInputStream fin = new FileInputStream("C:\\Users\\sarawuti\\Desktop\\file serializer\\thailandRegion.dat");
			ois = new ObjectInputStream(fin);
			
			ThailandRegion region = (ThailandRegion)ois.readObject();

			System.out.println("finished");
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(ois != null) ois.close(); } catch (Exception e) {}
		}
	}*/
			
}
