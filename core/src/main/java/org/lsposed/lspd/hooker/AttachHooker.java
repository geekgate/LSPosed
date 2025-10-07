package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import androidx.annotation.NonNull;

import java.lang.reflect.Executable;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.XposedInterface;
public class AttachHooker implements XposedInterface.PostInjector {
    @Override
    public void inject(@NonNull XposedInterface.AfterHookContext context, Object returnValue, Throwable throwable) {
        XposedInit.loadModules((ActivityThread) context.getThis());
    }
}
