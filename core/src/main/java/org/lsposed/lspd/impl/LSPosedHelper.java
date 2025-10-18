package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.Hook;
import io.github.libxposed.api.Injector.Handler;
import io.github.libxposed.api.Post;
import io.github.libxposed.api.Pre;
import io.github.libxposed.api.errors.HookFailedError;

@SuppressWarnings("unused")
public class LSPosedHelper {

    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static Handler<Method> hookMethod(Pre injector, @NonNull Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static Handler<Method> hookMethod(Post injector, @NonNull Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static Handler<Method> hookMethod(Hook injector, @NonNull Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            var method = clazz.getDeclaredMethod(methodName, parameterTypes);
            return LSPosedBridge.hook(method, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static Set<Handler<Method>> hookAllMethods(@NonNull Class<?> clazz, String methodName, Pre injector) {
        var unhooks = new HashSet<Handler<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, injector));
            }
        }
        return unhooks;
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static Set<Handler<Method>> hookAllMethods(@NonNull Class<?> clazz, String methodName, Post injector) {
        var unhooks = new HashSet<Handler<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, injector));
            }
        }
        return unhooks;
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static Set<Handler<Method>> hookAllMethods(@NonNull Class<?> clazz, String methodName, Hook injector) {
        var unhooks = new HashSet<Handler<Method>>();
        for (var method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                unhooks.add(LSPosedBridge.hook(method, injector));
            }
        }
        return unhooks;
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static <T> Handler<Constructor<T>> hookConstructor(Pre injector, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static <T> Handler<Constructor<T>> hookConstructor(Post injector, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
    @NonNull @SuppressWarnings("UnusedReturnValue")
    public static <T> Handler<Constructor<T>> hookConstructor(Hook injector, @NonNull Class<T> clazz, Class<?>... parameterTypes) {
        try {
            var constructor = clazz.getDeclaredConstructor(parameterTypes);
            return LSPosedBridge.hook(constructor, injector);
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }
    }
}
