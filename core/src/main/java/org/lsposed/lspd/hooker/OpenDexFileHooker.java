package org.lsposed.lspd.hooker;

import android.os.Build;

import org.lsposed.lspd.Tag;
import org.lsposed.lspd.impl.LSPosedBridge;
import org.lsposed.lspd.nativebridge.HookBridge;
import org.lsposed.lspd.util.Utils;

import io.github.libxposed.api.XposedInterface;

@Tag("OpenDexFileHooker")
public class OpenDexFileHooker implements XposedInterface.Hooker {

    public static void afterHookedMethod(XposedInterface.AfterHookCallback callback) {
        Utils.logI("[Injected] OpenDexFileHooker::afterHookedMethod");
        ClassLoader classLoader = null;
        for (var arg : callback.getArgs()) {
            if (arg instanceof ClassLoader) {
                classLoader = (ClassLoader) arg;
            }
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P && classLoader == null) {
            classLoader = LSPosedBridge.class.getClassLoader();
        }
        while (classLoader != null) {
            if (classLoader == LSPosedBridge.class.getClassLoader()) {
                HookBridge.setTrusted(callback.getResult());
                return;
            } else {
                classLoader = classLoader.getParent();
            }
        }
    }
}
