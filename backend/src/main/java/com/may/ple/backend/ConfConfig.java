package com.may.ple.backend;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.may.ple.backend.custom.PropertiesCustom;
import com.may.ple.backend.entity.Conf;
import com.may.ple.backend.repository.ConfRepository;

@Configuration
public class ConfConfig {
	private static final Logger LOG = Logger.getLogger(ConfConfig.class.getName());
	@Autowired
	private ConfRepository confRepository;
	
	@Bean
	public PropertiesCustom dbProp() throws Exception {
		try {
			LOG.info(":---------: Start load conf data :---------:");
			
			List<Conf> confs = confRepository.findAll();
			PropertiesCustom properties = new PropertiesCustom();
			for (Conf conf : confs) {
				properties.put(conf.getConfKey(), conf.getConfValue());
			}
			
			LOG.info(":---------: Finished load conf data :---------:");
			return properties;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}