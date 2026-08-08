package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.WorkoutRecordEntity;
import com.bryanhuang.workflow.model.workout.WorkoutRecord;
import org.springframework.stereotype.Component;

@Component
public class WorkoutRecordEntityMapper {

    public WorkoutRecord toWorkoutRecord(WorkoutRecordEntity entity) {
        return WorkoutRecord.builder()
                .userId(entity.getUserId())
                .date(entity.getDate())
                .weightKg(entity.getWeightKg())
                .calories(entity.getCalories())
                .proteinG(entity.getProteinG())
                .carbsG(entity.getCarbsG())
                .fatsG(entity.getFatsG())
                .cardioMin(entity.getCardioMin())
                .cardioZone(entity.getCardioZone())
                .steps(entity.getSteps())
                .workoutType(entity.getWorkoutType())
                .workoutRir(entity.getWorkoutRir())
                .totalSets(entity.getTotalSets())
                .sleepH(entity.getSleepH())
                .build();
    }

    public WorkoutRecordEntity toWorkoutRecordEntity(
            WorkoutRecord workoutRecord,
            WorkflowExecutionEntity entity
    ) {
        return WorkoutRecordEntity.builder()
                .workflowExecutionEntity(entity)
                .userId(workoutRecord.getUserId())
                .date(workoutRecord.getDate())
                .weightKg(workoutRecord.getWeightKg())
                .calories(workoutRecord.getCalories())
                .proteinG(workoutRecord.getProteinG())
                .carbsG(workoutRecord.getCarbsG())
                .fatsG(workoutRecord.getFatsG())
                .cardioMin(workoutRecord.getCardioMin())
                .cardioZone(workoutRecord.getCardioZone())
                .steps(workoutRecord.getSteps())
                .workoutType(workoutRecord.getWorkoutType())
                .workoutRir(workoutRecord.getWorkoutRir())
                .totalSets(workoutRecord.getTotalSets())
                .sleepH(workoutRecord.getSleepH())
                .build();
    }
}
