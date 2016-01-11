package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Image;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.entity.SubMenu;

@Service
public class LoadDataService {
	private static final Logger LOG = Logger.getLogger(LoadDataService.class.getName());
	private DataSource dataSource;
	
	@Autowired
	public LoadDataService(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public Map<String, List<Menu>> loadMenu() throws Exception {
		Map<String, List<Menu>> menuMap = new HashMap<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select m.id, m.name, m.price, m.is_recommented, mt.name as type_name, i.image_content ");
			sql.append(" from menu m join menu_type mt on m.menu_type_id = mt.id ");
			sql.append(" left join image i on m.image_id = i.id ");
			sql.append(" where m.status = 1 ");
			sql.append(" order by mt.id, m.name ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			
			String menuTypeNameDummy = "";
			List<Menu> menus = null;
			List<SubMenu> subMenus;
			String menuTypeName;
			Image image = null;
			MenuType menuType;
			byte[] imgArr;
			Menu menu;
			
			while(rst.next()) {
				menuTypeName = rst.getString("type_name");
				
				if(!menuTypeName.equals(menuTypeNameDummy)) {
					menuTypeNameDummy = menuTypeName;
					
					menus = new ArrayList<Menu>();
					menuMap.put(menuTypeName, menus);
					
				}
				
				imgArr = rst.getBytes("image_content");
				
				menuType = new MenuType(menuTypeName);
				image = new Image(null, null, null, null, null);
				image.setImageContentBase64(imgArr == null ? null : new String(Base64.encode(imgArr)));
				
				menu = new Menu(rst.getString("name"), null, null, null, null, image, 
								menuType, rst.getBoolean("is_recommented"), null);
				
				menu.setId(rst.getLong("id"));
				menu.setPrice(rst.getDouble("price"));
				
				subMenus = getSubMenu(conn, menu.getId());
				menu.setSubMenus(subMenus);
				
				menus.add(menu);
			}
			
			return menuMap;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	
	private List<SubMenu> getSubMenu(Connection conn, Long menuId) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select id, name, price ");
			sql.append(" from sub_menu ");
			sql.append(" where menu_id = ? ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setLong(1, menuId);
			
			rst = pstmt.executeQuery();
			SubMenu subMenu;
			List<SubMenu> subMenus = new ArrayList<>();
			
			while(rst.next()) {				
				subMenu = new SubMenu(rst.getString("name"), rst.getDouble("price"), null, null);
				subMenu.setId(rst.getLong("id"));
				subMenus.add(subMenu);
			}
			
			return subMenus;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
		}
	}
	
}
