plugins {
    id("java")
    id("checkstyle")
}

group = "polis"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
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
