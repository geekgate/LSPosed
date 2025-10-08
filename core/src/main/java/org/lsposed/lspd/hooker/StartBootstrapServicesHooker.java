/*
 * This file is part of LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 EdXposed Contributors
 * Copyright (C) 2021 LSPosed Contributors
 */

package org.lsposed.lspd.hooker;

import static org.lsposed.lspd.util.Utils.logD;

import androidx.annotation.NonNull;

import org.lsposed.lspd.impl.LSPosedContext;
import org.lsposed.lspd.util.Hookers;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import io.github.libxposed.api.XposedInterface;

public class StartBootstrapServicesHooker implements XposedInterface.PreInjector {

    @Override
    public void inject(@NonNull XposedInterface.BeforeHookContext context, Object[] args){
        logD("SystemServer#startBootstrapServices() starts");

        try {
            XposedInit.loadedPackagesInProcess.add("android");

            XC_LoadPackage.LoadPackageParam param = new XC_LoadPackage.LoadPackageParam(XposedBridge.sLoadedPackageCallbacks);
            param.packageName = "android";
            param.processName = "android"; // it's actually system_server, but other functions return this as well
            param.classLoader = HandleSystemServerProcessHooker.systemServerCL;
            param.appInfo = null;
            param.isFirstApplication = true;
            XC_LoadPackage.callAll(param);

            LSPosedContext.callOnSystemServerLoaded(() -> HandleSystemServerProcessHooker.systemServerCL);
        } catch (Throwable t) {
            Hookers.logE("error when hooking startBootstrapServices", t);
        }
    }
}
