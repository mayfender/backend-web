package com.may.ple.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.may.ple.backend.entity.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
