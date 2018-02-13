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
import com.may.ple.backend.action.AccessManagementAction;
import com.may.ple.backend.action.AddressAction;
import com.may.ple.backend.action.CodeAction;
import com.may.ple.backend.action.ContactAction;
import com.may.ple.backend.action.DashBoardAction;
import com.may.ple.backend.action.DymListAction;
import com.may.ple.backend.action.ForecastAction;
import com.may.ple.backend.action.ForecastResultReportAction;
import com.may.ple.backend.action.ImportMenuAction;
import com.may.ple.backend.action.ImportOthersAction;
import com.may.ple.backend.action.ImportOthersDetailAction;
import com.may.ple.backend.action.NewTaskAction;
import com.may.ple.backend.action.NoticeManagerAction;
import com.may.ple.backend.action.NoticeUploadAction;
import com.may.ple.backend.action.NoticeXDocUploadAction;
import com.may.ple.backend.action.PaymentDetailAction;
import com.may.ple.backend.action.PaymentOnlineCheckAction;
import com.may.ple.backend.action.PaymentReportAction;
import com.may.ple.backend.action.PaymentUploadAction;
import com.may.ple.backend.action.PluginAction;
import com.may.ple.backend.action.ProductAction;
import com.may.ple.backend.action.ProgramAction;
import com.may.ple.backend.action.ResultCodeGroupAction;
import com.may.ple.backend.action.SettingAction;
import com.may.ple.backend.action.TaskDetailAction;
import com.may.ple.backend.action.ThaiLandRegionAction;
import com.may.ple.backend.action.ToolsAction;
import com.may.ple.backend.action.TraceResultImportAction;
import com.may.ple.backend.action.TraceResultReportAction;
import com.may.ple.backend.action.TraceWorkAction;
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
		register(CodeAction.class);
		register(ToolsAction.class);		
		register(PluginAction.class);
		register(ContactAction.class);		
		register(AddressAction.class);
		register(NewTaskAction.class);
		register(ProductAction.class);
		register(SettingAction.class);
		register(DymListAction.class);		
		register(ProgramAction.class);
		register(ForecastAction.class);
		register(TraceWorkAction.class);
		register(DashBoardAction.class);
		register(ImportMenuAction.class);
		register(TaskDetailAction.class);
		register(ImportOthersAction.class);
		register(NoticeUploadAction.class);
		register(PaymentUploadAction.class);
		register(PaymentDetailAction.class);
		register(NoticeManagerAction.class);
		register(PaymentReportAction.class);
		register(ThaiLandRegionAction.class);
		register(ResultCodeGroupAction.class);
		register(AccessManagementAction.class);
		register(NoticeXDocUploadAction.class);
		register(TraceResultImportAction.class);
		register(TraceResultReportAction.class);
		register(ImportOthersDetailAction.class);		
		register(PaymentOnlineCheckAction.class);
		register(ForecastResultReportAction.class);
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
            public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return new ObjectId(jp.readValueAs(String.class));
            }
        };
    }
    
    private static JsonSerializer<Object> _idSerializer() {
        return new JsonSerializer<Object>() {
            public void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException, JsonProcessingException {
                jsonGenerator.writeString(obj == null ? null : obj.toString());
            }
        };
    }
    
}
