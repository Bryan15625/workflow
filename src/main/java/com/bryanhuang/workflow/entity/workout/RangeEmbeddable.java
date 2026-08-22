package com.bryanhuang.workflow.entity.workout;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
public class RangeEmbeddable {
    private BigDecimal min;
    private BigDecimal max;

    public RangeEmbeddable(BigDecimal min, BigDecimal max) {
        this.min = min;
        this.max = max;
    }
}