package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.ReportEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutUserAggregateEntity;
import com.bryanhuang.workflow.mapper.ReportEntityMapper;
import com.bryanhuang.workflow.mapper.WorkoutIdealEntityMapper;
import com.bryanhuang.workflow.mapper.WorkoutUserAggregateEntityMapper;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workout.*;
import com.bryanhuang.workflow.repository.ReportRepository;
import com.bryanhuang.workflow.repository.WorkoutIdealRepository;
import com.bryanhuang.workflow.repository.WorkoutUserAggregateRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class WorkoutMetricsEvaluationServiceTest {

    @Mock
    private WorkoutUserAggregateRepository workoutUserAggregateRepository;

    @Mock
    private WorkflowControlGate workflowControlGate;

    @Mock
    private WorkoutIdealsCalculator workoutIdealsCalculator;

    @Mock
    private WorkoutUserAggregateEntityMapper workoutUserAggregateEntityMapper;

    @Mock
    private WorkoutIdealEntityMapper workoutIdealEntityMapper;

    @Mock
    private WorkoutIdealRepository workoutIdealRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportEntityMapper reportEntityMapper;

    @InjectMocks
    private WorkoutMetricsEvaluationService service;

    @Nested
    @DisplayName("evaluateMetrics()")
    class EvaluateMetricsTests {

        @Test
        @DisplayName("evaluates single page and saves report")
        void evaluatesSinglePage() throws Exception {
            WorkflowExecutionEntity entity = mockEntity();
            Workflow workflow = mockWorkflow();
            Step step = mock(Step.class);

            WorkoutUserAggregateEntity aggregateEntity = mock(WorkoutUserAggregateEntity.class);
            WorkoutUserAggregate actual = buildActual("user_001");

            WorkoutIdeal ideal = buildIdeal();
            CohortAnalysisResult.Report report = mock(CohortAnalysisResult.Report.class);
            ReportEntity reportEntity = mock(ReportEntity.class);

            Slice<WorkoutUserAggregateEntity> slice = mockSlice(List.of(aggregateEntity), false);

            when(workoutUserAggregateRepository.findSliceByWorkflowExecutionEntity(eq(entity), any(Pageable.class)))
                    .thenReturn(slice);

            when(workoutIdealsCalculator.calculateIdeal(workflow.getCohortProfile()))
                    .thenReturn(ideal);

            when(workoutUserAggregateEntityMapper.toWorkoutUserAggregate(aggregateEntity))
                    .thenReturn(actual);

            when(reportEntityMapper.toReportEntity(any(CohortAnalysisResult.Report.class), eq(entity)))
                    .thenReturn(reportEntity);

            when(reportRepository.save(reportEntity)).thenReturn(reportEntity);

            JobControl result = service.evaluateMetrics(step, workflow, entity);

            assertEquals(JobControl.NONE, result);
            InOrder inOrder = inOrder(
                    workoutIdealsCalculator,
                    workoutUserAggregateRepository,
                    workoutUserAggregateEntityMapper,
                    reportEntityMapper,
                    reportRepository
            );

            inOrder.verify(workoutIdealsCalculator).calculateIdeal(workflow.getCohortProfile());
            inOrder.verify(workoutUserAggregateRepository)
                    .findSliceByWorkflowExecutionEntity(eq(entity), any(Pageable.class));
            inOrder.verify(workoutUserAggregateEntityMapper).toWorkoutUserAggregate(aggregateEntity);
            inOrder.verify(reportEntityMapper).toReportEntity(any(CohortAnalysisResult.Report.class), eq(entity));
            inOrder.verify(reportRepository).save(reportEntity);

        }

        @Test
        @DisplayName("evaluates multiple pages and saves report")
        void evaluatesMultiplePages() throws Exception {
            WorkflowExecutionEntity entity = mockEntity();
            Workflow workflow = mockWorkflow();
            Step step = mock(Step.class);
            when(step.getStepId()).thenReturn(1);

            WorkoutUserAggregateEntity firstEntity = mock(WorkoutUserAggregateEntity.class);
            WorkoutUserAggregateEntity secondEntity = mock(WorkoutUserAggregateEntity.class);

            WorkoutUserAggregate firstActual = buildActual("user_001");
            WorkoutUserAggregate secondActual = buildActual("user_002");

            WorkoutIdeal ideal = buildIdeal();

            ReportEntity reportEntity = mock(ReportEntity.class);

            Slice<WorkoutUserAggregateEntity> firstSlice = mockSlice(List.of(firstEntity), true);
            Slice<WorkoutUserAggregateEntity> secondSlice = mockSlice(List.of(secondEntity), false);

            when(workoutUserAggregateRepository.findSliceByWorkflowExecutionEntity(eq(entity), any(Pageable.class)))
                    .thenReturn(firstSlice, secondSlice);
            when(workoutIdealsCalculator.calculateIdeal(workflow.getCohortProfile()))
                    .thenReturn(ideal);
            when(workoutUserAggregateEntityMapper.toWorkoutUserAggregate(firstEntity))
                    .thenReturn(firstActual);
            when(workoutUserAggregateEntityMapper.toWorkoutUserAggregate(secondEntity))
                    .thenReturn(secondActual);
            when(workflowControlGate.checkpointStep(entity.getWorkflowExecutionId(), step.getStepId()))
                    .thenReturn(JobControl.NONE);
            when(reportEntityMapper.toReportEntity(any(CohortAnalysisResult.Report.class), eq(entity)))
                    .thenReturn(reportEntity);
            when(reportRepository.save(reportEntity)).thenReturn(reportEntity);

            JobControl result = service.evaluateMetrics(step, workflow, entity);
            assertEquals(JobControl.NONE, result);

            InOrder inOrder = inOrder(
                    workoutIdealsCalculator,
                    workoutUserAggregateRepository,
                    workoutUserAggregateEntityMapper,
                    workflowControlGate,
                    reportEntityMapper,
                    reportRepository
            );

            inOrder.verify(workoutIdealsCalculator).calculateIdeal(workflow.getCohortProfile());
            inOrder.verify(workoutUserAggregateRepository).findSliceByWorkflowExecutionEntity(
                            eq(entity),
                            any(Pageable.class));
            inOrder.verify(workoutUserAggregateEntityMapper).toWorkoutUserAggregate(firstEntity);
            inOrder.verify(workflowControlGate).checkpointStep(
                    entity.getWorkflowExecutionId(),
                    1
            );
            inOrder.verify(workoutUserAggregateRepository).findSliceByWorkflowExecutionEntity(
                    eq(entity),
                    any(Pageable.class));
            inOrder.verify(workoutUserAggregateEntityMapper).toWorkoutUserAggregate(secondEntity);
            inOrder.verify(reportEntityMapper).toReportEntity(any(CohortAnalysisResult.Report.class), eq(entity));
            inOrder.verify(reportRepository).save(reportEntity);
        }

        @Test
        @DisplayName("saves empty report when no aggregates exist")
        void evaluatesEmptyPage() throws Exception {
            WorkflowExecutionEntity entity = mockEntity();
            Workflow workflow = mockWorkflow();
            Step step = mock(Step.class);

            WorkoutIdeal ideal = buildIdeal();
            ReportEntity reportEntity = mock(ReportEntity.class);

            Slice<WorkoutUserAggregateEntity> slice = mock(Slice.class);
            when(slice.getContent()).thenReturn(List.of());

            when(workoutUserAggregateRepository.findSliceByWorkflowExecutionEntity(eq(entity), any(Pageable.class)))
                    .thenReturn(slice);

            when(workoutIdealsCalculator.calculateIdeal(workflow.getCohortProfile()))
                    .thenReturn(ideal);

            when(reportEntityMapper.toReportEntity(any(CohortAnalysisResult.Report.class), eq(entity)))
                    .thenReturn(reportEntity);

            when(reportRepository.save(reportEntity)).thenReturn(reportEntity);

            JobControl result = service.evaluateMetrics(step, workflow, entity);

            assertEquals(JobControl.NONE, result);

            InOrder inOrder = inOrder(
                    workoutIdealsCalculator,
                    workoutUserAggregateRepository,
                    reportEntityMapper,
                    reportRepository
            );

            inOrder.verify(workoutIdealsCalculator).calculateIdeal(workflow.getCohortProfile());
            inOrder.verify(workoutUserAggregateRepository).findSliceByWorkflowExecutionEntity(
                            eq(entity),
                            any(Pageable.class)
                    );
            inOrder.verify(reportEntityMapper).toReportEntity(
                            any(CohortAnalysisResult.Report.class),
                            eq(entity)
                    );
            inOrder.verify(reportRepository).save(reportEntity);

            verifyNoInteractions(
                    workoutUserAggregateEntityMapper,
                    workflowControlGate
            );
        }

        @Test
        @DisplayName("terminates when control gate requests termination")
        void terminatesEvaluation() throws Exception {
            WorkflowExecutionEntity entity = mockEntity();

            CohortProfile profile = mock(CohortProfile.class);
            when(profile.getGoal()).thenReturn(Goal.MUSCLE_GAIN);
            when(profile.getWeightKg()).thenReturn(70);

            Workflow workflow = mock(Workflow.class);
            when(workflow.getCohortProfile()).thenReturn(profile);
            Step step = mock(Step.class);
            when(step.getStepId()).thenReturn(1);

            WorkoutUserAggregateEntity aggregateEntity = mock(WorkoutUserAggregateEntity.class);
            WorkoutUserAggregate actual = buildActual("user_001");

            WorkoutIdeal ideal = buildIdeal();

            Slice<WorkoutUserAggregateEntity> slice = mockSlice(List.of(aggregateEntity), true);

            when(workoutUserAggregateRepository.findSliceByWorkflowExecutionEntity(eq(entity), any(Pageable.class)))
                    .thenReturn(slice);
            when(workoutIdealsCalculator.calculateIdeal(workflow.getCohortProfile()))
                    .thenReturn(ideal);
            when(workoutUserAggregateEntityMapper.toWorkoutUserAggregate(aggregateEntity))
                    .thenReturn(actual);
            when(workflowControlGate.checkpointStep(entity.getWorkflowExecutionId(), step.getStepId()))
                    .thenReturn(JobControl.TERMINATE);

            JobControl result = service.evaluateMetrics(step, workflow, entity);

            assertEquals(JobControl.TERMINATE, result);
            InOrder inOrder = inOrder(
                    workoutIdealsCalculator,
                    workoutUserAggregateRepository,
                    workoutUserAggregateEntityMapper,
                    workflowControlGate
            );

            inOrder.verify(workoutIdealsCalculator).calculateIdeal(workflow.getCohortProfile());
            inOrder.verify(workoutUserAggregateRepository)
                    .findSliceByWorkflowExecutionEntity(eq(entity), any(Pageable.class));
            inOrder.verify(workoutUserAggregateEntityMapper).toWorkoutUserAggregate(aggregateEntity);
            inOrder.verify(workflowControlGate).checkpointStep(entity.getWorkflowExecutionId(), 1);

            verify(reportEntityMapper, never()).toReportEntity(any(), any());
            verify(reportRepository, never()).save(any());
        }
    }

    private WorkflowExecutionEntity mockEntity() {
        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
        when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());
        return entity;
    }

    private Workflow mockWorkflow() {
        CohortProfile profile = mock(CohortProfile.class);
        when(profile.getGoal()).thenReturn(Goal.MUSCLE_GAIN);
        when(profile.getWeightKg()).thenReturn(70);
        when(profile.getDurationDays()).thenReturn(28);

        Workflow workflow = mock(Workflow.class);
        when(workflow.getCohortProfile()).thenReturn(profile);

        return workflow;
    }

    @SuppressWarnings("unchecked")
    private Slice<WorkoutUserAggregateEntity> mockSlice(
            List<WorkoutUserAggregateEntity> content,
            boolean hasNext
    ) {
        Slice<WorkoutUserAggregateEntity> slice = mock(Slice.class);
        when(slice.getContent()).thenReturn(content);
        when(slice.hasNext()).thenReturn(hasNext);
        return slice;
    }

    private WorkoutUserAggregate buildActual(String userId) {
        return WorkoutUserAggregate.builder()
                .userId(userId)
                .endWeightKg(BigDecimal.valueOf(71))
                .avgCalories(BigDecimal.valueOf(2600))
                .avgProteinG(BigDecimal.valueOf(140))
                .avgFatsG(BigDecimal.valueOf(65))
                .avgWeeklyCardioMin(BigDecimal.valueOf(200))
                .avgDailySteps(BigDecimal.valueOf(10000))
                .upperBodySessionsPerWeek(2)
                .lowerBodySessionsPerWeek(2)
                .avgWorkoutsPerWeek(BigDecimal.valueOf(4))
                .avgWorkoutRir(WorkoutRir.TWO) // use your actual enum value
                .avgSleepH(BigDecimal.valueOf(8))
                .build();
    }
    private WorkoutIdeal buildIdeal() {
        return WorkoutIdeal.builder()
                .idealEndWeightKg(new Range(70, 72))
                .idealCalories(new Range(2400, 2800))
                .idealProteinG(new Range(120, 160))
                .idealFatG(new Range(50, 80))
                .idealCarbG(new Range(200, 350))
                .idealWeeklyCardioMin(new Range(150, 300))
                .idealDailySteps(new Range(7000, 15000))
                .idealUpperBodySessionsPerWeek(new Range(2, 3))
                .idealLowerBodySessionsPerWeek(new Range(2, 3))
                .idealWorkoutsPerWeek(new Range(4, 6))
                .idealWorkoutRir(new Range(0, 3))
                .idealSleepH(new Range(7, 9))
                .build();
    }
}