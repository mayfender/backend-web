package com.may.ple.backend.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaResp;
import com.may.ple.backend.entity.SptRegistration;
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
	
	public void search() {
		String jpqlCount = "select count(r.fingerDetId) "
			    + "from SptImportFingerDet d "
			    + "where "; 
		
		String where = "";
		
		/*if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
		if(req.getIsActive() != null) where += "and u.enabled = :enabled ";
		
		jpqlCount = jpqlCount.replace("xxx", where);
		Query queryTotal = em.createQuery(jpqlCount);
		
		if(req.getFirstname() != null) queryTotal.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) queryTotal.setParameter("enabled", req.getIsActive());
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		String jpql = "select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.firstname, r.lastname, m.memberTypeName, u.enabled) "
			    + "from SptRegistration r, SptMemberType m, Users u "
			    + "where r.memberTypeId = m.memberTypeId and r.userId = u.id and u.enabled <> 9 xxx order by r.firstname "; 
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptRegistration.class);
		
		if(req.getFirstname() != null) query.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) query.setParameter("enabled", req.getIsActive());
		
		int startRecord = (req.getCurrentPage() - 1) * req.getItemsPerPage();
		LOG.debug("Start get record: " + startRecord);
		
		query.setFirstResult(startRecord);
		query.setMaxResults(req.getItemsPerPage());
		
		SptRegisteredFindCriteriaResp resp = new SptRegisteredFindCriteriaResp();
		List<SptRegistration> resultList = query.getResultList();
		resp.setTotalItems(countResult);
		resp.setRegistereds(resultList);
		
		return resp;*/
	}
	
}
