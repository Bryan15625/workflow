package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.entity.WorkoutRecordEntity;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Getter
public class WorkoutUserAggregateAccumulator {

    private final String userId;

    private LocalDate earliestDate;
    private LocalDate latestDate;

    private BigDecimal startWeightKg;
    private BigDecimal endWeightKg;

    private long recordCount = 0;
    private BigDecimal sumCalories = BigDecimal.ZERO;
    private BigDecimal sumProteinG = BigDecimal.ZERO;
    private BigDecimal sumCarbsG = BigDecimal.ZERO;
    private BigDecimal sumFatsG = BigDecimal.ZERO;
    private BigDecimal sumCardioMin = BigDecimal.ZERO;
    private BigDecimal sumCardioZoneOrdinal = BigDecimal.ZERO;
    private BigDecimal sumSleepH = BigDecimal.ZERO;
    private BigDecimal sumSteps = BigDecimal.ZERO;

    private long trainingDayCount = 0;
    private BigDecimal sumWorkoutRirValue = BigDecimal.ZERO;
    private BigDecimal sumSetsCompleted = BigDecimal.ZERO;
    private long upperBodySessionCount = 0;
    private long lowerBodySessionCount = 0;

    WorkoutUserAggregateAccumulator(String userId) {
        this.userId = userId;
    }

    void accumulate(WorkoutRecordEntity record) {
        if (earliestDate == null) {
            earliestDate = record.getDate();
            startWeightKg = record.getWeightKg();
        }
        latestDate = record.getDate();
        endWeightKg = record.getWeightKg();

        recordCount++;
        sumCalories = sumCalories.add(BigDecimal.valueOf(record.getCalories()));
        sumProteinG = sumProteinG.add(BigDecimal.valueOf(record.getProteinG()));
        sumCarbsG = sumCarbsG.add(BigDecimal.valueOf(record.getCarbsG()));
        sumFatsG = sumFatsG.add(BigDecimal.valueOf(record.getFatsG()));
        sumCardioMin = sumCardioMin.add(BigDecimal.valueOf(record.getCardioMin()));
        sumCardioZoneOrdinal = sumCardioZoneOrdinal.add(BigDecimal.valueOf(record.getCardioZone().ordinal()));
        sumSleepH = sumSleepH.add(record.getSleepH());
        sumSteps = sumSteps.add(BigDecimal.valueOf(record.getSteps()));

        if (record.getWorkoutType() != WorkoutType.REST) {
            trainingDayCount++;
            // WorkoutRir ordinal is shifted by 1 relative to raw RIR value
            sumWorkoutRirValue = sumWorkoutRirValue.add(
                    BigDecimal.valueOf(record.getWorkoutRir().ordinal() - 1));
            sumSetsCompleted = sumSetsCompleted.add(BigDecimal.valueOf(record.getTotalSets()));

            // PUSH/PULL/UPPER count as upper body, LEGS as lower.
            if (record.getWorkoutType() == WorkoutType.LEGS) {
                lowerBodySessionCount++;
            } else {
                upperBodySessionCount++;
            }
        }
    }

    private int getDaysTracked() {
        return (int) ChronoUnit.DAYS.between(earliestDate, latestDate) + 1;
    }

    private BigDecimal avg(BigDecimal sum, long count) {
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        return sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    BigDecimal getAvgCalories() { return avg(sumCalories, recordCount); }
    BigDecimal getAvgProteinG() { return avg(sumProteinG, recordCount); }
    BigDecimal getAvgCarbsG() { return avg(sumCarbsG, recordCount); }
    BigDecimal getAvgFatsG() { return avg(sumFatsG, recordCount); }
    BigDecimal getAvgSleepH() { return avg(sumSleepH, recordCount); }
    BigDecimal getAvgSetsCompleted() { return avg(sumSetsCompleted, trainingDayCount); }
    BigDecimal getAvgDailySteps() { return avg(sumSteps, recordCount); }

    CardioZone getAvgCardioZone() {
        if (recordCount == 0) {
            return CardioZone.NONE;
        }
        BigDecimal avgOrdinal = avg(sumCardioZoneOrdinal, recordCount);
        int rounded = avgOrdinal.setScale(0, RoundingMode.HALF_UP).intValue();
        rounded = Math.clamp(rounded, 0, CardioZone.values().length - 1);
        return rounded == 0 ? CardioZone.NONE : CardioZone.fromInt(rounded);
    }

    WorkoutRir getAvgWorkoutRir() {
        if (trainingDayCount == 0) {
            return WorkoutRir.NONE;
        }
        BigDecimal avgRir = avg(sumWorkoutRirValue, trainingDayCount);
        int rounded = avgRir.setScale(0, RoundingMode.HALF_UP).intValue();
        rounded = Math.clamp(rounded, 0, 10);
        return WorkoutRir.fromInt(rounded);
    }

    BigDecimal getWeeksTracked() {
        return BigDecimal.valueOf(getDaysTracked()).divide(BigDecimal.valueOf(7), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal getAvgWeeklyCardioMin() {
        return sumCardioMin.divide(getWeeksTracked(), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getAvgWorkoutsPerWeek() {
        return BigDecimal.valueOf(trainingDayCount).divide(getWeeksTracked(), 2, RoundingMode.HALF_UP);
    }

    public int getUpperBodySessionsPerWeek() {
        return BigDecimal.valueOf(upperBodySessionCount).divide(getWeeksTracked(), 0, RoundingMode.HALF_UP).intValue();
    }

    public int getLowerBodySessionsPerWeek() {
        return BigDecimal.valueOf(lowerBodySessionCount).divide(getWeeksTracked(), 0, RoundingMode.HALF_UP).intValue();
    }

    WorkoutUserAggregate toModel() {
        return WorkoutUserAggregate.builder()
                .userId(userId)
                .startWeightKg(startWeightKg)
                .endWeightKg(endWeightKg)
                .daysTracked(getDaysTracked())
                .avgCalories(getAvgCalories())
                .avgProteinG(getAvgProteinG())
                .avgCarbsG(getAvgCarbsG())
                .avgFatsG(getAvgFatsG())
                .avgWeeklyCardioMin(getAvgWeeklyCardioMin())
                .avgCardioZone(getAvgCardioZone())
                .avgWorkoutsPerWeek(getAvgWorkoutsPerWeek())
                .upperBodySessionsPerWeek(getUpperBodySessionsPerWeek())
                .lowerBodySessionsPerWeek(getLowerBodySessionsPerWeek())
                .avgWorkoutRir(getAvgWorkoutRir())
                .avgSetsCompleted(getAvgSetsCompleted())
                .avgSleepH(getAvgSleepH())
                .avgDailySteps(getAvgDailySteps())
                .build();
    }
}