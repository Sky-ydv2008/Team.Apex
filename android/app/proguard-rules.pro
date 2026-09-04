# Proguard rules for Apex Innovators Android app
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.apexinnovators.app.** { *; }
