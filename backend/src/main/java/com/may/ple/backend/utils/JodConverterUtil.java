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
	
	private static OpenOfficeConnection getConn(String host, Integer port) throws Exception {
		try {
			OpenOfficeConnection connection;
			if(StringUtils.isBlank(host)) {
				connection = new SocketOpenOfficeConnection();
			} else {
				connection = new SocketOpenOfficeConnection(host, port);
			}
			connection.connect();
			
			return connection;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static byte[] toPdf(InputStream inputStream, String sourceExt, String host, Integer port) throws Exception {
		OpenOfficeConnection connection = null;
		
		try {
			if(StringUtils.isBlank(sourceExt)) throw new Exception("sourceExt is null");
			
			FileTypeConstant source = FileTypeConstant.findByName(sourceExt);
			
			if(source != FileTypeConstant.ODT && source != FileTypeConstant.DOCX) {
				throw new Exception("File type {"+ sourceExt +"} is not supported");
			}
			
			LOG.debug("Connect to oppenoffice success");
			connection = getConn(host, port);
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
			DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection, formatRegistry);
			DocumentFormat odtFileFormat = formatRegistry.getFormatByFileExtension(source.getName());
			DocumentFormat pdfFileFormat = formatRegistry.getFormatByFileExtension(FileTypeConstant.PDF.getName());
			converter.convert(inputStream, odtFileFormat, outputStream, pdfFileFormat);
			
			return outputStream.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if (connection != null && connection.isConnected()) connection.disconnect();
		}
	}
	
	public static byte[] odtToDoc(InputStream inputStream, String sourceExt, String host, Integer port) throws Exception {
		OpenOfficeConnection connection = null;
		
		try {
			if(StringUtils.isBlank(sourceExt)) throw new Exception("sourceExt is null");
			
			FileTypeConstant source = FileTypeConstant.findByName(sourceExt);
			
			if(source != FileTypeConstant.ODT) {
				throw new Exception("Source File {"+ sourceExt +"} is wrong");
			}
			
			LOG.debug("Connect to oppenoffice success");
			connection = getConn(host, port);
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
			DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection, formatRegistry);
			DocumentFormat odtFileFormat = formatRegistry.getFormatByFileExtension(source.getName());
			DocumentFormat pdfFileFormat = formatRegistry.getFormatByFileExtension(FileTypeConstant.DOC.getName());
			converter.convert(inputStream, odtFileFormat, outputStream, pdfFileFormat);
			
			return outputStream.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if (connection != null && connection.isConnected()) connection.disconnect();
		}
	}
	
	public static byte[] convert(InputStream inputStream, String sourceExt, String resultExt, String host, Integer port) throws Exception {
		OpenOfficeConnection connection = null;
		
		try {
			if(StringUtils.isBlank(sourceExt)) throw new Exception("sourceExt is null");
			
			FileTypeConstant source = FileTypeConstant.findByName(sourceExt);
			FileTypeConstant result = FileTypeConstant.findByName(resultExt);
			
			LOG.debug("Connect to oppenoffice success");
			connection = getConn(host, port);
			
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			DefaultDocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();
			DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection, formatRegistry);
			DocumentFormat odtFileFormat = formatRegistry.getFormatByFileExtension(source.getName());
			DocumentFormat pdfFileFormat = formatRegistry.getFormatByFileExtension(result.getName());
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
