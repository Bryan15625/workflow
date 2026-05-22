package com.bryanhuang.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@RestController
public class WorkflowApplication {
	private static final Logger logger = LoggerFactory.getLogger(WorkflowApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(WorkflowApplication.class, args);
	}
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		logger.info("Hit hello endpoint");
		return String.format("Hello %s!", name);
	}

}
