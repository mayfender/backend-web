package com.may.ple.backend.service;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.Image;
import com.may.ple.backend.entity.ImageType;
import com.may.ple.backend.repository.ImageRepository;
import com.may.ple.backend.repository.ImageTypeRepository;

@Service
public class UploadNicEditService {
	private static final Logger LOG = Logger.getLogger(UploadNicEditService.class.getName());
	private ImageRepository imageRepository;
	private ImageTypeRepository imageTypeRepository;
	
	@Autowired
	public UploadNicEditService(ImageRepository imageRepository, ImageTypeRepository imageTypeRepository) {
		this.imageRepository = imageRepository;
		this.imageTypeRepository = imageTypeRepository;
	}
	
	public void saveImg(String imgName, String imgType, byte imageContent[]) {
		try {
			Date date = new Date();
			
			ImageType imageType = imageTypeRepository.findByTypeName(imgType.toUpperCase());
			
			Image image = new Image(imgName, imageContent, imageType, date, date);
			imageRepository.save(image);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public Image getImg(String imgName) {
		try {
			return imageRepository.findByImageName(imgName);
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
}
