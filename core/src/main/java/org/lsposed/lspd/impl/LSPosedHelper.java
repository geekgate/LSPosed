package org.lsposed.lspd.impl;

import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.errors.HookFailedError;

public class LSPosedHelper {

    @SuppressWarnings("UnusedReturnValue")
    public static XposedInterface.Unhooker
    hookMethod(XposedInterface.Injector injector, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Set<XposedInterface.Unhooker>
    hookAllMethods(XposedInterface.Injector injector, Class<?> clazz, String methodName) {
        var unhooks = new HashSet<XposedInterface.Unhooker>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector));
            }
        }
        return unhooks;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static XposedInterface.Unhooker
    hookConstructor(XposedInterface.Injector injector, Class<?> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
}
