package com.may.ple.backend.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;


public class JodConverterUtil {
	private static final Logger LOG = Logger.getLogger(JodConverterUtil.class.getName());
	
	public static byte[] odt2pdf(InputStream inputStream) throws Exception {
		OpenOfficeConnection connection = null;
		
		try {
			connection = new SocketOpenOfficeConnection();
			connection.connect();
			
			LOG.debug("Connect to oppenoffice success");
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
			DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection, formatRegistry);
			DocumentFormat odtFileFormat = formatRegistry.getFormatByFileExtension("odt");
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
