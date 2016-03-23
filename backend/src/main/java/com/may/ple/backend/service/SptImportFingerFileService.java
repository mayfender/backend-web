package com.may.ple.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.SptImportFingerDet;
import com.may.ple.backend.entity.SptImportFingerFile;
import com.may.ple.backend.repository.SptImportFingerDetRepository;
import com.may.ple.backend.repository.SptImportFingerFileRepository;

@Service
public class SptImportFingerFileService {
	private static final Logger LOG = Logger.getLogger(SptImportFingerFileService.class.getName());
	private SptImportFingerFileRepository sptImportFingerFileRepository;
	private SptImportFingerDetRepository sptImportFingerDetRepository;
	
	@Autowired	
	public SptImportFingerFileService(SptImportFingerFileRepository sptImportFingerFileRepository, 
										SptImportFingerDetRepository sptImportFingerDetRepository) {
		this.sptImportFingerFileRepository = sptImportFingerFileRepository;
		this.sptImportFingerDetRepository = sptImportFingerDetRepository;
	}
	
	@Transactional
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		BufferedReader reader = null;
		
		try {	
			reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
	        String line;
	        String[] splited;
	        SptImportFingerDet det;
	        
	        SptImportFingerFile sptImportFingerFile = new SptImportFingerFile(fileDetail.getFileName(), new Date(), null, null, 1);
	        sptImportFingerFileRepository.save(sptImportFingerFile);
	        
	        while ((line = reader.readLine()) != null) {
	        	splited = line.split("\t");
	        	det = new SptImportFingerDet();
	        	
	        	for (int i = 0; i < splited.length; i++) {	
	        		if(i == 0) {
	        			det.setFingerId(splited[i].trim());
	        		} else if(i == 1) {
	        			det.setDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(splited[i].trim()));	        			
	        		} else if(i == 5) {
	        			det.setInOut(splited[i].trim());
	        		}	        		
				}
	        	
	        	det.setFile(sptImportFingerFile);
	        	sptImportFingerDetRepository.save(det);
	        }
	        
	        
			
	        
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(reader != null) reader.close(); } catch (IOException e) {}
		}
	}
	
}
