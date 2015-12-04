package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.OrderSearchCriteriaReq;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.entity.OrderMenu;
import com.may.ple.backend.repository.OrderRepository;

@Service
public class OderService {
	private static final Logger LOG = Logger.getLogger(OderService.class.getName());
	private OrderRepository orderRepository;
	private DataSource dataSource;
	
	@Autowired
	public OderService(OrderRepository orderRepository, DataSource dataSource) {
		this.orderRepository = orderRepository;
		this.dataSource = dataSource;
	}
	
	public List<OrderMenu> searchOrder(OrderSearchCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		List<OrderMenu> orders = new ArrayList<OrderMenu>();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select m.name as menuName, m.price, mt.name as menuTypeName, o.status, o.created_date_time, o.updated_date_time ");
			sql.append(" from order_menu o ");
			sql.append(" join menu m on o.menu_id = m.id ");
			sql.append(" join menu_type mt on m.menu_type_id = mt.id ");
			sql.append(" where 1=1 ");
			
			if(req != null) {
				if(req.getCusId() != null) {
					sql.append(" and o.cus_id = " + req.getCusId() + " ");
				}
			}
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			OrderMenu orderMenu;
			Menu menu;
			MenuType menuType;
			
			while(rst.next()) {
				menuType = new MenuType(rst.getString("menuTypeName"));
				menu = new Menu(rst.getString("menuName"), rst.getInt("price"), null, null, null, null, menuType, null);
				orderMenu = new OrderMenu(menu, rst.getTimestamp("created_date_time"), rst.getTimestamp("updated_date_time"), rst.getInt("status"));
				
				orders.add(orderMenu);
			}
			
			return orders;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public List<OrderMenu> findOrderByCus(Long cusId) {
		try {
			return orderRepository.findByCusId(cusId);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
