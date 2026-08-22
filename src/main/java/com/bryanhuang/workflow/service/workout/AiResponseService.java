package com.bryanhuang.workflow.service.workout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiResponseService {

    private final ChatModel chatModel;

    public String generateResponse(UUID workflowExecutionId, String prompt) {
        log.info("Generating response for workflowExecutionId: {}", workflowExecutionId);
        return chatModel.call(prompt);
    }
}