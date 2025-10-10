package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import org.lsposed.lspd.util.Utils;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.Injector;
import io.github.libxposed.api.XposedInterface;

public class AttachHooker implements Injector.PostInjector {

    public void inject(XposedInterface.AfterHookCallback callback, Object returnValue, Throwable throwable) {
        Utils.logI("[Injected] AttachHooker::afterHookedMethod");
        XposedInit.loadModules((ActivityThread) callback.getThisObject());
    }
}
