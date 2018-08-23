package com.may.ple.backend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;

import com.ibm.icu.util.Calendar;
import com.may.ple.backend.criteria.ChattingCriteriaReq;
import com.may.ple.backend.criteria.ChattingCriteriaResp;
import com.may.ple.backend.custom.CustomAggregationOperation;
import com.may.ple.backend.entity.Chatting;
import com.may.ple.backend.entity.ImgData;
import com.may.ple.backend.entity.Users;
import com.may.ple.backend.utils.ContextDetailUtil;
import com.may.ple.backend.utils.ImageUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

@Service
public class ChattingService {
	private static final Logger LOG = Logger.getLogger(ChattingService.class.getName());
	private MongoTemplate templateCore;
	private UserService uService;
	private ServletContext servletContext;
	private JWebsocketService jwsService;
	
	@Autowired	
	public ChattingService(MongoTemplate templateCore, UserService uService, ServletContext servletContext, JWebsocketService jwsService) {
		this.templateCore = templateCore;
		this.uService = uService;
		this.servletContext = servletContext;
		this.jwsService = jwsService;
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
			query.fields().include("members").include("lastMsg").include("updatedDateTime");
			
			List<Map> chatting = templateCore.find(query, Map.class, "chatting");
			if(chatting.size() == 0) return chatting;
			
			List<Users> friends = uService.getChatFriends(null, null, 1, 10000, null, null);
			
			byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
			List<String> friendChkStatus = new ArrayList<>();
			ImgData defaultThum = null;
			List<ObjectId> members;
			long unRead;
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
						
						query = Query.query(Criteria.where("chattingId").is(map.get("_id")).and("author").is(objId).and("read").nin(new ObjectId(user.getId())));
						unRead = templateCore.count(query, "chatting_message");
						if(unRead > 0) {
							map.put("unRead", unRead);
						}
						
						map.put("showname", fri.getShowname());
						map.put("username", fri.getUsername());
						map.put("firstName", fri.getFirstName());
						map.put("lastName", fri.getLastName());
						map.put("imgData", fri.getImgData());
						
						friendChkStatus.add(fri.getUsername());
						break;
					}
				}
			}
			
			LOG.info("Check Status with jwebsocket.");
			jwsService.checkStatus(friendChkStatus, user.getUsername());
			
			return chatting;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ChattingCriteriaResp getChatMsg(String chattingId, String friendId) throws Exception {
		try {
			Users user = ContextDetailUtil.getCurrentUser(templateCore);
			
			ChattingCriteriaResp resp = new ChattingCriteriaResp();
			List<Map> messages = null;
			Criteria criteria;
			Query query;
			
			if(!StringUtils.isBlank(friendId)) {
				//--
				chattingId = isChattingExit(friendId, user.getId());
				if(chattingId == null) return resp;
			}
			resp.setChattingId(chattingId);
			
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -5);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			Date dateBefore15Days = cal.getTime();
			
			criteria = Criteria.where("chattingId").in(new ObjectId(chattingId)).and("createdDateTime").gte(dateBefore15Days);
			BasicDBList idLst = new BasicDBList();
			idLst.add(new ObjectId(user.getId()));
			
			BasicDBList subSet = new BasicDBList();
			subSet.add(idLst);
			subSet.add("$read");
			
			BasicDBObject param = new BasicDBObject("isRead", new BasicDBObject("$setIsSubset", subSet));
			param.append("author", 1);
			param.append("createdDateTime", 1);
			param.append("body", 1);
			param.append("readCount", new BasicDBObject("$size", "$read"));
			
			Aggregation agg = Aggregation.newAggregation(			
					Aggregation.match(criteria),
					new CustomAggregationOperation(
					        new BasicDBObject(
					            "$project",
					            param
					        )
						)
			);
			
			AggregationResults<Map> aggregate = templateCore.aggregate(agg, "chatting_message", Map.class);
			messages = aggregate.getMappedResults();
			
			if(messages.size() == 0) return resp;
			
			//--
			List<Users> friends = uService.getChatFriends(null, null, 1, 10000, null, null);
			byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
			Map<String, ImgData> mapImg = new HashMap<>();
			String ext, dateFormat = "%1$td %1$tb";
			boolean ignoreChkRead = false;
			Date createdDateTime = null;
			ImgData defaultThum = null;
			List<ObjectId> ids;
			
			for (Map map : messages) {
				if(createdDateTime == null) {
					map.put("dateLabel", String.format(new Locale("th"), dateFormat, map.get("createdDateTime")));
				} else {
					if(!DateUtils.isSameDay(createdDateTime, (Date)map.get("createdDateTime"))) {
						map.put("dateLabel", String.format(new Locale("th"), dateFormat, map.get("createdDateTime")));
					}
				}
				createdDateTime =(Date)map.get("createdDateTime");
				
				if(map.get("author").toString().equals(user.getId())) {
					map.put("isMe", true);
					continue;
				}
				
				 if(!ignoreChkRead) {
					if(!Boolean.valueOf(map.get("isRead").toString())) {
						map.put("dateLabel", "ข้อความที่ยังไม่อ่าน");
						ignoreChkRead = true;
					}
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
			
			//--
			Update update = new Update();
			update.push("read", new ObjectId(user.getId()));
			query = Query.query(Criteria.where("chattingId").in(new ObjectId(chattingId)).and("author").ne(new ObjectId(user.getId())).and("read").nin(new ObjectId(user.getId())));
			templateCore.updateMulti(query, update, "chatting_message");
			
			resp.setMapData(messages);
			resp.setMapImg(mapImg);
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public ChattingCriteriaResp sendMsg(ChattingCriteriaReq req) throws Exception {
		try {
			ChattingCriteriaResp resp = new ChattingCriteriaResp();
			Users sender = ContextDetailUtil.getCurrentUser(templateCore);
			Date now = Calendar.getInstance().getTime();
			boolean isUpdate = true;
			
			if(StringUtils.isBlank(req.getChattingId())) {
				String chattingId = isChattingExit(req.getFriendId(), sender.getId());
				
				if(chattingId == null) {
					LOG.info("Create new chatting");
					List<ObjectId> members = new ArrayList<>();
					members.add(new ObjectId(sender.getId()));
					members.add(new ObjectId(req.getFriendId()));
					
					Chatting chatting = new Chatting();
					chatting.setCreatedDateTime(now);
					chatting.setUpdatedDateTime(now);
					chatting.setMembers(members);
					chatting.setLastMsg(req.getMessage());
					templateCore.save(chatting);
					
					//--
					DBCollection collection = templateCore.getCollection("chatting");
					collection.createIndex(new BasicDBObject("createdDateTime", 1));
					collection.createIndex(new BasicDBObject("updatedDateTime", 1));
					
					//--
					req.setChattingId(chatting.getId());
					isUpdate = false;
				} else {
					req.setChattingId(chattingId);
				}
			} 
			
			if(isUpdate) {
				Update update = new Update();
				update.set("updatedDateTime", now);
				update.set("lastMsg", req.getMessage());
				templateCore.updateFirst(Query.query(Criteria.where("_id").is(new ObjectId(req.getChattingId()))), update, "chatting");	
			}
			
			Map<String, Object> chattingMsg = new HashMap<>();
			chattingMsg.put("createdDateTime", now);
			chattingMsg.put("author", new ObjectId(sender.getId()));
			chattingMsg.put("body", req.getMessage());
			chattingMsg.put("chattingId", new ObjectId(req.getChattingId()));
			chattingMsg.put("read", new ArrayList());
			templateCore.save(chattingMsg, "chatting_message");
			
			//--
			DBCollection collection = templateCore.getCollection("chatting_message");
			collection.createIndex(new BasicDBObject("createdDateTime", 1));
			collection.createIndex(new BasicDBObject("chattingId", 1));
			collection.createIndex(new BasicDBObject("author", 1));
			
			//--
			resp.setChattingId(req.getChattingId());
			resp.setCreatedDateTime(now);
			
			LOG.info("Sent message to JWS");
			Map chatting = templateCore.findOne(Query.query(Criteria.where("_id").is(new ObjectId(req.getChattingId()))), Map.class, "chatting");
			List<ObjectId> members = (List)chatting.get("members");
			
			for (Object id : members) {
				if(id.toString().equals(sender.getId())) continue;
								
				Users receiver = uService.getUserById(id.toString(), "username");	
				jwsService.sendMsg(receiver.getUsername(), req.getMessage(), sender.getId(), req.getChattingId());
				break;
			}
			
			return resp;
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	public String getThumbnail(String userId) throws Exception {
		try {
			Users user = uService.getUserById(userId, "imgData");
			ImgData imgData = user.getImgData();
			
			if(imgData == null || imgData.getImgContent() == null) {
				byte[] defaultThumbnail = ImageUtil.getDefaultThumbnail(servletContext);
				return new String(Base64.encode(compressImg(defaultThumbnail, "png")));
			} else {
				String senderExt = FilenameUtils.getExtension(user.getImgData().getImgName());
				return new String(Base64.encode(compressImg(imgData.getImgContent(), senderExt)));
			}
		} catch (Exception e) {
			LOG.error(e.toString());
			throw e;
		}
	}
	
	private String isChattingExit(String id1, String id2) {
		try {
			//--: ID list
			List<Object> ids = new ArrayList<>();
			ids.add(new ObjectId(id1));
			ids.add(new ObjectId(id2));
			
			//--: reverse order ID list
			List<Object> idsReOrder = new ArrayList<>();
			idsReOrder.add(new ObjectId(id2));
			idsReOrder.add(new ObjectId(id1));
			
			Criteria criteria = new Criteria();
			criteria.orOperator(Criteria.where("members").is(ids), Criteria.where("members").is(idsReOrder));
			Map chatting = templateCore.findOne(Query.query(criteria), Map.class, "chatting");
			
			return chatting == null ? null : chatting.get("_id").toString();
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
