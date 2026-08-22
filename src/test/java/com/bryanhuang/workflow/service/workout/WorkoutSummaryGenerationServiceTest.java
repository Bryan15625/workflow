package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.ReportEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutIdealEntity;
import com.bryanhuang.workflow.exception.ReportNotFoundException;
import com.bryanhuang.workflow.exception.WorkoutIdealNotFoundException;
import com.bryanhuang.workflow.mapper.ReportEntityMapper;
import com.bryanhuang.workflow.mapper.WorkoutIdealEntityMapper;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.Sex;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.Data;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import com.bryanhuang.workflow.repository.ReportRepository;
import com.bryanhuang.workflow.repository.WorkoutIdealRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutSummaryGenerationServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportEntityMapper reportEntityMapper;

    @Mock
    private WorkflowControlGate workflowControlGate;

    @Mock
    private AiResponseService aiResponseService;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WorkoutIdealRepository workoutIdealRepository;

    @Mock
    private WorkoutIdealEntityMapper workoutIdealEntityMapper;

    @InjectMocks
    private WorkoutSummaryGenerationService workoutSummaryGenerationService;

    private UUID workflowExecutionId;
    private Integer stepId;

    private Workflow workflow;
    private Step step;
    private WorkflowExecutionEntity workflowExecutionEntity;
    private CohortProfile cohortProfile;
    private Data workflowData;

    private ReportEntity reportEntity;
    private WorkoutIdealEntity workoutIdealEntity;
    private CohortAnalysisResult.Report report;
    private WorkoutIdeal ideal;

    @BeforeEach
    void setUp() {
        workflowExecutionId = UUID.randomUUID();
        stepId = 1;

        step = mock(Step.class);
        workflowExecutionEntity = mock(WorkflowExecutionEntity.class);
        cohortProfile = mock(CohortProfile.class);
        workflow = mock(Workflow.class);
        workflowData = mock(Data.class);

        reportEntity = mock(ReportEntity.class);
        workoutIdealEntity = mock(WorkoutIdealEntity.class);
        report = mock(CohortAnalysisResult.Report.class);
        ideal = mock(WorkoutIdeal.class);
    }

    private void stubPromptInputs() {
        when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
        when(workflow.getCohortProfile()).thenReturn(cohortProfile);
        when(step.getStepId()).thenReturn(stepId);

        when(cohortProfile.getAge()).thenReturn(23);
        when(cohortProfile.getHeightCm()).thenReturn(174);
        when(cohortProfile.getWeightKg()).thenReturn(76);
        when(cohortProfile.getDurationDays()).thenReturn(100);
        when(cohortProfile.getSex()).thenReturn(Sex.MALE);
        when(cohortProfile.getGoal()).thenReturn(Goal.FAT_LOSS);
    }

    @Nested
    @DisplayName("generateSummary()")
    class GenerateSummaryTests {

        @Test
        @DisplayName("throws ReportNotFoundException when no report exists for the workflow execution")
        void reportNotFound_throwsException() {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity))
                    .isInstanceOf(ReportNotFoundException.class);

            verifyNoInteractions(
                    reportEntityMapper,
                    workoutIdealRepository,
                    workoutIdealEntityMapper,
                    workflowControlGate,
                    aiResponseService,
                    pdfGenerationService
            );
        }

        @Test
        @DisplayName("throws WorkoutIdealNotFoundException when no workout ideal exists for the workflow execution")
        void idealNotFound_throwsException() {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(reportEntity));
            when(reportEntityMapper.toReport(reportEntity)).thenReturn(report);
            when(workoutIdealRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity))
                    .isInstanceOf(WorkoutIdealNotFoundException.class);

            verifyNoInteractions(
                    workoutIdealEntityMapper,
                    workflowControlGate,
                    aiResponseService,
                    pdfGenerationService
            );
        }

        @Test
        @DisplayName("terminates before the LLM call when the first checkpoint signals TERMINATE")
        void terminateAtFirstCheckpoint_skipsLlmAndPdf() throws InterruptedException {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(reportEntity));
            when(reportEntityMapper.toReport(reportEntity)).thenReturn(report);
            when(workoutIdealRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(workoutIdealEntity));
            when(workoutIdealEntityMapper.toWorkoutIdeal(workoutIdealEntity)).thenReturn(ideal);
            when(step.getStepId()).thenReturn(stepId);
            when(workflowControlGate.checkpointStep(workflowExecutionId, stepId))
                    .thenReturn(JobControl.TERMINATE);

            JobControl result =
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity);

            assertThat(result).isEqualTo(JobControl.TERMINATE);
            verifyNoInteractions(aiResponseService, pdfGenerationService);
            verify(workflowControlGate, times(1)).checkpointStep(workflowExecutionId, stepId);
        }

        @Test
        @DisplayName("terminates after the LLM call but before PDF write when the second checkpoint signals TERMINATE")
        void terminateAtSecondCheckpoint_callsLlmButSkipsPdf() throws InterruptedException {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(reportEntity));
            when(reportEntityMapper.toReport(reportEntity)).thenReturn(report);
            when(workoutIdealRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(workoutIdealEntity));
            when(workoutIdealEntityMapper.toWorkoutIdeal(workoutIdealEntity)).thenReturn(ideal);
            when(step.getStepId()).thenReturn(stepId);
            when(workflow.getCohortProfile()).thenReturn(cohortProfile);
            stubPromptInputs();

            when(objectMapper.writeValueAsString(report)).thenReturn("{\"report\":true}");
            when(objectMapper.writeValueAsString(ideal)).thenReturn("{\"ideal\":true}");
            when(aiResponseService.generateResponse(eq(workflowExecutionId), anyString()))
                    .thenReturn("narrative text");

            // First checkpoint: proceed. Second checkpoint: terminate.
            when(workflowControlGate.checkpointStep(workflowExecutionId, stepId))
                    .thenReturn(JobControl.NONE, JobControl.TERMINATE);

            JobControl result =
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity);

            assertThat(result).isEqualTo(JobControl.TERMINATE);
            verify(aiResponseService, times(1))
                    .generateResponse(eq(workflowExecutionId), anyString());
            verifyNoInteractions(pdfGenerationService);
            verify(workflowControlGate, times(2)).checkpointStep(workflowExecutionId, stepId);
        }

        @Test
        @DisplayName("happy path: generates narrative, writes PDF, and returns JobControl.NONE")
        void happyPath_generatesNarrativeAndPdf_returnsNone() throws InterruptedException {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(reportEntity));
            when(reportEntityMapper.toReport(reportEntity)).thenReturn(report);
            when(workoutIdealRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(workoutIdealEntity));
            when(workoutIdealEntityMapper.toWorkoutIdeal(workoutIdealEntity)).thenReturn(ideal);
            when(step.getStepId()).thenReturn(stepId);
            when(workflow.getCohortProfile()).thenReturn(cohortProfile);
            when(workflow.getData()).thenReturn(workflowData);
            when(workflowData.getOutput()).thenReturn("report.pdf");
            stubPromptInputs();

            when(objectMapper.writeValueAsString(report)).thenReturn("{\"report\":true}");
            when(objectMapper.writeValueAsString(ideal)).thenReturn("{\"ideal\":true}");
            when(aiResponseService.generateResponse(eq(workflowExecutionId), anyString()))
                    .thenReturn("narrative text");
            when(workflowControlGate.checkpointStep(workflowExecutionId, stepId))
                    .thenReturn(JobControl.NONE, JobControl.NONE);

            JobControl result =
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity);

            assertThat(result).isEqualTo(JobControl.NONE);
            verify(pdfGenerationService, times(1)).generatePdf(
                    eq(cohortProfile),
                    eq(report),
                    eq("narrative text"),
                    eq("/data/output/report.pdf")
            );
            verify(workflowControlGate, times(2)).checkpointStep(workflowExecutionId, stepId);
        }

        @Test
        @DisplayName("wraps a JacksonException from serializing the report into a RuntimeException")
        void reportSerializationFails_throwsWrappedRuntimeException() throws InterruptedException {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(reportEntity));
            when(reportEntityMapper.toReport(reportEntity)).thenReturn(report);
            when(workoutIdealRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(workoutIdealEntity));
            when(workoutIdealEntityMapper.toWorkoutIdeal(workoutIdealEntity)).thenReturn(ideal);
            when(step.getStepId()).thenReturn(stepId);
            when(workflow.getCohortProfile()).thenReturn(cohortProfile);
            when(workflowControlGate.checkpointStep(workflowExecutionId, stepId))
                    .thenReturn(JobControl.NONE);

            when(objectMapper.writeValueAsString(report)).thenThrow(mock(JacksonException.class));

            assertThatThrownBy(() ->
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to serialize report for LLM prompt");

            verifyNoInteractions(aiResponseService, pdfGenerationService);
        }

        @Test
        @DisplayName("wraps a JacksonException from serializing the ideal into a RuntimeException")
        void idealSerializationFails_throwsWrappedRuntimeException() throws InterruptedException {
            when(workflowExecutionEntity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);
            when(reportRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(reportEntity));
            when(reportEntityMapper.toReport(reportEntity)).thenReturn(report);
            when(workoutIdealRepository.findByWorkflowExecutionEntity_WorkflowExecutionId(workflowExecutionId))
                    .thenReturn(Optional.of(workoutIdealEntity));
            when(workoutIdealEntityMapper.toWorkoutIdeal(workoutIdealEntity)).thenReturn(ideal);
            when(step.getStepId()).thenReturn(stepId);
            when(workflow.getCohortProfile()).thenReturn(cohortProfile);
            when(workflowControlGate.checkpointStep(workflowExecutionId, stepId))
                    .thenReturn(JobControl.NONE);

            when(objectMapper.writeValueAsString(report)).thenReturn("{\"report\":true}");
            when(objectMapper.writeValueAsString(ideal))
                    .thenThrow(mock(JacksonException.class));

            assertThatThrownBy(() ->
                    workoutSummaryGenerationService.generateSummary(step, workflow, workflowExecutionEntity))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to serialize report for LLM prompt");

            verifyNoInteractions(aiResponseService, pdfGenerationService);
        }
    }
}