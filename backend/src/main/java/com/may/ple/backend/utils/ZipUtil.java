package com.may.ple.backend.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

public class ZipUtil {
	private static final Logger LOG = Logger.getLogger(ZipUtil.class.getName());
	
	public static List<String> unZip(File tarFile, File directory) throws IOException {
	    List<String> result = new ArrayList<String>();
	    InputStream inputStream = new FileInputStream(tarFile);
	    ZipArchiveInputStream in = new ZipArchiveInputStream(inputStream);
	    ZipArchiveEntry entry = in.getNextZipEntry();
	    while (entry != null) {
	        if (entry.isDirectory()) {
	            entry = in.getNextZipEntry();
	            continue;
	        }
	        File curfile = new File(directory, entry.getName());
	        File parent = curfile.getParentFile();
	        if (!parent.exists()) {
	            parent.mkdirs();
	        }
	        OutputStream out = new FileOutputStream(curfile);
	        IOUtils.copy(in, out);
	        out.close();
	        result.add(entry.getName());
	        entry = in.getNextZipEntry();
	    }
	    in.close();
	    return result;
	}
	
	public static List<String> unZipToCurrentFolder(File tarFile) throws Exception {
		try {
			List<String> result = new ArrayList<String>();
		    InputStream inputStream = new FileInputStream(tarFile);
		    ZipArchiveInputStream in = new ZipArchiveInputStream(inputStream);
		    ZipArchiveEntry entry = in.getNextZipEntry();
		    while (entry != null) {
		        if (entry.isDirectory()) {
		            entry = in.getNextZipEntry();
		            continue;
		        }
		        
		        String directory = FilenameUtils.removeExtension(tarFile.getCanonicalPath());
		        LOG.debug("Unzip to " + directory);
		        
		        File curfile = new File(directory, entry.getName());
		        File parent = curfile.getParentFile();
		        if (!parent.exists()) {
		            parent.mkdirs();
		        }
		        OutputStream out = new FileOutputStream(curfile);
		        IOUtils.copy(in, out);
		        out.close();
		        result.add(entry.getName());
		        entry = in.getNextZipEntry();
		    }
		    in.close();
		    return result;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}

}
