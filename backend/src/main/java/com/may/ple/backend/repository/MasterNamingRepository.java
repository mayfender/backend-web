package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.MasterNaming;

public interface MasterNamingRepository extends JpaRepository<MasterNaming, Long> {
	List<MasterNaming> findByStatus(Integer status);
}
