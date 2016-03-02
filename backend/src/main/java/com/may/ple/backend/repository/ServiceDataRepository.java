package com.may.ple.backend.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.ServiceData;

public interface ServiceDataRepository extends JpaRepository<ServiceData, Long> {
	Page<ServiceData> findByserviceTypeId(Pageable pageable, Long type);
	Page<ServiceData> findByserviceTypeIdAndDocNoContaining(Pageable pageable, Long type, String docNo);
	Page<ServiceData> findByserviceTypeIdAndCreatedDateTimeBetween(Pageable pageable, Long type, Date startDate, Date endDate);
	Page<ServiceData> findByserviceTypeIdAndCreatedDateTimeGreaterThanEqual(Pageable pageable, Long type, Date startDate);
	Page<ServiceData> findByserviceTypeIdAndCreatedDateTimeLessThanEqual(Pageable pageable, Long type, Date endDate);
	Page<ServiceData> findByserviceTypeIdAndCreatedDateTimeBetweenAndDocNoContaining(Pageable pageable, Long type, Date startDate, Date endDate, String docNo);
	Page<ServiceData> findByserviceTypeIdAndCreatedDateTimeGreaterThanEqualAndDocNoContaining(Pageable pageable, Long type, Date startDate, String docNo);
	Page<ServiceData> findByserviceTypeIdAndCreatedDateTimeLessThanEqualAndDocNoContaining(Pageable pageable, Long type, Date endDate, String docNo);
}
