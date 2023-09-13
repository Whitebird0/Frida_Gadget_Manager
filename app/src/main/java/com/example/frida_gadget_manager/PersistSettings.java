package com.example.frida_gadget_manager;

import android.system.Os;

import com.example.utils.MyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class PersistSettings {

    private static final String TAG = "Whitebird_Aosp";
    private static final String SETTINGS_DIR = "/data/system/xsettings/tmp/persist";
    private static final String CONFIG_JS_DIR = "/data/system/xsettings/tmp/jscfg";
    public static final String WHITEBIRD_PERSIST = "whitebird_persist";

    public static boolean copyJSFileToAppJSPath(String srcjsFilePath, String dstJsPath) {
        try {
            File file = new File(srcjsFilePath);
            if (!file.exists()) {
                return false;
            }
            file = new File(dstJsPath);
            if (file.exists()) {
                file.delete();
            }

            FileInputStream fileInputStream = new FileInputStream(new File(srcjsFilePath));
            FileOutputStream fileOutputStream = new FileOutputStream(new File(dstJsPath));
            byte[] dataBytes = new byte[4 * 1024];
            int len = -1;
            while ((len = fileInputStream.read(dataBytes)) != -1) {
                fileOutputStream.write(dataBytes, 0, len);
                fileOutputStream.flush();
            }
            try {
                file = new File(dstJsPath);
                Os.chmod(file.getAbsolutePath(), 0775);
            } catch (Exception e) {
                e.printStackTrace();
            }
            fileOutputStream.close();
            fileInputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    public static boolean isAppJsPathExists(String pkgName) {
        File file = new File(CONFIG_JS_DIR + "/" + pkgName + "/config.js");
        return file.exists();
    }

    public static String getAppJSPath(String pkgName) {
        File file = new File(CONFIG_JS_DIR);
        if (!file.exists()) {
            file.mkdirs();
            try {
                Os.chmod(file.getAbsolutePath(), 0775);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        file = new File(CONFIG_JS_DIR + "/" + pkgName);
        if (!file.exists()) {
            file.mkdirs();
            try {
                Os.chmod(file.getAbsolutePath(), 0775);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        file = new File(CONFIG_JS_DIR + "/" + pkgName + "/config.js");
        return file.getAbsolutePath();
    }

    //判断app是否打开自动注入脚本功能
    @Deprecated
    public static boolean isEnableFridaInject(String pkgName) {
        File enableFile = new File(SETTINGS_DIR, pkgName + "/enable");
        return enableFile.exists();
    }

    /**********************判断是否开启持久化***************************/
    public static boolean setEnablePersist(String pkgName, String methodType, boolean isEnable) {
        File pkgFile = new File(SETTINGS_DIR, pkgName);
        if (!pkgFile.exists()) {
            try {
                boolean mkdirIsOk = pkgFile.mkdir();
                if(!mkdirIsOk) return false;
                Os.chmod(pkgFile.getAbsolutePath(), 0775);
                MyLog.d(TAG, "mkdir pkgFile success");
            } catch (Exception e) {
                MyLog.d(TAG, "mkdir pkgFile errror:" + pkgFile.getAbsolutePath());
                e.printStackTrace();
                return false;
            }
        }
        File enableFile = new File(pkgFile, methodType);
        if (isEnable) {
            if (!enableFile.exists()) {
                try {
                    boolean createIsOk = enableFile.createNewFile();
                    if(!createIsOk) return false;
                    Os.chmod(enableFile.getAbsolutePath(), 0775);
                    MyLog.d(TAG, "create enableFile success");
                } catch (Exception e) {
                    MyLog.d(TAG, "create enableFile errror:" + enableFile.getAbsolutePath());
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            if (enableFile.exists()) {
                try {
                    boolean deleteIsOk = enableFile.delete();
                    if(!deleteIsOk) return false;
                } catch (Exception e) {
                    MyLog.d(TAG, "delete enableFile errror:" + enableFile.getAbsolutePath());
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    //判断app是否打开自动注入脚本功能
    public static boolean isEnablePersistFrida(String pkgName, String methodType) {
        File enableFile = new File(SETTINGS_DIR, pkgName + "/" + methodType);
        return enableFile.exists();
    }
}
