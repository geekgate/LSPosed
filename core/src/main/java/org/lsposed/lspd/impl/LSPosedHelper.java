package org.lsposed.lspd.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.errors.HookFailedError;

public class LSPosedHelper {

    @SuppressWarnings("UnusedReturnValue")
    public static XposedInterface.MethodUnhooker<XposedInterface.Hooker<Method>, Method>
    hookMethod(XposedInterface.Hooker<Method> hooker, Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.doHook(method, XposedInterface.PRIORITY_DEFAULT, hooker);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static Set<XposedInterface.MethodUnhooker<XposedInterface.Hooker<Method>, Method>>
    hookAllMethods(XposedInterface.Hooker<Method> hooker, Class<?> clazz, String methodName) {
        var unhooks = new HashSet<XposedInterface.MethodUnhooker<XposedInterface.Hooker<Method>, Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.doHook(method, XposedInterface.PRIORITY_DEFAULT, hooker));
            }
        }
        return unhooks;
    }

    @SuppressWarnings("UnusedReturnValue")
    public static XposedInterface.MethodUnhooker<XposedInterface.Hooker<Constructor<?>>, Constructor<?>>
    hookConstructor(XposedInterface.Hooker<Constructor<?>> hooker, Class<?> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.doHook(constructor, XposedInterface.PRIORITY_DEFAULT, hooker);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
}
