package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.SptMemberType;

public interface SptMemberTypeRepository extends JpaRepository<SptMemberType, Long> {
	List<SptMemberType> findByStatus(Integer status);
}
