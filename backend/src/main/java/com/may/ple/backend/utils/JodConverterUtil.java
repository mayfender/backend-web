package com.may.ple.backend.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import com.may.ple.backend.constant.FileTypeConstant;


public class JodConverterUtil {
	private static final Logger LOG = Logger.getLogger(JodConverterUtil.class.getName());
	
	public static byte[] toPdf(InputStream inputStream, String sourceExt) throws Exception {
		OpenOfficeConnection connection = null;
		
		try {
			if(StringUtils.isBlank(sourceExt)) throw new Exception("sourceExt is null");
			
			FileTypeConstant source = FileTypeConstant.findByName(sourceExt);
			
			if(source != FileTypeConstant.ODT && source != FileTypeConstant.DOCX) {
				throw new Exception("File type {"+ sourceExt +"} is not supported");
			}
			
			connection = new SocketOpenOfficeConnection();
			connection.connect();
			
			LOG.debug("Connect to oppenoffice success");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
			DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection, formatRegistry);
			DocumentFormat odtFileFormat = formatRegistry.getFormatByFileExtension(source.getName());
			DocumentFormat pdfFileFormat = formatRegistry.getFormatByFileExtension("pdf");
			converter.convert(inputStream, odtFileFormat, outputStream, pdfFileFormat);
			
			return outputStream.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if (connection != null && connection.isConnected()) connection.disconnect();
		}
	}

}
