package com.example.hookdetection;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(() -> {
            if (isHook(this)) {
                // 在这里处理检测到Hook的情况，例如显示警告、收集日志等
                Log.wtf("HookDetection", "Hook detected on the device.");
            }
            boolean isHookDetected = HookDetectionUtils.isHookFrameworkInstalled(this);
            if (isHookDetected) {
                // 检测到Hook框架，根据您的需求执行相应操作
                Log.wtf("HookDetection", "Xposed on the device.");
            }
        }).start();
    }
    private static boolean findHookAppName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> applicationInfoList = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);
        int flag = 0;
        for (ApplicationInfo applicationInfo : applicationInfoList) {
            if (applicationInfo.packageName.equals("de.robv.android.xposed.installer")) {
                Log.wtf("HookDetection", "Xposed found on the system.");
                flag = 1;
            }
            if (applicationInfo.packageName.equals("com.saurik.substrate")) {
                Log.wtf("HookDetection", "Substrate found on the system.");
                flag = 1;
            }
        }

        if(flag==1){
            return true;
        }else{
            Log.wtf("HookDetection", "No hook detected");
            return false;
        }

    }

    private static boolean findHookAppFile() {
        try {
            Set<String> libraries = new HashSet<String>();
            String mapsFilename = "/proc/" + android.os.Process.myPid() + "/maps";
            BufferedReader reader = new BufferedReader(new FileReader(mapsFilename));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".so") || line.endsWith(".jar")) {
                    int n = line.lastIndexOf(" ");
                    libraries.add(line.substring(n + 1));
                }
            }
            reader.close();
            for (String library : libraries) {
                if (library.contains("com.saurik.substrate")) {
                    Log.wtf("HookDetection", "Substrate shared object found: " + library);
                    return true;
                }
                if (library.contains("XposedBridge.jar")) {
                    Log.wtf("HookDetection", "Xposed JAR found: " + library);
                    return true;
                }
            }
        } catch (Exception e) {
            Log.wtf("HookDetection", e.toString());
        }
        return false;
    }
    private static boolean findHookStack() {
        try {
            throw new Exception("findhook");
        } catch (Exception e) {

            // 读取栈信息
            // for(StackTraceElement stackTraceElement : e.getStackTrace()) {
            // Log.wtf("HookDetection", stackTraceElement.getClassName() + "->"+
            // stackTraceElement.getMethodName());
            // }

            int zygoteInitCallCount = 0;
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                if (stackTraceElement.getClassName().equals("com.android.internal.os.ZygoteInit")) {
                    zygoteInitCallCount++;
                    if (zygoteInitCallCount == 2) {
                        Log.wtf("HookDetection", "Substrate is active on the device.");
                        return true;
                    }
                }
                if (stackTraceElement.getClassName().equals("com.saurik.substrate.MS$2")
                        && stackTraceElement.getMethodName().equals("invoked")) {
                    Log.wtf("HookDetection", "A method on the stack trace has been hooked using Substrate.");
                    return true;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge")
                        && stackTraceElement.getMethodName().equals("main")) {
                    Log.wtf("HookDetection", "Xposed is active on the device.");
                    return true;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge")
                        && stackTraceElement.getMethodName().equals("handleHookedMethod")) {
                    Log.wtf("HookDetection", "A method on the stack trace has been hooked using Xposed.");
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isHook(Context context) {
        if (findHookAppName(context) || findHookAppFile() || findHookStack()) {
            return true;
        }
        return false;
    }
    // 将您的isHook和相关检测方法放在这里
}

