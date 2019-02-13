package com.lancaiwu.sleep.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.TimerTask;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by lancaiwu on 2019/1/23.
 */

public class MyTimer extends TimerTask {
    private long time;
    private Object context;
    private String packageName;

    public MyTimer(Object context, String packageName, long time) {
        this.time = time;
        this.context = context;
        this.packageName = packageName;
    }

    @Override
    public void run() {
        Log.i("lanc", "进程启动 " + time);
        try {
            if ((packageName.equals(Constants.MY_PACKAGE_NAME) || packageName.equals(Constants.XP_PACKAGE_NAME) || packageName.equals(Constants.SETTING_PACKAGE_NAME)) && time < 10000) {
                // 防止时间到了，还进来设置关闭
                System.exit(0);
                return;
            }
            Thread.sleep(time);
            if (context instanceof Context) {
                try {
                    //        Toast.makeText((Context) context, "时间已到,杜绝手机", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Thread.sleep(1000);
            XposedBridge.log("lanc  时间已到,杜绝手机");
            System.exit(0);
        } catch (InterruptedException e) {
        }
    }
}
