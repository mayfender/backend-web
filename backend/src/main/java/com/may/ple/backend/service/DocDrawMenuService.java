package com.may.ple.backend.service;

import java.util.List;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.SubMenu;

public class DocDrawMenuService implements DrawMenu {

	@Override
	public void drawMenuType(XWPFTable table, String name) {
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
	public void drawSubMenuType(XWPFTable table, String name) {
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
	public void drawMenu(XWPFTable table, Menu menu, List<SubMenu> subMenus, int index) {
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

}
