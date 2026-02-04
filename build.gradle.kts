import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.springframework.boot") version "3.4.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.jetbrains.kotlinx.kover") version "0.9.5"
}

group = "ru.spbstu.memory.cards"

sourceSets {
    named("main") { resources.setSrcDirs(listOf("src/main/resources")) }
    named("test") { resources.setSrcDirs(listOf("src/test/resources")) }
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories { mavenCentral() }

val exposedVersion = "0.60.0"
val postgresVersion = "42.7.4"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.liquibase:liquibase-core")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.testcontainers:junit-jupiter:1.20.1")
    testImplementation("org.testcontainers:postgresql:1.20.1")
}

ktlint {
    verbose.set(true)
    android.set(false)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
    }
}

tasks.withType<KotlinCompile> {
    dependsOn("ktlintFormat")
}
tasks.named("ktlintCheck") {
    dependsOn("ktlintFormat")
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "ru.spbstu.memory.cards.Application*",
                    "ru.spbstu.memory.cards.config*",
                    "ru.spbstu.memory.cards.persistence.config*",
                    "ru.spbstu.memory.cards.persistence.table*",
                )
            }
        }

        total {
            html { onCheck = true }
            xml { onCheck = true }
        }
    }
}

tasks.named("check") {
    dependsOn("ktlintCheck", "test", "koverHtmlReport", "koverVerify")
}
tasks.named("build") {
    dependsOn("ktlintCheck", "test", "koverHtmlReport", "koverVerify")
}

tasks.withType<Test> {
    useJUnitPlatform()

    val logEvents =
        setOf(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED,
        )

    testLogging {
        lifecycle {
            events = logEvents
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
        }
    }

    val failedTests = mutableListOf<TestDescriptor>()
    val skippedTests = mutableListOf<TestDescriptor>()

    fun List<TestDescriptor>.printSummary(subject: String) {
        logger.lifecycle(subject)
        forEach { test ->
            val suite = test.parent?.name ?: "-"
            logger.lifecycle("\t$suite - ${test.name}")
        }
    }

    addTestListener(
        object : TestListener {
            override fun beforeSuite(suite: TestDescriptor) {}

            override fun beforeTest(testDescriptor: TestDescriptor) {}

            override fun afterTest(
                testDescriptor: TestDescriptor,
                result: TestResult,
            ) {
                when (result.resultType) {
                    TestResult.ResultType.FAILURE -> failedTests.add(testDescriptor)
                    TestResult.ResultType.SKIPPED -> skippedTests.add(testDescriptor)
                    else -> Unit
                }
            }

            override fun afterSuite(
                suite: TestDescriptor,
                result: TestResult,
            ) {
                if (suite.parent == null) {
                    logger.lifecycle(
                        """
                        Test result: ${result.resultType}
                        Test summary:
                            ${result.testCount} tests,
                            ${result.successfulTestCount} succeeded,
                            ${result.failedTestCount} failed,
                            ${result.skippedTestCount} skipped
                        """.trimIndent(),
                    )
                    if (failedTests.isNotEmpty()) failedTests.printSummary("Failed Tests:")
                    if (skippedTests.isNotEmpty()) skippedTests.printSummary("Skipped Tests:")
                }
            }
        },
    )
}
