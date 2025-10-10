package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.Injector;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.errors.HookFailedError;

public class LSPosedHelper {

    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Method>
    hookMethod(Class<? extends XposedInterface.Hooker> hooker, @NonNull Class<T> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, hooker);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Method>
    hookMethod(Injector.PreInjector injector, @NonNull Class<T> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Method>
    hookMethod(Injector.PostInjector injector, @NonNull Class<T> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Method>
    hookMethod(Injector.Hook injector, @NonNull Class<T> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }

    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> Set<XposedInterface.MethodUnhooker<Method>>
    hookAllMethods(Class<? extends XposedInterface.Hooker> hooker, @NonNull Class<T> clazz, String methodName) {
        var unhooks = new HashSet<XposedInterface.MethodUnhooker<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, hooker));
            }
        }
        return unhooks;
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> Set<XposedInterface.MethodUnhooker<Method>>
    hookAllMethods(@NonNull Class<T> clazz, String methodName, Injector.PreInjector injector) {
        var unhooks = new HashSet<XposedInterface.MethodUnhooker<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector));
            }
        }
        return unhooks;
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> Set<XposedInterface.MethodUnhooker<Method>>
    hookAllMethods(@NonNull Class<T> clazz, String methodName, Injector.PostInjector injector) {
        var unhooks = new HashSet<XposedInterface.MethodUnhooker<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector));
            }
        }
        return unhooks;
    }

    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> Set<XposedInterface.MethodUnhooker<Method>>
    hookAllMethods(@NonNull Class<T> clazz, String methodName, Injector.Hook injector) {
        var unhooks = new HashSet<XposedInterface.MethodUnhooker<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, XposedInterface.PRIORITY_DEFAULT, injector));
            }
        }
        return unhooks;
    }

    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Constructor<T>>
    hookConstructor(Injector.PreInjector injector, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Constructor<T>>
    hookConstructor(Injector.PostInjector injector, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Constructor<T>>
    hookConstructor(Injector.Hook injector, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, XposedInterface.PRIORITY_DEFAULT, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull
    @SuppressWarnings("UnusedReturnValue")
    public static <T> XposedInterface.MethodUnhooker<Constructor<T>>
    hookConstructor(Class<? extends XposedInterface.Hooker> hooker, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, XposedInterface.PRIORITY_DEFAULT, hooker);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
}
