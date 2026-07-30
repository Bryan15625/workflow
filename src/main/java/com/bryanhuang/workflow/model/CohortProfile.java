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

    public CohortProfile(Integer age, Integer weightKg, Integer heightCm, Sex sex, Goal goal) {
        this.age = age;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.sex = sex;
        this.goal = goal;
    }
}