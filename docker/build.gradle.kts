import java.lang.System.out

dependencies {
    module(":pv-stats")
}

val pvStatsProject = findProject(":pv-stats")!!

val sourceDirectory = File(projectDir, "src")
val bundleDirectory = File(buildDir, "bundle")
val imagesDirectory = File(bundleDirectory, "install")
val resourcesToFilter = listOf(Pair("mariadb/init.sql", "resources/maria-init"))
val propertiesToAppendToEnv = mapOf(Pair("PV_STATS_VERSION", pvStatsProject.version))

val envProperties = java.util.Properties()
envProperties.load(java.io.FileInputStream(File(sourceDirectory, ".env")))

tasks.register("clean", type = Delete::class) {
    delete(buildDir)
}

tasks.register("dockerCompose") {
    dependsOn("createArchive")
}

tasks.register("createArchive", type = Zip::class) {
    dependsOn("copyResources", "filterResources", "saveImages")

    from(bundleDirectory)
    include("**/*")
    archiveBaseName.set("smart-home")
    archiveVersion.set(pvStatsProject.version.toString())
    destinationDirectory.set(buildDir)
}

tasks.register("saveImages", type = Exec::class) {
    dependsOn("buildImages")

    val pvStatsArchiveName = "pv-stats-${pvStatsProject.version}.tar"

    doFirst {
        imagesDirectory.mkdirs()
    }

    workingDir("src/pv-stats")
    standardOutput = out
    errorOutput = out
    commandLine("cmd", "/c", "docker save pv-stats:${pvStatsProject.version} -o ${imagesDirectory.absolutePath}/$pvStatsArchiveName")

    doLast {
        val installerScript = """
            #!/bin/sh
            
            docker load --input $pvStatsArchiveName
        """.trimIndent()
        val installerScriptFile = File(imagesDirectory, "install.sh")
        installerScriptFile.writeText(installerScript)
    }
}

tasks.register("buildImages", type = Exec::class) {
    dependsOn("copyArtifacts")

    workingDir("src/pv-stats")
    standardOutput = out
    errorOutput = out
    commandLine("cmd", "/c", "docker build --tag pv-stats:${pvStatsProject.version} .")
}

tasks.register("copyArtifacts", type = Copy::class) {
    dependsOn(":pv-stats:bootWar")

    val artifactDir = File(pvStatsProject.buildDir, "libs")
    from(artifactDir) {
        include("*.war")
        into("pv-stats")
        rename(".*", "application.war")
    }

    destinationDir = sourceDirectory
}

tasks.register("copyResources", type = Copy::class) {
    from(sourceDirectory) {
        include(".env", "*.yml")
        into(".")
    }

    from("src/pv-stats") {
        include("*.yml", "*.xml")
        into("config/pv-stats")
    }

    destinationDir = bundleDirectory

    doLast {
        var text = ""
        propertiesToAppendToEnv.forEach { (k, v) ->
            text += String.format("$k=$v%n")
        }
        val targetEnvFile = File(bundleDirectory, ".env")
        targetEnvFile.appendText(text)
    }
}

tasks.register("filterResources") {
    doLast {
        resourcesToFilter.forEach {
            val file = File(sourceDirectory, it.first)
            val filtered = swapPlaceholders(file.readText())
            val outDir = File(bundleDirectory, it.second)
            if (!outDir.isDirectory)
                outDir.mkdirs()
            val outFile = File(outDir, file.name)
            outFile.writeText(filtered)
        }
    }
}

fun swapPlaceholders(input: String): String {
    var output = input
    for (propertyName in envProperties.propertyNames()) {
        output = output.replace("\${$propertyName}", envProperties[propertyName].toString(), false)
    }

    return output
}