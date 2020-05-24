plugins {
    kotlin("jvm")
}

version = "1.0.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compileOnly("com.fasterxml.jackson.core:jackson-core:2.11.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.11.0")
    compileOnly("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.0")
}