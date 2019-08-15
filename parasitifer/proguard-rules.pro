# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 1
-dontusemixedcaseclassnames
-keepattributes SourceFile,LineNumberTable,Exceptions,*Annotation*
-dontpreverify
-verbose


-keep public class * extends android.app.Activity

-keep public class * extends android.app.Application

-keep public class * extends android.app.Service

-keep public class * extends android.content.BroadcastReceiver

-keep public class * extends android.content.ContentProvider

-keep public class com.android.vending.licensing.ILicensingService

-keep class * extends java.lang.annotation.Annotation { *; }

-keepclassmembers class * {
    @com.chouyu.pluginframework.parasitifer.BySDKLibInvoke <methods>;
}

-keep interface com.chouyu.pluginframework.parasitifer.OnPluginLoadCallback {
    public <methods>;
}

-keep interface com.chouyu.pluginframework.parasitifer.OnPluginDownloadCallback {
    public <methods>;
}

-keep class * extends android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}

-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}