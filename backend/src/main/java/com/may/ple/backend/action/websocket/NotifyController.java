package com.may.ple.backend.action.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.google.gson.Gson;
import com.ibm.icu.util.Calendar;

@Controller
public class NotifyController {
	@Autowired
	private SimpMessageSendingOperations messagingTemplate;

	/*@MessageMapping("/greeting")
	public String handle(Message<?> greeting, Principal p) {
		System.out.println(p);
		return "[" + greeting + "]";
	}

	@MessageMapping("/may")
	@SendTo("/topic/greetings")
	public String may(String str) {
		System.out.println(str);
		return "[" + str + "]";
	}*/

	@MessageMapping("/pinNumNotify")
	public void pinNumNotify(String dealerId, String userName) {
		Map<String, Object> data = new HashMap<>();
		data.put("dateTime", Calendar.getInstance().getTimeInMillis());
		data.put("userName", userName);
		messagingTemplate.convertAndSend("/topic/" + dealerId.substring(dealerId.length() - 3) + "/pinNum", new Gson().toJson(data));
	}

}
