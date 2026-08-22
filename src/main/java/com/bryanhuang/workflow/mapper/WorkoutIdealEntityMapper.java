package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.RangeEmbeddable;
import com.bryanhuang.workflow.entity.workout.WorkoutIdealEntity;
import com.bryanhuang.workflow.model.workout.Range;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class WorkoutIdealEntityMapper {

    public WorkoutIdealEntity toWorkoutIdealEntity(
            WorkoutIdeal ideal,
            WorkflowExecutionEntity entity
    ) {
        return WorkoutIdealEntity.builder()
                .workflowExecutionEntity(entity)
                .daysTracked(ideal.getDaysTracked())
                .idealEndWeightKg(toRangeEmbeddable(ideal.getIdealEndWeightKg()))
                .idealCalories(toRangeEmbeddable(ideal.getIdealCalories()))
                .idealProteinG(toRangeEmbeddable(ideal.getIdealProteinG()))
                .idealFatG(toRangeEmbeddable(ideal.getIdealFatG()))
                .idealCarbG(toRangeEmbeddable(ideal.getIdealCarbG()))
                .idealWeeklyCardioMin(toRangeEmbeddable(ideal.getIdealWeeklyCardioMin()))
                .idealDailySteps(toRangeEmbeddable(ideal.getIdealDailySteps()))
                .idealUpperBodySessionsPerWeek(toRangeEmbeddable(ideal.getIdealUpperBodySessionsPerWeek()))
                .idealLowerBodySessionsPerWeek(toRangeEmbeddable(ideal.getIdealLowerBodySessionsPerWeek()))
                .idealWorkoutsPerWeek(toRangeEmbeddable(ideal.getIdealWorkoutsPerWeek()))
                .idealWorkoutRir(toRangeEmbeddable(ideal.getIdealWorkoutRir()))
                .idealSleepH(toRangeEmbeddable(ideal.getIdealSleepH()))
                .build();
    }

    public WorkoutIdeal toWorkoutIdeal(WorkoutIdealEntity entity) {
        return WorkoutIdeal.builder()
                .daysTracked(entity.getDaysTracked())
                .idealEndWeightKg(toRange(entity.getIdealEndWeightKg()))
                .idealCalories(toRange(entity.getIdealCalories()))
                .idealProteinG(toRange(entity.getIdealProteinG()))
                .idealFatG(toRange(entity.getIdealFatG()))
                .idealCarbG(toRange(entity.getIdealCarbG()))
                .idealWeeklyCardioMin(toRange(entity.getIdealWeeklyCardioMin()))
                .idealDailySteps(toRange(entity.getIdealDailySteps()))
                .idealUpperBodySessionsPerWeek(toRange(entity.getIdealUpperBodySessionsPerWeek()))
                .idealLowerBodySessionsPerWeek(toRange(entity.getIdealLowerBodySessionsPerWeek()))
                .idealWorkoutsPerWeek(toRange(entity.getIdealWorkoutsPerWeek()))
                .idealWorkoutRir(toRange(entity.getIdealWorkoutRir()))
                .idealSleepH(toRange(entity.getIdealSleepH()))
                .build();
    }

    private RangeEmbeddable toRangeEmbeddable(Range range) {
        if (range == null) {
            return null;
        }

        return RangeEmbeddable.builder()
                .min(BigDecimal.valueOf(range.min()))
                .max(BigDecimal.valueOf(range.max()))
                .build();
    }

    private Range toRange(RangeEmbeddable range) {
        if (range == null) {
            return null;
        }

        return new Range(
                range.getMin().doubleValue(),
                range.getMax().doubleValue()
        );
    }
}