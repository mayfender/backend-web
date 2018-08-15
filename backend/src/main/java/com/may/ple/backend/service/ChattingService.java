package com.may.ple.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.may.ple.backend.criteria.ChattingCriteriaResp;
import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.ImageUtil;

@Service
public class ChattingService {
	private static final Logger LOG = Logger.getLogger(ChattingService.class.getName());
	private MongoTemplate templateCore;
	private UserService uService;
	private ServletContext servletContext;
	
	@Autowired	
	public ChattingService(MongoTemplate templateCore, UserService uService, ServletContext servletContext) {
		this.templateCore = templateCore;
		this.uService = uService;
		this.servletContext = servletContext;
	}
	
	public List<Users> getFriends(Integer currentPage, Integer itemsPerPage, String keyword) throws Exception {
		try {
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			
			List<String> roles = new ArrayList<>();
			roles.add("ROLE_USER");
			roles.add("ROLE_SUPERVISOR");
			roles.add("ROLE_ADMIN");
			
			List<Users> friends = uService.getChatFriends(null, roles, currentPage, itemsPerPage, keyword, user.getId());
			byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
			ImgData defaultThum = null;
			String ext;
			
			for (Users users : friends) {
				if(users.getImgData() == null || users.getImgData().getImgContent() == null) {
					if(defaultThum == null) {
						LOG.debug("Create Default Thumbnail.");
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
			List<Users> friends = uService.getChatFriends(null, roles, 1, 10000, null, null);
			
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
								LOG.debug("Create Default Thumbnail.");
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
	
	public ChattingCriteriaResp getChatMsg(String id, int tab) throws Exception {
		try {
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			
			ChattingCriteriaResp resp = new ChattingCriteriaResp();
			List<Map> messages = null;
			Criteria criteria;
			Query query;
			Map chatting;
			
			if(tab != 1) {
				//--: ID list
				List<Object> ids = new ArrayList<>();
				ids.add(new ObjectId(id));
				ids.add(new ObjectId(user.getId()));
				
				//--: reverse order ID list
				List<Object> idsReOrder = new ArrayList<>();
				idsReOrder.add(new ObjectId(user.getId()));
				idsReOrder.add(new ObjectId(id));
				
				criteria = new Criteria();
				criteria.orOperator(Criteria.where("members").is(ids), Criteria.where("members").is(idsReOrder));
				
				query = Query.query(criteria);
				chatting = templateCore.findOne(query, Map.class, "chatting");
				if(chatting == null) return resp;
				
				id = chatting.get("_id").toString();
			}
			criteria = Criteria.where("chatting_id").in(new ObjectId(id));
			query = Query.query(criteria)
			.with(new Sort("createdDateTime"));
			messages = templateCore.find(query, Map.class, "chatting_message");
			if(messages.size() == 0) return resp;
			
			
			List<String> roles = new ArrayList<>();
			roles.add("ROLE_USER");
			roles.add("ROLE_SUPERVISOR");
			roles.add("ROLE_ADMIN");
			
			List<Users> friends = uService.getChatFriends(null, roles, 1, 10000, null, null);
			byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
			Map<String, ImgData> mapImg = new HashMap<>();
			ImgData defaultThum = null;
			String ext;
			
			for (Map map : messages) {
				if(map.get("author").toString().equals(user.getId())) {
					map.put("isMe", true);
					continue;
				}
				
				for (Users u : friends) {
					if(!map.get("author").toString().equals(u.getId())) continue;
					
					if(u.getImgData() == null || u.getImgData().getImgContent() == null) {
						if(defaultThum == null) {
							LOG.debug("Create Default Thumbnail.");
							defaultThum = new ImgData();
							defaultThum.setImgContent(compressImg(defaultThumbnail, "png"));
						}
						
						if(!mapImg.containsKey(u.getId())) {
							mapImg.put(u.getId(), defaultThum);
						}
					} else {
						ext = FilenameUtils.getExtension(u.getImgData().getImgName());
						u.getImgData().setImgContent(compressImg(u.getImgData().getImgContent(), ext));
						
						if(!mapImg.containsKey(u.getId())) {
							mapImg.put(u.getId(), u.getImgData());
						}
					}
				}
			}
			
			resp.setMapData(messages);
			resp.setMapImg(mapImg);
			
			return resp;
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
