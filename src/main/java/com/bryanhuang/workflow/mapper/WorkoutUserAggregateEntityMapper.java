package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.WorkoutUserAggregateEntity;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import org.springframework.stereotype.Component;

@Component
public class WorkoutUserAggregateEntityMapper {

    public WorkoutUserAggregateEntity toWorkoutUserAggregateEntity(
            WorkoutUserAggregate aggregate,
            WorkflowExecutionEntity entity
    ) {
        return WorkoutUserAggregateEntity.builder()
                .workflowExecutionEntity(entity)
                .userId(aggregate.getUserId())
                .startWeightKg(aggregate.getStartWeightKg())
                .endWeightKg(aggregate.getEndWeightKg())
                .daysTracked(aggregate.getDaysTracked())
                .avgCalories(aggregate.getAvgCalories())
                .avgProteinG(aggregate.getAvgProteinG())
                .avgCarbsG(aggregate.getAvgCarbsG())
                .avgFatsG(aggregate.getAvgFatsG())
                .avgWeeklyCardioMin(aggregate.getAvgWeeklyCardioMin())
                .avgCardioZone(aggregate.getAvgCardioZone())
                .avgDailySteps(aggregate.getAvgDailySteps())
                .avgWorkoutsPerWeek(aggregate.getAvgWorkoutsPerWeek())
                .upperBodySessionsPerWeek(aggregate.getUpperBodySessionsPerWeek())
                .lowerBodySessionsPerWeek(aggregate.getLowerBodySessionsPerWeek())
                .avgWorkoutRir(aggregate.getAvgWorkoutRir())
                .avgSetsCompleted(aggregate.getAvgSetsCompleted())
                .avgSleepH(aggregate.getAvgSleepH())
                .build();
    }

    public WorkoutUserAggregate toWorkoutUserAggregate(WorkoutUserAggregateEntity entity) {

        return WorkoutUserAggregate.builder()
                .userId(entity.getUserId())
                .startWeightKg(entity.getStartWeightKg())
                .endWeightKg(entity.getEndWeightKg())
                .daysTracked(entity.getDaysTracked())
                .avgCalories(entity.getAvgCalories())
                .avgProteinG(entity.getAvgProteinG())
                .avgCarbsG(entity.getAvgCarbsG())
                .avgFatsG(entity.getAvgFatsG())
                .avgWeeklyCardioMin(entity.getAvgWeeklyCardioMin())
                .avgCardioZone(entity.getAvgCardioZone())
                .avgDailySteps(entity.getAvgDailySteps())
                .avgWorkoutsPerWeek(entity.getAvgWorkoutsPerWeek())
                .upperBodySessionsPerWeek(entity.getUpperBodySessionsPerWeek())
                .lowerBodySessionsPerWeek(entity.getLowerBodySessionsPerWeek())
                .avgWorkoutRir(entity.getAvgWorkoutRir())
                .avgSetsCompleted(entity.getAvgSetsCompleted())
                .avgSleepH(entity.getAvgSleepH())
                .build();

    }
}
