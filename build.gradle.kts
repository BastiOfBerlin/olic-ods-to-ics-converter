plugins {
    id("java")
    application
    alias(libs.plugins.shadow)
}

application {
    mainClass = "de.bastiofberlin.olicodstoicsconverter.Application"
    applicationName = "olic-ods-to-ics"
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

tasks.withType<Jar>().configureEach {
    manifest {
        attributes["Main-Class"] = application.mainClass
        attributes["Implementation-Version"] = project.version
    }
}

tasks.shadowJar {
    // odfdom (xerces) and ical4j register implementations via META-INF/services
    mergeServiceFiles()
    // INCLUDE is required so all copies of the service files reach the merger
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    // Duplicate license/notice files from the dependencies are not needed in the fat jar
    exclude("META-INF/LICENSE*", "META-INF/NOTICE*", "META-INF/DEPENDENCIES", "META-INF/versions/9/OSGI-INF/MANIFEST.MF")
}

tasks.test {
    useJUnitPlatform()
}