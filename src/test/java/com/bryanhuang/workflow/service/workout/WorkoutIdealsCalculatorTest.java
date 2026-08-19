package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.model.CohortProfile;
import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.Sex;
import com.bryanhuang.workflow.model.workout.Range;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkoutIdealsCalculatorTest {

    private final WorkoutIdealsCalculator calculator = new WorkoutIdealsCalculator();

    @Nested
    @DisplayName("calculateIdeal()")
    class CalculateBmrTests {

        @Test
        void calculateIdeal_returnsExpectedValuesForMuscleGainMan() {
            CohortProfile cohortProfile = CohortProfile.builder()
                    .weightKg(70)
                    .age(25)
                    .heightCm(175)
                    .sex(Sex.MALE)
                    .goal(Goal.MUSCLE_GAIN)
                    .durationDays(28)
                    .build();

            WorkoutIdeal result = calculator.calculateIdeal(cohortProfile);

            assertEquals(28, result.getDaysTracked());

            // 70*(1.00125)^4 to 70*(1.0025)^4 --> 70.35 to 70.7kg
            assertRangeEquals(new Range(70.35, 70.7), result.getIdealEndWeightKg());

            // avgWeight is ( 70 + (70.35+70.7)/2 ) / 2 = 70.2625
            assertRangeEquals(new Range(1.6 * 70.2625, 2.2 * 70.2625), result.getIdealProteinG());

            assertRangeEquals(new Range(0.7 * 70.2625, 1.0 * 70.2625), result.getIdealFatG());
            assertRangeEquals(new Range(3.0 * 70.2625, 5.0 * 70.2625), result.getIdealCarbG());
            assertRangeEquals(new Range(150, 300), result.getIdealWeeklyCardioMin());
            assertRangeEquals(new Range(7000, 15000), result.getIdealDailySteps());
            assertRangeEquals(new Range(2, 3), result.getIdealUpperBodySessionsPerWeek());
            assertRangeEquals(new Range(2, 3), result.getIdealLowerBodySessionsPerWeek());
            assertRangeEquals(new Range(4, 6), result.getIdealWorkoutsPerWeek());
            assertRangeEquals(new Range(0, 3), result.getIdealWorkoutRir());
            assertRangeEquals(new Range(7, 9), result.getIdealSleepH());
        }

        @Test
        void calculateIdeal_returnsExpectedValuesForFatLossWoman() {
            CohortProfile cohortProfile = CohortProfile.builder()
                    .weightKg(50)
                    .age(25)
                    .heightCm(155)
                    .sex(Sex.FEMALE)
                    .goal(Goal.FAT_LOSS)
                    .durationDays(28)
                    .build();

            WorkoutIdeal result = calculator.calculateIdeal(cohortProfile);

            assertEquals(28, result.getDaysTracked());

            // 50*(0.99)^4 to 50*(0.995)^4 --> 48.0 to 49kg
            assertRangeEquals(new Range(48.0, 49.0), result.getIdealEndWeightKg());

            // avgWeight is ( 50 + (48.0+49)/2 ) / 2 = 49.25
            assertRangeEquals(new Range(1.8 * 49.25, 2.2 * 49.25), result.getIdealProteinG());

            assertRangeEquals(new Range(0.6 * 49.25, 0.8 * 49.25), result.getIdealFatG());
            assertRangeEquals(new Range(2.0 * 49.25, 4.0 * 49.25), result.getIdealCarbG());
            assertRangeEquals(new Range(150, 300), result.getIdealWeeklyCardioMin());
            assertRangeEquals(new Range(7000, 15000), result.getIdealDailySteps());
            assertRangeEquals(new Range(2, 3), result.getIdealUpperBodySessionsPerWeek());
            assertRangeEquals(new Range(2, 3), result.getIdealLowerBodySessionsPerWeek());
            assertRangeEquals(new Range(4, 6), result.getIdealWorkoutsPerWeek());
            assertRangeEquals(new Range(0, 3), result.getIdealWorkoutRir());
            assertRangeEquals(new Range(7, 9), result.getIdealSleepH());
        }

        @Test
        void calculateIdeal_interpolateAbove109kg() {
            CohortProfile cohortProfile = CohortProfile.builder()
                    .weightKg(110)
                    .age(25)
                    .heightCm(200)
                    .sex(Sex.MALE)
                    .goal(Goal.MUSCLE_GAIN)
                    .durationDays(28)
                    .build();

            WorkoutIdeal result = calculator.calculateIdeal(cohortProfile);

            assertEquals(28, result.getDaysTracked());
        }

        @Test
        void calculateIdeal_exactWeightKgInLookupTable() {
            CohortProfile cohortProfile = CohortProfile.builder()
                    .weightKg(54)
                    .age(25)
                    .heightCm(165)
                    .sex(Sex.FEMALE)
                    .goal(Goal.FAT_LOSS)
                    .durationDays(0)
                    .build();

            WorkoutIdeal result = calculator.calculateIdeal(cohortProfile);

            assertEquals(0, result.getDaysTracked());
        }

    }

    private void assertRangeEquals(Range expected, Range actual) {
        assertEquals(expected.min(), actual.min(), 0.000001);
        assertEquals(expected.max(), actual.max(), 0.000001);
    }
}