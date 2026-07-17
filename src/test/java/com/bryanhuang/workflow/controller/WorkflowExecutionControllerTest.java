package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.response.CreateWorkflowExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.exception.WorkflowNotFoundException;
import com.bryanhuang.workflow.model.JobStatus;
import com.bryanhuang.workflow.service.WorkflowExecutionOrchestratorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowExecutionController.class)
class WorkflowExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowExecutionOrchestratorService workflowExecutionOrchestratorService;

    @Nested
    @DisplayName("createWorkflowExecution()")
    class CreateWorkflowExecutionTests {

        @Test
        @DisplayName("Succeeds when workflowId is found")
        void succeedsWhenWorkflowIdFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();
            var response = new CreateWorkflowExecutionResponse(workflowExecutionId);

            when(workflowExecutionOrchestratorService.createWorkflowExecution(workflowId))
                    .thenReturn(response);

            mockMvc.perform(post("/workflows/{workflowId}/executions", workflowId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.workflowExecutionId")
                            .value(workflowExecutionId.toString()));
        }

        @Test
        @DisplayName("Fails when workflowId is not found")
        void failsWhenWorkflowIdNotFound() throws Exception {
            UUID workflowId = UUID.randomUUID();

            when(workflowExecutionOrchestratorService.createWorkflowExecution(workflowId))
                    .thenThrow(new WorkflowNotFoundException("Workflow not found with id: {workflowId}"));

            mockMvc.perform(post("/workflows/{workflowId}/executions", workflowId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Workflow not found"))
                    .andExpect(jsonPath("$.errors.workflow").isNotEmpty());

        }
    }

    @Nested
    @DisplayName("getExecutionStatus()")
    class GetExecutionStatusTests {

        @Test
        @DisplayName("Succeeds when executionId is found")
        void succeedsWhenExecutionIdFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            var response = WorkflowExecutionResponse.builder()
                    .workflowExecutionId(workflowExecutionId)
                    .workflowId(UUID.randomUUID())
                    .status(JobStatus.READY)
                    .stepStatuses(List.of())
                    .createdAt(Instant.now())
                    .startedAt(null)
                    .completedAt(null)
                    .build();
            when(workflowExecutionOrchestratorService.getWorkflowExecutionStatus(workflowExecutionId))
                    .thenReturn(response);

            mockMvc.perform(get("/workflow-executions/{executionId}", workflowExecutionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workflowExecutionId")
                            .value(workflowExecutionId.toString()))
                    .andExpect(jsonPath("$.status").value(response.getStatus().name()))
                    .andExpect(jsonPath("$.stepStatuses").isEmpty())
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.startedAt").doesNotExist())
                    .andExpect(jsonPath("$.completedAt").doesNotExist());
        }

        @Test
        @DisplayName("Fails when executionId is not found")
        void failsWhenExecutionIdNotFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            when(workflowExecutionOrchestratorService.getWorkflowExecutionStatus(workflowExecutionId))
                    .thenThrow(new WorkflowExecutionNotFoundException("Workflow execution not found with id: {workflowExecutionId}"));

            mockMvc.perform(get("/workflow-executions/{executionId}", workflowExecutionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Workflow execution not found"))
                    .andExpect(jsonPath("$.errors.execution").isNotEmpty());

        }
    }

    @Nested
    @DisplayName("pauseExecution()")
    class pauseExecutionTests {

        @Test
        @DisplayName("Succeeds when executionId is found")
        void succeedsWhenExecutionIdFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            mockMvc.perform(post("/workflow-executions/{executionId}/pause", workflowExecutionId))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Fails when executionId is not found")
        void failsWhenExecutionIdNotFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();

            doThrow(new WorkflowExecutionNotFoundException("Workflow execution not found"))
                    .when(workflowExecutionOrchestratorService)
                    .pauseExecution(workflowExecutionId);

            mockMvc.perform(post("/workflow-executions/{executionId}/pause", workflowExecutionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Workflow execution not found"))
                    .andExpect(jsonPath("$.errors.execution").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("resumeExecution()")
    class resumeExecutionTests {

        @Test
        @DisplayName("Succeeds when executionId is found")
        void succeedsWhenExecutionIdFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            mockMvc.perform(post("/workflow-executions/{executionId}/resume", workflowExecutionId))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Fails when executionId is not found")
        void failsWhenExecutionIdNotFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();

            doThrow(new WorkflowExecutionNotFoundException("Workflow execution not found"))
                    .when(workflowExecutionOrchestratorService)
                    .resumeExecution(workflowExecutionId);

            mockMvc.perform(post("/workflow-executions/{executionId}/resume", workflowExecutionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Workflow execution not found"))
                    .andExpect(jsonPath("$.errors.execution").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("terminateExecution()")
    class terminateExecutionTests {

        @Test
        @DisplayName("Succeeds when executionId is found")
        void succeedsWhenExecutionIdFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();
            mockMvc.perform(post("/workflow-executions/{executionId}/terminate", workflowExecutionId))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("Fails when executionId is not found")
        void failsWhenExecutionIdNotFound() throws Exception {
            UUID workflowExecutionId = UUID.randomUUID();

            doThrow(new WorkflowExecutionNotFoundException("Workflow execution not found"))
                    .when(workflowExecutionOrchestratorService)
                    .terminateExecution(workflowExecutionId);

            mockMvc.perform(post("/workflow-executions/{executionId}/terminate", workflowExecutionId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Workflow execution not found"))
                    .andExpect(jsonPath("$.errors.execution").isNotEmpty());
        }
    }


}
