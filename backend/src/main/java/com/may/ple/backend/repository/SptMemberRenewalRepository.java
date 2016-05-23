package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.may.ple.backend.entity.SptMemberRenewal;
import com.may.ple.backend.entity.SptRegistration;

public interface SptMemberRenewalRepository extends JpaRepository<SptMemberRenewal, Long> {
	
	@Query("select count(r.renewalId) from SptMemberRenewal r where r.registration = ?1 ")
	Long countRenewal(SptRegistration registration);
	
}
