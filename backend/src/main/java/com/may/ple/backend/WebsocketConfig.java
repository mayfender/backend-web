package com.may.ple.backend;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
	private static final Logger LOG = Logger.getLogger(WebsocketConfig.class.getName());

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
		LOG.info("configureMessageBroker");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry
		.addEndpoint("/websocketHandler")
		.setAllowedOrigins("*")
//		.addInterceptors(new StompHandshakeInterceptor())
		.withSockJS();
		LOG.info("registerStompEndpoints");
	}

}