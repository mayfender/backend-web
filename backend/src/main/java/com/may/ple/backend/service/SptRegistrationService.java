package com.may.ple.backend.service;

import java.rmi.server.UID;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.PersistUserCriteriaReq;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaReq;
import com.may.ple.backend.criteria.SptRegisteredFindCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationEditCriteriaResp;
import com.may.ple.backend.criteria.SptRegistrationSaveCriteriaReq;
import com.may.ple.backend.entity.Image;
import com.may.ple.backend.entity.ImageType;
import com.may.ple.backend.entity.SptMasterNamingDet;
import com.may.ple.backend.entity.SptMemberType;
import com.may.ple.backend.entity.SptRegistration;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.entity.Zipcodes;
import com.may.ple.backend.repository.ImageRepository;
import com.may.ple.backend.repository.ImageTypeRepository;
import com.may.ple.backend.repository.SptMasterNamingDetRepository;
import com.may.ple.backend.repository.SptRegistrationRepository;
import com.may.ple.backend.repository.UserRepository;
import com.may.ple.backend.repository.ZipCodesRepository;
import com.may.ple.backend.utils.DateUtil;

@Service
public class SptRegistrationService {
	private static final Logger LOG = Logger.getLogger(SptRegistrationService.class.getName());
	private SptMasterNamingDetRepository masterNamingDetailRepository;
	private SptRegistrationRepository sptRegistrationRepository;
	private SptMemberTypeService sptMemberTypeService;
	private ImageTypeRepository imageTypeRepository;
	private MasterNamingDetailService detailService;
	private ZipCodesRepository zipCodesRepository;
	private ImageRepository imageRepository;
	private ZipcodesService zipcodesService;
	private UserRepository userRepository;
	private UserService userService;
	private EntityManager em;
	
	@Autowired
	public SptRegistrationService(EntityManager em, SptRegistrationRepository sptRegistrationRepository, 
									UserRepository userRepository, UserService userService,
									ImageTypeRepository imageTypeRepository,
									ImageRepository imageRepository, 
									SptMemberTypeService sptMemberTypeService,
									MasterNamingDetailService detailService,
									SptMasterNamingDetRepository masterNamingDetailRepository,
									ZipCodesRepository zipCodesRepository, 
									ZipcodesService zipcodesService) {
		this.em = em;
		this.userRepository = userRepository;
		this.sptRegistrationRepository = sptRegistrationRepository;
		this.userService = userService;
		this.imageTypeRepository = imageTypeRepository;
		this.imageRepository = imageRepository;
		this.sptMemberTypeService = sptMemberTypeService;
		this.detailService = detailService;
		this.masterNamingDetailRepository = masterNamingDetailRepository;
		this.zipCodesRepository = zipCodesRepository;
		this.zipcodesService = zipcodesService;
	}
	
	public SptRegisteredFindCriteriaResp findRegistered(SptRegisteredFindCriteriaReq req) {
		SptRegisteredFindCriteriaResp resp = new SptRegisteredFindCriteriaResp();
		
		String jpqlCount = "select count(r.regId) "
			    + "from SptRegistration r, Users u "
			    + "where r.userId = u.id and u.enabled <> 9 xxx "; 
		
		String where = "";
		
		if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
		if(req.getIsActive() != null) where += "and u.enabled = :enabled ";
		if(req.getMemberId() != null) where += "and r.memberId = :memberId ";
		if(req.getExpireDate() != null) where += "and r.expireDate = :expireDate ";
		
		jpqlCount = jpqlCount.replace("xxx", where);
		Query queryTotal = em.createQuery(jpqlCount);
		
		if(req.getFirstname() != null) queryTotal.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) queryTotal.setParameter("enabled", req.getIsActive());
		if(req.getMemberId() != null) queryTotal.setParameter("memberId", req.getMemberId());
		if(req.getExpireDate() != null) queryTotal.setParameter("expireDate", req.getExpireDate(), TemporalType.DATE);
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		resp.setTotalItems(countResult);
		//-------------------------------------------------------------------------------------------------------------------------
		
		String jpql = "select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.firstname, r.lastname, m.memberTypeName, u.enabled, r.memberId, r.expireDate) "
			    + "from SptRegistration r, SptMemberType m, Users u "
			    + "where r.memberTypeId = m.memberTypeId and r.userId = u.id and u.enabled <> 9 xxx order by r.firstname "; 
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptRegistration.class);
		
		if(req.getFirstname() != null) query.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) query.setParameter("enabled", req.getIsActive());
		if(req.getMemberId() != null) query.setParameter("memberId", req.getMemberId());
		if(req.getExpireDate() != null) query.setParameter("expireDate", req.getExpireDate(), TemporalType.DATE);
		
		int startRecord = (req.getCurrentPage() - 1) * req.getItemsPerPage();
		LOG.debug("Start get record: " + startRecord);
		
		query.setFirstResult(startRecord);
		query.setMaxResults(req.getItemsPerPage());
		
		List<SptRegistration> resultList = query.getResultList();
		resp.setRegistereds(resultList);
		
		return resp;
	}
	
	public List<SptRegistration> findRegisteredForExport(SptRegisteredFindCriteriaReq req) {
		String where = "";
		
		String jpql = "select NEW com.may.ple.backend.entity.SptRegistration"
				    + "(r.memberId, r.empireNo, r.fingerId, r.prefixName, r.firstname, r.lastname, r.firstnameEng, r.lastnameEng, "
				    + "r.birthday, r.citizenId, m.memberTypeName, r.registerDate, r.expireDate, "
				    + "u.enabled, r.conAddress, r.zipcode, r.conTelNo, r.conMobileNo1, r.conMobileNo2, r.conMobileNo3, "
				    + "r.conEmail, r.conFacebook, r.conLineId, "
				    + "u.createdDateTime, cu.userName, u.updatedDateTime, uu.userName) "				
			    	+ "from SptRegistration r, SptMemberType m, Users u, Users cu, Users uu "
			    	+ "where r.memberTypeId = m.memberTypeId and r.userId = u.id and r.createdBy = cu.id and r.modifiedBy = uu.id "
			    	+ "and u.enabled <> 9 xxx order by r.firstname "; 
		
		if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
		if(req.getIsActive() != null) where += "and u.enabled = :enabled ";
		if(req.getMemberId() != null) where += "and r.memberId = :memberId ";
		if(req.getExpireDate() != null) where += "and r.expireDate = :expireDate ";
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptRegistration.class);
		
		if(req.getFirstname() != null) query.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) query.setParameter("enabled", req.getIsActive());
		if(req.getMemberId() != null) query.setParameter("memberId", req.getMemberId());
		if(req.getExpireDate() != null) query.setParameter("expireDate", req.getExpireDate(), TemporalType.DATE);
		
		List<SptRegistration> resultList = query.getResultList();
		
		return resultList;
	}
	
	@Transactional
	public Long saveRegistration(SptRegistrationSaveCriteriaReq req) throws Exception {
		UID uid = new UID();
		req.getAuthen().setUserNameShow("-");
		req.getAuthen().setUserName(uid.toString());
		req.getAuthen().setPassword(new String(Base64.encode("123".getBytes())));
		
		Long userId = userService.saveUser(req.getAuthen());
		LOG.debug("UserId: " + userId);
		
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		Image image = null;
		Date date = new Date();
		
		if(!StringUtils.isBlank(req.getImgName())) {
			byte[] imageContent = Base64.decode(req.getImgContent().getBytes());
			String imgNameAndType[] = req.getImgName().split("\\.");
			String imgName = imgNameAndType[0];
			String imgType = imgNameAndType[1];
			
			ImageType imageType = imageTypeRepository.findByTypeName(imgType.toUpperCase());
			image = new Image(imgName, imageContent, imageType, date, date);
			imageRepository.save(image);
			LOG.debug("Save Image");
		}
		
		/*StringBuilder jpql = new StringBuilder();
		jpql.append("select r.memberId ");
		jpql.append("from SptRegistration r ");
		jpql.append("where r.regId = (select max(sr.regId) from SptRegistration sr ) ");
		
		Query query = em.createQuery(jpql.toString(), String.class);
		String memberId = "";
		
		try {
			memberId = (String)query.getSingleResult();
			int runNumber = Integer.parseInt(memberId.substring(9)) + 1;
			memberId = String.format("SPT%1$tY%1$tm" + String.format("%03d", runNumber), new Date());
		} catch (NoResultException e) {
			memberId = String.format("SPT%1$tY%1$tm" + String.format("%03d", 1), new Date());
		}
		LOG.debug("Next memberId: " + memberId);*/
		
		SptMasterNamingDet prefixName = masterNamingDetailRepository.findOne(req.getPrefixName().getNamingDetId());
		Zipcodes zipcode = zipCodesRepository.findOne(req.getAddressId());
		
		SptRegistration sptRegistration = new SptRegistration(req.getMemberId(), req.getEmpireNo(), prefixName, req.getFirstname(), 
				req.getLastname(), req.getFirstnameEng(), req.getLastnameEng(), req.getCitizenId(), req.getBirthday(), 
				req.getFingerId(), req.getRegisterDate(), req.getExpireDate(), req.getConTelNo(), 
				req.getConMobileNo1(), req.getConMobileNo2(), req.getConMobileNo3(), req.getConLineId(), req.getConFacebook(), 
				req.getConEmail(), req.getConAddress(), 0, u.getId(), u.getId(), 
				req.getMemberTypeId(), userId, image == null ? null : image.getId(), req.getPayType(), req.getPrice(), zipcode);
		
		sptRegistrationRepository.save(sptRegistration);
		
		return sptRegistration.getRegId();
	}
	
	public SptRegistrationEditCriteriaResp editRegistration(Long id) {
		LOG.debug("showMemberType");
		SptRegistrationEditCriteriaResp resp = new SptRegistrationEditCriteriaResp();
		
		List<SptMemberType> memberTypes = sptMemberTypeService.showMemberType();
		resp.setMemberTyps(memberTypes);
		resp.setTodayDate(new Date());
		
		List<SptMasterNamingDet> namingDetails = detailService.findNaming(2l, 1);
		resp.setNamingDetails(namingDetails);
		
		if(id == null) return resp;
		
		LOG.debug("Get registration data to edit");
		
		StringBuilder jpql = new StringBuilder();
		jpql.append("select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.memberId, r.empireNo, r.prefixName, r.firstname, r.lastname, r.firstnameEng, r.lastnameEng, ");
		jpql.append("r.citizenId, r.birthday, r.fingerId, r.expireDate, r.registerDate, r.conTelNo, r.conMobileNo1, r.conMobileNo2, r.conMobileNo3, ");
		jpql.append("r.conLineId, r.conFacebook, r.conEmail, ");
		jpql.append("r.conAddress, r.memberTypeId, u.userName, rl.authority, u.enabled, r.imgId, r.payType, r.price, r.zipcode) ");
		jpql.append("from SptRegistration r, SptMemberType m, Users u, Roles rl ");
		jpql.append("where r.memberTypeId = m.memberTypeId and r.userId = u.id and u.userName = rl.userName and r.regId = :regId ");
		
		Query query = em.createQuery(jpql.toString(), SptRegistration.class);
		query.setParameter("regId", id);
		
		SptRegistration registration = (SptRegistration)query.getSingleResult();
		
		if(registration.getImgId() != null) {
			Image img = imageRepository.findOne(registration.getImgId());
			if(img.getImageContent() != null) {
				registration.setImgBase64(new String(Base64.encode(img.getImageContent())));				
			}
			registration.setImgId(null);
		}
		
		LOG.debug("Get zipcodes");
		List<Zipcodes> zipcodes = zipcodesService.findByZipcode(registration.getZipcode().getZipcode());
		
		resp.setZipcodes(zipcodes);
		resp.setRegistration(registration);
		
		return resp;
	}
	
	@Transactional
	public void updateRegistration(SptRegistrationSaveCriteriaReq req) throws Exception {
		SptRegistration registration = sptRegistrationRepository.findOne(req.getRegId());
		
		PersistUserCriteriaReq userCriteriaReq = new PersistUserCriteriaReq();
		userCriteriaReq.setId(registration.getUserId());
		userCriteriaReq.setUserNameShow("-");
		userCriteriaReq.setUserName(req.getAuthen().getUserName());
		userCriteriaReq.setStatus(req.getAuthen().getStatus());
		userCriteriaReq.setAuthority(req.getAuthen().getAuthority());
		userCriteriaReq.setPassword(req.getAuthen().getPassword());
		
		userService.updateUser(userCriteriaReq);
		LOG.debug("Updated User");
		
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		LOG.debug("User: "+ user.getUsername());
		Users u = userRepository.findByUserName(user.getUsername());
		
		SptRegistration sptRegistration = sptRegistrationRepository.findOne(req.getRegId());
		
		if(req.getIsChangedImg()) {
			LOG.debug("Change image");
			Date date = new Date();
			byte[] imageContent;
			String imgName = null;
			String imgType = null;
			
			if(!StringUtils.isBlank(req.getImgName())) {
				LOG.debug("Have Image passed");
				imageContent = Base64.decode(req.getImgContent().getBytes());
				String imgNameAndType[] = req.getImgName().split("\\.");
				imgName = imgNameAndType[0];
				imgType = imgNameAndType[1];			
				
				ImageType imageType = imageTypeRepository.findByTypeName(imgType.toUpperCase());
				
				if(sptRegistration.getImgId() != null) {
					LOG.debug("Update Image");
					Image image = imageRepository.findOne(sptRegistration.getImgId());
					image.setImageName(imgName);
					image.setImageContent(imageContent);
					image.setImageType(imageType);
					image.setUpdatedDate(date);
					imageRepository.save(image);
				} else {
					LOG.debug("Save new Image");
					Image image = new Image(imgName, imageContent, imageType, date, date);
					imageRepository.save(image);
					
					sptRegistration.setImgId(image.getId());
				}
			} else {
				LOG.debug("Don't have Image passed");
				if(sptRegistration.getImgId() != null) {
					LOG.debug("Delete Image");
					Image image = imageRepository.findOne(sptRegistration.getImgId());
					imageRepository.delete(image);
					sptRegistration.setImgId(null);
				}
			}
		}
		
		SptMasterNamingDet prefixName = masterNamingDetailRepository.findOne(req.getPrefixName().getNamingDetId());
		Zipcodes zipcode = zipCodesRepository.findOne(req.getAddressId());
		
		sptRegistration.setPrefixName(prefixName);
		sptRegistration.setFirstname(req.getFirstname());
		sptRegistration.setLastname(req.getLastname());
		sptRegistration.setFirstnameEng(req.getFirstnameEng());
		sptRegistration.setLastnameEng(req.getLastnameEng());
		sptRegistration.setCitizenId(req.getCitizenId());
		sptRegistration.setBirthday(req.getBirthday());
		sptRegistration.setFingerId(req.getFingerId());
		sptRegistration.setExpireDate(req.getExpireDate());
		sptRegistration.setConTelNo(req.getConTelNo());
		sptRegistration.setConMobileNo1(req.getConMobileNo1());
		sptRegistration.setConMobileNo2(req.getConMobileNo2());
		sptRegistration.setConMobileNo3(req.getConMobileNo3());
		sptRegistration.setConLineId(req.getConLineId());
		sptRegistration.setConFacebook(req.getConFacebook());
		sptRegistration.setConEmail(req.getConEmail());
		sptRegistration.setConAddress(req.getConAddress());
		sptRegistration.setModifiedBy(u.getId());
		sptRegistration.setMemberTypeId(req.getMemberTypeId());
		sptRegistration.setPayType(req.getPayType());
		sptRegistration.setPrice(req.getPrice());
		sptRegistration.setRegisterDate(req.getRegisterDate());
		sptRegistration.setZipcode(zipcode);		
		sptRegistration.setEmpireNo(req.getEmpireNo());
		sptRegistration.setMemberId(req.getMemberId());
		
		sptRegistrationRepository.save(sptRegistration);
		LOG.debug("Update registration data");
	}
	
	public void deleteRegistration(Long regId) {
		SptRegistration registration = sptRegistrationRepository.findOne(regId);
		Long userId = registration.getUserId();
		
		Users users = userRepository.findOne(userId);
		users.setEnabled(9);
		
		userRepository.save(users);
	}
	
	public SptRegisteredFindCriteriaResp findRenewal(SptRegisteredFindCriteriaReq req) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 3);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		
		String jpqlCount = "select count(r.regId) "
					     + "from SptRegistration r, Users u "
					     + "where r.userId = u.id and u.enabled <> 9 xxx "; 
		
		String where = "";
		boolean isDatePeriod = false;
		
		if(req.getFirstname() != null) where += "and (r.firstname like :firstname or r.lastname like :firstname ) ";
		if(req.getIsActive() != null) where += "and u.enabled = :enabled ";
		if(where.isEmpty()) {
			isDatePeriod = true;
			where += "and r.expireDate <= :expireAdvance ";
		}
		
		jpqlCount = jpqlCount.replace("xxx", where);
		Query queryTotal = em.createQuery(jpqlCount);
		
		if(req.getFirstname() != null) queryTotal.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) queryTotal.setParameter("enabled", req.getIsActive());
		if(isDatePeriod) queryTotal.setParameter("expireAdvance", cal.getTime());
		
		long countResult = (long)queryTotal.getSingleResult();
		LOG.debug("Totol record: " + countResult);
		
		//-------------------------------------------------------------------------------------------------------------------------
		
		String jpql = "select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.firstname, r.lastname, m.memberTypeName, "
				    + "u.enabled, r.registerDate, r.expireDate, r.memberTypeId, r.status) "
			        + "from SptRegistration r, SptMemberType m, Users u "
			        + "where r.memberTypeId = m.memberTypeId and r.userId = u.id and u.enabled <> 9 xxx order by r.firstname "; 
		
		jpql = jpql.replace("xxx", where);
		Query query = em.createQuery(jpql, SptRegistration.class);
		
		if(req.getFirstname() != null) query.setParameter("firstname", "%" + req.getFirstname() + "%");
		if(req.getIsActive() != null) query.setParameter("enabled", req.getIsActive());
		if(isDatePeriod) query.setParameter("expireAdvance", cal.getTime());
		
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
	
	public List<SptRegistration> findExpireAdvance(int dayNum) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, dayNum);
		
		String jpql = "select NEW com.may.ple.backend.entity.SptRegistration(r.regId, r.firstname, r.lastname, m.memberTypeName, "
				    + "u.enabled, r.registerDate, r.expireDate, r.memberTypeId, r.status, r.conEmail) "
			        + "from SptRegistration r, SptMemberType m, Users u "
			        + "where r.memberTypeId = m.memberTypeId and r.userId = u.id and u.enabled = 1 and r.expireDate = :expireAdvance order by r.firstname "; 
		
		Query query = em.createQuery(jpql, SptRegistration.class);
		query.setParameter("expireAdvance", cal.getTime(), TemporalType.DATE);
		
		return query.getResultList();
	}
	
	public boolean memberIdCheckExist(String memberId, Long id) {
		SptRegistration registration = sptRegistrationRepository.findByMemberId(memberId);
		
		if(registration == null || (registration != null && registration.getRegId().equals(id))) {
			return false;
		} else {
			return true;			
		}
	}
	
	public void period(List<SptRegistration> registrations) {
		Calendar calendarE;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		
		for (SptRegistration reg : registrations) {
			calendarE = Calendar.getInstance();
			calendarE.setTime(reg.getExpireDate());
			calendarE.set(Calendar.HOUR_OF_DAY, 23);
			calendarE.set(Calendar.MINUTE, 59);
			calendarE.set(Calendar.SECOND, 59);
			
			String dateDiff = DateUtil.dateDiff(calendar.getTime(), calendarE.getTime());
			reg.setPeriod(dateDiff == null ? "หมดอายุ" : dateDiff);
		}
	}
	
}
