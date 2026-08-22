package com.bryanhuang.workflow.service.workout;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.ai.chat.model.ChatModel;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AiResponseServiceTest {

    @Mock
    ChatModel chatModel;

    @InjectMocks
    AiResponseService aiResponseService;

    @Nested
    @DisplayName("generateResponse()")
    class GenerateResponseTests {

        @Test
        @DisplayName("delegates the prompt to the chat model")
        void delegatesPromptToChatModel() {
            UUID workflowExecutionId = UUID.randomUUID();
            String prompt = "Generate the report for the workout";

            aiResponseService.generateResponse(workflowExecutionId, prompt);
            verify(chatModel).call(eq(prompt));
        }
    }
}
