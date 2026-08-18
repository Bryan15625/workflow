package com.bryanhuang.workflow.service.workflow;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.exception.WorkflowExecutionNotFoundException;
import com.bryanhuang.workflow.repository.WorkflowExecutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkflowExecutionServiceTest {

    @Mock
    private WorkflowExecutionRepository workflowExecutionRepository;

    @InjectMocks
    private WorkflowExecutionService workflowExecutionService;

    @Nested
    @DisplayName("start()")
    class StartTests {

        @Test
        @DisplayName("should call start() on found workflow execution")
        void startCallsEntityStartWhenExecutionFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(entity));

            workflowExecutionService.start(workflowExecutionId);

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
            verify(entity).start();
            verifyNoMoreInteractions(entity);
        }

        @Test
        @DisplayName("should throw WorkflowExecutionStatusNotFoundException when workflow execution not found")
        void startThrowsWhenExecutionNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> workflowExecutionService.start(workflowExecutionId)
            );

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("complete()")
    class CompleteTests {

        @Test
        @DisplayName("should call complete() on found workflow execution")
        void completeCallsEntityCompleteWhenExecutionFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(entity));

            workflowExecutionService.complete(workflowExecutionId);

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
            verify(entity).complete();
            verifyNoMoreInteractions(entity);
        }

        @Test
        @DisplayName("should throw WorkflowExecutionStatusNotFoundException when workflow execution not found")
        void completeThrowsWhenExecutionNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> workflowExecutionService.complete(workflowExecutionId)
            );

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("fail()")
    class FailTests {

        @Test
        @DisplayName("should call fail() on found workflow execution")
        void failCallsEntityFailWhenExecutionFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            String errorMessage = "error message";

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(entity));

            workflowExecutionService.fail(workflowExecutionId, errorMessage);

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
            verify(entity).fail(errorMessage);
            verifyNoMoreInteractions(entity);
        }

        @Test
        @DisplayName("should throw WorkflowExecutionStatusNotFoundException when workflow execution not found")
        void failThrowsWhenExecutionNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            String errorMessage = "error message";

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> workflowExecutionService.fail(workflowExecutionId, errorMessage)
            );

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("pause()")
    class PauseTests {

        @Test
        @DisplayName("should call pause() on found workflow execution")
        void pauseCallsEntityPauseWhenExecutionFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(entity));

            workflowExecutionService.pause(workflowExecutionId);

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
            verify(entity).pause();
            verifyNoMoreInteractions(entity);
        }

        @Test
        @DisplayName("should throw WorkflowExecutionStatusNotFoundException when workflow execution not found")
        void pauseThrowsWhenExecutionNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> workflowExecutionService.pause(workflowExecutionId)
            );

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("resume()")
    class ResumeTests {

        @Test
        @DisplayName("should call resume() on found workflow execution")
        void resumeCallsEntityResumeWhenExecutionFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(entity));

            workflowExecutionService.resume(workflowExecutionId);

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
            verify(entity).resume();
            verifyNoMoreInteractions(entity);
        }

        @Test
        @DisplayName("should throw WorkflowExecutionStatusNotFoundException when workflow execution not found")
        void pauseThrowsWhenExecutionNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> workflowExecutionService.resume(workflowExecutionId)
            );

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
        }
    }

    @Nested
    @DisplayName("terminate()")
    class TerminateTests {

        @Test
        @DisplayName("should call terminate() on found workflow execution")
        void terminateCallsEntityTerminateWhenExecutionFound() {
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(entity));

            workflowExecutionService.terminate(workflowExecutionId);

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
            verify(entity).terminate();
            verifyNoMoreInteractions(entity);
        }

        @Test
        @DisplayName("should throw WorkflowExecutionStatusNotFoundException when workflow execution not found")
        void pauseThrowsWhenExecutionNotFound() {
            UUID workflowExecutionId = UUID.randomUUID();

            when(workflowExecutionRepository
                    .findByWorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThrows(
                    WorkflowExecutionNotFoundException.class,
                    () -> workflowExecutionService.terminate(workflowExecutionId)
            );

            verify(workflowExecutionRepository)
                    .findByWorkflowExecutionId(workflowExecutionId);
        }
    }
}
