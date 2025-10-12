package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Member;

import io.github.libxposed.api.Post;
import io.github.libxposed.api.Pre;

public class LSPosedHookContext implements Pre.Context, Post.Context {

    public Member method;

    public Object thisObject;

    public Object[] args;

    public Object result;

    public Throwable throwable;

    public boolean isSkipped;

    public LSPosedHookContext() {
    }

    // Both before and after

    @NonNull
    @Override
    public Member getMember() {
        return this.method;
    }

    @Nullable
    @Override
    public Object getThisObject() {
        return this.thisObject;
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
}
