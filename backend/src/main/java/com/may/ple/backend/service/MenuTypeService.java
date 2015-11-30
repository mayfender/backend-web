package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.repository.MenuTypeRepository;

@Service
public class MenuTypeService {
	private static final Logger LOG = Logger.getLogger(MenuTypeService.class.getName());
	private MenuTypeRepository menuTypeRepository;
	
	@Autowired
	public MenuTypeService(MenuTypeRepository menuTypeRepository) {
		this.menuTypeRepository = menuTypeRepository;
	}
	
	public List<MenuType> loadMenuType() {
		try {
			return menuTypeRepository.findByIsDeleted(false);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
