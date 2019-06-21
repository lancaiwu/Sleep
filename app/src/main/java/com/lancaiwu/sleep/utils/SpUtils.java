package com.lancaiwu.sleep.utils;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lancaiwu.sleep.bean.AppBean;
import com.lancaiwu.sleep.bean.SettingBean;
import com.lancaiwu.sleep.bean.TimeBean;

import java.util.List;

public class SpUtils {
    public static SettingBean getSetting(SharedPreferences sharedPreferences) {
        SettingBean settingBean = new SettingBean();
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

        String appBeansStr = sharedPreferences.getString("white_list_apps", null);
        if (appBeansStr != null) {
            settingBean.setAppBeans((List<AppBean>) new Gson().fromJson(appBeansStr, new TypeToken<List<AppBean>>() {
            }.getType()));
        }
        return settingBean;
    }
}
