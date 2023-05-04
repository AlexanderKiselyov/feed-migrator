plugins {
    id("java")
    id("dev.jacomet.logging-capabilities") version "0.11.0"
}

group = "polis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ok-api"))
    implementation(project(":api-rate-limiter"))

    implementation("org.json:json:20230227")
    implementation("org.telegram:telegrambots:6.5.0")
    implementation("org.telegram:telegrambotsextensions:6.5.0")
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.4")
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra:3.0.4")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.5.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")

    implementation("org.slf4j:slf4j-api:2.0.7")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.7")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

val run by tasks.registering(JavaExec::class) {
    group = "build"
    mainClass.set("polis.FeedMigratorBot")
    workingDir = rootDir
    classpath = files(tasks.compileJava, configurations.runtimeClasspath)
}

configurations.all {
    resolutionStrategy.capabilitiesResolution.withCapability("dev.jacomet.logging", "slf4j-impl:1.0") {
        select("org.slf4j:slf4j-simple:0")
    }
    resolutionStrategy.capabilitiesResolution.withCapability("dev.jacomet.logging", "commons-logging-impl:1.0") {
        select("commons-logging:commons-logging:0")
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
