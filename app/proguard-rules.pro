# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/justin/Android/Dev/android-sdk-macosx/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Butter Knife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

# Icepick
-dontwarn icepick.**
-keep class **$$Icepick { *; }
-keepnames class * { @icepick.State *; }
-keepclasseswithmembernames class * {
    @icepick.* <fields>;
}

# Timber (may only be needed for release builds logging to a third party like Crashlytics)
-keep public class timber.log.**

# LeakCanary 1.4-SNAPSHOT
-dontwarn com.squareup.leakcanary.DisplayLeakService
-dontwarn com.squareup.haha.**
