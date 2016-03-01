package com.may.ple.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.ServiceData;

public interface ServiceDataRepository extends JpaRepository<ServiceData, Long> {
	Page<ServiceData> findByserviceTypeId(Pageable pageable, Long type);
	Page<ServiceData> findByserviceTypeIdAndDocNoContaining(Pageable pageable, Long type, String docNo);
}
