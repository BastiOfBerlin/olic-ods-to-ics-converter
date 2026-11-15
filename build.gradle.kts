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
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "de.bastiofberlin.olicodstoicsconverter.Application"
        //attributes["Class-Path"] = configurations.runtimeClasspath.files.collect { it.getName() }.join(' ')
    }
}

tasks.test {
    useJUnitPlatform()
}