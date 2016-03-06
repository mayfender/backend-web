package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.ServiceData;

public interface ServiceDataRepository extends JpaRepository<ServiceData, Long> {

}
