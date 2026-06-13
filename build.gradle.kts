plugins {
    java
    id("org.springframework.boot") version "3.2.6"
    id("io.spring.dependency-management") version "1.1.5"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.cardgame"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Redis
    implementation("redis.clients:jedis")

    // OpenAPI/Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    // Jackson for JSON
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.21.4")
    testImplementation("org.testcontainers:junit-jupiter:1.21.4")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

// Testcontainers reuse configuration
tasks.withType<Test> {
    systemProperty("testcontainers.reuse.enable", "true")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.19.2")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(4)
    }
}

tasks.register("preCommit") {
    dependsOn("spotlessApply", "test")
    description = "Run all pre-commit checks (spotless + tests)"
}

tasks.register("integrationTest", Test::class) {
    description = "Run integration tests only"
    group = "verification"

    useJUnitPlatform()

    // Performance tuning
    maxHeapSize = "1024m"
    jvmArgs("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=100")

    // Test execution settings - disable parallelization to avoid Redis connection conflicts
    maxParallelForks = 1

    // Filter by package/class name
    filter {
        includeTestsMatching("com.cardgame.integration.*")
    }

    // Show detailed test output
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    // Ensure Docker is available for Testcontainers
    doFirst {
        println("🐳 Starting integration tests with Testcontainers...")
        println("📦 Max parallel forks: $maxParallelForks")
    }

    doLast {
        println("✅ Integration tests completed!")
    }
}
