package tn.esprit.spring.b2u.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class ChatController {

    /**
     * Receive a message from a client and broadcast it to all subscribers of the room
     *
     * @param roomId the room identifier
     * @param message the message content
     * @return the message to broadcast
     */
    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("/topic/messages/{roomId}")
    public Map<String, Object> sendMessage(
            @DestinationVariable String roomId,
            Map<String, Object> message) {

        System.out.println("📨 Message reçu dans la room " + roomId + " : " + message);

        // Add timestamp if not present
        if (!message.containsKey("timestamp")) {
            message.put("timestamp", new java.util.Date().toString());
        }

        // Broadcast the message to all subscribers of this room
        return message;
    }
}