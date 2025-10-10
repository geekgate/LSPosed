package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import org.lsposed.lspd.Tag;
import org.lsposed.lspd.util.Utils;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.XposedInterface;

@Tag("AttachHooker")
public class AttachHooker implements XposedInterface.Hooker {

    public static void afterHookedMethod(XposedInterface.AfterHookCallback callback) {
        Utils.logI("[Injected] AttachHooker::afterHookedMethod");
        XposedInit.loadModules((ActivityThread) callback.getThisObject());
    }
}
