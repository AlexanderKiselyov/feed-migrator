plugins {
    java
}

group = "polis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("org.json:json:20230227")

    compileOnly("com.vk.api:sdk:1.0.9")

    implementation("org.slf4j:slf4j-api:2.0.7")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.7")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}