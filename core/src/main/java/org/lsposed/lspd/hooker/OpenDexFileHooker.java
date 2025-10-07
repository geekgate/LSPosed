package org.lsposed.lspd.hooker;

import android.os.Build;

import androidx.annotation.NonNull;

import org.lsposed.lspd.impl.LSPosedBridge;
import org.lsposed.lspd.nativebridge.HookBridge;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;

public class OpenDexFileHooker implements XposedInterface.PostInjector {

    @Override
    public void inject(@NonNull XposedInterface.AfterHookContext context, Object returnValue, Throwable throwable) {
        ClassLoader classLoader = null;
        for (var arg : context.getArgs()) {
            if (arg instanceof ClassLoader) {
                classLoader = (ClassLoader) arg;
            }
        }
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P && classLoader == null) {
            classLoader = LSPosedBridge.class.getClassLoader();
        }
        while (classLoader != null) {
            if (classLoader == LSPosedBridge.class.getClassLoader()) {
                HookBridge.setTrusted(context.getResult());
                return;
            } else {
                classLoader = classLoader.getParent();
            }
        }
    }
}
