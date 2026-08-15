package com.bryanhuang.workflow.model.workout;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class WorkoutUserAggregate {

    private String userId;

    private BigDecimal startWeightKg;
    private BigDecimal endWeightKg;
    private Integer daysTracked;

    private BigDecimal avgCalories;
    private BigDecimal avgProteinG;
    private BigDecimal avgCarbsG;
    private BigDecimal avgFatsG;

    private BigDecimal avgWeeklyCardioMin;
    private CardioZone avgCardioZone;
    private BigDecimal avgDailySteps;

    private BigDecimal avgWorkoutsPerWeek;
    private Integer upperBodySessionsPerWeek;
    private Integer lowerBodySessionsPerWeek;

    private WorkoutRir avgWorkoutRir;
    private BigDecimal avgSetsCompleted;
    private BigDecimal avgSleepH;

    public WorkoutUserAggregate(
            String userId,
            BigDecimal startWeightKg,
            BigDecimal endWeightKg,
            Integer daysTracked,
            BigDecimal avgCalories,
            BigDecimal avgProteinG,
            BigDecimal avgCarbsG,
            BigDecimal avgFatsG,
            BigDecimal avgWeeklyCardioMin,
            CardioZone avgCardioZone,
            BigDecimal avgDailySteps,
            BigDecimal avgWorkoutsPerWeek,
            Integer upperBodySessionsPerWeek,
            Integer lowerBodySessionsPerWeek,
            WorkoutRir avgWorkoutRir,
            BigDecimal avgSetsCompleted,
            BigDecimal avgSleepH
    ) {
        this.userId = userId;
        this.startWeightKg = startWeightKg;
        this.endWeightKg = endWeightKg;
        this.daysTracked = daysTracked;
        this.avgCalories = avgCalories;
        this.avgCarbsG = avgCarbsG;
        this.avgProteinG = avgProteinG;
        this.avgFatsG = avgFatsG;
        this.avgWeeklyCardioMin = avgWeeklyCardioMin;
        this.avgCardioZone = avgCardioZone;
        this.avgDailySteps = avgDailySteps;
        this.avgWorkoutsPerWeek = avgWorkoutsPerWeek;
        this.upperBodySessionsPerWeek = upperBodySessionsPerWeek;
        this.lowerBodySessionsPerWeek = lowerBodySessionsPerWeek;
        this.avgWorkoutRir = avgWorkoutRir;
        this.avgSetsCompleted = avgSetsCompleted;
        this.avgSleepH = avgSleepH;
    }

}
