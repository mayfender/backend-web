package com.may.ple.backend.service;

import java.util.List;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.SubMenu;

public interface DrawMenu {
	
	void init();
	
	void drawMenuType(String name);
	
	void drawSubMenuType(String name);
	
	void drawMenu(Menu menu, List<SubMenu> subMenus, int index);
	
	byte[] getByte();
	
}