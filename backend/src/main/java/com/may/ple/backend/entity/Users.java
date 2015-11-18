package com.may.ple.backend.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/* --------------: Use for native query
 * @NamedNativeQueries({ 
	@NamedNativeQuery (name = "User.findByStatus", 
				query = "select * from user where enabled = ?1", 
				resultClass = User.class)
})*/

@NamedQueries({ 
	@NamedQuery(name = "Users.findByStatus", 
			query = "select u from Users u where u.enabled = ?1 order by u.userName asc") 
})
@Entity
public class Users implements Serializable {
	private static final long serialVersionUID = 6644354613536505474L;
	/*@Id
	@TableGenerator(name="userId", pkColumnValue="users.id", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy=GenerationType.TABLE, generator="userId")*/
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@Column(name="username_show", nullable=false)
	private String userNameShow;
	@Column(name="username", nullable=false)
	private String userName;
	@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
	@JoinColumn(name="username", referencedColumnName="username")
	private List<Roles> roles;
	@Column(name="password", nullable=false)
	private String password;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdDateTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updatedDateTime;
	private int enabled;	
	
	protected Users() {}
	
	public Users(String userNameShow, String userName, String password, Date createdDateTime, Date updatedDateTime, int enabled, List<Roles> roles) {
		this.userNameShow = userNameShow;
		this.userName = userName;
		this.password = password;
		this.createdDateTime = createdDateTime;
		this.updatedDateTime = updatedDateTime;
		this.enabled = enabled;
		this.roles = roles;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@JsonIgnore
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}

	public List<Roles> getRoles() {
		return roles;
	}

	public void setRoles(List<Roles> roles) {
		this.roles = roles;
	}

	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public Date getCreatedDateTime() {
		return createdDateTime;
	}

	public void setCreatedDateTime(Date createdDateTime) {
		this.createdDateTime = createdDateTime;
	}

	public Date getUpdatedDateTime() {
		return updatedDateTime;
	}

	public void setUpdatedDateTime(Date updatedDateTime) {
		this.updatedDateTime = updatedDateTime;
	}

	public String getUserNameShow() {
		return userNameShow;
	}

	public void setUserNameShow(String userNameShow) {
		this.userNameShow = userNameShow;
	}
	
}
