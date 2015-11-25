package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.GetImageCriteriaResp;
import com.may.ple.backend.criteria.MenuCriteriaReq;
import com.may.ple.backend.criteria.MenuCriteriaResp;
import com.may.ple.backend.entity.Image;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.repository.MenuRepository;

@Service
public class MenuService {
	private static final Logger LOG = Logger.getLogger(MenuService.class.getName());
	private MenuRepository menuRepository;
	private DataSource dataSource;
	
	@Autowired
	public MenuService(MenuRepository menuRepository, DataSource dataSource) {
		this.menuRepository = menuRepository;
		this.dataSource = dataSource;
	}
	
	public MenuCriteriaResp searchMenu(MenuCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		MenuCriteriaResp resp = new MenuCriteriaResp();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select m.id, m.name, m.image_id, m.status, m.price, mt.name as type_name ");
			sql.append(" from menu m join menu_type mt on m.menu_type_id = mt.id ");
			sql.append(" where 1=1 ");
			
			if(req != null) {
				if(!StringUtils.isBlank(req.getName())) {
					sql.append(" and m.name like '%" + req.getName() + "%' ");
				}
				if(req.getStatus() != null) {
					sql.append(" and m.status = " + req.getStatus() + " ");
				}
			}
			
			try {			
				StringBuilder sqlCount = new StringBuilder();
				sqlCount.append("select count(id) as size from ( " + sql.toString()+ " ) sub");
				
				conn = dataSource.getConnection();
				pstmt = conn.prepareStatement(sqlCount.toString());
				rst = pstmt.executeQuery();
				
				if(rst.next()) {
					resp.setTotalItems(rst.getLong("size"));
				}
			} catch (Exception e) {
				LOG.error(e.toString(), e);
				throw e;
			} finally {
				try { if(rst != null) rst.close(); } catch (Exception e2) {}
				try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			}
			
			sql.append(" order by name ");
			sql.append(" limit " + (req.getCurrentPage() - 1) * req.getItemsPerPage() + ", " + req.getItemsPerPage());
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<Menu> menus = new ArrayList<Menu>();
			Menu menu;
			
			while(rst.next()) {
				
				menu = new Menu(rst.getString("name"), rst.getInt("price"), 
								rst.getInt("status"), null, null, null, 
								new MenuType(rst.getString("type_name")));
				menu.setId(rst.getLong("id"));
				
				rst.getLong("image_id");
				menu.setHasImg(!rst.wasNull());
				
				menus.add(menu);
			}
			
			resp.setMenus(menus);
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	@Transactional
	public GetImageCriteriaResp getImage(long id) {
		try {
			GetImageCriteriaResp resp = new GetImageCriteriaResp();
			
			Menu menu = menuRepository.getOne(id);
			Image image = menu.getImage();
			
			resp.setImgBase64(new String(Base64.encode(image.getImageContent())));
			resp.setImgName(image.getImageName());
			resp.setImgType(image.getImageType().getTypeName());
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
