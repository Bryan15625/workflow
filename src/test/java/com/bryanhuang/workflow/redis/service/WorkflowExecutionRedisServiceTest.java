package com.bryanhuang.workflow.redis.service;

import com.bryanhuang.workflow.model.workflow.JobControl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WorkflowExecutionRedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private WorkflowExecutionRedisService service;

    @Nested
    @DisplayName("setControl()")
    class SetControlTests {
        @Test
        @DisplayName("Stores JobControl under expected Redis key")
        void setControl_storesControlWithExpectedKey() {
            UUID workflowExecutionId = UUID.randomUUID();
            JobControl control = JobControl.PAUSE;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            service.setControl(workflowExecutionId, control);

            // Capture key and value passed to Redis
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

            verify(valueOperations).set(keyCaptor.capture(), valueCaptor.capture());

            String expectedKey = "workflow_execution:" + workflowExecutionId + ":control";
            assertThat(keyCaptor.getValue()).isEqualTo(expectedKey);
            assertThat(valueCaptor.getValue()).isEqualTo(control.name());
        }
    }

    @Nested
    @DisplayName("getControl()")
    class GetControlTests {

        @Test
        @DisplayName("Returns JobControl when value is present in Redis")
        void getControl_returnsControlWhenPresent() {
            UUID workflowExecutionId = UUID.randomUUID();
            JobControl expectedControl = JobControl.RESUME;

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            String expectedKey = "workflow_execution:" + workflowExecutionId + ":control";
            when(valueOperations.get(expectedKey)).thenReturn(expectedControl.name());

            JobControl result = service.getControl(workflowExecutionId);

            assertThat(result).isEqualTo(expectedControl);
        }

        @Test
        @DisplayName("Returns null when value is not present in Redis")
        void getControl_returnsNullWhenAbsent() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            String expectedKey = "workflow_execution:" + workflowExecutionId + ":control";
            when(valueOperations.get(expectedKey)).thenReturn(null);

            JobControl result = service.getControl(workflowExecutionId);

            assertThat(result).isNull();
        }
    }
}