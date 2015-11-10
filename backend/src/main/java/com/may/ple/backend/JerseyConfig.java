package com.may.ple.backend;

import javax.ws.rs.ApplicationPath;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.may.ple.backend.action.UserAction;

@Component
@ApplicationPath(value="/restAct")
public class JerseyConfig extends ResourceConfig {
	private static final Logger LOG = Logger.getLogger(JerseyConfig.class.getName());
	
	public JerseyConfig() {
		LOG.info(":----------: Register Rest Service :----------:");
		register(UserAction.class);
	}

}
