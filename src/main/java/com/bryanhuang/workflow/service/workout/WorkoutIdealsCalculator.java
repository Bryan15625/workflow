package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.Sex;
import com.bryanhuang.workflow.model.workout.Range;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import org.springframework.stereotype.Service;

import java.util.TreeMap;

@Service
public class WorkoutIdealsCalculator {

    private static final double TEF = 1.1;
    private static final double KG_OF_FAT = 7700;

    private static final TreeMap<Double, double[]> CARDIO_ZONE_CAL_PER_MIN = new TreeMap<>();
    private static final TreeMap<Double, Double> CAL_PER_STEP = new TreeMap<>();

    static {
        CARDIO_ZONE_CAL_PER_MIN.put(54.0, new double[]{1.9, 3.8, 6.2, 8.6, 11.4});
        CARDIO_ZONE_CAL_PER_MIN.put(68.0, new double[]{2.4, 4.8, 7.7, 10.7, 14.3});
        CARDIO_ZONE_CAL_PER_MIN.put(82.0, new double[]{2.9, 5.7, 9.3, 12.9, 17.2});
        CARDIO_ZONE_CAL_PER_MIN.put(95.0, new double[]{3.3, 6.7, 10.8, 15.0, 20.0});
        CARDIO_ZONE_CAL_PER_MIN.put(109.0, new double[]{3.8, 7.6, 12.4, 17.2, 22.9});

        CAL_PER_STEP.put(54.0, 0.023);
        CAL_PER_STEP.put(68.0, 0.029);
        CAL_PER_STEP.put(82.0, 0.034);
        CAL_PER_STEP.put(95.0, 0.04);
        CAL_PER_STEP.put(109.0, 0.046);
    }

    // BMR using the Mifflin-St Jeor equation
    private double calculateBmr(int age, Sex sex, double weightKg, double heightCm) {
        double base = 10 * weightKg + 6.25 * heightCm - 5 * age;
        return sex == Sex.MALE ? base + 5 : base - 161;
    }

    private double caloriesPerStep(double weightKg) {
        return interpolate(CAL_PER_STEP, weightKg);
    }

    private double cardioCaloriesPerMin(double weightKg, int zone) {
        var floorEntry = CARDIO_ZONE_CAL_PER_MIN.floorEntry(weightKg);
        var ceilingEntry = CARDIO_ZONE_CAL_PER_MIN.ceilingEntry(weightKg);

        if (floorEntry == null) return ceilingEntry.getValue()[zone - 1];
        if (ceilingEntry == null) return floorEntry.getValue()[zone - 1];
        if (floorEntry.getKey().equals(ceilingEntry.getKey())) return floorEntry.getValue()[zone - 1];

        double w1 = floorEntry.getKey(), w2 = ceilingEntry.getKey();
        double c1 = floorEntry.getValue()[zone - 1], c2 = ceilingEntry.getValue()[zone - 1];
        double fraction = (weightKg - w1) / (w2 - w1);
        return c1 + fraction * (c2 - c1);
    }

    private double interpolate(TreeMap<Double, Double> table, double weightKg) {
        var floorEntry = table.floorEntry(weightKg);
        var ceilingEntry = table.ceilingEntry(weightKg);

        if (floorEntry == null) return ceilingEntry.getValue();
        if (ceilingEntry == null) return floorEntry.getValue();
        if (floorEntry.getKey().equals(ceilingEntry.getKey())) return floorEntry.getValue();

        double w1 = floorEntry.getKey(), w2 = ceilingEntry.getKey();
        double c1 = floorEntry.getValue(), c2 = ceilingEntry.getValue();
        double fraction = (weightKg - w1) / (w2 - w1);
        return c1 + fraction * (c2 - c1);
    }

    // Based on the ideal weight change per week, an ideal weight change per day can be derived
    // Based on that daily weight change, a calorie deficit can be prescribed
    private Range calorieOffsetRange(double weightKg, Goal goal) {
        Range rateRange = safeWeeklyChangeRatePercent(goal);
        double minWeeklyChangeKg = weightKg * (rateRange.min() / 100.0);
        double maxWeeklyChangeKg = weightKg * (rateRange.max() / 100.0);
        double minDailyCalories = (minWeeklyChangeKg * KG_OF_FAT) / 7.0;
        double maxDailyCalories = (maxWeeklyChangeKg * KG_OF_FAT) / 7.0;

        return goal == Goal.MUSCLE_GAIN
                ? new Range(minDailyCalories, maxDailyCalories)
                : new Range(-maxDailyCalories, -minDailyCalories); // FAT_LOSS: deficit is negative
    }

    private Range proteinTargetGrams(double weightKg, Goal goal) {
        return goal == Goal.MUSCLE_GAIN
                ? new Range(1.6 * weightKg, 2.2 * weightKg)
                : new Range(1.8 * weightKg, 2.2 * weightKg); // FAT_LOSS
    }

    private Range fatTargetGrams(double weightKg, Goal goal) {
        return goal == Goal.MUSCLE_GAIN
                ? new Range(0.7 * weightKg, 1.0 * weightKg)
                : new Range(0.6 * weightKg, 0.8 * weightKg); // FAT_LOSS
    }

    private Range carbTargetGrams(double weightKg, Goal goal) {
        return goal == Goal.MUSCLE_GAIN
                ? new Range(3.0 * weightKg, 5.0 * weightKg)
                : new Range(2.0 * weightKg, 4.0 * weightKg); // FAT_LOSS
    }

    private Range safeWeeklyChangeRatePercent(Goal goal) {
        return goal == Goal.MUSCLE_GAIN
                ? new Range(0.125, 0.25)
                : new Range(0.5, 1.0); // FAT_LOSS
    }

    private Range idealSleepHours() {
        return new Range(7.0, 9.0);
    }

    // 7000-10000 steps/day is optimal for moderate-intensity training, but 15000 is a reasonable upper bound.
    private Range idealDailySteps() {
        return new Range(7000, 15000);
    }

    private Range idealWeeklyCardioMin() {
        return new Range(150, 300); // WHO/CDC moderate-intensity guideline
    }

    private Range idealSessionsPerMuscleGroupPerWeek() {
        return new Range(2, 3);
    }

    // RIR 0-3 is generally considered optimal for hypertrophy/strength training
    private Range idealTrainingRir() {
        return new Range(0, 3);
    }

    private Range idealWorkoutsPerWeek(Range sessionsPerMuscleGroup) {
        return new Range(
                sessionsPerMuscleGroup.min() * 2, // upper + lower
                sessionsPerMuscleGroup.max() * 2
        );
    }

    public WorkoutIdeal calculateIdeal(CohortProfile cohortProfile) {
        double startWeightKg = cohortProfile.getWeightKg();
        int daysTracked = cohortProfile.getDurationDays();
        double weeksTracked = daysTracked / 7.0;
        Goal goal = cohortProfile.getGoal();

        Range rateRange = safeWeeklyChangeRatePercent(goal);
        double minChangeKg = startWeightKg * (rateRange.min() / 100.0) * weeksTracked;
        double maxChangeKg = startWeightKg * (rateRange.max() / 100.0) * weeksTracked;
        Range idealEndWeight = goal == Goal.MUSCLE_GAIN
                ? new Range(startWeightKg + minChangeKg, startWeightKg + maxChangeKg)
                : new Range(startWeightKg - maxChangeKg, startWeightKg - minChangeKg); // FAT_LOSS

        double projectedEndWeightKg = (idealEndWeight.min() + idealEndWeight.max()) / 2.0;
        double avgWeightKg = (startWeightKg + projectedEndWeightKg) / 2.0;

        double avgBmr = calculateBmr(
                cohortProfile.getAge(),
                cohortProfile.getSex(),
                avgWeightKg,
                cohortProfile.getHeightCm()
        );

        Range stepsRange = idealDailySteps();
        double assumedSteps = (stepsRange.min() + stepsRange.max()) / 2.0;
        double avgNeat = caloriesPerStep(avgWeightKg) * assumedSteps;

        Range cardioMinRange = idealWeeklyCardioMin();
        double assumedWeeklyCardioMin = (cardioMinRange.min() + cardioMinRange.max()) / 2.0;
        double assumedDailyCardioMin = assumedWeeklyCardioMin / 7.0;
        int assumedZone = 2; // zone 2 = standard "general health" moderate-intensity default
        double avgEat = cardioCaloriesPerMin(avgWeightKg, assumedZone) * assumedDailyCardioMin;

        double avgMaintenanceCalories = (avgBmr + avgNeat + avgEat) * TEF;

        Range calorieOffset = calorieOffsetRange(avgWeightKg, goal);
        Range idealCalories = new Range(avgMaintenanceCalories + calorieOffset.min(),
                avgMaintenanceCalories + calorieOffset.max());

        Range idealProtein = proteinTargetGrams(avgWeightKg, goal);
        Range idealFat = fatTargetGrams(avgWeightKg, goal);
        Range idealCarb = carbTargetGrams(avgWeightKg, goal);

        Range sessionsPerMuscleGroup = idealSessionsPerMuscleGroupPerWeek();
        Range idealWorkoutsPerWeek = idealWorkoutsPerWeek(sessionsPerMuscleGroup);

        return WorkoutIdeal.builder()
                .daysTracked(daysTracked)
                .idealEndWeightKg(idealEndWeight)
                .idealCalories(idealCalories)
                .idealProteinG(idealProtein)
                .idealFatG(idealFat)
                .idealCarbG(idealCarb)
                .idealWeeklyCardioMin(cardioMinRange)
                .idealDailySteps(stepsRange)
                .idealUpperBodySessionsPerWeek(sessionsPerMuscleGroup)
                .idealLowerBodySessionsPerWeek(sessionsPerMuscleGroup)
                .idealWorkoutsPerWeek(idealWorkoutsPerWeek)
                .idealWorkoutRir(idealTrainingRir())
                .idealSleepH(idealSleepHours())
                .build();
    }
}