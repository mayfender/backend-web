package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.TableLandPersistCriteriaReq;
import com.may.ple.backend.entity.TableLand;
import com.may.ple.backend.repository.TableLandRepository;

@Service
public class TableLandService {
	private static final Logger LOG = Logger.getLogger(TableLandService.class.getName());
	private TableLandRepository tableRepository;
	
	@Autowired
	public TableLandService(TableLandRepository tableRepository) {
		this.tableRepository = tableRepository;
	}
	
	public List<TableLand> loadTableLand() {
		try {
			return tableRepository.findByIsDeleted(false);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Long persistTable(TableLandPersistCriteriaReq req) {
		try {
			TableLand tableLand;
			
			if(req.getId() != null) {
				tableLand = tableRepository.findOne(req.getId());
				tableLand.setName(req.getName());
			} else {
				Date date = new Date();
				tableLand = new TableLand(req.getName(), date, date, false);
			}
			
			tableRepository.save(tableLand);
			return tableLand.getId();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteMenuType(Long id) throws Exception {
		try {
//			TableLand tableLand = tableRepository.findOne(id);
			/*List<Menu> menus = tableRepository.findByMenuType(tableLand);
			
			if(menus.size() > 0) {
				throw new CustomerException(5000, "Can not delete this MenuType because it still have relation to some MENU");
			}*/
			
			tableRepository.delete(id);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
