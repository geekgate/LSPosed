package org.lsposed.lspd.hooker;

import android.util.Log;

import androidx.annotation.NonNull;

import org.lsposed.lspd.impl.LSPosedBridge;

import io.github.libxposed.api.Pre;

public class CrashDumpHooker implements Pre {

    public void inject(@NonNull Context ctx, @NonNull Object[] args) {
        // Utils.logI("[Injected] CrashDumpHooker::beforeHookedMethod");
        try {
            var e = (Throwable) args[0];
            LSPosedBridge.log("Crash unexpectedly: " + Log.getStackTraceString(e));
        } catch (Throwable ignored) {
        }
    }
}
