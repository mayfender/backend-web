package com.may.ple.backend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.constant.OrderStatusConstant;
import com.may.ple.backend.criteria.OrderSaveCriteriaReq;
import com.may.ple.backend.criteria.OrderSearchCriteriaResp;
import com.may.ple.backend.criteria.OrderUpdateCriteriaReq;
import com.may.ple.backend.entity.Customer;
import com.may.ple.backend.entity.Menu;
import com.may.ple.backend.entity.MenuType;
import com.may.ple.backend.entity.OrderMenu;
import com.may.ple.backend.entity.SubMenu;
import com.may.ple.backend.exception.CustomerException;
import com.may.ple.backend.repository.CustomerRepository;
import com.may.ple.backend.repository.MenuRepository;
import com.may.ple.backend.repository.OrderRepository;

@Service
public class OderService {
	private static final Logger LOG = Logger.getLogger(OderService.class.getName());
	private OrderRepository orderRepository;
	private MenuRepository menuRepository;
	private CustomerRepository customerRepository;
	private DataSource dataSource;
	private SimpMessagingTemplate template;
	
	@Autowired
	public OderService(OrderRepository orderRepository, MenuRepository menuRepository, 
			CustomerRepository customerRepository, DataSource dataSource, SimpMessagingTemplate template) {
		this.orderRepository = orderRepository;
		this.dataSource = dataSource;
		this.menuRepository = menuRepository;
		this.customerRepository = customerRepository;
		this.template = template;
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
			Map<String, Object> mapResult;
			MenuType menuType;
			Double totalPrice = new Double(0);
			Double price = new Double(0);
			Double subTotalPrice = new Double(0);
			int amount;
			boolean isCancel;
			
			while(rst.next()) {
				isCancel = rst.getBoolean("is_cancel");
				amount = rst.getInt("amount");
				price = rst.getDouble("price");
				
				if(!isCancel) {
					totalPrice += price * amount;					
				}
				
				menuType = new MenuType(rst.getString("menuTypeName"), null, null, null);
				menu = new Menu(rst.getString("menuName"), price, null, null, null, null, menuType, null, null);
				orderMenu = new OrderMenu(
						menu, 
						null, 
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
				
				mapResult = getSubMenu(conn, orderMenu.getId(), isCancel);
				orderMenu.setSubMenus((List<SubMenu>)mapResult.get("subMenus"));
				orders.add(orderMenu);
				
				subTotalPrice = (Double)mapResult.get("totalPrice");
				totalPrice += subTotalPrice;
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
			sql.append(" select orm.id, orm.created_date_time, orm.finished_changed_status_date_time, orm.status, orm.amount, orm.comment, orm.is_take_home, ");
			sql.append(" m.id as menu_id, m.name as menu_name, c.table_detail, c.ref, c.status as cus_status, mt.icon_color, mt.parent_id ");
			sql.append(" from order_menu orm join menu m on orm.menu_id = m.id ");
			sql.append(" join menu_type mt on m.menu_type_id = mt.id ");
			sql.append(" join customer c on orm.cus_id = c.id ");
//			sql.append(" where orm.is_cancel = false and m.status = 1 and c.created_date_time >= DATE_SUB(NOW(), INTERVAL 15 HOUR)");
			sql.append(" where orm.is_cancel = false and m.status = 1 and DATE(c.created_date_time) = CURDATE() ");
			sql.append(" order by orm.created_date_time ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			rst = pstmt.executeQuery();
			
			List<OrderMenu> orderMenusStart = new ArrayList<>();
			List<OrderMenu> orderMenusDoing = new ArrayList<>();
			List<OrderMenu> orderMenusFinished = new ArrayList<>();
			OrderMenu orderMenu;
			Map<String, Object> mapResult;
			int status;
			int cusStatus;
			
			while(rst.next()) {
				status = rst.getInt("status");
				cusStatus = rst.getInt("cus_status");
				
				if(cusStatus == 1) {
					orderMenu = getResultOrder(conn, rst, status);
					
					// Get Sub-Menu
					mapResult = getSubMenu(conn, orderMenu.getId(), true);
					orderMenu.setSubMenus((List<SubMenu>)mapResult.get("subMenus"));
					
					if(status == 0) {
						orderMenusStart.add(orderMenu);
					} else if(status == 1) {
						orderMenusDoing.add(orderMenu);
					} else if(status == 2) {
						orderMenusFinished.add(orderMenu);											
					}
				} else {
					if(status == 2) {
						orderMenu = getResultOrder(conn, rst, status);
						
						// Get Sub-Menu
						mapResult = getSubMenu(conn, orderMenu.getId(), true);
						orderMenu.setSubMenus((List<SubMenu>)mapResult.get("subMenus"));
						
						orderMenusFinished.add(orderMenu);					
					}					
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
	public void setCancelSub(Long id, Long orderId, boolean val) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" update order_sub_menu set is_cancel = ? ");
			sql.append(" where order_menu_id = ? and sub_menu_id = ? ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setBoolean(1, val);
			pstmt.setLong(2, orderId);
			pstmt.setLong(3, id);
			int update = pstmt.executeUpdate();
			if(update == 0) throw new CustomerException(4000, "Cann't update data");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
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
	
	public void updateSubAmount(OrderUpdateCriteriaReq req) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" update order_sub_menu set amount = ? ");
			sql.append(" where order_menu_id = ? and sub_menu_id = ? ");
			
			conn = dataSource.getConnection();
			pstmt = conn.prepareStatement(sql.toString());
			
			pstmt.setInt(1, req.getAmount());
			pstmt.setLong(2, req.getParentId());
			pstmt.setLong(3, req.getId());
			
			int update = pstmt.executeUpdate();
			if(update == 0) throw new CustomerException(4000, "Cann't update data");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	public void changeStatus(List<String> ids, Integer status) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			String orderIds = "";
			for (String id : ids) orderIds += "," + id;
			
			
			StringBuilder sql = new StringBuilder();
			sql.append(" update order_menu set status = ? ");
			
			OrderStatusConstant statusCon = OrderStatusConstant.findByStatusNo(status);
			if(statusCon == OrderStatusConstant.DOING) {
				sql.append(", doing_changed_status_date_time = NOW() ");
			} else if(statusCon == OrderStatusConstant.FINISHED) {
				sql.append(", finished_changed_status_date_time = NOW() ");				
			}
			
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
	
	@Transactional
	public OrderMenu saveOrder(OrderSaveCriteriaReq req) throws Exception {
		try {
			Date date = new Date();
			Long menuId = req.getMenuId();
			
			Menu menu = menuRepository.findOne(menuId);
			Customer customer = customerRepository.findByStatusAndTableDetailAndRef(1, req.getTableName(), req.getRef());
			
			if(customer == null) {
				customer = new Customer(req.getRef(), req.getTableName(), 1, date, date, null, null, null);
				customerRepository.save(customer);
				template.convertAndSend("/topic/newCus", customer);
			}
			
			OrderMenu orderMenu = new OrderMenu(menu, date, date, null, 0, req.getAmount(), req.getIsTakeHome(), false, null, req.getComment(), customer);			
			orderRepository.save(orderMenu);
			
			saveSubMenu(orderMenu.getId(), req.getSubMenus());
			
			return orderMenu;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private OrderMenu getResultOrder(Connection conn, ResultSet rst, int status) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst2 = null;
		
		try {
			
			String iconColor = "";
			long parentId = rst.getLong("parent_id");
			
			if(!rst.wasNull()) {
				LOG.debug("Get parent menuType");
				StringBuilder sql = new StringBuilder();
				sql.append(" select icon_color from menu_type where id = ? ");
				
				pstmt = conn.prepareStatement(sql.toString());
				
				pstmt.setLong(1, parentId);
				rst2 = pstmt.executeQuery();
				if(rst2.next()) {
					iconColor = rst2.getString("icon_color");					
				}
			} else {
				iconColor = rst.getString("icon_color");
			}
			
			MenuType menuType = new MenuType(null, null, null, iconColor);
			Menu menu = new Menu(rst.getString("menu_name"), null, null, null, null, null, menuType, null, null);
			menu.setId(rst.getLong("menu_id"));
			
			Customer customer = new Customer(rst.getString("ref"), rst.getString("table_detail"), null, null, null, null, null, null);
			
			OrderMenu orderMenu = new OrderMenu(menu, rst.getTimestamp("created_date_time"), null, rst.getTimestamp("finished_changed_status_date_time"),
									  status, rst.getInt("amount"), rst.getBoolean("is_take_home"), 
									  null, null, rst.getString("comment"), customer);
			orderMenu.setId(rst.getLong("id"));
			
			return orderMenu;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst2 != null) rst2.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}	
		}
	}
	
	private void saveSubMenu(Long orderMenuId, List<SubMenu> subMenus) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			StringBuilder sql = new StringBuilder();
			
			if(subMenus == null) return;
			
			conn = dataSource.getConnection();
			
			for (SubMenu subMenu : subMenus) {
				sql.delete(0, sql.length());
				sql.append(" insert into order_sub_menu(order_menu_id, sub_menu_id, amount) ");
				sql.append(" values(?, ?, ?) ");				
				
				pstmt = conn.prepareStatement(sql.toString());
				pstmt.setLong(1, orderMenuId);
				pstmt.setLong(2, subMenu.getId());
				pstmt.setInt(3, subMenu.getAmount());
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
			try { if(conn != null) conn.close(); } catch (Exception e2) {}
		}
	}
	
	private Map<String, Object> getSubMenu(Connection conn, Long orderMenuId, boolean isParentCancel) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		Map<String, Object> result = new HashMap<>();
		
		try {
			StringBuilder sql = new StringBuilder();
			sql.append(" select sm.id, sm.name, sm.price, ors.amount, ors.is_cancel ");
			sql.append(" from order_sub_menu ors ");
			sql.append(" join sub_menu sm on ors.sub_menu_id = sm.id ");
			sql.append(" where ors.order_menu_id = ? ");
			
			pstmt = conn.prepareStatement(sql.toString());
			pstmt.setLong(1, orderMenuId);
			
			rst = pstmt.executeQuery();
			SubMenu subMenu;
			Integer amount;
			List<SubMenu> subMenus = new ArrayList<>();
			boolean isCancel;
			Double totalPrice = new Double(0);
			Double price = new Double(0);
			
			while(rst.next()) {				
				amount = rst.getInt("amount");
				if(rst.wasNull()) {
					amount = null;
				}
				
				isCancel = rst.getBoolean("is_cancel");
				price = rst.getDouble("price");
				
				if(!isCancel && !isParentCancel) {
					totalPrice += price * (amount == null ? 1 : amount);					
				}
				
				subMenu = new SubMenu(rst.getString("name"), price, null, null);
				subMenu.setId(rst.getLong("id"));
				subMenu.setAmount(amount);
				subMenu.setIsCancel(isCancel);
				subMenus.add(subMenu);
			}
			
			result.put("subMenus", subMenus);
			result.put("totalPrice", totalPrice);
			
			return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			try { if(rst != null) rst.close(); } catch (Exception e2) {}
			try { if(pstmt != null) pstmt.close(); } catch (Exception e2) {}
		}
	}

}
