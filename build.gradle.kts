plugins {
	java
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.ddbs"
version = "0.0.1-SNAPSHOT"
description = "User Service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(24)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
	implementation("org.springframework:spring-jdbc:6.2.10")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	compileOnly("org.projectlombok:lombok:1.18.38")
//	implementation("org.projectlombok:lombok:1.18.38")
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("bootJar") {
	enabled = true
}

tasks.named("jar") {
	enabled = false
}
