package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.workout.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CohortMetricsAccumulatorTest {

    private static final double START_WEIGHT = 70.0;

    private CohortMetricsAccumulator createAccumulator() {
        return new CohortMetricsAccumulator(Goal.MUSCLE_GAIN, START_WEIGHT);
    }

    private CohortAnalysisResult.MetricResult recordEndWeight(
            Goal goal,
            double startWeight,
            double endWeight
    ) {
        CohortMetricsAccumulator accumulator =
                new CohortMetricsAccumulator(goal, startWeight);

        WorkoutIdeal ideal = WorkoutIdeal.builder()
                .idealEndWeightKg(new Range(70, 72))
                .idealCalories(new Range(2000, 2500))
                .idealProteinG(new Range(120, 160))
                .idealFatG(new Range(50, 80))
                .idealCarbG(new Range(200, 300))
                .idealWeeklyCardioMin(new Range(150, 300))
                .idealDailySteps(new Range(7000, 15000))
                .idealUpperBodySessionsPerWeek(new Range(2, 3))
                .idealLowerBodySessionsPerWeek(new Range(2, 3))
                .idealWorkoutsPerWeek(new Range(4, 6))
                .idealWorkoutRir(new Range(0, 3))
                .idealSleepH(new Range(7, 9))
                .build();

        WorkoutUserAggregate actual = WorkoutUserAggregate.builder()
                .endWeightKg(BigDecimal.valueOf(endWeight))

                // Values for the other metrics are inside their ranges.
                .avgCalories(BigDecimal.valueOf(2200))
                .avgProteinG(BigDecimal.valueOf(140))
                .avgFatsG(BigDecimal.valueOf(65))
                .avgWeeklyCardioMin(BigDecimal.valueOf(200))
                .avgDailySteps(BigDecimal.valueOf(10000))
                .upperBodySessionsPerWeek(2)
                .lowerBodySessionsPerWeek(2)
                .avgWorkoutsPerWeek(BigDecimal.valueOf(4))
                .avgWorkoutRir(WorkoutRir.TWO)
                .avgSleepH(BigDecimal.valueOf(8))
                .build();

        accumulator.record(actual, ideal);

        return accumulator.toReport(28)
                .getMetrics()
                .stream()
                .filter(metric -> metric.getId().equals("end_weight"))
                .findFirst()
                .orElseThrow();
    }

    private WorkoutIdeal buildIdeal() {
        return WorkoutIdeal.builder()
                .idealEndWeightKg(new Range(71.0, 72.0))
                .idealCalories(new Range(2500, 2800))
                .idealProteinG(new Range(120, 160))
                .idealFatG(new Range(50, 80))
                .idealCarbG(new Range(250, 350))
                .idealWeeklyCardioMin(new Range(150, 300))
                .idealDailySteps(new Range(7000, 15000))
                .idealUpperBodySessionsPerWeek(new Range(2, 3))
                .idealLowerBodySessionsPerWeek(new Range(2, 3))
                .idealWorkoutsPerWeek(new Range(4, 6))
                .idealWorkoutRir(new Range(0, 3))
                .idealSleepH(new Range(7, 9))
                .build();
    }

    private WorkoutUserAggregate buildActual() {
        return WorkoutUserAggregate.builder()
                .endWeightKg(BigDecimal.valueOf(71.5))
                .avgCalories(BigDecimal.valueOf(2650))
                .avgProteinG(BigDecimal.valueOf(140))
                .avgFatsG(BigDecimal.valueOf(65))
                .avgWeeklyCardioMin(BigDecimal.valueOf(200))
                .avgDailySteps(BigDecimal.valueOf(10000))
                .upperBodySessionsPerWeek(2)
                .lowerBodySessionsPerWeek(2)
                .avgWorkoutsPerWeek(BigDecimal.valueOf(5))
                .avgWorkoutRir(WorkoutRir.fromInt(2))
                .avgSleepH(BigDecimal.valueOf(8))
                .build();
    }

    @Nested
    @DisplayName("CohortMetricsAccumulator()")
    class ConstructorTests {

        @Test
        @DisplayName("initializes accumulator for all metrics")
        void initializesAllMetricAccumulators() {
            CohortMetricsAccumulator accumulator = createAccumulator();

            CohortAnalysisResult.Report report = accumulator.toReport(28);
            assertNotNull(report);
            assertEquals(11, report.getMetrics().size());
        }
    }

    @Nested
    @DisplayName("record()")
    class RecordTests {

        @Test
        @DisplayName("records values that fall within their ideal ranges")
        void recordsPassingMetrics() {
            CohortMetricsAccumulator accumulator = createAccumulator();

            WorkoutIdeal ideal = buildIdeal();
            WorkoutUserAggregate actual = buildActual();

            accumulator.record(actual, ideal);

            CohortAnalysisResult.Report report =
                    accumulator.toReport(28);

            assertEquals(1, report.getOverallResult().getPassedCount());
            assertEquals(0, report.getOverallResult().getFailedCount());

            for (CohortAnalysisResult.MetricResult metric : report.getMetrics()) {
                assertEquals(1, metric.getPassed().getNumber());
                assertEquals(0, metric.getFailed().getNumberAboveIdeal());
                assertEquals(0, metric.getFailed().getNumberBelowIdeal());
            }
        }

        @Test
        @DisplayName("records values above and below ideal ranges")
        void recordsFailedMetrics() {
            CohortMetricsAccumulator accumulator = createAccumulator();

            WorkoutIdeal ideal = buildIdeal();

            WorkoutUserAggregate actual = WorkoutUserAggregate.builder()
                    .endWeightKg(BigDecimal.valueOf(75))       // above
                    .avgCalories(BigDecimal.valueOf(3000))     // above
                    .avgProteinG(BigDecimal.valueOf(100))      // below
                    .avgFatsG(BigDecimal.valueOf(90))          // above
                    .avgWeeklyCardioMin(BigDecimal.valueOf(100)) // below
                    .avgDailySteps(BigDecimal.valueOf(16000))  // above
                    .upperBodySessionsPerWeek(1) // below
                    .lowerBodySessionsPerWeek(4) // above
                    .avgWorkoutsPerWeek(BigDecimal.valueOf(3)) // below
                    .avgWorkoutRir(WorkoutRir.fromInt(4)) // above
                    .avgSleepH(BigDecimal.valueOf(6))          // below
                    .build();

            accumulator.record(actual, ideal);

            CohortAnalysisResult.Report report = accumulator.toReport(28);

            assertEquals(0, report.getOverallResult().getPassedCount());
            assertEquals(1, report.getOverallResult().getFailedCount());

            int passedCount = 0;
            int failedCountAbove = 0;
            int failedCountBelow = 0;
            for (CohortAnalysisResult.MetricResult metric : report.getMetrics()) {
                if (metric.getPassed().getNumber() > 0) {
                    passedCount++;
                } else if (metric.getFailed().getNumberAboveIdeal() > 0) {
                    failedCountAbove++;
                } else if (metric.getFailed().getNumberBelowIdeal() > 0) {
                    failedCountBelow++;
                }
            }
            assertEquals(0, passedCount);
            assertEquals(6, failedCountAbove);
            assertEquals(5, failedCountBelow);

        }
    }

    @Nested
    @DisplayName("toReport()")
    class ToReportTests {

        @Test
        @DisplayName("builds report with all metric definitions")
        void buildsAllMetricResults() {
            CohortMetricsAccumulator accumulator = createAccumulator();

            CohortAnalysisResult.Report report = accumulator.toReport(28);

            assertNotNull(report);
            assertNotNull(report.getOverallResult());
            assertNotNull(report.getMetrics());

            assertEquals(11, report.getMetrics().size());

            assertEquals("end_weight", report.getOverallResult().getBasis());
        }

        @Test
        @DisplayName("uses end weight as the overall result")
        void usesEndWeightAsOverallResult() {
            CohortMetricsAccumulator accumulator = createAccumulator();

            WorkoutIdeal ideal = buildIdeal();
            WorkoutUserAggregate actual = buildActual();

            accumulator.record(actual, ideal);

            CohortAnalysisResult.Report report = accumulator.toReport(28);

            assertEquals("end_weight", report.getOverallResult().getBasis());
            assertEquals(1, report.getOverallResult().getPassedCount());
            assertEquals(0, report.getOverallResult().getFailedCount());
        }

        @Test
        @DisplayName("contains expected metric IDs")
        void containsExpectedMetricIds() {
            CohortMetricsAccumulator accumulator = createAccumulator();
            CohortAnalysisResult.Report report = accumulator.toReport(28);

            assertEquals("end_weight", report.getMetrics().get(0).getId());
            assertEquals("calories", report.getMetrics().get(1).getId());
            assertEquals("protein", report.getMetrics().get(2).getId());
            assertEquals("fat", report.getMetrics().get(3).getId());
            assertEquals("weekly_cardio_min", report.getMetrics().get(4).getId());
            assertEquals("daily_steps", report.getMetrics().get(5).getId());
            assertEquals("upper_body_frequency", report.getMetrics().get(6).getId());
            assertEquals("lower_body_frequency", report.getMetrics().get(7).getId());
            assertEquals("workout_frequency", report.getMetrics().get(8).getId());
            assertEquals("training_rir", report.getMetrics().get(9).getId());
            assertEquals("sleep", report.getMetrics().get(10).getId());
        }

        @Test
        void endWeightAboveIdeal_muscleGain() {
            var metric = recordEndWeight(Goal.MUSCLE_GAIN, 70, 75);

            assertEquals(
                    "Participants gained weight too quickly, likely resulting in excess fat gain " +
                            "alongside muscle rather than lean hypertrophy.",
                    metric.getFailed().getAboveDescription()
            );
        }

        @Test
        void endWeightAboveIdeal_fatLossAndWeightIncreased() {
            var metric = recordEndWeight(Goal.FAT_LOSS, 70, 75);

            assertEquals(
                    "Participants finished heavier than their starting weight, indicating they gained " +
                            "fat instead of losing it -- a caloric surplus contrary to the fat loss goal.",
                    metric.getFailed().getAboveDescription()
            );
        }

        @Test
        void endWeightAboveIdeal_fatLossAndWeightDidNotIncrease() {
            var metric = recordEndWeight(Goal.FAT_LOSS, 70, 70);

            assertEquals(
                    "Participants lost weight too slowly, resulting in minimal or insignificant fat loss " +
                            "over the tracked period.",
                    metric.getFailed().getAboveDescription()
            );
        }

        @Test
        void endWeightBelowIdeal_muscleGainAndWeightDecreased() {
            var metric = recordEndWeight(Goal.MUSCLE_GAIN, 70, 65);

            assertEquals(
                    "Participants finished lighter than their starting weight, indicating they lost weight " +
                            "instead of gaining it -- a caloric deficit contrary to the muscle gain goal, resulting " +
                            "in little to no muscular hypertrophy.",
                    metric.getFailed().getBelowDescription()
            );
        }

        @Test
        void endWeightBelowIdeal_muscleGainAndWeightIncreasedTooSlowly() {
            var metric = recordEndWeight(Goal.MUSCLE_GAIN, 69, 69.2);

            assertEquals(
                    "Participants gained weight too slowly, resulting in minimal muscular hypertrophy.",
                    metric.getFailed().getBelowDescription()
            );
        }
    }
}