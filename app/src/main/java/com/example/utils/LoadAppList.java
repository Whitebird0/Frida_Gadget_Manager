package com.example.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.example.frida_gadget_manager.PersistSettings;
import com.example.module.ModuleData;

import java.util.ArrayList;

import java.util.List;

public class LoadAppList {
    public static List<ModuleData> loadAllInstalledApps(Context context){
        PackageManager pm=context.getPackageManager();
        List<ModuleData> moduleDataList = new ArrayList<>();
        for (PackageInfo pkg : pm.getInstalledPackages(PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES)) {
            ModuleData moduleData = new ModuleData();
            ApplicationInfo app = pkg.applicationInfo;

            if (!app.enabled){
                continue;
            }
            //公开可用部分 #sourceDir的完整路径，包括资源和清单
            String apkPath =app.publicSourceDir;
            //获取应用程序名字
            String apkName = pm.getApplicationLabel(app).toString();
            if (TextUtils.isEmpty(apkPath)) {
                //此应用程序的基本 APK 的完整路径。
                apkPath = app.sourceDir;
            }
            if (app.metaData != null && (app.metaData.containsKey("x_module") || app.metaData.containsKey("x_droid"))) {
                continue;
            }
            if (!TextUtils.isEmpty(apkPath)){
                if (apkPath.startsWith("/data/app")){
                    moduleData.apkPath = apkPath;
                    moduleData.appName = apkName;
                    moduleData.pkgName = app.packageName;
                    moduleData.uid = app.uid + "";
                    moduleData.versionName = pkg.versionName;
                    moduleData.icon =app.loadIcon(pm);
                    moduleData.isWhitebird_Persist = PersistSettings.isEnablePersistFrida(app.packageName, PersistSettings.WHITEBIRD_PERSIST);
                    moduleDataList.add(moduleData);
                }
            }

        }
        return moduleDataList;
    }

}
