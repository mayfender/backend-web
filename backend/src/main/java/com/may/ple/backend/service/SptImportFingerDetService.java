package com.may.ple.backend.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptImportFingerDetFindCriteriaReq;
import com.may.ple.backend.criteria.SptImportFingerDetFindCriteriaResp;
import com.may.ple.backend.entity.SptImportFingerDet;
import com.may.ple.backend.repository.SptImportFingerDetRepository;

@Service
public class SptImportFingerDetService {
	private static final Logger LOG = Logger.getLogger(SptImportFingerDetService.class.getName());
	private SptImportFingerDetRepository sptImportFingerDetRepository;
	private EntityManager em;
	
	@Autowired	
	public SptImportFingerDetService(SptImportFingerDetRepository sptImportFingerDetRepository, EntityManager em) {
		this.sptImportFingerDetRepository = sptImportFingerDetRepository;
		this.em = em;
	}
	
	public SptImportFingerDetFindCriteriaResp search(SptImportFingerDetFindCriteriaReq req) {
		String jpql = "select xxx "
					+ "from SptImportFingerDet d ";
		
		String where = "";
		
		/*if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
		if(req.getIsActive() != null) where += "and u.enabled = :enabled ";*/
		
		Query queryTotal = em.createQuery(jpql.replace("xxx", "count(d.fingerDetId)"));
		
//		if(req.getFirstname() != null) queryTotal.setParameter("firstname", "%" + req.getFirstname() + "%");
//		if(req.getIsActive() != null) queryTotal.setParameter("enabled", req.getIsActive());
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		jpql = jpql.replace("xxx", "d");
		Query query = em.createQuery(jpql, SptImportFingerDet.class);
		
//		if(req.getFirstname() != null) query.setParameter("firstname", "%" + req.getFirstname() + "%");
//		if(req.getIsActive() != null) query.setParameter("enabled", req.getIsActive());
		
		int startRecord = (req.getCurrentPage() - 1) * req.getItemsPerPage();
		LOG.debug("Start get record: " + startRecord);
		
		query.setFirstResult(startRecord);
		query.setMaxResults(req.getItemsPerPage());
		
		SptImportFingerDetFindCriteriaResp resp = new SptImportFingerDetFindCriteriaResp();
		List<SptImportFingerDet> resultList = query.getResultList();
		resp.setTotalItems(countResult);
		resp.setFingerDet(resultList);
		
		return resp;
	}
	
}
