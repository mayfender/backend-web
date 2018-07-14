package com.may.ple.backend.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.JWebSocketTokenClient;
import org.jwebsocket.token.MapToken;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javolution.util.FastMap;

@Service
public class JWebsocketService implements WebSocketClientTokenListener {
	private static final Logger LOG = Logger.getLogger(JWebsocketService.class.getName());
	private MongoTemplate template;
	private JWebSocketTokenClient client;
	private int count = 0;
	
	@Autowired	
	public JWebsocketService(MongoTemplate template) {
		this.template = template;
	}
	
	@PostConstruct
	private void init() {
		try {
			LOG.info("Initial jwebsocket...");
			client = new JWebSocketTokenClient();
			client.addTokenClientListener(this);		
			client.open("ws://localhost:8787/jWebSocket/jWebSocket");
			client.login("root", "root");
			LOG.info("jwebsocket successfully initiated.");
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}
	
	public void pushAlert() {
		try {
			LOG.info("Push alert message");
			MapToken token = new MapToken("org.jwebsocket.plugins.debtalert", "getUsers");
			client.sendToken(token);
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}

	@Override
	public void processClosed(WebSocketClientEvent arg0) {
		LOG.info("processClosed");
	}

	@Override
	public void processOpened(WebSocketClientEvent arg0) {
		try {
			LOG.info("Map user with clientID");
			MapToken token = new MapToken("org.jwebsocket.plugins.debtalert", "registerUser");
			token.setString("username", "JWebsocketServer");
			client.sendToken(token);
			LOG.info("Map user success");
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}

	@Override
	public void processOpening(WebSocketClientEvent arg0) {
		LOG.info("processOpening");
	}

	@Override
	public void processPacket(WebSocketClientEvent arg0, WebSocketPacket aPacket) {
		try {
			Token aToken = TokenFactory.packetToToken("json", aPacket);
			
			if(aToken.getNS().equals("org.jwebsocket.plugins.debtalert") && aToken.getType().equals("getUsersResp")) {
				List<String> users = aToken.getList("users");
				if(users.size() == 0) return;
				
				LOG.info("test push message");
				Map<String, Integer> mUser = new HashMap<>();
				mUser.put("sadmin", count++);
				
				FastMap<String, Object> map = new FastMap<String, Object>().shared();
				MapToken token = new MapToken(map);
				token.setMap("users", mUser);
				token.setNS("org.jwebsocket.plugins.debtalert");
				token.setType("alert");
				
				token.setMap(map);
				client.sendToken(token);
			}
		} catch (Exception e) {
			LOG.error(e.toString(), e);
		}
	}

	@Override
	public void processReconnecting(WebSocketClientEvent arg0) {
		LOG.info("processReconnecting");
	}

	@Override
	public void processToken(WebSocketClientEvent arg0, Token arg1) {
		LOG.info("processToken");
	}
	
}
