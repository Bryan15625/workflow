package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutRecordEntity;
import com.bryanhuang.workflow.model.workflow.JobStatus;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRecord;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkoutRecordEntityMapperTest {

    private final WorkoutRecordEntityMapper mapper = new WorkoutRecordEntityMapper();

    @Nested
    @DisplayName("toWorkoutRecord")
    class ToWorkoutRecordTests {

        @Test
        void toWorkoutRecord_shouldMapAllFields() {

            WorkoutRecordEntity workoutRecordEntity = WorkoutRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .workflowExecutionEntity(WorkflowExecutionEntity.builder().build())
                    .userId("user_001")
                    .date(LocalDate.now())
                    .weightKg(BigDecimal.valueOf(72))
                    .calories(2660)
                    .proteinG(170)
                    .carbsG(270)
                    .fatsG(100)
                    .cardioMin(45)
                    .cardioZone(CardioZone.TWO)
                    .steps(11000)
                    .workoutType(WorkoutType.PUSH)
                    .workoutRir(WorkoutRir.FOUR)
                    .totalSets(20)
                    .sleepH(BigDecimal.valueOf(7.5))
                    .build();

            WorkoutRecord workoutRecord = mapper.toWorkoutRecord(workoutRecordEntity);

            assertEquals(workoutRecordEntity.getUserId(), workoutRecord.getUserId());
            assertEquals(workoutRecordEntity.getDate(), workoutRecord.getDate());
            assertEquals(workoutRecordEntity.getWeightKg(), workoutRecord.getWeightKg());
            assertEquals(workoutRecordEntity.getCalories(), workoutRecord.getCalories());
            assertEquals(workoutRecordEntity.getProteinG(), workoutRecord.getProteinG());
            assertEquals(workoutRecordEntity.getCarbsG(), workoutRecord.getCarbsG());
            assertEquals(workoutRecordEntity.getFatsG(), workoutRecord.getFatsG());
            assertEquals(workoutRecordEntity.getCardioMin(), workoutRecord.getCardioMin());
            assertEquals(workoutRecordEntity.getCardioZone(), workoutRecord.getCardioZone());
            assertEquals(workoutRecordEntity.getSteps(), workoutRecord.getSteps());
            assertEquals(workoutRecordEntity.getWorkoutType(), workoutRecord.getWorkoutType());
            assertEquals(workoutRecordEntity.getWorkoutRir(), workoutRecord.getWorkoutRir());
            assertEquals(workoutRecordEntity.getTotalSets(), workoutRecord.getTotalSets());
            assertEquals(workoutRecordEntity.getSleepH(), workoutRecord.getSleepH());

        }

    }

    @Nested
    @DisplayName("toWorkoutRecordEntity")
    class ToWorkoutRecordEntityTests {

        @Test
        void toWorkoutRecordEntity_shouldMapAllFields() {

            WorkflowExecutionEntity workflowExecutionEntity = WorkflowExecutionEntity.builder()
                            .workflowExecutionId(UUID.randomUUID())
                            .workflowId(UUID.randomUUID())
                            .status(JobStatus.READY)
                            .createdAt(Instant.now().minusSeconds(15))
                            .startedAt(Instant.now())
                            .completedAt(null)
                            .errorMessage(null)
                            .build();

            WorkoutRecord workoutRecord =
                    WorkoutRecord.builder()
                            .userId("user_001")
                            .date(LocalDate.now())
                            .weightKg(BigDecimal.valueOf(72))
                            .calories(2660)
                            .proteinG(170)
                            .carbsG(270)
                            .fatsG(100)
                            .cardioMin(45)
                            .cardioZone(CardioZone.TWO)
                            .steps(11000)
                            .workoutType(WorkoutType.PUSH)
                            .workoutRir(WorkoutRir.FOUR).totalSets(20)
                            .sleepH(BigDecimal.valueOf(7.5)).build();


            WorkoutRecordEntity workoutRecordEntity = mapper.toWorkoutRecordEntity(workoutRecord,
                    workflowExecutionEntity);

            assertEquals(workflowExecutionEntity, workoutRecordEntity.getWorkflowExecutionEntity());
            assertEquals(workoutRecord.getUserId(), workoutRecordEntity.getUserId());
            assertEquals(workoutRecord.getDate(), workoutRecordEntity.getDate());
            assertEquals(workoutRecord.getWeightKg(), workoutRecordEntity.getWeightKg());
            assertEquals(workoutRecord.getCalories(), workoutRecordEntity.getCalories());
            assertEquals(workoutRecord.getProteinG(), workoutRecordEntity.getProteinG());
            assertEquals(workoutRecord.getCarbsG(), workoutRecordEntity.getCarbsG());
            assertEquals(workoutRecord.getFatsG(), workoutRecordEntity.getFatsG());
            assertEquals(workoutRecord.getCardioMin(), workoutRecordEntity.getCardioMin());
            assertEquals(workoutRecord.getCardioZone(), workoutRecordEntity.getCardioZone());
            assertEquals(workoutRecord.getSteps(), workoutRecordEntity.getSteps());
            assertEquals(workoutRecord.getWorkoutType(), workoutRecordEntity.getWorkoutType());
            assertEquals(workoutRecord.getWorkoutRir(), workoutRecordEntity.getWorkoutRir());
            assertEquals(workoutRecord.getTotalSets(), workoutRecordEntity.getTotalSets());
            assertEquals(workoutRecord.getSleepH(), workoutRecordEntity.getSleepH());
        }
    }

}
