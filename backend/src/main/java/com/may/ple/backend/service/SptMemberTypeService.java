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

import com.may.ple.backend.criteria.SptMemberTypeFindCriteriaReq;
import com.may.ple.backend.criteria.SptMemberTypeSaveCriteriaReq;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.SptMemberTypeRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class SptMemberTypeService {
	private static final Logger LOG = Logger.getLogger(SptMemberTypeService.class.getName());
	private SptMemberTypeRepository sptMemberTypeRepository;
	private UserRepository userRepository;
	private EntityManager em;
	
	@Autowired
	public SptMemberTypeService(EntityManager em, SptMemberTypeRepository sptMemberTypeRepository, UserRepository userRepository) {
		this.em = em;
		this.userRepository = userRepository;
		this.sptMemberTypeRepository = sptMemberTypeRepository;
	}
	
	public List<SptMemberType> findMemberType(SptMemberTypeFindCriteriaReq req) {
		String jpql = "select NEW com.may.ple.backend.entity.SptMemberType(m.memberTypeId, m.memberTypeName, m.status) "
				    + "from SptMemberType m "
				    + "where m.status != 2 xxx order by m.memberTypeName "; 
		
		String where = "";
		
		if(req.getMemberTypeName() != null) where += "and m.memberTypeName like :memberTypeName ";
		if(req.getDurationType() != null) where += "and m.durationType = :durationType ";
		if(req.getStatus() != null) where += "and m.status = :status ";
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptMemberType.class);
		
		if(req.getMemberTypeName() != null) query.setParameter("memberTypeName", "%" + req.getMemberTypeName() + "%");
		if(req.getDurationType() != null) query.setParameter("durationType", req.getDurationType());
		if(req.getStatus() != null) query.setParameter("status", req.getStatus());
		
		List<SptMemberType> resultList = query.getResultList();
		return resultList;
	}
	
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

}
