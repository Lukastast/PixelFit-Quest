import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

// Release signing is read from Android/local.properties (gitignored) or the environment.
// Never commit pixelfit-upload.jks or passwords. See SIGNING.md.
val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

fun signingProp(name: String): String? {
    val envKeys = buildList {
        add(name)
        when (name) {
            "storeFile" -> addAll(listOf("STORE_FILE", "PIXELFIT_STORE_FILE", "RELEASE_STORE_FILE"))
            "storePassword" -> addAll(listOf("STORE_PASSWORD", "PIXELFIT_STORE_PASSWORD", "RELEASE_STORE_PASSWORD"))
            "keyAlias" -> addAll(listOf("KEY_ALIAS", "PIXELFIT_KEY_ALIAS", "RELEASE_KEY_ALIAS"))
            "keyPassword" -> addAll(listOf("KEY_PASSWORD", "PIXELFIT_KEY_PASSWORD", "RELEASE_KEY_PASSWORD"))
        }
    }
    for (key in envKeys) {
        System.getenv(key)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    }
    for (key in envKeys) {
        localProperties.getProperty(key)?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
    }
    return null
}

fun resolveStoreFile(path: String): File {
    val given = File(path)
    if (given.isAbsolute) return given
    val candidates = listOf(
        file(path),
        rootProject.file(path),
        rootProject.file("../$path"),
    )
    return candidates.firstOrNull { it.exists() } ?: file(path)
}

val releaseStoreFile = signingProp("storeFile")
val releaseStorePassword = signingProp("storePassword")
val releaseKeyAlias = signingProp("keyAlias")
val releaseKeyPassword = signingProp("keyPassword")
val releaseSigningValues = listOf(
    "storeFile" to releaseStoreFile,
    "storePassword" to releaseStorePassword,
    "keyAlias" to releaseKeyAlias,
    "keyPassword" to releaseKeyPassword,
)
val presentSigningKeys = releaseSigningValues.filter { !it.second.isNullOrBlank() }.map { it.first }
val missingSigningKeys = releaseSigningValues.filter { it.second.isNullOrBlank() }.map { it.first }
if (presentSigningKeys.isNotEmpty() && missingSigningKeys.isNotEmpty()) {
    throw GradleException(
        "Incomplete release signing. Have: ${presentSigningKeys.joinToString()}. " +
            "Missing: ${missingSigningKeys.joinToString()}. " +
            "Set storeFile, storePassword, keyAlias, and keyPassword in local.properties or the environment. See SIGNING.md."
    )
}
val hasReleaseSigning = missingSigningKeys.isEmpty()

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

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                val keystore = resolveStoreFile(releaseStoreFile!!)
                if (!keystore.exists()) {
                    throw GradleException(
                        "Release keystore not found at '$releaseStoreFile' " +
                            "(resolved to ${keystore.absolutePath}). See SIGNING.md."
                    )
                }
                storeFile = keystore
                storePassword = releaseStorePassword!!
                keyAlias = releaseKeyAlias!!
                keyPassword = releaseKeyPassword!!
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
        buildConfig = true
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
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.hilt.common)
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.room.compiler)
    ksp(libs.kotlin.metadata.jvm)

    // --- Other Utilities ---
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.gson)
    implementation(libs.androidx.health.connect.client)

    // --- Debug dependencies ---
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}