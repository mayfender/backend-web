package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.may.ple.backend.entity.SptMasterNamingDet;

public interface SptMasterNamingDetRepository extends JpaRepository<SptMasterNamingDet, Long> {
	
	@Query("select NEW com.may.ple.backend.entity.SptMasterNamingDet(n.displayValue, n.namingDetId) from SptMasterNamingDet n where n.namingId = ?1 and n.isActive = ?2 ")
	List<SptMasterNamingDet> findNaming(Long id, Integer isActive);
	
}
