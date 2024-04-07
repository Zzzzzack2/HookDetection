package com.example.hookdetection;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import java.util.List;

public class HookDetectionUtils {

    /**
     * 检查系统中是否安装了Xposed Installer等已知的Hook框架应用。
     * @return 如果检测到已知的Hook框架应用，则返回true；否则返回false。
     */
    public static boolean isHookFrameworkInstalled(Context context) {

        PackageManager packageManager = context.getPackageManager();

        List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        // 定义已知的Hook框架应用的包名列表
        String[] knownHookApps = {
                "de.robv.android.xposed.installer", // Xposed Installer的包名
                "com.saurik.substrate", // Cydia Substrate的包名
                // 可以添加更多已知的Hook框架
        };


        for (ApplicationInfo app : installedApps) {
            for (String hookAppPackageName : knownHookApps) {
                if (hookAppPackageName.equals(app.packageName)) {
                    // 如果找到已知的Hook框架应用，返回true
                    return true;
                }
            }
        }

        // 如果遍历完成后没有找到已知的Hook框架应用，返回false
        return false;
    }
}
