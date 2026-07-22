package com.bryanhuang.workflow.service;

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
import com.bryanhuang.workflow.model.*;
import com.bryanhuang.workflow.redis.service.WorkflowExecutionRedisService;
import com.bryanhuang.workflow.repository.StepExecutionStatusRepository;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionOrchestrationServiceTest {

    @Mock
    private WorkflowExecutionEntityMapper workflowExecutionEntityMapper;

    @Mock
    private WorkflowQueryService workflowQueryService;

    @Mock
    private WorkflowExecutionMapper workflowExecutionMapper;

    @Mock
    private StepExecutionStatusEntityMapper stepExecutionStatusEntityMapper;

    @Mock
    private KafkaEventPublisher publisher;

    @Mock
    private WorkflowExecutionRepository workflowExecutionRepository;

    @Mock
    private StepExecutionStatusRepository stepExecutionStatusRepository;

    @Mock
    private WorkflowExecutionRedisService workflowExecutionRedisService;

    @Mock
    private WorkflowExecutionWorker workflowExecutionWorker;

    @InjectMocks
    private WorkflowExecutionOrchestratorService orchestratorService;

    @Nested
    @DisplayName("createWorkflowExecution()")
    class CreateWorkflowExecutionTests {

        @Test
        @DisplayName("builds execution + step statuses, persists them, publishes event, and returns executionId")
        void createWorkflowExecution_happyPath() {
            UUID workflowId = UUID.randomUUID();
            UUID generatedExecutionId = UUID.randomUUID();

            // Domain workflow with 2 steps
            Step step1 = Step.builder().stepId(1).dependsOnStepIds(List.of()).build();
            Step step2 = Step.builder().stepId(2).dependsOnStepIds(List.of(1)).build();
            Workflow workflow = Workflow.builder()
                    .workflowId(workflowId)
                    .steps(List.of(step1, step2))
                    .build();

            // Returned by query service
            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            // WorkflowExecution built by orchestrator
            ArgumentCaptor<WorkflowExecution> executionCaptor =
                    ArgumentCaptor.forClass(WorkflowExecution.class);
            WorkflowExecutionEntity executionEntity = mock(WorkflowExecutionEntity.class);
            when(executionEntity.getWorkflowExecutionId()).thenReturn(generatedExecutionId);

            when(workflowExecutionEntityMapper.toWorkflowExecutionEntity(executionCaptor.capture()))
                    .thenReturn(executionEntity);

            // Persist execution entity
            when(workflowExecutionRepository.save(executionEntity))
                    .thenReturn(executionEntity);

            // Map step statuses to entities
            StepExecutionStatus status1 = StepExecutionStatus.builder()
                    .id(UUID.randomUUID())
                    .stepId(1)
                    .status(JobStatus.READY)
                    .dependsOnStepIds(List.of())
                    .build();
            StepExecutionStatus status2 = StepExecutionStatus.builder()
                    .id(UUID.randomUUID())
                    .stepId(2)
                    .status(JobStatus.READY)
                    .dependsOnStepIds(List.of(1))
                    .build();

            // Let buildStepExecutionStatus run its real code; or stub it explicitly.
            // Here we stub the mapper result directly.
            StepExecutionStatusEntity entity1 = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity entity2 = mock(StepExecutionStatusEntity.class);
            when(stepExecutionStatusEntityMapper
                    .toStepExecutionStatusEntity(any(StepExecutionStatus.class), eq(executionEntity)))
                    .thenReturn(entity1, entity2);

            // SaveAll returns the same iterable (we don't care about exact value)
            when(stepExecutionStatusRepository.saveAll(anyIterable()))
                    .thenReturn(List.of(entity1, entity2));

            // Act
            CreateWorkflowExecutionResponse response =
                    orchestratorService.createWorkflowExecution(workflowId);

            // Assert response contains executionId from saved entity
            assertNotNull(response);
            assertEquals(generatedExecutionId, response.workflowExecutionId());

            // Verify execution was built and persisted
            WorkflowExecution capturedExecution = executionCaptor.getValue();
            assertNotNull(capturedExecution.getWorkflowExecutionId());
            assertEquals(workflowId, capturedExecution.getWorkflowId());
            assertEquals(JobStatus.READY, capturedExecution.getStatus());

            InOrder inOrder = inOrder(
                    workflowQueryService,
                    workflowExecutionEntityMapper,
                    stepExecutionStatusEntityMapper,
                    workflowExecutionRepository,
                    stepExecutionStatusRepository,
                    publisher
            );

            inOrder.verify(workflowQueryService).findWorkflowEntityAndMapToWorkflow(workflowId);
            inOrder.verify(workflowExecutionEntityMapper).toWorkflowExecutionEntity(capturedExecution);
            inOrder.verify(stepExecutionStatusEntityMapper, times(2))
                    .toStepExecutionStatusEntity(any(StepExecutionStatus.class), eq(executionEntity));
            inOrder.verify(workflowExecutionRepository).save(executionEntity);
            inOrder.verify(stepExecutionStatusRepository).saveAll(anyIterable());

            // Verify event publishing
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<EventEnvelope<WorkflowExecutionEvent>> eventCaptor =
                    ArgumentCaptor.forClass((Class) EventEnvelope.class);

            verify(publisher).publish(keyCaptor.capture(), eventCaptor.capture());
            assertEquals(generatedExecutionId.toString(), keyCaptor.getValue());

            EventEnvelope<WorkflowExecutionEvent> envelope = eventCaptor.getValue();
            assertEquals(EventType.WORKFLOW_EXECUTION_CREATED, envelope.getEventType());
            assertNotNull(envelope.getEventId());
            assertNotNull(envelope.getTimestamp());
            assertEquals(generatedExecutionId, envelope.getPayload().getWorkflowExecutionId());
            assertEquals(workflowId, envelope.getPayload().getWorkflowId());
        }
    }

    @Nested
    @DisplayName("getWorkflowExecutionStatus()")
    class GetWorkflowExecutionStatusTests {

        @Test
        @DisplayName("loads execution + step statuses and maps to response")
        void getWorkflowExecutionStatus_happyPath() {
            UUID executionId = UUID.randomUUID();

            WorkflowExecutionEntity executionEntity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository.findById(executionId))
                    .thenReturn(Optional.of(executionEntity));

            StepExecutionStatusEntity stepEntity1 = mock(StepExecutionStatusEntity.class);
            StepExecutionStatusEntity stepEntity2 = mock(StepExecutionStatusEntity.class);
            when(stepExecutionStatusRepository
                    .findByWorkflowExecutionEntityOrderByStepIdAsc(executionEntity))
                    .thenReturn(List.of(stepEntity1, stepEntity2));

            WorkflowExecution execution = WorkflowExecution.builder()
                    .workflowExecutionId(executionId)
                    .workflowId(UUID.randomUUID())
                    .status(JobStatus.RUNNING)
                    .build();
            when(workflowExecutionEntityMapper.toWorkflowExecution(executionEntity))
                    .thenReturn(execution);

            StepExecutionStatus status1 = StepExecutionStatus.builder().stepId(1).build();
            StepExecutionStatus status2 = StepExecutionStatus.builder().stepId(2).build();
            when(stepExecutionStatusEntityMapper.toStepExecutionStatus(stepEntity1))
                    .thenReturn(status1);
            when(stepExecutionStatusEntityMapper.toStepExecutionStatus(stepEntity2))
                    .thenReturn(status2);

            WorkflowExecutionResponse expected = mock(WorkflowExecutionResponse.class);
            when(workflowExecutionMapper.toWorkflowExecutionResponse(execution, List.of(status1, status2)))
                    .thenReturn(expected);

            // Act
            WorkflowExecutionResponse actual =
                    orchestratorService.getWorkflowExecutionStatus(executionId);

            // Assert
            assertSame(expected, actual);

            verify(workflowExecutionRepository).findById(executionId);
            verify(stepExecutionStatusRepository)
                    .findByWorkflowExecutionEntityOrderByStepIdAsc(executionEntity);
            verify(workflowExecutionEntityMapper).toWorkflowExecution(executionEntity);
            verify(stepExecutionStatusEntityMapper).toStepExecutionStatus(stepEntity1);
            verify(stepExecutionStatusEntityMapper).toStepExecutionStatus(stepEntity2);
            verify(workflowExecutionMapper)
                    .toWorkflowExecutionResponse(execution, List.of(status1, status2));
            verifyNoMoreInteractions(
                    workflowExecutionRepository,
                    stepExecutionStatusRepository,
                    workflowExecutionEntityMapper,
                    stepExecutionStatusEntityMapper,
                    workflowExecutionMapper
            );
        }
    }

    @Nested
    @DisplayName("buildWorkflowExecutionObject()")
    class BuildWorkflowExecutionObjectTests {

        @Test
        @DisplayName("creates a READY execution with non-null executionId and given workflowId")
        void buildWorkflowExecutionObject_createsReadyExecution() {
            UUID workflowId = UUID.randomUUID();

            WorkflowExecution execution =
                    orchestratorService.buildWorkflowExecutionObject(workflowId);

            assertNotNull(execution);
            assertNotNull(execution.getWorkflowExecutionId());
            assertEquals(workflowId, execution.getWorkflowId());
            assertEquals(JobStatus.READY, execution.getStatus());
        }
    }

    @Nested
    @DisplayName("buildStepExecutionStatus()")
    class BuildStepExecutionStatusTests {

        @Test
        @DisplayName("builds one READY step status per workflow step")
        void buildStepExecutionStatus_buildsFromWorkflowSteps() {
            UUID workflowId = UUID.randomUUID();

            Step step1 = Step.builder()
                    .stepId(1)
                    .stepName(StepName.DETECT_ISSUES)
                    .dependsOnStepIds(List.of())
                    .build();
            Step step2 = Step.builder()
                    .stepId(2)
                    .stepName(StepName.PARSE_CSV)
                    .dependsOnStepIds(List.of(1))
                    .build();
            Workflow workflow = Workflow.builder()
                    .workflowId(workflowId)
                    .steps(List.of(step1, step2))
                    .build();

            when(workflowQueryService.findWorkflowEntityAndMapToWorkflow(workflowId))
                    .thenReturn(workflow);

            List<StepExecutionStatus> statuses =
                    orchestratorService.buildStepExecutionStatus(workflowId);

            assertEquals(2, statuses.size());
            StepExecutionStatus s1 = statuses.get(0);
            StepExecutionStatus s2 = statuses.get(1);

            assertNotNull(s1.getId());
            assertEquals(step1.getStepId(), s1.getStepId());
            assertEquals(step1.getStepName(), s1.getStepName());
            assertEquals(step1.getDependsOnStepIds(), s1.getDependsOnStepIds());
            assertEquals(JobStatus.READY, s1.getStatus());

            assertNotNull(s2.getId());
            assertEquals(step2.getStepId(), s2.getStepId());
            assertEquals(step2.getStepName(), s2.getStepName());
            assertEquals(step2.getDependsOnStepIds(), s2.getDependsOnStepIds());
            assertEquals(JobStatus.READY, s2.getStatus());
        }
    }

    @Nested
    @DisplayName("onCreated()")
    class OnCreatedTests {

        @Test
        @DisplayName("delegates to WorkflowExecutionWorker.executeWorkflow")
        void onCreated_delegatesToWorker() {
            UUID executionId = UUID.randomUUID();
            UUID workflowId = UUID.randomUUID();

            WorkflowExecutionEvent event = WorkflowExecutionEvent.builder()
                    .workflowExecutionId(executionId)
                    .workflowId(workflowId)
                    .build();

            orchestratorService.onCreated(event);

            verify(workflowExecutionWorker).executeWorkflow(executionId);
            verifyNoMoreInteractions(workflowExecutionWorker);
        }
    }

    @Nested
    @DisplayName("pause/resume/terminate execution")
    class ControlExecutionTests {

        @Test
        @DisplayName("pauseExecution checks entity exists and sets PAUSE control")
        void pauseExecution_setsPauseControl() {
            UUID executionId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository.findById(executionId))
                    .thenReturn(Optional.of(entity));

            orchestratorService.pauseExecution(executionId);

            verify(workflowExecutionRepository).findById(executionId);
            verify(workflowExecutionRedisService)
                    .setControl(executionId, JobControl.PAUSE);
            verifyNoMoreInteractions(workflowExecutionRepository, workflowExecutionRedisService);
        }

        @Test
        @DisplayName("resumeExecution checks entity exists and sets RESUME control")
        void resumeExecution_setsResumeControl() {
            UUID executionId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository.findById(executionId))
                    .thenReturn(Optional.of(entity));

            orchestratorService.resumeExecution(executionId);

            verify(workflowExecutionRepository).findById(executionId);
            verify(workflowExecutionRedisService)
                    .setControl(executionId, JobControl.RESUME);
            verifyNoMoreInteractions(workflowExecutionRepository, workflowExecutionRedisService);
        }

        @Test
        @DisplayName("terminateExecution checks entity exists and sets TERMINATE control")
        void terminateExecution_setsTerminateControl() {
            UUID executionId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository.findById(executionId))
                    .thenReturn(Optional.of(entity));

            orchestratorService.terminateExecution(executionId);

            verify(workflowExecutionRepository).findById(executionId);
            verify(workflowExecutionRedisService)
                    .setControl(executionId, JobControl.TERMINATE);
            verifyNoMoreInteractions(workflowExecutionRepository, workflowExecutionRedisService);
        }

        @Test
        @DisplayName("terminateExecution checks entity exists, does not exist so TERMINATE not set")
        void terminateExecution_terminateControlNotSetIfEntityNotFound() {
            UUID executionId = UUID.randomUUID();

            when(workflowExecutionRepository.findById(executionId))
                    .thenReturn(Optional.empty());

            WorkflowExecutionNotFoundException ex = assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> orchestratorService.terminateExecution(executionId)
            );

            assertEquals( "Workflow execution not found with id: " + executionId, ex.getMessage());
            verify(workflowExecutionRepository).findById(executionId);

            verifyNoInteractions(workflowExecutionRedisService);
            verifyNoMoreInteractions(workflowExecutionRepository, workflowExecutionRedisService);
        }
    }
}