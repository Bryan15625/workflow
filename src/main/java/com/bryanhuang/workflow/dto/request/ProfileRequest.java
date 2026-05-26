package com.bryanhuang.workflow.dto.request;

import com.bryanhuang.workflow.model.Goal;
import com.bryanhuang.workflow.model.Sex;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileRequest {
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
}
