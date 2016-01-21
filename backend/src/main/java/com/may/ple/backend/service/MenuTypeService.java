package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.MenuTypePersistCriteriaReq;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.MenuRepository;
import com.may.ple.backend.repository.MenuTypeRepository;

@Service
public class MenuTypeService {
	private static final Logger LOG = Logger.getLogger(MenuTypeService.class.getName());
	private MenuTypeRepository menuTypeRepository;
	private MenuRepository menuRepository;
	
	@Autowired
	public MenuTypeService(MenuTypeRepository menuTypeRepository, MenuRepository menuRepository) {
		this.menuTypeRepository = menuTypeRepository;
		this.menuRepository = menuRepository;
	}
	
	public List<MenuType> loadMenuType() throws Exception {
		
		try {
			List<MenuType> menuTypes = menuTypeRepository.findByParentId(null);
			
			for (MenuType menuType : menuTypes) {
				menuType.setChilds(menuTypeRepository.findByParentId(menuType.getId()));
			}
			
			return menuTypes;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public Long persistMenuType(MenuTypePersistCriteriaReq req) {
		try {
			MenuType menuType;
			
			if(req.getId() != null) {
				menuType = menuTypeRepository.findOne(req.getId());
				menuType.setName(req.getName());
				menuType.setIsEnabled(req.getIsEnabled());
			} else {
				menuType = new MenuType(req.getName(), req.getIsEnabled(), req.getParentId());
			}
			
			menuTypeRepository.save(menuType);
			return menuType.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteMenuType(Long id) throws Exception {
		try {
			MenuType menuType = menuTypeRepository.findOne(id);
			List<Menu> menus = menuRepository.findByMenuTypeNotDeleted(menuType);
			
			if(menus.size() > 0) {
				throw new CustomerException(5000, "Can not delete this MenuType because it still have relation to some MENU");
			}
			
			List<MenuType> childs = menuTypeRepository.findByParentId(menuType.getId());
			
			if(childs.size() > 0) {
				throw new CustomerException(5001, "Can not delete this MenuType because it still have childs");
			}
			
			menuTypeRepository.delete(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<MenuType> getMenuTypeChilds(Long menuTypeId) throws Exception {
		try {
			List<MenuType> menuTypeChilds = menuTypeRepository.findByParentId(menuTypeId);
			return menuTypeChilds;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
