# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.example.repattack.**$$serializer { *; }
-keepclassmembers class com.example.repattack.** { *** Companion; }
-keepclasseswithmembers class com.example.repattack.** { kotlinx.serialization.KSerializer serializer(...); }
