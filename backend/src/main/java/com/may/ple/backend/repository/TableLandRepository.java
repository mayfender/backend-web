package com.may.ple.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.TableLand;

public interface TableLandRepository extends JpaRepository<TableLand, Long> {
	
	List<TableLand> findByIsDeleted(Boolean val);

}
