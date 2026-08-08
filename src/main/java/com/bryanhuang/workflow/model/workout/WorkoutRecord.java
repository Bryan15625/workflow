package com.bryanhuang.workflow.model.workout;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class WorkoutRecord {
    private String userId;
    private LocalDate date;
    private BigDecimal weightKg;
    private Integer calories;
    private Integer proteinG;
    private Integer carbsG;
    private Integer fatsG;
    private Integer cardioMin;
    private CardioZone cardioZone;
    private Integer steps;
    private WorkoutType workoutType;
    private WorkoutRir workoutRir;
    private Integer totalSets;
    private BigDecimal sleepH;

    public WorkoutRecord(
        String userId,
        LocalDate date,
        BigDecimal weightKg,
        Integer calories,
        Integer proteinG,
        Integer carbsG,
        Integer fatsG,
        Integer cardioMin,
        CardioZone cardioZone,
        Integer steps,
        WorkoutType workoutType,
        WorkoutRir workoutRir,
        Integer totalSets,
        BigDecimal sleepH
    ) {
        this.userId = userId;
        this.date = date;
        this.weightKg = weightKg;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatsG = fatsG;
        this.cardioMin = cardioMin;
        this.cardioZone = cardioZone;
        this.steps = steps;
        this.workoutType = workoutType;
        this.workoutRir = workoutRir;
        this.totalSets = totalSets;
        this.sleepH = sleepH;
    }
}
