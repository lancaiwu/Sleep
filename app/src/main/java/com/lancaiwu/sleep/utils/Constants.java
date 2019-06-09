package com.lancaiwu.sleep.utils;

/**
 * Created by lancaiwu on 2019/1/21.
 */

public class Constants {
    public static String MY_PACKAGE_NAME = "com.lancaiwu.sleep";
    public static String XP_PACKAGE_NAME = "de.robv.android.xposed.installer";
    public static String SETTING_PACKAGE_NAME = "com.android.settings";
    public static String ZHIHU_PACKAGE_NAME = "com.zhihu.android";
    /**
     * 程序文件保存路径
     */
    public static String FILE_PATH = "/sleep";
    /**
     * 配置名称
     */
    public static String FILE_NAME = "/setting.json";

    public static String SP_NAME = "SLEEP_DATA";
    //
    public static final int HOOK_TYPE_ACTIVITY_CREATE=1;
    public static final int HOOK_TYPE_ACTIVITY_START=2;
    public static final int HOOK_TYPE_APPLICATION_CREATE=3;
    public static final int HOOK_TYPE_URL_CREATE=4;



}
