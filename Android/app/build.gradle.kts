plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.pixelfitquest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pixelfitquest"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.pixelfitquest.HiltTestRunner"
        proguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
    buildToolsVersion = "36.0.0"
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // --- Compose BOM ---
    // This dictates the versions for all androidx.compose libraries below it
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)

    // --- Navigation & UI ---
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    // --- Firebase ---
    implementation(libs.firebase.auth)
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-firestore")

    // --- Notifications ---
    implementation("androidx.work:work-runtime-ktx:2.11.1")

    // --- Google ---
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.tagmanager.v4.impl)

    // --- Hilt & Room ---
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation(libs.hilt.android)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.hilt.common)
    ksp(libs.hilt.android.compiler)
    ksp(libs.kotlin.metadata.jvm)

    // --- Other Utilities ---
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.gson)
    implementation(files("libs/samsung-health.aar"))

    // --- Debug dependencies ---
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}