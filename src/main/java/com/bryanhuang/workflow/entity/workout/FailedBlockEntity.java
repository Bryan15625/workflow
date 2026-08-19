package com.bryanhuang.workflow.entity.workout;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "cohort_failed_block")
@Getter
@NoArgsConstructor
@Builder
public class FailedBlockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private long numberAboveIdeal;

    @Column(nullable = false)
    private long numberBelowIdeal;

    private Double averageDeviationAboveIdeal;

    private Double averageDeviationBelowIdeal;

    private String aboveDescription;

    private String belowDescription;

    public FailedBlockEntity(
            UUID id,
            long numberAboveIdeal,
            long numberBelowIdeal,
            Double averageDeviationAboveIdeal,
            Double averageDeviationBelowIdeal,
            String aboveDescription,
            String belowDescription
    ) {
        this.id = id;
        this.numberAboveIdeal = numberAboveIdeal;
        this.numberBelowIdeal = numberBelowIdeal;
        this.averageDeviationAboveIdeal = averageDeviationAboveIdeal;
        this.averageDeviationBelowIdeal = averageDeviationBelowIdeal;
        this.aboveDescription = aboveDescription;
        this.belowDescription = belowDescription;
    }
}