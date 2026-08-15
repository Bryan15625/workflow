package com.bryanhuang.workflow.entity;

import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "workout_user_aggregate")
@Getter
@NoArgsConstructor
@Builder
public class WorkoutUserAggregateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id")
    private WorkflowExecutionEntity workflowExecutionEntity;

    private String userId;

    private BigDecimal startWeightKg;
    private BigDecimal endWeightKg;
    private Integer daysTracked;

    private BigDecimal avgCalories;
    private BigDecimal avgProteinG;
    private BigDecimal avgCarbsG;
    private BigDecimal avgFatsG;

    private BigDecimal avgWeeklyCardioMin;

    @Enumerated(EnumType.STRING)
    private CardioZone avgCardioZone;

    private BigDecimal avgDailySteps;
    private BigDecimal avgWorkoutsPerWeek;
    private Integer upperBodySessionsPerWeek;
    private Integer lowerBodySessionsPerWeek;

    @Enumerated(EnumType.STRING)
    private WorkoutRir avgWorkoutRir;

    private BigDecimal avgSetsCompleted;
    private BigDecimal avgSleepH;

    public WorkoutUserAggregateEntity(
            UUID id,
            WorkflowExecutionEntity workflowExecutionEntity,
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
        this.id = id;
        this.workflowExecutionEntity = workflowExecutionEntity;
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
