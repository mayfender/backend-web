package com.may.ple.backend.excel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.may.ple.backend.entity.SptRegistration;

public class RegisterReport {
	private static final Logger LOG = Logger.getLogger(RegisterReport.class.getName());	
	private final String DATE_FORMAT = "%1$td/%1$tm/%1$tY";
	private final int START_ROW = 1;
	private List<SptRegistration> registereds;
	private ByteArrayOutputStream out;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	private String path;
	
	public RegisterReport(List<SptRegistration> registereds, String path) {
		this.registereds = registereds;
		this.path = path;
	}
	
	public byte[] proceed() throws Exception {
		LOG.debug("Start");
		try {
			out = new ByteArrayOutputStream();
			workbook = new XSSFWorkbook(new FileInputStream(path));
			sheet = workbook.getSheetAt(0);
			
			
			writeData();
			
			
			//----------------------------------------------------
			workbook.write(out);
			LOG.debug("End");
			return out.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try {if(workbook != null) workbook.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
		}
	}

	public void writeData() {
		
		if(registereds == null) return;
		
		Date birthday, regDate, expDate, createdDateTime, updatedDateTime;
		SptRegistration reg;
		XSSFRow row = sheet.createRow(START_ROW);
		XSSFCell cell[] = new XSSFCell[28];
		int size = registereds.size();
		
		CellStyle style_1 = workbook.createCellStyle();
		style_1.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		
		CellStyle style_2 = workbook.createCellStyle();
		style_2.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		
		CellStyle style_3 = workbook.createCellStyle();
		style_3.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		
		for (int i = 0; i < size; i++) {
			row = sheet.createRow(START_ROW + i);
			reg = registereds.get(i);
			for (int j = 0; j < cell.length; j++) {
				cell[j] = row.createCell(j);
				
				switch (j) {
				case 0: cell[j].setCellValue(i + 1); cell[j].setCellStyle(style_2); break;
				case 1: cell[j].setCellValue(reg.getMemberId()); cell[j].setCellStyle(style_2); break;
				case 2: cell[j].setCellValue(reg.getFingerId()); cell[j].setCellStyle(style_2); break;
				case 3: cell[j].setCellValue(reg.getPrefixName().getDisplayValue()); break;
				case 4: cell[j].setCellValue(reg.getFirstname()); break;
				case 5: cell[j].setCellValue(reg.getLastname()); break;
				case 6: {					
					birthday = reg.getBirthday();
					String btd = "";
					
					if(birthday != null) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(birthday);
						calendar.add(Calendar.YEAR, 543);
						btd = String.format(DATE_FORMAT, calendar.getTime());
					}
					cell[j].setCellValue(btd); 
					cell[j].setCellStyle(style_2); 
					break;
				}
				case 7: cell[j].setCellValue(reg.getCitizenId()); cell[j].setCellStyle(style_2); break;
				case 8: cell[j].setCellValue(reg.getMemberTypeName()); break;
				case 9: {
					regDate = reg.getRegisterDate();
					String regDateStr = "";
					if(regDate != null) {
						regDateStr = String.format(DATE_FORMAT, regDate);
					}
					cell[j].setCellValue(regDateStr); 
					cell[j].setCellStyle(style_2); 
					break;
				}
				case 10: {					
					expDate = reg.getExpireDate();
					String expDateStr = "";
					if(expDate != null) {
						expDateStr = String.format(DATE_FORMAT, expDate);
					}
					cell[j].setCellValue(expDateStr); 
					cell[j].setCellStyle(style_2); 
					break;
				}
				case 11: cell[j].setCellValue(reg.getEnabled() == 1 ? "เปิด" : "ปิด"); cell[j].setCellStyle(style_2); break;
				case 12: cell[j].setCellValue(reg.getConAddress()); break;
				case 13: cell[j].setCellValue(reg.getZipcode().getDistrict().getDistrictName()); break;
				case 14: cell[j].setCellValue(reg.getZipcode().getDistrict().getAmphur().getAmphurName()); break;
				case 15: cell[j].setCellValue(reg.getZipcode().getDistrict().getProvince().getProvinceName()); break;
				case 16: cell[j].setCellValue(reg.getZipcode().getZipcode()); cell[j].setCellStyle(style_2); break;
				case 17: cell[j].setCellValue(reg.getConTelNo()); cell[j].setCellStyle(style_2); break;
				case 18: cell[j].setCellValue(reg.getConMobileNo1()); cell[j].setCellStyle(style_2); break;
				case 19: cell[j].setCellValue(reg.getConMobileNo2()); cell[j].setCellStyle(style_2); break;
				case 20: cell[j].setCellValue(reg.getConMobileNo3()); cell[j].setCellStyle(style_2); break;
				case 21: cell[j].setCellValue(reg.getConEmail()); break;
				case 22: cell[j].setCellValue(reg.getConFacebook()); break;
				case 23: cell[j].setCellValue(reg.getConLineId()); break;
				case 24: {
					createdDateTime = reg.getCreatedDateTime();
					String createdDateTimeStr = "";
					if(createdDateTime != null) {
						createdDateTimeStr = String.format(DATE_FORMAT, createdDateTime);
					}
					cell[j].setCellValue(createdDateTimeStr);
					cell[j].setCellStyle(style_2); 
					break;					
				}
				case 25: cell[j].setCellValue(reg.getCreatedByName()); break;
				case 26: {
					updatedDateTime = reg.getUpdatedDateTime();
					String updatedDateStr = "";
					if(updatedDateStr != null) {
						updatedDateStr = String.format(DATE_FORMAT, updatedDateTime);
					}
					cell[j].setCellValue(updatedDateStr);
					cell[j].setCellStyle(style_2); 
					break;
				}
				case 27: cell[j].setCellValue(reg.getUpdatedByName()); break;
				default: break;
				}
			}
		}
	}

}
