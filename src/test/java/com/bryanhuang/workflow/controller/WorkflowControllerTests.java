package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowService workflowService;

    @Test
    void createWorkflowDefinition_whenValidationFails_returnsValidationErrorResponse() throws Exception {
        mockMvc.perform(post("/definition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workflowDefinitionName": " ",
                                  "steps": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.workflowDefinitionName")
                        .value("Workflow definition name is required and cannot be blank"))
                .andExpect(jsonPath("$.errors.steps")
                        .value("At least one definition step is required"));
    }

    @Test
    void createWorkflowDefinition_whenRequestBodyHasInvalidType_returnsInvalidRequestBodyResponse() throws Exception {
        mockMvc.perform(post("/definition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workflowDefinitionName": "Test workflow",
                                  "steps": [
                                    {
                                      "stepId": 1,
                                      "stepName": "First step",
                                      "nextStepIds": ["a"]
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.errors['steps[0].nextStepIds[0]']")
                        .value(containsString("Invalid value. Expected Integer")));
    }

    @Test
    void createWorkflowDefinition_whenRequestBodyIsMalformed_returnsInvalidRequestBodyResponse() throws Exception {
        mockMvc.perform(post("/definition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workflowDefinitionName": "Test workflow",
                                  "steps": [
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.errors.requestBody")
                        .value("Malformed JSON or invalid request body"));
    }

    @Test
    void createWorkflowDefinition_whenRequestBodyContainsUnknownFields_returnsInvalidRequestBodyResponse() throws Exception {
        mockMvc.perform(post("/definition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workflowDefinitionName": "Test workflow",
                                  "steps": [
                                  {
                                      "stepId": 1,
                                      "stepName": "First step",
                                      "UnknownField": 37,
                                      "nextStepIds": []
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid request body"))
                .andExpect(jsonPath("$.errors.requestBody")
                        .value("Malformed JSON or invalid request body"));
    }
}