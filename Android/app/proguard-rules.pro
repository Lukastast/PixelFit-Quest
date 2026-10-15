# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep line numbers for crash traces (P1-6).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Credential Manager / Play Services Auth
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
*;
}

# --- P1-6: reflection / codegen keep rules ---

# Gson models (field names used as JSON keys; TypeToken / fromJson).
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeToken
-keep class com.pixelfitquest.firebase.model.** { *; }
-keep class com.pixelfitquest.feature.workout.model.** { *; }
-keep class com.pixelfitquest.feature.workoutBuilder.model.** { *; }
-keep class com.pixelfitquest.local.export.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Firestore / Firebase Auth (client SDKs ship consumer rules; belt-and-suspenders).
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Room entities / DB
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# Hilt / Dagger (libraries provide consumer rules; keep generated entry points).
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Health Connect client
-keep class androidx.health.connect.** { *; }
-dontwarn androidx.health.connect.**

# Kotlin metadata / coroutines used by reflection in some libs
-dontwarn kotlin.Metadata
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
