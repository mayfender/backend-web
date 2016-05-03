package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.Zipcodes;

public interface ZipCodesRepository extends JpaRepository<Zipcodes, Long> {
	List<Zipcodes> findByZipcode(String zipcode);
}
