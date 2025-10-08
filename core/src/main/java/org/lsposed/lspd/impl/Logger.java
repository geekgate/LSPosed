package org.lsposed.lspd.impl;

import android.util.Log;
import androidx.annotation.NonNull;

import io.github.libxposed.api.XposedInterface;

public class Logger implements XposedInterface.Logger {

    private final String tag;

    public Logger() {
        this("");
    }

    public Logger(String tag) {
        this.tag = tag;
    }

    @NonNull
    private String join(Object... msg) {
        if (msg == null || msg.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        try {
            for (Object arg : msg) {
                sb.append(arg);
            }
        } catch (Exception e) {
            sb.append("[Exception] Logger exception: ");
            sb.append(e);
        }
        return sb.toString();
    }

    @Override
    public void i(Object... args) {
        Log.i(tag, join(args));
    }

    @Override
    public void w(Object... args) {
        Log.w(tag, join(args));
    }

    @Override
    public void e(Object... args) {
        Log.e(tag, join(args));
    }

    @Override
    public void e(String message, @NonNull Throwable t) {
        Log.e(tag, join(message, t.getMessage()));
    }

    @Override
    public void d(Object... args) {
        Log.d(tag, join(args));
    }

    @Override
    public void v(Object... args) {
        Log.v(tag, join(args));
    }

    @Override
    public void z(Object... args) {
        Log.i("", join( args ));
    }
}
