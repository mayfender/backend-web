package com.may.ple.backend.utils;

import java.util.Date;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.may.ple.backend.model.FileDetail;

public class FileUtil {
	private static final Logger LOG = Logger.getLogger(FileUtil.class.getName());
	
	public static FileDetail getFileName(FormDataContentDisposition fileDetail, Date date) throws Exception {
		try {
			LOG.debug("Start");
			int indexFile = fileDetail.getFileName().lastIndexOf(".");
			String fileName = new String(fileDetail.getFileName().substring(0, indexFile).getBytes("iso-8859-1"), "UTF-8");
			String fileExt = fileDetail.getFileName().substring(indexFile);
			fileName = fileName + "_" + String.format("%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", date) + fileExt;
			
			FileDetail fd = new FileDetail();
			fd.fileName = fileName;
			fd.fileExt = fileExt;
			
			LOG.debug("End");
			return fd;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
