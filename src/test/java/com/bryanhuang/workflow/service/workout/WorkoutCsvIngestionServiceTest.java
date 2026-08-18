package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutRecordEntity;
import com.bryanhuang.workflow.exception.InvalidRowException;
import com.bryanhuang.workflow.mapper.WorkoutRecordEntityMapper;
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

    @InjectMocks
    private WorkoutCsvIngestionService workoutCsvIngestionService;

    private MockedStatic<Files> filesMock;

    @BeforeEach
    void setUp() {
        // Intercept Files.newBufferedReader so no real file/path on disk is needed --
        // the hardcoded "/data/input/" prefix in ingestCsv makes this the simplest
        // way to feed in fake CSV content without touching the filesystem.
        filesMock = mockStatic(Files.class);
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
        // Every parsing call succeeds with arbitrary valid values -- the specific
        // values don't matter here since WorkoutValidationService has its own tests;
        // this only verifies ingestCsv's batching/checkpoint/error-handling behavior.
        when(validationService.parseUserId(any(), any())).thenReturn("user-1");
        when(validationService.parseDate(any(), any())).thenReturn(LocalDate.of(2024, 1, 1));
        when(validationService.parseBigDecimal(any(), any())).thenReturn(java.math.BigDecimal.TEN);
        when(validationService.parseInt(any(), any())).thenReturn(10);
        when(validationService.parseIntOrNull(any(), any())).thenReturn(5);
        when(validationService.parseWorkoutType(any(), any())).thenReturn(WorkoutType.PUSH);
        when(validationService.parseCardioZone(any(), any())).thenReturn(CardioZone.TWO);
        when(validationService.parseWorkoutRir(any(), any())).thenReturn(WorkoutRir.THREE);
        when(workoutRecordEntityMapper.toWorkoutRecordEntity(any(), any()))
                .thenReturn(mock(WorkoutRecordEntity.class));
    }

    private String buildCsvWithRows(int rowCount) {
        String header = "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,cardio_min,cardio_zone," +
                "steps,workout_type,workout_rir,total_sets,sleep_h";
        String row = "u1,2024-01-01,70,2000,150,200,60,30,2,8000,PUSH,3,5,7.5";
        String rows = IntStream.range(0, rowCount)
                .mapToObj(i -> row)
                .collect(Collectors.joining("\n"));
        return header + "\n" + rows;
    }

    private String buildInvalidCsv() {
        String header = "user_id,date,weight_kg,calories,protein_g,carbs_g,fats_g,cardio_min,cardio_zone," +
                "steps,workout_type,workout_rir,total_sets";
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
            stubReaderContent(buildCsvWithRows(50));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());

            JobControl result = workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);
            verify(workoutRecordRepository, times(1)).saveAll(anyList());
            verify(workflowControlGate, never()).checkpointStep(any(), any());
        }

        @Test
        @DisplayName("saves once, checkpoints, and does not have remaining records to save")
        void savesOnceCheckpointsNoRemainingRecords_whenBelowBatchSize() throws Exception {
            stubValidRowParsing();
            stubReaderContent(buildCsvWithRows(10_000));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());

            JobControl result = workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);
            verify(workoutRecordRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("saves in batches and checkpoints between batches when row count exceeds batch size")
        void savesInBatchesAndCheckpoints_whenAboveBatchSize() throws Exception {
            stubValidRowParsing();
            // batchSize is 10_000; use 10_001 rows to force exactly one mid-file
            // batch save + checkpoint, plus one final remainder save of 1 row.
            stubReaderContent(buildCsvWithRows(10_001));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);

            when(workflowControlGate.checkpointStep(workflowExecutionId, 1))
                    .thenReturn(JobControl.NONE);

            JobControl result = workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.NONE, result);
            verify(workoutRecordRepository, times(2)).saveAll(anyList());
            verify(workflowControlGate, times(1)).checkpointStep(workflowExecutionId, 1);
        }

        @Test
        @DisplayName("stops processing immediately when checkpoint returns TERMINATE mid-file")
        void stopsProcessing_whenCheckpointReturnsTerminate() throws Exception {
            stubValidRowParsing();
            // 20_000 rows so a second batch boundary would occur if processing continued --
            // it must not, since TERMINATE fires at the first checkpoint.
            stubReaderContent(buildCsvWithRows(20_000));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            UUID workflowExecutionId = UUID.randomUUID();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(workflowExecutionId);

            when(workflowControlGate.checkpointStep(workflowExecutionId, 1))
                    .thenReturn(JobControl.TERMINATE);

            JobControl result = workoutCsvIngestionService.ingestCsv(step, workflow, entity);

            assertEquals(JobControl.TERMINATE, result);
            // Only the first batch (10,000 rows) is saved before TERMINATE stops the loop
            verify(workoutRecordRepository, times(1)).saveAll(anyList());
            verify(workflowControlGate, times(1)).checkpointStep(workflowExecutionId, 1);
        }

        @Test
        @DisplayName("wraps IOException from file reading in a RuntimeException")
        void wrapsIOException() {
            filesMock.when(() -> Files.newBufferedReader(any(Path.class)))
                    .thenThrow(new IOException("disk error"));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());

            assertThrows(
                    RuntimeException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            verify(workoutRecordRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("propagates InvalidRowException from row validation without catching it")
        void propagatesInvalidRowException() {
            stubReaderContent(buildCsvWithRows(1));

            when(validationService.parseUserId(any(), any()))
                    .thenThrow(new InvalidRowException("bad user_id"));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());

            assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            verify(workoutRecordRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("throws InvalidRowException when the number of columns is not 14")
        void throwsInvalidRowException_whenColumnsNot14() {
            stubReaderContent(buildInvalidCsv());

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());

            assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );

            verify(workoutRecordRepository, never()).saveAll(anyList());


        }

        @Test
        @DisplayName("logs a warning but does not throw when close() fails after a row validation error")
        void doesNotPropagateCloseFailure_whenBodyAlreadyThrew() throws Exception {
            BufferedReader realReader = new BufferedReader(new StringReader(buildCsvWithRows(1)));
            BufferedReader spyReader = spy(realReader);
            doThrow(new IOException("close failed")).when(spyReader).close();

            filesMock.when(() -> Files.newBufferedReader(any(Path.class)))
                    .thenReturn(spyReader);

            when(validationService.parseUserId(any(), any()))
                    .thenThrow(new InvalidRowException("bad user_id"));

            Step step = Step.builder().stepId(1).build();
            Workflow workflow = Workflow.builder()
                    .data(Data.builder().input("test.csv").build())
                    .build();
            WorkflowExecutionEntity entity = mock(WorkflowExecutionEntity.class);
            when(entity.getWorkflowExecutionId()).thenReturn(UUID.randomUUID());

            // The original InvalidRowException should still be what's thrown -- the
            // close() failure is caught and logged internally, not propagated or attached.
            InvalidRowException ex = assertThrows(
                    InvalidRowException.class,
                    () -> workoutCsvIngestionService.ingestCsv(step, workflow, entity)
            );
            assertEquals("bad user_id", ex.getMessage());
            verify(spyReader).close();
        }
    }
}