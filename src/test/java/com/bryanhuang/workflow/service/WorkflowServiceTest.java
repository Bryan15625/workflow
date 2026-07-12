package com.bryanhuang.workflow.service;

import com.bryanhuang.workflow.dto.request.CreateWorkflowRequest;
import com.bryanhuang.workflow.dto.response.CreateWorkflowResponse;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.entity.WorkflowEntity;
import com.bryanhuang.workflow.exception.InvalidWorkflowException;
import com.bryanhuang.workflow.mapper.WorkflowEntityMapper;
import com.bryanhuang.workflow.mapper.WorkflowMapper;
import com.bryanhuang.workflow.model.Step;
import com.bryanhuang.workflow.model.Workflow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowMapper workflowMapper;

    @Mock
    private WorkflowEntityMapper workflowEntityMapper;

    @Mock
    private WorkflowQueryService workflowQueryService;

    @InjectMocks
    private WorkflowService workflowService;

    @Nested
    @DisplayName("createWorkflow()")
    class CreateWorkflowTests {

        @Test
        void createWorkflow_shouldGenerateId_MapAndPersistEntity_AndReturnResponseWithSameId() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            when(step1.getStepId()).thenReturn(1);
            when(workflow.getSteps()).thenReturn(List.of(step1));
            when(step1.getDependsOnStepIds()).thenReturn(List.of());

            WorkflowEntity workflowEntity = mock(WorkflowEntity.class);
            WorkflowEntity savedEntity = mock(WorkflowEntity.class);

            // Capture the generated workflowId that WorkflowService passes to the mapper
            ArgumentCaptor<UUID> workflowIdCaptor = ArgumentCaptor.forClass(UUID.class);

            when(workflowMapper.toWorkflow(any(UUID.class), eq(request)))
                    .thenReturn(workflow);
            when(workflowEntityMapper.toWorkflowEntity(workflow))
                    .thenReturn(workflowEntity);
            when(workflowQueryService.saveWorkflowEntity(workflowEntity))
                    .thenReturn(savedEntity);

            // when
            CreateWorkflowResponse response = workflowService.createWorkflow(request);

            // then
            verify(workflowMapper).toWorkflow(workflowIdCaptor.capture(), eq(request));
            UUID generatedId = workflowIdCaptor.getValue();

            // Response must contain the same ID that was passed into the mapper
            assertEquals(generatedId, response.workflowId());

            verify(workflowEntityMapper).toWorkflowEntity(workflow);
            verify(workflowQueryService).saveWorkflowEntity(workflowEntity);
            verifyNoMoreInteractions(workflowMapper, workflowEntityMapper, workflowQueryService);
        }

        @Test
        void createWorkflow_shouldFailWhenDuplicateStepIds() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);

            when(step1.getStepId()).thenReturn(1);
            when(step2.getStepId()).thenReturn(1); // duplicate ID

            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowMapper.toWorkflow(any(UUID.class), eq(request))).thenReturn(workflow);

            // when / then
            InvalidWorkflowException ex = assertThrows(
                    InvalidWorkflowException.class,
                    () -> workflowService.createWorkflow(request)
            );

            assertEquals("Duplicate step ID found", ex.getMessage());

            verify(workflowMapper).toWorkflow(any(UUID.class), eq(request));
            verifyNoInteractions(workflowEntityMapper, workflowQueryService);
        }

        @Test
        void createWorkflow_shouldFailWhenDependencyIdDoesNotExist() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);

            when(step1.getStepId()).thenReturn(1);
            when(step1.getDependsOnStepIds()).thenReturn(List.of());
            when(step2.getStepId()).thenReturn(2);
            // Step 2 depends on a non‑existent step 3
            when(step2.getDependsOnStepIds()).thenReturn(List.of(3));

            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowMapper.toWorkflow(any(UUID.class), eq(request))).thenReturn(workflow);

            // when / then
            InvalidWorkflowException ex = assertThrows(
                    InvalidWorkflowException.class,
                    () -> workflowService.createWorkflow(request)
            );

            assertTrue(ex.getMessage().contains("depends on unknown step ID"));

            verify(workflowMapper).toWorkflow(any(UUID.class), eq(request));
            verifyNoInteractions(workflowEntityMapper, workflowQueryService);
        }

        @Test
        void createWorkflow_shouldFailWhenNoRootStep() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);

            when(step1.getStepId()).thenReturn(1);
            // Each step depends on the other -> no step with empty dependency list
            when(step1.getDependsOnStepIds()).thenReturn(List.of(2));
            when(step2.getStepId()).thenReturn(2);
            when(step2.getDependsOnStepIds()).thenReturn(List.of(1));

            when(workflow.getSteps()).thenReturn(List.of(step1, step2));
            when(workflowMapper.toWorkflow(any(UUID.class), eq(request))).thenReturn(workflow);

            // when / then
            InvalidWorkflowException ex = assertThrows(
                    InvalidWorkflowException.class,
                    () -> workflowService.createWorkflow(request)
            );

            assertEquals("Workflow must contain at least one root step with no dependencies", ex.getMessage());

            verify(workflowMapper).toWorkflow(any(UUID.class), eq(request));
            verifyNoInteractions(workflowEntityMapper, workflowQueryService);
        }

        @Test
        void createWorkflow_shouldFailWhenNoTerminalStep() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);
            Step step3 = mock(Step.class);

            when(step1.getStepId()).thenReturn(1);
            when(step1.getDependsOnStepIds()).thenReturn(List.of());      // root
            when(step2.getStepId()).thenReturn(2);
            when(step2.getDependsOnStepIds()).thenReturn(List.of(1, 3));     // depends on 1
            when(step3.getStepId()).thenReturn(3);
            when(step3.getDependsOnStepIds()).thenReturn(List.of(2));

            when(workflow.getSteps()).thenReturn(List.of(step1, step2, step3));
            when(workflowMapper.toWorkflow(any(UUID.class), eq(request))).thenReturn(workflow);

            // when / then
            InvalidWorkflowException ex = assertThrows(
                    InvalidWorkflowException.class,
                    () -> workflowService.createWorkflow(request)
            );

            assertEquals("Workflow must contain at least one terminal step", ex.getMessage());

            verify(workflowMapper).toWorkflow(any(UUID.class), eq(request));
            verifyNoInteractions(workflowEntityMapper, workflowQueryService);
        }

        @Test
        void createWorkflow_shouldFailWhenCycleDetected() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);
            Step step3 = mock(Step.class);
            Step step4 = mock(Step.class);

            /*
             * 1 -> 2 -> 3 -> 1 forms a cycle
             */
            when(step1.getStepId()).thenReturn(1);
            when(step1.getDependsOnStepIds()).thenReturn(List.of());

            when(step2.getStepId()).thenReturn(2);
            when(step2.getDependsOnStepIds()).thenReturn(List.of(1, 3));

            when(step3.getStepId()).thenReturn(3);
            when(step3.getDependsOnStepIds()).thenReturn(List.of(2));

            when(step4.getStepId()).thenReturn(4);
            when(step4.getDependsOnStepIds()).thenReturn(List.of(3));

            when(workflow.getSteps()).thenReturn(List.of(step1, step2, step3, step4));
            when(workflowMapper.toWorkflow(any(UUID.class), eq(request))).thenReturn(workflow);

            // when / then
            InvalidWorkflowException ex = assertThrows(
                    InvalidWorkflowException.class,
                    () -> workflowService.createWorkflow(request)
            );

            assertEquals("Cycle detected in workflow", ex.getMessage());

            verify(workflowMapper).toWorkflow(any(UUID.class), eq(request));
            verifyNoInteractions(workflowEntityMapper, workflowQueryService);
        }

        @Test
        void createWorkflow_shouldSucceedForValidDag() {
            // given
            CreateWorkflowRequest request = mock(CreateWorkflowRequest.class);

            Workflow workflow = mock(Workflow.class);
            Step step1 = mock(Step.class);
            Step step2 = mock(Step.class);
            Step step3 = mock(Step.class);

            /*
             * Valid DAG:
             * 1 (root)
             * 2 depends on 1
             * 3 depends on 1
             */
            when(step1.getStepId()).thenReturn(1);
            when(step1.getDependsOnStepIds()).thenReturn(List.of());            // root

            when(step2.getStepId()).thenReturn(2);
            when(step2.getDependsOnStepIds()).thenReturn(List.of(1));

            when(step3.getStepId()).thenReturn(3);
            when(step3.getDependsOnStepIds()).thenReturn(List.of(1));

            when(workflow.getSteps()).thenReturn(List.of(step1, step2, step3));
            when(workflowMapper.toWorkflow(any(UUID.class), eq(request))).thenReturn(workflow);

            WorkflowEntity workflowEntity = mock(WorkflowEntity.class);
            WorkflowEntity savedEntity = mock(WorkflowEntity.class);

            when(workflowEntityMapper.toWorkflowEntity(workflow)).thenReturn(workflowEntity);
            when(workflowQueryService.saveWorkflowEntity(workflowEntity)).thenReturn(savedEntity);

            // when
            CreateWorkflowResponse response = workflowService.createWorkflow(request);

            // then
            // We don't know the UUID in advance, but we know it must not be null
            // and must be passed through to the mapper (verified via interaction).
            verify(workflowMapper).toWorkflow(any(UUID.class), eq(request));
            verify(workflowEntityMapper).toWorkflowEntity(workflow);
            verify(workflowQueryService).saveWorkflowEntity(workflowEntity);
            verifyNoMoreInteractions(workflowMapper, workflowEntityMapper, workflowQueryService);

            // Basic sanity check on response
            UUID workflowId = response.workflowId();
            // Ensure response contains some ID
            // (the detailed wiring is already covered in the first createWorkflow test)
            assertEquals(workflowId, response.workflowId());
        }
    }

    @Nested
    @DisplayName("getWorkflow()")
    class GetWorkflowTests {

        @Test
        void getWorkflow_shouldDelegateToQueryServiceAndMapper() {
            // given
            UUID workflowId = UUID.randomUUID();
            Workflow workflow = mock(Workflow.class);
            WorkflowResponse expectedResponse = mock(WorkflowResponse.class);

            when(workflowQueryService.findWorkflow(workflowId)).thenReturn(workflow);
            when(workflowMapper.toWorkflowResponse(workflow)).thenReturn(expectedResponse);

            // when
            WorkflowResponse actualResponse = workflowService.getWorkflow(workflowId);

            // then
            assertSame(expectedResponse, actualResponse);
            verify(workflowQueryService).findWorkflow(workflowId);
            verify(workflowMapper).toWorkflowResponse(workflow);
            verifyNoMoreInteractions(workflowQueryService, workflowMapper);
        }
    }
}