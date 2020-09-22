plugins {
    id("maven")
    id("maven-publish")
}

subprojects {
    group = "dev.drzepka.smarthome"
    version = "1.3.0"
}

allprojects {
    repositories {
        jcenter()
        mavenCentral()
        mavenLocal()

        maven {
            setupSmartHomeRepo("https://gitlab.com/api/v4/projects/21177602/packages/maven", false)
        }
    }
}

fun MavenArtifactRepository.setupSmartHomeRepo(repoUrl: String, publishing: Boolean) {
    setUrl(repoUrl)
    credentials(HttpHeaderCredentials::class) {
        val ciToken = System.getenv("CI_JOB_TOKEN")
        val privateToken = findProperty("gitLabPrivateToken") as String? // from ~/.gradle/gradle.properties

        when {
            ciToken != null -> {
                name = "Job-Token"
                value = ciToken
            }
            privateToken != null -> {
                name = "Private-Token"
                value = privateToken
            }
            else -> {
                val suffix = if (publishing) "publishing will fail" else "Smart Home dependencies cannot be downloaded"
                logger.warn("Neither job nor private token were defined, $suffix")
            }
        }
    }
    authentication {
        create<HttpHeaderAuthentication>("header")
    }
}