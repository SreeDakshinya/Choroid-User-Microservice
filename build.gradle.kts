plugins {
	java
	id("org.springframework.boot") version "2.7.18"
	id("io.spring.dependency-management") version "1.1.5"
}

group = "com.ddbs"
version = "0.0.1-SNAPSHOT"
description = "User Service"
java {
	toolchain { languageVersion = JavaLanguageVersion.of(11) } }

configurations {
//	val localRunClasspath by creating {
//		extendsFrom(configurations.runtimeClasspath.get(), configurations.compileOnly.get())
//	}
	compileOnly { extendsFrom(configurations.annotationProcessor.get()) }
	all {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
		exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
		exclude(group = "org.slf4j", module = "slf4j-reload4j")
		exclude(group = "com.sun.jersey")
		exclude(group = "com.sun.jersey.contribs")
		exclude(group = "org.glassfish.hk2.external")

//		exclude(group = "com.fasterxml.jackson.core") // Target Jackson serialization
//		exclude(group = "com.fasterxml.jackson.datatype")
//		exclude(group = "com.fasterxml.jackson.module")
//		exclude(group = "org.codehaus.jackson")
//		exclude(group = "io.netty") // Leave this in the ALL block for aggressive cleanup

		resolutionStrategy {
			force(
				"io.netty:netty-all:4.1.96.Final",
				"io.netty:netty-transport-native-epoll:4.1.96.Final",
				"io.netty:netty-transport-native-kqueue:4.1.96.Final",
				"io.netty:netty-handler:4.1.96.Final",
				"io.netty:netty-transport:4.1.96.Final",
				"io.netty:netty-common:4.1.96.Final",
				"io.netty:netty-buffer:4.1.96.Final",
				"io.netty:netty-codec:4.1.96.Final",
				"io.netty:netty-resolver:4.1.96.Final",
				"io.netty:netty-transport-native-unix-common:4.1.96.Final",
				"io.netty:netty-codec-http:4.1.96.Final",
				"io.netty:netty-codec-http2:4.1.96.Final",
				"io.netty:netty-handler-proxy:4.1.96.Final",
				"io.netty:netty-transport-classes-epoll:4.1.96.Final",
				"io.netty:netty-transport-classes-kqueue:4.1.96.Final",
				"io.netty:netty-codec-socks:4.1.96.Final"
			)
			eachDependency {
				if (requested.group == "io.netty") {
					useVersion("4.1.96.Final")
					because("Force Netty 4.1.96.Final to match Spark Docker cluster")
				}
		}
		}
	}
}

ext["netty.version"] = "4.1.96.Final"

repositories { mavenCentral() }

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-log4j2")
	implementation("javax.persistence:javax.persistence-api")
	implementation("org.springframework:spring-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	compileOnly("org.projectlombok:lombok:1.18.38")

//	implementation("org.apache.spark:spark-connect-client-jvm_2.13:3.5.3")

	implementation("org.apache.spark:spark-core_2.12:3.5.3") {
//		exclude(group = "io.netty")
		exclude(group = "org.eclipse.jetty")
		exclude(group = "javax.ws.rs")
		exclude(group = "com.sun.jersey")
		exclude(group = "org.glassfish.jersey.core")
		exclude(group = "org.glassfish.jersey.containers")
		exclude(group = "org.glassfish.jersey.inject")
		exclude(group = "com.fasterxml.jackson.core")
//		exclude(group = "org.slf4j")
//		exclude(group = "org.apache.logging.log4j")
	}
	implementation("org.apache.spark:spark-sql_2.12:3.5.3") {
//		exclude(group = "io.netty")
		exclude(group = "org.eclipse.jetty")
		exclude(group = "javax.ws.rs")
		exclude(group = "com.sun.jersey")
		exclude(group = "org.glassfish.jersey.core")
		exclude(group = "org.glassfish.jersey.containers")
		exclude(group = "org.glassfish.jersey.inject")
		exclude(group = "com.fasterxml.jackson.core")
//		exclude(group = "org.slf4j")
//		exclude(group = "org.apache.logging.log4j")
	}
//	implementation("io.netty:netty-all:4.1.124.Final")
//	val nettyVersion = "4.1.124.Final" // Use the version from your log or 4.1.109.Final (Spark 3.5's typical dependency)
//
//	implementation("io.netty:netty-buffer:$nettyVersion")
//	implementation("io.netty:netty-codec:$nettyVersion")
//	implementation("io.netty:netty-common:$nettyVersion")
//	implementation("io.netty:netty-handler:$nettyVersion")
//	implementation("io.netty:netty-transport:$nettyVersion")
//	implementation("org.apache.spark:spark-network-common_2.13:3.5.3")
	implementation("org.apache.spark:spark-common-utils_2.12:3.5.3")
	// Remove incompatible log4j-slf4j2-impl - Spring Boot 2.7.18 uses log4j-slf4j-impl

	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:3.1.8") // Compatible with Spring Boot 2.7.x
	runtimeOnly("com.h2database:h2")
	annotationProcessor("org.projectlombok:lombok:1.18.38")
// implementation("org.apache.hadoop:hadoop-client-api:3.4.1")
// implementation("org.apache.hadoop:hadoop-client-runtime:3.4.1")
// Explicitly add Hadoop common to ensure proper version
// implementation("org.apache.hadoop:hadoop-common:3.4.1")
// { // exclude(group = "org.slf4j") // exclude(group = "log4j") // exclude(group = "org.apache.logging.log4j") // exclude(group = "com.sun.jersey") // }
	// Force Hadoop 3.3.2
//	implementation("org.apache.hadoop:hadoop-client:3.3.2") {
//		// exclude transitive Hadoop 3.3.4 shipped by Spark
//		exclude(group = "org.apache.hadoop", module = "hadoop-common")
//		exclude(group = "org.apache.hadoop", module = "hadoop-auth")
//		exclude(group = "org.apache.hadoop", module = "hadoop-hdfs")
//		exclude(group = "org.apache.hadoop", module = "hadoop-mapreduce-client-core")
//	}
//	implementation("org.apache.hadoop:hadoop-common:3.3.2")
//	implementation("org.apache.hadoop:hadoop-auth:3.3.2")
//	implementation("org.apache.hadoop:hadoop-hdfs:3.3.2")
//	implementation("org.apache.hadoop:hadoop-mapreduce-client-core:3.3.2")
//	implementation("org.apache.hadoop:hadoop-client-runtime:3.3.2")
	implementation("javax.servlet:javax.servlet-api:4.0.1")
	// Add Jersey servlet container for Spark UI
	implementation("org.glassfish.jersey.containers:jersey-container-servlet-core:2.35")
	implementation("org.glassfish.jersey.inject:jersey-hk2:2.35")
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


//
//tasks.withType<JavaExec> {
//	jvmArgs = listOf( "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-exports=java.base/sun.security.action=ALL-UNNAMED" )
//}
//
//tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
//	dependsOn(tasks.compileJava)
//	jvmArgs = listOf( "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens=java.base/java.lang=ALL-UNNAMED", "--add-opens=java.base/java.nio=ALL-UNNAMED", "--add-exports=java.base/sun.security.action=ALL-UNNAMED" )
////	classpath = configurations["localRunClasspath"]
//}

// Java 11 doesn't need module system flags like Java 17
// tasks.withType<JavaExec> {
//	 jvmArgs = listOf(...)
// }

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
	dependsOn(tasks.compileJava)
	// Java 11 normally doesn't require aggressive opens, but keep for Spark compatibility
	val sparkModuleOpens = listOf(
		"--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
		"--add-opens=java.base/java.lang=ALL-UNNAMED",
		"--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
		"--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
		"--add-opens=java.base/java.io=ALL-UNNAMED",
		"--add-opens=java.base/java.net=ALL-UNNAMED",
		"--add-opens=java.base/java.nio=ALL-UNNAMED",
		"--add-opens=java.base/java.util=ALL-UNNAMED",
		"--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
		"--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
		"--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
		"--add-opens=java.base/sun.nio.cs=ALL-UNNAMED",
		"--add-opens=java.base/sun.security.action=ALL-UNNAMED",
		"--add-opens=java.base/sun.util.calendar=ALL-UNNAMED",
		"--add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED"
	)
	jvmArgs(sparkModuleOpens)
}

// Configure JVM arguments for Spark/Hadoop compatibility with Java 17+
// tasks.withType<JavaExec> {
// jvmArgs = listOf( // "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED", // "--add-opens=java.base/java.lang=ALL-UNNAMED", // "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED", // "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", // "--add-opens=java.base/java.io=ALL-UNNAMED", // "--add-opens=java.base/java.net=ALL-UNNAMED", // "--add-opens=java.base/java.nio=ALL-UNNAMED", // "--add-opens=java.base/java.util=ALL-UNNAMED", // "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED", // "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED", // "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", // "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED", // "--add-opens=java.base/sun.security.action=ALL-UNNAMED", // "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED", // "--add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED" // ) //} //
// tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") { // jvmArgs = listOf( // "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED", // "--add-opens=java.base/java.lang=ALL-UNNAMED", // "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED", // "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", // "--add-opens=java.base/java.io=ALL-UNNAMED", // "--add-opens=java.base/java.net=ALL-UNNAMED", // "--add-opens=java.base/java.nio=ALL-UNNAMED", // "--add-opens=java.base/java.util=ALL-UNNAMED", // "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED", // "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED", // "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", // "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED", // "--add-opens=java.base/sun.security.action=ALL-UNNAMED", // "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED", // "--add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED" // ) //}

