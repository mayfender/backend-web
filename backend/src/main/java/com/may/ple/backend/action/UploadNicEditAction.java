package com.may.ple.backend.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Component;

import com.may.ple.backend.entity.Image;
import com.may.ple.backend.service.UploadNicEditService;

@Component
@Path("uploadNicEdit")
public class UploadNicEditAction {
	private static final Logger LOG = Logger.getLogger(UploadNicEditAction.class.getName());
	private UploadNicEditService service;
	private HttpServletRequest request;
	private String link;
	
	@Autowired
	public UploadNicEditAction(UploadNicEditService service, HttpServletRequest request) {
		this.service = service;
		this.request = request;
	}
	
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Procedure(MediaType.APPLICATION_JSON)
	public Response upload(@FormDataParam("image") InputStream uploadedInputStream,
			@FormDataParam("image") FormDataContentDisposition fileDetail) {
		
		LOG.debug("Upload file");
		String output;
		
		try {
			byte[] content = writeToByteArray(uploadedInputStream);
			
			String imgNameAndType[] = fileDetail.getFileName().split("\\.");
			String imgName = String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS", new Date()) + "_" + imgNameAndType[0];
			String imgType = imgNameAndType[1];
			
			link = "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + "/restAct/uploadNicEdit/getImg?fileName=" + imgName;
			
			output = "{\"data\": {\"link\":\"" + link + "\", \"width\": 100}}";
			
			service.saveImg(imgName, imgType, content);			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
			output = "{\"data\": {\"error\":\"Server Error\"}}";
		}

		return Response.status(200).entity(output).build();
	}
	
	@GET
	@Path("/getImg")
	@Produces({"image/jpg", "image/png", "image/gif"})
	public Response getImg(@QueryParam("fileName") String fileName) {
		LOG.debug("getImg NicEdit");
		Image image = service.getImg(fileName);
		return Response.ok(image.getImageContent(), "image/" + image.getImageType().getTypeName()).build();	
	}

	private byte[] writeToByteArray(InputStream uploadedInputStream) throws IOException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()){
			byte[] bytes = new byte[1024];
			int read = 0;

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				bos.write(bytes, 0, read);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			throw e;
		}
	}

}
