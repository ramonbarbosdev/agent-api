package com.agentapi.conversation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.web.ConversationMessageDto;

@RestController
@RequestMapping("/api/agent/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @GetMapping(value = "/{conversationId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ConversationMessageDto> listMessages(@PathVariable UUID conversationId) {
        return conversationService.loadMessagesForClient(conversationId);
    }
}
