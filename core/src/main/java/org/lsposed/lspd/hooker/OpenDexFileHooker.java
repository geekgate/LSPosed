package org.lsposed.lspd.hooker;

import android.os.Build;

import androidx.annotation.NonNull;

import org.lsposed.lspd.impl.LSPosedBridge;
import org.lsposed.lspd.nativebridge.HookBridge;

import io.github.libxposed.api.Post;

public class OpenDexFileHooker implements Post {

    public void inject(@NonNull Context ctx, Object returnValue, Throwable throwable) {
        // Utils.logI("[Injected] OpenDexFileHooker::afterHookedMethod");
        ClassLoader classLoader = null;
        for (var arg : ctx.getArgs()) {
            if (arg instanceof ClassLoader) {
                classLoader = (ClassLoader) arg;
            }
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P && classLoader == null) {
            classLoader = LSPosedBridge.class.getClassLoader();
        }
        while (classLoader != null) {
            if (classLoader == LSPosedBridge.class.getClassLoader()) {
                HookBridge.setTrusted(ctx.getResult());
                return;
            } else {
                classLoader = classLoader.getParent();
            }
        }
    }
}
