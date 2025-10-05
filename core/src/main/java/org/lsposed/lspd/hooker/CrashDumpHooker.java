package org.lsposed.lspd.hooker;

import android.util.Log;
import androidx.annotation.NonNull;
import java.lang.reflect.Executable;

import org.lsposed.lspd.impl.LSPosedBridge;
import io.github.libxposed.api.XposedInterface;

public class CrashDumpHooker<T extends Executable> implements XposedInterface.Hooker<T> {
    @Override
    public void before(@NonNull XposedInterface.BeforeHookCallback<T> callback){
        try {
            Throwable e = callback.getArg(0);
            LSPosedBridge.log("Crash unexpectedly: " + Log.getStackTraceString(e));
        } catch (Throwable ignored) {
        }
    }
    @Override
    public void after(@NonNull XposedInterface.AfterHookCallback<T> callback) {
    }
}
