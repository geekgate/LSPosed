package org.lsposed.lspd.impl;

import android.util.Log;
import androidx.annotation.NonNull;

import io.github.libxposed.api.XposedInterface;

public class Logger implements XposedInterface.Logger {

    private void write(String tag, @NonNull Object[] msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for (Object arg : msg) {
            sb.append(arg);
        }
        Log.i(tag, sb.toString());
    }

    @Override
    public void i(Object... args) {
        write("[I]", args);
    }

    @Override
    public void w(Object... args) {
        write("[W]", args);
    }

    @Override
    public void e(Object... args) {
        write("[E]", args);
    }

    @Override
    public void e(String message, @NonNull Throwable t) {
        write("[E]", new Object[] {message, t.getMessage()});
    }

    @Override
    public void d(Object... args) {
        write("[D]", args);
    }

    @Override
    public void v(Object... args) {
        write("[V]", args);
    }

    @Override
    public void z(Object... args) {
        if (args == null || args.length == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg);
        }
        Log.i("", sb.toString());
    }
}
