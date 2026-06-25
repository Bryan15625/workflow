package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.dto.response.CreateWorkflowExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.EventType;
import com.bryanhuang.workflow.event.execution.WorkflowExecutionEvent;
import com.bryanhuang.workflow.kafka.producer.KafkaEventPublisher;
import com.bryanhuang.workflow.mapper.WorkflowExecutionEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowExecutionMapper;
import com.bryanhuang.workflow.model.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionService {

    private final WorkflowExecutionEntityMapper workflowExecutionEntityMapper;
    private final WorkflowQueryService workflowQueryService;
    private final WorkflowExecutionMapper workflowExecutionMapper;
    private final KafkaEventPublisher publisher;
    private final TaskExecutionService taskExecutionService;

    /**
     * Save workflow execution to postgres and then publish to kafka
     * @param workflowId
     * @return
     */
    public CreateWorkflowExecutionResponse createWorkflowExecution(UUID workflowId) {

        WorkflowExecution workflowExecution = buildWorkflowExecutionObject(workflowId);

        WorkflowExecutionEntity entity = workflowExecutionEntityMapper
                .toWorkflowExecutionEntity(workflowExecution);

        WorkflowExecutionEntity saved = workflowQueryService
                .saveWorkflowExecutionEntity(entity);

        WorkflowExecutionEvent payload = WorkflowExecutionEvent.builder()
                .workflowExecutionId(saved.getWorkflowExecutionId())
                .workflowId(workflowId)
                .build();

        EventEnvelope<WorkflowExecutionEvent> event = EventEnvelope.<WorkflowExecutionEvent>builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.WORKFLOW_EXECUTION_CREATED)
                .timestamp(Instant.now())
                .payload(payload)
                .build();

        publisher.publish(
                saved.getWorkflowExecutionId().toString(),
                event
        );

        return new CreateWorkflowExecutionResponse(saved.getWorkflowExecutionId());
    }

    public WorkflowExecutionResponse getWorkflowExecutionStatus(UUID executionId) {

        WorkflowExecution workflowExecution = workflowQueryService.findWorkflowExecution(executionId);
        return workflowExecutionMapper.toWorkflowExecutionResponse(workflowExecution);
    }

    public WorkflowExecution buildWorkflowExecutionObject(UUID workflowId) {
        UUID executionId = UUID.randomUUID();
        List<Step> steps = workflowQueryService.findWorkflow(workflowId).getSteps();

        List<StepExecutionStatus> stepStatuses = steps.stream()
                .map(step -> StepExecutionStatus.builder()
                        .stepId(step.getStepId())
                        .stepName(step.getStepName())
                        .status(JobStatus.READY)
                        .build())
                .toList();

        return WorkflowExecution.builder()
                .workflowExecutionId(executionId)
                .workflowId(workflowId)
                .status(JobStatus.READY)
                .stepStatuses(stepStatuses)
                .build();
    }

    @Transactional
    public void onCreated(WorkflowExecutionEvent event) {
        log.info("Change status to RUNNING for workflowExecutionId={}", event.getWorkflowExecutionId());
        WorkflowExecutionEntity entity = workflowQueryService
                .getWorkflowExecutionEntityById(event.getWorkflowExecutionId());
        // Use sleep to see the transition from READY to RUNNING
        try {
            Thread.sleep(32000);
        } catch (InterruptedException e) {}
        entity.start();
        taskExecutionService.executeWorkflow(entity);

    }


}
