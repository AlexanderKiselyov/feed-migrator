plugins {
    java
}

group = "polis"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("org.json:json:20230227")

    compileOnly("com.vk.api:sdk:1.0.14")

    implementation("org.slf4j:slf4j-api:2.0.7")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.7")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    testImplementation("org.apache.logging.log4j:log4j-core:2.17.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

configurations.all {
    resolutionStrategy.capabilitiesResolution.withCapability("dev.jacomet.logging", "slf4j-impl:1.0") {
        select("org.slf4j:slf4j-simple:0")
    }
    resolutionStrategy.capabilitiesResolution.withCapability("dev.jacomet.logging", "commons-logging-impl:1.0") {
        select("commons-logging:commons-logging:0")
    }
    resolutionStrategy.capabilitiesResolution.withCapability("dev.jacomet.logging", "log4j2-vs-slf4j:1.0") {
        select("org.apache.logging.log4j:log4j-to-slf4j:0")
    }
    resolutionStrategy.capabilitiesResolution.withCapability("dev.jacomet.logging", "log4j2-impl:1.0") {
        select("org.apache.logging.log4j:log4j-core:0")
    }
}
