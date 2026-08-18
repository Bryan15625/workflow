package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutUserAggregateEntity;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorkoutUserAggregateEntityMapperTest {

    private final WorkoutUserAggregateEntityMapper mapper = new WorkoutUserAggregateEntityMapper();
    
    @Nested
    @DisplayName("toWorkoutUserAggregateEntity()")
    class ToWorkoutUserAggregateEntityTests {
        
        @Test
        void toWorkoutUserAggregateEntity_shouldMapAllFields() {
            WorkoutUserAggregate aggregate = WorkoutUserAggregate.builder()
                    .userId("user_001")
                    .startWeightKg(BigDecimal.valueOf(72))
                    .endWeightKg(BigDecimal.valueOf(75))
                    .daysTracked(100)
                    .avgCalories(BigDecimal.valueOf(3042))
                    .avgProteinG(BigDecimal.valueOf(160))
                    .avgCarbsG(BigDecimal.valueOf(443))
                    .avgFatsG(BigDecimal.valueOf(70))
                    .avgWeeklyCardioMin(BigDecimal.valueOf(90))
                    .avgCardioZone(CardioZone.fromInt(2))
                    .avgDailySteps(BigDecimal.valueOf(8005))
                    .avgWorkoutsPerWeek(BigDecimal.valueOf(4))
                    .upperBodySessionsPerWeek(2)
                    .lowerBodySessionsPerWeek(2)
                    .avgWorkoutRir(WorkoutRir.fromInt(1))
                    .avgSetsCompleted(BigDecimal.valueOf(20))
                    .avgSleepH(BigDecimal.valueOf(7.5))
                    .build();

            WorkflowExecutionEntity workflowExecutionEntity = WorkflowExecutionEntity.builder()
                    .workflowExecutionId(UUID.randomUUID())
                    .build();

            WorkoutUserAggregateEntity entity = mapper.toWorkoutUserAggregateEntity(
                    aggregate, workflowExecutionEntity);

            assertEquals(workflowExecutionEntity, entity.getWorkflowExecutionEntity());
            assertEquals(aggregate.getUserId(), entity.getUserId());
            assertEquals(aggregate.getStartWeightKg(), entity.getStartWeightKg());
            assertEquals(aggregate.getEndWeightKg(), entity.getEndWeightKg());
            assertEquals(aggregate.getDaysTracked(), entity.getDaysTracked());
            assertEquals(aggregate.getAvgCalories(), entity.getAvgCalories());
            assertEquals(aggregate.getAvgProteinG(), entity.getAvgProteinG());
            assertEquals(aggregate.getAvgCarbsG(), entity.getAvgCarbsG());
            assertEquals(aggregate.getAvgFatsG(), entity.getAvgFatsG());
            assertEquals(aggregate.getAvgWeeklyCardioMin(), entity.getAvgWeeklyCardioMin());
            assertEquals(aggregate.getAvgCardioZone(), entity.getAvgCardioZone());
            assertEquals(aggregate.getAvgDailySteps(), entity.getAvgDailySteps());
            assertEquals(aggregate.getAvgWorkoutsPerWeek(), entity.getAvgWorkoutsPerWeek());
            assertEquals(aggregate.getUpperBodySessionsPerWeek(), entity.getUpperBodySessionsPerWeek());
            assertEquals(aggregate.getLowerBodySessionsPerWeek(), entity.getLowerBodySessionsPerWeek());
            assertEquals(aggregate.getAvgWorkoutRir(), entity.getAvgWorkoutRir());
            assertEquals(aggregate.getAvgSetsCompleted(), entity.getAvgSetsCompleted());
            assertEquals(aggregate.getAvgSleepH(), entity.getAvgSleepH());

        }
    }
    
    @Nested
    @DisplayName("toWorkoutUserAggregate()")
    class ToWorkoutUserAggregateTests {
        
        @Test
        void toWorkoutUserAggregate_shouldMapAllFields() {
            WorkoutUserAggregateEntity entity =
                    WorkoutUserAggregateEntity.builder()
                            .id(UUID.randomUUID())
                            .workflowExecutionEntity(new WorkflowExecutionEntity())
                            .userId("user_0001")
                            .startWeightKg(BigDecimal.valueOf(72))
                            .endWeightKg(BigDecimal.valueOf(75))
                            .daysTracked(100)
                            .avgCalories(BigDecimal.valueOf(3042))
                            .avgProteinG(BigDecimal.valueOf(160))
                            .avgCarbsG(BigDecimal.valueOf(443))
                            .avgFatsG(BigDecimal.valueOf(70))
                            .avgWeeklyCardioMin(BigDecimal.valueOf(90))
                            .avgCardioZone(CardioZone.fromInt(2))
                            .avgDailySteps(BigDecimal.valueOf(8005))
                            .avgWorkoutsPerWeek(BigDecimal.valueOf(4))
                            .upperBodySessionsPerWeek(2)
                            .lowerBodySessionsPerWeek(2)
                            .avgWorkoutRir(WorkoutRir.fromInt(1))
                            .avgSetsCompleted(BigDecimal.valueOf(20))
                            .avgSleepH(BigDecimal.valueOf(7.5))
                            .build();

            WorkoutUserAggregate aggregate = mapper.toWorkoutUserAggregate(entity);

            assertEquals(entity.getUserId(), aggregate.getUserId());
            assertEquals(entity.getStartWeightKg(), aggregate.getStartWeightKg());
            assertEquals(entity.getEndWeightKg(), aggregate.getEndWeightKg());
            assertEquals(entity.getDaysTracked(), aggregate.getDaysTracked());
            assertEquals(entity.getAvgCalories(), aggregate.getAvgCalories());
            assertEquals(entity.getAvgProteinG(), aggregate.getAvgProteinG());
            assertEquals(entity.getAvgCarbsG(), aggregate.getAvgCarbsG());
            assertEquals(entity.getAvgFatsG(), aggregate.getAvgFatsG());
            assertEquals(entity.getAvgWeeklyCardioMin(), aggregate.getAvgWeeklyCardioMin());
            assertEquals(entity.getAvgCardioZone(), aggregate.getAvgCardioZone());
            assertEquals(entity.getAvgDailySteps(), aggregate.getAvgDailySteps());
            assertEquals(entity.getAvgWorkoutsPerWeek(), aggregate.getAvgWorkoutsPerWeek());
            assertEquals(entity.getUpperBodySessionsPerWeek(), aggregate.getUpperBodySessionsPerWeek());
            assertEquals(entity.getLowerBodySessionsPerWeek(), aggregate.getLowerBodySessionsPerWeek());
            assertEquals(entity.getAvgWorkoutRir(), aggregate.getAvgWorkoutRir());
            assertEquals(entity.getAvgSetsCompleted(), aggregate.getAvgSetsCompleted());
            assertEquals(entity.getAvgSleepH(), aggregate.getAvgSleepH());
        }
        
    }
}
