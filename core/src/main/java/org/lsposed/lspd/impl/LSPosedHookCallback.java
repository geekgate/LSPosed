package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.util.ConcurrentModificationException;

import io.github.libxposed.api.XposedInterface;

public class LSPosedHookCallback<T extends Executable> implements XposedInterface.BeforeHookCallback<T>, XposedInterface.AfterHookCallback<T> {

    public Member method;

    public Object thisObject;

    public Object[] args;

    public Object result;

    public Throwable throwable;

    public boolean isSkipped;

    public LSPosedHookCallback() {
    }

    // Both before and after


    @NonNull
    @Override
    public T getOrigin() {
        return null;
    }

    @Nullable
    @Override
    public Object getThis() {
        return null;
    }

    @NonNull
    @Override
    public Object[] getArgs() {
        return this.args;
    }

    @Nullable
    @Override
    public <U> U getArg(int index) {
        return null;
    }

    @Override
    public <U> void setArg(int index, U value) {

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
