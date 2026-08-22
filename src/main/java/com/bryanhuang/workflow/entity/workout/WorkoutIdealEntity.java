package com.bryanhuang.workflow.entity.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "workout_ideal_entity")
@Getter
@NoArgsConstructor
@Builder
public class WorkoutIdealEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workflow_execution_id", nullable = false, unique = true)
    private WorkflowExecutionEntity workflowExecutionEntity;

    private Integer daysTracked;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_end_weight_kg_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_end_weight_kg_max"))
    })
    private RangeEmbeddable idealEndWeightKg;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_calories_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_calories_max"))
    })
    private RangeEmbeddable idealCalories;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_protein_g_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_protein_g_max"))
    })
    private RangeEmbeddable idealProteinG;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_fat_g_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_fat_g_max"))
    })
    private RangeEmbeddable idealFatG;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_carb_g_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_carb_g_max"))
    })
    private RangeEmbeddable idealCarbG;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_weekly_cardio_min_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_weekly_cardio_min_max"))
    })
    private RangeEmbeddable idealWeeklyCardioMin;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_daily_steps_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_daily_steps_max"))
    })
    private RangeEmbeddable idealDailySteps;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_upper_body_sessions_per_week_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_upper_body_sessions_per_week_max"))
    })
    private RangeEmbeddable idealUpperBodySessionsPerWeek;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_lower_body_sessions_per_week_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_lower_body_sessions_per_week_max"))
    })
    private RangeEmbeddable idealLowerBodySessionsPerWeek;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_workouts_per_week_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_workouts_per_week_max"))
    })
    private RangeEmbeddable idealWorkoutsPerWeek;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_workout_rir_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_workout_rir_max"))
    })
    private RangeEmbeddable idealWorkoutRir;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "min", column = @Column(name = "ideal_sleep_h_min")),
            @AttributeOverride(name = "max", column = @Column(name = "ideal_sleep_h_max"))
    })
    private RangeEmbeddable idealSleepH;

    public WorkoutIdealEntity(
            UUID id,
            WorkflowExecutionEntity workflowExecutionEntity,
            Integer daysTracked,
            RangeEmbeddable idealEndWeightKg,
            RangeEmbeddable idealCalories,
            RangeEmbeddable idealProteinG,
            RangeEmbeddable idealFatG,
            RangeEmbeddable idealCarbG,
            RangeEmbeddable idealWeeklyCardioMin,
            RangeEmbeddable idealDailySteps,
            RangeEmbeddable idealUpperBodySessionsPerWeek,
            RangeEmbeddable idealLowerBodySessionsPerWeek,
            RangeEmbeddable idealWorkoutsPerWeek,
            RangeEmbeddable idealWorkoutRir,
            RangeEmbeddable idealSleepH
    ) {
        this.id = id;
        this.workflowExecutionEntity = workflowExecutionEntity;
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