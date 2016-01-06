package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class OrderSubMenu implements Serializable {
	private static final long serialVersionUID = -567738520505915915L;
	@EmbeddedId
	private OrderSubMenuId id;
	
	protected OrderSubMenu() {}
	
	public OrderSubMenu(OrderSubMenuId id) {
		this.id = id;
	}
	public OrderMenu getOrderMenu() {
		return id.getOrderMenu();
	}
	public void setOrderMenu(OrderMenu orderMenu) {
		id.setOrderMenu(orderMenu);
	}
	public SubMenu getSubMenu() {
		return id.getSubMenu();
	}
	public void setSubMenu(SubMenu subMenu) {
		id.setSubMenu(subMenu);
	}
	
}
