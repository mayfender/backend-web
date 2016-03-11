package com.may.ple.backend.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.repository.SptRegistrationRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class SptRegistrationService {
	private static final Logger LOG = Logger.getLogger(SptRegistrationService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	private UserRepository userRepository;
	private EntityManager em;
	
	@Autowired
	public SptRegistrationService(EntityManager em, SptRegistrationRepository sptRegistrationRepository, UserRepository userRepository) {
		this.em = em;
		this.userRepository = userRepository;
		this.sptRegistrationRepository = sptRegistrationRepository;
	}
	
	public List<SptRegistration> findRegistered(SptRegisteredFindCriteriaReq req) {
		String jpql = "select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.firstname, r.lastname, r.isActive, m.memberTypeName) "
				    + "from SptRegistration r, SptMemberType m "
				    + "where r.isActive != 2 and r.memberTypeId = m.memberTypeId xxx order by r.firstname "; 
		
		String where = "";
		
		if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
		if(req.getIsActive() != null) where += "and r.isActive = :isActive ";
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptRegistration.class);
		
		if(req.getFirstname() != null) query.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) query.setParameter("isActive", req.getIsActive());
		
		List<SptRegistration> resultList = query.getResultList();
		return resultList;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
	public void saveMemberType(SptMemberTypeSaveCriteriaReq req) {
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptMemberType memberType = new SptMemberType(req.getStatus(), 
				u.getId(), date, u.getId(), date, 
				req.getMemberTypeName(), 
				req.getDurationType(), 
				req.getDurationQty(), 
				req.getMemberPrice());
		
		sptMemberTypeRepository.save(memberType);
	}
	
	public void updateMemberType(SptMemberTypeSaveCriteriaReq req) {
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptMemberType memberType = sptMemberTypeRepository.findOne(req.getMemberTypeId());
		memberType.setMemberTypeName(req.getMemberTypeName());
		memberType.setDurationType(req.getDurationType());
		memberType.setDurationQty(req.getDurationQty());
		memberType.setMemberPrice(req.getMemberPrice());
		memberType.setStatus(req.getStatus());
		memberType.setModifiedBy(u.getId());
		memberType.setModifiedDate(date);
		
		sptMemberTypeRepository.save(memberType);
	}
	
	public void deleteMemberType(Long id) {
		Date date = new Date();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptMemberType memberType = sptMemberTypeRepository.findOne(id);
		memberType.setStatus(2);
		memberType.setModifiedBy(u.getId());
		memberType.setModifiedDate(date);
		
		sptMemberTypeRepository.save(memberType);
	}*/

}
