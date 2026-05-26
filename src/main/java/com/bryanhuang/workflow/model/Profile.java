package com.bryanhuang.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    private Integer age;
    private Integer weightKg;
    private Integer heightCm;
    private Sex sex;
    private Goal goal;
}