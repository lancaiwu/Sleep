package com.lancaiwu.sleep.hook;

import android.os.Bundle;

import com.lancaiwu.sleep.bean.SettingBean;
import com.lancaiwu.sleep.bean.TimeBean;
import com.lancaiwu.sleep.utils.Constants;
import com.lancaiwu.sleep.utils.MyTimer;
import com.lancaiwu.sleep.utils.NetUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


/**
 * Created by lancaiwu on 2019/1/21.
 */

public class Hook implements IXposedHookLoadPackage {
    SettingBean settingBean = null;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        hookMyApp(lpparam);

        hookOtherApp(lpparam);

    }

    private void hookOtherApp(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws ClassNotFoundException {
        XposedBridge.log("lanc  " + loadPackageParam.packageName + "   " + (loadPackageParam.appInfo == null || (loadPackageParam.appInfo.flags & 1) > 0 || (loadPackageParam.appInfo.flags & 129) != 0));

        if (loadPackageParam.appInfo == null || (loadPackageParam.appInfo.flags & 1) > 0 || (loadPackageParam.appInfo.flags & 129) != 0) {

            if (!loadPackageParam.packageName.equals(Constants.SETTING_PACKAGE_NAME)) {
                // 过滤 系统app
                return;
            }
        }

        // 忽略 支付宝
        if (loadPackageParam.packageName.equals("com.eg.android.AlipayGphone")) {
            return;
        }

        final String timeStr = NetUtils.getNetTime();

        Set hookAllStartActivityMethods = XposedBridge.hookAllMethods(Class.forName("android.app.Activity"), "onStart", new Activity_XC_MethodHook(settingBean, loadPackageParam, timeStr));

        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onCreate", Bundle.class, new Activity_XC_MethodHook(settingBean, loadPackageParam, timeStr));

        Set hookAllApplicationMethods = XposedBridge.hookAllMethods(Class.forName("android.app.Application"), "onCreate", new Activity_XC_MethodHook(settingBean, loadPackageParam, timeStr));

        if (loadPackageParam.packageName.equals(Constants.ZHIHU_PACKAGE_NAME)) {
            try {
                Set hookZhiHuApplicationMethods = XposedBridge.hookAllConstructors(Class.forName("java.net.URL"), new Activity_XC_MethodHook(settingBean, loadPackageParam, timeStr));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void hookMyApp(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        try {
            if (loadPackageParam.packageName.equals(Constants.MY_PACKAGE_NAME)) {
                XposedHelpers.findAndHookMethod(Constants.MY_PACKAGE_NAME + ".MainActivity", loadPackageParam.classLoader, "isEnableXP", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult(true);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
