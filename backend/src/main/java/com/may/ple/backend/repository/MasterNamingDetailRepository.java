package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.MasterNamingDetail;

public interface MasterNamingDetailRepository extends JpaRepository<MasterNamingDetail, Long> {
	
	List<MasterNamingDetail> findByNamingId(Long id);
	
}
