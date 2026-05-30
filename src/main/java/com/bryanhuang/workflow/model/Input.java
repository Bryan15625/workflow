package com.bryanhuang.workflow.model;

import lombok.*;

@Getter
@Builder
public class Input {
    private String sourceFilePath;
    private String resultFilePath;

    public Input(String sourceFilePath, String resultFilePath) {
        this.sourceFilePath = sourceFilePath;
        this.resultFilePath = resultFilePath;
    }
}