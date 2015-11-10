package com.may.ple.backend.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Conf implements Serializable {
	private static final long serialVersionUID = -4869544725778164273L;
	@Id
	private String confKey;
	private String confValue;
	
	protected Conf(){}
	
	public Conf(String confKey, String confValue) {
		this.confKey = confKey;
		this.confValue = confValue;
	}

	public String getConfKey() {
		return confKey;
	}

	public void setConfKey(String confKey) {
		this.confKey = confKey;
	}

	public String getConfValue() {
		return confValue;
	}

	public void setConfValue(String confValue) {
		this.confValue = confValue;
	}
	
}
