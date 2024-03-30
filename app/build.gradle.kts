plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.xinto.mauth"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.xinto.mauth"
        minSdk = 21
        targetSdk = 34
        versionCode = 80
        versionName = "0.8.0"

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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = freeCompilerArgs +
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi" +
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi" +
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api" +
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        applicationVariants.all {
            getByName(name) {
                kotlin.srcDir("build/generated/ksp/$name/kotlin")
            }
        }
    }

    lint {
        disable += "MissingTranslation"
        disable += "ExtraTranslation"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    val composeBom = platform("androidx.compose:compose-bom:2024.03.00")
    implementation(composeBom)
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    val cameraxVersion = "1.3.2"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    val roomVersion = "2.6.1"
    implementation("androidx.room:room-common:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("dev.olshevski.navigation:reimagined:1.5.0")

    implementation("commons-codec:commons-codec:1.15")

    implementation("com.google.zxing:core:3.5.0")

    implementation("io.coil-kt:coil-compose:2.4.0")

    implementation("io.insert-koin:koin-androidx-compose:3.4.5")

    val accompanistVersion = "0.32.0"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}