package com.may.ple.backend.service;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationSaveCriteriaReq;
import com.may.ple.backend.entity.Image;
import com.may.ple.backend.entity.ImageType;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.repository.ImageRepository;
import com.may.ple.backend.repository.ImageTypeRepository;
import com.may.ple.backend.repository.SptRegistrationRepository;
import com.may.ple.backend.repository.UserRepository;

@Service
public class SptRegistrationService {
	private static final Logger LOG = Logger.getLogger(SptRegistrationService.class.getName());
	private SptRegistrationRepository sptRegistrationRepository;
	private SptMemberTypeService sptMemberTypeService;
	private ImageTypeRepository imageTypeRepository;
	private ImageRepository imageRepository;
	private UserRepository userRepository;
	private UserService userService;
	private EntityManager em;
	
	@Autowired
	public SptRegistrationService(EntityManager em, SptRegistrationRepository sptRegistrationRepository, 
									UserRepository userRepository, UserService userService,
									ImageTypeRepository imageTypeRepository,
									ImageRepository imageRepository, SptMemberTypeService sptMemberTypeService) {
		this.em = em;
		this.userRepository = userRepository;
		this.sptRegistrationRepository = sptRegistrationRepository;
		this.userService = userService;
		this.imageTypeRepository = imageTypeRepository;
		this.imageRepository = imageRepository;
		this.sptMemberTypeService = sptMemberTypeService;
	}
	
	public SptRegisteredFindCriteriaResp findRegistered(SptRegisteredFindCriteriaReq req) {
		
		String jpqlCount = "select count(r.regId) "
			    + "from SptRegistration r, Users u "
			    + "where r.userId = u.id and u.enabled <> 2 xxx "; 
		
		String where = "";
		
		if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
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
		
		return resp;
	}
	
	@Transactional
	public void saveRegistration(SptRegistrationSaveCriteriaReq req) throws Exception {
		req.getAuthen().setUserNameShow("]y[");
		Long userId = userService.saveUser(req.getAuthen());
		LOG.debug("UserId: " + userId);
		
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		Image image = null;
		
		if(!StringUtils.isBlank(req.getImgName())) {
			Date date = new Date();
			byte[] imageContent = Base64.decode(req.getImgContent().getBytes());
			String imgNameAndType[] = req.getImgName().split("\\.");
			String imgName = imgNameAndType[0];
			String imgType = imgNameAndType[1];
			
			ImageType imageType = imageTypeRepository.findByTypeName(imgType.toUpperCase());
			image = new Image(imgName, imageContent, imageType, date, date);
			imageRepository.save(image);
			LOG.debug("Save Image");
		}
		
//		SptRegistration registrationLast = sptRegistrationRepository.findByMaxRegId();
//		LOG.debug("Last registration(memberId): " + registrationLast.getMemberId());
		
		SptRegistration sptRegistration = new SptRegistration(null, req.getPrefixName(), req.getFirstname(), 
				req.getLastname(), req.getCitizenId(), req.getBirthday(), 
				req.getFingerId(), null, req.getExpireDate(), req.getConTelNo(), 
				req.getConMobileNo(), req.getConLineId(), req.getConFacebook(), 
				req.getConEmail(), req.getConAddress(), null, u.getId(), u.getId(), 
				req.getMemberTypeId(), userId, image == null ? null : image.getId());
		
		sptRegistrationRepository.save(sptRegistration);
	}
	
	public SptRegistrationEditCriteriaResp editRegistration(Long id) {
		SptRegistrationEditCriteriaResp resp = new SptRegistrationEditCriteriaResp();
		
		List<SptMemberType> memberTypes = sptMemberTypeService.showMemberType();
		resp.setMemberTyps(memberTypes);
		
		
		
		/*StringBuilder jpql = new StringBuilder();
		jpql.append("select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.firstname, r.lastname, m.memberTypeName, u.enabled) ");
		jpql.append("from SptRegistration r, SptMemberType m, Users u ");
		jpql.append("where r.memberTypeId = m.memberTypeId and r.userId = u.id and regId = :regId ");
		
		Query query = em.createQuery(jpql.toString(), SptRegistration.class);
		query.setParameter("regId", id);
		
		SptRegistration registration = (SptRegistration)query.getSingleResult();
		resp.setRegistration(registration);*/
		
		return resp;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*
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
