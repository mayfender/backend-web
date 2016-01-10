package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Menu implements Serializable {
	private static final long serialVersionUID = -442486517351376074L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private String name;
	private Double price;
	private Integer status;
	private Boolean isRecommented;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDate;
	@OneToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST})
	@JoinColumn(name="image_id", referencedColumnName="id")
	private Image image;
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="menu_type_id", referencedColumnName="id")
	private MenuType menuType;
	private String menuDetailHtml;
    @OneToMany(mappedBy="menuOwer")
	private List<SubMenu> subMenus;
	
	protected Menu() {}
	
	public Menu(String name, Double price, Integer status, Date createdDate, Date updatedDate, 
			Image image, MenuType menuType, Boolean isRecommented, String menuDetailHtml) {
		this.name = name;
		this.price = price;
		this.status = status;
		this.createdDate = createdDate;
		this.updatedDate = updatedDate;
		this.image = image;
		this.menuType = menuType;
		this.isRecommented = isRecommented;
		this.menuDetailHtml = menuDetailHtml;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Image getImage() {
		return image;
	}
	public void setImage(Image image) {
		this.image = image;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public MenuType getMenuType() {
		return menuType;
	}
	public void setMenuType(MenuType menuType) {
		this.menuType = menuType;
	}
	public Boolean getIsRecommented() {
		return isRecommented;
	}
	public void setIsRecommented(Boolean isRecommented) {
		this.isRecommented = isRecommented;
	}
	public String getMenuDetailHtml() {
		return menuDetailHtml;
	}
	public void setMenuDetailHtml(String menuDetailHtml) {
		this.menuDetailHtml = menuDetailHtml;
	}
	public List<SubMenu> getSubMenus() {
		return subMenus;
	}
	public void setSubMenus(List<SubMenu> subMenus) {
		this.subMenus = subMenus;
	}
	
}
