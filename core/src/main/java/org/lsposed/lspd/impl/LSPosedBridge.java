package org.lsposed.lspd.impl;

import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.lsposed.lspd.nativebridge.HookBridge;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.robv.android.xposed.XposedBridge;
import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.errors.HookFailedError;

public class LSPosedBridge {

    private static final String TAG = "LSPosed-Bridge";

    private static final String castException = "Return value's type from hook callback does not match the hooked method";

    private static final Method getCause;
    private static final Class<?> objArrayClass = Object[].class;

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
        final Object thisObject;

        final int beforeParams;
        final int afterParams;

        public HookerCallback(@NonNull Method beforeInvocation, @NonNull Method afterInvocation, Object thisObject) {
            this.beforeInvocation = beforeInvocation;
            this.afterInvocation = afterInvocation;
            this.beforeParams = beforeInvocation.getParameterCount();
            this.afterParams = afterInvocation.getParameterCount();
            this.thisObject = thisObject;
        }
        public HookerCallback(@NonNull Method beforeInvocation, @NonNull Method afterInvocation) {
            this.beforeInvocation = beforeInvocation;
            this.afterInvocation = afterInvocation;
            this.beforeParams = beforeInvocation.getParameterCount();
            this.afterParams = afterInvocation.getParameterCount();
            this.thisObject = null;
        }
    }

    public static void log(String text) {
        Log.i(TAG, text);
    }

    public static void log(Throwable t) {
        String logStr = Log.getStackTraceString(t);
        Log.e(TAG, logStr);
    }

    @Nullable
    public static Pair<Method, Method> getInjectorMethod(@NonNull XposedInterface.Injector injector) {
        Method pre = null, post = null;
        for (Method m : injector.getClass().getDeclaredMethods()) {
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 2 && ps[0] == XposedInterface.BeforeHookContext.class && ps[1] == objArrayClass) {
                pre = m;
                continue;
            }
            if (ps.length == 3 && ps[0] == XposedInterface.AfterHookContext.class && ps[1] == Object.class && ps[2] == Throwable.class) {
                post = m;
            }
        }
        if (pre == null && post == null) {
            return null;
        }
        return new Pair<>(pre, post);
    }

    public static class NativeHooker<T extends Executable> {
        private final Object params;
        private final ClassLoader classLoader;

        private NativeHooker(Executable method, ClassLoader classLoader) {
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
            this.classLoader = classLoader;
        }

        // This method is quite critical. We should try not to use system methods to avoid
        // endless recursive
        public Object callback(Object[] args) throws Throwable {
            LSPosedHookContext context = new LSPosedHookContext();

            var array = ((Object[]) params);

            @SuppressWarnings("unchecked")
            var method = (T) array[0];
            var returnType = (Class<?>) array[1];
            var isStatic = (Boolean) array[2];

            context.origin = method;
            context.classLoader = classLoader;

            if (isStatic) {
                context.thisObject = null;
                context.args = args;
            } else {
                context.thisObject = args[0];
                context.args = new Object[args.length - 1];
                //noinspection ManualArrayCopy
                for (int i = 0; i < args.length - 1; ++i) {
                    context.args[i] = args[i + 1];
                }
            }

            Object[][] callbacksSnapshot = HookBridge.callbackSnapshot(HookerCallback.class, method);
            Object[] modernSnapshot = callbacksSnapshot[0];
            Object[] legacySnapshot = callbacksSnapshot[1];

            if (modernSnapshot.length == 0 && legacySnapshot.length == 0) {
                try {
                    return HookBridge.invokeOriginalMethod(method, context.thisObject, context.args);
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
                        ctxArray[beforeIdx] = hooker.beforeInvocation.invoke(hooker.thisObject);
                    } else {
                        ctxArray[beforeIdx] = hooker.beforeInvocation.invoke(hooker.thisObject, context);
                    }
                } catch (Throwable t) {
                    LSPosedBridge.log(t);

                    // reset result (ignoring what the unexpectedly exiting context did)
                    context.setResult(null);
                    context.isSkipped = false;
                    continue;
                }

                if (context.isSkipped) {
                    // skip remaining "before" callbacks and corresponding "after" callbacks
                    beforeIdx++;
                    break;
                }
            }

            if (!context.isSkipped && legacySnapshot.length != 0) {
                // TODO: Separate classloader
                legacy = new XposedBridge.LegacyApiSupport<>(context, legacySnapshot);
                legacy.handleBefore();
            }

            // call original method if not requested otherwise
            if (!context.isSkipped) {
                try {
                    var result = HookBridge.invokeOriginalMethod(method, context.thisObject, context.args);
                    context.setResult(result);
                } catch (InvocationTargetException e) {
                    var throwable = (Throwable) HookBridge.invokeOriginalMethod(getCause, e);
                    context.setThrowable(throwable);
                }
            }

            // call "after method" callbacks
            for (int afterIdx = beforeIdx - 1; afterIdx >= 0; afterIdx--) {
                Object lastResult = context.getResult();
                Throwable lastThrowable = context.getThrowable();
                var hooker = (HookerCallback) modernSnapshot[afterIdx];
                try {
                    if (hooker.afterParams == 0) {
                        hooker.afterInvocation.invoke(hooker.thisObject);
                    } else if (hooker.afterParams == 1) {
                        hooker.afterInvocation.invoke(hooker.thisObject, context);
                    } else {
                        hooker.afterInvocation.invoke(hooker.thisObject, context, ctxArray[afterIdx]);
                    }
                } catch (Throwable t) {
                    LSPosedBridge.log(t);

                    // reset to last result (ignoring what the unexpectedly exiting context did)
                    if (lastThrowable == null) {
                        context.setResult(lastResult);
                    } else {
                        context.setThrowable(lastThrowable);
                    }
                }
            }

            if (legacy != null) {
                legacy.handleAfter();
            }

            // return
            var t = context.getThrowable();
            if (t != null) {
                throw t;
            } else {
                var result = context.getResult();
                if (returnType != null && !returnType.isPrimitive() && !HookBridge.instanceOf(result, returnType)) {
                    throw new ClassCastException(castException);
                }
                return result;
            }
        }
    }

    @NonNull
    public static XposedInterface.Unhooker hook(@NonNull Method method, int priority, XposedInterface.Injector injector) {
        return doHook(method, priority, injector);
    }
    @NonNull
    public static XposedInterface.Unhooker hook(@NonNull Constructor<?> constructor, int priority, XposedInterface.Injector injector) {
        return doHook(constructor, priority, injector);
    }

    @NonNull
    private static XposedInterface.Unhooker
    doHook(@NonNull Executable hookMethod, int priority, XposedInterface.Injector injector) {
        if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod);
        } else if (hookMethod.getDeclaringClass().getClassLoader() == LSPosedContext.class.getClassLoader()) {
            throw new IllegalArgumentException("Do not allow hooking inner methods");
        } else if (hookMethod.getDeclaringClass() == Method.class && hookMethod.getName().equals("invoke")) {
            throw new IllegalArgumentException("Cannot hook Method.invoke");
        } else if (injector == null) {
            throw new IllegalArgumentException("injector should not be null!");
        }

        if (HookBridge.hookMethod(false, hookMethod, LSPosedBridge.NativeHooker.class, priority, injector)) {
            return new XposedInterface.Unhooker() {
                @NonNull
                @Override
                public Executable getOrigin() {
                    return hookMethod;
                }

                @NonNull
                @Override
                public XposedInterface.Injector getInjector() {
                    return injector;
                }

                @Override
                public void unhook() {
                    HookBridge.unhookMethod(false, hookMethod, injector);
                }
            };
        }
        throw new HookFailedError("Cannot hook " + hookMethod);
    }
}
