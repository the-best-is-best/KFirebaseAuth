import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
    id("maven-publish")
    id("signing")

}




apply(plugin = "maven-publish")
apply(plugin = "signing")


tasks.withType<PublishToMavenRepository> {
    val isMac = getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}


extra["packageNameSpace"] = "io.github.firebase_auth"
extra["groupId"] = "io.github.the-best-is-best"
extra["artifactId"] = "kfirebase-auth"
extra["version"] = "1.1.0"
extra["packageName"] = "KFirebaseAuth"
extra["packageUrl"] = "https://github.com/the-best-is-best/KFirebaseAuth"
extra["packageDescription"] =
    "KFirebaseAuth is a Kotlin Multiplatform library that provides a unified and idiomatic API for integrating Firebase Authentication across Android and iOS. It enables you to handle sign-in, sign-out, user session management, and email verification using shared Kotlin code—no need to write platform-specific logic."
extra["system"] = "GITHUB"
extra["issueUrl"] = "https://github.com/the-best-is-best/KFirebaseAuth/issues"
extra["connectionGit"] = "https://github.com/the-best-is-best/KFirebaseAuth.git"

extra["developerName"] = "Michelle Raouf"
extra["developerNameId"] = "MichelleRaouf"
extra["developerEmail"] = "eng.michelle.raouf@gmail.com"


mavenPublishing {
    coordinates(
        extra["groupId"].toString(),
        extra["artifactId"].toString(),
        extra["version"].toString()
    )

    publishToMavenCentral(SonatypeHost.S01, true)
    signAllPublications()

    pom {
        name.set(extra["packageName"].toString())
        description.set(extra["packageDescription"].toString())
        url.set(extra["packageUrl"].toString())
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        issueManagement {
            system.set(extra["system"].toString())
            url.set(extra["issueUrl"].toString())
        }
        scm {
            connection.set(extra["connectionGit"].toString())
            url.set(extra["packageUrl"].toString())
        }
        developers {
            developer {
                id.set(extra["developerNameId"].toString())
                name.set(extra["developerName"].toString())
                email.set(extra["developerEmail"].toString())
            }
        }
    }

}


signing {
    useGpgCmd()
    sign(publishing.publications)
}

kotlin {

// Target declarations - add or remove as needed below. These define
// which platforms this KMP module supports.
// See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "io.github.kfirebase_auth"
        compileSdk = 35
        minSdk = 23
    }

// For iOS targets, this is also where you should
// configure native binary output. For more information, see:
// https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

// A step-by-step guide on how to include this library in an XCode
// project can be found here:
// https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "kfirebaseAuthKit"

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { target ->
        target.binaries {
            framework {
                baseName = xcfName
                isStatic = true
            }
        }
        target.compilations.getByName("main") {
            val defFileName = when (target.name) {
                "iosX64" -> "iosX64.def"
                "iosArm64" -> "iosArm64.def"
                "iosSimulatorArm64" -> "iosSimulatorArm64.def"
                "macosX64" -> "macosX64.def"
                "macosArm64" -> "macosArm64.def"
                "tvosX64" -> "tvosX64.def"
                "tvosArm64" -> "tvosArm64.def"
                "tvosSimulatorArm64" -> "tvosSimulatorArm64.def"
                "watchosSimulatorArm64" -> "watchosSimulatorArm64.def"
                "watchosX64" -> "watchosX64.def"
                "watchosArm32" -> "watchosArm32.def"
                "watchosArm64" -> "watchosArm64.def"

                else -> throw IllegalStateException("Unsupported target: ${target.name}")
            }

            val defFile = project.file("interop/$defFileName")
            if (defFile.exists()) {
                cinterops.create("FirebseAuth") {
                    defFile(defFile)
                    packageName = "io.github.native.kfirebase.auth"
                }
            } else {
                logger.warn("Def file not found for target ${target.name}: ${defFile.absolutePath}")
            }
        }
    }

// Source set declarations.
// Declaring a target automatically creates a source set with the same name. By default, the
// Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
// common to share sources between related targets.
// See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        all {
            languageSettings.apply {

                progressiveMode = true
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.coroutines.FlowPreview")
                optIn("kotlinx.serialization.InternalSerializationApi")
                if (name.lowercase().contains("ios")) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                // Add KMP dependencies here
                implementation(libs.kotlinx.coroutines.core)
                api(libs.kfirebase.core)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.


                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.auth)

            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }
    }
}
abstract class GenerateDefFilesTask : DefaultTask() {

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val interopDir: DirectoryProperty

    @TaskAction
    fun generate() {
        // Ensure the directory exists
        interopDir.get().asFile.mkdirs()

        // Constants
        val firebaseAuthHeaders = listOf(
            "KFirebaseAuthInterop-Swift.h"

            // Add other headers as necessary
        )

        // Map targets to their respective paths
        val targetToPath = mapOf(
            "iosX64" to "ios-arm64_x86_64-simulator",
            "iosArm64" to "ios-arm64",
            "iosSimulatorArm64" to "ios-arm64_x86_64-simulator",
            "macosX64" to "macos-arm64_x86_64",
            "macosArm64" to "macos-arm64_x86_64",
            "tvosArm64" to "tvos-arm64",
            "tvosX64" to "tvos-arm64_x86_64-simulator",
            "tvosSimulatorArm64" to "tvos-arm64_x86_64-simulator",
            "watchosSimulatorArm64" to "watchos-arm64_x86_64-simulator",
            "watchosX64" to "watchos-arm64_arm64_32",
            "watchosArm32" to "watchos-arm64_arm64_32",
            "watchosArm64" to "watchos-arm64_arm64_32"
        )

        // Helper function to generate header paths
        fun headerPath(target: String, file: String): String {
            return interopDir.dir("src/$file")
                .get().asFile.absolutePath
        }

        // Generate headerPaths dynamically
        val headerPaths = firebaseAuthHeaders.map { file ->
            targetToPath.mapValues { (target, _) ->
                headerPath(target, file)
            }
        }

        // List of targets derived from targetToPath keys
        val iosTargets = targetToPath.keys.toList()

        // Loop through the targets and create the .def files
        iosTargets.forEach { target ->
            val allHeaderPaths = headerPaths.joinToString(" ") { map -> map[target] ?: "" }.trim()

            if (allHeaderPaths.isNotEmpty()) {
                val defFile = File(interopDir.get().asFile, "$target.def")

                // Generate the content for the .def file
                val content = """
                    language = Objective-C
                    package = ${packageName.get()}
                    headers = $allHeaderPaths
                """.trimIndent()

                // Write content to the .def file
                defFile.writeText(content)
                println("Generated: ${defFile.absolutePath} with headers = $allHeaderPaths")
            }
        }
    }
}

// Register the task within the Gradle build
tasks.register<GenerateDefFilesTask>("generateDefFiles") {
    packageName.set("io.github.native.kfirebase.auth")
    interopDir.set(project.layout.projectDirectory.dir("interop"))
}

tasks.named("build") {
    dependsOn(tasks.named("generateDefFiles"))
}
