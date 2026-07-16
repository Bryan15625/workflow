package com.bryanhuang.workflow.kafka.producer;

import com.bryanhuang.workflow.event.EventEnvelope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaEventPublisher kafkaEventPublisher;

    @Test
    void publish_shouldSendEventToWorkflowEventsTopic() {
        // given
        String key = "some-key";
        @SuppressWarnings("unchecked")
        EventEnvelope<String> event = mock(EventEnvelope.class);

        // when
        kafkaEventPublisher.publish(key, event);

        // then
        verify(kafkaTemplate).send(
                "workflow-events",
                key,
                event
        );
        verifyNoMoreInteractions(kafkaTemplate);
    }
}