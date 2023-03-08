plugins {
    id("java")
}

group = "polis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("org.apache.httpcomponents:httpmime:4.5.14")
    implementation("org.json:json:20230227")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
