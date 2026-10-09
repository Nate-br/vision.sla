# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in build.gradle.kts.

# ─── Kotlin / Coroutines ──────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ─── OkHttp / Retrofit ────────────────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ─── kotlinx.serialization ────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class et.vbl.**$$serializer { *; }
-keepclassmembers class et.vbl.** {
    *** Companion;
}
-keepclasseswithmembers class et.vbl.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ─── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }

# ─── Firebase ─────────────────────────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ─── Room ─────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ─── SQLCipher ────────────────────────────────────────────────────────────────
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**

# ─── Tink ─────────────────────────────────────────────────────────────────────
-keep class com.google.crypto.tink.** { *; }

# ─── Play Integrity ────────────────────────────────────────────────────────────
-keep class com.google.android.play.core.integrity.** { *; }

# ─── Security: NEVER keep BuildConfig in release (no secrets exposure) ─────────
# R8 full mode removes debug fields automatically — this is a reminder.
-dontskipnonpubliclibraryclassmembers
