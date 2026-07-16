# ProGuard rules for Agent.AI

# Hilt & Dagger
-keep class * extends class dagger.internal.DoubleCheck
-keep class * extends class dagger.internal.InstanceFactory
-keep class * extends class dagger.internal.MapFactory
-keep class * extends class dagger.internal.ProviderOfLazy
-keep class * extends class dagger.internal.SetFactory
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

# Retrofit 2
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp 3
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @okhttp3.** *;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-dontwarn kotlinx.coroutines.**

# Keep our data models so Gson can parse them
-keep class com.aimobile.models.** { *; }
-keep class com.aimobile.api.** { *; }

# Keep accessibility service classes
-keep class com.aimobile.accessibility.** { *; }
-keep class com.aimobile.handlers.** { *; }
