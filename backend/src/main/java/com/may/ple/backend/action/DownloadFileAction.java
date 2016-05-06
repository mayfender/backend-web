package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.may.ple.backend.constant.ExportTypeConstant;
import com.may.ple.backend.excel.RegisterReport;

@Component
@Path("downloadFile")
public class DownloadFileAction {
	private static final Logger LOG = Logger.getLogger(DownloadFileAction.class.getName());
	@Value("${ext.template.register.excel}")
	private String registerReportPath;
	
	@GET
	@Path("/getFileExcel")
	@Produces("application/vnd.ms-excel")
	public Response getFileExcel(@QueryParam("id") final Long id, @QueryParam("type") final Integer type) throws Exception {
		try {
			LOG.debug("ID: " + id + ", TYPE: " + type);
			
			StreamingOutput stream = new StreamingOutput() {
				
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					OutputStream out = null;
					ByteArrayInputStream in = null;
					
					try {
						LOG.debug("Start");
						
						ExportTypeConstant typeConstant = ExportTypeConstant.findById(type);
						byte[] data = null;
						
						switch (typeConstant) {
						case REGISTER: 
							data = new RegisterReport().proceed(id, registerReportPath); 
							break;
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
			
			ResponseBuilder response = Response.ok(stream);
			response.header("Content-Disposition", "attachment; filename=report_" + String.format("%1$tY%1$tm%1$td", new Date())+ ".xlsx");
			return response.build();
		
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

}
