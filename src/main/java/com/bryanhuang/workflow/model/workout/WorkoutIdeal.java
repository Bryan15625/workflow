package com.bryanhuang.workflow.model.workout;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkoutIdeal {

    private Integer daysTracked;
    private Range idealEndWeightKg;
    private Range idealCalories;
    private Range idealProteinG;
    private Range idealFatG;
    private Range idealCarbG;
    private Range idealWeeklyCardioMin;
    private Range idealDailySteps;
    private Range idealUpperBodySessionsPerWeek;
    private Range idealLowerBodySessionsPerWeek;
    private Range idealWorkoutsPerWeek;
    private Range idealWorkoutRir;
    private Range idealSleepH;

    public WorkoutIdeal(
            Integer daysTracked,
            Range idealEndWeightKg,
            Range idealCalories,
            Range idealProteinG,
            Range idealFatG,
            Range idealCarbG,
            Range idealWeeklyCardioMin,
            Range idealDailySteps,
            Range idealUpperBodySessionsPerWeek,
            Range idealLowerBodySessionsPerWeek,
            Range idealWorkoutsPerWeek,
            Range idealWorkoutRir,
            Range idealSleepH
    ) {
        this.daysTracked = daysTracked;
        this.idealEndWeightKg = idealEndWeightKg;
        this.idealCalories = idealCalories;
        this.idealProteinG = idealProteinG;
        this.idealFatG = idealFatG;
        this.idealCarbG = idealCarbG;
        this.idealWeeklyCardioMin = idealWeeklyCardioMin;
        this.idealDailySteps = idealDailySteps;
        this.idealUpperBodySessionsPerWeek = idealUpperBodySessionsPerWeek;
        this.idealLowerBodySessionsPerWeek = idealLowerBodySessionsPerWeek;
        this.idealWorkoutsPerWeek = idealWorkoutsPerWeek;
        this.idealWorkoutRir = idealWorkoutRir;
        this.idealSleepH = idealSleepH;
    }
}