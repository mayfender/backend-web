package com.may.ple.backend.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Database {
	private String dbName;
	private String host;
	private Integer port;
	private String userName;
	private String password;
	
	public Database(){}
	
	public Database(String dbName, String host, Integer port, String userName, String password) {
		this.dbName = dbName;
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Database)) {
			return false;
		}
		
		Database db = (Database)obj;
		
		if(this.getDbName().equals(db.getDbName()) &&
		   this.getHost().equals(db.getHost()) &&
		   this.getPort().equals(db.getPort()) &&
		   this.getUserName().equals(db.getUserName()) &&
	       this.getPassword().equals(db.getPassword())) {
			
			return true;
		} else {
			return false;
		}
	}
	
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
}
