package com.may.ple.backend;

import javax.ws.rs.ApplicationPath;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.may.ple.backend.action.ReportAction;
import com.may.ple.backend.action.SettingAction;
import com.may.ple.backend.action.UserAction;
import com.may.ple.backend.action.VehicleAction;

@Component
@ApplicationPath(value="/restAct")
public class JerseyConfig extends ResourceConfig {
	private static final Logger LOG = Logger.getLogger(JerseyConfig.class.getName());
	
	public JerseyConfig() {
		LOG.info(":----------: Register Rest Service :----------:");
		register(UserAction.class).
		register(VehicleAction.class).
		register(SettingAction.class).
		register(ReportAction.class);
	}

}
