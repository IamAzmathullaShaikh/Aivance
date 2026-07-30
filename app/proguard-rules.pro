# Aivance ProGuard / R8 Rules
# ============================================================

# Keep data classes used with Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.bangersoul.aivance.**$$serializer { *; }
-keepclassmembers class com.bangersoul.aivance.** {
    *** Companion;
}
-keepclasseswithmembers class com.bangersoul.aivance.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Room entities and DAOs
-keep class com.bangersoul.aivance.core.database.model.** { *; }
-keep class com.bangersoul.aivance.core.database.dao.** { *; }

# Keep Hilt injected classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Retrofit interfaces
-keep,allowobfuscation interface com.bangersoul.aivance.**.api.** { *; }
-keep,allowobfuscation interface com.bangersoul.aivance.**.*Api { *; }

# Keep OkHttp and Retrofit
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep Timber
-keep class timber.log.Timber** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Remove logging in release
-assumenosideeffects class timber.log.Timber {
    public static void v(...);
    public static void d(...);
    public static void i(...);
    public static void w(...);
}

# Keep explicit rules from rules.keep
-keep @interface com.bangersoul.aivance.**.Keep { *; }

# Gson/Room type adapters
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
