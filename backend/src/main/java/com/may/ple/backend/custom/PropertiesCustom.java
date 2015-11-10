package com.may.ple.backend.custom;

import java.util.Properties;

public class PropertiesCustom extends Properties {
	private static final long serialVersionUID = -7557897621110479768L;

	public Integer getInt(String key) {
		String val = getProperty(key);
		return Integer.parseInt(val);
	}
	
	public Long getLong(String key) {
		String val = getProperty(key);
		return Long.parseLong(val);
	}
	
	public Double getDouble(String key) {
		String val = getProperty(key);
		return Double.parseDouble(val);
	}
	
	public Boolean getBoolean(String key) {
		String val = getProperty(key);
		return Boolean.parseBoolean(val);
	}

}
