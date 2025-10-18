package org.lsposed.lspd.impl;

import android.util.Log;

import androidx.annotation.NonNull;

import org.lsposed.lspd.nativebridge.HookBridge;

import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import io.github.libxposed.api.Injector.Handler;
import io.github.libxposed.api.Hook;
import io.github.libxposed.api.Post;
import io.github.libxposed.api.Pre;
import io.github.libxposed.api.Stateful;
import io.github.libxposed.api.errors.HookFailedError;

public class LSPosedBridge {

    private static final String TAG = "LSPosedPro/Bridge";

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
            LSPosedHookContext context = new LSPosedHookContext();

            var array = ((Object[]) params);

            @SuppressWarnings("unchecked")
            var method = (T) array[0];
            var returnType = (Class<?>) array[1];
            var isStatic = (Boolean) array[2];

            context.target = method;

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
                        ctxArray[beforeIdx] = hooker.beforeInvocation.invoke(null);
                    } else {
                        ctxArray[beforeIdx] = hooker.beforeInvocation.invoke(null, context);
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
                Object lastResult = context.result;
                Throwable lastThrowable = context.throwable;
                var hooker = (HookerCallback) modernSnapshot[afterIdx];
                try {
                    if (hooker.afterParams == 0) {
                        hooker.afterInvocation.invoke(null);
                    } else if (hooker.afterParams == 1) {
                        hooker.afterInvocation.invoke(null, context);
                    } else {
                        hooker.afterInvocation.invoke(null, context, ctxArray[afterIdx]);
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
            var t = context.throwable;
            if (t != null) {
                throw t;
            } else {
                var result = context.result;
                if (returnType != null && !returnType.isPrimitive() && !HookBridge.instanceOf(result, returnType)) {
                    throw new ClassCastException(castException);
                }
                return result;
            }
        }
    }

    public static class NativeInjector<T extends Executable> {
        private final Object target;

        private NativeInjector(Executable method) {
            var isStatic = Modifier.isStatic(method.getModifiers());
            Object returnType;
            if (method instanceof Method) {
                returnType = ((Method) method).getReturnType();
            } else {
                returnType = null;
            }
            target = new Object[]{
                method,
                returnType,
                isStatic,
            };
        }

        // This method is quite critical. We should try not to use system methods to avoid
        // endless recursive
        public Object callback(Object[] args) throws Throwable {
            LSPosedHookContext context = new LSPosedHookContext();

            var targets = ((Object[]) target);

            @SuppressWarnings("unchecked")
            var method = (T) targets[0];
            var returnType = (Class<?>) targets[1];
            var isStatic = (Boolean) targets[2];

            context.target = method;

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
            Object[] legacySnapshot = callbacksSnapshot[1];

            if (legacySnapshot.length == 0) {
                try {
                    return HookBridge.invokeOriginalMethod(method, context.thisObject, context.args);
                } catch (InvocationTargetException ite) {
                    throw (Throwable) HookBridge.invokeOriginalMethod(getCause, ite);
                }
            }

            int beforeIdx;

            for (beforeIdx = 0; beforeIdx < legacySnapshot.length; beforeIdx++) {
                var cb = (XC_MethodHook) legacySnapshot[beforeIdx];
                if (cb instanceof Stateful x && !x.isReady()) {
                    continue;
                }
                try {
                    if (cb instanceof HookCallback x) {
                        x.inject(context, context.args);
                    } else if (cb instanceof PreCallback x) {
                        x.inject(context, context.args);
                    } else {
                        var param = context.export();
                        cb.callBeforeHookedMethod(param);
                        context.update(param);
                    }
                } catch (Throwable t) {
                    XposedBridge.log("throw error while Pre.inject(...)");
                    XposedBridge.log(t);

                    // reset result (ignoring what the unexpectedly exiting callback did)
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

            for (int afterIdx = beforeIdx - 1; afterIdx >= 0; afterIdx--) {
                Object lastResult = context.result;
                Throwable lastThrowable = context.throwable;
                var cb = (XC_MethodHook) legacySnapshot[afterIdx];
                if (cb instanceof Stateful x && !x.isReady()) {
                    continue;
                }
                try {
                    if (cb instanceof HookCallback x) {
                        x.inject(context, context.result, context.throwable);
                    } else if (cb instanceof PostCallback x) {
                        x.inject(context, context.result, context.throwable);
                    } else {
                        var param = context.export();
                        cb.callAfterHookedMethod(param);
                        context.update(param);
                    }
                } catch (Throwable t) {
                    XposedBridge.log("throw error while Post.inject(...)");
                    XposedBridge.log(t);

                    // reset to last result (ignoring what the unexpectedly exiting callback did)
                    if (lastThrowable == null) {
                        context.setResult(lastResult);
                    } else {
                        context.setThrowable(lastThrowable);
                    }
                }
            }

            for (int afterIdx = legacySnapshot.length - 1; afterIdx >= 0; afterIdx--) {
                var cb = (XC_MethodHook) legacySnapshot[afterIdx];
                if (cb instanceof Stateful x && !x.isReady()) {
                    continue;
                }
                if (cb instanceof Callback x) {
                    x.done(context);
                }
            }

            // return
            var t = context.throwable;
            if (t != null) {
                throw t;
            }

            var result = context.result;
            if (returnType != null && !returnType.isPrimitive() && !HookBridge.instanceOf(result, returnType)) {
                throw new ClassCastException(castException);
            }
            return result;
        }
    }


    private abstract sealed static class Callback extends XC_MethodHook implements Stateful.Default {

        protected final LSPosedHookContext context = new LSPosedHookContext();

        public Callback(int priority) {
            super(priority);
        }

        protected void assign(XC_MethodHook.MethodHookParam<?> param) {
            context.target     = (Executable) param.method;
            context.thisObject = param.thisObject;
            context.args       = param.args;
            context.result     = param.result;
            context.throwable  = param.throwable;
            context.isSkipped  = param.returnEarly;
        }

        protected void update(XC_MethodHook.MethodHookParam<?> param) {
            param.args = context.args;
            param.result = context.result;
            param.throwable = context.throwable;
            param.returnEarly = context.isSkipped;
        }

        public abstract void done(LSPosedHookContext context);

        // 添加状态

        private final AtomicReference<Stateful.State> state = new AtomicReference<>(Stateful.State.Ready);

        @Override
        public Stateful.State getState() {
            return state.get();
        }

        @Override
        public void setState(Stateful.State state) {
            this.state.set( state );
        }
    }

    private static final class PreCallback extends Callback {

        private final Pre injector;

        PreCallback(@NonNull Pre injector) {
            super(injector.getPriority());
            this.injector = injector;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam<?> param) {
            assign(param);
            injector.inject(context, context.args);
            update(param);
        }

        public void inject(LSPosedHookContext context, Object[] args) {
            injector.inject(context, args);
        }

        public void done(LSPosedHookContext context) {
            injector.done(context);
        }
    }

    private static final class PostCallback extends Callback {

        private final Post injector;
        private final LSPosedHookContext context = new LSPosedHookContext();

        PostCallback(@NonNull Post injector) {
            super(injector.getPriority());
            this.injector = injector;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam<?> param) {
            assign(param);
            injector.inject(context, context.result, context.throwable);
            update(param);
        }

        public void inject(LSPosedHookContext context, Object result, Throwable throwable) {
            injector.inject(context, result, throwable);
        }

        public void done(LSPosedHookContext context) {
            injector.done(context);
        }
    }

    private static final class HookCallback extends Callback {

        private final Hook injector;

        HookCallback(@NonNull Hook injector) {
            super(injector.getPriority());
            this.injector = injector;
        }

        @Override
        protected void beforeHookedMethod(MethodHookParam<?> param) {
            assign(param);
            injector.inject(context, context.args);
            update(param);
        }

        @Override
        protected void afterHookedMethod(MethodHookParam<?> param) {
            assign(param);
            injector.inject(context, context.result, context.throwable);
            update(param);
        }

        public void inject(LSPosedHookContext context, Object[] args) {
            injector.inject(context, args);
        }

        public void inject(LSPosedHookContext context, Object result, Throwable throwable) {
            injector.inject(context, result, throwable);
        }

        public void done(LSPosedHookContext context) {
            injector.done(context);
        }
    }

    private static class StateHandler<T extends Executable> implements Handler<T> {

        private final AtomicReference<Stateful.State> state = new AtomicReference<>(Stateful.State.Ready);
        private final T origin;
        private final Callback callback;

        public StateHandler(T origin, Callback callback) {
            this.origin = origin;
            this.callback = callback;
        }

        @NonNull
        @Override
        public T getOrigin() {
            return origin;
        }

        @Override
        public void cancel() {
            HookBridge.unhookMethod(false, origin, callback);
        }

        @Override
        public Stateful.State getState() {
            return state.get();
        }

        @Override
        public void setState(Stateful.State state) {
            this.state.set( state );
        }
    }

    @NonNull
    public static <T extends Executable> Handler<T> hook(@NonNull T hookMethod, Pre injector) {
        if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod);
        } else if (hookMethod.getDeclaringClass().getClassLoader() == LSPosedContext.class.getClassLoader()) {
            throw new IllegalArgumentException("Do not allow hooking inner methods");
        } else if (hookMethod.getDeclaringClass() == Method.class && hookMethod.getName().equals("invoke")) {
            throw new IllegalArgumentException("Cannot hook Method.invoke");
        } else if (injector == null) {
            throw new IllegalArgumentException("injector should not be null!");
        }

        var callback = new PreCallback(injector);

        if (HookBridge.hookMethod(false, hookMethod, LSPosedBridge.NativeInjector.class, injector.getPriority(), callback)) {
            return new StateHandler<>(hookMethod,  callback);
        }
        throw new HookFailedError("Cannot hook " + hookMethod);
    }
    @NonNull
    public static <T extends Executable> Handler<T> hook(@NonNull T hookMethod, Post injector) {
        if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod);
        } else if (hookMethod.getDeclaringClass().getClassLoader() == LSPosedContext.class.getClassLoader()) {
            throw new IllegalArgumentException("Do not allow hooking inner methods");
        } else if (hookMethod.getDeclaringClass() == Method.class && hookMethod.getName().equals("invoke")) {
            throw new IllegalArgumentException("Cannot hook Method.invoke");
        } else if (injector == null) {
            throw new IllegalArgumentException("injector should not be null!");
        }

        var callback = new PostCallback(injector);

        if (HookBridge.hookMethod(false, hookMethod, LSPosedBridge.NativeInjector.class, injector.getPriority(), callback)) {
            return new StateHandler<>(hookMethod, callback);
        }
        throw new HookFailedError("Cannot hook " + hookMethod);
    }
    @NonNull
    public static <T extends Executable> Handler<T> hook(@NonNull T hookMethod, Hook injector) {
        if (Modifier.isAbstract(hookMethod.getModifiers())) {
            throw new IllegalArgumentException("Cannot hook abstract methods: " + hookMethod);
        } else if (hookMethod.getDeclaringClass().getClassLoader() == LSPosedContext.class.getClassLoader()) {
            throw new IllegalArgumentException("Do not allow hooking inner methods");
        } else if (hookMethod.getDeclaringClass() == Method.class && hookMethod.getName().equals("invoke")) {
            throw new IllegalArgumentException("Cannot hook Method.invoke");
        } else if (injector == null) {
            throw new IllegalArgumentException("injector should not be null!");
        }

        var callback = new HookCallback(injector);

        if (HookBridge.hookMethod(false, hookMethod, LSPosedBridge.NativeInjector.class, injector.getPriority(), callback)) {
            return new StateHandler<>(hookMethod, callback);
        }
        throw new HookFailedError("Cannot hook " + hookMethod);
    }

}
