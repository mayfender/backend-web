package com.may.ple.backend.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.SubMenu;

@Service
public class DocDrawMenuService implements DrawMenu {
	private static final Logger LOG = Logger.getLogger(DocDrawMenuService.class.getName());
	@Value("${ext.template.menu.doc}")
	private String menuTemplateDocPath;
	private XWPFDocument document;
	private ByteArrayOutputStream out;
	private XWPFTable table;
	
	public void init() {
		try {
			out = new ByteArrayOutputStream();
			document = new XWPFDocument(new FileInputStream(menuTemplateDocPath)); 
			
			List<XWPFTable> tables = document.getTables();
			table = tables.get(0);
		} catch (Exception e) {
			LOG.error(e.toString());
		}
	}

	@Override
	public void drawMenuType(String name) {
		boolean isBold = true;
		int fontSize = 16;
		
		XWPFTableRow row = table.createRow();	
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphs().get(0);
		XWPFRun rh = para.createRun();
		
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(name);
		
		cell = row.createCell();
		cell.setText("");
		
		cell = row.createCell();
		cell.setText("");
	}

	@Override
	public void drawSubMenuType(String name) {
		boolean isBold = true;
		int fontSize = 16;
		
		XWPFTableRow row = table.createRow();	
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.CENTER);
		XWPFRun rh = para.createRun();
		
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(name);
		
		cell = row.createCell();
		cell.setText("");
		
		cell = row.createCell();
		cell.setText("");
	}

	@Override
	public void drawMenu(Menu menu, List<SubMenu> subMenus, int index) {
		boolean isBold = false;
		int fontSize = 14;
		String subMenuTxt = "";
		
		for (SubMenu subMenu : subMenus) {
			subMenuTxt += ", " + subMenu.getName() + " " + String.format("%,.2f-", subMenu.getPrice());
		}
		
		if(subMenuTxt.length() > 0) {
			subMenuTxt = subMenuTxt.substring(1);			
		}
		
		XWPFTableRow row = table.createRow();	
		XWPFTableCell cell = row.getCell(0);
		XWPFParagraph para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.LEFT);
		XWPFRun rh = para.createRun();
		
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(" " + (index + 1) + ". " + menu.getName() + "  " + subMenuTxt);
		
		cell = row.createCell();
		para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.CENTER);
		rh = para.createRun();
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText("");
		
		cell = row.createCell();
		para = cell.getParagraphs().get(0);
		para.setAlignment(ParagraphAlignment.RIGHT);
		rh = para.createRun();
		rh.setFontSize(fontSize);
		rh.setBold(isBold);
		rh.setFontFamily("Angsana New");
		rh.setText(String.format("%,.2f-", menu.getPrice()));
	}
	
	@Override
	public byte[] getByte() {
		byte[] result = null;
		
		try {
			
			document.write(out);
			result = out.toByteArray();
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try {if(document != null) document.close();} catch (Exception e2) {}
			try {if(out != null) out.close();} catch (Exception e2) {}
			
			out = null;
			document = null;
			table = null;
		}
		return result;
	}

}
