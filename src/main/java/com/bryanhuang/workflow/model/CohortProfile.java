package com.bryanhuang.workflow.model;

import lombok.*;

@Getter
@Builder
public class CohortProfile {
    private Integer age;
    private Integer weightKg;
    private Integer heightCm;
    private Sex sex;
    private Goal goal;
    private Integer durationDays;

    public CohortProfile(
            Integer age,
            Integer weightKg,
            Integer heightCm,
            Sex sex,
            Goal goal,
            Integer durationDays
    ) {
        this.durationDays = durationDays;
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.sex = sex;
        this.goal = goal;
    }
}