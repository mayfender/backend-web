package com.may.ple.backend.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static void main(String[] args) {
		String line = "0000000001234567890";
		Pattern r = Pattern.compile("[1-9]");
		Matcher m = r.matcher(line);
		
		if(m.find()) {
			System.out.println(line.substring(m.start()));			
		} else {
			System.out.println("Not found");
		}
	}
	
	@Transactional(rollbackOn = Throwable.class)
	public void save(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws Exception {
		BufferedReader reader = null;
		
		try {	
			reader = new BufferedReader(new InputStreamReader(uploadedInputStream, "utf8"));
			Date date;
			Date minDate = null;
			Date maxDate = null;
			Matcher matcher;
			String fId;
	        String line;
	        String[] splited;
	        String[] timeSplit;
	        SptImportFingerDet det;
	        SptImportFingerFile sptImportFingerFile = new SptImportFingerFile(fileDetail.getFileName(), new Date(), null, null);
	        sptImportFingerFileRepository.save(sptImportFingerFile);
	        
	        while ((line = reader.readLine()) != null) {
	        	splited = line.split(",");
	        	det = new SptImportFingerDet();
	        	
	        	for (int i = 0; i < splited.length; i++) {	
	        		if(i == 0) {
	        			fId = splited[i].trim();
	        			matcher = Pattern.compile("[1-9]").matcher(fId);
	        			
	        			if(matcher.find()) {
	        				det.setFingerId(fId.substring(matcher.start()));
	        			}
	        		} else if(i == 2) {
	        			date = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(splited[i].trim());
	        			
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
	        			
	        			timeSplit = splited[i].trim().split(" ");
	        			
	        			date = new SimpleDateFormat("dd/MM/yyyy").parse(timeSplit[0]);
	        			det.setDateStamp(date);
	        			date = new SimpleDateFormat("HH:mm:ss").parse(timeSplit[1]);
	        			det.setTimeStamp(date);
	        			
	        		} else if(i == 5) {
	        			det.setAction(splited[i].trim());
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
