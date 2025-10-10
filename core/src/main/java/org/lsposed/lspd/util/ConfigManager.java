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
 * Copyright (C) 2021 LSPosed Contributors
 */

package org.lsposed.lspd.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.lsposed.lspd.receivers.LSPManagerServiceHolder;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static List<PackageInfo> appList;

    synchronized public static List<PackageInfo> getAppList(boolean force) throws RemoteException {
        if (appList == null || force) {
            appList = getInstalledPackagesFromAllUsers(PackageManager.GET_META_DATA | PackageManager.MATCH_UNINSTALLED_PACKAGES, true);
            PackageInfo system = null;
            for (var app : appList) {
                if ("android".equals(app.packageName)) {
                    var p = Parcel.obtain();
                    app.writeToParcel(p, 0);
                    p.setDataPosition(0);
                    system = PackageInfo.CREATOR.createFromParcel(p);
                    system.packageName = "system";
                    system.applicationInfo.packageName = system.packageName;
                    break;
                }
            }
            if (system != null) {
                appList.add(system);
            }
        }
        return appList;
    }

    @NonNull
    @Contract("_, _ -> new")
    public static List<PackageInfo> getInstalledPackagesFromAllUsers(int flags, boolean filterNoProcess) throws RemoteException {
        return new ArrayList<>(LSPManagerServiceHolder.getService().getInstalledPackagesFromAllUsers(flags, filterNoProcess).getList());
    }

}
