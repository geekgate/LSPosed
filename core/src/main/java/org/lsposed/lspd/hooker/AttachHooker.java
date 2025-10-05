package org.lsposed.lspd.hooker;

import android.app.ActivityThread;

import androidx.annotation.NonNull;

import java.lang.reflect.Executable;

import de.robv.android.xposed.XposedInit;
import io.github.libxposed.api.XposedInterface;
public class AttachHooker<T extends Executable> implements XposedInterface.Hooker<T> {
    @Override
    public void before(@NonNull XposedInterface.BeforeHookCallback<T> callback){

    }
    @Override
    public void after(@NonNull XposedInterface.AfterHookCallback<T> callback) {
        XposedInit.loadModules((ActivityThread) callback.getThis());
    }
}
