package com.lancaiwu.sleep.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.TimerTask;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by lancaiwu on 2019/1/23.
 */

public class MyTimer extends TimerTask {
    private long time;
    private Context context;
    private String packageName;
    private Object thisObj;
    private Handler handler;
    private int type;

    public MyTimer(int type, Object thisObj, Context context, Handler handler, String packageName, long time) {
        this.time = time;
        this.context = context;
        this.thisObj = thisObj;
        this.packageName = packageName;
        this.handler = handler;
        this.type = type;
    }

    @Override
    public void run() {
        Log.i("lanc", "进程启动 " + time);
        try {
            if ((packageName.equals(Constants.MY_PACKAGE_NAME) || packageName.equals(Constants.XP_PACKAGE_NAME) || packageName.equals(Constants.SETTING_PACKAGE_NAME)) && time < 300000) {
                // 防止时间到了，还进来设置关闭
                System.exit(0);
                return;
            }
            Thread.sleep(time);
            XposedBridge.log("lanc  时间已到,杜绝手机  --  " + type);
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
