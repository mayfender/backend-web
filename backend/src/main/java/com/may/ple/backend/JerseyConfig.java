package com.may.ple.backend;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.may.ple.backend.action.MenuAction;
import com.may.ple.backend.action.UserAction;

@Component
@ApplicationPath(value="/restAct")
public class JerseyConfig extends ResourceConfig {
	private static final Logger LOG = Logger.getLogger(JerseyConfig.class.getName());
	
	public JerseyConfig() {
		LOG.info(":----------: Register Rest Service :----------:");
		register(new ObjectMapperContextResolver());
		register(UserAction.class);
		register(MenuAction.class);		
	}

}

class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;

    public ObjectMapperContextResolver() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
    
}
