package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.SptRegistration;

public interface SptRegistrationRepository extends JpaRepository<SptRegistration, Long> {
	List<SptRegistration> findByStatus(Integer status);
	SptRegistration findByMemberId(String memberId);
}
