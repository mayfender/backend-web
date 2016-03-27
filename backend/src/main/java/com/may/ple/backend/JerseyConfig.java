package com.may.ple.backend;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.may.ple.backend.action.MasterNamingAction;
import com.may.ple.backend.action.SptImportFingerFileAction;
import com.may.ple.backend.action.SptMemberTypeAction;
import com.may.ple.backend.action.SptRegistrationAction;
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
		register(MasterNamingAction.class);
		register(SptMemberTypeAction.class);
		register(SptRegistrationAction.class);
		register(SptImportFingerFileAction.class);
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
