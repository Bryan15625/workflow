package com.bryanhuang.workflow.dto;

import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.Sex;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CohortProfileDto {
    @NotNull(message = "Age is required")
    private Integer age;

    @NotNull(message = "Weight is required")
    private Integer weightKg;

    @NotNull(message = "Height is required")
    private Integer heightCm;

    @NotNull(message = "Sex is required")
    private Sex sex;

    @NotNull(message = "Goal is required")
    private Goal goal;

    @NotNull(message = "Duration is required")
    private Integer durationDays;

    public CohortProfileDto(
            Integer age,
            Integer weightKg,
            Integer heightCm,
            Sex sex,
            Goal goal,
            Integer durationDays
    ) {
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.sex = sex;
        this.goal = goal;
        this.durationDays = durationDays;
    }
}
