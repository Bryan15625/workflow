package com.bryanhuang.workflow.model;

import lombok.*;

@Getter
@Builder
public class Data {
    private String input;
    private String output;

    public Data(
            String input,
            String output
    ) {
        this.input = input;
        this.output = output;
    }
}