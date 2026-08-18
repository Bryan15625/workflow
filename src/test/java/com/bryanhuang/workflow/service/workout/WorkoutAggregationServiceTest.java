package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutRecordEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutUserAggregateEntity;
import com.bryanhuang.workflow.mapper.WorkoutUserAggregateEntityMapper;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.repository.WorkoutUserAggregateRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkoutAggregationServiceTest {

    @Mock
    private WorkoutRecordRepository workoutRecordRepository;

    @Mock
    private WorkoutUserAggregateRepository workoutUserAggregateRepository;

    @Mock
    private WorkoutUserAggregateEntityMapper workoutUserAggregateEntityMapper;

    @Mock
    private WorkflowControlGate workflowControlGate;

    @InjectMocks
    private WorkoutAggregationService workoutAggregationService;

    private WorkoutRecordEntity mockRecord(String userId) {
        WorkoutRecordEntity record = mock(WorkoutRecordEntity.class);

        when(record.getUserId()).thenReturn(userId);
        when(record.getDate()).thenReturn(LocalDate.now());
        when(record.getWeightKg()).thenReturn(BigDecimal.valueOf(72));

        when(record.getCalories()).thenReturn(2500);
        when(record.getProteinG()).thenReturn(150);
        when(record.getCarbsG()).thenReturn(250);
        when(record.getFatsG()).thenReturn(70);
        when(record.getCardioMin()).thenReturn(30);
        when(record.getCardioZone()).thenReturn(CardioZone.TWO);
        when(record.getSleepH()).thenReturn(new BigDecimal("7.5"));
        when(record.getSteps()).thenReturn(10000);
        when(record.getWorkoutType()).thenReturn(WorkoutType.REST);

        return record;
    }

    @Nested
    @DisplayName("aggregateWorkoutData()")
    class AggregateWorkoutDataTests {

        @Test
        @DisplayName("returns NONE when no workout records exist")
        void returnsNoneWhenNoRecordsExist() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();

            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionEntity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            Step step = mock(Step.class);

            Slice<WorkoutRecordEntity> emptySlice = new SliceImpl<>(List.of());

            when(workoutRecordRepository.findSliceByWorkflowExecutionEntity(
                    eq(workflowExecutionEntity),
                    any(Pageable.class)
            )).thenReturn(emptySlice);

            JobControl result = workoutAggregationService.aggregateWorkoutData(
                    step,
                    workflowExecutionEntity
            );

            assertEquals(JobControl.NONE, result);

            verify(workoutRecordRepository)
                    .findSliceByWorkflowExecutionEntity(
                            eq(workflowExecutionEntity),
                            any(Pageable.class)
                    );

            verifyNoInteractions(workoutUserAggregateRepository);
            verifyNoInteractions(workoutUserAggregateEntityMapper);
            verifyNoInteractions(workflowControlGate);
        }

        @Test
        @DisplayName("aggregates and saves one user's records")
        void aggregatesAndSavesOneUser() throws InterruptedException {
            UUID workflowExecutionId = UUID.randomUUID();

            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
            when(workflowExecutionEntity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            Step step = mock(Step.class);

            WorkoutRecordEntity record1 = mockRecord("user_001");

            Slice<WorkoutRecordEntity> slice =
                    new SliceImpl<>(List.of(record1), Pageable.ofSize(10_000), false);

            when(workoutRecordRepository.findSliceByWorkflowExecutionEntity(
                    eq(workflowExecutionEntity),
                    any(Pageable.class)
            )).thenReturn(slice);

            WorkoutUserAggregateEntity aggregateEntity =
                    mock(WorkoutUserAggregateEntity.class);

            when(workoutUserAggregateEntityMapper.toWorkoutUserAggregateEntity(
                    any(WorkoutUserAggregate.class),
                    eq(workflowExecutionEntity)
            )).thenReturn(aggregateEntity);

            JobControl result = workoutAggregationService.aggregateWorkoutData(
                    step,
                    workflowExecutionEntity
            );

            assertEquals(JobControl.NONE, result);

            verify(workoutUserAggregateEntityMapper)
                    .toWorkoutUserAggregateEntity(
                            any(WorkoutUserAggregate.class),
                            eq(workflowExecutionEntity)
                    );

            verify(workoutUserAggregateRepository)
                    .saveAll(List.of(aggregateEntity));

            verify(workflowControlGate, never())
                    .checkpointStep(any(), anyInt());
        }
    }

    @Test
    @DisplayName("finalizes previous user when a new user is encountered")
    void finalizesPreviousUserWhenUserChanges() throws InterruptedException {
        UUID workflowExecutionId = UUID.randomUUID();

        WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
        when(workflowExecutionEntity.getWorkflowExecutionId())
                .thenReturn(workflowExecutionId);

        Step step = mock(Step.class);

        WorkoutRecordEntity user1Record1 = mockRecord("user_001");
        WorkoutRecordEntity user1Record2 = mockRecord("user_001");
        WorkoutRecordEntity user2Record = mockRecord("user_002");

        Slice<WorkoutRecordEntity> slice =
                new SliceImpl<>(
                        List.of(user1Record1, user1Record2, user2Record),
                        Pageable.ofSize(10_000),
                        false
                );

        when(workoutRecordRepository.findSliceByWorkflowExecutionEntity(
                eq(workflowExecutionEntity),
                any(Pageable.class)
        )).thenReturn(slice);

        WorkoutUserAggregateEntity user1Aggregate =
                mock(WorkoutUserAggregateEntity.class);
        WorkoutUserAggregateEntity user2Aggregate =
                mock(WorkoutUserAggregateEntity.class);

        when(workoutUserAggregateEntityMapper.toWorkoutUserAggregateEntity(
                any(WorkoutUserAggregate.class),
                eq(workflowExecutionEntity)
        )).thenReturn(user1Aggregate, user2Aggregate);

        JobControl result = workoutAggregationService.aggregateWorkoutData(
                step,
                workflowExecutionEntity
        );

        assertEquals(JobControl.NONE, result);

        verify(workoutUserAggregateEntityMapper, times(2))
                .toWorkoutUserAggregateEntity(
                        any(WorkoutUserAggregate.class),
                        eq(workflowExecutionEntity)
                );

        verify(workoutUserAggregateRepository)
                .saveAll(List.of(user1Aggregate, user2Aggregate));
    }

    @Test
    @DisplayName("continues accumulating a user across batch boundaries")
    void continuesAccumulatingUserAcrossBatches() throws InterruptedException {
        UUID workflowExecutionId = UUID.randomUUID();

        WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
        when(workflowExecutionEntity.getWorkflowExecutionId())
                .thenReturn(workflowExecutionId);

        Step step = mock(Step.class);
        when(step.getStepId()).thenReturn(2);

        WorkoutRecordEntity batch1Record = mockRecord("user_001");
        WorkoutRecordEntity batch2Record = mockRecord("user_001");

        Slice<WorkoutRecordEntity> firstSlice =
                new SliceImpl<>(
                        List.of(batch1Record),
                        PageRequest.of(0, 10_000),
                        true
                );

        Slice<WorkoutRecordEntity> secondSlice =
                new SliceImpl<>(
                        List.of(batch2Record),
                        PageRequest.of(1, 10_000),
                        false
                );

        when(workoutRecordRepository.findSliceByWorkflowExecutionEntity(
                eq(workflowExecutionEntity),
                any(Pageable.class)
        )).thenReturn(firstSlice, secondSlice);

        when(workflowControlGate.checkpointStep(workflowExecutionId, 2))
                .thenReturn(JobControl.NONE);

        WorkoutUserAggregateEntity aggregateEntity =
                mock(WorkoutUserAggregateEntity.class);

        when(workoutUserAggregateEntityMapper.toWorkoutUserAggregateEntity(
                any(WorkoutUserAggregate.class),
                eq(workflowExecutionEntity)
        )).thenReturn(aggregateEntity);

        JobControl result = workoutAggregationService.aggregateWorkoutData(
                step,
                workflowExecutionEntity
        );

        assertEquals(JobControl.NONE, result);

        verify(workflowControlGate)
                .checkpointStep(workflowExecutionId, 2);

        verify(workoutUserAggregateEntityMapper)
                .toWorkoutUserAggregateEntity(
                        any(),
                        eq(workflowExecutionEntity)
                );

        verify(workoutUserAggregateRepository)
                .saveAll(List.of(aggregateEntity));
    }

    @Test
    @DisplayName("terminates when checkpoint requests termination")
    void terminatesWhenCheckpointRequestsTermination() throws InterruptedException {
        UUID workflowExecutionId = UUID.randomUUID();

        WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
        when(workflowExecutionEntity.getWorkflowExecutionId())
                .thenReturn(workflowExecutionId);

        Step step = mock(Step.class);
        when(step.getStepId()).thenReturn(2);

        WorkoutRecordEntity record = mockRecord("user_001");

        Slice<WorkoutRecordEntity> firstSlice =
                new SliceImpl<>(
                        List.of(record),
                        PageRequest.of(0, 10_000),
                        true
                );

        when(workoutRecordRepository.findSliceByWorkflowExecutionEntity(
                eq(workflowExecutionEntity),
                any(Pageable.class)
        )).thenReturn(firstSlice);

        when(workflowControlGate.checkpointStep(workflowExecutionId, 2))
                .thenReturn(JobControl.TERMINATE);

        JobControl result = workoutAggregationService.aggregateWorkoutData(
                step,
                workflowExecutionEntity
        );

        assertEquals(JobControl.TERMINATE, result);

        verify(workflowControlGate)
                .checkpointStep(workflowExecutionId, 2);

        verify(workoutRecordRepository, times(1))
                .findSliceByWorkflowExecutionEntity(
                        eq(workflowExecutionEntity),
                        any(Pageable.class)
                );

        verifyNoInteractions(workoutUserAggregateEntityMapper);
        verifyNoInteractions(workoutUserAggregateRepository);
    }

    @Test
    @DisplayName("saves aggregates when pending save batch reaches 10,000 users and saves currentAccumulator " +
            "separately")
    void savesWhenPendingBatchReachesSaveBatchSize() throws InterruptedException {
        UUID workflowExecutionId = UUID.randomUUID();

        WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
        when(workflowExecutionEntity.getWorkflowExecutionId())
                .thenReturn(workflowExecutionId);

        Step step = mock(Step.class);

        List<WorkoutRecordEntity> records = new ArrayList<>(10_001);

        for (int i = 0; i < 10_001; i++) {
            records.add(mockRecord("user_" + i));
        }

        Slice<WorkoutRecordEntity> slice =
                new SliceImpl<>(
                        records,
                        PageRequest.of(0, 10_000),
                        false
                );

        when(workoutRecordRepository.findSliceByWorkflowExecutionEntity(
                eq(workflowExecutionEntity),
                any(Pageable.class)
        )).thenReturn(slice);

        WorkoutUserAggregateEntity aggregateEntity =
                mock(WorkoutUserAggregateEntity.class);

        when(workoutUserAggregateEntityMapper.toWorkoutUserAggregateEntity(
                any(WorkoutUserAggregate.class),
                eq(workflowExecutionEntity)
        )).thenReturn(aggregateEntity);

        List<Integer> saveSizes = new ArrayList<>();

        doAnswer(invocation -> {
            Iterable<WorkoutUserAggregateEntity> entities =
                    invocation.getArgument(0);

            int size = (int) StreamSupport
                    .stream(entities.spliterator(), false)
                    .count();

            saveSizes.add(size);

            return null;
        }).when(workoutUserAggregateRepository).saveAll(any());

        JobControl result = workoutAggregationService.aggregateWorkoutData(
                step,
                workflowExecutionEntity
        );

        assertEquals(JobControl.NONE, result);

        verify(workoutUserAggregateRepository, times(2))
                .saveAll(any());

        assertEquals(List.of(10_000, 1), saveSizes);
    }


}
