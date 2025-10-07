package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.util.ConcurrentModificationException;

import io.github.libxposed.api.XposedInterface;

public class LSPosedHookContext implements XposedInterface.BeforeHookContext, XposedInterface.AfterHookContext {

    public Executable origin;
    public Object thisObject;
    public Object[] args;
    public Object result;
    public Throwable throwable;
    public boolean isSkipped;
    public volatile ClassLoader classLoader;
    public final XposedInterface.Logger logger = new Logger();

    public LSPosedHookContext() {}

    // Both before and after

    @NonNull
    @Override
    public Executable getOrigin() {
        return origin;
    }

    @Nullable
    @Override
    public Object getThis() {
        return thisObject;
    }

    @NonNull
    @Override
    public Object[] getArgs() {
        return this.args;
    }

    // Before

    @Override
    public void returnAndSkip(@Nullable Object result) {
        this.result = result;
        this.throwable = null;
        this.isSkipped = true;
    }

    @Override
    public void throwAndSkip(@Nullable Throwable throwable) {
        this.result = null;
        this.throwable = throwable;
        this.isSkipped = true;
    }

    @Nullable
    @Override
    public Object invokeOrigin() throws InvocationTargetException, IllegalAccessException {
        return null;
    }

    @Override
    public Class<?> loadClass(@NonNull String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

    @Override
    public XposedInterface.Logger getLogger() {
        return logger;
    }

    @Override
    public <U> void setExtra(@NonNull String key, @Nullable U value) throws ConcurrentModificationException {

    }

    // After

    @Nullable
    @Override
    public Object getResult() {
        return this.result;
    }

    @Nullable
    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public boolean isSkipped() {
        return this.isSkipped;
    }

    @Override
    public void setResult(@Nullable Object result) {
        this.result = result;
        this.throwable = null;
    }

    @Override
    public void setThrowable(@Nullable Throwable throwable) {
        this.result = null;
        this.throwable = throwable;
    }

    @Nullable
    @Override
    public <U> U getExtra(@NonNull String key) {
        return null;
    }
}
