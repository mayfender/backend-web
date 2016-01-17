package com.may.ple.backend.criteria;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.may.ple.backend.entity.SubMenu;

public class OrderSaveCriteriaReq {	
	private Long menuId;
	private String tableName;
	private String ref;
	private Integer amount;
	private String comment;
	private Boolean isTakeHome;
	private List<SubMenu> subMenus;
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getMenuId() {
		return menuId;
	}
	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getRef() {
		return ref;
	}
	public void setRef(String ref) {
		this.ref = ref;
	}
	public Integer getAmount() {
		return amount;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Boolean getIsTakeHome() {
		return isTakeHome;
	}
	public void setIsTakeHome(Boolean isTakeHome) {
		this.isTakeHome = isTakeHome;
	}
	public List<SubMenu> getSubMenus() {
		return subMenus;
	}
	public void setSubMenus(List<SubMenu> subMenus) {
		this.subMenus = subMenus;
	}
	
}
