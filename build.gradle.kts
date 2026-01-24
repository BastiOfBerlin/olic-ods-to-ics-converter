plugins {
    id("java")
}

group = "de.bastiofberlin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 25
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

dependencies {
    implementation(libs.picocli)
    implementation(libs.guava)
    implementation(libs.odf)
    implementation(libs.ical4j)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.picocli.codegen)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Source - https://stackoverflow.com/a
// Posted by Stanislav, modified by community. See post 'Timeline' for change history
// Retrieved 2025-12-18, License - CC BY-SA 4.0

// Source - https://stackoverflow.com/a
// Posted by Denis Zavedeev
// Retrieved 2025-12-18, License - CC BY-SA 4.0

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "de.bastiofberlin.olicodstoicsconverter.Application"
        //attributes["Class-Path"] = configurations.runtimeClasspath.files.collect { it.getName() }.join(' ')
    }
}

tasks.test {
    useJUnitPlatform()
}