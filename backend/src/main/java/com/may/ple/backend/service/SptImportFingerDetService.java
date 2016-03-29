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
					+ "from SptImportFingerDet d, SptRegistration r "
					+ "where d.fingerId = r.fingerId xxxx order by r.firstname, d.dateTime ";
		
		String where = "";
		
		if(req.getName() != null) where += "and (r.firstname like :name or r.lastname like :name ) ";
		
		jpql = jpql.replace("xxxx", where);
		
		Query queryTotal = em.createQuery(jpql.replace("xxx", "count(d.fingerDetId)"));
		
		if(req.getName() != null) queryTotal.setParameter("name", "%" + req.getName() + "%");
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		jpql = jpql.replace("xxx", "NEW com.may.ple.backend.entity.SptImportFingerDet(r.firstname, r.lastname, d.dateTime, d.inOut) ");
		
		Query query = em.createQuery(jpql, SptImportFingerDet.class);
		
		if(req.getName() != null) query.setParameter("name", "%" + req.getName() + "%");
		
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
