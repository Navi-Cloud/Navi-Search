import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
	kotlin("plugin.jpa") version "1.5.21"
	id("jacoco")
}

jacoco {
	toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
	reports {
		html.required.set(true)
		xml.required.set(false)
		csv.required.set(true)
	}
	finalizedBy("jacocoTestCoverageVerification")
}

tasks.jacocoTestCoverageVerification {
	violationRules {
		rule {
			enabled = true
			element = "CLASS"

			limit {
				counter = "BRANCH"
				value = "COVEREDRATIO"
				minimum = "0.90".toBigDecimal()
			}

			limit {
				counter = "LINE"
				value = "COVEREDRATIO"
				minimum = "0.95".toBigDecimal()
			}

			limit {
				counter = "LINE"
				value = "TOTALCOUNT"
				maximum = "200".toBigDecimal()
			}
			excludes = listOf(
				"com.navi.searchservice.NaviSearchServiceApplication",
				"com.navi.searchservice.NaviSearchServiceApplicationKt",
				"com.navi.searchservice.model.**"
			)
		}
	}
}

group = "com.navi"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.0")
	implementation("com.fasterxml.jackson.core:jackson-core:2.12.0")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.12.0")
	implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.0")

	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation ("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0") //Mockito-Kotlin
	testImplementation ("org.mockito:mockito-inline:2.21.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	finalizedBy("jacocoTestReport")
}