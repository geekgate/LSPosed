package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Executable;

import de.robv.android.xposed.XC_MethodHook;
import io.github.libxposed.api.Post;
import io.github.libxposed.api.Pre;

public class LSPosedHookContext implements Pre.Context, Post.Context {

    public Executable target;
    public Object thisObject;
    public Object[] args;
    public boolean isSkipped;
    public Object result;
    public Throwable throwable;

    public LSPosedHookContext() {}
    public LSPosedHookContext(XC_MethodHook.MethodHookParam<?> param) {
        this.target = (Executable) param.method;
        this.thisObject = param.thisObject;
        this.args = param.args;
        this.result = param.result;
        this.throwable = param.throwable;
        this.isSkipped = param.returnEarly;
    }

    /**
     * Update the context with the given param.
     *
     * @param param The param to update from.
     */
    public <T extends Executable> void update(@NonNull XC_MethodHook.MethodHookParam<T> param) {
        this.args = param.args;
        this.result = param.result;
        this.throwable = param.throwable;
        this.isSkipped = param.returnEarly;
    }

    /**
     * Update the given param with the context.
     *
     * @param param The param to update.
     */
    public <T extends Executable> void sync(@NonNull XC_MethodHook.MethodHookParam<T> param) {
        param.args = this.args;
        param.result = this.result;
        param.throwable = this.throwable;
        param.returnEarly = this.isSkipped;
    }

    @NonNull
    @Contract(" -> _")
    public <T extends Executable> XC_MethodHook.MethodHookParam<T> export() {
        XC_MethodHook.MethodHookParam<T> param = new XC_MethodHook.MethodHookParam<>();

        param.method = this.target;
        param.thisObject = this.thisObject;
        param.args = this.args;
        param.result = this.result;
        param.throwable = this.throwable;
        param.returnEarly = this.isSkipped;

        return param;
    }

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

    @NonNull
    @Override
    public Object[] getArgs() {
        return this.args;
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

    @NonNull
    @Override
    public Executable getTarget() {
        return this.target;
    }

    @Nullable
    @Override
    public Object getThisObject() {
        return this.thisObject;
    }

}
