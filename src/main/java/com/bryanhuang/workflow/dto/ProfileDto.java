package com.bryanhuang.workflow.dto;

import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.Sex;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
public class ProfileDto {
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

    public ProfileDto(
            Integer age,
            Integer weightKg,
            Integer heightCm,
            Sex sex,
            Goal goal
    ) {
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.sex = sex;
        this.goal = goal;
    }
}
