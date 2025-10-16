-keep class de.robv.android.xposed.** {*;}
-keep class io.github.libxposed.** {*;}
# -keep class org.lsposed.lspd.** {*;}
-keepattributes RuntimeVisibleAnnotations
-keep class android.** { *; }
-keepclasseswithmembers,includedescriptorclasses class * {
    native <methods>;
}
-keepclassmembers class org.lsposed.lspd.impl.LSPosedContext {
    public <methods>;
}
-keepclassmembers class org.lsposed.lspd.impl.LSPosedHookContext {
    public <methods>;
}
-keepclassmembers class org.lsposed.lspd.impl.LSPosedBridge$NativeHooker {
    <init>(java.lang.reflect.Executable);
    callback(...);
}
-keepclassmembers class org.lsposed.lspd.impl.LSPosedBridge$NativeInjector {
    <init>(java.lang.reflect.Executable);
    callback(...);
}
-keepclassmembers class org.lsposed.lspd.impl.LSPosedBridge$HookerCallback {
    final *** beforeInvocation;
    final *** afterInvocation;
    HookerCallback(...);
}
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
}
-repackageclasses
-allowaccessmodification
-dontwarn org.slf4j.impl.StaticLoggerBinder
