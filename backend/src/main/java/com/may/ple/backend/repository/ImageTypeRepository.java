package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.ImageType;

public interface ImageTypeRepository extends JpaRepository<ImageType, Long>{
	
	ImageType findByTypeName(String typeName);

}
