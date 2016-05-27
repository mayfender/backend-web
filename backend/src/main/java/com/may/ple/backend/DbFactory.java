package com.may.ple.backend;

import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;

public class DbFactory {
	private Map<String, MongoTemplate> templates;

	public Map<String, MongoTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Map<String, MongoTemplate> templates) {
		this.templates = templates;
	}

}
