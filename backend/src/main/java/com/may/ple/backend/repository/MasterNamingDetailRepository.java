package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.SptMasterNamingDet;

public interface MasterNamingDetailRepository extends JpaRepository<SptMasterNamingDet, Long> {
	
	List<SptMasterNamingDet> findByNamingId(Long id);
	
}
