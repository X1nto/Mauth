import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    kotlin("plugin.compose")
    id("com.google.protobuf")
}

android {
    namespace = "com.xinto.mauth"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.xinto.mauth"
        minSdk = 23
        targetSdk = 35
        versionCode = 100
        versionName = "0.10.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            // Distinguish between debug and release version
            // Without this they cannot be installed both at the same time
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        disable += "MissingTranslation"
        disable += "ExtraTranslation"
    }

    sourceSets {
        // Expose the exported Room schemas to instrumented tests (MigrationTestHelper).
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)

        val buildDir = layout.buildDirectory.asFile.get().absolutePath
        if (project.findProperty("composeCompilerReports") == "true") {
            freeCompilerArgs.add(
                "-P plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${buildDir}/compose_compiler"
            )
        }
        if (project.findProperty("composeCompilerMetrics") == "true") {
            freeCompilerArgs.add(
                "-P plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${buildDir}/compose_compiler"
            )
        }
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_stability.conf"))
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.3"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    val composeBom = platform("androidx.compose:compose-bom-alpha:2026.06.01")
    implementation(composeBom)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.compose.ui:ui-tooling-preview")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    val cameraxVersion = "1.6.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    val roomVersion = "2.8.4"
    implementation("androidx.room:room-common:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    // Needed for room-testing
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core") {
        version { strictly("1.8.1") }
    }

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto-ktx:1.1.0")

    implementation("androidx.datastore:datastore-preferences:1.2.1")

    implementation("androidx.emoji2:emoji2-emojipicker:1.6.0")

    implementation("sh.calvin.reorderable:reorderable:3.1.0")

    implementation("com.google.protobuf:protobuf-javalite:4.35.1")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.5.0")

    val navigationVersion = "1.1.4"
    implementation("androidx.navigation3:navigation3-runtime:$navigationVersion")
    implementation("androidx.navigation3:navigation3-ui:$navigationVersion")

    implementation("commons-codec:commons-codec:1.22.0")

    implementation("com.google.zxing:core:3.5.4")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("io.insert-koin:koin-androidx-compose:4.2.2")

    val accompanistVersion = "0.37.3"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}