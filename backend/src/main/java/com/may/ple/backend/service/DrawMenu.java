package com.may.ple.backend.service;

import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFTable;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.SubMenu;

public interface DrawMenu {
	
	void drawMenuType(XWPFTable table, String name);
	
	void drawSubMenuType(XWPFTable table, String name);
	
	void drawMenu(XWPFTable table, Menu menu, List<SubMenu> subMenus, int index);
	
}