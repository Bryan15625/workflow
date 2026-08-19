package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutRecordEntity;
import com.bryanhuang.workflow.exception.InvalidRowException;
import com.bryanhuang.workflow.mapper.WorkoutRecordEntityMapper;
import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.Data;
import com.bryanhuang.workflow.model.workflow.JobControl;
import com.bryanhuang.workflow.model.workflow.Step;
import com.bryanhuang.workflow.model.workflow.Workflow;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import com.bryanhuang.workflow.repository.WorkoutRecordRepository;
import com.bryanhuang.workflow.service.workflow.WorkflowControlGate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutCsvIngestionServiceTest {

    @Mock
    private WorkoutRecordRepository workoutRecordRepository;

    @Mock
    private WorkoutRecordEntityMapper workoutRecordEntityMapper;

    @Mock
    private WorkoutValidationService validationService;

    @Mock
    private WorkflowControlGate workflowControlGate;

    @Mock
    private CohortProfile cohortProfile;

    @InjectMocks
    private WorkoutCsvIngestionService workoutCsvIngestionService;

    private MockedStatic<Files> filesMock;

    @BeforeEach
    void setUp() {
        filesMock = mockStatic(Files.class);

        when(cohortProfile.getWeightKg()).thenReturn(70);
        when(cohortProfile.getDurationDays()).thenReturn(10);
    }

    @AfterEach
    void tearDown() {
        filesMock.close();
    }

    private void stubReaderContent(String csvContent) {
        filesMock.when(() -> Files.newBufferedReader(any(Path.class)))
                .thenReturn(new BufferedReader(new StringReader(csvContent)));
    }

    private void stubValidRowParsing() {
        when(validationService.parseUserId(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(validationService.parseDate(any(), any()))
                .thenReturn(LocalDate.of(2024, 1, 1));

        // Preserve the actual value from the CSV.
        when(validationService.parseBigDecimal(any(), any()))
                .thenAnswer(invocation ->
                        BigDecimal.valueOf(
                                Double.parseDouble(invocation.<String>getArgument(0))
                        )
                );

        when(validationService.parseInt(any(), any()))
                .thenReturn(10);

        when(validationService.parseIntOrNull(any(), any()))
                .thenReturn(5);

        when(validationService.parseWorkoutType(any(), any()))
                .thenReturn(WorkoutType.PUSH);

        when(validationService.parseCardioZone(any(), any()))
                .thenReturn(CardioZone.TWO);

        when(validationService.parseWorkoutRir(any(), any()))
                .thenReturn(WorkoutRir.THREE);
    }

    private void stubEntityMapping() {
        when(workoutRecordEntityMapper.toWorkoutRecordEntity(any(), any()))
                .thenReturn(mock(WorkoutRecordEntity.class));
    }

    private Workflow buildWorkflow() {
        return Workflow.builder()
                .data(Data.builder()
                        .input("test.csv")
                        .build())
                .cohortProfile(cohortProfile)
                .build();
    }

    private String buildCsvWithRows(int rowCount) {
        String header = "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,"
                + "cardio_min,cardio_zone,steps,workout_type,workout_rir,total_sets,sleep_h";

        int durationDays = 10;

        String rows = IntStream.range(0, rowCount)
                .mapToObj(i -> {
                    int userNumber = i / durationDays + 1;

                    return "u" + userNumber
                            + ",2024-01-01,70,2000,150,200,60,30,2,8000,PUSH,3,5,7.5";
                })
                .collect(Collectors.joining("\n"));

        return header + "\n" + rows;
    }

    private String buildCsvForSingleUser(int rowCount, int weightKg) {
        String header = "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,"
                + "cardio_min,cardio_zone,steps,workout_type,workout_rir,total_sets,sleep_h";

        String rows = IntStream.range(0, rowCount)
                .mapToObj(i ->
                        "u1,2024-01-01,"
                                + weightKg
                                + ",2000,150,200,60,30,2,8000,PUSH,3,5,7.5"
                )
                .collect(Collectors.joining("\n"));

        return header + "\n" + rows;
    }

    private String buildCsvWithUserRowCounts(int firstUserRowCount, int secondUserRowCount) {
        String header = "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,"
                + "cardio_min,cardio_zone,steps,workout_type,workout_rir,total_sets,sleep_h";

        String firstUserRows = IntStream.range(0, firstUserRowCount)
                .mapToObj(i ->
                        "u1,2024-01-01,70,2000,150,200,60,30,2,8000,PUSH,3,5,7.5"
                )
                .collect(Collectors.joining("\n"));

        String secondUserRows = IntStream.range(0, secondUserRowCount)
                .mapToObj(i ->
                        "u2,2024-01-01,70,2000,150,200,60,30,2,8000,PUSH,3,5,7.5"
                )
                .collect(Collectors.joining("\n"));

        return header + "\n" + firstUserRows + "\n" + secondUserRows;
    }

    private String buildInvalidCsv() {
        String header = "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,"
                + "cardio_min,cardio_zone,steps,workout_type,workout_rir,total_sets";

        String row = "u1,2024-01-01,70,2000,150,200,60,30,2,8000,PUSH,3,5";

        String rows = IntStream.range(0, 13)
                .mapToObj(i -> row)
                .collect(Collectors.joining("\n"));

        return header + "\n" + rows;
    }

    @Nested
    @DisplayName("ingestCsv")
    class IngestCsvTests {

        @Test
        @DisplayName("saves once and never checkpoints when row count is below batch size")
        void savesOnce_whenBelowBatchSize() throws Exception {
            stubValidRowParsing();
            stubEntityMapping();
            stubReaderContent(buildCsvWithRows(50));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            when(cohortProfile.getParticipants()).thenReturn(5);

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            JobControl result =
                    workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);

            verify(workoutRecordRepository, times(1))
                    .saveAll(anyList());

            verify(workflowControlGate, never())
                    .checkpointStep(any(), any());
        }

        @Test
        @DisplayName("saves once and checkpoints when row count equals batch size")
        void savesOnceAndCheckpoints_whenExactlyBatchSize() throws Exception {
            stubValidRowParsing();
            stubEntityMapping();
            stubReaderContent(buildCsvWithRows(10_000));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            when(cohortProfile.getParticipants()).thenReturn(1000);

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            UUID workflowExecutionId = UUID.randomUUID();

            when(entity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            when(workflowControlGate.checkpointStep(workflowExecutionId, 1))
                    .thenReturn(JobControl.NONE);

            JobControl result =
                    workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);

            verify(workoutRecordRepository, times(1))
                    .saveAll(anyList());

            verify(workflowControlGate, times(1))
                    .checkpointStep(workflowExecutionId, 1);
        }

        @Test
        @DisplayName("saves in batches and checkpoints between batches when row count exceeds batch size")
        void savesInBatchesAndCheckpoints_whenAboveBatchSize() throws Exception {
            stubValidRowParsing();
            stubEntityMapping();

            // 10,010 rows:
            // - 10,000 saved at the batch boundary
            // - checkpoint
            // - 10 remaining rows saved at EOF
            stubReaderContent(buildCsvWithRows(10_010));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            when(cohortProfile.getParticipants()).thenReturn(1001);

            UUID workflowExecutionId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            when(workflowControlGate.checkpointStep(workflowExecutionId, 1))
                    .thenReturn(JobControl.NONE);

            JobControl result =
                    workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);

            verify(workoutRecordRepository, times(2))
                    .saveAll(anyList());

            verify(workflowControlGate, times(1))
                    .checkpointStep(workflowExecutionId, 1);
        }

        @Test
        @DisplayName("stops processing immediately when checkpoint returns TERMINATE mid-file")
        void stopsProcessing_whenCheckpointReturnsTerminate() throws Exception {
            stubValidRowParsing();
            stubEntityMapping();

            // 20,000 rows = 2 batches.
            // Processing must stop after the first checkpoint.
            stubReaderContent(buildCsvWithRows(20_000));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            UUID workflowExecutionId = UUID.randomUUID();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            when(workflowControlGate.checkpointStep(workflowExecutionId, 1))
                    .thenReturn(JobControl.TERMINATE);

            JobControl result =
                    workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.TERMINATE, result);

            verify(workoutRecordRepository, times(1))
                    .saveAll(anyList());

            verify(workflowControlGate, times(1))
                    .checkpointStep(workflowExecutionId, 1);
        }

        @Test
        @DisplayName("wraps IOException from file reading in a RuntimeException")
        void wrapsIOException() {
            filesMock.when(() -> Files.newBufferedReader(any(Path.class)))
                    .thenThrow(new IOException("disk error"));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            assertThrows(
                    RuntimeException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());
        }

        @Test
        @DisplayName("propagates InvalidRowException from row validation without catching it")
        void propagatesInvalidRowException() {
            stubReaderContent(buildCsvWithRows(1));

            when(validationService.parseUserId(any(), any()))
                    .thenThrow(new InvalidRowException("bad user_id"));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());
        }

        @Test
        @DisplayName("throws InvalidRowException when the number of columns is not 14")
        void throwsInvalidRowException_whenColumnsNot14() {
            stubReaderContent(buildInvalidCsv());

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());
        }

        @Test
        @DisplayName("logs a warning but does not throw when close() fails after a row validation error")
        void doesNotPropagateCloseFailure_whenBodyAlreadyThrew() throws Exception {
            BufferedReader realReader =
                    new BufferedReader(
                            new StringReader(buildCsvWithRows(1))
                    );

            BufferedReader spyReader = spy(realReader);

            doThrow(new IOException("close failed"))
                    .when(spyReader)
                    .close();

            filesMock.when(() -> Files.newBufferedReader(any(Path.class)))
                    .thenReturn(spyReader);

            when(validationService.parseUserId(any(), any()))
                    .thenThrow(new InvalidRowException("bad user_id"));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            InvalidRowException ex = assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            assertEquals("bad user_id", ex.getMessage());

            verify(spyReader).close();
        }

        @Test
        @DisplayName("accepts starting weight exactly 1 kg below cohort weight")
        void acceptsStartingWeightAtLowerBoundary() throws Exception {
            stubValidRowParsing();
            stubEntityMapping();
            stubReaderContent(buildCsvForSingleUser(10, 69));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            when(cohortProfile.getParticipants()).thenReturn(1);

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            UUID workflowExecutionId = UUID.randomUUID();

            when(entity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            JobControl result =
                    workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);

            verify(validationService).checkWeightKgInRange(
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(69)) == 0
                    ),
                    eq("weight_kg"),
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(70)) == 0
                    )
            );

            verify(workoutRecordRepository, times(1))
                    .saveAll(anyList());
        }

        @Test
        @DisplayName("accepts starting weight exactly 1 kg above cohort weight")
        void acceptsStartingWeightAtUpperBoundary() throws Exception {
            stubValidRowParsing();
            stubEntityMapping();
            stubReaderContent(buildCsvForSingleUser(10, 71));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            when(cohortProfile.getParticipants()).thenReturn(1);

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            UUID workflowExecutionId = UUID.randomUUID();

            when(entity.getWorkflowExecutionId())
                    .thenReturn(workflowExecutionId);

            JobControl result =
                    workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);

            verify(validationService).checkWeightKgInRange(
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(71)) == 0
                    ),
                    eq("weight_kg"),
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(70)) == 0
                    )
            );

            verify(workoutRecordRepository, times(1))
                    .saveAll(anyList());
        }

        @Test
        @DisplayName("propagates InvalidRowException when starting weight is more than 1 kg below cohort weight")
        void rejectsStartingWeightBelowAllowedRange() {
            stubValidRowParsing();
            stubReaderContent(buildCsvForSingleUser(10, 68));

            doThrow(new InvalidRowException(
                    "weight_kg must be within 1 kg of cohort starting weight"
            )).when(validationService).checkWeightKgInRange(
                    any(),
                    eq("weight_kg"),
                    any()
            );

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            InvalidRowException ex = assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            assertEquals(
                    "weight_kg must be within 1 kg of cohort starting weight",
                    ex.getMessage()
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());

            verify(validationService).checkWeightKgInRange(
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(68)) == 0
                    ),
                    eq("weight_kg"),
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(70)) == 0
                    )
            );
        }

        @Test
        @DisplayName("propagates InvalidRowException when starting weight is more than 1 kg above cohort weight")
        void rejectsStartingWeightAboveAllowedRange() {
            stubValidRowParsing();
            stubReaderContent(buildCsvForSingleUser(10, 72));

            doThrow(new InvalidRowException(
                    "weight_kg must be within 1 kg of cohort starting weight"
            )).when(validationService).checkWeightKgInRange(
                    any(),
                    eq("weight_kg"),
                    any()
            );

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            InvalidRowException ex = assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            assertEquals(
                    "weight_kg must be within 1 kg of cohort starting weight",
                    ex.getMessage()
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());

            verify(validationService).checkWeightKgInRange(
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(72)) == 0
                    ),
                    eq("weight_kg"),
                    argThat(weight ->
                            weight.compareTo(BigDecimal.valueOf(70)) == 0
                    )
            );
        }

        @Test
        @DisplayName("rejects user with fewer rows than durationDays")
        void rejectsUserWithTooFewRows() {
            stubValidRowParsing();
            stubReaderContent(buildCsvForSingleUser(9, 70));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            InvalidRowException ex = assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            assertEquals(
                    "User u1 has 9 rows, expected 10",
                    ex.getMessage()
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());
        }

        @Test
        @DisplayName("rejects user with more rows than durationDays")
        void rejectsUserWithTooManyRows() {
            stubValidRowParsing();
            stubReaderContent(buildCsvForSingleUser(11, 70));

            Step step = Step.builder()
                    .stepId(1)
                    .build();

            Workflow workflow = buildWorkflow();

            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId())
                    .thenReturn(UUID.randomUUID());

            InvalidRowException ex = assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            assertEquals(
                    "User u1 has 11 rows, expected 10",
                    ex.getMessage()
            );

            verify(workoutRecordRepository, never())
                    .saveAll(anyList());
        }
    }
    @Test
    @DisplayName("does not validate a last user when CSV contains no data rows")
    void doesNotValidateLastUser_whenCsvIsEmpty() throws Exception {
        stubReaderContent(
                "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,"
                        + "cardio_min,cardio_zone,steps,workout_type,workout_rir,total_sets,sleep_h"
        );

        Step step = Step.builder()
                .stepId(1)
                .build();

        Workflow workflow = buildWorkflow();

        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
        when(entity.getWorkflowExecutionId())
                .thenReturn(UUID.randomUUID());

        JobControl result =
                workoutCsvIngestionService.ingestCsv(step, workflow, entity);

        assertEquals(JobControl.NONE, result);

        verify(workoutRecordRepository, never())
                .saveAll(anyList());

        verify(validationService, never())
                .checkWeightKgInRange(any(), any(), any());
    }
    @Test
    @DisplayName("rejects previous user when row count is incorrect")
    void rejectsPreviousUserWithIncorrectRowCount() {
        stubValidRowParsing();
        stubEntityMapping();

        // u1 has 9 rows, then u2 starts.
        // The validation should happen when transitioning from u1 to u2.
        String csv = buildCsvWithUserRowCounts(9, 10);
        stubReaderContent(csv);

        Step step = Step.builder()
                .stepId(1)
                .build();

        Workflow workflow = buildWorkflow();

        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
        when(entity.getWorkflowExecutionId())
                .thenReturn(UUID.randomUUID());

        InvalidRowException ex = assertThrows(
                InvalidRowException.class,
                () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
        );

        assertEquals(
                "User u1 has 9 rows, expected 10",
                ex.getMessage()
        );

        verify(workoutRecordRepository, never())
                .saveAll(anyList());
    }

    @Test
    @DisplayName("accepts CSV when participant count matches cohort")
    void acceptsParticipantCount_whenCorrect() throws Exception {
        stubValidRowParsing();
        stubEntityMapping();
        stubReaderContent(buildCsvWithRows(100)); // 10 users × 10 rows

        Step step = Step.builder()
                .stepId(1)
                .build();

        Workflow workflow = buildWorkflow();

        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);

        when(cohortProfile.getParticipants()).thenReturn(10);

        when(entity.getWorkflowExecutionId())
                .thenReturn(UUID.randomUUID());

        JobControl result =
                workoutCsvIngestionService.ingestCsv(step, workflow, entity);

        assertEquals(JobControl.NONE, result);

        verify(workoutRecordRepository, times(1))
                .saveAll(anyList());
    }

    @Test
    @DisplayName("rejects CSV when participant count is below cohort")
    void rejectsParticipantCount_whenBelowExpected() {
        stubValidRowParsing();
        stubEntityMapping();
        stubReaderContent(buildCsvWithRows(90)); // 9 users × 10 rows

        Step step = Step.builder()
                .stepId(1)
                .build();

        Workflow workflow = buildWorkflow();

        when(cohortProfile.getParticipants()).thenReturn(10);

        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
        when(entity.getWorkflowExecutionId())
                .thenReturn(UUID.randomUUID());

        InvalidRowException ex = assertThrows(
                InvalidRowException.class,
                () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
        );

        assertEquals(
                "Found 9 participants, expected 10",
                ex.getMessage()
        );

        verify(workoutRecordRepository, never())
                .saveAll(anyList());
    }

    @Test
    @DisplayName("rejects CSV when participant count is above cohort")
    void rejectsParticipantCount_whenAboveExpected() {
        stubValidRowParsing();
        stubEntityMapping();
        stubReaderContent(buildCsvWithRows(110)); // 11 users × 10 rows

        Step step = Step.builder()
                .stepId(1)
                .build();

        Workflow workflow = buildWorkflow();
        when(cohortProfile.getParticipants()).thenReturn(10);

        WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
        when(entity.getWorkflowExecutionId())
                .thenReturn(UUID.randomUUID());

        InvalidRowException ex = assertThrows(
                InvalidRowException.class,
                () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
        );

        assertEquals(
                "Found 11 participants, expected 10",
                ex.getMessage()
        );

        verify(workoutRecordRepository, never())
                .saveAll(anyList());
    }
}