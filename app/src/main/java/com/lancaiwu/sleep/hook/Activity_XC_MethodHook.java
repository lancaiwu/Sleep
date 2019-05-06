package com.lancaiwu.sleep.hook;

import android.os.Environment;

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

    public Activity_XC_MethodHook(SettingBean settingBean, XC_LoadPackage.LoadPackageParam loadPackageParam, String timeStr) {
        this.settingBean = settingBean;
        this.timeStr = timeStr;
        this.loadPackageParam = loadPackageParam;
    }

    public Activity_XC_MethodHook(SettingBean settingBean, XC_LoadPackage.LoadPackageParam loadPackageParam) {
        this.settingBean = settingBean;
        this.loadPackageParam = loadPackageParam;
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

        if (settingBean != null &&settingBean.getAppBean() != null
                && settingBean.getAppBean().getPackageName() != null && loadPackageParam.packageName.startsWith(settingBean.getAppBean().getPackageName())) {
            // 白名单
            return;
        }

        TimerTask timerTask = new MyTimer(param.thisObject, loadPackageParam.packageName, getWaitTime());
        Timer timer = new Timer();
        timer.schedule(timerTask, 0, 100);
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

            if (startTime > endTime) {
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
            }
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
