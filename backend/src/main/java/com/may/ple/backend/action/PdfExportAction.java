package com.may.ple.backend.action;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import com.may.ple.backend.pdf.ReceiptRegistration;

@Path("pdfExport")
public class PdfExportAction {
	private static final Logger LOG = Logger.getLogger(PdfExportAction.class.getName());
	
	@GET
	@Path("/getRegistrationReceipt")
	@Produces("application/pdf")
	public Response getPdfFile(@QueryParam("id") Long id) throws Exception {
		try {
			LOG.debug("ID: " + id);
			
			final byte[] data = new ReceiptRegistration().createPdf();
		
			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					OutputStream out = null;
					ByteArrayInputStream in = null;
					
					try {
						LOG.debug("Start");
						
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
			
			String fileName = "sptr_registration_receipt.pdf";	
			ResponseBuilder response = Response.ok(stream);
			response.header("Content-Disposition", "inline; filename=" + fileName);
			return response.build();
		
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			throw e;
		}
	}

}
