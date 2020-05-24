import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("war")
    kotlin("jvm")
}

version = "1.1.0"

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
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0")
    implementation("org.apache.httpcomponents:httpclient:4.5.12")

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

tasks.register("fatJar", type = Jar::class) {
    manifest {
        attributes["Main-Class"] = "dev.drzepka.pvstats.logger.PVStatsDataLogger"
        attributes["Implementation-Version"] = project.version.toString()
        attributes["Implementation-Title"] = "PV-Stats data logger"
    }
    archiveBaseName.set("pvstats-logger")
    archiveVersion.set(project.version.toString())

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}
