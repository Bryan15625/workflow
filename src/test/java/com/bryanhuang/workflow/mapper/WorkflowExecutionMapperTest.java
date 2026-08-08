package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.dto.StepExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.model.workflow.StepExecutionStatus;
import com.bryanhuang.workflow.model.workflow.StepName;
import com.bryanhuang.workflow.model.workflow.WorkflowExecution;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkflowExecutionMapperTest {

    private final WorkflowExecutionMapper mapper = new WorkflowExecutionMapper();

    @Test
    void toWorkflowExecutionResponse_shouldMapAllFields() {
        UUID workflowExecutionId = UUID.randomUUID();
        UUID workflowId = UUID.randomUUID();
        JobStatus status = JobStatus.RUNNING;
        Instant createdAt = Instant.now().minusSeconds(300);
        Instant startedAt = Instant.now().minusSeconds(200);
        Instant completedAt = Instant.now().minusSeconds(100);

        WorkflowExecution workflowExecution = WorkflowExecution.builder()
                .workflowExecutionId(workflowExecutionId)
                .workflowId(workflowId)
                .status(status)
                .createdAt(createdAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .build();

        UUID stepExecutionId = UUID.randomUUID();

        StepExecutionStatus step = StepExecutionStatus.builder()
                .id(stepExecutionId)
                .stepId(1)
                .stepName(StepName.INGEST_CSV)
                .status(JobStatus.COMPLETED)
                .startedAt(Instant.now().minusSeconds(180))
                .completedAt(Instant.now().minusSeconds(150))
                .dependsOnStepIds(List.of())
                .build();

        List<StepExecutionStatus> stepExecutionStatuses = List.of(step);

        WorkflowExecutionResponse response =
                mapper.toWorkflowExecutionResponse(workflowExecution, stepExecutionStatuses);

        assertEquals(workflowExecutionId, response.getWorkflowExecutionId());
        assertEquals(workflowId, response.getWorkflowId());
        assertEquals(status, response.getStatus());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(startedAt, response.getStartedAt());
        assertEquals(completedAt, response.getCompletedAt());

        List<StepExecutionResponse> stepResponses = response.getStepStatuses();
        assertEquals(1, stepResponses.size());

        StepExecutionResponse stepExecutionResponse = stepResponses.getFirst();
        StepExecutionStatus step1 = stepExecutionStatuses.getFirst();
        assertEquals(step1.getId(), stepExecutionResponse.getStepExecutionId());
        assertEquals(step1.getStepId(), stepExecutionResponse.getStepId());
        assertEquals(step1.getStepName(), stepExecutionResponse.getStepName());
        assertEquals(step1.getStatus(), stepExecutionResponse.getStatus());
        assertEquals(step1.getStartedAt(), stepExecutionResponse.getStartedAt());
        assertEquals(step1.getCompletedAt(), stepExecutionResponse.getCompletedAt());
        assertEquals(step1.getDependsOnStepIds(), stepExecutionResponse.getDependsOnStepIds());

    }
}