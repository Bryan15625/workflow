package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.WorkoutRecordEntity;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkoutUserAggregateAccumulatorTest {

    @Nested
    @DisplayName("accumulate()")
    class AccumulateTests {

        @Test
        @DisplayName("accumulates a record correctly")
        void accumulatesRecordCorrectly() {
            WorkoutUserAggregateAccumulator accumulator = new WorkoutUserAggregateAccumulator("user_001");
            WorkoutRecordEntity record1 = WorkoutRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .workflowExecutionEntity(new WorkflowExecutionEntity())
                    .userId("user_001")
                    .date(LocalDate.now().minusDays(1))
                    .weightKg(BigDecimal.valueOf(72.2))
                    .calories(3140)
                    .proteinG(160)
                    .carbsG(400)
                    .fatsG(100)
                    .cardioMin(10)
                    .cardioZone(CardioZone.TWO)
                    .steps(15000)
                    .workoutType(WorkoutType.PUSH)
                    .workoutRir(WorkoutRir.ONE)
                    .totalSets(20)
                    .sleepH(BigDecimal.valueOf(7.5))
                    .build();

            WorkoutRecordEntity record2 = WorkoutRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .workflowExecutionEntity(new WorkflowExecutionEntity())
                    .userId("user_001")
                    .date(LocalDate.now())
                    .weightKg(BigDecimal.valueOf(74.4))
                    .calories(3160)
                    .proteinG(180)
                    .carbsG(420)
                    .fatsG(120)
                    .cardioMin(10)
                    .cardioZone(CardioZone.FOUR)
                    .steps(13000)
                    .workoutType(WorkoutType.LEGS)
                    .workoutRir(WorkoutRir.THREE)
                    .totalSets(30)
                    .sleepH(BigDecimal.valueOf(8.5))
                    .build();

            accumulator.accumulate(record1);
            accumulator.accumulate(record2);

            WorkoutUserAggregate result = accumulator.toModel();

            assertEquals(new BigDecimal("72.2"), result.getStartWeightKg());
            assertEquals(new BigDecimal("74.4"), result.getEndWeightKg());
            assertEquals(2, result.getDaysTracked());
            assertEquals(new BigDecimal("3150.00"), result.getAvgCalories());
            assertEquals(new BigDecimal("170.00"), result.getAvgProteinG());
            assertEquals(new BigDecimal("410.00"), result.getAvgCarbsG());
            assertEquals(new BigDecimal("110.00"), result.getAvgFatsG());
            assertEquals(new BigDecimal("70.00"), result.getAvgWeeklyCardioMin());
            assertEquals(CardioZone.THREE, result.getAvgCardioZone());
            assertEquals(new BigDecimal("14000.00"), result.getAvgDailySteps());
            assertEquals(new BigDecimal("7.00"), result.getAvgWorkoutsPerWeek());
            assertEquals(4, result.getUpperBodySessionsPerWeek());
            assertEquals(4, result.getLowerBodySessionsPerWeek());
            assertEquals(WorkoutRir.TWO, result.getAvgWorkoutRir());
        }

        @Test
        @DisplayName("accumulates records for two people correctly")
        void accumulatesRecordCorrectly_zeroOrNoneCases() {
            WorkoutUserAggregateAccumulator accumulator1 = new WorkoutUserAggregateAccumulator(
                    "user_001");
            WorkoutUserAggregateAccumulator accumulator2 = new WorkoutUserAggregateAccumulator(
                    "user_002");

            WorkoutRecordEntity user1Record = WorkoutRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .workflowExecutionEntity(new WorkflowExecutionEntity())
                    .userId("user_001")
                    .date(LocalDate.now().minusDays(1))
                    .weightKg(BigDecimal.valueOf(72.2))
                    .calories(3140)
                    .proteinG(160)
                    .carbsG(400)
                    .fatsG(100)
                    .cardioMin(0)
                    .cardioZone(CardioZone.NONE)
                    .steps(15000)
                    .workoutType(WorkoutType.REST)
                    .workoutRir(WorkoutRir.NONE)
                    .totalSets(20)
                    .sleepH(BigDecimal.valueOf(7.5))
                    .build();

            WorkoutRecordEntity user2Record1 = WorkoutRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .workflowExecutionEntity(new WorkflowExecutionEntity())
                    .userId("user_002")
                    .date(LocalDate.now().minusDays(1))
                    .weightKg(BigDecimal.valueOf(72.2))
                    .calories(3140)
                    .proteinG(160)
                    .carbsG(400)
                    .fatsG(100)
                    .cardioMin(0)
                    .cardioZone(CardioZone.NONE)
                    .steps(15000)
                    .workoutType(WorkoutType.PUSH)
                    .workoutRir(WorkoutRir.ONE)
                    .totalSets(20)
                    .sleepH(BigDecimal.valueOf(7.5))
                    .build();

            WorkoutRecordEntity user2Record2 = WorkoutRecordEntity.builder()
                    .id(UUID.randomUUID())
                    .workflowExecutionEntity(new WorkflowExecutionEntity())
                    .userId("user_002")
                    .date(LocalDate.now())
                    .weightKg(BigDecimal.valueOf(74.4))
                    .calories(3160)
                    .proteinG(180)
                    .carbsG(420)
                    .fatsG(120)
                    .cardioMin(0)
                    .cardioZone(CardioZone.NONE)
                    .steps(13000)
                    .workoutType(WorkoutType.LEGS)
                    .workoutRir(WorkoutRir.THREE)
                    .totalSets(30)
                    .sleepH(BigDecimal.valueOf(8.5))
                    .build();

            accumulator1.accumulate(user1Record);

            WorkoutUserAggregate result1 = accumulator1.toModel();

            assertEquals(new BigDecimal("72.2"), result1.getStartWeightKg());
            assertEquals(new BigDecimal("72.2"), result1.getEndWeightKg());
            assertEquals(1, result1.getDaysTracked());
            assertEquals(new BigDecimal("3140.00"), result1.getAvgCalories());
            assertEquals(new BigDecimal("160.00"), result1.getAvgProteinG());
            assertEquals(new BigDecimal("400.00"), result1.getAvgCarbsG());
            assertEquals(new BigDecimal("100.00"), result1.getAvgFatsG());
            assertEquals(new BigDecimal("0.00"), result1.getAvgWeeklyCardioMin());
            assertEquals(CardioZone.NONE, result1.getAvgCardioZone());
            assertEquals(new BigDecimal("15000.00"), result1.getAvgDailySteps());
            assertEquals(new BigDecimal("0.00"), result1.getAvgWorkoutsPerWeek());
            assertEquals(0, result1.getUpperBodySessionsPerWeek());
            assertEquals(0, result1.getLowerBodySessionsPerWeek());
            assertEquals(WorkoutRir.NONE, result1.getAvgWorkoutRir());

            accumulator2.accumulate(user2Record1);
            accumulator2.accumulate(user2Record2);

            WorkoutUserAggregate result2 = accumulator2.toModel();

            assertEquals(new BigDecimal("72.2"), result2.getStartWeightKg());
            assertEquals(new BigDecimal("74.4"), result2.getEndWeightKg());
            assertEquals(2, result2.getDaysTracked());
            assertEquals(new BigDecimal("3150.00"), result2.getAvgCalories());
            assertEquals(new BigDecimal("170.00"), result2.getAvgProteinG());
            assertEquals(new BigDecimal("410.00"), result2.getAvgCarbsG());
            assertEquals(new BigDecimal("110.00"), result2.getAvgFatsG());
            assertEquals(new BigDecimal("0.00"), result2.getAvgWeeklyCardioMin());
            assertEquals(CardioZone.NONE, result2.getAvgCardioZone());
            assertEquals(new BigDecimal("14000.00"), result2.getAvgDailySteps());
            assertEquals(new BigDecimal("7.00"), result2.getAvgWorkoutsPerWeek());
            assertEquals(4, result2.getUpperBodySessionsPerWeek());
            assertEquals(4, result2.getLowerBodySessionsPerWeek());
            assertEquals(WorkoutRir.TWO, result2.getAvgWorkoutRir());
        }

        @Test
        @DisplayName("returns NONE/zero defaults when accumulator has no accumulated records")
        void returnsDefaults_whenNoRecordsAccumulated() {
            WorkoutUserAggregateAccumulator emptyAccumulator = new WorkoutUserAggregateAccumulator("user_003");

            assertEquals(CardioZone.NONE, emptyAccumulator.getAvgCardioZone());
            assertEquals(WorkoutRir.NONE, emptyAccumulator.getAvgWorkoutRir());
        }
    }
}
