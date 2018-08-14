package com.may.ple.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.model.DbFactory;
import com.may.ple.backend.utils.ContextDetailUtil;
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
			ImgData defaultThum = null;
			String ext;
			
			for (Users users : friends) {
				if(users.getImgData() == null || users.getImgData().getImgContent() == null) {
					if(defaultThum == null) {
						defaultThum = new ImgData();
						defaultThum.setImgContent(compressImg(defaultThumbnail, "png"));
					}
					users.setImgData(defaultThum);
				} else {					
					ext = FilenameUtils.getExtension(users.getImgData().getImgName());
					users.getImgData().setImgContent(compressImg(users.getImgData().getImgContent(), ext));
				}
			}
			
			return friends;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public List<Map> getLastChatFriend() throws Exception {
		try {
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			
			Criteria criteria = Criteria.where("members").in(new ObjectId(user.getId()));
			Query query = Query.query(criteria)
			.with(new PageRequest(0, 10))
			.with(new Sort(Sort.Direction.DESC, "updatedDateTime"));
			query.fields().include("members").include("lastMsg");
			
			List<Map> chatting = templateCore.find(query, Map.class, "chatting");
			if(chatting.size() == 0) return chatting;
			
			List<String> roles = new ArrayList<>();
			roles.add("ROLE_USER");
			roles.add("ROLE_SUPERVISOR");
			roles.add("ROLE_ADMIN");
			List<Users> friends = uService.getChatFriends(null, roles, 1, 10000, null);
			
			byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
			ImgData defaultThum = null;
			List<ObjectId> members;
			String ext;
			
			for (Map map : chatting) {
				members = (List)map.get("members");
				
				for (ObjectId objId : members) {
					if(objId.toString().equals(user.getId())) continue;
					
					for (Users fri : friends) {
						if(!objId.toString().equals(fri.getId())) continue;
						
						if(fri.getImgData() == null || fri.getImgData().getImgContent() == null) {
							if(defaultThum == null) {
								defaultThum = new ImgData();
								defaultThum.setImgContent(compressImg(defaultThumbnail, "png"));
							}
							fri.setImgData(defaultThum);
						} else {
							ext = FilenameUtils.getExtension(fri.getImgData().getImgName());
							fri.getImgData().setImgContent(compressImg(fri.getImgData().getImgContent(), ext));
						}
						
						map.put("showname", fri.getShowname());
						map.put("firstName", fri.getFirstName());
						map.put("lastName", fri.getLastName());
						map.put("imgData", fri.getImgData());
						break;
					}
				}
			}
			
			return chatting;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private byte[] compressImg(byte[] source, String fileExt) throws Exception {
		ByteArrayOutputStream baos = null;
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(source);
			
			BufferedImage bImg = Thumbnails.of(ImageIO.read(in))
			.size(80, 80)
		    .outputFormat(fileExt)
		    .asBufferedImage();
		    
		    baos = new ByteArrayOutputStream();
			ImageIO.write(bImg, fileExt, baos);
			
			return baos.toByteArray();
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		} finally {
			if(in != null) in.close();
			if(baos != null) baos.close();
		}
	}
	
}
