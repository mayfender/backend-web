package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

public class NoticeDownloadCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(NoticeDownloadCriteriaResp.class.getName());
	private String filePath;
	private boolean isFillTemplate;
	private String address;
	private byte data[];

	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		
		try {
			byte[] data = null;
			
			if(isFillTemplate) {
				data = this.data;
			} else {
				LOG.debug("Get byte");
				java.nio.file.Path path = Paths.get(filePath);
				data = Files.readAllBytes(path);									
			}
			
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
			if(fis != null) fis.close();
			if(in != null) in.close();			
			if(out != null) out.close();			
		}	
	}
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isFillTemplate() {
		return isFillTemplate;
	}

	public void setFillTemplate(boolean isFillTemplate) {
		this.isFillTemplate = isFillTemplate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
