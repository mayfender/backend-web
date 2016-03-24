package com.may.ple.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptImportFingerFileCriteriaResp;
import com.may.ple.backend.entity.SptImportFingerDet;
import com.may.ple.backend.entity.SptImportFingerFile;
import com.may.ple.backend.repository.SptImportFingerDetRepository;
import com.may.ple.backend.repository.SptImportFingerFileRepository;

@Service
public class SptImportFingerFileService {
	private static final Logger LOG = Logger.getLogger(SptImportFingerFileService.class.getName());
	private SptImportFingerFileRepository sptImportFingerFileRepository;
	private SptImportFingerDetRepository sptImportFingerDetRepository;
	private EntityManager em;
	
	@Autowired	
	public SptImportFingerFileService(SptImportFingerFileRepository sptImportFingerFileRepository, 
										SptImportFingerDetRepository sptImportFingerDetRepository,
										EntityManager em) {
		this.sptImportFingerFileRepository = sptImportFingerFileRepository;
		this.sptImportFingerDetRepository = sptImportFingerDetRepository;
		this.em = em;
	}
	
	@Transactional(rollbackOn = Throwable.class)
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		BufferedReader reader = null;
		
		try {	
			reader = new BufferedReader(new InputStreamReader(uploadedInputStream));
			Date date;
			Date minDate = null;
			Date maxDate = null;
	        String line;
	        String[] splited;
	        SptImportFingerDet det;
	        
	        SptImportFingerFile sptImportFingerFile = new SptImportFingerFile(fileDetail.getFileName(), new Date(), null, null);
	        sptImportFingerFileRepository.save(sptImportFingerFile);
	        
	        while ((line = reader.readLine()) != null) {
	        	splited = line.split("\t");
	        	det = new SptImportFingerDet();
	        	
	        	for (int i = 0; i < splited.length; i++) {	
	        		if(i == 0) {
	        			det.setFingerId(splited[i].trim());
	        		} else if(i == 1) {
	        			date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(splited[i].trim());
	        			det.setDateTime(date);
	        			
	        			if(minDate == null) {
	        				minDate = date;
	        				maxDate = date;
	        			} else {
	        				if(minDate.after(date)) {
	        					minDate = date;
	        				}
	        				if(maxDate.before(date)) {
	        					maxDate = date;
	        				}
	        			}
	        		} else if(i == 5) {
	        			det.setInOut(splited[i].trim());
	        		}	        		
				}
	        	
	        	det.setFile(sptImportFingerFile);
	        	sptImportFingerDetRepository.save(det);
	        }
	        
	        sptImportFingerFile = sptImportFingerFileRepository.findOne(sptImportFingerFile.getFingerFileId());
	        sptImportFingerFile.setStartedDateTime(minDate);
	        sptImportFingerFile.setEndedDateTime(maxDate);
	        sptImportFingerFileRepository.save(sptImportFingerFile);
	        
		} catch (Exception e) {
			throw e;
		} finally {
			try { if(reader != null) reader.close(); } catch (IOException e) {}
		}
	}
	
	public SptImportFingerFileCriteriaResp findAll(Integer currentPage, Integer itemsPerPage) {
		String jpqlCount = "select count(f.fingerFileId) from SptImportFingerFile f ";
		
		Query queryTotal = em.createQuery(jpqlCount);
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		String jpql = "select f from SptImportFingerFile f order by f.createdDateTime desc ";
		
		Query query = em.createQuery(jpql, SptImportFingerFile.class);
		
		int startRecord = (currentPage - 1) * itemsPerPage;
		LOG.debug("Start get record: " + startRecord);
		
		query.setFirstResult(startRecord);
		query.setMaxResults(itemsPerPage);
		
		SptImportFingerFileCriteriaResp resp = new SptImportFingerFileCriteriaResp();
		List<SptImportFingerFile> resultList = query.getResultList();
		resp.setTotalItems(countResult);
		resp.setFingerFiles(resultList);
		
		return resp;
	}
	
}
