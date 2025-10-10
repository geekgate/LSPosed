package org.lsposed.lspd.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import org.lsposed.lspd.nativebridge.HookBridge;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import io.github.libxposed.api.Injector;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.errors.HookFailedError;

public class LSPosedBridge {

    private static final String TAG = "LSPosedPro-Bridge";

    private static final String castException = "Return value's type from hook callback does not match the hooked method";

    private static final Method getCause;

    static {
        Method tmp;
        try {
            tmp = InvocationTargetException.class.getMethod("getCause");
        } catch (Throwable e) {
            tmp = null;
        }
        getCause = tmp;
    }

    public static class HookerCallback {
        @NonNull
        final Method beforeInvocation;
        @NonNull
        final Method afterInvocation;

        final int beforeParams;
        final int afterParams;

        public HookerCallback(@NonNull Method beforeInvocation, @NonNull Method afterInvocation) {
            this.beforeInvocation = beforeInvocation;
            this.afterInvocation = afterInvocation;
            this.beforeParams = beforeInvocation.getParameterCount();
            this.afterParams = afterInvocation.getParameterCount();
        }
    }

    public static void log(String text) {
        Log.i(TAG, text);
    }

    public static void log(Throwable t) {
        String logStr = Log.getStackTraceString(t);
        Log.e(TAG, logStr);
    }

    public static class NativeHooker<T extends Executable> {
        private final Object params;

        private NativeHooker(Executable method) {
            var isStatic = Modifier.isStatic(method.getModifiers());
            Object returnType;
            if (method instanceof Method) {
                returnType = ((Method) method).getReturnType();
            } else {
                returnType = null;
            }
            params = new Object[]{
                    method,
                    returnType,
                    isStatic,
            };
        }

        // This method is quite critical. We should try not to use system methods to avoid
        // endless recursive
        public Object callback(Object[] args) throws Throwable {
            LSPosedHookCallback<T> callback = new LSPosedHookCallback<>();

            var array = ((Object[]) params);

            var method = (T) array[0];
            var returnType = (Class<?>) array[1];
            var isStatic = (Boolean) array[2];

            callback.method = method;

            if (isStatic) {
                callback.thisObject = null;
                callback.args = args;
            } else {
                callback.thisObject = args[0];
                callback.args = new Object[args.length - 1];
                //noinspection ManualArrayCopy
                for (int i = 0; i < args.length - 1; ++i) {
                    callback.args[i] = args[i + 1];
                }
            }

            Object[][] callbacksSnapshot = HookBridge.callbackSnapshot(HookerCallback.class, method);
            Object[] modernSnapshot = callbacksSnapshot[0];
            Object[] legacySnapshot = callbacksSnapshot[1];

            if (modernSnapshot.length == 0 && legacySnapshot.length == 0) {
                try {
                    return HookBridge.invokeOriginalMethod(method, callback.thisObject, callback.args);
                } catch (InvocationTargetException ite) {
                    throw (Throwable) HookBridge.invokeOriginalMethod(getCause, ite);
                }
            }

            Object[] ctxArray = new Object[modernSnapshot.length];
            XposedBridge.LegacyApiSupport<T> legacy = null;

            // call "before method" callbacks
            int beforeIdx;
            for (beforeIdx = 0; beforeIdx < modernSnapshot.length; beforeIdx++) {
                try {
                    var hooker = (HookerCallback) modernSnapshot[beforeIdx];
                    if (hooker.beforeParams == 0) {
                        ctxArray[beforeIdx] = hooker.beforeInvocation.invoke(null);
                    } else {
                        ctxArray[beforeIdx] = hooker.beforeInvocation.invoke(null, callback);
                    }
                } catch (Throwable t) {
                    LSPosedBridge.log(t);

                    // reset result (ignoring what the unexpectedly exiting callback did)
                    callback.setResult(null);
                    callback.isSkipped = false;
                    continue;
                }

                if (callback.isSkipped) {
                    // skip remaining "before" callbacks and corresponding "after" callbacks
                    beforeIdx++;
                    break;
                }
            }

            if (!callback.isSkipped && legacySnapshot.length != 0) {
                // TODO: Separate classloader
                legacy = new XposedBridge.LegacyApiSupport<>(callback, legacySnapshot);
                legacy.handleBefore();
            }

            // call original method if not requested otherwise
            if (!callback.isSkipped) {
                try {
                    var result = HookBridge.invokeOriginalMethod(method, callback.thisObject, callback.args);
                    callback.setResult(result);
                } catch (InvocationTargetException e) {
                    var throwable = (Throwable) HookBridge.invokeOriginalMethod(getCause, e);
                    callback.setThrowable(throwable);
                }
            }

            // call "after method" callbacks
            for (int afterIdx = beforeIdx - 1; afterIdx >= 0; afterIdx--) {
                Object lastResult = callback.getResult();
                Throwable lastThrowable = callback.getThrowable();
                var hooker = (HookerCallback) modernSnapshot[afterIdx];
                try {
                    if (hooker.afterParams == 0) {
                        hooker.afterInvocation.invoke(null);
                    } else if (hooker.afterParams == 1) {
                        hooker.afterInvocation.invoke(null, callback);
                    } else {
                        hooker.afterInvocation.invoke(null, callback, ctxArray[afterIdx]);
                    }
                } catch (Throwable t) {
                    LSPosedBridge.log(t);

                    // reset to last result (ignoring what the unexpectedly exiting callback did)
                    if (lastThrowable == null) {
                        callback.setResult(lastResult);
                    } else {
                        callback.setThrowable(lastThrowable);
                    }
                }
            }

            if (legacy != null) {
                legacy.handleAfter();
            }

            // return
            var t = callback.getThrowable();
            if (t != null) {
                throw t;
            } else {
                var result = callback.getResult();
                if (returnType != null && !returnType.isPrimitive() && !HookBridge.instanceOf(result, returnType)) {
                    throw new ClassCastException(castException);
                }
                return result;
            }
        }
    }

    public static void dummyCallback() {
    }

    private static boolean isBeforeInvocation(Method method) {
        var ps = method.getParameterTypes();
        return ps.length == 1 && ps[0]==XposedInterface.BeforeHookCallback.class;
    }

    private static boolean isAfterInvocation(Method method) {
        var ps = method.getParameterTypes();
        return ps.length > 0 && ps[0]==XposedInterface.AfterHookCallback.class;
    }

    @NonNull
    public static <T extends Executable> XposedInterface.MethodUnhooker<T>
    hook(T hookMethod, int priority, Class<? extends XposedInterface.Hooker> hooker) {
        if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod);
        } else if (hookMethod.getDeclaringClass().getClassLoader() == LSPosedContext.class.getClassLoader()) {
            throw new IllegalArgumentException("Do not allow hooking inner methods");
        } else if (hookMethod.getDeclaringClass() == Method.class && hookMethod.getName().equals("invoke")) {
            throw new IllegalArgumentException("Cannot hook Method.invoke");
        } else if (hooker == null) {
            throw new IllegalArgumentException("hooker should not be null!");
        }

        Method beforeInvocation = null, afterInvocation = null;

        for (var method : hooker.getDeclaredMethods()) {
            var m = method.getModifiers();

            if (Modifier.isPublic(m) && Modifier.isStatic(m)) {
                if (isBeforeInvocation( method )) {
                    if (beforeInvocation != null) {
                        throw new IllegalArgumentException("More than one method with before invocation");
                    }
                    beforeInvocation = method;
                    continue;
                }
                if (isAfterInvocation( method )) {
                    if (afterInvocation != null) {
                        throw new IllegalArgumentException("More than one method with after invocation");
                    }
                    afterInvocation = method;
                    continue;
                }
            }

            Log.i(TAG, method.toString());
        }

        if (beforeInvocation == null && afterInvocation == null) {
            throw new IllegalArgumentException("before/after invocation undefined: " + hooker.getCanonicalName() );
        }
        try {
            if (beforeInvocation == null) {
                beforeInvocation = LSPosedBridge.class.getMethod("dummyCallback");
            } else if (afterInvocation == null) {
                afterInvocation = LSPosedBridge.class.getMethod("dummyCallback");
            } else {
                var ret = beforeInvocation.getReturnType();
                var params = afterInvocation.getParameterTypes();
                if (ret != void.class && params.length == 2 && !ret.equals(params[1])) {
                    throw new IllegalArgumentException("BeforeInvocation and AfterInvocation method format is invalid");
                }
            }
        } catch (NoSuchMethodException e) {
            throw new HookFailedError(e);
        }

        var callback = new LSPosedBridge.HookerCallback(beforeInvocation, afterInvocation);
        if (HookBridge.hookMethod(true, hookMethod, LSPosedBridge.NativeHooker.class, priority, callback)) {
            return new XposedInterface.MethodUnhooker<>() {
                @NonNull
                @Override
                public T getOrigin() {
                    return hookMethod;
                }

                @Override
                public void unhook() {
                    HookBridge.unhookMethod(true, hookMethod, callback);
                }
            };
        }
        throw new HookFailedError("Cannot hook " + hookMethod);
    }

    private static class Callback<T extends Executable> extends XC_MethodHook {

        private final Injector injector;
        private final LSPosedHookCallback<T> callback = new LSPosedHookCallback<>();

        Callback(Injector injector, int priority) {
            super(priority);
            this.injector = injector;
        }

        private void refresh(XC_MethodHook.MethodHookParam<?> param) {
            callback.method     = param.method;
            callback.thisObject = param.thisObject;
            callback.args       = param.args;
            callback.result     = param.result;
            callback.throwable  = param.throwable;
            callback.isSkipped  = param.returnEarly;
        }

        private void restore(MethodHookParam<?> param) {
            param.args = callback.args;
            param.result = callback.result;
            param.throwable = callback.throwable;
            param.returnEarly = callback.isSkipped;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam<?> param) throws Throwable {
            refresh(param);
            if (injector instanceof Injector.Hook x) {
                x.inject(callback, callback.args);
                restore(param);
            } else if (injector instanceof Injector.PreInjector x) {
                x.inject(callback, callback.args);
                restore(param);
            }
        }

        @Override
        protected void afterHookedMethod(MethodHookParam<?> param) throws Throwable {
            refresh(param);
            if (injector instanceof Injector.Hook x) {
                x.inject(callback, callback.result, callback.throwable);
                restore(param);
            } else if (injector instanceof Injector.PostInjector x) {
                x.inject(callback, callback.result, callback.throwable);
                restore(param);
            }
        }
    }

    public static <T extends Executable> XposedInterface.MethodUnhooker<T>
    hook(T hookMethod, int priority, Injector.PreInjector injector) {
        return inject(hookMethod, priority, injector);
    }
    public static <T extends Executable> XposedInterface.MethodUnhooker<T>
    hook(T hookMethod, int priority, Injector.PostInjector injector) {
        return inject(hookMethod, priority, injector);
    }
    public static <T extends Executable> XposedInterface.MethodUnhooker<T>
    hook(T hookMethod, int priority, Injector.Hook injector) {
        return inject(hookMethod, priority, injector);
    }

    private static <T extends Executable> XposedInterface.MethodUnhooker<T>
    inject(T hookMethod, int priority, Injector injector) {
        if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod);
        } else if (hookMethod.getDeclaringClass().getClassLoader() == LSPosedContext.class.getClassLoader()) {
            throw new IllegalArgumentException("Do not allow hooking inner methods");
        } else if (hookMethod.getDeclaringClass() == Method.class && hookMethod.getName().equals("invoke")) {
            throw new IllegalArgumentException("Cannot hook Method.invoke");
        } else if (injector == null) {
            throw new IllegalArgumentException("injector should not be null!");
        }

        var callback = new Callback<T>(injector, priority);

        if (HookBridge.hookMethod(false, hookMethod, LSPosedBridge.NativeHooker.class, priority, callback)) {
            return new XposedInterface.MethodUnhooker<>() {
                @NonNull
                @Override
                public T getOrigin() {
                    return hookMethod;
                }

                @Override
                public void unhook() {
                    HookBridge.unhookMethod(true, hookMethod, callback);
                }
            };
        }
        throw new HookFailedError("Cannot hook " + hookMethod);
    }

}
