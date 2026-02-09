# RainCast ProGuard Rules

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# Gson
-keepattributes *Annotation*
-keep class com.raincast.data.api.models.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**
