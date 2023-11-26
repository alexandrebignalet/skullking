plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.allopen") version "1.9.10"
    id("io.quarkus")
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation("io.quarkus:quarkus-websockets")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("commons-io:commons-io:2.15.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-resteasy-reactive-qute")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("com.github.f4b6a3:ksuid-creator:4.1.0")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.quarkiverse.mockk:quarkus-junit5-mockk:2.0.0")
    testImplementation("io.rest-assured:rest-assured")
    testApi("org.assertj:assertj-core:3.11.1")
    testImplementation("org.awaitility:awaitility-kotlin:4.0.3")
    testImplementation("com.approvaltests:approvaltests:22.3.2")
}

group = "org.skull.king"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}
allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}
