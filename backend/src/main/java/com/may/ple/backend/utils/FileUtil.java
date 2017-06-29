package com.may.ple.backend.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
			String fileExt = fileDetail.getFileName().substring(indexFile).toLowerCase();
			
			if(date != null) {
				fileName = fileName + "_" + String.format(Locale.ENGLISH, "%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", date) + fileExt;				
			} else {
				fileName = fileName + fileExt;
			}
			
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
	
	public static FileDetail getFileName2(FormDataContentDisposition fileDetail, Date date) throws Exception {
		try {
			LOG.debug("Start");
			int indexFile = fileDetail.getFileName().lastIndexOf(".");
			String fileName = new String(fileDetail.getFileName().substring(0, indexFile).getBytes("iso-8859-1"), "UTF-8");
			String fileExt = fileDetail.getFileName().substring(indexFile);
			
			if(date != null) {
				fileName = fileName + "_" + String.format(Locale.ENGLISH, "%1$tY%1$tm%1$td%1$tH%1$tM%1$tS%1$tL", date);				
			}
			
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
	
	public static String getPath(String prodId) throws Exception {
		try {
			return prodId + "/" + String.format(Locale.ENGLISH, "%tY", Calendar.getInstance().getTime());
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public static File[] listDir(String path) {
		FileFilter directoryFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		
		File[] listDir = new File(path).listFiles(directoryFilter);
		
		return listDir;
	}
	
	public static File[] listFile(String path) {
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isFile();
			}
		};
		
		File[] listFile = new File(path).listFiles(fileFilter);
		
		return listFile;
	}

}