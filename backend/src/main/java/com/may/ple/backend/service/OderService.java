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

import com.may.ple.backend.criteria.OrderSearchCriteriaResp;
import com.may.ple.backend.criteria.OrderUpdateCriteriaReq;
import com.may.ple.backend.entity.Customer;
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
	
	public OrderSearchCriteriaResp findOrderByCus(Long cusId) throws Exception {
		OrderSearchCriteriaResp resp = new OrderSearchCriteriaResp(); 
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
			Double totalPrice = new Double(0);
			Double price = new Double(0);
			int amount;
			boolean isCancel;
			
			while(rst.next()) {
				isCancel = rst.getBoolean("is_cancel");
				amount = rst.getInt("amount");
				price = rst.getDouble("price");
				
				if(!isCancel) {
					totalPrice += price * amount;					
				}
				
				menuType = new MenuType(rst.getString("menuTypeName"));
				menu = new Menu(rst.getString("menuName"), price, null, null, null, null, menuType, null);
				orderMenu = new OrderMenu(
						menu, 
						null, 
						null, 
						rst.getInt("status"), 
						amount,
						rst.getBoolean("is_take_home"), 
						isCancel,
						rst.getInt("order_round"),
						null,
						null
				);
				orderMenu.setId(rst.getLong("id"));
				
				orders.add(orderMenu);
			}
			
			resp.setOrders(orders);
			resp.setTotalPrice(totalPrice);
			
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
	
	public OrderSearchCriteriaResp searchOrder() throws Exception {
		OrderSearchCriteriaResp resp = new OrderSearchCriteriaResp();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select orm.id, orm.created_date_time, orm.status, orm.amount, orm.comment, orm.is_take_home, ");
			sql.append(" m.id as menu_id, m.name as menu_name, c.table_detail, c.ref ");
			sql.append(" from order_menu orm join menu m on orm.menu_id = m.id ");
			sql.append(" join customer c on orm.cus_id = c.id ");
			sql.append(" where orm.is_cancel = false and c.status = 1 and orm.created_date_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)");
			sql.append(" order by orm.created_date_time ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			
			List<OrderMenu> orderMenusStart = new ArrayList<>();
			List<OrderMenu> orderMenusDoing = new ArrayList<>();
			List<OrderMenu> orderMenusFinished = new ArrayList<>();
			OrderMenu orderMenu;
			Customer customer;
			Menu menu;
			int status;
			
			while(rst.next()) {
				status = rst.getInt("status");
				
				menu = new Menu(rst.getString("menu_name"), null, null, null, null, null, null, null);
				menu.setId(rst.getLong("menu_id"));
				
				customer = new Customer(rst.getString("ref"), rst.getString("table_detail"), null, null, null, null, null, null);
				
				orderMenu = new OrderMenu(menu, rst.getTimestamp("created_date_time"), null, 
										  status, rst.getInt("amount"), rst.getBoolean("is_take_home"), 
										  null, null, rst.getString("comment"), customer);
				orderMenu.setId(rst.getLong("id"));
				
				if(status == 0) {
					orderMenusStart.add(orderMenu);
				} else if(status == 1) {
					orderMenusDoing.add(orderMenu);
				} else if(status == 2) {
					orderMenusFinished.add(orderMenu);					
				}
			}
			
			resp.setOrdersStart(orderMenusStart);
			resp.setOrdersDoing(orderMenusDoing);
			resp.setOrdersFinished(orderMenusFinished);
			
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
	
	public void setCancel(Long id, boolean val) {
		try {
			OrderMenu orderMenu = orderRepository.findOne(id);
			orderMenu.setIsCancel(val);
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
	
	public void changeStatus(List<String> ids, Integer status) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			String orderIds = "";
			for (String id : ids) orderIds += "," + id;
			
			StringBuilder sql = new StringBuilder();
			sql.append(" update order_menu set status = ?, updated_date_time = NOW() ");
			sql.append(" where id in ( " + orderIds.substring(1) + " ) ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setInt(1, status);
			
			pstmt.executeUpdate();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}

}
