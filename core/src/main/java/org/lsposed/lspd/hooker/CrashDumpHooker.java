package org.lsposed.lspd.hooker;

import android.util.Log;
import androidx.annotation.NonNull;
import java.lang.reflect.Executable;

import org.lsposed.lspd.impl.LSPosedBridge;
import io.github.libxposed.api.XposedInterface;

public class CrashDumpHooker implements XposedInterface.PreInjector {
    @Override
    public void inject(@NonNull XposedInterface.BeforeHookContext context, Object[] args){
        try {
            Throwable e = (Throwable) args[0];
            LSPosedBridge.log("Crash unexpectedly: " + Log.getStackTraceString(e));
        } catch (Throwable ignored) {
        }
    }
}
