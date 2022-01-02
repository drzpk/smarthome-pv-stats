plugins {
    id("com.google.cloud.tools.jib") version "2.5.0"
    id("java")
}

jib {
    from {
        image = "openjdk:8u265-jre-slim-buster"
    }
    to {
        image = "registry.gitlab.com/smart-home-dr/pv-stats/pv-stats"
        tags = setOf(project.version as String)

        val token = getContainerRegistryToken()
        if (token != null) {
            auth {
                username = "d_rzepka"
                password = token
            }
        }
    }
    container {
        val archiveName = "pv-stats-${version}.war"
        entrypoint = listOf("java", "-jar", "/app/$archiveName")
        ports = listOf("8080")
        workingDirectory = "/app"

        creationTime = "USE_CURRENT_TIMESTAMP"
        labels = mapOf(
                Pair("Maintainer", "dominik.1.rzepka@gmail.com")
        )
    }
    extraDirectories {
        paths {
            path {
                setFrom(File(project(":pv-stats").buildDir, "libs"))
                into = "/app"
            }
        }
    }
}

tasks.withType<com.google.cloud.tools.jib.gradle.JibTask> {
    dependsOn(":pv-stats:cleanBootWar")
}

fun getContainerRegistryToken(): String? {
    // from ~/.gradle/gradle.properties
    return findProperty("gitLabPrivateToken") as String?
}
