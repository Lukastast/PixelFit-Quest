import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("dagger.hilt.android.plugin")
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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }
    buildToolsVersion = "36.0.0"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true

            // Fix JaCoCo + Robolectric conflict
            all {
                it.extensions.configure(JacocoTaskExtension::class.java) {
                    isIncludeNoLocationClasses = true
                    excludes = listOf("jdk.internal.*")
                }

                // Add JVM args to handle the conflict
                it.jvmArgs(
                    "-noverify",
                    "--add-opens=java.base/java.lang=ALL-UNNAMED",
                    "--add-opens=java.base/java.util=ALL-UNNAMED"
                )

                // Set max heap size if needed
                it.maxHeapSize = "2048m"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation:1.9.4")
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    //firebase
    implementation(libs.firebase.auth)
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-firestore")

    //notifications
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    //google
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    //hilt
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("com.google.dagger:hilt-android:2.57.2")
    implementation(libs.androidx.room.ktx)
    implementation(libs.play.services.tagmanager.v4.impl)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.ui.test.junit4)
    implementation(libs.androidx.compose.foundation)
    kapt("com.google.dagger:hilt-compiler:2.57.2")
    kapt("com.google.dagger:hilt-android-compiler:2.57.2")

    //json passing
    implementation(libs.gson)

    // Samsung Health SDK
    implementation(files("libs/samsung-health.aar"))

    // Unit Tests (JVM)
    testImplementation(libs.junit)
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation(kotlin("test"))
    testImplementation("org.robolectric:robolectric:4.14")
    testImplementation("app.cash.turbine:turbine:1.1.0")

    // Hilt testing for unit tests
    testImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    kaptTest("com.google.dagger:hilt-android-compiler:2.57.2")

    // Android Instrumented Tests (Device/Emulator)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Hilt testing for instrumented tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.57.2")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.57.2")

    // Debug dependencies
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}