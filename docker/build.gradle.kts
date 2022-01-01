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
        val archiveName = getArtifactName()
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

fun getContainerRegistryToken(): String? {
    // from ~/.gradle/gradle.properties
    return findProperty("gitLabPrivateToken") as String?
}

fun getArtifactName(): String {
    return project(":pv-stats").let {
        val libsDir = File(it.buildDir, "libs")
        if (libsDir.isDirectory) {
            val list = libsDir.listFiles()!!
            if (list.isNotEmpty()) {
                val name = list[0].name
                logger.info("pv-stats argifact found: $name")
                name
            } else {
                logger.error("pv-stats artifact wasn't found")
                null
            }
        } else null
    } ?: "unknown.war"
}