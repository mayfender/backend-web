package com.may.ple.backend.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SubMenuPersistCriteriaReq;
import com.may.ple.backend.entity.SubMenu;
import com.may.ple.backend.repository.SubMenuRepository;

@Service
public class SubMenuService {
	private static final Logger LOG = Logger.getLogger(SubMenuService.class.getName());
	private SubMenuRepository subMenuRepository;
	
	@Autowired
	public SubMenuService(SubMenuRepository subMenuRepository) {
		this.subMenuRepository = subMenuRepository;
	}
	
	public List<SubMenu> findByMenuId(Long menuId) {
		try {
			return subMenuRepository.findByMenuId(menuId);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Long persistSubMenu(SubMenuPersistCriteriaReq req) {
		try {
			SubMenu subMenu;
			
			if(req.getId() != null) {
				subMenu = subMenuRepository.findOne(req.getId());
				subMenu.setName(req.getName());
				subMenu.setPrice(req.getPrice());
				subMenu.setAmountFlag(req.getAmountFlag());
			} else {
				subMenu = new SubMenu(req.getName(), req.getPrice(), req.getMenuId(), req.getAmountFlag());
			}
			
			subMenuRepository.save(subMenu);
			return subMenu.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteSubMenu(Long id) throws Exception {
		try {
			subMenuRepository.delete(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
