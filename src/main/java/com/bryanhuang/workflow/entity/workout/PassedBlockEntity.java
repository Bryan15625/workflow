package com.bryanhuang.workflow.entity.workout;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cohort_passed_block")
@Getter
@NoArgsConstructor
@Builder
public class PassedBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private long number;

    private Double averageValue;

    @Column(nullable = false)
    private String description;

    public PassedBlockEntity(
            UUID id,
            long number,
            Double averageValue,
            String description
    ) {
        this.id = id;
        this.number = number;
        this.averageValue = averageValue;
        this.description = description;
    }
}