package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import androidx.annotation.NonNull;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.Post;

public class AttachHooker implements Post.Default {

    public void inject(@NonNull Context ctx, Object returnValue, Throwable throwable) {
        // Utils.logI("[Injected] AttachHooker::afterHookedMethod");
        XposedInit.loadModules((ActivityThread) ctx.getThisObject());
    }
}
