plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.xinto.mauth"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.xinto.mauth"
        minSdk = 21
        targetSdk = 33
        versionCode = 51
        versionName = "0.5.1"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs +
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi" +
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi" +
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api" +
            "-opt-in=com.google.accompanist.permissions.ExperimentalPermissionsApi"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }

    packagingOptions {
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
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0")
    implementation("androidx.activity:activity-compose:1.6.1")

//    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
//    implementation(composeBom)
    implementation("androidx.compose.foundation:foundation:1.4.0-rc01")
    implementation("androidx.compose.material:material-icons-extended:1.4.0-rc01")
    implementation("androidx.compose.material3:material3:1.1.0-alpha08")
//    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.4.0-rc01")
    debugImplementation("androidx.compose.ui:ui-tooling:1.4.0-rc01")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.4.0-rc01")

    val cameraxVersion = "1.2.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")

    val roomVersion = "2.5.0"
    implementation("androidx.room:room-common:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("com.github.X1nto.taxi:taxi:1.3.0")

    implementation("com.holix.android:bottomsheetdialog-compose:1.0.1")

    implementation("commons-codec:commons-codec:1.15")

    implementation("com.google.zxing:core:3.5.0")

    implementation("io.coil-kt:coil-compose:2.2.2")

    implementation("io.insert-koin:koin-androidx-compose:3.4.1")

    val accompanistVersion = "0.29.2-rc"
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanistVersion")
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}