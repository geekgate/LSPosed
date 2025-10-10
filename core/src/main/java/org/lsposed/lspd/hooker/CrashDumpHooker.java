package org.lsposed.lspd.hooker;

import android.util.Log;

import org.lsposed.lspd.Tag;
import org.lsposed.lspd.impl.LSPosedBridge;
import org.lsposed.lspd.util.Utils;

import io.github.libxposed.api.XposedInterface;

@Tag("CrashDumpHooker")
public class CrashDumpHooker implements XposedInterface.Hooker {

    public static void beforeHookedMethod(XposedInterface.BeforeHookCallback callback) {
        Utils.logI("[Injected] CrashDumpHooker::beforeHookedMethod");
        try {
            var e = (Throwable) callback.getArgs()[0];
            LSPosedBridge.log("Crash unexpectedly: " + Log.getStackTraceString(e));
        } catch (Throwable ignored) {
        }
    }
}
