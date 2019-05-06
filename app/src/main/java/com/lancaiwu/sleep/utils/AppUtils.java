package com.lancaiwu.sleep.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lancaiwu.sleep.bean.AppBean;

import java.util.ArrayList;
import java.util.List;

public class AppUtils {
    public static List<AppBean> getThirdAppList(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        // 判断是否系统应用：
        //List<PackageInfo> apps = new ArrayList<PackageInfo>();
        List<AppBean> appBeans = new ArrayList<>();
        for (int i = 0; i < packageInfoList.size(); i++) {
            PackageInfo packageInfo = (PackageInfo) packageInfoList.get(i);
            //判断是否为系统预装的应用
            if ((packageInfo.applicationInfo.flags & packageInfo.applicationInfo.FLAG_SYSTEM) <= 0) {
                if (packageInfo.packageName.equals(Constants.MY_PACKAGE_NAME)
                || packageInfo.packageName.equals(Constants.SETTING_PACKAGE_NAME)
                        || packageInfo.packageName.equals(Constants.XP_PACKAGE_NAME)
                     ) {
                    continue;
                }
                AppBean appBean = new AppBean();
                // 第三方应用
                // apps.add(pak);
                appBean.setAppName(packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString());
                appBean.setPackageName(packageInfo.packageName);
                appBeans.add(appBean);
            } else {
                //系统应用
            }
        }
        return appBeans;

    }
}
