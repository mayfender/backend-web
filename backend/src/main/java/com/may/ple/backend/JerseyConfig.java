package com.may.ple.backend;

import java.io.IOException;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.ext.ContextResolver;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.may.ple.backend.action.DMSAction;
import com.may.ple.backend.action.DealerAction;
import com.may.ple.backend.action.OrderAction;
import com.may.ple.backend.action.OrderGroupAction;
import com.may.ple.backend.action.ReceiverAction;
import com.may.ple.backend.action.SendRoundAction;
import com.may.ple.backend.action.UploadFileAction;
import com.may.ple.backend.action.UserAction;

@Component
@ApplicationPath(value="/restAct")
public class JerseyConfig extends ResourceConfig {
	private static final Logger LOG = Logger.getLogger(JerseyConfig.class.getName());

	public JerseyConfig() {
		LOG.info(":----------: Register Rest Service :----------:");
		register(MultiPartFeature.class);
		register(new ObjectMapperContextResolver());
		register(DMSAction.class);
		register(UserAction.class);
		register(OrderAction.class);
		register(DealerAction.class);
		register(ReceiverAction.class);
		register(SendRoundAction.class);
		register(UploadFileAction.class);
		register(OrderGroupAction.class);
	}

}

class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    private final ObjectMapper mapper;
    private static Class<ObjectId> _id = ObjectId.class;

    public ObjectMapperContextResolver() {
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);

        mapper.registerModule(new SimpleModule("jersey", new Version(1, 0, 0, null))
        .addSerializer(_id, _idSerializer())
        .addDeserializer(_id, _idDeserializer()));
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }

    private static JsonDeserializer<ObjectId> _idDeserializer() {
        return new JsonDeserializer<ObjectId>() {
            @Override
			public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return new ObjectId(jp.readValueAs(String.class));
            }
        };
    }

    private static JsonSerializer<Object> _idSerializer() {
        return new JsonSerializer<Object>() {
            @Override
			public void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(obj == null ? null : obj.toString());
            }
        };
    }

}
