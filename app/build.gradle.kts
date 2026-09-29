import com.github.takahirom.roborazzi.AnnotationFilter
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    kotlin("plugin.compose")
    id("com.google.protobuf")
    kotlin("plugin.serialization")
    id("io.github.takahirom.roborazzi")
}

android {
    namespace = "com.xinto.mauth"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.xinto.mauth"
        minSdk = 24
        targetSdk = 37
        versionCode = 110
        versionName = "0.11.0"

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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                it.systemProperties["robolectric.pixelCopyRenderMode"] = "hardware"
            }
        }
    }

    sourceSets {
        // Expose the exported Room schemas to instrumented tests (MigrationTestHelper).
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)

    }
}

composeCompiler {
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_stability.conf"))

    if (project.findProperty("composeCompilerReports") == "true") {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }
    if (project.findProperty("composeCompilerMetrics") == "true") {
        metricsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}

roborazzi {
    outputDir.set(file("src/test/screenshots"))

    @OptIn(ExperimentalRoborazziApi::class)
    generateComposePreviewRobolectricTests {
        enable = true
        packages = listOf("com.xinto.mauth")
        includePrivatePreviews = true
        annotationFilter = AnnotationFilter.Include("com.xinto.mauth.ui.preview.Screenshot")
        testerQualifiedClassName = "com.xinto.mauth.screenshot.PreviewScreenshotTester"
        // PreviewScreenshotTester delegates scanning to AndroidComposePreviewTester, which applies the options above
        useScanOptionParametersInTester = true
        robolectricConfig = mapOf(
            "sdk" to "[36]",
            "qualifiers" to "RobolectricDeviceQualifiers.Pixel4a",
            // Skip the app's Application, which starts Koin once per process
            "application" to "android.app.Application::class",
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.36.2"
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
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")

    val composeBom = platform("androidx.compose:compose-bom-alpha:2026.09.01")
    implementation(composeBom)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3.adaptive:adaptive")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    val cameraxVersion = "1.6.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    val roomVersion = "2.8.5"
    implementation("androidx.room:room-common:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")

    implementation("org.bouncycastle:bcprov-jdk18on:1.86")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto-ktx:1.1.0")

    implementation("androidx.datastore:datastore-preferences:1.2.1")

    implementation("androidx.emoji2:emoji2-emojipicker:1.7.0")

    implementation("sh.calvin.reorderable:reorderable:3.1.0")

    implementation("com.google.protobuf:protobuf-javalite:4.36.2")

    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.5.2")

    val navigationVersion = "1.2.0"
    implementation("androidx.navigation3:navigation3-runtime:$navigationVersion")
    implementation("androidx.navigation3:navigation3-ui:$navigationVersion")

    implementation("commons-codec:commons-codec:1.22.1")

    implementation("com.google.zxing:core:3.5.4")

    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("io.insert-koin:koin-androidx-compose:4.2.2")

    val accompanistVersion = "0.37.3"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    testImplementation(composeBom)
    testImplementation("androidx.compose.ui:ui-test-junit4")
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.75.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.75.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose-preview-scanner-support:1.75.0")
    testImplementation("io.github.sergio-sastre.ComposablePreviewScanner:android:0.9.3")
    testImplementation("io.github.darkxanter:webp-imageio:0.3.3")
    testImplementation("androidx.test.ext:junit:1.3.0")
}

// The more screenshots I add the hungrier it gets
tasks.withType<Test>().configureEach {
    maxHeapSize = "4g"
}
