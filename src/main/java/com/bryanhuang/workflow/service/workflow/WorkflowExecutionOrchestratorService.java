package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.dto.response.CreateWorkflowExecutionResponse;
import com.bryanhuang.workflow.dto.response.WorkflowExecutionResponse;
import com.bryanhuang.workflow.entity.StepExecutionStatusEntity;
import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.event.EventEnvelope;
import com.bryanhuang.workflow.event.EventType;
import com.bryanhuang.workflow.event.execution.WorkflowExecutionEvent;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.kafka.producer.KafkaEventPublisher;
import com.bryanhuang.workflow.mapper.StepExecutionStatusEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowExecutionEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowExecutionMapper;
import com.bryanhuang.workflow.model.workflow.*;
import com.bryanhuang.workflow.redis.service.WorkflowExecutionRedisService;
import com.bryanhuang.workflow.repository.StepExecutionStatusRepository;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionOrchestratorService {

    private final WorkflowExecutionEntityMapper workflowExecutionEntityMapper;
    private final WorkflowQueryService workflowQueryService;
    private final WorkflowExecutionMapper workflowExecutionMapper;
    private final StepExecutionStatusEntityMapper stepExecutionStatusEntityMapper;
    private final KafkaEventPublisher publisher;
    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final StepExecutionStatusRepository stepExecutionStatusRepository;
    private final WorkflowExecutionRedisService workflowExecutionRedisService;
    private final WorkflowExecutionWorker workflowExecutionWorker;

    public CreateWorkflowExecutionResponse createWorkflowExecution(UUID workflowId) {

        WorkflowExecution workflowExecution = buildWorkflowExecutionObject(workflowId);
        List<StepExecutionStatus> stepStatuses = buildStepExecutionStatus(
                workflowId);

        WorkflowExecutionEntity executionEntity = workflowExecutionEntityMapper
                .toWorkflowExecutionEntity(workflowExecution);
        List<StepExecutionStatusEntity> stepEntities = stepStatuses.stream().map(
                status -> stepExecutionStatusEntityMapper
                        .toStepExecutionStatusEntity(status, executionEntity)
        ).toList();

        WorkflowExecutionEntity savedWorkflowExecutionEntity = workflowExecutionRepository
                .save(executionEntity);
        Iterable<StepExecutionStatusEntity> savedStepExecutionStatusEntity = stepExecutionStatusRepository
                .saveAll(stepEntities);

        WorkflowExecutionEvent payload = WorkflowExecutionEvent.builder()
                .workflowExecutionId(savedWorkflowExecutionEntity.getWorkflowExecutionId())
                .workflowId(workflowId)
                .build();

        EventEnvelope<WorkflowExecutionEvent> event = EventEnvelope.<WorkflowExecutionEvent>builder()
                .eventId(UUID.randomUUID())
                .eventType(EventType.WORKFLOW_EXECUTION_CREATED)
                .timestamp(Instant.now())
                .payload(payload)
                .build();

        publisher.publish(
                savedWorkflowExecutionEntity.getWorkflowExecutionId().toString(),
                event
        );

        return new CreateWorkflowExecutionResponse(savedWorkflowExecutionEntity.getWorkflowExecutionId());
    }

    public WorkflowExecutionResponse getWorkflowExecutionStatus(UUID workflowExecutionId) {

        WorkflowExecutionEntity workflowExecutionEntity = findWorkflowExecutionEntity(workflowExecutionId);
        List<StepExecutionStatusEntity> stepStatusEntities = stepExecutionStatusRepository
                .findByWorkflowExecutionEntityOrderByStepIdAsc(workflowExecutionEntity);

        WorkflowExecution workflowExecution = workflowExecutionEntityMapper
                .toWorkflowExecution(workflowExecutionEntity);
        List<StepExecutionStatus> stepExecutionStatuses = stepStatusEntities.stream().map(
                stepExecutionStatusEntityMapper::toStepExecutionStatus
        ).toList();

        return workflowExecutionMapper.toWorkflowExecutionResponse(workflowExecution, stepExecutionStatuses);
    }

    public WorkflowExecution buildWorkflowExecutionObject(UUID workflowId) {
        UUID executionId = UUID.randomUUID();

        return WorkflowExecution.builder()
                .workflowExecutionId(executionId)
                .workflowId(workflowId)
                .status(JobStatus.READY)
                .build();
    }

    public List<StepExecutionStatus> buildStepExecutionStatus(UUID workflowId) {
        List<Step> steps = workflowQueryService
                .findWorkflowEntityAndMapToWorkflow(workflowId).getSteps();

        return steps.stream()
                .map(step -> StepExecutionStatus.builder()
                        .id(UUID.randomUUID())
                        .stepId(step.getStepId())
                        .stepName(step.getStepName())
                        .status(JobStatus.READY)
                        .startedAt(null)
                        .completedAt(null)
                        .dependsOnStepIds(step.getDependsOnStepIds())
                        .build())
                .toList();
    }

    public void onCreated(WorkflowExecutionEvent event) {
        log.info("Change status to RUNNING for workflowExecutionId={}", event.getWorkflowExecutionId());
        workflowExecutionWorker.executeWorkflow(event.getWorkflowExecutionId());
    }

    public void pauseExecution(UUID workflowExecutionId) {
        findWorkflowExecutionEntity(workflowExecutionId);
        workflowExecutionRedisService.setControl(workflowExecutionId, JobControl.PAUSE);
    }
    public void resumeExecution(UUID workflowExecutionId) {
        findWorkflowExecutionEntity(workflowExecutionId);
        workflowExecutionRedisService.setControl(workflowExecutionId, JobControl.RESUME);
    }
    public void terminateExecution(UUID workflowExecutionId) {
        findWorkflowExecutionEntity(workflowExecutionId);
        workflowExecutionRedisService.setControl(workflowExecutionId, JobControl.TERMINATE);
    }

    private WorkflowExecutionEntity findWorkflowExecutionEntity(UUID workflowExecutionId) {
        return workflowExecutionRepository
                .findById(workflowExecutionId)
                .orElseThrow(() -> new WorkflowExecutionNotFoundException(
                        "Workflow execution not found with id: " + workflowExecutionId
                ));
    }

}
