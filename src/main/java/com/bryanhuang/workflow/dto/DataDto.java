package com.bryanhuang.workflow.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DataDto {
    @NotBlank(message = "Input is required and cannot be blank")
    private String input;

    @NotBlank(message = "Output is required and cannot be blank")
    private String output;

    public DataDto(
            String input,
            String output
    ) {
        this.input = input;
        this.output = output;
    }
}
