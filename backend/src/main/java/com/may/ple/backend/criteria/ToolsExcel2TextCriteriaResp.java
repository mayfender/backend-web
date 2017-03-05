package com.may.ple.backend.criteria;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

public class ToolsExcel2TextCriteriaResp extends CommonCriteriaResp implements StreamingOutput {
	private static final Logger LOG = Logger.getLogger(ToolsExcel2TextCriteriaResp.class.getName());
	private byte[] data;
	
	@Override
	public void write(OutputStream os) throws IOException, WebApplicationException {
		OutputStream out = null;
		ByteArrayInputStream in = null;
		FileInputStream fis = null;
		
		try {
			out = new BufferedOutputStream(os);
			in = new ByteArrayInputStream(data);
			int bytes;
			
			while ((bytes = in.read()) != -1) {
				out.write(bytes);
			}
			
			LOG.debug("End");
		} catch (Exception e) {
			LOG.error(e.toString());
		} finally {
			try { if(fis != null) fis.close(); } catch (Exception e2) {}
			try { if(in != null) in.close(); } catch (Exception e2) {}			
			try { if(out != null) out.close(); } catch (Exception e2) {}			
		}	
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
