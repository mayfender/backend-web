package com.may.ple.backend.action.websocket;

import java.security.Principal;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;

@Controller
public class GreeingController {

	@MessageMapping("/greeting")
	public String handle(Message<?> greeting, Principal p) {
		System.out.println(p);
		return "[" + greeting + "]";
	}

	@MessageMapping("/may")
	@SendTo("/topic/greetings")
	public String may(String str) {
		System.out.println(str);
		return "[" + str + "]";
	}

	@MessageMapping("/pinNumNotify")
	public String pinNumNotify() {
		String name = new Gson().fromJson("", Map.class).get("name").toString();
		return name;
	}

}
