plugins {
    kotlin("jvm") version "1.3.71" apply false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
    }
}

subprojects {
    group = "dev.drzepka"
    version = "1.2.2"
}