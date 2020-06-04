package com.may.ple.backend.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
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
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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
	
	public static byte[] createZip(String directoryPath) throws IOException {
		LOG.debug("Start cratezip");
		ByteArrayOutputStream out = null;
        BufferedOutputStream bOut = null;
        ZipArchiveOutputStream tOut = null;
 
        try {
            out = new ByteArrayOutputStream();
            bOut = new BufferedOutputStream(out);
            tOut = new ZipArchiveOutputStream(bOut);
            addFileToZip(tOut, directoryPath, "");
        } finally {
            tOut.finish();
            tOut.close();
            bOut.close();
            out.close();
        }
        LOG.debug("End cratezip");
        return out.toByteArray();
	}
	
	public static void createZip(String directoryPath, String zipPath) throws IOException {
		LOG.debug("Start cratezip");
        FileOutputStream fOut = null;
        BufferedOutputStream bOut = null;
        ZipArchiveOutputStream tOut = null;
 
        try {
            fOut = new FileOutputStream(new File(zipPath));
            bOut = new BufferedOutputStream(fOut);
            tOut = new ZipArchiveOutputStream(bOut);
            addFileToZip(tOut, directoryPath, "");
        } finally {
            tOut.finish();
            tOut.close();
            bOut.close();
            fOut.close();
        }
        LOG.debug("End cratezip");
    }
	
	private static void addFileToZip(ZipArchiveOutputStream zOut, String path, String base) throws IOException {
        File f = new File(path);
        String entryName = base + f.getName();
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(f, entryName);
 
        zOut.putArchiveEntry(zipEntry);
 
        if (f.isFile()) {
            FileInputStream fInputStream = null;
            try {
                fInputStream = new FileInputStream(f);
                IOUtils.copy(fInputStream, zOut);
                zOut.closeArchiveEntry();
            } finally {
                IOUtils.closeQuietly(fInputStream);
            }
 
        } else {
            zOut.closeArchiveEntry();
            File[] children = f.listFiles();
 
            if (children != null) {
                for (File child : children) {
                    addFileToZip(zOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }

}