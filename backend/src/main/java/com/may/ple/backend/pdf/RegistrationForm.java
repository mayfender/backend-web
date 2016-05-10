package com.may.ple.backend.pdf;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.security.crypto.codec.Base64;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptRegistration;

public class RegistrationForm extends BaseReportBuilder {
	private static final Logger LOG = Logger.getLogger(RegistrationForm.class.getName());
	private final String DATE_FORMAT = "%1$td/%1$tm/%1$tY";
	private SptMemberType memberType;
	private SptRegistration reg;
	private Document document;
	private Font fontBold;
	private Font font;
	
	public RegistrationForm(SptRegistration reg, SptMemberType memberType) {
		this.reg = reg;
		this.memberType = memberType;
	}
	
	private void pageBorder(PdfWriter writer) throws Exception {
		Rectangle rect = new Rectangle(15, 36, 580, 825);
        rect.setBorder(Rectangle.BOX);
        rect.setBorderWidth(0.7f);
        PdfContentByte canvas = writer.getDirectContent();
        canvas.rectangle(rect);
	}
	
	private PdfPTable memberDetail() throws Exception {
		try {
			PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setWidths(new int[]{25, 45, 3, 27});
			
			PdfPCell cell = new PdfPCell(new Phrase("เลขที่สมาชิก", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0.7f);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(15);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + reg.getMemberId(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0.7f);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);			
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(15);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell();
			cell.setBorderWidth(0);
			cell.setRowspan(6);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			
			Image logo = null;
			if(reg.getImgBase64() != null) {
				LOG.debug("Show Image");
				byte[] bytes = Base64.decode(reg.getImgBase64().getBytes());				
				logo = Image.getInstance(bytes);
			}
			
			cell = new PdfPCell(logo, true);
			cell.setFixedHeight(100);
			cell.setBorderWidth(0.7f);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(1);
			cell.setRowspan(6);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("Finger Scan", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getFingerId() == null ? ":" : ": " + reg.getFingerId(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("ชื่อสมาชิก", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + reg.getPrefixName().getDisplayValue()+ " " + reg.getFirstname() + " " + reg.getLastname(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("ชื่อสมาชิกภาษาอังกฤษ", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + reg.getPrefixName().getDisplayValue()+ " " + reg.getFirstnameEng() + " " + reg.getLastnameEng(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("วันเดือนปีเกิด", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			Date birthday = reg.getBirthday();
			String bthday = "";
			if(birthday != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(reg.getBirthday());
				calendar.add(Calendar.YEAR, 543);
				bthday = String.format(DATE_FORMAT, calendar.getTime());
			}
			
			cell = new PdfPCell(new Phrase(": " + bthday, font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("หมายเลขบัตรประชาชน", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0.7f);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingBottom(15);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + reg.getCitizenId(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0.7f);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingBottom(15);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			
			return table;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private PdfPTable registerDetail() throws Exception {
		try {
			PdfPTable table = new PdfPTable(4);
			table.setSpacingBefore(15);
			table.setWidthPercentage(100);
			table.setWidths(new int[]{25, 25, 10, 40});
						
			PdfPCell cell = new PdfPCell(new Phrase("ประเภทสมาชิก", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0.7f);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(15);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + memberType.getMemberTypeName(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0.7f);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);			
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(15);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("ราคาค่าสมัครสมาชิก", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + String.format("%,.2f", reg.getPrice()), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);			
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("ประเภทการชำระเงิน", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getPayType() == 1 ? ": เงินสด" : ": บัตรเครดิต", font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);			
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("วันที่เริ่มสมาชิก", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + String.format(DATE_FORMAT, reg.getRegisterDate()), font));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase("วันหมดอายุ", fontBold));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(": " + String.format(DATE_FORMAT, reg.getExpireDate()), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("จำนวนครั้งที่สมัคร", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(":", font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			Phrase addr = new Phrase();
			addr.add(new Chunk("ที่อยู่ ", fontBold));
			addr.add(new Chunk(reg.getConAddress(), font));
			
			cell = new PdfPCell(addr);
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			cell.setColspan(4);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			String districtPrefix, amphurPrefix, provincePrefix;
			if(reg.getZipcode().getDistrict().getProvince().getProvinceName().trim().equals("กรุงเทพมหานคร")) {
				districtPrefix = "แขวง";
				amphurPrefix = "";
				provincePrefix = "";
			} else {
				districtPrefix = "ตำบล";
				amphurPrefix = "อำเภอ";
				provincePrefix = "จังหวัด";
			}
			
			cell = new PdfPCell(new Phrase(districtPrefix + reg.getZipcode().getDistrict().getDistrictName(), font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(31);
			cell.setColspan(4);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase(amphurPrefix + reg.getZipcode().getDistrict().getAmphur().getAmphurName(), font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(31);
			cell.setColspan(4);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase(provincePrefix + reg.getZipcode().getDistrict().getProvince().getProvinceName(), font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(31);
			cell.setColspan(4);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase(reg.getZipcode().getZipcode(), font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(31);
			cell.setPaddingBottom(15);
			cell.setColspan(4);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("เบอร์โทรศัพท์ติดต่อ", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			cell.setColspan(4);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("เบอร์โทรศัพท์บ้าน", font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConTelNo() == null ? ":" : ": " + reg.getConTelNo(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("มือถือหลัก", font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConMobileNo1() == null ? ":" : ": " + reg.getConMobileNo1(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("มือ (สำรอง 1)", font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConMobileNo2() == null ? ":" : ": " + reg.getConMobileNo2(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("มือ (สำรอง 2)", font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			cell.setPaddingBottom(15);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConMobileNo3() == null ? ":" : ": " + reg.getConMobileNo3(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("อีเมล์", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConEmail() == null ? ":" : ": " + reg.getConEmail(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("Facebook", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConFacebook() == null ? ":" : ": " + reg.getConFacebook(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("Line ID", fontBold));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0);			
			cell.setBorderWidthBottom(0.7f);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
			cell.setPaddingBottom(15);
			table.addCell(cell);
			//-----: Column :-----
			cell = new PdfPCell(new Phrase(reg.getConLineId() == null ? ":" : ": " + reg.getConLineId(), font));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0.7f);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			cell.setPaddingBottom(15);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			
			return table;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}

	@Override
	public byte[] createPdf() throws Exception {
		ByteArrayOutputStream out = null;
		
		try {
			out = new ByteArrayOutputStream();
			
			document = new Document();
			document.setPageSize(PageSize.A4);
			document.setMargins(30, 30, 30, 0);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			
			//-----
			fontBold = new Font(baseFont, 14, Font.BOLD);
			font = new Font(baseFont, 14);
			
			//----- 
			LOG.debug("pageBorder");
			pageBorder(writer);
			LOG.debug("memberDetail");
			document.add(memberDetail());
			LOG.debug("registerDetail");
			document.add(registerDetail());
			
			document.close();
			
			return out.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(out != null) out.close();
		}
	}
	
	/*public static void main(String[] args) {
	
		FileOutputStream out = null;
		try {
			SptRegistrationEditCriteriaResp resp = new SptRegistrationEditCriteriaResp();
			
			SptRegistration registration = new SptRegistration("memberId", new SptMasterNamingDet("นาย", null), 
					"firstname", "lastname", "firstnameEng", "lastnameEng", "citizenId", 
					new Date(), "fingerId", new Date(), new Date(), "conTelNo", "conMobileNo1", 
					"conMobileNo2", "conMobileNo3", "conLineId", "conFacebook", "conEmail", "conAddress", 
					null, null, null, null, null, null, null, null, null);
			
			resp.setRegistration(registration);
			byte[] bytes = new RegistrationForm(resp).createPdf();
			out = new FileOutputStream("C:\\Users\\sarawuti\\Desktop\\test.pdf");
			out.write(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();				
			} catch (Exception e2) {
			}
		}
	}*/

}
