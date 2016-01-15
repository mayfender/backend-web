package com.may.ple.backend.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.GetImageCriteriaResp;
import com.may.ple.backend.criteria.MenuSaveCriteriaReq;
import com.may.ple.backend.criteria.MenuSearchCriteriaReq;
import com.may.ple.backend.criteria.MenuSearchCriteriaResp;
import com.may.ple.backend.entity.Image;
import com.may.ple.backend.entity.ImageType;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.repository.ImageRepository;
import com.may.ple.backend.repository.ImageTypeRepository;
import com.may.ple.backend.repository.MenuRepository;
import com.may.ple.backend.repository.MenuTypeRepository;

@Service
public class MenuService {
	private static final Logger LOG = Logger.getLogger(MenuService.class.getName());
	private MenuRepository menuRepository;
	private DataSource dataSource;
	private ImageRepository imageRepository;
	private MenuTypeRepository menuTypeRepository;
	private ImageTypeRepository imageTypeRepository;
	
	@Autowired
	public MenuService(
			MenuRepository menuRepository, 
			MenuTypeRepository menuTypeRepository,
			ImageRepository imageRepository, 
			ImageTypeRepository imageTypeRepository, 
			DataSource dataSource
			) {
		
		this.menuRepository = menuRepository;
		this.dataSource = dataSource;
		this.imageRepository = imageRepository;
		this.menuTypeRepository = menuTypeRepository;
		this.imageTypeRepository = imageTypeRepository;
	}
	
	public MenuSearchCriteriaResp searchMenu(MenuSearchCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		MenuSearchCriteriaResp resp = new MenuSearchCriteriaResp();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select m.id, m.name, m.image_id, m.status, m.price, m.is_recommented, m.menu_detail_html, mt.name as type_name, mt.parent_id, mt.id as type_id ");
			sql.append(" from menu m join menu_type mt on m.menu_type_id = mt.id ");
			sql.append(" where m.status != 2 ");
			
			if(req != null) {
				if(!StringUtils.isBlank(req.getName())) {
					sql.append(" and m.name like '%" + req.getName() + "%' ");
				}
				if(req.getStatus() != null) {
					sql.append(" and m.status = " + req.getStatus() + " ");
				}
				if(req.getIsRecommented() != null && req.getIsRecommented()) {
					sql.append(" and m.is_recommented = " + req.getIsRecommented() + " ");
				}
				if(req.getMenuTypeId() != null) {
					sql.append(" and (m.menu_type_id = " + req.getMenuTypeId() + " or mt.parent_id = " + req.getMenuTypeId() + ") ");
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
			
			sql.append(" order by m.menu_type_id, m.name ");
			sql.append(" limit " + (req.getCurrentPage() - 1) * req.getItemsPerPage() + ", " + req.getItemsPerPage());
			
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			List<Menu> menus = new ArrayList<Menu>();
			MenuType menuType;
			Menu menu;
			Double price;
			Long imageId;
			Image image;
			Long menuTypeId;
			Long parentId;
			Long menuTypeChildId = null;
			boolean wasNull;
			
			while(rst.next()) {
				parentId = rst.getLong("parent_id");
				wasNull = rst.wasNull();
				menuTypeId = rst.getLong("type_id");
				
				if(!wasNull) {
					//-- This is child
					menuTypeChildId = menuTypeId;
					menuTypeId = parentId;
				}
				
				menuType = new MenuType(rst.getString("type_name"), null, null);
				menuType.setId(menuTypeId);
				
				menu = new Menu(rst.getString("name"), null, 
								rst.getInt("status"), null, null, null, 
								menuType, rst.getBoolean("is_recommented"), rst.getString("menu_detail_html"));
				
				imageId = rst.getLong("image_id");
				image = new Image(null, null, null, null, null);
				image.setId(rst.wasNull() ? null : imageId);
				
				price = rst.getDouble("price");
				
				menu.setId(rst.getLong("id"));
				menu.setImage(image);
				menu.setPrice(rst.wasNull() ? null : price);
				menu.setMenuTypeChildId(menuTypeChildId);
				
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
	public void saveMenu(MenuSaveCriteriaReq req) {
		try {
			Date date = new Date();
			Image image = null;
			
			if(!StringUtils.isBlank(req.getImgName())) {
				byte[] imageContent = Base64.decode(req.getImgContent().getBytes());
				String imgNameAndType[] = req.getImgName().split("\\.");
				String imgName = imgNameAndType[0];
				String imgType = imgNameAndType[1];
				
				ImageType imageType = imageTypeRepository.findByTypeName(imgType.toUpperCase());
				image = new Image(imgName, imageContent, imageType, date, date);
			}
			
			MenuType menuType = menuTypeRepository.findOne(req.getMenuTypeId());
			
			Menu menu = new Menu(
					req.getName(), 
					req.getPrice(), 
					req.getStatus(), 
					date, date, 
					image, menuType, 
					req.getIsRecommented(),
					req.getMenuDetailHtml()
					);
			
			menuRepository.save(menu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	@Transactional
	public void updateMenu(MenuSaveCriteriaReq req) {
		try {
			Date date = new Date();
			Menu menu = menuRepository.findOne(req.getId());
			menu.setUpdatedDate(date);
			menu.setName(req.getName());
			menu.setPrice(req.getPrice());
			menu.setStatus(req.getStatus());
			menu.setIsRecommented(req.getIsRecommented());
			menu.setMenuType(menuTypeRepository.findOne(req.getMenuTypeId()));
			menu.setMenuDetailHtml(req.getMenuDetailHtml());
			
			if(!StringUtils.isBlank(req.getImgName())) {
				byte[] imageContent = Base64.decode(req.getImgContent().getBytes());
				String imgNameAndType[] = req.getImgName().split("\\.");
				String imgName = imgNameAndType[0];
				String imgType = imgNameAndType[1];

				ImageType imageType = imageTypeRepository.findByTypeName(imgType.toUpperCase());
				
				Image image = menu.getImage();
				if(image == null) {
					image = new Image(imgName, imageContent, imageType, date, date);
				} else {
					image.setImageName(imgName);
					image.setImageContent(imageContent);
					image.setImageType(imageType);
					image.setUpdatedDate(date);					
				}
				
				menu.setImage(image);
			} else {
				if(req.getIsChangedImg() != null && req.getIsChangedImg()) {
					Image image = menu.getImage();
					menu.setImage(null);
					
					if(image != null) {
						imageRepository.delete(image);						
					}
				}
			}
			
			menuRepository.save(menu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void deleteMenu(Long id) {
		try {
			Menu menu = menuRepository.findOne(id);
			menu.setStatus(2);
			
			menuRepository.save(menu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public GetImageCriteriaResp getImage(long id) {
		try {
			GetImageCriteriaResp resp = new GetImageCriteriaResp();
			
			Image image = imageRepository.findOne(id);
			
			resp.setImgBase64(new String(Base64.encode(image.getImageContent())));
			resp.setImgName(image.getImageName());
			resp.setImgType(image.getImageType().getTypeName());
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String getMenuDetailHtml(long id) {
		try {
			Menu menu = menuRepository.findOne(id);
			return menu.getMenuDetailHtml();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static void exportMenu() throws Exception {
		XWPFDocument document = null;
		FileOutputStream out = null;
		
		try {
			out = new FileOutputStream(new File("D:\\menu2.docx"));
			document = new XWPFDocument(new FileInputStream("D:\\menu.docx")); 
			
			List<XWPFTable> tables = document.getTables();
			XWPFTable table = tables.get(0);
			XWPFTableRow row;
			XWPFTableCell cell;
			
			for (int i = 0; i < 10; i++) {
				row = table.createRow();	
				
				cell = row.getCell(0);
				XWPFParagraph p = cell.getParagraphs().get(0);
				XWPFRun rh = p.createRun();
				rh.setFontSize(14);
				cell.setText(" 3. ชามะนาว");
				
				cell = row.createCell();
				cell.setText("BB");
				
				cell = row.createCell();
				cell.setText("CC");
			}
			
			document.write(out);
			
			System.out.println("Success");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(document != null) document.close();
			if(out != null) out.close();
		}
	}

	public static void main(String[] args) {
		try {
			exportMenu();			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
