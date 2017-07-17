package com.may.ple.backend.schedulers.jobs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.log4j.Logger;

public class BackupCommons {
	private static final Logger LOG = Logger.getLogger(BackupCommons.class.getName());
	
	public static void clearFileOldThan1Month(String backupRoot) {
		try {
			File folder = new File(backupRoot);
			
			if(!folder.exists()) return;
				
			LOG.debug("Get zip file");
            List<File> files = (List<File>) FileUtils.listFiles(new File(backupRoot), FileFilterUtils.suffixFileFilter("zip"), null);
            
            if(files.size() == 0) return;
            
        	Calendar car = Calendar.getInstance(); 
        	car.add(Calendar.MONTH, -1);
        	int underscoreIndex, dotIndex;
        	String fileDateStr;
        	Date fileDate;
        	
        	for (File file : files) {
        		underscoreIndex = file.getName().lastIndexOf("_");
        		dotIndex = file.getName().lastIndexOf(".");
        		fileDateStr = file.getName().substring(underscoreIndex + 1, dotIndex);
        		fileDate = new SimpleDateFormat("yyyyMMddHHmm", Locale.ENGLISH).parse(fileDateStr);
        		
        		if(fileDate.before(car.getTime())) {
        			LOG.debug("file: " + file.getName() + " before: " + car.getTime());
        			FileUtils.forceDelete(file);
        		}
			}
		} catch (Exception e) {
			LOG.error(e.toString());
		}
	}	

}
