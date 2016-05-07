package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.ExportTypeConstant;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.service.RegistrationReportService;
import com.may.ple.backend.service.SptRegistrationReceiptService;

@Component
@Path("fileServer")
public class FileServerAction {
	private static final Logger LOG = Logger.getLogger(FileServerAction.class.getName());
	private SptRegistrationReceiptService service;
	private RegistrationReportService registrationReport;
	
	@Autowired
	public FileServerAction(SptRegistrationReceiptService service, RegistrationReportService registrationReport) {
		this.service = service;
		this.registrationReport = registrationReport;
	}
	
	@GET
	@Path("/getFileById")
	public Response getFileById(@QueryParam("id") final Long id, @QueryParam("type") final Integer type) throws Exception {
		try {
			LOG.debug("ID: " + id + ", TYPE: " + type);
			
			ResponseBuilder response = Response.ok(getStream(id, type, null));
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	@POST
	@Path("/getFileByRegister")
	public Response getFile(SptRegisteredFindCriteriaReq req) throws Exception {
		try {
			LOG.debug(req);
			
			ResponseBuilder response = Response.ok(getStream(null, req.getReportType(), req));
			return response.build();
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}
	
	private StreamingOutput getStream(final Long id, final Integer type, final Object criteria) {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream os) throws IOException, WebApplicationException {
				OutputStream out = null;
				ByteArrayInputStream in = null;
				
				try {
					LOG.debug("Start");
					
					ExportTypeConstant typeConstant = ExportTypeConstant.findById(type);
					byte[] data = null;
					
					switch (typeConstant) {
					case RECEIPT:  data = service.proceed(id); break;
					case REGISTER: data = registrationReport.proceed((SptRegisteredFindCriteriaReq)criteria);
					default: break;
					}
					
					LOG.debug("Got byte");
					
					in = new ByteArrayInputStream(data);
					out = new BufferedOutputStream(os);
					int bytes;
					
					while ((bytes = in.read()) != -1) {
						out.write(bytes);
					}
					
					LOG.debug("End");
				} catch (Exception e) {
					LOG.error(e.toString());
				} finally {
					if(in != null) in.close();			
					if(out != null) out.close();			
				}	
			}
		};
	}

}
