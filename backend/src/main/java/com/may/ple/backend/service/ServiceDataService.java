package com.may.ple.backend.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.criteria.ServiceDataFindCriteriaReq;
import com.may.ple.backend.criteria.ServiceDataFindCriteriaResp;
import com.may.ple.backend.entity.ServiceData;
import com.may.ple.backend.repository.ServiceDataRepository;

@Service
public class ServiceDataService {
	private static final Logger LOG = Logger.getLogger(ServiceDataService.class.getName());
	private ServiceDataRepository serviceDataRepository;
	private DataSource dataSource;
	@PersistenceContext 
	private EntityManager em;
	
	@Autowired	
	public ServiceDataService(ServiceDataRepository serviceDataRepository, DataSource dataSource) {
		this.serviceDataRepository = serviceDataRepository;
		this.dataSource = dataSource;
	}
	
	public ServiceDataFindCriteriaResp findServiceData(ServiceDataFindCriteriaReq req) {
		String jpql = "select xxx from ServiceData s where 1=1 xxxx order by s.createdDateTime desc "; 
		String where = "";
		
		if(req.getDateTimeStart() != null) where += " and s.createdDateTime >= :startDate " ;
		if(req.getDateTimeEnd() != null) where += " and s.createdDateTime <= :endDate " ;
		if(!StringUtils.isBlank(req.getDocNo())) where += " and s.docNo like :docNo " ;
		
		jpql = jpql.replace("xxxx", where);
		
		Query queryTotal = em.createQuery(jpql.replace("xxx", "count(s.id)"));
		if(req.getDateTimeStart() != null)
			queryTotal.setParameter("startDate", req.getDateTimeStart());
			
		if(req.getDateTimeEnd() != null)		
			queryTotal.setParameter("endDate", req.getDateTimeEnd());
		
		if(!StringUtils.isBlank(req.getDocNo()))
			queryTotal.setParameter("docNo", "%" + req.getDocNo() + "%");
		
		long countResult = (long)queryTotal.getSingleResult();
		
		TypedQuery<ServiceData> query = em.createQuery(jpql.replace("xxx", "s"), ServiceData.class);
		
		if(req.getDateTimeStart() != null)
			query.setParameter("startDate", req.getDateTimeStart());
			
		if(req.getDateTimeEnd() != null)		
			query.setParameter("endDate", req.getDateTimeEnd());
		
		if(!StringUtils.isBlank(req.getDocNo()))
			query.setParameter("docNo", "%" + req.getDocNo() + "%");
		
		query.setFirstResult((req.getCurrentPage() - 1) * req.getItemsPerPage());
		query.setMaxResults(req.getItemsPerPage());
		List<ServiceData> resultList = query.getResultList();
		
		ServiceDataFindCriteriaResp resp = new ServiceDataFindCriteriaResp();
		resp.setTotalItems(countResult);
		resp.setServiceDatas(resultList);
		
		
		/*PageRequest page = new PageRequest(req.getCurrentPage() - 1, req.getItemsPerPage(), Sort.Direction.DESC, "createdDateTime");
		Page<ServiceData> pageData = null;
		
		if(req.getDateTimeStart() != null && req.getDateTimeEnd() != null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeBetweenAndDocNoContaining(page, req.getServiceTypeId(), req.getDateTimeStart(), req.getDateTimeEnd(), req.getDocNo());
			
		} else if(req.getDateTimeStart() != null && req.getDateTimeEnd() != null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeBetween(page, req.getServiceTypeId(), req.getDateTimeStart(), req.getDateTimeEnd());
			
		} else if(req.getDateTimeStart() != null && req.getDateTimeEnd() == null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeGreaterThanEqualAndDocNoContaining(page, req.getServiceTypeId(), req.getDateTimeStart(), req.getDocNo());
			
		} else if(req.getDateTimeStart() != null && req.getDateTimeEnd() == null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeGreaterThanEqual(page, req.getServiceTypeId(), req.getDateTimeStart());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() != null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeLessThanEqualAndDocNoContaining(page, req.getServiceTypeId(), req.getDateTimeEnd(), req.getDocNo());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() != null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndCreatedDateTimeLessThanEqual(page, req.getServiceTypeId(), req.getDateTimeEnd());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() == null && !StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeIdAndDocNoContaining(page, req.getServiceTypeId(), req.getDocNo());
			
		} else if(req.getDateTimeStart() == null && req.getDateTimeEnd() == null && StringUtils.isBlank(req.getDocNo())) {
			pageData = serviceDataRepository.findByserviceTypeId(page, req.getServiceTypeId());		
			
		}*/
		
		return resp;
	}
	
}
