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
					+ "where d.fingerId = r.fingerId xxxx order by r.firstname, d.dateStamp, d.timeStamp ";
		
		String where = "";
		
		if(req.getName() != null) where += "and (r.firstname like :name or r.lastname like :name ) ";
		if(req.getStartDate() != null) where += "and d.dateStamp >= :startDate ";
		if(req.getEndDate() != null) where += "and d.dateStamp <= :endDate ";
		if(req.getStartTime() != null) where += "and d.timeStamp >= :startTime ";
		if(req.getEndTime() != null) where += "and d.timeStamp <= :endTime ";
			
		jpql = jpql.replace("xxxx", where);
		
		Query queryTotal = em.createQuery(jpql.replace("xxx", "count(d.fingerDetId)"));
		
		if(req.getName() != null) queryTotal.setParameter("name", "%" + req.getName() + "%");
		if(req.getStartDate() != null) queryTotal.setParameter("startDate", req.getStartDate());
		if(req.getEndDate() != null) queryTotal.setParameter("endDate", req.getEndDate());
		if(req.getStartTime() != null) queryTotal.setParameter("startTime", req.getStartTime());
		if(req.getEndTime() != null) queryTotal.setParameter("endTime", req.getEndTime());
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		jpql = jpql.replace("xxx", "NEW com.may.ple.backend.entity.SptImportFingerDet(r.firstname, r.lastname, d.dateStamp, d.timeStamp, d.inOut) ");
		
		Query query = em.createQuery(jpql, SptImportFingerDet.class);
		
		if(req.getName() != null) query.setParameter("name", "%" + req.getName() + "%");
		if(req.getStartDate() != null) query.setParameter("startDate", req.getStartDate());
		if(req.getEndDate() != null) query.setParameter("endDate", req.getEndDate());
		if(req.getStartTime() != null) query.setParameter("startTime", req.getStartTime());
		if(req.getEndTime() != null) query.setParameter("endTime", req.getEndTime());
		
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
