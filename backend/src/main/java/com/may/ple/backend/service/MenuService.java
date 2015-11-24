package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.repository.MenuRepository;

@Service
public class MenuService {
	private static final Logger LOG = Logger.getLogger(MenuService.class.getName());
	private MenuRepository menuRepository;
	
	@Autowired
	public MenuService(MenuRepository menuRepository) {
		this.menuRepository = menuRepository;
	}
	
	public List<Menu> loadAllMenu() {
		try {
			List<Menu> menus = menuRepository.findAll();
			
			for (Menu menu : menus) {
				menu.setPicBase64(new String(Base64.encode(menu.getPic())));
			}
			
			return menus;		
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
