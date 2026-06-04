# YUK 24 ProGuard rules

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class uz.yuk24.app.**$$serializer { *; }
-keepclassmembers class uz.yuk24.app.** {
    *** Companion;
}
-keepclasseswithmembers class uz.yuk24.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# OSMDroid
-dontwarn org.osmdroid.**
-keep class org.osmdroid.** { *; }

# Hilt
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
