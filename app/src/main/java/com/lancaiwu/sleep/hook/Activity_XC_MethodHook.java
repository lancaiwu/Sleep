package com.lancaiwu.sleep.hook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lancaiwu.sleep.bean.AppBean;
import com.lancaiwu.sleep.bean.SettingBean;
import com.lancaiwu.sleep.bean.TimeBean;
import com.lancaiwu.sleep.utils.Constants;
import com.lancaiwu.sleep.utils.IOUtils;
import com.lancaiwu.sleep.utils.MyTimer;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by lancaiwu on 2019/1/28.
 */

public class Activity_XC_MethodHook extends XC_MethodHook {
    private SettingBean settingBean;
    private XC_LoadPackage.LoadPackageParam loadPackageParam;
    private String timeStr;
    private Context context;
    private int type;

    public Activity_XC_MethodHook(int type, Context context, SettingBean settingBean, XC_LoadPackage.LoadPackageParam loadPackageParam, String timeStr) {
        this.type = type;
        this.settingBean = settingBean;
        this.timeStr = timeStr;
        this.loadPackageParam = loadPackageParam;
        this.context = context;
    }

    public Activity_XC_MethodHook(int type, Context context, SettingBean settingBean, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.type = type;
        this.settingBean = settingBean;
        this.loadPackageParam = loadPackageParam;
        this.context = context;
    }

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);

        getData();

        if (settingBean != null) {
            XposedBridge.log("lanc  " + new Gson().toJson(settingBean));
        }
        if (loadPackageParam.packageName.equals(Constants.SETTING_PACKAGE_NAME)) {
            if (settingBean != null && !settingBean.isEnable()) {
                settingBean = readFileConfig();
            }
        }

        if (settingBean != null && !settingBean.isEnable()) {
            // 没有开启
            return;
        }

        if (settingBean != null && settingBean.getAppBean() != null
                && settingBean.getAppBean().getPackageName() != null && loadPackageParam.packageName.startsWith(settingBean.getAppBean().getPackageName())) {
            // 白名单
            return;
        }

        final long waitTime = getWaitTime();
        Handler handler = new Handler(Looper.getMainLooper());
        if (10000 <waitTime && waitTime <= 300000 ) {
            Toast.makeText(((Context) param.thisObject), "五分钟内即将杜绝手机!!!" +loadPackageParam.processName, Toast.LENGTH_LONG).show();
        }
        if (waitTime <= 10000 ) {

            if (param.thisObject instanceof Application) {
                Toast.makeText(((Application) param.thisObject), "时间已到,杜绝手机" +loadPackageParam.processName, Toast.LENGTH_LONG).show();
            }

            if (param.thisObject instanceof Activity) {
                Toast.makeText(((Activity) param.thisObject), "时间已到,杜绝手机" +loadPackageParam.processName, Toast.LENGTH_LONG).show();
            }

            if (param.thisObject instanceof Context ) {
                Toast.makeText(((Context) param.thisObject), "时间已到,杜绝手机" +loadPackageParam.processName, Toast.LENGTH_LONG).show();
            }

            TimerTask timerTask = new MyTimer(type, param.thisObject, context, handler, loadPackageParam.packageName, waitTime);
            Thread thread = new Thread(timerTask);
            thread.start();
        } else {
            TimerTask timerTask = new MyTimer(type, param.thisObject, context, handler, loadPackageParam.packageName, waitTime);
            Timer timer = new Timer();
            timer.schedule(timerTask, 0, 20000);
        }

    }

    private long getWaitTime() {
        // 默认处于禁用状态时间
        long waitTime = 1000;
        if (timeStr != null) {
            Date date = new Date(Long.valueOf(timeStr));

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            long currentTime = calendar.getTimeInMillis();

            calendar.set(Calendar.HOUR_OF_DAY, settingBean.getStartTime().getHour());
            calendar.set(Calendar.MINUTE, settingBean.getStartTime().getMinute());

            long startTime = calendar.getTimeInMillis();

            calendar.set(Calendar.HOUR_OF_DAY, settingBean.getEndTime().getHour());
            calendar.set(Calendar.MINUTE, settingBean.getEndTime().getMinute());

            long endTime = calendar.getTimeInMillis();

            XposedBridge.log("lanc  " + "c:" + currentTime + " s:" + startTime + " e:" + endTime);

            if (startTime > endTime) {
                // 表示不是同一天，要跨天
                if (endTime > currentTime) {
                    // 表示处于 禁用期 第二天
                } else if (currentTime > startTime) {
                    // 表示处于 禁用期 第一天
                } else if (endTime < currentTime && currentTime < startTime) {
                    // 处于等待时间
                    waitTime = startTime - currentTime;
                }
            } else {
                // 同一天
                if (startTime > currentTime) {
                    waitTime = startTime - currentTime;
                } else if (endTime < currentTime) {
                    // 等待期 -- 等下一天
                    waitTime = startTime + 86400000L - currentTime;
                } else if (currentTime > startTime && currentTime < endTime) {
                    // 处于 禁用期
                }
            }


           /* if (startTime > endTime) {
                // 跨天了
                endTime = endTime + 86400000L;
            }

            // 处于等待期
            if (startTime > currentTime) {
                // 开始时间还没到
                waitTime = startTime - currentTime;
            } else if (currentTime > endTime) {
                // 跨天了
                startTime = startTime + 86400000L;
                waitTime = startTime - currentTime;
            }*/
        }
        return waitTime;
    }

    private void getData() {
        settingBean = new SettingBean();
        XSharedPreferences sharedPreferences = new XSharedPreferences(Constants.MY_PACKAGE_NAME, Constants.SP_NAME);
        settingBean.setEnable(sharedPreferences.getBoolean("isEnable", false));

        TimeBean startTimeBean = new TimeBean();
        startTimeBean.setHour(sharedPreferences.getInt("startTime_hour", 23));
        startTimeBean.setMinute(sharedPreferences.getInt("startTime_minute", 0));
        settingBean.setStartTime(startTimeBean);
        TimeBean endTimeBean = new TimeBean();

        endTimeBean.setHour(sharedPreferences.getInt("endTime_hour", 6));
        endTimeBean.setMinute(sharedPreferences.getInt("endTime_minute", 0));
        settingBean.setEndTime(endTimeBean);

        String appBeanStr = sharedPreferences.getString("white_list_app", null);
        if (appBeanStr != null) {
            settingBean.setAppBean(new Gson().fromJson(appBeanStr, AppBean.class));
        }
    }

    private SettingBean readFileConfig() {
        String text = IOUtils.readText(Environment.getExternalStorageDirectory().getAbsolutePath() + Constants.FILE_PATH + Constants.FILE_NAME);
        if (text == null || text.equals("")) {
            return null;
        } else {
            try {
                return new Gson().fromJson(text, SettingBean.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
