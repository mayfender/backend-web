package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.SptMasterNaming;

public interface SptMasterNamingRepository extends JpaRepository<SptMasterNaming, Long> {
	List<SptMasterNaming> findByIsActive(Integer value);
}
