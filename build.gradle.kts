plugins {
	java
	id("org.springframework.boot") version "3.5.16"
	id("io.spring.dependency-management") version "1.1.7"
	id("jacoco")
}

group = "com.bryanhuang"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.12"
}

val jacocoExcludes = listOf(
	"**/com/bryanhuang/workflow/WorkflowApplication.class",
	"**/com/bryanhuang/workflow/kafka/config/**",
	"**/com/bryanhuang/workflow/redis/config/**",
	"**/com/bryanhuang/workflow/exception/*Exception.class",
	"**/com/bryanhuang/workflow/model/**",
	"**/com/bryanhuang/workflow/config/**"
)


tasks.test {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)

	reports {
		html.required.set(true)
		xml.required.set(true)
		csv.required.set(false)
	}
	classDirectories.setFrom(
		files(classDirectories.files.map { file ->
			fileTree(file) {
				exclude(jacocoExcludes)
			}
		})
	)
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)

	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(jacocoExcludes)
			}
		})
	)

	violationRules {
		rule {
			limit {
				minimum = "1.00".toBigDecimal()
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}