package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.Post;
import io.github.libxposed.api.Pre;

public class AttachHooker implements Post.Default {

    public void inject(Context ctx, Object returnValue, Throwable throwable) {
        // Utils.logI("[Injected] AttachHooker::afterHookedMethod");
        XposedInit.loadModules((ActivityThread) ctx.getThisObject());
    }
}
