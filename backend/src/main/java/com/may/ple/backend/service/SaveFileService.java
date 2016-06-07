package com.may.ple.backend.service;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

public class SaveFileService extends Thread {
	private static final Logger LOG = Logger.getLogger(SaveFileService.class.getName());
	private Workbook workbook;
	private String fileNameFull;
	private String filePathTask;
	
	public SaveFileService(Workbook workbook, String filePathTask, String fileNameFull) {
		this.workbook = workbook;
		this.filePathTask = filePathTask;
		this.fileNameFull = fileNameFull;
	}
	
	@Override
	public void run() {
		try {
			saveToDisk(workbook, fileNameFull);			
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}
	
	public void saveToDisk(Workbook workbook, String fileNameFull) throws Exception {
		FileOutputStream fileOut = null;
		
		try {
			LOG.debug("Start save file");
			
			File file = new File(filePathTask);
			if(!file.exists()) {
				boolean result = file.mkdirs();				
				if(!result) throw new Exception("Cann't create task-file folder");
				LOG.debug("Create Folder SUCCESS!");
			}
			
			fileOut = new FileOutputStream(filePathTask + "/" + fileNameFull);
			
			LOG.debug("Save to file");
			workbook.write(fileOut);
		
			LOG.debug("Finished save file");
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(workbook != null) workbook.close();
			if(fileOut != null) fileOut.close();
		}
	}

}
