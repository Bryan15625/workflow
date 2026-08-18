package com.bryanhuang.workflow.entity.workout;

import com.bryanhuang.workflow.entity.workflow.WorkflowExecutionEntity;
import com.bryanhuang.workflow.model.workout.CardioZone;
import com.bryanhuang.workflow.model.workout.WorkoutRir;
import com.bryanhuang.workflow.model.workout.WorkoutType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "workout_record")
@Getter
@NoArgsConstructor
@Builder
public class WorkoutRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_execution_id")
    private WorkflowExecutionEntity workflowExecutionEntity;

    private String userId;
    private LocalDate date;
    private BigDecimal weightKg;
    private Integer calories;
    private Integer proteinG;
    private Integer carbsG;
    private Integer fatsG;
    private Integer cardioMin;

    @Enumerated(EnumType.STRING)
    private CardioZone cardioZone;
    private Integer steps;

    @Enumerated(EnumType.STRING)
    private WorkoutType workoutType;

    @Enumerated(EnumType.STRING)
    private WorkoutRir workoutRir;
    private Integer totalSets;
    private BigDecimal sleepH;

    public WorkoutRecordEntity(
            UUID id,
            WorkflowExecutionEntity workflowExecutionEntity,
            String userId,
            LocalDate date,
            BigDecimal weightKg,
            Integer calories,
            Integer proteinG,
            Integer carbsG,
            Integer fatsG,
            Integer cardioMin,
            CardioZone cardioZone,
            Integer steps,
            WorkoutType workoutType,
            WorkoutRir workoutRir,
            Integer totalSets,
            BigDecimal sleepH
    ) {
        this.id = id;
        this.workflowExecutionEntity = workflowExecutionEntity;
        this.userId = userId;
        this.date = date;
        this.weightKg = weightKg;
        this.calories = calories;
        this.proteinG = proteinG;
        this.carbsG = carbsG;
        this.fatsG = fatsG;
        this.cardioMin = cardioMin;
        this.cardioZone = cardioZone;
        this.steps = steps;
        this.workoutType = workoutType;
        this.workoutRir = workoutRir;
        this.totalSets = totalSets;
        this.sleepH = sleepH;
    }
}
