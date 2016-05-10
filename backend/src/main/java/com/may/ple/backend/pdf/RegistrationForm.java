package com.may.ple.backend.pdf;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.security.crypto.codec.Base64;

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
import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.entity.SptRegistration;

public class RegistrationForm extends BaseReportBuilder {
	private static final Logger LOG = Logger.getLogger(RegistrationForm.class.getName());
	private Document document;
	private Font fontBoldLabel;
	private Font fontBold;
	private Font font;
	private SptRegistrationEditCriteriaResp resp;
	
	public RegistrationForm(SptRegistrationEditCriteriaResp resp) {
		this.resp = resp;
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
			String dateFormat = "%1$td/%1$tm/%1$tY";
			PdfPTable table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setWidths(new int[]{30, 40, 3, 27});
			
			SptRegistration reg = resp.getRegistration();
						
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
			cell = new PdfPCell(new Phrase(": " + reg.getMemberId(), fontBold));
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
			cell.setRowspan(5);
			cell.setPaddingLeft(10);
			table.addCell(cell);
			//-----: Column :-----
			
			Image logo = null;
			if(resp.getRegistration().getImgBase64() != null) {
				LOG.debug("Show Image");
				byte[] bytes = Base64.decode(resp.getRegistration().getImgBase64().getBytes());				
				logo = Image.getInstance(bytes);
			}
			
			cell = new PdfPCell(logo, true);
			cell.setFixedHeight(100);
			cell.setBorderWidth(0.7f);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			cell.setPadding(1);
			cell.setRowspan(5);
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
			cell = new PdfPCell(new Phrase(reg.getFingerId() == null ? ":" : ": " + reg.getFingerId(), fontBold));
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
			cell = new PdfPCell(new Phrase(": " + reg.getPrefixName().getDisplayValue()+ " " + reg.getFirstname() + " " + reg.getLastname(), fontBold));
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
				bthday = String.format(dateFormat, reg.getBirthday());
			}
			
			cell = new PdfPCell(new Phrase(": " + bthday, fontBold));
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
			cell = new PdfPCell(new Phrase(": " + reg.getCitizenId(), fontBold));
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
			table.setWidths(new int[]{25, 25, 25, 25});
						
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setColspan(3);
			table.addCell(cell);
			//--------------------------------: Row :------------------------------------
			cell = new PdfPCell(new Phrase("ที่อยู่", fontBold));
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
			cell = new PdfPCell(new Phrase("ตำบล/แขวง", font));
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
			cell = new PdfPCell(new Phrase("อำเภอ/เขต", font));
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
			cell = new PdfPCell(new Phrase("จังหวัด", font));
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
			cell = new PdfPCell(new Phrase("รหัสไปรษณีย์", font));
			cell.setBorderWidthLeft(0.7f);
			cell.setBorderWidthTop(0);
			cell.setBorderWidthRight(0.7f);			
			cell.setBorderWidthBottom(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingLeft(10);
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			cell = new PdfPCell(new Phrase(":", fontBold));
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
			fontBoldLabel = new Font(baseFont, 30, Font.BOLD);
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
			byte[] bytes = new RegistrationForm(null).createPdf();
			out = new FileOutputStream("C:\\Users\\mayfender\\Desktop\\test.pdf");
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
