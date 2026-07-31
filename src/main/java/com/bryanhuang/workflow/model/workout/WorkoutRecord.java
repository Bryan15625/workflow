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
}
