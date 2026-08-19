package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.workout.CohortAnalysisResult;
import com.bryanhuang.workflow.model.workout.Range;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import com.bryanhuang.workflow.model.workout.WorkoutUserAggregate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


class CohortMetricsAccumulator {

    @FunctionalInterface
    private interface DirectionalDescription {
        String describe(double avgValueInDirection, double startWeightKg, Goal goal);
    }

    private record MetricDefinition(
            String id,
            String title,
            String description,
            Function<WorkoutUserAggregate, Double> actualExtractor,
            Function<WorkoutIdeal, Range> idealExtractor,
            String passedDescription,
            DirectionalDescription aboveDescription,
            DirectionalDescription belowDescription
    ) {
        static DirectionalDescription constant(String text) {
            return (avg, start, goal) -> text;
        }
    }

    private static final List<MetricDefinition> METRIC_DEFINITIONS = List.of(

            new MetricDefinition(
                    "end_weight",
                    "Ending Weight",
                    "How each participant's ending body weight compared to the target range for their goal.",
                    a -> a.getEndWeightKg().doubleValue(),
                    WorkoutIdeal::getIdealEndWeightKg,
                    "Ending weight landed in the range consistent with a healthy, sustainable rate of change for the stated goal.",
                    (avg, start, goal) -> goal == Goal.MUSCLE_GAIN
                            ? "Participants gained weight too quickly, likely resulting in excess fat gain alongside muscle rather than lean hypertrophy."
                            : (avg > start
                               ? "Participants finished heavier than their starting weight, indicating they gained fat instead of losing it -- a caloric surplus contrary to the fat loss goal."
                               : "Participants lost weight too slowly, resulting in minimal or insignificant fat loss over the tracked period."),
                    (avg, start, goal) -> goal == Goal.MUSCLE_GAIN
                            ? (avg < start
                               ? "Participants finished lighter than their starting weight, indicating they lost weight instead of gaining it -- a caloric deficit contrary to the muscle gain goal, resulting in little to no muscular hypertrophy."
                               : "Participants gained weight too slowly, resulting in minimal muscular hypertrophy.")
                            : "Participants lost weight too quickly, increasing the risk of muscle loss, metabolic slowdown, hormonal disruption, and unsustainable crash-diet behavior."
            ),

            new MetricDefinition(
                    "calories",
                    "Daily Calorie Target",
                    "Average daily calorie intake compared to the target range for the stated goal.",
                    a -> a.getAvgCalories().doubleValue(),
                    WorkoutIdeal::getIdealCalories,
                    "Average intake stayed within the calorie range needed to support the stated goal.",
                    MetricDefinition.constant("Average intake exceeded the target range, undermining the intended deficit or overshooting the intended surplus."),
                    MetricDefinition.constant("Average intake fell short of the target range, which can blunt training performance and recovery.")
            ),

            new MetricDefinition(
                    "protein",
                    "Daily Protein Goal",
                    "Average daily protein intake compared to the recommended range.",
                    a -> a.getAvgProteinG().doubleValue(),
                    WorkoutIdeal::getIdealProteinG,
                    "Average protein intake supported muscle repair and retention.",
                    MetricDefinition.constant("Protein intake exceeded the recommended range, displacing other macronutrients without added benefit."),
                    MetricDefinition.constant("Protein intake fell short of the recommended range, which can impair muscle repair, growth, and recovery.")
            ),

            new MetricDefinition(
                    "fat",
                    "Daily Fat Intake",
                    "Average daily fat intake compared to the recommended range.",
                    a -> a.getAvgFatsG().doubleValue(),
                    WorkoutIdeal::getIdealFatG,
                    "Average fat intake stayed within a healthy range for hormone production and nutrient absorption.",
                    MetricDefinition.constant("Fat intake exceeded the recommended range, contributing excess calories beyond the target."),
                    MetricDefinition.constant("Fat intake fell short of the recommended range, which can impair hormone production and fat-soluble vitamin absorption.")
            ),

            new MetricDefinition(
                    "weekly_cardio_min",
                    "Weekly Cardio Minutes",
                    "Average weekly cardio minutes compared to the recommended range.",
                    a -> a.getAvgWeeklyCardioMin().doubleValue(),
                    WorkoutIdeal::getIdealWeeklyCardioMin,
                    "Weekly cardio volume met general cardiovascular health guidelines.",
                    MetricDefinition.constant("Cardio volume exceeded the recommended range, which may add unnecessary recovery demand and interfere with strength or muscle goals if not managed."),
                    MetricDefinition.constant("Cardio volume fell short of the recommended range for general cardiovascular health.")
            ),

            new MetricDefinition(
                    "daily_steps",
                    "Daily Steps",
                    "Average daily step count compared to the recommended range.",
                    a -> a.getAvgDailySteps().doubleValue(),
                    WorkoutIdeal::getIdealDailySteps,
                    "Daily step count supported general activity and calorie expenditure goals.",
                    MetricDefinition.constant("Daily step count exceeded the typical recommended range; not inherently harmful, but may increase overall fatigue if paired with high training volume."),
                    MetricDefinition.constant("Daily step count fell short of the recommended range, associated with poorer cardiovascular and metabolic health outcomes.")
            ),

            new MetricDefinition(
                    "upper_body_frequency",
                    "Upper Body Training Frequency",
                    "Upper body sessions per week compared to the recommended range.",
                    a -> a.getUpperBodySessionsPerWeek().doubleValue(),
                    WorkoutIdeal::getIdealUpperBodySessionsPerWeek,
                    "Upper body training frequency supported adequate muscle stimulus.",
                    MetricDefinition.constant("Upper body frequency exceeded the recommended range, raising the risk of inadequate recovery between sessions."),
                    MetricDefinition.constant("Upper body frequency fell short of the recommended range, limiting stimulus for growth and strength adaptation.")
            ),

            new MetricDefinition(
                    "lower_body_frequency",
                    "Lower Body Training Frequency",
                    "Lower body sessions per week compared to the recommended range.",
                    a -> a.getLowerBodySessionsPerWeek().doubleValue(),
                    WorkoutIdeal::getIdealLowerBodySessionsPerWeek,
                    "Lower body training frequency supported adequate muscle stimulus.",
                    MetricDefinition.constant("Lower body frequency exceeded the recommended range, raising the risk of inadequate recovery between sessions."),
                    MetricDefinition.constant("Lower body frequency fell short of the recommended range, limiting stimulus for growth and strength adaptation.")
            ),

            new MetricDefinition(
                    "workout_frequency",
                    "Total Training Frequency",
                    "Total training sessions per week compared to the recommended range.",
                    a -> a.getAvgWorkoutsPerWeek().doubleValue(),
                    WorkoutIdeal::getIdealWorkoutsPerWeek,
                    "Overall training frequency supported consistent progress toward the goal.",
                    MetricDefinition.constant("Total training frequency exceeded the recommended range, raising the risk of overtraining or inadequate recovery."),
                    MetricDefinition.constant("Total training frequency fell short of the recommended range, limiting overall training stimulus.")
            ),

            new MetricDefinition(
                    "training_rir",
                    "Training Intensity (RIR)",
                    "Average reps-in-reserve compared to the recommended range for effective training.",
                    a -> (double) (a.getAvgWorkoutRir().ordinal() - 1), // raw RIR value, see WorkoutRir's NONE-offset
                    WorkoutIdeal::getIdealWorkoutRir,
                    "Training intensity stayed close enough to failure to provide an effective muscle-building stimulus.",
                    MetricDefinition.constant("Training stayed too far from failure on average, providing insufficient stimulus for meaningful strength or hypertrophy gains."),
                    MetricDefinition.constant("Training pushed too close to or beyond failure on average, increasing injury risk and impairing recovery between sessions.")
            ),

            new MetricDefinition(
                    "sleep",
                    "Average Sleep",
                    "Average nightly sleep compared to the recommended range.",
                    a -> a.getAvgSleepH().doubleValue(),
                    WorkoutIdeal::getIdealSleepH,
                    "Average sleep duration supported recovery and hormone regulation.",
                    MetricDefinition.constant("Sleep duration exceeded the typical recommended range; not clearly harmful on its own, but worth noting alongside other recovery metrics."),
                    MetricDefinition.constant("Sleep duration fell short of the recommended range, which can impair recovery, hormone regulation, and training adaptation.")
            )
    );

    private final Map<String, MetricAccumulator> accumulatorsById = new LinkedHashMap<>();
    private final Goal goal;
    private final double startWeightKg;

    CohortMetricsAccumulator(Goal goal, double startWeightKg) {
        this.goal = goal;
        this.startWeightKg = startWeightKg;
        for (MetricDefinition def : METRIC_DEFINITIONS) {
            accumulatorsById.put(def.id(), new MetricAccumulator());
        }
    }

    void record(WorkoutUserAggregate actual, WorkoutIdeal ideal) {
        for (MetricDefinition def : METRIC_DEFINITIONS) {
            double actualValue = def.actualExtractor().apply(actual);
            Range idealRange = def.idealExtractor().apply(ideal);
            accumulatorsById.get(def.id()).record(actualValue, idealRange);
        }
    }

    CohortAnalysisResult.Report toReport(int durationDays) {
        MetricAccumulator endWeightAcc = accumulatorsById.get("end_weight");

        CohortAnalysisResult.OverallResult overallResult = CohortAnalysisResult.OverallResult.builder()
                .basis("end_weight")
                .passedCount((int) endWeightAcc.getPassedCount())
                .failedCount((int) endWeightAcc.getFailedCount())
                .build();

        List<CohortAnalysisResult.MetricResult> metricResults = METRIC_DEFINITIONS.stream()
                .map(def -> toMetricResult(def, accumulatorsById.get(def.id())))
                .toList();

        return CohortAnalysisResult.Report.builder()
                .overallResult(overallResult)
                .metrics(metricResults)
                .build();
    }

    private CohortAnalysisResult.MetricResult toMetricResult(MetricDefinition def, MetricAccumulator acc) {
        CohortAnalysisResult.PassedBlock passed = CohortAnalysisResult.PassedBlock.builder()
                .number(acc.getPassedCount())
                .averageValue(acc.getAvgPassedValue())
                .description(def.passedDescription())
                .build();

        Double avgAbove = acc.getAvgFailedAboveValue();
        Double avgBelow = acc.getAvgFailedBelowValue();

        CohortAnalysisResult.FailedBlock failed = CohortAnalysisResult.FailedBlock.builder()
                .numberAboveIdeal(acc.getFailedAboveCount())
                .numberBelowIdeal(acc.getFailedBelowCount())
                .averageDeviationAboveIdeal(acc.getAvgDeviationAboveIdeal())
                .averageDeviationBelowIdeal(acc.getAvgDeviationBelowIdeal())
                .aboveDescription(def.aboveDescription().describe(avgAbove, startWeightKg, goal))
                .belowDescription(def.belowDescription().describe(avgBelow, startWeightKg, goal))
                .build();

        return CohortAnalysisResult.MetricResult.builder()
                .id(def.id())
                .title(def.title())
                .description(def.description())
                .passed(passed)
                .failed(failed)
                .build();
    }
}