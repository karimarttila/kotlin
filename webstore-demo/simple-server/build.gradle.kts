import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.gradle.plugins.ide.idea.model.IdeaLanguageLevel
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val gradleVersion = "5.6.3"
val kotlinVersion = "1.3.50"
val ktorVersion = "1.2.5"
val slf4jVersion = "1.7.25"
val junitVersion = "5.5.2"
val hamcrestVersion = "2.1"
val logbackVersion = "1.2.3"
val opencsvVersion = "4.6"
val commonsCodecVersion = "1.11"
val jvmTarget = "1.8"

plugins {
    kotlin("jvm") version "1.3.50"
    java
    idea
}

repositories {
    mavenCentral()
    "http://dl.bintray.com/kotlin".let {
        maven { setUrl("$it/ktor") }
        maven { setUrl("$it/kotlinx") }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.opencsv:opencsv:$opencsvVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("commons-codec:commons-codec:$commonsCodecVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-client-jetty:$ktorVersion")
    implementation("io.ktor:ktor-client-auth-basic:$ktorVersion")
    implementation("io.ktor:ktor-client-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-gson:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    implementation("com.natpryce:konfig:1.6.10.0")
    // Tests.
    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testCompile("org.hamcrest:hamcrest-library:$hamcrestVersion")
    testCompile("io.ktor:ktor-server-tests:$ktorVersion")
    runtime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

}

tasks.withType(Wrapper::class.java).configureEach {
    gradleVersion = gradleVersion
    distributionType = Wrapper.DistributionType.BIN
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = jvmTarget
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<IdeaModel> {
    project {
        languageLevel = IdeaLanguageLevel(JavaVersion.VERSION_1_8)
    }
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events = setOf(FAILED, PASSED, SKIPPED, STANDARD_OUT)
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showStackTraces = true
        showExceptions = true
    }
    addTestListener(object : TestListener {
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            printTestSummary(suite, result)
        }
        // Not needed but required to implement the TestListener interfaces.
        override fun beforeSuite(suite: TestDescriptor) = Unit
        override fun beforeTest(test: TestDescriptor) = Unit
        override fun afterTest(test: TestDescriptor, result: TestResult) = Unit
    })
}

enum class Color(val number: Byte) {
    BLACK(0), RED(1), GREEN(2), YELLOW(3), BLUE(4), MAGENTA(5), CYAN(6), WHITE(7);
}

fun printTestSummary(testDescriptor: TestDescriptor, testResult: TestResult) {
    val prefix = "\u001B"
    val reset = "$prefix[0m"
    if (testDescriptor.parent != null) {
        val testResultSummary = testResult.run {
            // See TestResult interface.
            "Test results: $resultType (" + "$testCount tests, " + "$successfulTestCount successes, " +
                    "$failedTestCount failures, " + "$skippedTestCount skipped" + ")"
        }
        // Let's add some colors...
        val color = when(testResult.resultType) {
            TestResult.ResultType.SUCCESS -> "$prefix[0;3${Color.GREEN.number}m"
            TestResult.ResultType.FAILURE -> "$prefix[0;3${Color.RED.number}m"
            // TODO: Skipped not showing.
            TestResult.ResultType.SKIPPED -> "$prefix[0;3${Color.YELLOW.number}m"
        }
        val line = color + "*".repeat(testResultSummary.length) + reset
        println("\n" + line + "\n" + testResultSummary + "\n" + line + "\n")
    }
}
