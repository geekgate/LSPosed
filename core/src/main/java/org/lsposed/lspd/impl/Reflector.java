package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Executable;
import java.util.ArrayList;

import io.github.libxposed.api.Injector;
import io.github.libxposed.api.XposedInterface;

public class Reflector {
    private final Executable[] members;
    private Reflector(Executable... members) {
        this.members = members;
    }
    public Reflector(@NonNull Class<?> cls, String method, Class<?> ... params) throws NoSuchMethodException {
        this(cls.getDeclaredMethod( method,  params ));
    }
    public Reflector(@NonNull Class<?> cls, Class<?> ... params) throws NoSuchMethodException {
        this(cls.getDeclaredConstructor( params ));
    }
    @NonNull
    @Contract("_, _ -> new")
    public static Reflector wildcard(@NonNull Class<?> cls, String method) throws NoSuchMethodException {
        var members = new ArrayList<Executable>();
        for (var m : cls.getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                members.add(m);
            }
        }
        return new Reflector(members.toArray(new Executable[0]));
    }
    public void inject(Injector.PreInjector injector, int priority) {
        for (var member : members) {
            LSPosedBridge.hook(member, priority, injector);
        }
    }
    public void inject(Injector.PostInjector injector, int priority) {
        for (var member : members) {
            LSPosedBridge.hook(member, priority, injector);
        }
    }
    public void inject(Injector.Hook injector, int priority) {
        for (var member : members) {
            LSPosedBridge.hook(member, priority, injector);
        }
    }
    public void inject(Injector.Hook injector) {
        for (var member : members) {
            LSPosedBridge.hook(member, XposedInterface.PRIORITY_DEFAULT, injector);
        }
    }
    public void inject(Injector.PreInjector injector) {
        for (var member : members) {
            LSPosedBridge.hook(member, XposedInterface.PRIORITY_DEFAULT, injector);
        }
    }
    public void inject(Injector.PostInjector injector) {
        for (var member : members) {
            LSPosedBridge.hook(member, XposedInterface.PRIORITY_DEFAULT, injector);
        }
    }
}
