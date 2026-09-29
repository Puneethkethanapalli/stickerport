# Stickerport release keep rules.
#
# AGP 9 only accepts `proguard-android-optimize.txt` as the default proguard file, and R8 runs in
# full mode with strict keep-rule handling (`-keep class A` no longer implies `-keep class A { <init>(); }`).
# Rules are added here per-sprint as reflection-based code lands; see T14.2 for the R8 smoke test.

# kotlinx.serialization keeps its generated serializers reachable through companion objects.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Keep line numbers for readable crash reports, but hide the original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
