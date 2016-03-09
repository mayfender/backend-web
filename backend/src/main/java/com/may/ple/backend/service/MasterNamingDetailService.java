package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.MasterNamingDetailCriteriaReq;
import com.may.ple.backend.entity.SptMasterNamingDet;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.MasterNamingDetailRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class MasterNamingDetailService {
	private static final Logger LOG = Logger.getLogger(MasterNamingDetailService.class.getName());
	private MasterNamingDetailRepository masterNamingDetailRepository;
	private UserRepository userRepository;
	private EntityManager em;
	
	@Autowired
	public MasterNamingDetailService(MasterNamingDetailRepository masterNamingDetailRepository, EntityManager em, UserRepository userRepository) {
		this.masterNamingDetailRepository = masterNamingDetailRepository;
		this.em = em;
		this.userRepository = userRepository;
	}
	
	public List<SptMasterNamingDet> findMasterDetail(MasterNamingDetailCriteriaReq req) {
		
		String jpql = "select m from SptMasterNamingDet m where m.namingId = :namingId and m.status != 2 xxx order by m.displayValue "; 
		String where = "";
		
		if(req.getDisplayValue() != null) where += " and m.displayValue like :displayValue ";
		if(req.getStatus() != null) where += " and m.status = :status ";
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptMasterNamingDet.class);
		query.setParameter("namingId", req.getMasterNamingId());
		
		if(req.getDisplayValue() != null) query.setParameter("displayValue", "%" + req.getDisplayValue() + "%");
		if(req.getStatus() != null) query.setParameter("status", req.getStatus());
		
		List<SptMasterNamingDet> resultList = query.getResultList();
		return resultList;
	}
	
	public Long save(MasterNamingDetailCriteriaReq req) {
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptMasterNamingDet sptMasterNamingDet = new SptMasterNamingDet(req.getDisplayValue(), req.getStatus(), u.getId(), date, u.getId(), date, req.getMasterNamingId());
		masterNamingDetailRepository.save(sptMasterNamingDet);
		return sptMasterNamingDet.getNamingDetId();
	}
	
	public void update(MasterNamingDetailCriteriaReq req) {
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptMasterNamingDet detail = masterNamingDetailRepository.findOne(req.getMasterNamingDetailId());
		detail.setDisplayValue(req.getDisplayValue());
		detail.setStatus(req.getStatus());
		detail.setModifiedBy(u.getId());
		detail.setModifiedDate(date);
		
		masterNamingDetailRepository.save(detail);
	}
	
	public void delete(Long id) {
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptMasterNamingDet detail = masterNamingDetailRepository.findOne(id);
		detail.setStatus(2);
		detail.setModifiedBy(u.getId());
		detail.setModifiedDate(date);
		
		masterNamingDetailRepository.save(detail);
	}

}
