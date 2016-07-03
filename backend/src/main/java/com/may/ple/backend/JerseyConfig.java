package com.may.ple.backend;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.may.ple.backend.action.ImportMenuAction;
import com.may.ple.backend.action.ImportOthersAction;
import com.may.ple.backend.action.ImportOthersDetailAction;
import com.may.ple.backend.action.NewTaskAction;
import com.may.ple.backend.action.ProductAction;
import com.may.ple.backend.action.TaskDetailAction;
import com.may.ple.backend.action.UserAction;

@Component
@ApplicationPath(value="/restAct")
public class JerseyConfig extends ResourceConfig {
	private static final Logger LOG = Logger.getLogger(JerseyConfig.class.getName());
	
	public JerseyConfig() {
		LOG.info(":----------: Register Rest Service :----------:");
		register(MultiPartFeature.class);
		register(new ObjectMapperContextResolver());
		register(UserAction.class);
		register(NewTaskAction.class);
		register(ProductAction.class);
		register(ImportMenuAction.class);
		register(TaskDetailAction.class);
		register(ImportOthersAction.class);
		register(ImportOthersDetailAction.class);
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
