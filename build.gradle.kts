plugins {
    id("java")
    id("checkstyle")
}

group = "polis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

subprojects {
    apply(plugin = "checkstyle")
    tasks.withType<Checkstyle>().configureEach {
        configFile = project.rootDir.absoluteFile.resolve("checkstyle.xml")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

tasks.register("checkstyleMainAll") {
    group = "other"
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("checkstyleMain") })
}
