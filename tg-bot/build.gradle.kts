plugins {
    id("java")
}

group = "polis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":ok-api"))
    implementation("com.puppycrawl.tools:checkstyle:10.3.3")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.apache.httpcomponents:httpclient:4.5.14")
    implementation("org.json:json:20230227")
    implementation("org.telegram:telegrambots:6.5.0")
    implementation("org.telegram:telegrambotsextensions:6.5.0")
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("org.springframework.boot:spring-boot-starter-web:3.0.4")
    implementation("org.telegram:telegrambots-spring-boot-starter:6.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
