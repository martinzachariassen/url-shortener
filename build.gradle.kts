plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.mockk:mockk:1.13.14")
    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
    testImplementation("org.testcontainers:postgresql:1.20.4")

    // Explicitly adding the patched version of commons-compress (1.27.1) to override a vulnerable transitive dependency.
    // Testcontainers transitively brings in commons-compress:1.24.0, which has known vulnerabilities:
    // - GHSA-4g9r-vxhx-9pgx (Denial of Service via infinite loop with corrupted DUMP files)
    // - GHSA-4265-ccf5-phj5 (OutOfMemoryError with broken Pack200 files)
    // Adding this dependency ensures the application uses a safe version of commons-compress during testing.
    testImplementation("org.apache.commons:commons-compress:1.27.1")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
