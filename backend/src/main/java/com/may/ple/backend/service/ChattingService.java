package com.may.ple.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ImageUtil;

@Service
public class ChattingService {
	private static final Logger LOG = Logger.getLogger(ChattingService.class.getName());
	private MongoTemplate templateCore;
	private DbFactory dbFactory;
	private UserService uService;
	private ServletContext servletContext;
	
	@Autowired	
	public ChattingService(MongoTemplate templateCore, DbFactory dbFactory, UserService uService, ServletContext servletContext) {
		this.templateCore = templateCore;
		this.dbFactory = dbFactory;
		this.uService = uService;
		this.servletContext = servletContext;
	}
	
	public List<Users> getFriends(Integer currentPage, Integer itemsPerPage, String keyword) throws Exception {
		try {
			List<String> roles = new ArrayList<>();
			roles.add("ROLE_USER");
			roles.add("ROLE_SUPERVISOR");
			roles.add("ROLE_ADMIN");
			List<Users> friends = uService.getChatFriends(null, roles, currentPage, itemsPerPage, keyword);
			byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
			ByteArrayOutputStream baos;
			BufferedImage bImg;
			ImgData imgData;
			InputStream in;
			String ext;
			
			for (Users users : friends) {
				if(users.getImgData() == null || users.getImgData().getImgContent() == null) {
					imgData = new ImgData();
					imgData.setImgContent(defaultThumbnail);
					imgData.setImgName("default.png");
					users.setImgData(imgData);
				}
				
				LOG.debug("Before " + users.getImgData().getImgContent().length);
				in = new ByteArrayInputStream(users.getImgData().getImgContent());
				ext = FilenameUtils.getExtension(users.getImgData().getImgName());
				
				bImg = Thumbnails.of(ImageIO.read(in))
				.size(80, 80)
			    .outputFormat(ext)
			    .asBufferedImage();
			    
			    baos = new ByteArrayOutputStream();
				ImageIO.write( bImg, ext, baos );
				users.getImgData().setImgContent(baos.toByteArray());
				LOG.debug("After " + users.getImgData().getImgContent().length);
			}
			
			return friends;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Users> getChat() throws Exception {
		try {
			
			return null;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
}
