plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.kotlin.multiplatform.library)

}

kotlin {
    jvmToolchain(17)


    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)

            implementation(project(":kfirebaseAuth"))

            implementation(libs.compose.google.sigin.in1)
            implementation(libs.compose.utils.core)


        }



        androidMain.dependencies {
            implementation(libs.ui.tooling)
            implementation(libs.androidx.activityCompose)
        }

    }

    android {
        namespace = "io.gituhb.demo"
        compileSdk = 36
        minSdk = 23

    }
}

