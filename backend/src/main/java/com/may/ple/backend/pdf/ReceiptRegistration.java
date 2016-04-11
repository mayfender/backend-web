package com.may.ple.backend.pdf;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class ReceiptRegistration extends BaseReportBuilder {
	private static final Logger LOG = Logger.getLogger(ReceiptRegistration.class.getName());
	private Document document;
	private Font fontBoldLabel;
	private Font fontBold;
	private Font font;
	
	private Image createLogo() throws Exception {
		try {
			URL urlLogo = getClass().getClassLoader().getResource("spt_logo.jpg");
			Image logo = Image.getInstance(urlLogo.getFile());
			logo.scaleToFit(150, 60);
			logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 65f);			
			return logo;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private Image createHeader() throws Exception {
		try {
			URL urlLogo = getClass().getClassLoader().getResource("spt_logo.jpg");
			Image logo = Image.getInstance(urlLogo.getFile());
			logo.scaleToFit(150, 60);
			logo.setAbsolutePosition(30, document.getPageSize().getHeight() - 65f);			
			return logo;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private PdfPTable createPart_1() throws Exception {
		try {
			StringBuilder msg_1 = new StringBuilder();
			msg_1.append("บริษัท ซุปเปอร์เทรดเดอร์ รีพับบลิค จำกัด (สำนักงานใหญ่)\n");
			msg_1.append("เลขที่ 1 อาคารเอ็มไพร์ทาวเวอร์ ชั้นที่ 19 ห้อง 1907/2-1908\n");
			msg_1.append("ถนนสาทรใต้ แขวงยานาวา เขตสาทร กรุงเทพมหานคร 10120\n");
			msg_1.append("เลขประจำตัวผู้เสียภาษี: 0-1055-59016-90-9\n");
			msg_1.append("Tel: 062-642-9241\n");
			msg_1.append("e-mail: SuperTraderRepublic@gmail.com\n");
			
			StringBuilder msg_2 = new StringBuilder();
			msg_2.append("1345/54 MB Grand ถ.พหลโยธิน\n");
			msg_2.append("แขวงสามเสนใน, เขตพญาไท, กรุงเทพมหานคร\n");
			msg_2.append("10400 Thailand\n");
			msg_2.append("เบอร์โทรศัพท์: 0853373173\n");
			msg_2.append("aehmsit11@gmail.com\n");
			
			StringBuilder msg_3 = new StringBuilder();
			msg_3.append("หมายเลขใบเสร็จรับเงิน: SPTP-R0000440\n");
			msg_3.append("วันที่ออกใบเสร็จรับเงิน: 4 เม.ย. 59\n");
			msg_3.append("วิธีชำระค่า");
			
			Paragraph info = new Paragraph("ข้อมูล\n", fontBold);
			info.setSpacingAfter(5);
			info.setSpacingBefore(3);
			
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setWidths(new int[]{60, 40});
			
			PdfPCell cell = new PdfPCell();
			cell.setBorderWidth(0);
			table.addCell(cell);
			
			cell = new PdfPCell(new Phrase("ใบเสร็จรับเงิน", fontBoldLabel));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
			table.addCell(cell);
			
			//--------------------------------------------------------------------------------------------------
			
			cell = new PdfPCell();
			cell.setBorderWidth(0);
			cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
			
			cell.addElement(new Phrase(20, msg_1.toString(), font));
			
			table.addCell(cell);
			
			cell = new PdfPCell();
			cell.setBorderWidth(0);
			Paragraph paragraph = new Paragraph();
			paragraph.add(new Paragraph(13, "พัชกร นามเสถียร", fontBold));
			paragraph.add(new Paragraph(13, msg_2.toString(), font));
			
			cell.addElement(paragraph);
			cell.addElement(info);
			
			paragraph = new Paragraph(13, msg_3.toString(), font);
			paragraph.add(new Chunk(" เงินสด", fontBold));
			
			cell.addElement(paragraph);
			
			table.addCell(cell);
			
			return table;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private PdfPTable createPart_2() throws Exception {
		try {
			PdfPTable table = new PdfPTable(4);
			table.setSpacingBefore(5);
			table.setWidthPercentage(100);
			table.setWidths(new int[]{40, 20, 20, 20});
			
			PdfPCell cell = new PdfPCell(new Paragraph("รายละเอียด", fontBold));
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setBorderWidthTop(0.1f);
			cell.setBorderWidthBottom(0.2f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph("จำนวน", fontBold));
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setBorderWidthTop(0.1f);
			cell.setBorderWidthBottom(0.2f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph("ราคาต่อหน่วย", fontBold));
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setBorderWidthTop(0.1f);
			cell.setBorderWidthBottom(0.2f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph("ราคารวม", fontBold));
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setBorderWidthTop(0.1f);
			cell.setBorderWidthBottom(0.2f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			//----------------------------------------------------------------------------------------
			
			cell = new PdfPCell(new Paragraph("ค่าลงทะเบียนสมัครสมาชิก", fontBold));
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setBorderWidthBottom(0.1f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph("1", font));
			cell.setBorderWidthBottom(0.1f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph("350.00", font));
			cell.setBorderWidthBottom(0.1f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph("350.00", font));
			cell.setBorderWidthBottom(0.1f);
			cell.setBorderWidthLeft(0);
			cell.setBorderWidthRight(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setPaddingTop(5);
			cell.setPaddingBottom(5);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			return table;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private PdfPTable createPart_3() throws Exception {
		try {
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setWidths(new int[]{67, 33});
			
			PdfPCell cell = new PdfPCell(new Paragraph(10, "ยอดสุทธิ", fontBold));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph(10, "350.00", font));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph(10, "ภาษีมูลค่าเพิ่ม 7%", fontBold));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph(10, "22.90", font));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph(10, "จำนวนเงินก่อนภาษีมูลค่าเพิ่ม", fontBold));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			cell = new PdfPCell(new Paragraph(10, "327.10", font));
			cell.setBorderWidth(0);
			cell.setUseAscender(true);
			cell.setUseDescender(true);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			
			table.addCell(cell);
			
			return table;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private Paragraph createSign() throws Exception {
		try {
			Paragraph sign = new Paragraph("ลงชื่อ...............................................................................................ผู้รับเงิน", fontBold);
			sign.setSpacingBefore(10);
			sign.setAlignment(Element.ALIGN_RIGHT);
			return sign;
		} catch (Exception e) {
			LOG.debug(e.toString());
			throw e;
		}
	}
	
	private Paragraph createFooter() throws Exception {
		try {
			StringBuilder msg = new StringBuilder();
			msg.append("ใบเสร็จรับเงินฉบับนี้จะสมบูรณ์ เมื่อบริษัท บริษัท ซุปเปอร์เทรดเดอร์ รีพับบลิค จำกัด ได้รับเงินเรียบร้อย\n");
			msg.append("This receipt is not valid unless duly signed by authorized signature and\n");
			msg.append("payment is received by Super Trader Republic Co., Ltd.");
			
			Paragraph footer = new Paragraph(20, msg.toString(), font);
			footer.setSpacingBefore(5);
			
			return footer;
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
			document.setPageSize(PageSize.A5.rotate());
			document.setMargins(30, 30, 10, 0);
			PdfWriter.getInstance(document, out);
			document.open();
			
			//-----
			fontBoldLabel = new Font(baseFont, 30, Font.BOLD);
			fontBold = new Font(baseFont, 14, Font.BOLD);
			font = new Font(baseFont, 14);
			
			//-----
			LOG.debug("Create Logo");
			document.add(createLogo());
			LOG.debug("Create Header");
			document.add(createHeader());
			LOG.debug("Create Part_1");
			document.add(createPart_1());
			LOG.debug("Create Part_2");
			document.add(createPart_2());
			LOG.debug("Create Part_3");
			document.add(createPart_3());
			LOG.debug("Create Sign");
			document.add(createSign());
			LOG.debug("Create Footer");
			document.add(createFooter());
			document.close();
			
			return out.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(out != null) out.close();
		}
	}

}
