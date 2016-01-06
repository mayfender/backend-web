package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class OrderSubMenuId implements Serializable {
	private static final long serialVersionUID = -5843568420273978713L;
	private OrderMenu orderMenu;
	private SubMenu subMenu;
	
	public OrderMenu getOrderMenu() {
		return orderMenu;
	}
	public void setOrderMenu(OrderMenu orderMenu) {
		this.orderMenu = orderMenu;
	}
	public SubMenu getSubMenu() {
		return subMenu;
	}
	public void setSubMenu(SubMenu subMenu) {
		this.subMenu = subMenu;
	}
	
}
