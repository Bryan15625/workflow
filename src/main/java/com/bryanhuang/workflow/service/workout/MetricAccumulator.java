package com.bryanhuang.workflow.service.workout;

import com.bryanhuang.workflow.model.workout.Range;
import lombok.Getter;


@Getter
class MetricAccumulator {

    private long passedCount = 0;
    private double sumPassedValue = 0;

    private long failedAboveCount = 0;
    private double sumFailedAboveValue = 0;
    private double sumFailedAboveDeviation = 0;

    private long failedBelowCount = 0;
    private double sumFailedBelowValue = 0;
    private double sumFailedBelowDeviation = 0;

    void record(double actualValue, Range ideal) {

        if (ideal.contains(actualValue)) {
            passedCount++;
            sumPassedValue += actualValue;
        } else if (actualValue > ideal.max()) {
            failedAboveCount++;
            sumFailedAboveValue += actualValue;
            sumFailedAboveDeviation += actualValue - ideal.max();
        } else {
            failedBelowCount++;
            sumFailedBelowValue += actualValue;
            sumFailedBelowDeviation += ideal.min() - actualValue;
        }
    }

    long getFailedCount() {
        return failedAboveCount + failedBelowCount;
    }

    long getFailedAboveCount() {
        return failedAboveCount;
    }

    long getFailedBelowCount() {
        return failedBelowCount;
    }

    Double getAvgPassedValue() {
        return passedCount == 0 ? 0 : sumPassedValue / passedCount;
    }

    Double getAvgFailedAboveValue() {
        return failedAboveCount == 0 ? 0 : sumFailedAboveValue / failedAboveCount;
    }

    Double getAvgFailedBelowValue() {
        return failedBelowCount == 0 ? 0 : sumFailedBelowValue / failedBelowCount;
    }

    Double getAvgDeviationAboveIdeal() {
        return failedAboveCount == 0 ? 0 : sumFailedAboveDeviation / failedAboveCount;
    }

    Double getAvgDeviationBelowIdeal() {
        return failedBelowCount == 0 ? 0 : sumFailedBelowDeviation / failedBelowCount;
    }
}