package com.bryanhuang.workflow.controller;

import com.bryanhuang.workflow.dto.response.CreateWorkflowResponse;
import com.bryanhuang.workflow.dto.response.WorkflowResponse;
import com.bryanhuang.workflow.exception.WorkflowNotFoundException;
import com.bryanhuang.workflow.service.WorkflowService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowController.class)
class WorkflowControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowService workflowService;

    @Nested
    @DisplayName("createWorkflow()")
    class CreateWorkflowTests {

        @Test
        @DisplayName("Succeeds with minimal case of all fields being set")
        void succeedsWhenAllFieldsSet() throws Exception {

            var response = new CreateWorkflowResponse(UUID.randomUUID());
            when(workflowService.createWorkflow(any())).thenReturn(response);

            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "workflowName": "any non empty string works",
                                      "profile": {
                                          "age": 23,
                                          "weightKg": 76,
                                          "heightCm": 174,
                                          "sex": "MALE",
                                          "goal": "FAT_LOSS"
                                      },
                                      "input": {
                                          "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                          "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                      },
                                      "steps": [
                                          {
                                          "stepId": 1,
                                          "stepName": "DOWNLOAD_FILE",
                                          "dependsOnStepIds": []
                                          }
                                      ]
                                  }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.workflowId").exists());
        }

        @Test
        @DisplayName("CreateWorkflowRequest: Fails when workflow name is not provided")
        void failsWhenWorkflowNameBlank() throws Exception {

            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "profile": {
                                          "age": 23,
                                          "weightKg": 76,
                                          "heightCm": 174,
                                          "sex": "MALE",
                                          "goal": "FAT_LOSS"
                                      },
                                      "input": {
                                          "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                          "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                      },
                                      "steps": [
                                          {
                                          "stepId": 1,
                                          "stepName": "DOWNLOAD_FILE",
                                          "dependsOnStepIds": []
                                          }
                                      ]
                                  }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.workflowName")
                            .value("Workflow name is required and cannot be blank"));
        }

        @Test
        @DisplayName("CreateWorkflowRequest: Fails when Profile is not provided")
        void failsWhenProfileBlank() throws Exception {

            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "workflowName": "any non empty string works",
                                      "input": {
                                          "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                          "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                      },
                                      "steps": [
                                          {
                                          "stepId": 1,
                                          "stepName": "DOWNLOAD_FILE",
                                          "dependsOnStepIds": []
                                          }
                                      ]
                                  }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.profile")
                            .value("Profile is required"));
        }

        @Test
        @DisplayName("CreateWorkflowRequest: Fails when Input is not provided")
        void failsWhenInputBlank() throws Exception {
            mockMvc.perform(post("/workflows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                                    {
                                      "workflowName": "any non empty string works",
                                      "profile": {
                                          "age": 23,
                                          "weightKg": 76,
                                          "heightCm": 174,
                                          "sex": "MALE",
                                          "goal": "FAT_LOSS"
                                      },
                                      "steps": [
                                          {
                                          "stepId": 1,
                                          "stepName": "DOWNLOAD_FILE",
                                          "dependsOnStepIds": []
                                          }
                                      ]
                                  }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.input")
                            .value("Input is required"));
        }

        @Test
        @DisplayName("CreateWorkflowRequest: Fails when steps is not provided")
        void failsWhenStepsBlank() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "workflowName": "any non empty string works",
                                      "profile": {
                                          "age": 23,
                                          "weightKg": 76,
                                          "heightCm": 174,
                                          "sex": "MALE",
                                          "goal": "FAT_LOSS"
                                      },
                                      "input": {
                                          "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                          "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                      }
                                  }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.steps")
                            .value("At least one step is required"));
        }

        @Test
        @DisplayName("ProfileDto: Fails when age is not provided")
        void failsWhenAgeMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "workflowName": "any non empty string works",
                              "profile": {
                                  "weightKg": 76,
                                  "heightCm": 174,
                                  "sex": "MALE",
                                  "goal": "FAT_LOSS"
                              },
                              "input": {
                                  "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                  "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                              },
                              "steps": [
                                  {
                                  "stepId": 1,
                                  "stepName": "DOWNLOAD_FILE",
                                  "dependsOnStepIds": []
                                  }
                              ]
                          }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['profile.age']")
                            .value("Age is required"));
        }

        @Test
        @DisplayName("ProfileDto: Fails when weightKg is not provided")
        void failsWhenWeightKgMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "workflowName": "any non empty string works",
                              "profile": {
                                  "age": 23,
                                  "heightCm": 174,
                                  "sex": "MALE",
                                  "goal": "FAT_LOSS"
                              },
                              "input": {
                                  "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                  "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                              },
                              "steps": [
                                  {
                                  "stepId": 1,
                                  "stepName": "DOWNLOAD_FILE",
                                  "dependsOnStepIds": []
                                  }
                              ]
                          }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['profile.weightKg']")
                            .value("Weight is required"));
        }

        @Test
        @DisplayName("ProfileDto: Fails when heightCm is not provided")
        void failsWhenHeightCmMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "workflowName": "any non empty string works",
                              "profile": {
                                  "age": 23,
                                  "weightKg": 76,
                                  "sex": "MALE",
                                  "goal": "FAT_LOSS"
                              },
                              "input": {
                                  "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                  "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                              },
                              "steps": [
                                  {
                                  "stepId": 1,
                                  "stepName": "DOWNLOAD_FILE",
                                  "dependsOnStepIds": []
                                  }
                              ]
                          }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['profile.heightCm']")
                            .value("Height is required"));
        }

        @Test
        @DisplayName("ProfileDto: Fails when Sex is not provided")
        void failsWhenSexMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "workflowName": "any non empty string works",
                              "profile": {
                                  "age": 23,
                                  "weightKg": 76,
                                  "heightCm": 174,
                                  "goal": "FAT_LOSS"
                              },
                              "input": {
                                  "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                  "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                              },
                              "steps": [
                                  {
                                  "stepId": 1,
                                  "stepName": "DOWNLOAD_FILE",
                                  "dependsOnStepIds": []
                                  }
                              ]
                          }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['profile.sex']")
                            .value("Sex is required"));
        }

        @Test
        @DisplayName("ProfileDto: Fails when Goal is not provided")
        void failsWhenGoalMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                              "workflowName": "any non empty string works",
                              "profile": {
                                  "age": 23,
                                  "weightKg": 76,
                                  "heightCm": 174,
                                  "sex": "MALE"
                              },
                              "input": {
                                  "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                  "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                              },
                              "steps": [
                                  {
                                  "stepId": 1,
                                  "stepName": "DOWNLOAD_FILE",
                                  "dependsOnStepIds": []
                                  }
                              ]
                          }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['profile.goal']")
                            .value("Goal is required"));
        }

        @Test
        @DisplayName("InputDto: Fails when sourceFilePath is not provided")
        void failsWhenSourceFilePathMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                                    {
                                      "workflowName": "any non empty string works",
                                      "profile": {
                                          "age": 23,
                                          "weightKg": 76,
                                          "heightCm": 174,
                                          "sex": "MALE",
                                          "goal": "FAT_LOSS"
                                      },
                                      "input": {
                                          "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                      },
                                      "steps": [
                                          {
                                          "stepId": 1,
                                          "stepName": "DOWNLOAD_FILE",
                                          "dependsOnStepIds": []
                                          }
                                      ]
                                  }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['input.sourceFilePath']")
                            .value("Source file path is required and cannot be blank"));
        }

        @Test
        @DisplayName("InputDto: Fails when resultFilePath is not provided")
        void failsWhenResultFilePathMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "workflowName": "any non empty string works",
                                      "profile": {
                                          "age": 23,
                                          "weightKg": 76,
                                          "heightCm": 174,
                                          "sex": "MALE",
                                          "goal": "FAT_LOSS"
                                      },
                                      "input": {
                                          "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv"
                                      },
                                      "steps": [
                                          {
                                          "stepId": 1,
                                          "stepName": "DOWNLOAD_FILE",
                                          "dependsOnStepIds": []
                                          }
                                      ]
                                  }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['input.resultFilePath']")
                            .value("Result file path is required and cannot be blank"));
        }

        @Test
        @DisplayName("StepDto: Fails when stepId is not provided")
        void failsWhenStepIdMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "workflowName": "any non empty string works",
                                  "profile": {
                                      "age": 23,
                                      "weightKg": 76,
                                      "heightCm": 174,
                                      "sex": "MALE",
                                      "goal": "FAT_LOSS"
                                  },
                                  "input": {
                                      "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                      "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                  },
                                  "steps": [
                                      {
                                      "stepName": "DOWNLOAD_FILE",
                                      "dependsOnStepIds": []
                                      }
                                  ]
                              }
                              """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['steps[0].stepId']")
                            .value(containsString("Step ID is required and must be an Integer")));
        }

        @Test
        @DisplayName("StepDto: Fails when stepName is not provided")
        void failsWhenStepNameMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                 "workflowName": " ",
                                 "profile": {
                                     "age": 23,
                                     "weightKg": 76,
                                     "heightCm": 174,
                                     "sex": "MALE",
                                     "goal": "FAT_LOSS"
                                 },
                                 "input": {
                                     "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                     "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                 },
                                 "steps": [
                                     {
                                     "stepId": 1,
                                     "dependsOnStepIds": []
                                     }
                                 ]
                             }
                             """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['steps[0].stepName']")
                            .value(containsString("Step name is required and cannot be blank")));
        }

        @Test
        @DisplayName("StepDto: Fails when dependsOnStepIds is missing")
        void failsWhenNextStepIdsMissing() throws Exception {
            mockMvc.perform(post("/workflows")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                 "workflowName": " ",
                                 "profile": {
                                     "age": 23,
                                     "weightKg": 76,
                                     "heightCm": 174,
                                     "sex": "MALE",
                                     "goal": "FAT_LOSS"
                                 },
                                 "input": {
                                     "sourceFilePath": "/Users/bryanhuang/Documents/workout_data.csv",
                                     "resultFilePath": "/Users/bryanhuang/Documents/summary.txt"
                                 },
                                 "steps": [
                                     {
                                     "stepId": 1,
                                     "stepName": "DOWNLOAD_FILE"
                                     }
                                 ]
                             }
                             """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors['steps[0].dependsOnStepIds']")
                            .value(containsString("Dependent IDs are required and cannot be blank")));
        }

        @Test
        @DisplayName("Fails when request body is malformed")
        void failsWhenBodyMalformed() throws Exception {
            mockMvc.perform(post("/workflows")
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
        @DisplayName("Fails when request body contains unknown fields")
        void createWorkflow_whenRequestBodyContainsUnknownFields_returnsInvalidRequestBodyResponse() throws Exception {
            mockMvc.perform(post("/workflows")
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

    @Nested
    @DisplayName("getWorkflow()")
    class GetWorkflow {

        @Test
        @DisplayName("Succeeds when workflowId is found")
        void succeedsWhenWorkflowIdSet() throws Exception {

            var response = new WorkflowResponse();
            when(workflowService.getWorkflow(any())).thenReturn(response);
            UUID workflowId = UUID.randomUUID();

            mockMvc.perform(get("/workflows/{workflowId}", workflowId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").exists())
                    .andExpect(jsonPath("$").isNotEmpty());
        }

        @Test
        @DisplayName("Fails when workflowId is not found")
        void failsWhenWorkflowIdNotFound() throws Exception {
            UUID workflowId = UUID.randomUUID();
            when(workflowService.getWorkflow(any()))
                    .thenThrow(new WorkflowNotFoundException("Workflow not found with id: {workflowId}"));


            mockMvc.perform(get("/workflows/{workflowId}", workflowId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Workflow not found"))
                    .andExpect(jsonPath("$.errors.workflow").isNotEmpty());
        }
    }


}