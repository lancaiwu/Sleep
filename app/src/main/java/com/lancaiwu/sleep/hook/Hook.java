package com.lancaiwu.sleep.hook;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.lancaiwu.sleep.bean.SettingBean;
import com.lancaiwu.sleep.utils.Constants;
import com.lancaiwu.sleep.utils.NetUtils;
import com.lancaiwu.sleep.utils.PerUtils;

import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;


/**
 * Created by lancaiwu on 2019/1/21.
 */

public class Hook implements IXposedHookLoadPackage {
    SettingBean settingBean = null;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        hookMyApp(lpparam);
      hook(lpparam);
    }

    private void hookOtherApp(final XC_LoadPackage.LoadPackageParam loadPackageParam, Context context,String timeStr) throws ClassNotFoundException {


     //   if (loadPackageParam.packageName.contains(":")) {
       //     // 子进程 不 销毁，省的 一直重启子进程
         //   return;
       // }

        if(!PerUtils.checkNetPer(context,loadPackageParam.packageName)){
            // 判断是否有网络权限
            // 没有网络权限
            return;
        }

        Log.e("lanc","sleep: "+loadPackageParam.processName);


        if(timeStr==null){
            Log.e("lanc","时间获取失败: "+loadPackageParam.packageName);
            return;
        }

        Set hookAllStartActivityMethods = XposedBridge.hookAllMethods(Class.forName("android.app.Activity"), "onStart", new Activity_XC_MethodHook(Constants.HOOK_TYPE_ACTIVITY_START,context,settingBean, loadPackageParam, timeStr));

        XposedHelpers.findAndHookMethod(android.app.Activity.class, "onCreate", Bundle.class, new Activity_XC_MethodHook(Constants.HOOK_TYPE_ACTIVITY_CREATE,context,settingBean, loadPackageParam, timeStr));

        Set hookAllApplicationMethods = XposedBridge.hookAllMethods(Class.forName("android.app.Application"), "onCreate", new Activity_XC_MethodHook(Constants.HOOK_TYPE_APPLICATION_CREATE,context,settingBean, loadPackageParam, timeStr));

        if (loadPackageParam.packageName.equals(Constants.ZHIHU_PACKAGE_NAME)) {
            try {
                Set hookZhiHuApplicationMethods = XposedBridge.hookAllConstructors(Class.forName("java.net.URL"), new Activity_XC_MethodHook(Constants.HOOK_TYPE_URL_CREATE,context,settingBean, loadPackageParam, timeStr));
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


    public void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam){
        XposedBridge.log("lanc  " + loadPackageParam.packageName + "   " + (loadPackageParam.appInfo == null || (loadPackageParam.appInfo.flags & 1) > 0 || (loadPackageParam.appInfo.flags & 129) != 0));

        if (loadPackageParam.appInfo == null || (loadPackageParam.appInfo.flags & 1) > 0 || (loadPackageParam.appInfo.flags & 129) != 0) {

            if (!loadPackageParam.packageName.equals(Constants.SETTING_PACKAGE_NAME)) {
                // 过滤 系统app、除了设置 app
                return;
            }
        }

        // 忽略 支付宝
        if (loadPackageParam.packageName.startsWith("com.eg.android.AlipayGphone")) {
            return;
        }

        final String timeStr = NetUtils.getNetTime();


        try {


            findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String versionName = null;
                    final Class<?> activityThread = XposedHelpers.findClass("android.app.ActivityThread", loadPackageParam.classLoader);
                    if (activityThread != null) {
                        Object currentActivityThread = XposedHelpers.callStaticMethod(activityThread, "currentActivityThread");
                        if (currentActivityThread != null) {
                            Context systemContext = (Context) XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
                            if (systemContext != null) {
                                PackageManager packageManager = systemContext.getPackageManager();
                                final PackageInfo packageInfo = packageManager.getPackageInfo(loadPackageParam.packageName, 0);
                                if (packageInfo != null) {
                                    Context context = (Context) param.args[0];

                                    hookOtherApp(loadPackageParam,context,timeStr);
                                }
                            }
                        }
                    }
                }
            });
        } catch (XposedHelpers.ClassNotFoundError classNotFoundError) {
            XposedBridge.log(classNotFoundError);
            return;
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}
