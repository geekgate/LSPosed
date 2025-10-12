package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.Post;

public class AttachHooker implements Post {

    public void inject(Context ctx, Object returnValue, Throwable throwable) {
        // Utils.logI("[Injected] AttachHooker::afterHookedMethod");
        XposedInit.loadModules((ActivityThread) ctx.getThisObject());
    }
}
