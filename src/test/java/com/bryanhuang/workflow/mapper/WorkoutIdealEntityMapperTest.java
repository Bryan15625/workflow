package com.bryanhuang.workflow.mapper;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.entity.workout.RangeEmbeddable;
import com.bryanhuang.workflow.entity.workout.WorkoutIdealEntity;
import com.bryanhuang.workflow.model.workout.Range;
import com.bryanhuang.workflow.model.workout.WorkoutIdeal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WorkoutIdealEntityMapperTest {

    private final WorkoutIdealEntityMapper mapper = new WorkoutIdealEntityMapper();

    @Nested
    @DisplayName("toWorkoutIdealEntity")
    class ToWorkoutIdealEntityTests {

        @Test
        @DisplayName("should map all fields")
        void shouldMapAllFields() {
            WorkflowExecutionEntity workflowExecutionEntity = mock(WorkflowExecutionEntity.class);

            WorkoutIdeal ideal = WorkoutIdeal.builder()
                    .daysTracked(28)
                    .idealEndWeightKg(new Range(70.35, 70.70))
                    .idealCalories(new Range(2471.4, 2568.0))
                    .idealProteinG(new Range(112.42, 154.5775))
                    .idealFatG(new Range(49.18375, 70.2625))
                    .idealCarbG(new Range(210.7875, 351.3125))
                    .idealWeeklyCardioMin(new Range(120, 180))
                    .idealDailySteps(new Range(8000, 10000))
                    .idealUpperBodySessionsPerWeek(new Range(2, 4))
                    .idealLowerBodySessionsPerWeek(new Range(2, 4))
                    .idealWorkoutsPerWeek(new Range(4, 6))
                    .idealWorkoutRir(new Range(1, 3))
                    .idealSleepH(new Range(7, 9))
                    .build();

            WorkoutIdealEntity result = mapper.toWorkoutIdealEntity(ideal, workflowExecutionEntity);

            assertThat(result.getWorkflowExecutionEntity()).isSameAs(workflowExecutionEntity);
            assertThat(result.getDaysTracked()).isEqualTo(ideal.getDaysTracked());

            assertRange(result.getIdealEndWeightKg(), 70.35, 70.70);
            assertRange(result.getIdealCalories(), 2471.4, 2568.0);
            assertRange(result.getIdealProteinG(), 112.42, 154.5775);
            assertRange(result.getIdealFatG(), 49.18375, 70.2625);
            assertRange(result.getIdealCarbG(), 210.7875, 351.3125);
            assertRange(result.getIdealWeeklyCardioMin(), 120, 180);
            assertRange(result.getIdealDailySteps(), 8000, 10000);
            assertRange(result.getIdealUpperBodySessionsPerWeek(), 2, 4);
            assertRange(result.getIdealLowerBodySessionsPerWeek(), 2, 4);
            assertRange(result.getIdealWorkoutsPerWeek(), 4, 6);
            assertRange(result.getIdealWorkoutRir(), 1, 3);
            assertRange(result.getIdealSleepH(), 7, 9);
        }

        @Test
        @DisplayName("should map null ranges to null")
        void shouldMapNullRangesToNull() {
            WorkoutIdeal ideal = WorkoutIdeal.builder()
                    .daysTracked(28)
                    .idealEndWeightKg(null)
                    .idealCalories(null)
                    .idealProteinG(null)
                    .idealFatG(null)
                    .idealCarbG(null)
                    .idealWeeklyCardioMin(null)
                    .idealDailySteps(null)
                    .idealUpperBodySessionsPerWeek(null)
                    .idealLowerBodySessionsPerWeek(null)
                    .idealWorkoutsPerWeek(null)
                    .idealWorkoutRir(null)
                    .idealSleepH(null)
                    .build();

            WorkoutIdealEntity result =
                    mapper.toWorkoutIdealEntity(ideal, null);

            assertThat(result.getDaysTracked())
                    .isEqualTo(ideal.getDaysTracked());

            assertThat(result.getIdealEndWeightKg()).isNull();
            assertThat(result.getIdealCalories()).isNull();
            assertThat(result.getIdealProteinG()).isNull();
            assertThat(result.getIdealFatG()).isNull();
            assertThat(result.getIdealCarbG()).isNull();
            assertThat(result.getIdealWeeklyCardioMin()).isNull();
            assertThat(result.getIdealDailySteps()).isNull();
            assertThat(result.getIdealUpperBodySessionsPerWeek()).isNull();
            assertThat(result.getIdealLowerBodySessionsPerWeek()).isNull();
            assertThat(result.getIdealWorkoutsPerWeek()).isNull();
            assertThat(result.getIdealWorkoutRir()).isNull();
            assertThat(result.getIdealSleepH()).isNull();
        }
    }

    @Nested
    @DisplayName("toWorkoutIdeal")
    class ToWorkoutIdealTests {

        @Test
        @DisplayName("should map all fields")
        void shouldMapAllFields() {
            WorkoutIdealEntity entity = WorkoutIdealEntity.builder()
                    .daysTracked(28)
                    .idealEndWeightKg(range(70.35, 70.70))
                    .idealCalories(range(2471.4, 2568.0))
                    .idealProteinG(range(112.42, 154.5775))
                    .idealFatG(range(49.18375, 70.2625))
                    .idealCarbG(range(210.7875, 351.3125))
                    .idealWeeklyCardioMin(range(120, 180))
                    .idealDailySteps(range(8000, 10000))
                    .idealUpperBodySessionsPerWeek(range(2, 4))
                    .idealLowerBodySessionsPerWeek(range(2, 4))
                    .idealWorkoutsPerWeek(range(4, 6))
                    .idealWorkoutRir(range(1, 3))
                    .idealSleepH(range(7, 9))
                    .build();

            WorkoutIdeal result = mapper.toWorkoutIdeal(entity);

            assertThat(result.getDaysTracked()).isEqualTo(entity.getDaysTracked());
            assertThat(result.getIdealEndWeightKg()).isEqualTo(new Range(70.35, 70.70));
            assertThat(result.getIdealCalories()).isEqualTo(new Range(2471.4, 2568.0));
            assertThat(result.getIdealProteinG()).isEqualTo(new Range(112.42, 154.5775));
            assertThat(result.getIdealFatG()).isEqualTo(new Range(49.18375, 70.2625));
            assertThat(result.getIdealCarbG()).isEqualTo(new Range(210.7875, 351.3125));
            assertThat(result.getIdealWeeklyCardioMin()).isEqualTo(new Range(120, 180));
            assertThat(result.getIdealDailySteps()).isEqualTo(new Range(8000, 10000));
            assertThat(result.getIdealUpperBodySessionsPerWeek()).isEqualTo(new Range(2, 4));
            assertThat(result.getIdealLowerBodySessionsPerWeek()).isEqualTo(new Range(2, 4));
            assertThat(result.getIdealWorkoutsPerWeek()).isEqualTo(new Range(4, 6));
            assertThat(result.getIdealWorkoutRir()).isEqualTo(new Range(1, 3));
            assertThat(result.getIdealSleepH()).isEqualTo(new Range(7, 9));
        }

        @Test
        @DisplayName("should map null ranges to null")
        void shouldMapNullRangesToNull() {
            WorkoutIdealEntity entity = WorkoutIdealEntity.builder()
                    .daysTracked(28)
                    .idealEndWeightKg(null)
                    .idealCalories(null)
                    .idealProteinG(null)
                    .idealFatG(null)
                    .idealCarbG(null)
                    .idealWeeklyCardioMin(null)
                    .idealDailySteps(null)
                    .idealUpperBodySessionsPerWeek(null)
                    .idealLowerBodySessionsPerWeek(null)
                    .idealWorkoutsPerWeek(null)
                    .idealWorkoutRir(null)
                    .idealSleepH(null)
                    .build();

            WorkoutIdeal result = mapper.toWorkoutIdeal(entity);

            assertThat(result.getDaysTracked()).isEqualTo(entity.getDaysTracked());

            assertThat(result.getIdealEndWeightKg()).isNull();
            assertThat(result.getIdealCalories()).isNull();
            assertThat(result.getIdealProteinG()).isNull();
            assertThat(result.getIdealFatG()).isNull();
            assertThat(result.getIdealCarbG()).isNull();
            assertThat(result.getIdealWeeklyCardioMin()).isNull();
            assertThat(result.getIdealDailySteps()).isNull();
            assertThat(result.getIdealUpperBodySessionsPerWeek()).isNull();
            assertThat(result.getIdealLowerBodySessionsPerWeek()).isNull();
            assertThat(result.getIdealWorkoutsPerWeek()).isNull();
            assertThat(result.getIdealWorkoutRir()).isNull();
            assertThat(result.getIdealSleepH()).isNull();
        }
    }

    private static RangeEmbeddable range(
            double min,
            double max
    ) {
        return com.bryanhuang.workflow.entity.workout.RangeEmbeddable.builder()
                .min(BigDecimal.valueOf(min))
                .max(BigDecimal.valueOf(max))
                .build();
    }

    private static void assertRange(
            RangeEmbeddable range,
            double expectedMin,
            double expectedMax
    ) {
        assertThat(range).isNotNull();
        assertThat(range.getMin()).isEqualByComparingTo(BigDecimal.valueOf(expectedMin));
        assertThat(range.getMax()).isEqualByComparingTo(BigDecimal.valueOf(expectedMax));
    }
}

