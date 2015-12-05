package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.OrderUpdateCriteriaReq;
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
	
	public List<OrderMenu> findOrderByCus(Long cusId) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		List<OrderMenu> orders = new ArrayList<OrderMenu>();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select o.id, m.name as menuName, m.price, mt.name as menuTypeName, ");
			sql.append(" o.status, o.amount, o.is_take_home, o.order_round, o.is_cancel ");
			sql.append(" from order_menu o ");
			sql.append(" join menu m on o.menu_id = m.id ");
			sql.append(" join menu_type mt on m.menu_type_id = mt.id ");
			sql.append(" where o.cus_id = ? ");
			sql.append(" order by created_date_time, m.name ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setLong(1, cusId);
			
			rst = pstmt.executeQuery();
			OrderMenu orderMenu;
			Menu menu;
			MenuType menuType;
			
			while(rst.next()) {
				menuType = new MenuType(rst.getString("menuTypeName"));
				menu = new Menu(rst.getString("menuName"), rst.getInt("price"), null, null, null, null, menuType, null);
				orderMenu = new OrderMenu(
						menu, 
						null, 
						null, 
						rst.getInt("status"), 
						rst.getInt("amount"),
						rst.getBoolean("is_take_home"), 
						rst.getBoolean("is_cancel"),
						rst.getInt("order_round")
				);
				orderMenu.setId(rst.getLong("id"));
				
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
	
	public void cancelByOrderId(Long id) {
		try {
			OrderMenu orderMenu = orderRepository.findOne(id);
			orderMenu.setIsCancel(true);
			orderMenu.setUpdatedDateTime(new Date());
			
			orderRepository.save(orderMenu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public void updateOrder(OrderUpdateCriteriaReq req) {
		try {
			OrderMenu orderMenu = orderRepository.findOne(req.getId());
			orderMenu.setAmount(req.getAmount());
			orderMenu.setUpdatedDateTime(new Date());
			
			orderRepository.save(orderMenu);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
