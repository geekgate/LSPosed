package org.lsposed.lspd.impl;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Executable;
import java.util.ArrayList;

import io.github.libxposed.api.Hook;
import io.github.libxposed.api.Injector;
import io.github.libxposed.api.Post;
import io.github.libxposed.api.Pre;

public final class Reflector {
    public static final String CONSTRUCTOR = "<init>";

    private final Executable[] members;

    private Reflector(Executable... members) {
        this.members = members;
    }

    /**
     * Create a Reflector for a method.
     * @param cls Class to reflect
     * @param method  Name of method to match
     * @param params  Parameters to match method
     * @throws NoSuchMethodException if no suitable method was found.
     */
    public Reflector(@NonNull Class<?> cls, String method, Class<?> ... params) throws NoSuchMethodException {
        this( isConstructor( method ) ? cls.getDeclaredConstructor( params ) : cls.getDeclaredMethod( method,  params ));
    }

    /**
     * Create a Reflector for a constructor.
     * @param cls Class to reflect
     * @param params  Parameters to match constructor
     * @throws NoSuchMethodException if no suitable constructor was found.
     */
    @NonNull
    @Contract("_, _ -> new")
    public static Reflector constructor(@NonNull Class<?> cls, Class<?> ... params) throws NoSuchMethodException {
        return new Reflector(cls.getDeclaredConstructor( params ));
    }

    @NonNull
    @Contract("_, _ -> new")
    public static Reflector wildcard(@NonNull Class<?> cls, String method) throws NoSuchMethodException {
        var members = new ArrayList<Executable>();
        if (isConstructor( method )) {
            members.add(cls.getDeclaredConstructor());
        } else for (var m : cls.getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                members.add(m);
            }
        }
        return new Reflector(members.toArray(new Executable[0]));
    }
    public <C extends Pre.Context> void inject(Pre<C> injector, int priority) {
        for (var member : members) {
            LSPosedBridge.hook(member, priority, injector);
        }
    }
    public <C extends Post.Context> void inject(Post<C> injector, int priority) {
        for (var member : members) {
            LSPosedBridge.hook(member, priority, injector);
        }
    }
    public <C extends Pre.Context, D extends Post.Context> void inject(Hook<C, D> injector, int priority) {
        for (var member : members) {
            LSPosedBridge.hook(member, priority, injector);
        }
    }
    public <C extends Pre.Context, D extends Post.Context> void inject(Hook<C, D> injector) {
        for (var member : members) {
            LSPosedBridge.hook(member, Injector.PRIORITY_DEFAULT, injector);
        }
    }
    public <C extends Pre.Context> void inject(Pre<C> injector) {
        for (var member : members) {
            LSPosedBridge.hook(member, Injector.PRIORITY_DEFAULT, injector);
        }
    }
    public <C extends Post.Context> void inject(Post<C> injector) {
        for (var member : members) {
            LSPosedBridge.hook(member, Injector.PRIORITY_DEFAULT, injector);
        }
    }

    /**
     * Check if the given method name is a constructor.
     * @param name  Name of method to check
     * @return true if the method name is a constructor
     */
    public static boolean isConstructor(String name) {
        return name == null || name.isEmpty() || CONSTRUCTOR.equals(name);
    }
}
