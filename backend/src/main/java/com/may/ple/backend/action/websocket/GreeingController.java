package com.may.ple.backend.action.websocket;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GreeingController {

	@MessageMapping("/greeting")
	public String handle(Message<?> greeting, Principal p) {
		System.out.println(p);
		return "[" + greeting + "]";
	}
	
	@MessageMapping("/may")
	public String may(String str) {
		System.out.println(str);
		return "[" + str + "]";
	}

}
