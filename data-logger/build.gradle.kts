import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("war")
    kotlin("jvm")
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":common"))

    implementation("com.fasterxml.jackson.core:jackson-core:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")

    testImplementation("org.junit.platform:junit-platform-launcher:1.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
