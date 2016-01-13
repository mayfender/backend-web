package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class MenuType implements Serializable {
	private static final long serialVersionUID = 768441182289412792L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Boolean isEnabled;
	private Long parentId;
	@Transient
	private List<MenuType> childs;
	
	protected MenuType() {}
	
	public MenuType(String name, Boolean isEnabled, Long parentId) {
		this.name = name;
		this.isEnabled = isEnabled;
		this.parentId = parentId;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Boolean getIsEnabled() {
		return isEnabled;
	}
	public void setIsEnabled(Boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public List<MenuType> getChilds() {
		return childs;
	}
	public void setChilds(List<MenuType> childs) {
		this.childs = childs;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

}
